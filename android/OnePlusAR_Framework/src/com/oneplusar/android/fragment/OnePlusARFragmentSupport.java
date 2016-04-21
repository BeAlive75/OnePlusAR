package com.oneplusar.android.fragment;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.FrameLayout.LayoutParams;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.oneplusar.android.opengl.renderer.ARRenderer.FpsUpdatable;
import com.oneplusar.android.screenshot.OnScreenshotListener;
import com.oneplusar.android.screenshot.ScreenshotHelper;
import com.oneplusar.android.sensor.OnePlusARSensorManager;
import com.oneplusar.android.util.math.geom.Ray;
import com.oneplusar.android.view.OnePlusARGLSurfaceView;
import com.oneplusar.android.view.OnePlusARViewAdapter;
import com.oneplusar.android.view.CameraView;
import com.oneplusar.android.view.OnClickOnePlusARObjectListener;
import com.oneplusar.android.view.OnTouchOnePlusARViewListener;
import com.oneplusar.android.world.OnePlusARObject;
import com.oneplusar.android.world.World;

/**
 * Support fragment class that displays and control the
 * {@link com.oneplusar.android.view.CameraView CameraView} and the
 * {@link com.oneplusar.android.view.OnePlusARGLSurfaceView OnePlusARGLSurfaceView}
 * . It also provide a set of utilities to control the usage of the augmented
 * reality world.
 * 
 */
