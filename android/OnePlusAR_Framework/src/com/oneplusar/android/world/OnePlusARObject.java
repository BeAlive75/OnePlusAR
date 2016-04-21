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
import java.util.List;

import com.oneplusar.android.opengl.colision.MeshCollider;
import com.oneplusar.android.opengl.colision.SquareMeshCollider;
import com.oneplusar.android.opengl.renderable.Renderable;
import com.oneplusar.android.opengl.renderable.SquareRenderable;
import com.oneplusar.android.opengl.renderer.ARRenderer;
import com.oneplusar.android.opengl.texture.Texture;
import com.oneplusar.android.plugin.OnePlusARObjectPlugin;
import com.oneplusar.android.plugin.Plugable;
import com.oneplusar.android.util.cache.BitmapCache;
import com.oneplusar.android.util.math.geom.Point3;

/**
 * Basic object to be used with augmented reality. This class contains all the
 * needed information to be used in the AR {@link World}.
 */
public class OnePlusARObject implements Plugable<OnePlusARObjectPlugin> {

	private Long mId;
	private int mTypeList;
	private Texture mTexture;
	private String mImageUri;
	private String mName;
	private boolean mVisible;
	private Renderable mRenderable;
	private Point3 mPosition;
	private Point3 mAngle;
	private boolean mFaceToCamera;
	private MeshCollider mMeshCollider;
	private double mDistanceFromUser;
	private Point3 mScreenPositionTopLeft, mScreenPositionTopRight, mScreenPositionBottomLeft,
			mScreenPositionBottomRight, mScreenPositionCenter;
	private Point3 mTopLeft, mBottomLeft, mBottomRight, mTopRight;

	/** This fields contains all the loaded plugins. */
	protected List<OnePlusARObjectPlugin> plugins;
	/** Use this lock to access the plugins field. */
	protected Object lockPlugins = new Object();

	/**
	 * Create an instance of a {@link com.oneplusar.android.world.OnePlusARObject
	 * OnePlusARObject} with an unique ID
	 * 
	 * @param id
	 *            Unique ID
	 */
	public OnePlusARObject(long id) {
		mId = id;
		init();
	}

	/**
	 * Create an instance of a {@link com.oneplusar.android.world.OnePlusARObject
	 * OnePlusARObject} with an unique ID. The hash of the object will be used as
	 * the {@link com.oneplusar.android.world.OnePlusARObject OnePlusARObject}
	 * unique id.
	 */
	public OnePlusARObject() {
		init();
	}

	private void init() {
		plugins = new ArrayList<OnePlusARObjectPlugin>(DEFAULT_PLUGINS_CAPACITY);
		mPosition = new Point3();
		mAngle = new Point3();
		mTexture = new Texture();
		faceToCamera(true);
		setVisible(true);

		mTopLeft = new Point3();
		mBottomLeft = new Point3();
		mBottomRight = new Point3();
		mTopRight = new Point3();

		mScreenPositionTopLeft = new Point3();
		mScreenPositionTopRight = new Point3();
		mScreenPositionBottomLeft = new Point3();
		mScreenPositionBottomRight = new Point3();
		mScreenPositionCenter = new Point3();
	}

	/**
	 * Get the unique id of the
	 * {@link com.oneplusar.android.world.OnePlusARObject OnePlusARObject}.
	 */
	public long getId() {
		if (mId == null) {
			mId = (long) hashCode();
		}
		return mId.longValue();
	}

	public void addPlugin(OnePlusARObjectPlugin plugin) {
		synchronized (lockPlugins) {
			if (plugins.contains(plugin)) {
				return;
			}
			plugins.add(plugin);
		}
	}

	@Override
	public boolean removePlugin(OnePlusARObjectPlugin plugin) {
		boolean removed = false;
		synchronized (lockPlugins) {
			removed = plugins.remove(plugin);
		}
		if (removed) {
			plugin.onDetached();
		}
		return removed;
	}

	@Override
	public void removeAllPlugins() {
		synchronized (lockPlugins) {
			plugins.clear();
		}
	}

	@Override
	public OnePlusARObjectPlugin getFirstPlugin(Class<? extends OnePlusARObjectPlugin> pluginClass) {
		synchronized (lockPlugins) {
			for (OnePlusARObjectPlugin plugin : plugins) {
				if (pluginClass.isInstance(plugin)) {
					return plugin;
				}
			}
		}
		return null;
	}

