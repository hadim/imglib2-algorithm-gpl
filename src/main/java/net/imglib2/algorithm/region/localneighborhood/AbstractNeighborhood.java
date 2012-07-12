package net.imglib2.algorithm.region.localneighborhood;

import net.imglib2.ExtendedRandomAccessibleInterval;
import net.imglib2.IterableInterval;
import net.imglib2.IterableRealInterval;
import net.imglib2.Localizable;
import net.imglib2.Positionable;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.RealPositionable;
import net.imglib2.outofbounds.OutOfBoundsFactory;
import net.imglib2.view.Views;

/**
 * A {@link Positionable} {@link IterableInterval} that serves as a local neighborhood, 
 * e.g. in filtering operation.
 * <p>
 * This particular class implements a movable nD domain, defined by a <code>span long[]</code> array.
 * The <code>span</code> array is such that the size of the neighborhood in dimension 
 * <code>d</code> will be <code>2 x span[d] + 1</code>.
 */
public abstract class AbstractNeighborhood<T>  implements Positionable, IterableInterval<T>  {

	/** The pixel coordinates of the center of this regions. */
	protected final long[] center;
	/** The span of this rectangle, such that the size of the rectangle in dimension 
	 * <code>d</code> will be <code>2 x span[d] + 1</code>. */
	protected final long[] span;
	protected final ExtendedRandomAccessibleInterval<T, RandomAccessibleInterval<T>> extendedSource;
	protected final RandomAccessibleInterval<T> source;


	/*
	 * CONSTRUCTOR
	 */
	
	public AbstractNeighborhood(final RandomAccessibleInterval<T> source, final OutOfBoundsFactory<T, RandomAccessibleInterval<T>> outOfBounds) {
		this.source = source;
		this.extendedSource = Views.extend(source, outOfBounds);
		this.center = new long[source.numDimensions()];
		this.span = new long[source.numDimensions()];
	}
	
	
	
	/*
	 * METHODS
	 */
	
	/**
	 * Set the span of this neighborhood.
	 * <p>
	 * The neighborhood span is such that the size of the neighborhood in dimension 
	 * <code>d</code> will be <code>2 x span[d] + 1</code>.
	 * @param span  this array content will be copied to the neighborhood internal field.
	 */
	public void setSpan(long[] span) {
		for (int d = 0; d < span.length; d++) {
			this.span[ d ] = span[ d ];
		}
	}

	@Override
	public int numDimensions() {
		return extendedSource.numDimensions();
	}

	@Override
	public void fwd(int d) {
		center[ d ]++;
	}

	@Override
	public void bck(int d) {
		center[ d ]--;
	}

	@Override
	public void move(int distance, int d) {
		center[ d ] = center[ d ] + distance;
	}

	@Override
	public void move(long distance, int d) {
		center[ d ] = center[ d ] + distance;
	}

	@Override
	public void move(Localizable localizable) {
		for (int d = 0; d < center.length; d++) {
			center[ d ] += localizable.getLongPosition(d);
		}
	}

	@Override
	public void move(int[] distance) {
		for (int d = 0; d < center.length; d++) {
			center[ d ] += distance[ d ];
		}
	}

	@Override
	public void move(long[] distance) {
		for (int d = 0; d < center.length; d++) {
			center[ d ] += distance[ d ];
		}
	}

	@Override
	public void setPosition(Localizable localizable) {
		for (int d = 0; d < center.length; d++) {
			center[ d ] = localizable.getLongPosition(d);
		}
	}

	@Override
	public void setPosition(int[] position) {
		for (int d = 0; d < center.length; d++) {
			center[ d ] = position[ d ];
		}
	}

	@Override
	public void setPosition(long[] position) {
		for (int d = 0; d < center.length; d++) {
			center[ d ] = position[ d ];
		}
	}

	@Override
	public void setPosition(int position, int d) {
		center[ d ] = position;
	}

	@Override
	public void setPosition(long position, int d) {
		center[ d ] = position;
	}

	@Override
	public long size() {
		long size = 1;
		for (int d = 0; d < span.length; d++) {
			size *= (2 * span[d] + 1);
		}
		return size;
	}

	/**
	 * Return the element at the top-left corner of this nD rectangle.
	 */
	@Override
	public T firstElement() {
		RandomAccess<T> ra = source.randomAccess();
		for (int d = 0; d < span.length; d++) {
			ra.setPosition(center[d] - span[ d ], d);
		}
		return ra.get();
	}

	@Override
	public Object iterationOrder() {
		return this;
	}

	@Override
	@Deprecated
	public boolean equalIterationOrder(IterableRealInterval<?> f) {
		if (!(f instanceof RectangleNeighborhood)) {
			return false;
		}
		RectangleNeighborhood<?> otherRectangle = (RectangleNeighborhood<?>) f;
		if (otherRectangle.numDimensions() != numDimensions()) {
			return false;
		}
		for (int d = 0; d < span.length; d++) {
			if (otherRectangle.span[d] != span[d]) {
				return false;
			}
		}
		return true;
	}

	@Override
	public double realMin(int d) {
		return center[ d ] - span[ d ];
	}

	@Override
	public void realMin(double[] min) {
		for (int d = 0; d < center.length; d++) {
			min[ d ] = center[ d ] - span [ d ];
		}
		
	}

	@Override
	public void realMin(RealPositionable min) {
		for (int d = 0; d < center.length; d++) {
			min.setPosition(center[ d ] - span [ d ], d);
		}
	}

	@Override
	public double realMax(int d) {
		return center[ d ] + span[ d ];
	}

	@Override
	public void realMax(double[] max) {
		for (int d = 0; d < center.length; d++) {
			max[ d ] = center[ d ] + span [ d ];
		}
	}

	@Override
	public void realMax(RealPositionable max) {
		for (int d = 0; d < center.length; d++) {
			max.setPosition(center[ d ] + span [ d ], d);
		}
	}

	@Override
	public long min(int d) {
		return center[d] - span[d];
	}

	@Override
	public void min(long[] min) {
		for (int d = 0; d < center.length; d++) {
			min[ d ] = center[ d ] - span [ d ];
		}
	}

	@Override
	public void min(Positionable min) {
		for (int d = 0; d < center.length; d++) {
			min.setPosition(center[ d ] - span [ d ], d);
		}
	}

	@Override
	public long max(int d) {
		return center[ d ] + span[ d ];
	}

	@Override
	public void max(long[] max) {
		for (int d = 0; d < center.length; d++) {
			max[ d ] = center[ d ] + span [ d ];
		}
	}

	@Override
	public void max(Positionable max) {
		for (int d = 0; d < center.length; d++) {
			max.setPosition(center[ d ] + span [ d ], d);
		}
	}

	@Override
	public void dimensions(long[] dimensions) {
		for (int d = 0; d < span.length; d++) {
			dimensions[d] = 2 * span[d] + 1; 
		}
	}

	@Override
	public long dimension(int d) {
		return (2 * span[d] + 1);
	}

}