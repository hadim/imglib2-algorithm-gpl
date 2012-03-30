package net.imglib2.algorithm.region.localneighborhood;

import static org.junit.Assert.*;
import net.imglib2.Cursor;
import net.imglib2.RandomAccess;
import net.imglib2.img.Img;
import net.imglib2.img.ImgFactory;
import net.imglib2.img.array.ArrayImgFactory;
import net.imglib2.type.numeric.integer.UnsignedByteType;

import org.junit.Test;

public class DomainCursorTest {

	private static final int DIM = 100;
	private static final int VAL = 1;


	@Test
	public final void testBehavior() {

		final ImgFactory< UnsignedByteType > imgFactory = new ArrayImgFactory<UnsignedByteType>();
		Img<UnsignedByteType> image = imgFactory.create(new int[] { DIM, DIM, DIM }, new UnsignedByteType());

		long[] center = new long[] { 50, 50 , 50 }; // the middle
		long[] span = new long[] { 30, 30, 0 }; // a single plane in the middle 

		RandomAccess<UnsignedByteType> ra = image.randomAccess();
		ra.setPosition(center);

		// Write into the image
		DomainCursor<UnsignedByteType> cursor = new DomainCursor<UnsignedByteType>(ra, span);
		while (cursor.hasNext()) {
			cursor.next().set(new UnsignedByteType(VAL));
		}
		
		// Test the image is as expected
		long[] position = new long[image.numDimensions()]; 
		Cursor<UnsignedByteType> ic = image.localizingCursor();
		boolean inside;
		int test;
		while(ic.hasNext()) {
			ic.fwd();
			ic.localize(position);
			
			inside = true;
			for (int i = 0; i < position.length; i++) {
				if (position[i] < center[i]-span[i] || position[i] > center[i]+span[i]) {
					inside = false;
					break;
				}
			}
			
			if (inside) {
				test = VAL;
			} else {
				test = 0;
			}
			
			assertEquals(test, ic.get().get());
			
		}
		

	}
}