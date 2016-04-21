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
package com.oneplusar.android.world;

import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.List;

import android.content.Context;
import android.location.Location;

import com.oneplusar.android.opengl.colision.MeshCollider;
import com.oneplusar.android.plugin.Plugable;
import com.oneplusar.android.plugin.WorldPlugin;
import com.oneplusar.android.util.cache.BitmapCache;
import com.oneplusar.android.util.math.Distance;
import com.oneplusar.android.util.math.geom.Plane;
import com.oneplusar.android.util.math.geom.Point3;
import com.oneplusar.android.util.math.geom.Ray;
import com.oneplusar.android.util.math.geom.Vector3;

/**
 * Container that holds all the
 * {@link com.oneplusar.android.world.OnePlusARObject OnePlusARObject} to be
 * rendered in the augmented reality world.
 */
public class World implements Plugable<WorldPlugin> {

	/**
	 * Default list type.
	 */
	public static final int LIST_TYPE_DEFAULT = 0;

	/** Image uri prefix for the default images */
	public static final String URI_PREFIX_DEFAULT_IMAGE = "com.oneplusar_default_type";

	private float ZERO = 1e-8f;
	private Object mLock = new Object();

	private List<OnePlusARObjectList> mOnePlusARObjectLists;
	private double mLongitude, mLatitude, mAltitude;

	private Context mContext;
	private BitmapCache mBitmapHolder;
	private String mDefaultBitmap;

	protected List<WorldPlugin> plugins;
	protected Object lockplugins = new Object();

	/**
	 * Create an instance of the augmented reality world.
	 * 
	 * @param context
	 */
	public World(Context context) {
		mContext = context;
		mBitmapHolder = BitmapCache.initialize(mContext.getResources(), getClass().getName(), true);
		createOnePlusARObjectListArray();
		plugins = new ArrayList<WorldPlugin>(DEFAULT_PLUGINS_CAPACITY);
	}

	/**
	 * Notify the world that the application has been resumed.
	 */
	public void onResume() {
		synchronized (lockplugins) {
			for (WorldPlugin plugin : plugins) {
				plugin.onResume();
			}
		}
	}

	/**
	 * Notify the world that the application has been paused.
	 */
	public void onPause() {
		synchronized (lockplugins) {
			for (WorldPlugin plugin : plugins) {
				plugin.onPause();
			}
		}
	}

	protected Context getContext() {
		return mContext;
	}

	/**
	 * Get the bitmap cache used to load all the images.
	 * 
	 * @return
	 */
	public BitmapCache getBitmapCache() {
		return mBitmapHolder;
	}

	private void createOnePlusARObjectListArray() {
		mOnePlusARObjectLists = new ArrayList<OnePlusARObjectList>();
		mOnePlusARObjectLists.add(new OnePlusARObjectList(LIST_TYPE_DEFAULT, this));

	}

	/**
	 * Add a {@link WorldPlugin} to the {@link World}. If the plugin exist it
	 * will not be added again.
	 */
	public void addPlugin(WorldPlugin plugin) {
		synchronized (lockplugins) {
			if (!plugins.contains(plugin)) {
				plugins.add(plugin);
			}
		}
		plugin.setup(this);
	}

	/**
	 * Remove existing plugin.
	 * 
	 * @param plugin
	 *            plugin to be removed
	 * @return True if the plugin has been removed, false otherwise
	 */
	@Override
	public boolean removePlugin(WorldPlugin plugin) {
		boolean removed = false;
		synchronized (lockplugins) {
			removed = plugins.remove(plugin);
		}
		if (removed) {
			plugin.onDetached();
		}
		return removed;
	}

	@Override
	public void removeAllPlugins() {
		synchronized (lockplugins) {
			for (WorldPlugin plugin : plugins) {
				removePlugin(plugin);
			}
		}
	}

	@Override
	public WorldPlugin getFirstPlugin(Class<? extends WorldPlugin> pluginClass) {
		synchronized (lockplugins) {
			for (WorldPlugin plugin : plugins) {
				if (pluginClass.isInstance(plugin)) {
					return plugin;
				}
			}
		}
		return null;
	}

	@Override
	public boolean containsAnyPlugin(Class<? extends WorldPlugin> pluginClass) {
		return getFirstPlugin(pluginClass) != null;
	}