	@Override
	public boolean containsAnyPlugin(Class<? extends OnePlusARObjectPlugin> pluginClass) {
		return getFirstPlugin(pluginClass) != null;
	}

	@Override
	public boolean containsPlugin(OnePlusARObjectPlugin plugin) {
		synchronized (lockPlugins) {
			return plugins.contains(plugin);
		}
	}

	@Override
	public List<OnePlusARObjectPlugin> getAllPugins(Class<? extends OnePlusARObjectPlugin> pluginClass) {
		ArrayList<OnePlusARObjectPlugin> result = new ArrayList<OnePlusARObjectPlugin>(5);
		return getAllPlugins(pluginClass, result);
	}

	@Override
	public List<OnePlusARObjectPlugin> getAllPlugins(Class<? extends OnePlusARObjectPlugin> pluginClass,
			List<OnePlusARObjectPlugin> result) {
		synchronized (lockPlugins) {
			for (OnePlusARObjectPlugin plugin : plugins) {
				if (pluginClass.isInstance(plugin)) {
					result.add(plugin);
				}
			}
		}
		return result;
	}

	/**
	 * Get a {@link List} copy of the added plugins. Adding/removing plugins to
	 * this list will not affect the added plugins
	 * 
	 * @return
	 */
	@Override
	public List<OnePlusARObjectPlugin> getAllPlugins() {
		synchronized (lockPlugins) {
			return new ArrayList<OnePlusARObjectPlugin>(plugins);
		}
	}

	void onRemoved() {
		synchronized (lockPlugins) {
			for (OnePlusARObjectPlugin plugin : plugins) {
				plugin.onDetached();
			}
		}
	}

	/**
	 * Get the used angle for rendering the
	 * {@link com.oneplusar.android.world.OnePlusARObject OnePlusARObject}.
	 * 
	 * @return The angle in degrees.
	 */
	public Point3 getAngle() {
		return mAngle;
	}

	/**
	 * Set the used angle for rendering the
	 * {@link com.oneplusar.android.world.OnePlusARObject OnePlusARObject}.
	 * 
	 * @param x
	 *            The angle in degrees for x.
	 * 
	 * @param y
	 *            The angle in degrees for y.
	 * @param z
	 *            The angle in degrees for z.
	 */
	public void setAngle(float x, float y, float z) {
		if (mAngle.x == x && mAngle.y == y && mAngle.z == z)
			return;
		mAngle.x = x;
		mAngle.y = y;
		mAngle.z = z;
		synchronized (lockPlugins) {
			for (OnePlusARObjectPlugin plugin : plugins) {
				plugin.onAngleChanged(mAngle);
			}
		}
	}

	/**
	 * Set the used angle for rendering the
	 * {@link com.oneplusar.android.world.OnePlusARObject OnePlusARObject}.
	 * 
	 * @param newAngle
	 *            The angle in degrees.
	 */
	public void setAngle(Point3 newAngle) {
		if (newAngle == mAngle)
			return;
		mAngle = newAngle;
		synchronized (lockPlugins) {
			for (OnePlusARObjectPlugin plugin : plugins) {
				plugin.onAngleChanged(mAngle);
			}
		}
	}

	/**
	 * Get the position where the
	 * {@link com.oneplusar.android.world.OnePlusARObject OnePlusARObject} is being
	 * rendered.
	 * 
	 * @return The 3D position.
	 */
	public Point3 getPosition() {
		return mPosition;
	}

	/**
	 * Get the position where the
	 * {@link com.oneplusar.android.world.OnePlusARObject OnePlusARObject} is being
	 * rendered.
	 * 
	 * @param newPos
	 *            New position.
	 */
	public void setPosition(Point3 newPos) {
		if (newPos == mPosition)
			return;
		mPosition = newPos;
		synchronized (lockPlugins) {
			for (OnePlusARObjectPlugin plugin : plugins) {
				plugin.onPositionChanged(mPosition);
			}
		}
	}

	/**
	 * Set the position where the
	 * {@link com.oneplusar.android.world.OnePlusARObject OnePlusARObject} is being
	 * rendered.
	 * 
	 * @param newPos
	 *            New position.
	 */
	public void setPosition(float x, float y, float z) {
		if (mPosition.x == x && mPosition.y == y && mPosition.z == z)
			return;
		mPosition.x = x;
		mPosition.y = y;
		mPosition.z = z;
		synchronized (lockPlugins) {
			for (OnePlusARObjectPlugin plugin : plugins) {
				plugin.onPositionChanged(mPosition);
			}
		}
	}

