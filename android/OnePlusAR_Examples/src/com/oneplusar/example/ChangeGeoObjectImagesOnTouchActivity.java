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
package com.oneplusar.example;

import java.util.ArrayList;
import java.util.Iterator;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.MotionEvent;
import android.view.Window;
import android.widget.TextView;

import com.oneplusar.android.fragment.OnePlusARFragmentSupport;
import com.oneplusar.android.view.OnePlusARGLSurfaceView;
import com.oneplusar.android.view.OnClickOnePlusARObjectListener;
import com.oneplusar.android.view.OnTouchOnePlusARViewListener;
import com.oneplusar.android.world.OnePlusARObject;
import com.oneplusar.android.world.World;

public class ChangeGeoObjectImagesOnTouchActivity extends FragmentActivity implements OnTouchOnePlusARViewListener,
        OnClickOnePlusARObjectListener {

	private OnePlusARFragmentSupport mOnePlusARFragment;
	private World mWorld;

	private TextView mLabelText;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Hide the window title.
		requestWindowFeature(Window.FEATURE_NO_TITLE);

		loadViewFromXML();

		// We create the world and fill it
		mWorld = CustomWorldHelper.generateObjects(this);

		mOnePlusARFragment.setWorld(mWorld);
		mOnePlusARFragment.showFPS(true);

		// set listener for the geoObjects
		mOnePlusARFragment.setOnTouchOnePlusARViewListener(this);
		mOnePlusARFragment.setOnClickOnePlusARObjectListener(this);

	}

	@Override
	public void onTouchOnePlusARView(MotionEvent event, OnePlusARGLSurfaceView oneplusarView) {

		float x = event.getX();
		float y = event.getY();

		ArrayList<OnePlusARObject> geoObjects = new ArrayList<OnePlusARObject>();

		// This method call is better to don't do it in the UI thread!
		oneplusarView.getOnePlusARObjectsOnScreenCoordinates(x, y, geoObjects);

		String textEvent = "";

		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			textEvent = "Event type ACTION_DOWN: ";
			break;
		case MotionEvent.ACTION_UP:
			textEvent = "Event type ACTION_UP: ";
			break;
		case MotionEvent.ACTION_MOVE:
			textEvent = "Event type ACTION_MOVE: ";
			break;
		default:
			break;
		}

		Iterator<OnePlusARObject> iterator = geoObjects.iterator();
		while (iterator.hasNext()) {
			OnePlusARObject geoObject = iterator.next();
			textEvent = textEvent + " " + geoObject.getName();

		}
		mLabelText.setText("Event: " + textEvent);
	}

	private void loadViewFromXML() {
		setContentView(R.layout.camera_with_text);
		mOnePlusARFragment = (OnePlusARFragmentSupport) getSupportFragmentManager().findFragmentById(
				R.id.oneplusarFragment);

		mLabelText = (TextView) findViewById(R.id.labelText);

	}

	@Override
	public void onClickOnePlusARObject(ArrayList<OnePlusARObject> oneplusarObjects) {
		if (oneplusarObjects.size() > 0) {
			oneplusarObjects.get(0).setImageResource(R.drawable.splash);
		}
	}

}