	@Override
	public boolean containsPlugin(WorldPlugin plugin) {
		synchronized (lockplugins) {
			return plugins.contains(plugin);
		}
	}

	@Override
	public List<WorldPlugin> getAllPugins(Class<? extends WorldPlugin> pluginClass) {
		ArrayList<WorldPlugin> result = new ArrayList<WorldPlugin>(5);
		return getAllPlugins(pluginClass, result);
	}

	@Override
	public List<WorldPlugin> getAllPlugins(Class<? extends WorldPlugin> pluginClass, List<WorldPlugin> result) {
		synchronized (lockplugins) {
			for (WorldPlugin plugin : plugins) {
				if (pluginClass.isInstance(plugin)) {
					result.add(plugin);
				}
			}
		}
		return result;
	}

	@Override
	public List<WorldPlugin> getAllPlugins() {
		synchronized (lockplugins) {
			return new ArrayList<WorldPlugin>(plugins);
		}
	}

	/**
	 * Add a {@link com.oneplusar.android.world.OnePlusARObject OnePlusARObject} to
	 * the default list in the world.
	 * 
	 * @param oneplusarObject
	 */
	public final synchronized void addOnePlusARObject(OnePlusARObject oneplusarObject) {
		addOnePlusARObject(oneplusarObject, LIST_TYPE_DEFAULT);
	}

	/**
	 * Add a {@link com.oneplusar.android.world.OnePlusARObject OnePlusARObject} to
	 * the specified list in the world.
	 * 
	 * @param oneplusarObject
	 */
	public synchronized void addOnePlusARObject(OnePlusARObject oneplusarObject, int worldListType) {
		if (oneplusarObject == null) {
			return;
		}
		synchronized (mLock) {
			OnePlusARObjectList listTmp = getOnePlusARObjectList(worldListType);
			if (listTmp == null) {
				listTmp = new OnePlusARObjectList(worldListType, this);
				mOnePlusARObjectLists.add(listTmp);
				synchronized (lockplugins) {
					for (WorldPlugin plugin : plugins) {
						plugin.onOnePlusARObjectListCreated(listTmp);
					}
				}
			}
			oneplusarObject.setWorldListType(worldListType);
			listTmp.add(oneplusarObject);
			synchronized (lockplugins) {
				for (WorldPlugin plugin : plugins) {
					plugin.onOnePlusARObjectAdded(oneplusarObject, listTmp);
				}
			}
		}
	}

	/**
	 * Remove a {@link com.oneplusar.android.world.OnePlusARObject OnePlusARObject}
	 * form the World. To do this, the function <code>getWorldListType</code> is
	 * used.
	 * 
	 * @param oneplusarObject
	 * @return True if the object has been removed, false otherwise
	 */
	public synchronized boolean remove(OnePlusARObject oneplusarObject) {
		synchronized (mLock) {
			OnePlusARObjectList listTmp = getOnePlusARObjectList(oneplusarObject.getWorldListType());
			if (listTmp != null) {
				listTmp.remove(oneplusarObject);
				synchronized (lockplugins) {
					for (WorldPlugin plugin : plugins) {
						plugin.onOnePlusARObjectRemoved(oneplusarObject, listTmp);
					}
				}
				oneplusarObject.onRemoved();
				return true;
			}
			return false;
		}
	}

	/**
	 * Force the world to remove all the removed
	 * {@link com.oneplusar.android.world.OnePlusARObject OnePlusARObject}.
	 */
	public synchronized void forceProcessRemoveQueue() {
		if (mOnePlusARObjectLists.size() > 0) {
			synchronized (mLock) {
				for (int i = 0; i < mOnePlusARObjectLists.size(); i++) {
					mOnePlusARObjectLists.get(i).forceRemoveObjectsInQueue();
				}
			}
		}
	}

	/**
	 * Clean all the data stored in the world object
	 */
	public synchronized void clearWorld() {
		synchronized (mLock) {
			mOnePlusARObjectLists.clear();
			mBitmapHolder.clean();
		}
	}

	/**
	 * Get the user's longitude
	 * 
	 * @return
	 */
	public double getLongitude() {
		return mLongitude;
	}

	/**
	 * Get the user's altitude
	 * 
	 * @return
	 */
	public double getAltitude() {
		return mAltitude;
	}