	/**
	 * Override this method to change the default
	 * {@link com.oneplusar.android.opengl.renderable.Renderable Renderable}.
	 * 
	 * @return The new {@link com.oneplusar.android.opengl.renderable.Renderable
	 *         Renderable}.
	 */
	protected Renderable createRenderable() {
		return SquareRenderable.getInstance();
	}

	/**
	 * Get the {@link com.oneplusar.android.opengl.texture.Texture Texture} used
	 * to render the {@link com.oneplusar.android.world.OnePlusARObject
	 * OnePlusARObject}.
	 * 
	 * @return {@link com.oneplusar.android.opengl.texture.Texture Texture}
	 *         object in use.
	 */
	public Texture getTexture() {
		return mTexture;
	}

	/**
	 * Set the texture pointer of the
	 * {@link com.oneplusar.android.world.OnePlusARObject OnePlusARObject}.
	 * 
	 * @param texturePointer
	 *            The new texture pointer.
	 */
	public void setTexturePointer(int texturePointer) {
		if (texturePointer == mTexture.getTexturePointer())
			return;
		mTexture.setTexturePointer(texturePointer);
		synchronized (lockPlugins) {
			for (OnePlusARObjectPlugin plugin : plugins) {
				plugin.onTextureChanged(mTexture);
			}
		}
	}

	/**
	 * Set the {@link com.oneplusar.android.opengl.texture.Texture Texture} used
	 * to render the {@link com.oneplusar.android.world.OnePlusARObject
	 * OnePlusARObject}.
	 * 
	 */
	public void setTexture(Texture texture) {
		if (texture == this.mTexture) {
			return;
		}
		if (texture == null) {
			texture = new Texture();
		}
		this.mTexture = texture;
		synchronized (lockPlugins) {
			for (OnePlusARObjectPlugin plugin : plugins) {
				plugin.onTextureChanged(this.mTexture);
			}
		}
	}

	/**
	 * Get {@link com.oneplusar.android.opengl.renderable.Renderable Renderable}
	 * that renders the {@link com.oneplusar.android.world.OnePlusARObject
	 * OnePlusARObject}
	 * 
	 * @return The {@link com.oneplusar.android.opengl.renderable.Renderable
	 *         Renderable} used for rendering.
	 */
	public Renderable getOpenGLObject() {
		if (null == mRenderable) {
			mRenderable = createRenderable();
		}
		return mRenderable;
	}

	/**
	 * Set a custom {@link com.oneplusar.android.opengl.renderable.Renderable
	 * Renderable} for the {@link com.oneplusar.android.world.OnePlusARObject
	 * OnePlusARObject}
	 * 
	 * @param renderable
	 */
	public void setRenderable(Renderable renderable) {
		if (renderable == this.mRenderable)
			return;
		this.mRenderable = renderable;
		synchronized (lockPlugins) {
			for (OnePlusARObjectPlugin plugin : plugins) {
				plugin.onRenderableChanged(this.mRenderable);
			}
		}
	}

	/**
	 * The the image uri used to represent the
	 * {@link com.oneplusar.android.world.OnePlusARObject OnePlusARObject}
	 * 
	 * @return
	 */
	public String getImageUri() {
		return mImageUri;
	}

	/**
	 * Define if the {@link com.oneplusar.android.world.OnePlusARObject
	 * OnePlusARObject} should face the camera.
	 * 
	 * @param faceToCamera
	 *            true if it should face the camera, false otherwise.
	 */
	public void faceToCamera(boolean faceToCamera) {
		if (faceToCamera == this.mFaceToCamera)
			return;
		this.mFaceToCamera = faceToCamera;
		synchronized (lockPlugins) {
			for (OnePlusARObjectPlugin plugin : plugins) {
				plugin.onFaceToCameraChanged(this.mFaceToCamera);
			}
		}
	}

	/**
	 * Check if the {@link com.oneplusar.android.world.OnePlusARObject
	 * OnePlusARObject} is facing the camera.
	 * 
	 * @return True if it is facing.
	 */
	public boolean isFacingToCamera() {
		return mFaceToCamera;
	}

	/**
	 * Set the visibility of this object. if it is false, the engine will not
	 * render it.
	 * 
	 * @param visible
	 *            True to set it visible, false to don't render it.
	 */
	public void setVisible(boolean visible) {
		if (visible == this.mVisible)
			return;
		this.mVisible = visible;
		synchronized (lockPlugins) {
			for (OnePlusARObjectPlugin plugin : plugins) {
				plugin.onVisibilityChanged(this.mVisible);
			}
		}
	}

