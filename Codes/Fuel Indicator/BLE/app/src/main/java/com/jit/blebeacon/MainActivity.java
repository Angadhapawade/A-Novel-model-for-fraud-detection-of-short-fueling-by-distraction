package com.jit.blebeacon;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.github.anastr.speedviewlib.AwesomeSpeedometer;
import com.github.anastr.speedviewlib.ProgressiveGauge;
import com.github.anastr.speedviewlib.SpeedView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

public class MainActivity extends AppCompatActivity implements LocationListener {

    private static final int PERMISSION_REQUEST_WRITE_STORAGE = 1;
    private static final int PERMISSION_REQUEST_LOCATION = 2;
    private static final int PERMISSION_REQUEST_SMS = 3;
    private static final int PERMISSION_REQUEST_CALL = 4;
    private static final int CAMERA_REQUEST = 5;

    String address = null;
    TextView lblStatus;
    Button btnSettings;
    AwesomeSpeedometer prgLevels;
    private ProgressDialog progress;
    EditText txtMaxLevel;

    BluetoothAdapter myBluetooth = null;
    BluetoothSocket btSocket = null;
    private boolean isBtConnected = false;
    static final UUID myUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    String fileData = "";
    String FILE_NAME = "sensed_data";
    boolean allPermissionsGranted = false;

    private LocationManager locationManager;
    private String provider;
    public static Location location = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        askForPermissions();


        lblStatus = (TextView) findViewById(R.id.lblStatus);

        prgLevels = (AwesomeSpeedometer) findViewById(R.id.prgLevels);
        txtMaxLevel = (EditText)findViewById(R.id.txtMaxFuel);

        Intent intent = getIntent();
        address = intent.getStringExtra("device_address");