	/**
	 * Get the user's latitude
	 * 
	 * @return
	 */
	public double getLatitude() {
		return mLatitude;
	}

	/**
	 * Set user geo position.
	 * 
	 * @param latitude
	 * @param longitude
	 * @param altitude
	 */
	public void setGeoPosition(double latitude, double longitude, double altitude) {
		this.mLatitude = latitude;
		this.mLongitude = longitude;
		this.mAltitude = altitude;
		synchronized (lockplugins) {
			for (WorldPlugin plugin : plugins) {
				plugin.onGeoPositionChanged(latitude, longitude, altitude);
			}
		}
	}

	/**
	 * Set user geo position.
	 * 
	 * @param latitude
	 * @param longitude
	 */
	public final void setGeoPosition(double latitude, double longitude) {
		setGeoPosition(latitude, longitude, mAltitude);
	}

	/**
	 * Set user geo position using the {@link Location} object. The altitude is
	 * not used because it is a big source of issues, the accuracy is too bad.
	 * 
	 * @param location
	 */
	public void setLocation(Location location) {

		if (location == null) {
			return;
		}
		// We do not set the altitude because it is a big source of issues, the
		// accuracy is too bad
		setGeoPosition(location.getLatitude(), location.getLongitude());

	}

	/**
	 * Set the default image of the world. This default image is used when the
	 * {@link com.oneplusar.android.world.OnePlusARObject OnePlusARObject} image is
	 * not available.
	 * 
	 * @param imageResId
	 *            Default image.
	 */
	public synchronized void setDefaultImage(int imageResId) {
		setDefaultImage(BitmapCache.normalizeURI(imageResId));
	}

	/**
	 * Set the default image of the world. This default image is used when the
	 * {@link com.oneplusar.android.world.OnePlusARObject OnePlusARObject} image is
	 * not available.
	 * 
	 * @param uri
	 *            Default image.
	 */
	public synchronized void setDefaultImage(String uri) {
		mDefaultBitmap = uri;
		synchronized (lockplugins) {
			for (WorldPlugin plugin : plugins) {
				plugin.onDefaultImageChanged(uri);
			}
		}
	}

	/**
	 * Set the default image for the specified list type. This image is used
	 * when there are not any image loaded for a
	 * {@link com.oneplusar.android.world.OnePlusARObject OnePlusARObject}.
	 * 
	 * @param uri
	 *            The default image
	 * @param type
	 *            The type of the list to set the bitmap
	 * @return The URI of the image loaded or null if the image has not been
	 *         loaded
	 */
	public synchronized boolean setDefaultImage(String uri, int type) {
		OnePlusARObjectList list = getOnePlusARObjectList(type);
		if (list != null) {
			list.setDefaultImageUri(uri);
			return true;
		}
		return false;
	}

	/**
	 * Set the default image for the specified list type. This image is used
	 * when there are no images loaded for a
	 * {@link com.oneplusar.android.world.OnePlusARObject OnePlusARObject}.
	 * 
	 * @param imageResource
	 *            The default image reference.
	 * @param type
	 *            The type of the list to set the image.
	 * @return true if the image has been loaded properly, false otherwise.
	 */
	public synchronized boolean setDefaultBitmap(int imageResource, int type) {
		return setDefaultImage(BitmapCache.normalizeURI(imageResource), type);
	}

	/**
	 * Get the default image URI of the specified list.
	 * 
	 * @param type
	 *            the type of the list
	 * @return
	 */
	public synchronized String getDefaultImage(int type) {
		OnePlusARObjectList list = getOnePlusARObjectList(type);
		if (list != null && list.getDefaultImageUri() != null) {
			return list.getDefaultImageUri();
		}
		return mDefaultBitmap;
	}

	/**
	 * Get the default image URI of the world
	 * 
	 * @return
	 */
	public synchronized String getDefaultImage() {
		return mDefaultBitmap;
	}

