package com.jit.blebeacon;


import android.app.Application;
import android.bluetooth.BluetoothDevice;
import android.app.Application;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.text.TextUtils;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.Volley;

import java.io.BufferedReader;
import java.io.FileReader;

public class SocketApplication extends Application {
	public static final String TAG = SocketApplication.class.getSimpleName();
	private RequestQueue mRequestQueue;
	private ImageLoader mImageLoader;
	private static SocketApplication mInstance;
	@Override
	public void onCreate() {
		super.onCreate();
		mInstance = this;
	}

	public static synchronized SocketApplication getInstance() {
		return mInstance;
	}

	public boolean isConnected() {

		ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

		NetworkInfo netInfo = cm.getActiveNetworkInfo();

		return netInfo != null && netInfo.isConnectedOrConnecting();

	}

	public RequestQueue getRequestQueue() {

		if (mRequestQueue == null) {

			mRequestQueue = Volley.newRequestQueue(getApplicationContext());
		}

		return mRequestQueue;
	}

	public <T> void addToRequestQueue(Request<T> req, String tag) {

		// set the default tag if tag is empty
		req.setTag(TextUtils.isEmpty(tag) ? TAG : tag);
		getRequestQueue().add(req);
	}

	public <T> void addToRequestQueue(Request<T> req) {
		req.setTag(TAG);
		getRequestQueue().add(req);
	}

	public void cancelPendingRequests(Object tag) {

		if (mRequestQueue != null) {

			mRequestQueue.cancelAll(tag);
		}
	}

	private BluetoothDevice device = null;

	public BluetoothDevice getDevice() {
		return device;
	}

	public void setDevice(BluetoothDevice device) {
		this.device = device;
	}
	
}