        new MainActivity.ConnectBT().execute();

    }

    public void askForPermissions() {
        Activity thisActivity = this;
        if (ContextCompat.checkSelfPermission(thisActivity,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            allPermissionsGranted = false;
            // Permission is not granted
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(thisActivity,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                Toast.makeText(MainActivity.this, "Permission needed for storing data", Toast.LENGTH_LONG).show();
                ActivityCompat.requestPermissions(thisActivity,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        PERMISSION_REQUEST_WRITE_STORAGE);
            } else {
                // No explanation needed; request the permission
                ActivityCompat.requestPermissions(thisActivity,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        PERMISSION_REQUEST_WRITE_STORAGE);
            }
        } else if (ContextCompat.checkSelfPermission(thisActivity,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            //Check for location
            if (ContextCompat.checkSelfPermission(thisActivity,
                    Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {
                allPermissionsGranted = false;
                // Permission is not granted
                // Should we show an explanation?
                if (ActivityCompat.shouldShowRequestPermissionRationale(thisActivity,
                        Manifest.permission.ACCESS_FINE_LOCATION)) {
                    Toast.makeText(MainActivity.this, "Permission needed for storing location", Toast.LENGTH_LONG).show();
                    ActivityCompat.requestPermissions(thisActivity,
                            new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                            PERMISSION_REQUEST_LOCATION);
                } else {
                    // No explanation needed; request the permission
                    ActivityCompat.requestPermissions(thisActivity,
                            new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                            PERMISSION_REQUEST_LOCATION);
                }
            } else {
                // Permission has already been granted
                allPermissionsGranted = true;
            }
        } else if (ContextCompat.checkSelfPermission(thisActivity,
                Manifest.permission.SEND_SMS)
                != PackageManager.PERMISSION_GRANTED) {
            //Check for location
            if (ContextCompat.checkSelfPermission(thisActivity,
                    Manifest.permission.SEND_SMS)
                    != PackageManager.PERMISSION_GRANTED) {
                allPermissionsGranted = false;
                // Permission is not granted
                // Should we show an explanation?
                if (ActivityCompat.shouldShowRequestPermissionRationale(thisActivity,
                        Manifest.permission.SEND_SMS)) {
                    Toast.makeText(MainActivity.this, "Permission needed for storing location", Toast.LENGTH_LONG).show();
                    ActivityCompat.requestPermissions(thisActivity,
                            new String[]{Manifest.permission.SEND_SMS},
                            PERMISSION_REQUEST_SMS);
                } else {
                    // No explanation needed; request the permission
                    ActivityCompat.requestPermissions(thisActivity,
                            new String[]{Manifest.permission.SEND_SMS},
                            PERMISSION_REQUEST_SMS);
                }
            } else {
                // Permission has already been granted
                allPermissionsGranted = true;
            }
        } else if (ContextCompat.checkSelfPermission(thisActivity,
                Manifest.permission.CALL_PHONE)
                != PackageManager.PERMISSION_GRANTED) {
            //Check for location
            if (ContextCompat.checkSelfPermission(thisActivity,
                    Manifest.permission.CALL_PHONE)
                    != PackageManager.PERMISSION_GRANTED) {
                allPermissionsGranted = false;
                // Permission is not granted
                // Should we show an explanation?
                if (ActivityCompat.shouldShowRequestPermissionRationale(thisActivity,
                        Manifest.permission.CALL_PHONE)) {
                    Toast.makeText(MainActivity.this, "Permission needed for storing location", Toast.LENGTH_LONG).show();
                    ActivityCompat.requestPermissions(thisActivity,
                            new String[]{Manifest.permission.CALL_PHONE},
                            PERMISSION_REQUEST_CALL);
                } else {
                    // No explanation needed; request the permission
                    ActivityCompat.requestPermissions(thisActivity,
                            new String[]{Manifest.permission.CALL_PHONE},
                            PERMISSION_REQUEST_CALL);
                }
            } else {
                // Permission has already been granted
                allPermissionsGranted = true;
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_WRITE_STORAGE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    allPermissionsGranted = true;
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    allPermissionsGranted = false;
                    Intent relaunch = new Intent(MainActivity.this, MainActivity.class);
                    startActivity(relaunch);

                    askForPermissions();
                }

                return;
            }

            case PERMISSION_REQUEST_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    allPermissionsGranted = true;
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    allPermissionsGranted = false;
                    Intent relaunch = new Intent(MainActivity.this, MainActivity.class);
                    startActivity(relaunch);

                    askForPermissions();
                }

                return;
            }

            case PERMISSION_REQUEST_SMS: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    allPermissionsGranted = true;
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    allPermissionsGranted = false;
                    Intent relaunch = new Intent(MainActivity.this, MainActivity.class);
                    startActivity(relaunch);

                    askForPermissions();
                }

                return;
            }

            case PERMISSION_REQUEST_CALL: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    allPermissionsGranted = true;
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    allPermissionsGranted = false;
                    Intent relaunch = new Intent(MainActivity.this, MainActivity.class);
                    startActivity(relaunch);

                    askForPermissions();
                }

                return;
            }
            // other 'case' lines to check for other
            // permissions this app might request.
        }
    }

    private void sendSignal(String number) {
        if (btSocket != null) {
            try {
                btSocket.getOutputStream().write(number.toString().getBytes());
            } catch (IOException e) {
                msg("Error");
            }
        }
    }

    private void Disconnect() {
        if (btSocket != null) {
            try {
                btSocket.close();
            } catch (IOException e) {
                msg("Error");
            }
        }

        finish();
    }

    private void msg(String s) {
        Toast.makeText(getApplicationContext(), s, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onLocationChanged(Location location) {
        this.location = location;
        lblStatus.setText("Location setup!");
        Log.d("LOCATION", location.toString());
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    String locationStr = "";

    private class ConnectBT extends AsyncTask<Void, Void, Void> {
        private boolean ConnectSuccess = true;

        @Override
        protected void onPreExecute() {
            progress = ProgressDialog.show(MainActivity.this, "Connecting...", "Please Wait!!!");
        }

        @Override
        protected Void doInBackground(Void... devices) {
            try {
                if (btSocket == null || !isBtConnected) {
                    myBluetooth = BluetoothAdapter.getDefaultAdapter();
                    BluetoothDevice dispositivo = myBluetooth.getRemoteDevice(address);
                    btSocket = dispositivo.createInsecureRfcommSocketToServiceRecord(myUUID);
                    BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
                    btSocket.connect();
                }
            } catch (IOException e) {
                Log.e("CONNECT", "Connect:" + e.toString());
                ConnectSuccess = false;
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);

            if (!ConnectSuccess) {
                msg("Connection Failed. Is it a SPP Bluetooth? Try again.");
                finish();
            } else {
                msg("Connected");
                isBtConnected = true;

                startReading();
            }

            progress.dismiss();
        }

        AsyncTask<String, String, Void> ataskRead = null;
        int recCount = 0;

        void startReading() {
            Log.d("BT_STAT", "Starting...");
            ataskRead = new AsyncTask<String, String, Void>() {
                @Override
                protected Void doInBackground(String... strings) {
                    while (ataskRead.isCancelled() == false) {
                        try {
                            if (btSocket != null) {
                                InputStream in;
                                Log.d("BT_STAT", "Stream reading...");
                                BufferedReader br = new BufferedReader(new InputStreamReader(btSocket.getInputStream()));
                                String data = br.readLine();
                                publishProgress(data);
                                Log.d("BT_STAT", "Read:" + data);
                            }
                            Thread.sleep(1000);
                        } catch (Exception ex) {
                            Log.d("BT_STAT", ex.getMessage());
                        }
                    }
                    Log.d("BT_STAT", "Cancelled");
                    return null;
                }

                @Override
                protected void onProgressUpdate(String... values) {
                    super.onProgressUpdate(values);
                    String data_val = values[0];
                    data_val = data_val.replace("$", "");
                    data_val = data_val.replace("#", "");
                    data_val = data_val.trim();
                    try {
                        float progress = Float.parseFloat(data_val);
                        if(progress > 20) {
                            progress = 20;
                        }

                        float maxFuel = 40;
                        try {
                            maxFuel = Float.parseFloat(txtMaxLevel.getText().toString());
                        } catch (Exception ex) {

                        }

                        prgLevels.setMaxSpeed(maxFuel);
                        prgLevels.setMinSpeed(0);
                        float level = 20-progress;
                        level = level * maxFuel/20;

                        prgLevels.speedTo(level);
                        lblStatus.setText("Fuel Level " + level + " ltrs.");
                    } catch (Exception ex) {
                        lblStatus.setText("Please check device");
                    }
                }
            };
            ataskRead.execute("");
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CAMERA_REQUEST) {
            Bitmap photo = (Bitmap) data.getExtras().get("data");
            //imgCamera.setImageBitmap(photo);

            try {
                uploadBitmap(photo);
            } catch (Exception ex) {

            }

        }
    }

    public String getAddress(Location loc) {
        Geocoder geocoder = new Geocoder(MainActivity.this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(loc.getLatitude(), loc.getLongitude(), 1);
            Address obj = addresses.get(0);
            String add = obj.getAddressLine(0);
            add = add + ";" + obj.getLocality() + ";" + obj.getPhone();

            Log.d("LOCATION", "Address:" + add);
            // Toast.makeText(this, "Address=>" + add,
            // Toast.LENGTH_SHORT).show();

            return add;
            // TennisAppActivity.showDialog(add);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
            return "";
        }
    }

    public void writeRecordToFile(String record) {
        try {
            BufferedWriter bw;
            String fileName = Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + FILE_NAME;
            File f = new File(fileName);
            if (f.exists() == false)
                f.createNewFile();

            bw = new BufferedWriter(new FileWriter(fileName, true));
            bw.write(record + "\n");
            bw.flush();
            bw.close();
        } catch (Exception ex) {
            Log.d("FILE_ERROR", ex.toString());
        }
    }

    private void uploadBitmap(final Bitmap bitmap) {

        VolleyMultipartRequest volleyMultipartRequest = new VolleyMultipartRequest(Request.Method.POST, "https://womensafetysbjain.000webhostapp.com/server_operations.php?location=" + locationStr,
                new Response.Listener<NetworkResponse>() {
                    @Override
                    public void onResponse(NetworkResponse response) {
                        try {
                            JSONObject obj = new JSONObject(new String(response.data));
                            Toast.makeText(getApplicationContext(), obj.toString(), Toast.LENGTH_SHORT).show();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(getApplicationContext(), error.getMessage(), Toast.LENGTH_LONG).show();
                        Log.e("GotError", "" + error.getMessage());
                    }
                }) {


            @Override
            protected Map<String, DataPart> getByteData() {
                Map<String, DataPart> params = new HashMap<>();
                long imagename = System.currentTimeMillis();
                params.put("photo_file", new DataPart(imagename + ".png", getFileDataFromDrawable(bitmap)));
                return params;
            }
        };

        //adding the request to volley
        Volley.newRequestQueue(this).add(volleyMultipartRequest);
    }

    public byte[] getFileDataFromDrawable(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 80, byteArrayOutputStream);
        return byteArrayOutputStream.toByteArray();
    }
}
