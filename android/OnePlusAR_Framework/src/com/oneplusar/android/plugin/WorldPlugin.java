/*
 * Copyright (C) 2014 OnePlusAR
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.oneplusar.android.plugin;

import com.oneplusar.android.world.OnePlusARObject;
import com.oneplusar.android.world.OnePlusARObjectList;
import com.oneplusar.android.world.World;

/**
 * Base interface to create a plugin for a
 * {@link com.oneplusar.android.world.World World}.
 */
public interface WorldPlugin extends Plugin {

	/**
	 * This method is invoked when the plugin is removed.
	 */
	public void onDetached();

	/**
	 * Check if the plugin is attached.
	 * 
	 * @return
	 */
	public boolean isAttached();

	/**
	 * Setup the plugin according to the world.
	 * 
	 * @param world
	 *            The world that loads the plugin
	 * 
	 */
	public void setup(World world);

	/**
	 * This method is invoked when a new
	 * {@link com.oneplusar.android.world.OnePlusARObject OnePlusARObject} is added
	 * to the world. Use this method to add specific plugin's to the
	 * {@link com.oneplusar.android.world.OnePlusARObject OnePlusARObject}.
	 * 
	 * @param oneplusarObject
	 * @param oneplusarObjectList
	 */
	public void onOnePlusARObjectAdded(OnePlusARObject oneplusarObject, OnePlusARObjectList oneplusarObjectList);

	/**
	 * This method is invoked when a
	 * {@link com.oneplusar.android.world.OnePlusARObject OnePlusARObject} is
	 * removed from the world. Use this method to remove the plugin's that are
	 * not needed.
	 * 
	 * @param oneplusarObject
	 * @param oneplusarObjectList
	 */
	public void onOnePlusARObjectRemoved(OnePlusARObject oneplusarObject, OnePlusARObjectList oneplusarObjectList);

	/**
	 * This method is invoked when a new
	 * {@link com.oneplusar.android.world.OnePlusARObjectList OnePlusARObjectList}
	 * is created.
	 */
	public void onOnePlusARObjectListCreated(OnePlusARObjectList oneplusarObjectList);

	/**
	 * This method is invoked when the world is cleaned.
	 */
	public void onWorldCleaned();

	/**
	 * This method is invoked when the position is changed.
	 * 
	 * @param latitude
	 * @param longitude
	 * @param altitude
	 */
	public void onGeoPositionChanged(double latitude, double longitude, double altitude);

	/**
	 * This method is invoked when the default image for all the
	 * {@link com.oneplusar.android.world.OnePlusARObject OnePlusARObject} is set.
	 * 
	 * @param uri
	 */
	public void onDefaultImageChanged(String uri);

	/**
	 * Called when the activity has been paused.
	 */
	public void onPause();

	/**
	 * Called when the activity has been resumed.
	 */
	public void onResume();

}