public class OnePlusARFragmentSupport extends Fragment implements FpsUpdatable, OnClickListener,
		OnTouchListener {

	private static final int CORE_POOL_SIZE = 1;
	private static final int MAXIMUM_POOL_SIZE = 1;
	private static final long KEEP_ALIVE_TIME = 1000; // 1000 ms

	private CameraView mOnePlusARCameraView;
	private OnePlusARGLSurfaceView mOnePlusARGLSurface;
	private TextView mFpsTextView;
	private RelativeLayout mMainLayout;

	private World mWorld;

	private OnTouchOnePlusARViewListener mTouchListener;
	private OnClickOnePlusARObjectListener mClickListener;

	private float mLastScreenTouchX, mLastScreenTouchY;

	private ThreadPoolExecutor mThreadPool;
	private BlockingQueue<Runnable> mBlockingQueue;

	private SensorManager mSensorManager;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mBlockingQueue = new LinkedBlockingQueue<Runnable>();
		mThreadPool = new ThreadPoolExecutor(CORE_POOL_SIZE, MAXIMUM_POOL_SIZE, KEEP_ALIVE_TIME,
				TimeUnit.MILLISECONDS, mBlockingQueue);
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		mSensorManager = (SensorManager) activity.getSystemService(Context.SENSOR_SERVICE);
	}

	private void init() {
		android.view.ViewGroup.LayoutParams params = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
				ViewGroup.LayoutParams.MATCH_PARENT);

		mMainLayout = new RelativeLayout(getActivity());
		mOnePlusARGLSurface = createOnePlusARGLSurfaceView();
		mOnePlusARGLSurface.setOnTouchListener(this);

		mOnePlusARCameraView = createCameraView();

		mMainLayout.addView(mOnePlusARCameraView, params);
		mMainLayout.addView(mOnePlusARGLSurface, params);
	}

	private void checkIfSensorsAvailable() {
		PackageManager pm = getActivity().getPackageManager();
		boolean compass = pm.hasSystemFeature(PackageManager.FEATURE_SENSOR_COMPASS);
		boolean accelerometer = pm.hasSystemFeature(PackageManager.FEATURE_SENSOR_ACCELEROMETER);
		if (!compass && !accelerometer) {
			throw new IllegalStateException(getClass().getName()
					+ " can not run without the compass and the acelerometer sensors.");
		} else if (!compass) {
			throw new IllegalStateException(getClass().getName() + " can not run without the compass sensor.");
		} else if (!accelerometer) {
			throw new IllegalStateException(getClass().getName()
					+ " can not run without the acelerometer sensor.");
		}

	}

	/**
	 * Override this method to personalize the
	 * {@link com.oneplusar.android.view.OnePlusARGLSurfaceView
	 * OnePlusARGLSurfaceView} that will be instantiated.
	 * 
	 * @return
	 */
	protected OnePlusARGLSurfaceView createOnePlusARGLSurfaceView() {
		return new OnePlusARGLSurfaceView(getActivity());
	}

	/**
	 * Override this method to personalize the
	 * {@link com.oneplusar.android.view.CameraView CameraView} that will be
	 * instantiated.
	 * 
	 * @return
	 */
	protected CameraView createCameraView() {
		return new CameraView(getActivity());
	}

	/**
	 * 
	 * Returns the CameraView for this class instance.
	 * 
	 * @return
	 */
	public CameraView getCameraView() {
		return mOnePlusARCameraView;
	}

	/**
	 * Returns the SurfaceView for this class instance.
	 * 
	 * @return
	 */
	public OnePlusARGLSurfaceView getGLSurfaceView() {
		return mOnePlusARGLSurface;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		init();
		startRenderingAR();
		return mMainLayout;
	}

	@Override
	public void onResume() {
		super.onResume();
		mOnePlusARCameraView.startPreviewCamera();
		mOnePlusARGLSurface.onResume();
		OnePlusARSensorManager.resume(mSensorManager);
		if (mWorld != null) {
			mWorld.onResume();
		}
	}

	@Override
	public void onPause() {
		super.onPause();
		mOnePlusARCameraView.releaseCamera();
		mOnePlusARGLSurface.onPause();
		OnePlusARSensorManager.pause(mSensorManager);
		if (mWorld != null) {
			mWorld.onPause();
		}
	}

	/**
	 * Set the listener to get notified when the user touch the AR view.
	 * 
	 * @param listener
	 */
	public void setOnTouchOnePlusARViewListener(OnTouchOnePlusARViewListener listener) {
		mTouchListener = listener;
	}

	/**
	 * Set the {@link com.oneplusar.android.view.OnClickOnePlusARObjectListener
	 * OnClickOnePlusARObjectListener} to get notified when the user click on a
	 * {@link com.oneplusar.android.world.OnePlusARObject OnePlusARObject}
	 * 
	 * @param listener
	 */
	public void setOnClickOnePlusARObjectListener(OnClickOnePlusARObjectListener listener) {
		mClickListener = listener;
		mMainLayout.setClickable(listener != null);
		mMainLayout.setOnClickListener(this);
	}

	@Override
	public boolean onTouch(View v, final MotionEvent event) {
		mLastScreenTouchX = event.getX();
		mLastScreenTouchY = event.getY();

		if (mWorld == null || mTouchListener == null || event == null) {
			return false;
		}
		mTouchListener.onTouchOnePlusARView(event, mOnePlusARGLSurface);
		return false;
	}

	@Override
	public void onClick(View v) {
		if (v == mMainLayout) {
			if (mClickListener == null) {
				return;
			}
			final float lastX = mLastScreenTouchX;
			final float lastY = mLastScreenTouchY;

			mThreadPool.execute(new Runnable() {
				@Override
				public void run() {
					final ArrayList<OnePlusARObject> oneplusarObjects = new ArrayList<OnePlusARObject>();
					mOnePlusARGLSurface.getOnePlusARObjectsOnScreenCoordinates(lastX, lastY, oneplusarObjects);
					if (oneplusarObjects.size() == 0)
						return;
					mOnePlusARGLSurface.post(new Runnable() {
						@Override
						public void run() {
							OnClickOnePlusARObjectListener listener = mClickListener;
							if (listener != null) {
								listener.onClickOnePlusARObject(oneplusarObjects);
							}
						}
					});
				}
			});
		}
	}

	/**
	 * Get the {@link com.oneplusar.android.world.World World} in use by the
	 * fragment.
	 * 
	 * @return
	 */
	public World getWorld() {
		return mWorld;
	}

	/**
	 * Set the {@link com.oneplusar.android.world.World World} that contains all
	 * the {@link com.oneplusar.android.world.OnePlusARObject OnePlusARObject} that
	 * will be displayed.
	 * 
	 * @param world
	 *            The {@link com.oneplusar.android.world.World World} that holds
	 *            the information of all the elements.
	 * 
	 * @throws IllegalStateException
	 *             If the device do not have the required sensors available.
	 */
	public void setWorld(World world) {
		try {
			checkIfSensorsAvailable();
		} catch (IllegalStateException e) {
			throw e;
		}
		mWorld = world;
		mOnePlusARGLSurface.setWorld(world);
	}

	/**
	 * Specify the delay to apply to the accelerometer and the magnetic field
	 * sensor. If you don't know what is the best value, don't touch it. The
	 * following values are applicable:<br>
	 * <br>
	 * SensorManager.SENSOR_DELAY_UI<br>
	 * SensorManager.SENSOR_DELAY_NORMAL <br>
	 * SensorManager.SENSOR_DELAY_GAME <br>
	 * SensorManager.SENSOR_DELAY_GAME <br>
	 * SensorManager.SENSOR_DELAY_FASTEST <br>
	 * <br>
	 * 
	 * @see {@link android.hardware.SensorManager SensorManager}
	 * 
	 * @param delay
	 *            Sensor delay.
	 */
	public void setSensorDelay(int delay) {
		mOnePlusARGLSurface.setSensorDelay(delay);
	}

	/**
	 * Get the current sensor delay.
	 * 
	 * @see {@link android.hardware.SensorManager SensorManager}
	 * 
	 * @return Current sensor delay.
	 */
	public int getSensorDelay() {
		return mOnePlusARGLSurface.getSensorDelay();
	}

	/**
	 * Use this method to check the frames per second.
	 * 
	 * @param fpsUpdatable
	 *            Listener that will be notified with current fps.
	 * 
	 * @see FpsUpdatable
	 */
	public void setFpsUpdatable(FpsUpdatable fpsUpdatable) {
		mOnePlusARGLSurface.setFpsUpdatable(fpsUpdatable);
	}

	/**
	 * Disable the GLSurface to stop rendering the AR world.
	 */
	public void stopRenderingAR() {
		mOnePlusARGLSurface.setVisibility(View.INVISIBLE);
	}

	/**
	 * Enable the GLSurface to start rendering the AR world.
	 */
	public void startRenderingAR() {
		mOnePlusARGLSurface.setVisibility(View.VISIBLE);
	}

	/**
	 * Get the GeoObject that intersect with the coordinates x, y on the screen.<br>
	 * __Important__ When this method is called a new {@link List} is created.
	 * 
	 * @param x
	 *            X screen position.
	 * @param y
	 *            Y screen position.
	 * 
	 * @return A new list with the
	 *         {@link com.oneplusar.android.world.OnePlusARObject OnePlusARObject}
	 *         that collide with the screen cord
	 */
	public List<OnePlusARObject> getOnePlusARObjectsOnScreenCoordinates(float x, float y) {
		ArrayList<OnePlusARObject> oneplusarObjects = new ArrayList<OnePlusARObject>();
		mOnePlusARGLSurface.getOnePlusARObjectsOnScreenCoordinates(x, y, oneplusarObjects);
		return oneplusarObjects;
	}

	/**
	 * Get the GeoObject that intersect with the coordinates x, y on the screen.
	 * 
	 * @param x
	 *            X screen position.
	 * @param y
	 *            Y screen position.
	 * @param oneplusarObjects
	 *            The output list where all the
	 *            {@link com.oneplusar.android.world.OnePlusARObject
	 *            OnePlusARObject} that collide with the screen cord will be
	 *            stored.
	 * 
	 */
	public void getOnePlusARObjectsOnScreenCoordinates(float x, float y,
			ArrayList<OnePlusARObject> oneplusarObjects) {
		mOnePlusARGLSurface.getOnePlusARObjectsOnScreenCoordinates(x, y, oneplusarObjects);
	}

	/**
	 * Get the GeoObject that intersect with the coordinates x, y on the screen.
	 * 
	 * @param x
	 *            screen position.
	 * @param y
	 *            screen position.
	 * @param oneplusarObjects
	 *            The output list where all the
	 *            {@link com.oneplusar.android.world.OnePlusARObject
	 *            OnePlusARObject} that collide with the screen cord will be
	 *            stored.
	 * @param ray
	 *            The ray that will hold the direction of the screen coordinate.
	 * 
	 */
	public void getOnePlusARObjectsOnScreenCoordinates(float x, float y,
			ArrayList<OnePlusARObject> oneplusarObjects, Ray ray) {
		mOnePlusARGLSurface.getOnePlusARObjectsOnScreenCoordinates(x, y, oneplusarObjects, ray);

	}

	/**
	 * When a {@link com.oneplusar.android.world.GeoObject GeoObject} is rendered
	 * according to its position it could look very small if it is far away. Use
	 * this method to render far objects as if there were closer.<br>
	 * For instance if there are objects farther than 50 meters and we want them
	 * to be displayed as they where at 50 meters, we could use this method for
	 * that purpose. <br>
	 * To set it to the default behavior just set it to 0
	 * 
	 * @param maxDistanceSize
	 *            The top far distance (in meters) which we want to draw a
	 *            {@link com.oneplusar.android.world.GeoObject GeoObject} , 0 to
	 *            set again the default behavior
	 */
	public void setPullCloserDistance(float maxDistanceSize) {
		mOnePlusARGLSurface.setPullCloserDistance(maxDistanceSize);
	}

	/**
	 * Get the distance which all the {@link com.oneplusar.android.world.GeoObject
	 * GeoObject} will be rendered if the are farther that the returned distance.
	 * 
	 * @return The current max distance. 0 is the default behavior.
	 */
	public float getPullCloserDistance() {
		return mOnePlusARGLSurface.getPullCloserDistance();
	}

	/**
	 * When a {@link com.oneplusar.android.world.GeoObject GeoObject} is rendered
	 * according to its position it could look very big if it is too close. Use
	 * this method to render near objects as if there were farther.<br>
	 * For instance if there is an object at 1 meters and we want to have
	 * everything at to look like if they where at least at 10 meters, we could
	 * use this method for that purpose. <br>
	 * To set it to the default behavior just set it to 0.
	 * 
	 * @param minDistanceSize
	 *            The top near distance (in meters) which we want to draw a
	 *            {@link com.oneplusar.android.world.GeoObject GeoObject} , 0 to
	 *            set again the default behavior.
	 * 
	 */
	public void setPushAwayDistance(float minDistanceSize) {
		mOnePlusARGLSurface.setPushAwayDistance(minDistanceSize);
	}

	/**
	 * Get the closest distance which all the
	 * {@link com.oneplusar.android.world.GeoObject GeoObject} can be displayed.
	 * 
	 * @return The current minimum distance. 0 is the default behavior.
	 */
	public float getPushAwayDistance() {
		return mOnePlusARGLSurface.getPushAwayDistance();
	}
	
	/**
	 * Set the distance (in meters) which the objects will be considered to render.
	 * 
	 * @param meters to be rendered from the user.
	 */
	public void setMaxDistanceToRender(float meters) {
		mOnePlusARGLSurface.setMaxDistanceToRender(meters);
	}

	/**
	 * Get the distance (in meters) which the objects are being considered when
	 * rendering.
	 * 
	 * @return meters
	 */
	public float getMaxDistanceToRender() {
		return mOnePlusARGLSurface.getMaxDistanceToRender();
	}
	
	/**
	 * Set the distance factor for rendering all the objects. As bigger the
	 * factor the closer the objects.
	 * 
	 * @param factor
	 *            number bigger than 0.
	 */
	public void setDistanceFactor(float meters)
	{
		mOnePlusARGLSurface.setDistanceFactor(meters);
	}
	
	/**
	 * Get the distance factor.
	 * 
	 * @return Distance factor
	 */
	public float getDistanceFactor(){
		return mOnePlusARGLSurface.getDistanceFactor();
	}

	/**
	 * Take a screenshot of the oneplusar fragment. The screenshot will contain
	 * the camera and the AR world overlapped.
	 * 
	 * @param listener
	 *            {@link com.oneplusar.android.screenshot.OnScreenshotListener
	 *            OnScreenshotListener} That will be notified when the
	 *            screenshot is ready.
	 * @param options
	 *            Bitmap options.
	 */
	public void takeScreenshot(OnScreenshotListener listener, BitmapFactory.Options options) {
		ScreenshotHelper.takeScreenshot(getCameraView(), getGLSurfaceView(), listener, options);
	}

	/**
	 * Take a screenshot of the oneplusar fragment. The screenshot will contain
	 * the camera and the AR world overlapped.
	 * 
	 * @param listener
	 *            {@link com.oneplusar.android.screenshot.OnScreenshotListener
	 *            OnScreenshotListener} That will be notified when the
	 *            screenshot is ready.
	 */
	public void takeScreenshot(OnScreenshotListener listener) {
		BitmapFactory.Options options = new BitmapFactory.Options();
		// TODO: Improve this part
		options.inSampleSize = 4;
		// options.inSampleSize = 1;
		takeScreenshot(listener, options);
	}

	/**
	 * Show the number of frames per second in the left upper corner. False by
	 * default.
	 * 
	 * @param show
	 *            True to show the FPS, false otherwise.
	 */
	public void showFPS(boolean show) {
		if (show) {
			if (mFpsTextView == null) {
				mFpsTextView = new TextView(getActivity());
				mFpsTextView.setBackgroundResource(android.R.color.black);
				mFpsTextView.setTextColor(getResources().getColor(android.R.color.white));
				LayoutParams params = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
						ViewGroup.LayoutParams.WRAP_CONTENT);
				mMainLayout.addView(mFpsTextView, params);
			}
			mFpsTextView.setVisibility(View.VISIBLE);
			setFpsUpdatable(this);
		} else if (mFpsTextView != null) {
			mFpsTextView.setVisibility(View.GONE);
			setFpsUpdatable(null);
		}
	}

	@Override
	public void onFpsUpdate(final float fps) {
		if (mFpsTextView != null) {
			mFpsTextView.post(new Runnable() {
				@Override
				public void run() {
					mFpsTextView.setText("fps: " + fps);
				}
			});
		}
	}

	/**
	 * Set the adapter to draw the views on top of the AR View.
	 * 
	 * @param adapter
	 */
	public void setOnePlusARViewAdapter(OnePlusARViewAdapter adapter) {
		mOnePlusARGLSurface.setOnePlusARViewAdapter(adapter, mMainLayout);
	}

	/**
	 * Use this method to fill all the screen positions of the
	 * {@link com.oneplusar.android.world.OnePlusARObject OnePlusARObject} when a
	 * object is rendered. Remember that the information is filled when the
	 * object is rendered, so it is asynchronous.<br>
	 * 
	 * After this method is called you can use the following:<br>
	 * {@link com.oneplusar.android.world.OnePlusARObject
	 * OnePlusARObject.getScreenPositionBottomLeft()}<br>
	 * {@link com.oneplusar.android.world.OnePlusARObject
	 * OnePlusARObject.getScreenPositionBottomRight()}<br>
	 * {@link com.oneplusar.android.world.OnePlusARObject
	 * OnePlusARObject.getScreenPositionTopLeft()}<br>
	 * {@link com.oneplusar.android.world.OnePlusARObject
	 * OnePlusARObject.getScreenPositionTopRight()}
	 * 
	 * __Important__ Enabling this feature will reduce the FPS, use only when is
	 * needed.
	 * 
	 * @param fill
	 *            Enable or disable this feature.
	 */
	public void forceFillOnePlusARObjectPositionsOnRendering(boolean fill) {
		mOnePlusARGLSurface.forceFillOnePlusARObjectPositionsOnRendering(fill);
	}

	/**
	 * Use this method to fill all the screen positions of the
	 * {@link com.oneplusar.android.world.OnePlusARObject OnePlusARObject}. After
	 * this method is called you can use the following:<br>
	 * {@link com.oneplusar.android.world.OnePlusARObject
	 * OnePlusARObject.getScreenPositionBottomLeft()}<br>
	 * {@link com.oneplusar.android.world.OnePlusARObject
	 * OnePlusARObject.getScreenPositionBottomRight()}<br>
	 * {@link com.oneplusar.android.world.OnePlusARObject
	 * OnePlusARObject.getScreenPositionTopLeft()}<br>
	 * {@link com.oneplusar.android.world.OnePlusARObject
	 * OnePlusARObject.getScreenPositionTopRight()}
	 * 
	 * @param oneplusarObject
	 *            The {@link com.oneplusar.android.world.OnePlusARObject
	 *            OnePlusARObject} to compute
	 */
	public void fillOnePlusARObjectPositions(OnePlusARObject oneplusarObject) {
		mOnePlusARGLSurface.fillOnePlusARObjectPositions(oneplusarObject);
	}
	
	/**
	 * Use setPullCloserDistance instead.
	 */
	@Deprecated
	public void setMaxFarDistance(float maxDistanceSize) {
		setPullCloserDistance(maxDistanceSize);
	}

	/**
	 * Use getPushFrontDistance instead.
	 */
	@Deprecated
	public float getMaxDistanceSize() {
		return getPullCloserDistance();
	}

	/**
	 * Use setPushAwayDistance instead.
	 */
	@Deprecated
	public void setMinFarDistanceSize(float minDistanceSize) {
		setPushAwayDistance(minDistanceSize);
	}

	/**
	 * Use getPushAwayDistance instead.
	 */
	@Deprecated
	public float getMinDistanceSize() {
		return getPushAwayDistance();
	}
}
