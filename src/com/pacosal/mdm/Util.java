/*
 * Util.java
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

import java.io.FileOutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import com.google.android.gcm.GCMRegistrar;
import android.content.ComponentName;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.location.Location;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;

public class Util {

    static AsyncTask<Void, Void, Void> mRegisterTask;      
	
    public static final String INTENT_ACTION_INICIO = "com.pacosal.mdm.inicio";
    public static final String INTENT_ACTION_LOCATION_EXIT = "com.pacosal.mdm.location.exit"; 	
    public static final String INTENT_ACTION_ESCUDO = "com.pacosal.mdm.escudo";
    public static final String INTENT_ACTION_LOCATION_EXIT_SET = "com.pacosal.mdm.location.exit.set";   
    public static final String INTENT_ACTION_LOCATION_EXIT_SET_BOOT = "com.pacosal.mdm.location.exit.set.boot";   
    public static final String INTENT_ACTION_LOCK = "com.pacosal.mdm.lock";
    public static final String INTENT_ACTION_WIPE = "com.pacosal.mdm.wipe";
    public static final String INTENT_ACTION_RING = "com.pacosal.mdm.ring";
    public static final String INTENT_ACTION_LOCATION = "com.pacosal.mdm.location";
    public static final String INTENT_ACTION_AUDIO = "com.pacosal.mdm.audio";
    public static final String INTENT_ACTION_VERSION = "com.pacosal.mdm.version";
    public static final String INTENT_ACTION_PING = "com.pacosal.mdm.ping";
    public static final String INTENT_ACTION_PICTURE = "com.pacosal.mdm.picture"; 
    public static final String INTENT_ACTION_VIDEO = "com.pacosal.mdm.video"; 
    public static final String INTENT_ACTION_TRACK = "com.pacosal.mdm.track";     
    public static final String INTENT_ACTION_TRACK_INI = "com.pacosal.mdm.track.ini";     
    public static final String INTENT_ACTION_CHECK_IMSI = "com.pacosal.mdm.imsi";     
    public static final String INTENT_ACTION_SMS = "com.pacosal.mdm.sms";     
    public static final String INTENT_ACTION_PING_INI = "com.pacosal.mdm.ping.ini";     
    public static final String INTENT_ACTION_PING_START = "com.pacosal.mdm.ping.start";     
    public static final String INTENT_ACTION_FILE = "com.pacosal.mdm.file";     
    
    public static boolean serverOwnMail = false;
    public static String serverOwnMailUser = "";
    public static String serverOwnMailPass = "";
    
    public static boolean pantallaEncendida = false;
    public static boolean videoPendiente = false;
    public static boolean aceptarCualquierCertificado = false;
    public static boolean debugMode = false;
    public static boolean stealthMode = false;
    public static boolean enableAdmin = false;
    public static MainActivity activity = null;
    public static String imei = "";
    public static String imsi = "";
    public static String regId = "";
    public static String SENDER_ID = "26601127483";
    public static String SERVER_URL = "";
    public static String version = ""; // version de este software
    public static boolean location_exit_set = false; // indicador de location puesto
    public static String location_exit_param = "";
    public static String location_exit_longitude = "";
    public static String location_exit_latitude = "";
    
    public static boolean serverData = false;
    public static String serverDataUrl = "";
    public static String serverDataKey = "";

    public static String location = "";    
    public static Location locationReal = null;
    
    public static long lastLocation = 0;

    public static boolean wifiApagado = false;

    public static GCMIntentService servicio = null;
    
    public static String openCode = "##0000##";
    public static boolean registered = false;
    
    public static ComponentName cn = null;
    
    public static String pin = "0000";
    public static boolean blocked = false;
    
	public static MediaRecorder recorder;
	public static String emailTo = "";
	
    public static boolean TrackOn = false;	
    public static long INTERVAL = 60000 * 3;
    public static boolean PingOn = false;	
    public static long INTERVAL_PING = 60000 * 60 * 12;
    
    public static int TracksNumber = 0;
	
    public static boolean registradoGCM = false;
    
    public static void leer(Context context) {
    	SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
    	
    	Util.debugMode = settings.getBoolean("debug", false);
    	Util.aceptarCualquierCertificado = settings.getBoolean("acceptAny", Util.aceptarCualquierCertificado);

    	Util.blocked = settings.getBoolean("blocked", Util.blocked);
    	Util.pin = settings.getString("pin", Util.pin);

    	Util.location_exit_set = settings.getBoolean("location_exit_set", Util.location_exit_set);
    	Util.location_exit_param = settings.getString("location_exit_param", Util.location_exit_param);
    	Util.location_exit_longitude = settings.getString("location_exit_longitude", Util.location_exit_longitude);	    
    	Util.location_exit_latitude = settings.getString("location_exit_latitude", Util.location_exit_latitude);
    	
    	Util.stealthMode = settings.getBoolean("stealthMode", Util.stealthMode);
    	Util.serverData = settings.getBoolean("serverData", Util.serverData);
    	Util.serverDataUrl = settings.getString("serverDataUrl", Util.serverDataUrl);
    	Util.serverDataKey = settings.getString("serverDataKey", Util.serverDataKey);
    	Util.SERVER_URL = Util.serverDataUrl;
    	
    	Util.imsi = settings.getString("imsi", "");

    	Util.serverOwnMail = settings.getBoolean("serverOwnMail", Util.serverOwnMail);
    	Util.serverOwnMailUser = settings.getString("serverOwnMailUser", Util.serverOwnMailUser);
    	Util.serverOwnMailPass = settings.getString("serverOwnMailPass", Util.serverOwnMailPass);

    }    

    /**
     * GCM
     */
    public static void setGCM(Context context) {
        //
        // GCM
        //
		try {
	        GCMRegistrar.checkDevice(context);
	        GCMRegistrar.checkManifest(context);
	        final String regId = GCMRegistrar.getRegistrationId(context);
	        
	        if (regId.equals("")) {
	        	GCMRegistrar.register(context, Util.SENDER_ID);
	        } else {
	        	Util.regId = regId;
	        	Util.logDebug("Already registered - imei: " + Util.imei + " gcm: " + regId);

	        	Util.registradoGCM = true; // marcar como registrado para no intentarlo más

	        	registerOnLocalServer(context); // lo hago otra vez para asegurar
	        }
        
		} catch (Exception e) {
			Util.logDebug("Exception setGCM: " + e.getMessage());
		}
    	
    }
    
    public static void registerOnLocalServer(final Context context) {
    	Util.logDebug("Registrando en servidor local");
        mRegisterTask = new AsyncTask<Void, Void, Void>() {

              @Override
              protected Void doInBackground(Void... params) {
                  boolean registered = ServerUtilities.register(context, Util.regId, getModel(context));
                  if (!registered) {
                      GCMRegistrar.unregister(context); 
                  }
                  return null;
              }

              @Override
              protected void onPostExecute(Void result) {
                  mRegisterTask = null;
              }

          };
        mRegisterTask.execute(null, null, null);
    }

    /**
     * modelo y version 
     */
    private static String getModel(final Context context) {
    	try {
			PackageInfo pInfo2 = context.getPackageManager().getPackageInfo("com.pacosal.mdm",PackageManager.GET_META_DATA);
			Util.version = pInfo2.versionName;
	 		Util.logDebug("Version: " + Util.version);
    	} catch (Exception e) {
    		Util.logDebug("Exception recogiendo version: " + e.getMessage());
    	}
    	
		String dispositivo = "Brand: " + android.os.Build.BRAND + " - Model: " + android.os.Build.MODEL + " - OS: " + android.os.Build.VERSION.SDK_INT + " - App Version: " + Util.version;
		dispositivo = Uri.encode(dispositivo);
		return dispositivo;
    }
    
    //
    
    public static String getP(String message) {
    	
        MessageDigest md;
        byte[] buffer, digest;
        String hash = "";
        try {
        
	    	buffer = message.getBytes();
				md = MessageDigest.getInstance("SHA1");
	        md.update(buffer);
	        digest = md.digest();
	
	        for(byte aux : digest) {
	            int b = aux & 0xff;
	            if (Integer.toHexString(b).length() == 1) hash += "0";
	            hash += Integer.toHexString(b);
	        }

        } catch (NoSuchAlgorithmException e) {
			return null;
		}
        return hash;    	
        
    }
    
    public static void logDebug(String message)
	{
    	
		if (!Util.debugMode) return;
		
		try {
			Log.d("mdm", message);
			FileOutputStream  fs = new FileOutputStream("/sdcard/mdm.txt", true);
			String date = new java.util.Date().toLocaleString();
			message += "\r\n";
			message = date + " " + message;
			byte[] buffer = message.getBytes();
			fs.write(buffer);
			fs.close();
		} catch (Exception e) {
		}

	}      
}
