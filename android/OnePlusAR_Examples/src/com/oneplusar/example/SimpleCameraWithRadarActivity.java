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

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.Window;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.SeekBar.OnSeekBarChangeListener;

import com.oneplusar.android.fragment.OnePlusARFragmentSupport;
import com.oneplusar.android.plugin.radar.RadarView;
import com.oneplusar.android.plugin.radar.RadarWorldPlugin;
import com.oneplusar.android.world.World;

public class SimpleCameraWithRadarActivity extends FragmentActivity implements OnSeekBarChangeListener {

	private OnePlusARFragmentSupport mOnePlusARFragment;
	private RadarView mRadarView;
	private RadarWorldPlugin mRadarPlugin;
	private World mWorld;

	private SeekBar mSeekBarMaxDistance;
	private TextView mTextviewMaxDistance;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Hide the window title.
		requestWindowFeature(Window.FEATURE_NO_TITLE);

		setContentView(R.layout.simple_camera_with_radar);

		mOnePlusARFragment = (OnePlusARFragmentSupport) getSupportFragmentManager().findFragmentById(
				R.id.oneplusarFragment);

		mOnePlusARFragment = (OnePlusARFragmentSupport) getSupportFragmentManager().findFragmentById(
				R.id.oneplusarFragment);

		mTextviewMaxDistance = (TextView) findViewById(R.id.textMaxDistance);
		mSeekBarMaxDistance = (SeekBar) findViewById(R.id.seekBarMaxDistance);
		mRadarView = (RadarView) findViewById(R.id.radarView);

		// Create the Radar plugin
		mRadarPlugin = new RadarWorldPlugin(this);
		// set the radar view in to our radar plugin
		mRadarPlugin.setRadarView(mRadarView);
		// Set how far (in meters) we want to display in the view
		mRadarPlugin.setMaxDistance(100);
		
		// We can customize the color of the items
		mRadarPlugin.setListColor(CustomWorldHelper.LIST_TYPE_EXAMPLE_1, Color.RED);
		// and also the size
		mRadarPlugin.setListDotRadius(CustomWorldHelper.LIST_TYPE_EXAMPLE_1, 3);

		// We create the world and fill it ...
		mWorld = CustomWorldHelper.generateObjects(this);
		// .. and send it to the fragment
		mOnePlusARFragment.setWorld(mWorld);

		// add the plugin
		mWorld.addPlugin(mRadarPlugin);

		// We also can see the Frames per seconds
		mOnePlusARFragment.showFPS(true);
		
		mSeekBarMaxDistance.setOnSeekBarChangeListener(this);
		mSeekBarMaxDistance.setMax(300);
		mSeekBarMaxDistance.setProgress(23);

	}

	@Override
	public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
		if (mRadarPlugin == null)
			return;
		if (seekBar == mSeekBarMaxDistance) {
			// float value = ((float) progress/(float) 10000);
			mTextviewMaxDistance.setText("Max distance Value: " + progress);
			mRadarPlugin.setMaxDistance(progress);
		}
	}

	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {
	}

	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {
	}

}
