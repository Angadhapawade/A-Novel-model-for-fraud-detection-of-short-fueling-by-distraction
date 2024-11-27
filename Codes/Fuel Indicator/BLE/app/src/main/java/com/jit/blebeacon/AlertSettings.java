package com.jit.blebeacon;


import java.io.File;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.Toast;

public class AlertSettings extends Activity 
{
	private static final int PICK_CONTACT_CALL = 1;
	private static final int PICK_CONTACT_SMS = 2;
	EditText txtPhoneNumbersSMS;
	EditText txtPhoneNumberCalls;
	EditText txtEmergencySMS;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.app_settings);
		
		//Load the preference manager
		final SharedPreferences sp = getSharedPreferences("SETTINGS",0);
		
		txtPhoneNumbersSMS = (EditText)findViewById(R.id.txtPhoneNumbersSMS);
		txtPhoneNumberCalls = (EditText)findViewById(R.id.txtPhoneNumberCall);
		final CheckBox chkSendLocation = (CheckBox)findViewById(R.id.chkSendLocation);
		final CheckBox chkSendIMEI = (CheckBox)findViewById(R.id.chkSendIMEI);
		txtEmergencySMS = (EditText)findViewById(R.id.txtEmergencyMessage);
		final Button btnSaveSettings = (Button)findViewById(R.id.btnSettingsSave);
		final Button btnDiscardSettings = (Button)findViewById(R.id.btnDiscardChanges);
		
		final Button btnPickContactCall = (Button)findViewById(R.id.btnPickContactCall);
		final Button btnPickContactSMS = (Button)findViewById(R.id.btnPickContactSMS);
		final Button btnPickSMS = (Button)findViewById(R.id.btnPickSMS);
		

		btnPickContactCall.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) 
			{
				Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
				startActivityForResult(intent, PICK_CONTACT_CALL);	
			}
		});
		
		btnPickContactSMS.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) 
			{
				Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
				startActivityForResult(intent, PICK_CONTACT_SMS);	
			}
		});
		
		btnPickSMS.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) 
			{
				Intent intent = new Intent(Intent.ACTION_MAIN);
				intent.addCategory(Intent.CATEGORY_DEFAULT);
				intent.setType("vnd.android-dir/mms-sms");
				startActivity(intent);
				
				Toast.makeText(getApplicationContext(), "Copy any SMS from Inbox and Paste in the Application", Toast.LENGTH_LONG).show();
			}
		});
		
		//Load the preferences
		String PhoneNumbersSMS = sp.getString("PHONE_NUMBERS_SMS", "");
		String PhoneNumberCall = sp.getString("PHONE_NUMBER_CALL", "");
		String EmergencySMS = sp.getString("EMERGENCY_SMS", "Please save me, I am in trouble");
		boolean SendIMEI = sp.getBoolean("SEND_IMEI", true);
		boolean SendLocation = sp.getBoolean("SEND_LOCATION", true);
		
		txtPhoneNumbersSMS.setText(PhoneNumbersSMS);
		txtPhoneNumberCalls.setText(PhoneNumberCall);
		txtEmergencySMS.setText(EmergencySMS);
		chkSendIMEI.setChecked(SendIMEI);
		chkSendLocation.setChecked(SendLocation);
		
		
		btnDiscardSettings.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				finish();
			}
		});
		
		OnCheckedChangeListener occl = new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				// TODO Auto-generated method stub
				if(isChecked)
				{
					Toast.makeText(AlertSettings.this, "Good. Now you can be tracked more easily in case of Emergency", Toast.LENGTH_SHORT).show();
				}
				else
				{
					Toast.makeText(AlertSettings.this, "Checking this would help your near ones track you", Toast.LENGTH_SHORT).show();
				}
			}
		};
		chkSendIMEI.setOnCheckedChangeListener(occl);
		chkSendLocation.setOnCheckedChangeListener(occl);
		
		btnSaveSettings.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				//Save the Settings
				SharedPreferences.Editor edit = sp.edit();
				edit.putString("PHONE_NUMBERS_SMS", txtPhoneNumbersSMS.getText().toString());
				edit.putString("PHONE_NUMBER_CALL", txtPhoneNumberCalls.getText().toString());
				edit.putString("EMERGENCY_SMS", txtEmergencySMS.getText().toString());
				edit.putBoolean("SEND_IMEI", chkSendIMEI.isChecked());
				edit.putBoolean("SEND_LOCATION", chkSendLocation.isChecked());
				edit.commit();
				
				Toast.makeText(AlertSettings.this, "Settings Saved", Toast.LENGTH_SHORT).show();
				
				finish();
			}
		});
	}
	
	@Override
	public void onActivityResult(int reqCode, int resultCode, Intent data){
	    super.onActivityResult(reqCode, resultCode, data);
	 
	    switch(reqCode)
	    {
	       case (PICK_CONTACT_CALL):
	         if (resultCode == Activity.RESULT_OK){
	             Uri contactData = data.getData();
	             Cursor c = managedQuery(contactData, null, null, null, null);
	             ContentResolver cr = getContentResolver();
	             
	             if (c.moveToFirst())
	             {
	            	 try
	            	 {
	            		 String id = c.getString(c.getColumnIndex(ContactsContract.Contacts._ID));
	            		 if (Integer.parseInt(c.getString(c.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))) > 0) 
	            		 {
	            			Cursor pCur = cr.query(
    			 		    ContactsContract.CommonDataKinds.Phone.CONTENT_URI, 
    			 		    null, 
    			 		    ContactsContract.CommonDataKinds.Phone.CONTACT_ID +" = ?", 
    			 		    new String[]{id}, null);
	            			String phone = "";
    			 	        while (pCur.moveToNext()) 
    			 	        {
    			 	        	try
    			 	        	{
    			 	        		phone = phone + pCur.getString(pCur.getColumnIndex(ContactsContract.Contacts.Data.DATA1));
    			 	        		break;
    			 	        	}
    			 	        	catch(Exception ex)
    			 	        	{
    			 	        		
    			 	        	}
    			 	        } 
    			 	        pCur.close();
    			 	        /*
    			 	        if(phone.length() > 0)
    			 	        {
    			 	        	phone = phone.substring(0,phone.length()-1);
    			 	        }
    			 	        */
    			 	        txtPhoneNumberCalls.setText(phone);
	            		 }
	            	 }
	            	 catch(Exception ex)
	            	 {
	            		 Toast.makeText(AlertSettings.this, "No name selected", Toast.LENGTH_SHORT).show();
	            	 }
	             }
	         }
	       break;
	       //////////////////////////////
	       case (PICK_CONTACT_SMS):
		         if (resultCode == Activity.RESULT_OK){
		             Uri contactData = data.getData();
		             Cursor c = managedQuery(contactData, null, null, null, null);
		             ContentResolver cr = getContentResolver();
		             
		             if (c.moveToFirst())
		             {
		            	 try
		            	 {
		            		 String id = c.getString(c.getColumnIndex(ContactsContract.Contacts._ID));
		            		 if (Integer.parseInt(c.getString(c.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))) > 0) 
		            		 {
		            			Cursor pCur = cr.query(
	    			 		    ContactsContract.CommonDataKinds.Phone.CONTENT_URI, 
	    			 		    null, 
	    			 		    ContactsContract.CommonDataKinds.Phone.CONTACT_ID +" = ?", 
	    			 		    new String[]{id}, null);
		            			String phone = "";
	    			 	        while (pCur.moveToNext()) 
	    			 	        {
	    			 	        	try
	    			 	        	{
	    			 	        		phone = phone + pCur.getString(pCur.getColumnIndex(ContactsContract.Contacts.Data.DATA1)) + ",";
	    			 	        	}
	    			 	        	catch(Exception ex)
	    			 	        	{
	    			 	        		
	    			 	        	}
	    			 	        } 
	    			 	        pCur.close();
	    			 	        if(phone.length() > 0)
	    			 	        {
	    			 	        	phone = phone.substring(0,phone.length()-1);
	    			 	        }
	    			 	        
	    			 	        //Append a Comma in front
	    			 	        if(txtPhoneNumbersSMS.getText().toString().equals(""))
	    			 	        {
	    			 	        	txtPhoneNumbersSMS.setText(phone);	
	    			 	        }
	    			 	        else
	    			 	        {
	    			 	        	txtPhoneNumbersSMS.setText(txtPhoneNumbersSMS.getText() + "," + phone);
	    			 	        }
		            		 }
		            	 }
		            	 catch(Exception ex)
		            	 {
		            		 Toast.makeText(AlertSettings.this, "No name selected", Toast.LENGTH_SHORT).show();
		            	 }
		             }
		         }
	    }
	    
	    
	}
}
