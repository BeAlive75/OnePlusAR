package com.oneplusar.android.view;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.ListAdapter;

import com.oneplusar.android.util.math.geom.Point2;
import com.oneplusar.android.world.OnePlusARObject;

/**
 * Adapter to attach views to the
 * {@link com.oneplusar.android.world.OnePlusARObject OnePlusARObject}. This is an
 * example of how to use the adapter:
 * 
 * <pre>
 * <code>
 * private class CustomOnePlusARViewAdapter extends OnePlusARViewAdapter {
 * 
 * 		LayoutInflater inflater;
 * 
 * 		public CustomOnePlusARViewAdapter(Context context) {
 * 			super(context);
 * 			inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 * 		}
 * 
 * 		@Override
 * 		public View getView(OnePlusARObject oneplusarObject, View recycledView, ViewGroup parent) {
 * 			if (!showViewOn.contains(oneplusarObject)) {
 * 				return null;
 * 			}
 * 			if (recycledView == null) {
 * 				recycledView = inflater.inflate(R.layout.oneplusar_object_view, null);
 * 			}
 * 
 * 			TextView textView = (TextView) recycledView.findViewById(R.id.titleTextView);
 * 			textView.setText(oneplusarObject.getName());
 * 			Button button = (Button) recycledView.findViewById(R.id.button);
 * 			button.setOnClickListener(AttachViewToGeoObjectActivity.this);
 * 
 *          // Once the view is ready we specify the position
 * 			setPosition(oneplusarObject.getScreenPositionTopRight());
 * 
 * 			return recycledView;
 * 		}
 * 	}
 * </code>
 * </pre>
 * 
 * Then when the adapter is ready we can set it in the
 * {@link com.oneplusar.android.fragment.OnePlusARFragment OnePlusARFragment}:
 * 
 * <code>
 * <pre>
 * CustomOnePlusARViewAdapter customOnePlusARViewAdapter = new CustomOnePlusARViewAdapter(this); 
 * mOnePlusARFragment.setOnePlusARViewAdapter(customOnePlusARViewAdapter);
 * </code> </pre>
 */
public abstract class OnePlusARViewAdapter {

	Queue<ViewGroup> mReusedViews;
	Queue<ViewGroup> mNewViews;

	ViewGroup mParentView;
	Point2 mNewPosition;
	Context mContext;

	final LayoutParams mLayoutParams;

	public OnePlusARViewAdapter(Context context) {
		mReusedViews = new LinkedList<ViewGroup>();
		mNewViews = new LinkedList<ViewGroup>();
		mContext = context;
		mLayoutParams = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
	}

	void processList(final List<OnePlusARObject> list, ViewGroup parent, final OnePlusARGLSurfaceView glSurface) {
		mParentView = parent;

		mParentView.post(new Runnable() {
			@Override
			public void run() {
				for (OnePlusARObject oneplusarObject : list) {

					if (oneplusarObject.getScreenPositionCenter().z > 1) {
						continue;
					}
					CustomLayout recycledParent = (CustomLayout) mReusedViews.poll();
					glSurface.fillOnePlusARObjectPositions(oneplusarObject);
					View toRecycle = null;

					if (recycledParent != null && recycledParent.getChildCount() > 0) {
						toRecycle = recycledParent.getChildAt(0);
					}

					View view = getView(oneplusarObject, toRecycle, mParentView);

					boolean added = false;
					// Check if the recyclable view has been used, otherwise add
					// it to the queue to recycle it
					if ((toRecycle != view || view == null) && toRecycle != null) {
						// Store it again to recycle it
						mReusedViews.add(recycledParent);
						added = true;
					}

					// Check if the view has a parent, if not create it
					if (view != null && (recycledParent == null || view.getParent() != recycledParent)) {
						CustomLayout parentLayout = new CustomLayout(mContext);
						parentLayout.addView(view, mLayoutParams);
						if (!added) {
							mReusedViews.add(recycledParent);
						}
						recycledParent = parentLayout;
					}

					if (view != null) {
						mNewViews.add(recycledParent);
						if (recycledParent.getParent() == null) {
							mParentView.addView(recycledParent, mLayoutParams);
						}
						recycledParent.setPosition((int) mNewPosition.x, (int) mNewPosition.y);
					}
				}

				removeUnusedViews();
				Queue<ViewGroup> tmp = mNewViews;
				mNewViews = mReusedViews;
				mReusedViews = tmp;
				mNewPosition = null;
			}
		});
	}

	/**
	 * Get {@link Context}.
	 * 
	 * @return
	 */
	protected Context getContext() {
		return mContext;
	}

	/**
	 * Set the screen position of the view. When the view is created use this
	 * method to specify the position on the screen.
	 * 
	 * 
	 * @param position
	 */
	protected void setPosition(Point2 position) {
		mNewPosition = position;
	}

	private void removeUnusedViews() {
		while (!mReusedViews.isEmpty()) {
			View view = mReusedViews.poll();
			mParentView.removeView(view);
		}
	}

	/**
	 * Override this method to create your own views from the
	 * {@link com.oneplusar.android.world.OnePlusARObject OnePlusARObject}. The
	 * usage of this adapter is very similar to the {@link ListAdapter}.
	 * 
	 * <pre>
	 * <code>
	 * private class CustomOnePlusARViewAdapter extends OnePlusARViewAdapter {
	 * 
	 * 		LayoutInflater inflater;
	 * 
	 * 		public CustomOnePlusARViewAdapter(Context context) {
	 * 			super(context);
	 * 			inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	 * 		}
	 * 
	 * 		@Override
	 * 		public View getView(OnePlusARObject oneplusarObject, View recycledView, ViewGroup parent) {
	 * 			if (!showViewOn.contains(oneplusarObject)) {
	 * 				return null;
	 * 			}
	 * 			if (recycledView == null) {
	 * 				recycledView = inflater.inflate(R.layout.oneplusar_object_view, null);
	 * 			}
	 * 
	 * 			TextView textView = (TextView) recycledView.findViewById(R.id.titleTextView);
	 * 			textView.setText(oneplusarObject.getName());
	 * 			Button button = (Button) recycledView.findViewById(R.id.button);
	 * 			button.setOnClickListener(AttachViewToGeoObjectActivity.this);
	 * 
	 *          // Once the view is ready we specify the position
	 * 			setPosition(oneplusarObject.getScreenPositionTopRight());
	 * 
	 * 			return recycledView;
	 * 		}
	 * 	}
	 * </code>
	 * </pre>
	 * 
	 * @param oneplusarObject
	 * @param recycledView
	 * @param parent
	 * @return
	 */
	public abstract View getView(OnePlusARObject oneplusarObject, View recycledView, ViewGroup parent);
}
