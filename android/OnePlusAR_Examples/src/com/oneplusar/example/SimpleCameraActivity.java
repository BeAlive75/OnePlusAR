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

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.Window;

import com.oneplusar.android.fragment.OnePlusARFragmentSupport;
import com.oneplusar.android.world.World;

public class SimpleCameraActivity extends FragmentActivity {

	private OnePlusARFragmentSupport mOnePlusARFragment;
	private World mWorld;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Hide the window title.
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		
		setContentView(R.layout.simple_camera);
		
		mOnePlusARFragment = (OnePlusARFragmentSupport) getSupportFragmentManager().findFragmentById(
				R.id.oneplusarFragment);

		// We create the world and fill it ...
		mWorld = CustomWorldHelper.generateObjects(this);
		// ... and send it to the fragment
		mOnePlusARFragment.setWorld(mWorld);

		// We also can see the Frames per seconds
		mOnePlusARFragment.showFPS(true);
	}

}
