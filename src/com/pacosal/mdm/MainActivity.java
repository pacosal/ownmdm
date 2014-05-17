/*
 * MainActivity.java
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with GNU gv; see the file COPYING.  If not, write to
 * the Free Software Foundation, Inc., 59 Temple Place - Suite 330,
 * Boston, MA 02111-1307, USA.
 *
 *   Author: Paco Salazar
 * Internet: pacosal.com
 *     Mail: pacosal@gmail.com
*/

package com.pacosal.mdm;

import java.io.OutputStream;

import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.telephony.TelephonyManager;
import android.view.Menu;
import android.view.MenuItem;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;


public class MainActivity extends Activity {

    private static final int REQUEST_CODE_ENABLE_ADMIN = 1;

    // Interaction with the DevicePolicyManager
    DevicePolicyManager mDPM;
    ComponentName mDeviceAdmin;   
    
    AsyncTask<Void, Void, Void> mLocationTask;    
    AsyncTask<Void, Void, Void> mPingTask;    
    AsyncTask<Void, Void, Void> mLogTask;        
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main); 

		Util.activity = this;  
		
		leer();
		
		if (!Util.serverData) { 
			settings();
			return;
		}
		
		try {
	 		PackageInfo pInfo2 = getPackageManager().getPackageInfo("com.pacosal.mdm",PackageManager.GET_META_DATA);
	 	} catch (Exception e) {
		}        
		
		try {
			Intent i = this.getIntent();
			if (i.getExtras().getBoolean("Boot") == true) {
				// viene de boot
				Util.logDebug("boot");
				this.finish();
			}
		} catch (Exception e) {
			Util.logDebug("Exception: " + e.getMessage());
		}
		
		//
        TelephonyManager tm2 = (TelephonyManager) Util.activity.getApplicationContext().getSystemService(Context.TELEPHONY_SERVICE);
        Util.imei = tm2.getDeviceId();		
		mDPM = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
        mDeviceAdmin = new ComponentName(this, MdmDeviceAdminReceiver.class);        
        Util.enableAdmin = mDPM.isAdminActive(mDeviceAdmin);

		if (!Util.enableAdmin) { // activamos
	    	new AlertDialog.Builder(this).setMessage(this.getString(R.string.HowToUse))
	        .setCancelable(false)
	        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
	            public void onClick(DialogInterface dialog, int id) {
	    			inicio();
		        } 
	                
	        }).create().show();  	
		}
		else {
			inicio();
		}
		
		
    }
    

    void inicio() {
		// imei
        TelephonyManager tm2 = (TelephonyManager) Util.activity.getApplicationContext().getSystemService(Context.TELEPHONY_SERVICE);
        Util.imei = tm2.getDeviceId();
        Util.imsi = tm2.getSubscriberId(); 
        
		mDPM = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
        mDeviceAdmin = new ComponentName(this, MdmDeviceAdminReceiver.class);        
        Util.enableAdmin = mDPM.isAdminActive(mDeviceAdmin);

		if (!Util.enableAdmin) { // activamos
            Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
            intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, mDeviceAdmin);
            intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, "Activar administración" );
            startActivityForResult(intent, REQUEST_CODE_ENABLE_ADMIN);
		}        
        
      
		// iniciar GCM
		Util.setGCM(getApplicationContext());
		
		try {
			grabar();
			log(getApplicationContext(), "imsi: " + Util.imsi);
		} catch (Exception e) {
			Util.logDebug("Exception: " + e.getMessage());
		}		
		
		// arrancar alarma de ping
		try {
		    Intent i2 = new Intent(getApplicationContext(), LocAlarmService.class);
		    i2.setAction(Util.INTENT_ACTION_PING_START);
		    getApplicationContext().startService(i2);		
		} catch (Exception e) {
			Util.logDebug("Exception: " + e.getMessage());
		}		
    	
    }
    
    protected void grabar() { 
    	
    	SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
    	SharedPreferences.Editor editor = settings.edit();
    	
    	editor.putString("imsi", Util.imsi);

    	editor.commit();
    	
    	
    }    
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
    	Util.logDebug("onCreateOptionsMenu");
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }

    @Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    // Handle item selection
	    switch (item.getItemId()) {  
	    case R.id.menu_settings:
	        settings();
	        return true; 
	    case R.id.menu_about:
	        version();  
	        return true; 
	    case R.id.menu_root:
	        root();  
	        return true; 
	    default:
	        return super.onOptionsItemSelected(item);
	    }
	}	    

    public void root() {

    	try {
       		
       		Process p = null;
       		OutputStream o = null;
       		
       		p = Runtime.getRuntime().exec("su");
       		o = p.getOutputStream();
       		
       		String cmd = "LD_LIBRARY_PATH=/vendor/lib:/system/lib input keyevent 82"; // en ics hay que incluir primero la carga de las librerias
            o.write((cmd + "\n").getBytes("ASCII"));       		
            
		} catch (Exception e) {
			Util.logDebug("Exception " + e.getMessage());
		}
    }     
    
	/**
	 * settings
	 */
	private void settings() {
		Intent i = new Intent(MainActivity.this, MdmPreferenceActivity.class);
        startActivityForResult(i, 500);
		
	}
	
	private void version() {
		try {
			PackageInfo pInfo2 = getPackageManager().getPackageInfo("com.pacosal.mdm",PackageManager.GET_META_DATA);
		
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setMessage("ownMdm Version: " + pInfo2.versionName + "\n");
	        builder.setCancelable(false);
	        builder.setPositiveButton(this.getString(R.string.Accept), new DialogInterface.OnClickListener() {
	           public void onClick(DialogInterface dialog, int id) {
	        	   return;
	           }
	        });
			AlertDialog alert = builder.create();
			alert.show();			

	 	} catch (Exception e) {
	 		Util.logDebug("Exception version: " + e.getMessage());
		}
	}
		
    /**
     * log
     */
    private void log(final Context context, final String mensaje) {
        mLogTask = new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... params) {
                ServerUtilities.sendLog(context, mensaje);
                return null;
            }

            @Override
            protected void onPostExecute(Void result) {
                mLogTask = null;
            }

        };
        mLogTask.execute(null, null, null);
    	
    }
    
    @Override
    protected void onResume() {
        super.onResume();
    }
    
    
    protected void leer() {
    	Util.leer(getApplicationContext());
    }    
    
    
    
}
