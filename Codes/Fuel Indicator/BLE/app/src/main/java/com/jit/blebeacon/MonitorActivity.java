package com.jit.blebeacon;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Toast;

public class MonitorActivity extends MyActivity
{

	private static final int REQUEST_DISCOVERY = 0x1;;
	private final String TAG = "MonitorActivity";
	private Handler _handler = new Handler();
	private BluetoothDevice device = null;
	private BluetoothSocket socket = null;
	private OutputStream outputStream;
	private InputStream inputStream;

	public static StringBuffer hexString = new StringBuffer();

	Intent myIntent;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND,
				WindowManager.LayoutParams.FLAG_BLUR_BEHIND);
		setContentView(R.layout.monitor);
		myIntent = this.getIntent();
		mdrawer = (CDrawer) findViewById(R.id.drawer);

		BluetoothAdapter mBluetoothAdapter = BluetoothAdapter
				.getDefaultAdapter();
		mBluetoothAdapter.enable();

		try
		{
			Thread.sleep(2000);
		}
		catch (Exception ex)
		{

		}

		Intent intent = new Intent(MonitorActivity.this,
				SearchDeviceActivity.class);
		startActivityForResult(intent, 55);

	}

	/* after select, connect to device */
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		if (requestCode == 55)
		{
			// Start the Bluetooth Search
			BluetoothDevice finalDevice = SearchDeviceActivity.currentDevice;

			SocketApplication app = (SocketApplication) getApplicationContext();
			device = app.getDevice();
			if (finalDevice != null)
			{
				app.setDevice(finalDevice);
				device = app.getDevice();
			}

			new Thread()
			{
				public void run()
				{
					connect(device);
				};
			}.start();

			return;
		}
		if (requestCode != REQUEST_DISCOVERY)
		{
			finish();
			return;
		}
		if (resultCode != RESULT_OK)
		{
			finish();
			return;
		}
		final BluetoothDevice device = data
				.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
		new Thread()
		{
			public void run()
			{
				connect(device);
			};
		}.start();
	}

	protected void onDestroy()
	{
		super.onDestroy();
		try
		{
			if (socket != null)
			{
				socket.close();
			}
		}
		catch (IOException e)
		{
			Log.e(TAG, ">>", e);
		}
	}

	private CDrawer.CDrawThread mDrawThread;
	private CDrawer mdrawer;

	public void setBuffer(int[] paramArrayOfShort)
	{
		mDrawThread = mdrawer.getThread();
		mdrawer.CurrentMessage = current_msg;
		mDrawThread.setBuffer(paramArrayOfShort);
	}

	String current_msg = "";
	int byte_count = 0;
	short[] plot_bytes = new short[1000];

	protected void connect(BluetoothDevice device)
	{
		try
		{
			mdrawer = (CDrawer) findViewById(R.id.drawer);

			// Create a Socket connection: need the server's UUID number of
			// registered
			socket = device.createRfcommSocketToServiceRecord(UUID
					.fromString("00001101-0000-1000-8000-00805F9B34FB"));

			socket.connect();
			Log.d(TAG, ">>Client connected");
			inputStream = socket.getInputStream();
			outputStream = socket.getOutputStream();
			final int len = 2048;
			final byte[] bytes = new byte[len];

			for (; (inputStream.read(bytes, 0, len)) > -1;)
			{
				_handler.post(new Runnable()
				{
					public void run()
					{
						String data = new String(bytes);
						current_msg = data;

						Context context;
						int[] data_vals = {0,1,2,3,4};
						setBuffer(data_vals);

						try
						{
							Thread.sleep(1000);
						}
						catch (InterruptedException e)
						{
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				});
			}
			//dlg.cancel();
		}
		catch (IOException e)
		{
			Log.e(TAG, ">>", e);
			Toast.makeText(getBaseContext(),
					getResources().getString(R.string.ioexception),
					Toast.LENGTH_SHORT).show();
			return;
		}
		finally
		{
			if (socket != null)
			{
				try
				{
					Log.d(TAG, ">>Client Socket Close");
					socket.close();
					finish();
					return;
				}
				catch (IOException e)
				{
					Log.e(TAG, ">>", e);
				}
			}
		}
	}

	public String bufferStrToHex(String buffer, boolean flag)
	{
		String all = buffer;
		StringBuffer sb = new StringBuffer();
		String[] ones = all.split("<--");
		for (int i = 0; i < ones.length; i++)
		{
			if (ones[i] != "")
			{
				String[] twos = ones[i].split("-->");
				for (int j = 0; j < twos.length; j++)
				{
					if (twos[j] != "")
					{
						if (flag)
						{
							sb.append(SamplesUtils.stringToHex(twos[j]));
						}
						else
						{
							sb.append(SamplesUtils.hexToString(twos[j]));
						}
						if (j != twos.length - 1)
						{
							if (sb.toString() != "")
							{
								sb.append("\n");
							}
							sb.append("-->");
						}
					}
				}
				if (i != ones.length - 1)
				{
					if (sb.toString() != "")
					{
						sb.append("\n");
					}
					sb.append("<--");
				}
			}
		}
		return sb.toString();
	}
}