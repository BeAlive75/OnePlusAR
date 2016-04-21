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
package com.oneplusar.android.opengl.renderable;

import javax.microedition.khronos.opengles.GL10;

import com.oneplusar.android.opengl.texture.Texture;
import com.oneplusar.android.util.math.geom.Plane;
import com.oneplusar.android.util.math.geom.Point3;
import com.oneplusar.android.util.math.geom.Vector3;
import com.oneplusar.android.world.OnePlusARObject;

/**
 * Basic {@link com.oneplusar.android.opengl.renderable.Renderable Renderable}
 * for rendering simple 2D images.
 */
public class SquareRenderable implements Renderable {

	private static SquareRenderable mThis;

	private Texture mTexture;
	private OnePlusARObject mOnePlusARObject;
	private Point3 mAngle;
	private Point3 mPosition;

	private long mTimeMark;

	private SquareRenderable() {
		mAngle = new Point3();
		mPosition = new Point3();
		mTexture = new Texture();
	}

	/**
	 * Get unique instance of the {@link SquareRenderable}.
	 * 
	 * @return
	 */
	public synchronized static Renderable getInstance() {
		if (mThis == null) {
			mThis = new SquareRenderable();
		}
		return mThis;
	}

	@Override
	public boolean update(long time, double distance, OnePlusARObject oneplusarObject) {
		mTimeMark = time;
		mOnePlusARObject = oneplusarObject;

		mPosition.x = mOnePlusARObject.getPosition().x;
		mPosition.y = mOnePlusARObject.getPosition().y;
		mPosition.z = mOnePlusARObject.getPosition().z;

		mAngle.x = mOnePlusARObject.getAngle().x;
		mAngle.y = mOnePlusARObject.getAngle().y;
		mAngle.z = mOnePlusARObject.getAngle().z;

		return false;
	}

	/** The draw method for the square with the GL context */
	@Override
	public void draw(GL10 gl, Texture defaultTexture) {

		mTexture = mOnePlusARObject.getTexture();
		Texture texture = mTexture;

		gl.glTranslatef(mPosition.x, mPosition.y, mPosition.z);

		// ROTATE According to the angles

		gl.glRotatef((float) mAngle.x, 1, 0, 0);
		gl.glRotatef((float) mAngle.y, 0, 1, 0);
		gl.glRotatef((float) mAngle.z, 0, 0, 1);

		// bind the previously generated texture
		if (!mTexture.isLoaded()) {
			gl.glBindTexture(GL10.GL_TEXTURE_2D, defaultTexture.getTexturePointer());
			texture = defaultTexture;
		} else {
			gl.glBindTexture(GL10.GL_TEXTURE_2D, mTexture.getTexturePointer());
		}

		// Point to our buffers
		gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
		gl.glEnableClientState(GL10.GL_TEXTURE_COORD_ARRAY);

		// Set the face rotation
		gl.glFrontFace(GL10.GL_CW);

		// Point to our vertex buffer
		gl.glVertexPointer(3, GL10.GL_FLOAT, 0, texture.getVerticesBuffer());
		gl.glTexCoordPointer(2, GL10.GL_FLOAT, 0, texture.getTextureBuffer());

		// Draw the vertices as triangle strip
		gl.glDrawArrays(GL10.GL_TRIANGLE_STRIP, 0, texture.getVertices().length / 3);

		// Disable the client state before leaving
		gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);
		gl.glDisableClientState(GL10.GL_TEXTURE_COORD_ARRAY);

		// gl.glPopMatrix();

		// rotate to the previous state
		gl.glRotatef((float) mAngle.x, -1, 0, 0);
		gl.glRotatef((float) mAngle.y, 0, -1, 0);
		gl.glRotatef((float) mAngle.z, 0, 0, -1);

		gl.glTranslatef(-mPosition.x, -mPosition.y, -mPosition.z);
	}

	@Override
	public Plane getPlane() {
		Plane plane = new Plane(mOnePlusARObject.getPosition(), new Vector3(0, -1, 0));
		return plane;
	}

	@Override
	public void setPosition(float x, float y, float z) {
		mOnePlusARObject.getPosition().x = x;
		mOnePlusARObject.getPosition().y = y;
		mOnePlusARObject.getPosition().z = z;
	}

	@Override
	public void setAngle(float x, float y, float z) {
		mAngle.x = x;
		mAngle.y = y;
		mAngle.z = z;
	}

	@Override
	public Point3 getAngle() {
		return this.mAngle;
	}

	@Override
	public long getTimeMark() {
		return mTimeMark;
	}

	@Override
	public Point3 getPosition() {
		if (mOnePlusARObject == null) {
			return null;
		}
		return mOnePlusARObject.getPosition();
	}

	@Override
	public void onNotRendered(double dst) {
	}

	@Override
	public Texture getTexture() {
		return mTexture;
	}

}