	/**
	 * Check the visibility of the
	 * {@link com.oneplusar.android.world.OnePlusARObject OnePlusARObject}.
	 * 
	 * @return True if it is visible, false otherwise.
	 */
	public boolean isVisible() {
		return mVisible;
	}

	/**
	 * Set the name of the {@link com.oneplusar.android.world.OnePlusARObject
	 * OnePlusARObject}.
	 * 
	 * @param name
	 *            Name of the {@link com.oneplusar.android.world.OnePlusARObject
	 *            OnePlusARObject}.
	 */
	public void setName(String name) {
		if (name == this.mName)
			return;
		this.mName = name;
		synchronized (lockPlugins) {
			for (OnePlusARObjectPlugin plugin : plugins) {
				plugin.onNameChanged(this.mName);
			}
		}
	}

	/**
	 * Get the name of the {@link com.oneplusar.android.world.OnePlusARObject
	 * OnePlusARObject}.
	 * 
	 * @return The name of the {@link com.oneplusar.android.world.OnePlusARObject
	 *         OnePlusARObject}.
	 */
	public String getName() {
		return mName;
	}

	/**
	 * Set the image uri.
	 * 
	 * @param uri
	 *            The image uri that represents the
	 *            {@link com.oneplusar.android.world.OnePlusARObject
	 *            OnePlusARObject}.
	 */
	public void setImageUri(String uri) {
		if (uri == mImageUri)
			return;
		mImageUri = uri;
		synchronized (lockPlugins) {
			for (OnePlusARObjectPlugin plugin : plugins) {
				plugin.onImageUriChanged(mImageUri);
			}
		}
		setTexture(null);
	}

	/**
	 * Set an image resource for the
	 * {@link com.oneplusar.android.world.OnePlusARObject OnePlusARObject}.
	 * 
	 * @param resId
	 *            The resource id.
	 */
	public void setImageResource(int resId) {
		setImageUri(BitmapCache.generateUri(resId));
	}

	/**
	 * Set the list type of the this object. This type is configured once the
	 * object is added to the World.
	 * 
	 * @param worldListType
	 */
	void setWorldListType(int worldListType) {
		mTypeList = worldListType;
	}

	/**
	 * Get the list type of the
	 * {@link com.oneplusar.android.world.OnePlusARObject OnePlusARObject}.
	 * 
	 * @return The list type.
	 */
	public int getWorldListType() {
		return mTypeList;
	}

	/**
	 * Get the Distance from the user in meters.
	 * 
	 * @return Distance in meters.
	 */
	public double getDistanceFromUser() {
		return mDistanceFromUser;
	}

	/**
	 * Set how far is the object from the user (meters).
	 * 
	 * This method is used by the {@link ARRenderer} to set this value.
	 * 
	 * @param distance
	 *            Distance in meters.
	 */
	public void setDistanceFromUser(double distance) {
		mDistanceFromUser = distance;
	}

	/**
	 * Get the bottom left screen position of the
	 * {@link com.oneplusar.android.world.OnePlusARObject OnePlusARObject} on the
	 * screen. use the Z axis to check if the object is in front (z<1) or behind
	 * (z>1) the screen.
	 * 
	 * @return Bottom left screen position.
	 */
	public Point3 getScreenPositionBottomLeft() {
		return mScreenPositionBottomLeft;
	}

	/**
	 * Get the top left screen position of the
	 * {@link com.oneplusar.android.world.OnePlusARObject OnePlusARObject} on the
	 * screen. use the Z axis to check if the object is in front (z<1) or behind
	 * (z>1) the screen.
	 * 
	 * @return top left screen position.
	 */
	public Point3 getScreenPositionTopLeft() {
		return mScreenPositionTopLeft;
	}

	/**
	 * Get the top right screen position of the
	 * {@link com.oneplusar.android.world.OnePlusARObject OnePlusARObject} on the
	 * screen. use the Z axis to check if the object is in front (z<1) or behind
	 * (z>1) the screen.
	 * 
	 * @return Top right screen position.
	 */
	public Point3 getScreenPositionTopRight() {
		return mScreenPositionTopRight;
	}