	public int checkIntersectionPlane(Plane plane, Point3 position, Vector3 direction, double[] lamda,
			Vector3 pNormal) {

		double DotProduct = direction.dotProduct(plane.getNormal());
		double l2;

		// determine if ray parallel to plane
		if ((DotProduct < ZERO) && (DotProduct > -ZERO))
			return 0;

		Vector3 substract = new Vector3(plane.getPoint());
		substract.subtract(position);
		l2 = (plane.getNormal().dotProduct(substract)) / DotProduct;

		if (l2 < -ZERO)
			return 0;

		pNormal.set(plane.getNormal());
		lamda[0] = l2;
		return 1;
	}

	/**
	 * Get the {@link com.oneplusar.android.world.OnePlusARObjectList
	 * OnePlusARObjectList} for the specified type.
	 * 
	 * @return
	 */
	public OnePlusARObjectList getOnePlusARObjectList(int type) {
		OnePlusARObjectList list = null;
		for (int i = 0; i < mOnePlusARObjectLists.size(); i++) {
			list = mOnePlusARObjectLists.get(i);
			if (list.getType() == type) {
				return list;
			}
		}
		return null;

	}

	/**
	 * Get the container that holds all the
	 * {@link com.oneplusar.android.world.OnePlusARObjectList OnePlusARObjectList}
	 * in the {@link World}
	 * 
	 * @return The list of the lists
	 */
	public List<OnePlusARObjectList> getOnePlusARObjectLists() {
		return mOnePlusARObjectLists;
	}

	/**
	 * Get all the {@link com.oneplusar.android.world.OnePlusARObject
	 * OnePlusARObject} that collide with the {@link com.oneplusar.android.util.math.geom.Ray}.
	 * 
	 * @param ray
	 *            The ray to use for the collision calculus
	 * 
	 * @param oneplusarObjectsOutput
	 *            The {@link ArrayList} that will store the objects sorted by
	 *            proximity. This list will be cleaned before.
	 * @param maxDistance
	 *            Max distance to consider if a {@link com.oneplusar.android.world.OnePlusARObject} can
	 *            collide (in meters).
	 */
	public void getOnePlusARObjectsCollideRay(Ray ray, ArrayList<OnePlusARObject> oneplusarObjectsOutput,
			float maxDistance) {

		oneplusarObjectsOutput.clear();
		OnePlusARObjectList oneplusarList = null;

		try {
			for (int i = 0; i < mOnePlusARObjectLists.size(); i++) {
				oneplusarList = mOnePlusARObjectLists.get(i);
				if (oneplusarList != null) {
					for (int j = 0; j < oneplusarList.size(); j++) {

						OnePlusARObject oneplusarObject = oneplusarList.get(j);
						if (oneplusarObject == null) {
							continue;
						}

						if (oneplusarObject instanceof GeoObject) {
							GeoObject go = (GeoObject) oneplusarObject;
							double dst = Distance.calculateDistanceMeters(go.getLongitude(),
									go.getLatitude(), getLongitude(), getLatitude());
							if (dst > maxDistance) {
								continue;
							}
						}

						MeshCollider collisionDetector = oneplusarObject.getMeshCollider();

						Point3 point = collisionDetector.getIntersectionPoint(ray);

						if (point != null) {
							oneplusarObjectsOutput.add(oneplusarObject);
						}

					}
				}
			}
			if (oneplusarObjectsOutput.size() != 0) {
				sortGeoObjectByDistanceFromCenter(oneplusarObjectsOutput);
			}
		} catch (ConcurrentModificationException e) {
			getOnePlusARObjectsCollideRay(ray, oneplusarObjectsOutput, maxDistance);
		}
	}

	public static List<OnePlusARObject> sortGeoObjectByDistanceFromCenter(List<OnePlusARObject> vec) {
		boolean sorted = false;
		while (!sorted) {
			sorted = true;
			for (int i = 0; i < vec.size() - 1; i++) {
				OnePlusARObject go1 = vec.get(i);
				OnePlusARObject go2 = vec.get(i + 1);
				if (go2.getDistanceFromUser() < go1.getDistanceFromUser()) {
					sorted = false;
					vec.set(i, go2);
					vec.set(i + 1, go1);
				}
				// Vector3 vec1 = new Vector3(go1.getPosition());
				// Vector3 vec2 = new Vector3(go2.getPosition());
				//
				// if (vec2.module() < vec1.module()) {
				// sorted = false;
				// vec.set(i, go2);
				// vec.set(i + 1, go1);
				// }
			}
		}
		return vec;
	}
}
