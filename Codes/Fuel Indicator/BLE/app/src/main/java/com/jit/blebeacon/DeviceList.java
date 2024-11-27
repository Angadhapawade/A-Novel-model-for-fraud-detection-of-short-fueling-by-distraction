package com.jit.blebeacon;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class DeviceList extends Activity
{
    //widgets
    Button btnPaired;
    ListView devicelist;
    //Bluetooth
    private BluetoothAdapter myBluetooth = null;
    private Set<BluetoothDevice> pairedDevices;
    public static String EXTRA_ADDRESS = "device_address";
    private Handler _handler = new Handler();
    /* Get Default Adapter */
    private BluetoothAdapter _bluetooth = BluetoothAdapter.getDefaultAdapter();
    /* Storage the BT devices */
    private List<BluetoothDevice> _devices = new ArrayList<BluetoothDevice>();
    /* Discovery is Finished */
    private volatile boolean _discoveryFinished;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_list);

        //Calling widgets
        btnPaired = (Button)findViewById(R.id.button);
        devicelist = (ListView)findViewById(R.id.listView);

        //if the device has bluetooth
        myBluetooth = BluetoothAdapter.getDefaultAdapter();

        if(myBluetooth == null)
        {
            //Show a mensag. that the device has no bluetooth adapter
            Toast.makeText(getApplicationContext(), "Bluetooth Device Not Available", Toast.LENGTH_LONG).show();

            //finish apk
            finish();
        }
        else if(!myBluetooth.isEnabled())
        {
                //Ask to the user turn the bluetooth on
                Intent turnBTon = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(turnBTon,1);
        }

        /* Register Receiver */
        IntentFilter discoveryFilter = new IntentFilter(
                BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        registerReceiver(_discoveryReceiver, discoveryFilter);
        IntentFilter foundFilter = new IntentFilter(
                BluetoothDevice.ACTION_FOUND);
        registerReceiver(_foundReceiver, foundFilter);

        /* show a dialog "Scanning..." */
        SamplesUtils.indeterminate(DeviceList.this, _handler,
                getResources().getString(R.string.scaning), _discoveryWorkder,
                new DialogInterface.OnDismissListener()
                {
                    public void onDismiss(DialogInterface dialog)
                    {

                        for (; _bluetooth.isDiscovering();)
                        {

                            _bluetooth.cancelDiscovery();
                        }

                        _discoveryFinished = true;
                    }
                }, true);

        btnPaired.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                pairedDevicesList();
            }
        });

        //Open paired device list
        pairedDevicesList();
    }

    private void pairedDevicesList()
    {
        pairedDevices = myBluetooth.getBondedDevices();
        ArrayList list = new ArrayList();

        if (pairedDevices.size()>0)
        {
            for(BluetoothDevice bt : pairedDevices)
            {
                list.add(bt.getName() + "\n" + bt.getAddress()); //Get the device's name and the address
            }
        }
        else
        {
            Toast.makeText(getApplicationContext(), "No Paired Bluetooth Devices Found.", Toast.LENGTH_LONG).show();
        }

        final ArrayAdapter adapter = new ArrayAdapter(this,android.R.layout.simple_list_item_1, list);
        devicelist.setAdapter(adapter);
        devicelist.setOnItemClickListener(myListClickListener); //Method called when the device from the list is clicked

        String myAddress = "HC-05";
        for(int count=0;count<list.size();count++) {
            String info = list.get(count).toString();
            if(info.contains(myAddress)) {
                String address = info.substring(info.length() - 17);
                Log.d("ADDRESS", address);
                // Make an intent to start next activity.
                Intent i = new Intent(DeviceList.this, MainActivity.class);
                //Change the activity.
                i.putExtra(EXTRA_ADDRESS, address); //this will be received at MainScreen (class) Activity
                startActivity(i);
            }
        }

    }

    private AdapterView.OnItemClickListener myListClickListener = new AdapterView.OnItemClickListener()
    {
        public void onItemClick (AdapterView<?> av, View v, int arg2, long arg3)
        {
            // Get the device MAC address, the last 17 chars in the View
            String info = ((TextView) v).getText().toString();
            String address = info.substring(info.length() - 17);

            // Make an intent to start next activity.
            Intent i = new Intent(DeviceList.this, MainActivity.class);
            //Change the activity.
            i.putExtra(EXTRA_ADDRESS, address); //this will be received at MainControl (class) Activity
            startActivity(i);
        }
    };

    private BroadcastReceiver _foundReceiver = new BroadcastReceiver()
    {
        public void onReceive(Context context, Intent intent)
        {
            /* get the search results */
            BluetoothDevice device = intent
                    .getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
            /* add to list */
            _devices.add(device);
            /* show the devices list */
            showDevices();
        }
    };
    private BroadcastReceiver _discoveryReceiver = new BroadcastReceiver()
    {

        @Override
        public void onReceive(Context context, Intent intent)
        {
            /* unRegister Receiver */
            Log.d("EF-BTBee", ">>unregisterReceiver");
            unregisterReceiver(_foundReceiver);
            unregisterReceiver(this);
            _discoveryFinished = true;
        }
    };

    /* Show devices list */
    protected void showDevices()
    {
        List<String> list = new ArrayList<String>();
        if (_devices.size() > 0)
        {
            for (int i = 0, size = _devices.size(); i < size; ++i)
            {
                StringBuilder b = new StringBuilder();
                BluetoothDevice d = _devices.get(i);
                b.append(d.getAddress());
                b.append('\n');
                b.append(d.getName());
                String s = b.toString();
                list.add(s);
            }
        }
        else
            list.add(getResources().getString(R.string.nodevice));
        Log.d("EF-BTBee", ">>showDevices");
        final ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, list);
        _handler.post(new Runnable()
        {
            public void run()
            {
                devicelist.setAdapter(adapter);
                /* Prompted to select a server to connect */
                Toast.makeText(getBaseContext(),
                        getResources().getString(R.string.selectonedevice),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }
    private Runnable _discoveryWorkder = new Runnable()
    {
        public void run()
        {
            /* Start search device */
            _bluetooth.startDiscovery();
            Log.d("EF-BTBee", ">>Starting Discovery");
            for (;;)
            {
                if (_discoveryFinished)
                {
                    Log.d("EF-BTBee", ">>Finished");
                    break;
                }
                try
                {
                    Thread.sleep(100);
                }
                catch (InterruptedException e)
                {
                }
            }
        }
    };

    @Override
    protected void onPause() {
        super.onPause();
        finish();
    }
}