	/**
	 * Get the bottom right screen position of the
	 * {@link com.oneplusar.android.world.OnePlusARObject OnePlusARObject} on the
	 * screen. use the Z axis to check if the object is in front (z<1) or behind
	 * (z>1) the screen.
	 * 
	 * @return Bottom right screen position.
	 */
	public Point3 getScreenPositionBottomRight() {
		return mScreenPositionBottomRight;
	}

	/**
	 * Get the center screen position of the
	 * {@link com.oneplusar.android.world.OnePlusARObject OnePlusARObject} on the
	 * screen. use the Z axis to check if the object is in front (z<1) or behind
	 * (z>1) the screen.
	 * 
	 * @return Center screen position.
	 */
	public Point3 getScreenPositionCenter() {
		return mScreenPositionCenter;
	}

	/**
	 * Get the top left of the {@link com.oneplusar.android.world.OnePlusARObject
	 * OnePlusARObject} on the 3D world.
	 * 
	 * @return Top left 3D.
	 */
	public Point3 getTopLeft() {
		mTopLeft.x = mPosition.x + mTexture.getVertices()[3];
		mTopLeft.y = mPosition.y + mTexture.getVertices()[4];
		mTopLeft.z = mPosition.z + mTexture.getVertices()[5];

		mTopLeft.rotatePointDegrees_x(mAngle.x, mPosition);
		mTopLeft.rotatePointDegrees_y(mAngle.y, mPosition);
		mTopLeft.rotatePointDegrees_z(mAngle.z, mPosition);
		return mTopLeft;
	}

	/**
	 * Get the bottom left of the
	 * {@link com.oneplusar.android.world.OnePlusARObject OnePlusARObject} on the
	 * 3D world.
	 * 
	 * @return bottom left 3D.
	 */
	public Point3 getBottomLeft() {
		mBottomLeft.x = mPosition.x + mTexture.getVertices()[0];
		mBottomLeft.y = mPosition.y + mTexture.getVertices()[1];
		mBottomLeft.z = mPosition.z + mTexture.getVertices()[2];

		mBottomLeft.rotatePointDegrees_x(mAngle.x, mPosition);
		mBottomLeft.rotatePointDegrees_y(mAngle.y, mPosition);
		mBottomLeft.rotatePointDegrees_z(mAngle.z, mPosition);
		return mBottomLeft;
	}

	/**
	 * Get the bottom right of the
	 * {@link com.oneplusar.android.world.OnePlusARObject OnePlusARObject} on the
	 * 3D world.
	 * 
	 * @return Bottom right 3D.
	 */
	public Point3 getBottomRight() {
		mBottomRight.x = mPosition.x + mTexture.getVertices()[6];
		mBottomRight.y = mPosition.y + mTexture.getVertices()[7];
		mBottomRight.z = mPosition.z + mTexture.getVertices()[8];

		mBottomRight.rotatePointDegrees_x(mAngle.x, mPosition);
		mBottomRight.rotatePointDegrees_y(mAngle.y, mPosition);
		mBottomRight.rotatePointDegrees_z(mAngle.z, mPosition);
		return mBottomRight;
	}

	/**
	 * Get the top right of the
	 * {@link com.oneplusar.android.world.OnePlusARObject OnePlusARObject} on the
	 * 3D world.
	 * 
	 * @return Top right 3D.
	 */
	public Point3 getTopRight() {
		mTopRight.x = mPosition.x + mTexture.getVertices()[9];
		mTopRight.y = mPosition.y + mTexture.getVertices()[10];
		mTopRight.z = mPosition.z + mTexture.getVertices()[11];

		mTopRight.rotatePointDegrees_x(mAngle.x, mPosition);
		mTopRight.rotatePointDegrees_y(mAngle.y, mPosition);
		mTopRight.rotatePointDegrees_z(mAngle.z, mPosition);
		return mTopRight;
	}

	/**
	 * Get the {@link com.oneplusar.android.opengl.colision.MeshCollider
	 * MeshCollider} of the {@link com.oneplusar.android.world.GeoObject GeoObject}.
	 * 
	 * @return Mesh collider.
	 */
	public MeshCollider getMeshCollider() {
		// TODO: Improve the mesh collider!!
		Point3 topLeft = getTopLeft();
		Point3 bottomLeft = getBottomLeft();
		Point3 bottomRight = getBottomRight();
		Point3 topRight = getTopRight();

		// Generate the collision detector
		mMeshCollider = new SquareMeshCollider(topLeft, bottomLeft, bottomRight, topRight);
		return mMeshCollider;
	}
}
