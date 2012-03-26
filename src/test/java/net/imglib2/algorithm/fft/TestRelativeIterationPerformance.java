package net.imglib2.algorithm.fft;

import ij.ImageJ;

import java.io.File;

import net.imglib2.Cursor;
import net.imglib2.ExtendedRandomAccessibleInterval;
import net.imglib2.RandomAccess;
import net.imglib2.algorithm.Benchmark;
import net.imglib2.algorithm.OutputAlgorithm;
import net.imglib2.exception.IncompatibleTypeException;
import net.imglib2.img.Img;
import net.imglib2.img.ImgFactory;
import net.imglib2.img.array.ArrayImgFactory;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.io.ImgIOException;
import net.imglib2.io.ImgOpener;
import net.imglib2.outofbounds.OutOfBoundsRandomAccess;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.view.Views;

public class TestRelativeIterationPerformance<T extends RealType<T>> implements Benchmark, OutputAlgorithm<Img<FloatType>> {

	private long processingTime;
	private Img<T> input;
	private Img<FloatType> output;
	
	public IterationMethod method = IterationMethod.RANDOM_ACCESS;
	
	public static enum IterationMethod {
		RANDOM_ACCESS,
		TRANSLATE_VIEW
	}
	

	/*
	 * CONSTRUCTOR
	 */
	
	public TestRelativeIterationPerformance(Img<T> input) {
		this.input = input;
		try {
			output = input.factory().imgFactory(new FloatType()).create( input, new FloatType() );
		} catch (IncompatibleTypeException e) {
			e.printStackTrace();
		}

	}
	
	/*
	 * METHODS
	 */
	

	@Override
	public boolean checkInput() {
		return true;
	}

	@Override
	public boolean process() {
		long start = System.currentTimeMillis();

		switch (method) {
		case RANDOM_ACCESS:
			iterateWithRandoAccessible();
			break;
		case TRANSLATE_VIEW:
			iterateWithViews();
			break;
		}
		
		processingTime = System.currentTimeMillis() - start;
		
		return true;
	}
	
	
	
	private void iterateWithViews() {
		
		ExtendedRandomAccessibleInterval<T, Img<T>> extended = Views.extendMirrorSingle(input);
		
		Cursor<T> northCursor = Views.iterable(Views.interval(Views.translate(extended, new long[] {0, -1}), input) ).cursor();
		Cursor<T> northEastCursor = Views.iterable(Views.interval(Views.translate(extended, new long[] {1, -1}), input) ).cursor();
		Cursor<T> eastCursor = Views.iterable(Views.interval(Views.translate(extended, new long[] {1, 0}), input) ).cursor();
		Cursor<T> southEastCursor = Views.iterable(Views.interval(Views.translate(extended, new long[] {1, 1}), input) ).cursor();
		Cursor<T> southCursor = Views.iterable(Views.interval(Views.translate(extended, new long[] {0, 1}), input) ).cursor();
		Cursor<T> southWestCursor = Views.iterable(Views.interval(Views.translate(extended, new long[] {-1, 1}), input) ).cursor();
		Cursor<T> westCursor = Views.iterable(Views.interval(Views.translate(extended, new long[] {-1, 0}), input) ).cursor();
		Cursor<T> northWestCursor = Views.iterable(Views.interval(Views.translate(extended, new long[] {-1, 1}), input) ).cursor();
		
		Cursor<T> cursor = input.localizingCursor();
		RandomAccess<FloatType> oc = output.randomAccess();
		
		float I, In, Ine, Ie, Ise, Is, Isw, Iw, Inw;
		while (cursor.hasNext()) {
			
			I 	= cursor.next().getRealFloat();
			In 	= northCursor.next().getRealFloat();
			Ine = northEastCursor.next().getRealFloat();
			Ie 	= eastCursor.next().getRealFloat();
			Ise = southEastCursor.next().getRealFloat();
			Is 	= southCursor.next().getRealFloat();
			Isw	= southWestCursor.next().getRealFloat();
			Iw 	= westCursor.next().getRealFloat();
			Inw	= northWestCursor.next().getRealFloat();
			
			oc.setPosition(cursor);
			oc.get().set( I - 1/8f * (In+Ine+Ie+Ise+Is+Isw+Iw+Inw));
		}
		
	}
	
	
	private void iterateWithRandoAccessible() {
		

		OutOfBoundsRandomAccess<T> ra = Views.extendMirrorSingle(input).randomAccess();
		Cursor<T> cursor = input.localizingCursor();
		RandomAccess<FloatType> oc = output.randomAccess();
		
		float I, In, Ine, Ie, Ise, Is, Isw, Iw, Inw;
		
		while (cursor.hasNext()) {
			
			cursor.fwd();
			oc.setPosition(cursor);
			ra.setPosition(cursor);
			
			I = cursor.get().getRealFloat();
			ra.bck(1);
			In = ra.get().getRealFloat();
			ra.fwd(0);
			Ine = ra.get().getRealFloat();
			ra.fwd(1);
			Ie = ra.get().getRealFloat();
			ra.fwd(1);
			Ise = ra.get().getRealFloat();
			ra.bck(0);
			Is = ra.get().getRealFloat();
			ra.bck(0);
			Isw = ra.get().getRealFloat();
			ra.bck(1);
			Iw = ra.get().getRealFloat();
			ra.bck(1);
			Inw = ra.get().getRealFloat();

			oc.get().set( I - 1/8f * (In+Ine+Ie+Ise+Is+Isw+Iw+Inw));
			
		}
		
	}
	
	

