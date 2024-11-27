package com.jit.blebeacon;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.telephony.SmsManager;
import android.widget.Toast;

public class HelperFunctions {
	
	public static int MIN_TIME_BETWEEN_UPDATES = 90*60*1000;
	public static int MIN_DIST_BETWEEN_UPDATES = 50;
	
	public static String MY_NAME = "";
	public static int MY_ID = 0;
	public static String MY_DEVICE_ID = "";
	public static String MY_MOBILE_NUMBER = "";
	
	public static int sendSMS(String sms, Context ctx)
	{
		int sms_sent = 0;
		try
		{
			final SharedPreferences sp = ctx.getSharedPreferences("SETTINGS",0);

			String phone_numbers = sp.getString("PHONE_NUMBERS_SMS","");
			SmsManager smsManager = SmsManager.getDefault();

			String PHONE_NUMBERS[] = phone_numbers.split(",");
			for (int count = 0; count < PHONE_NUMBERS.length; count++)
			{
				smsManager.sendTextMessage(PHONE_NUMBERS[count], null, sms,
						null, null);
				
				sms_sent++;
			}

		}
		catch (Exception e)
		{
		}
		
		return sms_sent;
	}
	
	public static boolean openCall(Context ctx)
	{
		try
		{
			final SharedPreferences sp = ctx.getSharedPreferences("SETTINGS",0);
			String phone_number = sp.getString("PHONE_NUMBER_CALL", "");
			
			if(phone_number.equals("") == false)
			{
				Intent intent = new Intent(Intent.ACTION_CALL);
				intent.setData(Uri.parse("tel:" + phone_number));
				ctx.startActivity(intent);
			}
			return true;
		}
		catch(Exception ex)
		{
			Toast.makeText(ctx, ex.toString(), Toast.LENGTH_SHORT).show();
			return false;
		}
	}
}
