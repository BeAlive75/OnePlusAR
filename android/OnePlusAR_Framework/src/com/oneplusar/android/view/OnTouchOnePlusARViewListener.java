package com.oneplusar.android.view;

import android.view.MotionEvent;

/**
 * On touch listener to detect when a
 * {@link com.oneplusar.android.world.OnePlusARObject OnePlusARObject} has been
 * touched on the {@link com.oneplusar.android.view.OnePlusARGLSurfaceView
 * OnePlusARGLSurfaceView}.
 */
public interface OnTouchOnePlusARViewListener {

	/**
	 * Use
	 * {@link OnePlusARGLSurfaceView#getOnePlusARObjectsOnScreenCoordinates(float, float, java.util.ArrayList)}
	 * to get the object touched:<br>
	 * 
	 * <pre>
	 * {@code
	 * float x = event.getX();
	 * float y = event.getY();
	 * ArrayList<OnePlusARObject> geoObjects = new ArrayList<OnePlusARObject>();
	 * oneplusarView.getARObjectOnScreenCoordinates(x, y, geoObjects);
	 * ...
	 * Now we iterate the ArrayList. The first element will be the closest one to the user
	 * ...
	 * }
	 * </pre>
	 * 
	 * @param event
	 * @param oneplusarView
	 */
	public void onTouchOnePlusARView(MotionEvent event, OnePlusARGLSurfaceView oneplusarView);

}