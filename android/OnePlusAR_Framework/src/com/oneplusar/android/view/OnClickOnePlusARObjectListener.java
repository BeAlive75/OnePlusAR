package com.oneplusar.android.view;

import java.util.ArrayList;

import com.oneplusar.android.world.OnePlusARObject;

/**
 * On click listener to detect when a
 * {@link com.oneplusar.android.world.OnePlusARObject OnePlusARObject} has been
 * clicked on the {@link com.oneplusar.android.view.OnePlusARGLSurfaceView
 * OnePlusARGLSurfaceView}.
 */
public interface OnClickOnePlusARObjectListener {

	/**
	 * This method is called when the user click on a {@link com.oneplusar.android.world.OnePlusARObject OnePlusARObject}
	 * 
	 * @param oneplusarObjects
	 *            All the {@link com.oneplusar.android.world.OnePlusARObject OnePlusARObject} that collide with the ray
	 *            generated by the user click. If no object have been clicked
	 *            the {@link ArrayList} will be empty
	 */
	public void onClickOnePlusARObject(ArrayList<OnePlusARObject> oneplusarObjects);

}