	@Override
	public String getErrorMessage() {
		return null;
	}

	@Override
	public Img<FloatType> getResult() {
		return output;
	}

	@Override
	public long getProcessingTime() {
		return processingTime;
	}
	

	/*
	 * MAIN METHOD
	 */

	public static <T extends RealType<T> & NativeType< T >> void  main(String[] args) throws ImgIOException, IncompatibleTypeException {
		
//		File file = new File( "E:/Users/JeanYves/Desktop/Data/Y.tif");
		File file = new File( "/Users/tinevez/Desktop/Data/Y.tif");
		int niter = 500;
		
		// Open file in imglib2
		ImgFactory< ? > imgFactory = new ArrayImgFactory< T >();
		Img< T > image = new ImgOpener().openImg( file.getAbsolutePath(), imgFactory );

		// Display it via ImgLib using ImageJ
		new ImageJ();
		ImageJFunctions.show( image );

		// Init algo
		TestRelativeIterationPerformance<T> algo = new TestRelativeIterationPerformance<T>(image);

		algo.method = IterationMethod.RANDOM_ACCESS;
		
		System.out.println("With random access:");
		long totalTime = 0;
		for (int i = 0; i < niter; i++) {
			algo.process();
			totalTime += algo.getProcessingTime();
		}
		ImageJFunctions.show(algo.getResult());
		System.out.println(String.format("Time taken: %.2f ms/iteration.", (float) totalTime / niter));
		long width = image.dimension(0);
		long height = image.dimension(1);
		System.out.println(String.format("or: %.2f µs/pixel.", 1000f * totalTime / ((float) niter * width * height)));
		
		
		algo.method = IterationMethod.TRANSLATE_VIEW;
		
		System.out.println();
		System.out.println("With translated views:");
		totalTime = 0;
		for (int i = 0; i < niter; i++) {
			algo.process();
			totalTime += algo.getProcessingTime();
		}
		ImageJFunctions.show(algo.getResult());
		System.out.println(String.format("Time taken: %.2f ms/iteration.", (float) totalTime / niter));
		System.out.println(String.format("or: %.2f µs/pixel.", 1000f * totalTime / ((float) niter * width * height)));
		

	}
	
}
