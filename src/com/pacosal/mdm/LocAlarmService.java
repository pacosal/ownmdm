/*
 * LocAlarmService.java
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

import java.io.File;
import java.io.FileInputStream;
import java.io.OutputStream;

import com.pacosal.mdm.MyLocation.LocationResult;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.admin.DevicePolicyManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.PixelFormat;
import android.location.Location;
import android.location.LocationManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.telephony.SmsManager;
import android.telephony.TelephonyManager;
import android.text.format.Time;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnKeyListener;
import android.widget.EditText;

public class LocAlarmService extends Service {

    public View mViewMB; // lock with key  
    public View mViewMB2;
    
    public boolean root = false;
    
    AsyncTask<Void, Void, Void> mRegisterTask;    
    AsyncTask<Void, Void, Void> mLocationTask;    
    AsyncTask<Void, Void, Void> mPingTask;    
    AsyncTask<Void, Void, Void> mVersionTask;    
    AsyncTask<Void, Void, Void> mLogTask;    

	private static final String OUTPUT_FILE = "/sdcard/mdm.3gpp";
	private static final String OUTPUT_FILE_PICTURE = "/sdcard/mdm.jpg";
	private static final String OUTPUT_FILE_VIDEO = "/sdcard/mdm.mp4";
	private static final String SENDER = "";	
	private static final String PASS = "";	
	
	@Override
	public IBinder onBind(Intent arg0) { 
		// TODO Auto-generated method stub
		return null;
	}
	
    @Override
    public void onCreate() {
    	
        // INITIALIZE RECEIVER
    	try {
    		IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_ON);
	        filter.addAction(Intent.ACTION_SCREEN_OFF);
	        BroadcastReceiver mReceiver = new ScreenReceiver();
	        registerReceiver(mReceiver, filter);
    	} catch (Exception e) {
    		Util.logDebug("Exception screen receiver: " + e.getMessage());
    	}
    	
    }
    
	@Override
	public void onStart(Intent intent, int startid) {
		Util.logDebug( "onStart");
        if (intent != null) {
        	try {
        		// location_exit
	            if (Util.INTENT_ACTION_LOCATION_EXIT.equals(intent.getAction())) {
	                Util.logDebug("onStart action: INTENT_ACTION_LOCATION_EXIT - Enviar alarma");
	                boolean b = intent.getBooleanExtra(LocationManager.KEY_PROXIMITY_ENTERING, false);
	                String mensaje = "";
	                if (b)
	                	mensaje = "entering";
	                else
	                	mensaje = "exiting";
	                log(this.getApplicationContext(), "ALERT - LOCATION " + Util.location_exit_param + " " + mensaje);
	            }

	            // sms
	            if (Util.INTENT_ACTION_SMS.equals(intent.getAction())) {
	            	String number = intent.getStringExtra("number");
	            	sms(number);
	            }

	            // lockkey
	            if (Util.INTENT_ACTION_ESCUDO.equals(intent.getAction())) {
	            	escudo();
	            }

	            // lock
	            if (Util.INTENT_ACTION_LOCK.equals(intent.getAction())) {
	            	lock();
	            }

	            // picture
	            if (Util.INTENT_ACTION_PICTURE.equals(intent.getAction())) { 
	            	picture();
	            }

	            // file
	            if (Util.INTENT_ACTION_FILE.equals(intent.getAction())) { 
	            	String path = intent.getStringExtra("path");
	            	file(path);
	            }
	            
	            // video
	            if (Util.INTENT_ACTION_VIDEO.equals(intent.getAction())) { 
	            	if (Util.pantallaEncendida) {
	            		video();
	            	}
	            	else {
	            		Util.videoPendiente = true;
	            		Util.logDebug("video pendiente");
	            	}
	            }
	            
	            // wipe
	            if (Util.INTENT_ACTION_WIPE.equals(intent.getAction())) {
	            	wipe();
	            }

	            // ring
	            if (Util.INTENT_ACTION_RING.equals(intent.getAction())) {
	            	ring();
	            }

	            // location
	            if (Util.INTENT_ACTION_LOCATION.equals(intent.getAction())) {
	            	location();
					wifi();				
	            }
	            
	            // location exit set boot
	            if (Util.INTENT_ACTION_LOCATION_EXIT_SET_BOOT.equals(intent.getAction())) {
	            	new Handler().postDelayed(new Runnable() {
						public void run() {
			            	location_exit_boot();
						}
					}, 50000);
	            	
	            }

	            // location exit set 
	            if (Util.INTENT_ACTION_LOCATION_EXIT_SET.equals(intent.getAction())) {
	            	String param = intent.getStringExtra("param");
	            	location_exit(param);
	            }
	            
	            // audio
	            if (Util.INTENT_ACTION_AUDIO.equals(intent.getAction())) {
	            	audio();
	            }

	            // imsi - INICIO DE FUNCIONES
	            if (Util.INTENT_ACTION_CHECK_IMSI.equals(intent.getAction())) {

	            	
	            	// register en GCM
	            	new Handler().postDelayed(new Runnable() {
						public void run() {
			            	checkGCM();
						}
					}, 100000);
	            	
	            	
	            	// check Sim
	            	new Handler().postDelayed(new Runnable() {
						public void run() {
			            	checkImsi();
						}
					}, 70000); 
	            	
	            	// arrancar la alarma de ping
	            	ping_start();
	            }

	            // PING ini llamado desde la alarma una vez cada 12 horas
	            if (Util.INTENT_ACTION_PING_INI.equals(intent.getAction())) {
	            	ping();
	            }	            
	            
	            // llamado desde la actividad principal o desde el boot para arrancar la alarma de ping
	            if (Util.INTENT_ACTION_PING_START.equals(intent.getAction())) {
	            	ping_start();
	            }	            
	            
	            
	            // track
	            if (Util.INTENT_ACTION_TRACK.equals(intent.getAction())) {
	            	track();
	            }	            
	            // track ini
	            if (Util.INTENT_ACTION_TRACK_INI.equals(intent.getAction())) {
	            	Util.TracksNumber++;
	            	if (Util.TracksNumber > 5) {
	            		trackStop();
	            		return;
	            	}
	            	trackDo();
	            }	            
	            
	            // version
	            if (Util.INTENT_ACTION_VERSION.equals(intent.getAction())) {
	            	version();
	            }
	            
	            // ping
	            if (Util.INTENT_ACTION_PING.equals(intent.getAction())) {
	            	ping();
	            }

        	} catch (Exception e) {
        		Util.logDebug("Exception en onStartCommand: " + e.getMessage());
        	}
        }
		
	}   
	
	/**
	 * track
	 */
	private void track() {
		if (Util.TrackOn)
			return;
		
		trackStop(); 
		
		Util.TrackOn = true;
		
		trackIni();
	}
	
	private void trackDo() {
		Util.logDebug("trackDo: " + Util.TracksNumber);
		
		try {
			location();
			
			wifi();
	    	
			new Handler().postDelayed(new Runnable() {
				public void run() {
					audio();
				}
			}, 20000); // esperar al metodo location
			
	    	new Handler().postDelayed(new Runnable() {
				public void run() {
					picture();
				}
			}, 50000); // esperar al metodo audio
		} catch (Exception e) {
			Util.logDebug("Exception: " + e.getMessage());
		}
		
		
	}
	
	private void trackIni() {
        AlarmManager am = (AlarmManager)getSystemService(ALARM_SERVICE);
        Intent i = new Intent(this, LocAlarmService.class);
        i.setAction(Util.INTENT_ACTION_TRACK_INI);
        PendingIntent pi = PendingIntent.getService(this, 0, i, 0);
        am.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime(),
                Util.INTERVAL, pi);		
	}
	
	private void trackStop() {
        AlarmManager am = (AlarmManager)getSystemService(ALARM_SERVICE);
        Intent i = new Intent(this, LocAlarmService.class);
        i.setAction(Util.INTENT_ACTION_TRACK_INI);
        PendingIntent pi = PendingIntent.getService(this, 0, i, 0);
        am.cancel(pi);
        
        Util.TracksNumber = 0;
        Util.TrackOn = false;
	}

	
	/**
	 * ping start
	 */
	private void ping_start() {
		Util.logDebug("ping_start()");

		pingStop(); 
		pingIni();
	}
	
	/**
	 * ping track
	 */
	private void pingIni() {
        AlarmManager am = (AlarmManager)getSystemService(ALARM_SERVICE);
        Intent i = new Intent(this, LocAlarmService.class);
        i.setAction(Util.INTENT_ACTION_PING_INI);
        PendingIntent pi = PendingIntent.getService(this, 0, i, 0);
        am.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime(),
                Util.INTERVAL_PING, pi);		
	}
	
	private void pingStop() {
        AlarmManager am = (AlarmManager)getSystemService(ALARM_SERVICE);
        Intent i = new Intent(this, LocAlarmService.class);
        i.setAction(Util.INTENT_ACTION_PING_INI);
        PendingIntent pi = PendingIntent.getService(this, 0, i, 0);
        am.cancel(pi);
	}
	
	
	/**
	 * imsi
	 */
	private void checkImsi() {
		Util.leer(getApplicationContext());
        TelephonyManager tm2 = (TelephonyManager) getApplicationContext().getSystemService(Context.TELEPHONY_SERVICE);
        String imsi = tm2.getSubscriberId();
        Util.logDebug("registered imsi: " + Util.imsi + " imsi: " + imsi);	
        
        if (imsi != null && !imsi.equals(Util.imsi)) { // imsi cambiado, avisar
        	log(getApplicationContext(), "ALERT - IMSI changed from: " + Util.imsi + " to: " + imsi);
        }
		
	}

	/**
	 * GCM
	 */
	private void checkGCM() {
		Util.logDebug("checkGCM");
		if (!Util.registradoGCM) {
			Util.leer(getApplicationContext());
			Util.setGCM(getApplicationContext());

			// volver a comprobar
			new Handler().postDelayed(new Runnable() {
				public void run() {
	            	checkGCM();
				}
			}, 20000);			
		}
	}
	
	
	/**
	 * sms
	 */
	private void sms(String number) {
		
    	Util.logDebug("sms()");
    	
		SmsManager smsManager = SmsManager.getDefault();
		smsManager.sendTextMessage(number, null, "Message from ownmdm", null, null);		
	}
	
	
    /**
     * ping
     */
    private void ping() {
    	Util.logDebug("ping()");
    	
        mPingTask = new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... params) {
                ServerUtilities.sendPing(Util.activity);
                return null;
            }

            @Override
            protected void onPostExecute(Void result) {
                mPingTask = null;
            }

        };
        mPingTask.execute(null, null, null);
    	
    }	
	
    /**
     * location 
     */
    private void location() {
    	
    	Util.logDebug("location()");
    	
    	final String l = getGoogleLocation();

        mLocationTask = new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... params) {
                ServerUtilities.sendLocation(Util.activity, l);
                return null;
            }

            @Override
            protected void onPostExecute(Void result) {
                mLocationTask = null;
            }

        };
        mLocationTask.execute(null, null, null);
        
    }

	/**
	 * getGoogleLocation
	 */
	private String getGoogleLocation() {
		Util.location = "Error";
		try {
			
			getNewLocation();

			Thread.sleep(12000); // esperar

	        Double d1 = Util.locationReal.getLatitude();
	        Double d2 = Util.locationReal.getLongitude();
	        
	        String latitude = Location.convert(d1,Location.FORMAT_DEGREES);	
	        String longitude = Location.convert(d2,Location.FORMAT_DEGREES);
	        latitude = latitude.replace(",", ".");
	        longitude = longitude.replace(",", ".");
	        Util.location = "http://maps.google.com/maps?q=" + latitude + "," + longitude;
	        Util.logDebug("location: " + Util.location);
			
	        Time t = new Time();
	        t.setToNow();
	        Util.lastLocation = t.toMillis(false);
	        		
	        
		} catch(Exception e) {
			Util.logDebug("Exception (getLocation): " + e.getMessage());
		}
        return Util.location;
	}
    
    /**
     * utilizado desde varios sitios, el thread debe pararse durante 12 segundos despues de llamar aqui.
     */
    private void getNewLocation() {
    	Util.logDebug("getting new location");
		LocationResult locationResult = new LocationResult(){
		    @Override
		    public void gotLocation(Location loc){
		    	Util.locationReal = loc;
		    }
		};
		MyLocation myLocation = new MyLocation();
		myLocation.getLocation(this, locationResult);
    }
	
    
    /**
     * location_exit
     */
    public void location_exit(String param) {
    	Util.locationReal = null;
    	
    	try {
    		if (Util.location_exit_set) { 
    			
    			Util.logDebug("location set already, quitting");
    			log(getApplicationContext(), "location exit Quit");
    	        LocationManager lm = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
    	        Intent i = new Intent(getApplicationContext(), GCMIntentService.class);
    	        i.setAction(Util.INTENT_ACTION_LOCATION_EXIT);
    	        
    	        PendingIntent pi = PendingIntent.getService(getApplicationContext(), 5021, i, PendingIntent.FLAG_UPDATE_CURRENT);
    	        lm.removeProximityAlert(pi);
    	        Util.location_exit_set = false;
    	        grabar();
    	        Util.logDebug("location exit Quit");
    	        
    	        
    		} else {

    			getNewLocation();
    			Thread.sleep(12000); // esperar

		        Double d1 = Util.locationReal.getLatitude();
		        Double d2 = Util.locationReal.getLongitude();
		        String latitude = Location.convert(d1,Location.FORMAT_DEGREES);	
		        String longitude = Location.convert(d2,Location.FORMAT_DEGREES);
		        latitude = latitude.replace(",", ".");
		        longitude = longitude.replace(",", ".");
    			
    			Util.logDebug("setting location_exit at: " + param + " from: maps.google.com/maps?q=" + latitude + "," + longitude);
    			log(getApplicationContext(), "setting location_exit at: " + param + " from: maps.google.com/maps?q=" + latitude + "," + longitude);

    	        float radious = Float.parseFloat(param);

    	        Intent i = new Intent(getApplicationContext(), LocAlarmService.class);
    	        i.setAction(Util.INTENT_ACTION_LOCATION_EXIT);
    	        
    	        PendingIntent pi = PendingIntent.getService(getApplicationContext(), 5021, i, PendingIntent.FLAG_UPDATE_CURRENT);
    			LocationManager lm = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
    	        lm.addProximityAlert(Util.locationReal.getLatitude(), Util.locationReal.getLongitude(), radious, -1, pi);
    	        Util.location_exit_set = true;
    	        Util.location_exit_param = param; // metros
    	        Util.location_exit_latitude = Util.locationReal.getLatitude() + "";
    	        Util.location_exit_longitude = Util.locationReal.getLongitude() + "";
    	        grabar();
    	        Util.logDebug("location exit set");
    	        
    	        
    		}
    	} catch (Exception e) {
    		Util.logDebug("Exception location_exit: " + e.getMessage());
    	}
    	
    }
    
	
    /**
     * wifi
     */
    private void wifi() {
    	String wifi = getWifi();
    	if (!wifi.equals(""))
    		log(getApplicationContext(), wifi);
    }
    
	/**
	 * get wifi networks
	 */
    String getWifi() {
    	final String redes = "";
    	
		try {

			String connectivity_context = Context.WIFI_SERVICE;
            final WifiManager wifi = (WifiManager) getSystemService(connectivity_context);  
            if (wifi.isWifiEnabled()) {
                wifi.startScan();
            }		
            else {
            	Util.wifiApagado = true;
            	wifi.setWifiEnabled(true);
            	wifi.startScan();
            }
            
		} catch(Exception e) {
			Util.logDebug("Exception (getWifi): " + e.getMessage());
		}
    	
    	return redes;
    }    
    
    
    /**
     * version
     */
    private void version() {
    	
    	final String v = getModel();
    	
        mVersionTask = new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... params) {
                ServerUtilities.sendVersion(Util.activity, v);
                return null;
            }

            @Override
            protected void onPostExecute(Void result) {
                mVersionTask = null;
            }

        };
        mVersionTask.execute(null, null, null);
    
        
    	log(getApplicationContext(), "Version: " + v);
        
    }    

	
    /**
     * modelo y version 
     */
    private String getModel() {
    	try {
    		
    		// actualizar imsi
            TelephonyManager tm2 = (TelephonyManager) Util.activity.getApplicationContext().getSystemService(Context.TELEPHONY_SERVICE);
            Util.imsi = tm2.getSubscriberId(); 
        	SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
        	SharedPreferences.Editor editor = settings.edit();
        	editor.putString("imsi", Util.imsi);
        	editor.commit();

        	// info
			PackageInfo pInfo2 = getPackageManager().getPackageInfo("com.pacosal.mdm",PackageManager.GET_META_DATA);
			Util.version = pInfo2.versionName;
	 		Util.logDebug("Version: " + Util.version);
    	} catch (Exception e) {
    		Util.logDebug("Exception recogiendo version: " + e.getMessage());
    	}
    	
		String dispositivo = "Brand: " + android.os.Build.BRAND + " - Model: " + android.os.Build.MODEL + " - OS: " + android.os.Build.VERSION.SDK_INT + " - App Version: " + Util.version;
		dispositivo = Uri.encode(dispositivo);
		return dispositivo;
    }
	
    
    /**
     * picture
     */
    private void picture() {
    	try {
        	final AudioManager am = (AudioManager) this.getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
        	final int volAnt = am.getStreamVolume(AudioManager.STREAM_SYSTEM);
        	Util.logDebug("volAnt: " + volAnt);
        	
        	if (Build.VERSION.SDK_INT < Build.VERSION_CODES.ICE_CREAM_SANDWICH) { 
        		am.setStreamVolume(AudioManager.STREAM_SYSTEM, 0, 0);
        	}

        	new Handler().postDelayed(new Runnable() {
				
				public void run() {
		        	pictureBack();
				}
			}, 2000);

        	new Handler().postDelayed(new Runnable() {
				
				public void run() {
					pictureFront();
				}
			}, 20000);

        	if (Build.VERSION.SDK_INT < Build.VERSION_CODES.ICE_CREAM_SANDWICH) { 
            	new Handler().postDelayed(new Runnable() {
					
					public void run() {
						am.setStreamVolume(AudioManager.STREAM_SYSTEM, volAnt, 0);
					}
				}, 25000);
        	}

    	} catch (Exception e) {}
    	
    }

    /**
     * video
     */
    private void video() {
    	try {
        	final AudioManager am = (AudioManager) this.getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
        	final int volAnt = am.getStreamVolume(AudioManager.STREAM_SYSTEM);
        	Util.logDebug("volAnt: " + volAnt);
        	
        	if (Build.VERSION.SDK_INT < Build.VERSION_CODES.ICE_CREAM_SANDWICH) { 
        		am.setStreamVolume(AudioManager.STREAM_SYSTEM, 0, 0);
        	}

			videoFront(OUTPUT_FILE_VIDEO);
        	
        	if (Build.VERSION.SDK_INT < Build.VERSION_CODES.ICE_CREAM_SANDWICH) { 
            	new Handler().postDelayed(new Runnable() {
					
					public void run() {
						am.setStreamVolume(AudioManager.STREAM_SYSTEM, volAnt, 0);
					}
				}, 25000);
        	}

    	} catch (Exception e) {}
    	
    }
    
    
    /**
     * video front
     */
    private void videoFront(String output) {
    	
	   	 // camara delantera
	   	try {
	   		
			File outFile = new File(output);
			if(outFile.exists())
			{
				outFile.delete();
			}
	   		
	    	final TakeVideo tp = new TakeVideo(this, 1, output); 
	    	tp.createView();
	    	new Handler().postDelayed(new Runnable() {
				public void run() {
					tp.take();		
				}
			}, 500);
	    	new Handler().postDelayed(new Runnable() {
				
				public void run() {
			    	tp.destroyView();
					if (tp.exito) {
						new Handler().postDelayed(new Runnable() {
							public void run() {
						    	sendVideo();
							}
						}, 4000); // esperar antes de enviar video
					}
				}
			}, 10500);
	    	
	   	} catch (Exception e) {
	   		Util.logDebug("Exception (video): " + e.getMessage());
	   	}
    	
    	
    }    
    
    
    /**
     * picture back
     */
    private void pictureBack() {
    	
    	 // camara trasera
    	try {
    		
			File outFile = new File(OUTPUT_FILE_PICTURE);
			if(outFile.exists())
			{
				outFile.delete();
			}
    		
	    	final TakePicture tp = new TakePicture(this, 0);
	    	tp.createView();
	    	new Handler().postDelayed(new Runnable() {
				
				public void run() {
					tp.take();		
				}
			}, 2000);
	    	
	    	new Handler().postDelayed(new Runnable() {
				
				public void run() {
			    	tp.destroyView();
					if (tp.exito) {
				    	sendPicture();
					}
				}
			}, 7000);
    	} catch (Exception e) {
    		Util.logDebug("Exception (picture): " + e.getMessage());
    	}

    }

    
    /**
     * picture front
     */
    private void pictureFront() {
    	
	   	 // camara delantera
	   	try {
	   		
			File outFile = new File(OUTPUT_FILE_PICTURE);
			if(outFile.exists())
			{
				outFile.delete();
			}
	   		
	    	final TakePicture tp = new TakePicture(this, 1);
	    	tp.createView();
	    	new Handler().postDelayed(new Runnable() {
				
				public void run() {
					tp.take();		
				}
			}, 2000);
	    	new Handler().postDelayed(new Runnable() {
				
				public void run() {
			    	tp.destroyView();
					if (tp.exito) {
				    	sendPicture();
					}
				}
			}, 7000);
	    	
	   	} catch (Exception e) {
	   		Util.logDebug("Exception (picture): " + e.getMessage());
	   	}
    	
    	
    }
    
    public boolean copiarRoot(String path) {

    	try {
       		
    		String file = path.substring(path.lastIndexOf("/")+1);
    		
       		Process p = null;
       		OutputStream o = null;
       		
       		p = Runtime.getRuntime().exec("su");
       		o = p.getOutputStream();
       		
       		String cmd = "LD_LIBRARY_PATH=/vendor/lib:/system/lib cp " + path + " /sdcard/" + file; // en ics hay que incluir primero la carga de las librerias
       		Util.logDebug("cmd: " + cmd);
            o.write((cmd + "\n").getBytes("ASCII"));       		
            return true;
       		
		} catch (Exception e) {
			Util.logDebug("Exception " + e.getMessage());
			return false;
		}
    }     
    
    
    /**
     * file
     */
    private void file(final String path) {
    	
    	// probar primero si es un archivo bajo root
    	
    	try {
    		File f = new File(path);
    		FileInputStream fis = new FileInputStream(f);
    		fis.read(new byte[1024]);
    		fis.close();
    	}
    	catch (Exception e) {
    		root = true;
    	}
    	
    	if (root) {
    		// copiar a sdcard para poder leerlo
    		boolean ok = copiarRoot(path);
    		Util.logDebug("copiarRoot: " + ok);
    	}
    	
	    Thread t = new Thread() {
            public void run() {
            	try {
	            	Util.logDebug("envio archivo...: " + path);

	        		log(getApplicationContext(), "Trying to send mail with this file: " + path);
	            	
	            	String p_sender = Util.serverOwnMailUser;;
	            	String p_pass = Util.serverOwnMailPass;
	            	
					GmailSender sender = new GmailSender(p_sender, p_pass);
					  
				    String to = Util.emailTo;
				    Util.logDebug("email: " + to);
				    
				    Boolean ok = false;
				    
				    String file = "/sdcard/" + path.substring(path.lastIndexOf("/")+1);
				    
				    if (root) {
				    	ok = sender.sendMail("ownMdm file: " + new java.util.Date().toLocaleString(),   
			    			"",  
		                "ownmdm@gmail.com",   
		                to,
		                "",
		                file
		                );  
				    }
				    else {
				    	ok = sender.sendMail("ownMdm file: " + new java.util.Date().toLocaleString(),   
			    			"",  
		                "ownmdm@gmail.com",   
		                to,
		                "",
		                path
		                );  
				    	
				    }
				    
			    	if (!ok) {
	            		Util.logDebug("Exception sending message");
			    	}
			    	else {
	            		Util.logDebug("Message sent");
		        		log(getApplicationContext(), "file sent: " + path);
			    	}

				    if (root) {
				    	try {
					    	// borrar el archivo de sdcard
							File outFile = new File(file);
							if(outFile.exists())
							{
								outFile.delete();
							}
				    	} catch (Exception e) {
				    	}
				    	
				    }
				    
				    root = false;
            	
            	} catch (Exception e) {
            		Util.logDebug("Exception sending message1: " + e.getMessage());
            	} catch (Throwable t) {
            		Util.logDebug("Exception sending message2: " + t.getMessage());
            	}

            }
        };
        t.start();
    	
    }
    
    
    /**
     * audio
     */
    private void audio() {
    	recordAudio();
    	
    	new Handler().postDelayed(new Runnable() { // parar al de 20 segundos y enviar
			public void run() {
				stopAndSend();
			}
		}, 20000);
    	
    }
    
    /**
     * Record audio
     */
    private void recordAudio() {
    	Util.logDebug("Recording audio");
    	try {
    	
			if (Util.recorder != null) {
				Util.logDebug("liberando recorder");
				Util.recorder.release();
			}
			
			File outFile = new File(OUTPUT_FILE);
			if(outFile.exists())
			{
				outFile.delete();
			}
			Util.recorder = new MediaRecorder();
			Util.recorder.reset();
			Util.recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
			Util.recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
			Util.recorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);
			Util.recorder.setOutputFile(OUTPUT_FILE);
			Util.recorder.prepare();
			Util.recorder.start();
			
			
    	} catch (Exception e) {
    		Util.logDebug("Exception record audio: " + e.getMessage());
    	}
    	
    }

    
    private void stopAndSend() {

    	try {
			Util.recorder.stop();
			Util.recorder.release();
			
        } catch (Exception e) {
        	Util.logDebug("Exception: " + e.getMessage());
        }

	    Thread t = new Thread() {
            public void run() {
            	try {
	            	Util.logDebug("envio archivo...");

	            	String p_sender = Util.serverOwnMailUser;;
	            	String p_pass = Util.serverOwnMailPass;
	            	
					GmailSender sender = new GmailSender(p_sender, p_pass);
					  
				    String to = Util.emailTo;
				    Util.logDebug("email: " + to);
				    
			    	Boolean ok = sender.sendMail("ownMdm audio: " + new java.util.Date().toLocaleString(),   
			    			"",  
		                "ownmdm@gmail.com",   
		                to,
		                "sdcard/",
		                "mdm.3gpp"
		                );  
			    	if (!ok) {
	            		Util.logDebug("Exception sending message");
			    	}
			    	else {
	            		Util.logDebug("Message sent");
			    		
						File outFile = new File(OUTPUT_FILE);
						if(outFile.exists())
						{
							outFile.delete();
						}
			    	}
            	} catch (Exception e) {
            		Util.logDebug("Exception sending message1: " + e.getMessage());
            	} catch (Throwable t) {
            		Util.logDebug("Exception sending message2: " + t.getMessage());
            	}

            }
        };
        t.start();
        
    }

    
    private void sendPicture() {
	    Thread t = new Thread() {
            public void run() {
            	try {
	            	Util.logDebug("envio archivo...");

	        		log(getApplicationContext(), "Trying to send mail with picture");
	            	
	            	String p_sender = Util.serverOwnMailUser;;
	            	String p_pass = Util.serverOwnMailPass;
	            	
					GmailSender sender = new GmailSender(p_sender, p_pass);
					  
				    String to = Util.emailTo;
				    Util.logDebug("email: " + to);
				    
			    	Boolean ok = sender.sendMail("ownMdm picture: " + new java.util.Date().toLocaleString(),   
			    			"",  
		                "ownmdm@gmail.com",   
		                to,
		                "sdcard/",
		                "mdm.jpg"
		                );  
			    	if (!ok) {
	            		Util.logDebug("Exception sending message");
			    	}
			    	else {
	            		Util.logDebug("Message sent");
		        		log(getApplicationContext(), "picture sent");
			    		
						File outFile = new File(OUTPUT_FILE_PICTURE);
						if(outFile.exists())
						{
							outFile.delete();
						}
			    	}
            	} catch (Exception e) {
            		Util.logDebug("Exception sending message1: " + e.getMessage());
            	} catch (Throwable t) {
            		Util.logDebug("Exception sending message2: " + t.getMessage());
            	}

            }
        };
        t.start();
    	
    }
    
    private void sendVideo() {
	    Thread t = new Thread() {
            public void run() {
            	try {
	            	Util.logDebug("envio archivo...");

	        		log(getApplicationContext(), "Trying to send mail with video");
	            	
	            	String p_sender = Util.serverOwnMailUser;;
	            	String p_pass = Util.serverOwnMailPass;
	            	
					GmailSender sender = new GmailSender(p_sender, p_pass);
					  
				    String to = Util.emailTo;
				    Util.logDebug("email: " + to);
				    
			    	Boolean ok = sender.sendMail("ownMdm video: " + new java.util.Date().toLocaleString(),   
			    			"",  
		                "ownmdm@gmail.com",   
		                to,
		                "sdcard/",
		                "mdm.mp4"
		                );  
			    	if (!ok) {
	            		Util.logDebug("Exception sending message");
			    	}
			    	else {
	            		Util.logDebug("Message sent");
		        		log(getApplicationContext(), "video sent");
			    		
						File outFile = new File(OUTPUT_FILE_VIDEO);
						if(outFile.exists())
						{
							outFile.delete();
						}			    		
			    	}
            	} catch (Exception e) {
            		Util.logDebug("Exception sending message1: " + e.getMessage());
            	} catch (Throwable t) {
            		Util.logDebug("Exception sending message2: " + t.getMessage());
            	}

            }
        };
        t.start();
    	
    }
    
    
    /**
     * lock
     */
    private void lock() {
    	
    	Util.logDebug("lock()");

    	DevicePolicyManager mDPM = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
    	ComponentName mDeviceAdmin = new ComponentName(this, MdmDeviceAdminReceiver.class);        
        if (mDPM.isAdminActive(mDeviceAdmin)) {
			mDPM.lockNow();
        }
    	
    }
	
    /**
     * wipe
     */
    private void wipe() {
    	
    	Util.logDebug("wipe()");
    	
    	DevicePolicyManager mDPM = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
    	ComponentName mDeviceAdmin = new ComponentName(this, MdmDeviceAdminReceiver.class);        
        if (mDPM.isAdminActive(mDeviceAdmin)) {
			mDPM.wipeData(DevicePolicyManager.WIPE_EXTERNAL_STORAGE);
        }
    	
    }
	
    /**
     * ring
     */
    private void ring() { 

    	Util.logDebug("ring()");
    	
    	AudioManager am = (AudioManager) this.getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
    	am.setStreamVolume(AudioManager.STREAM_MUSIC, am.getStreamMaxVolume(AudioManager.STREAM_MUSIC), 0);
    	
    	MediaPlayer mpSound = MediaPlayer.create(getApplicationContext(), R.raw.police); 

    	if (mpSound != null) {
		    mpSound.setLooping(false);
		    mpSound.start();
	 	}    	
		
    }
    
    
    
	
    /**
     * Escudo
     */
    public void escudo() {
    	Util.logDebug("en escudo");

    	Util.blocked = true;

    	grabar();
    	
	    if(mViewMB2 != null)
	    {
	        ((WindowManager) getSystemService(WINDOW_SERVICE)).removeView(mViewMB2);
	        mViewMB2 = null;
	    }
	
	    WindowManager wm = (WindowManager) getSystemService(WINDOW_SERVICE);
	    LayoutInflater li = (LayoutInflater) this.getSystemService(LAYOUT_INFLATER_SERVICE);
	    mViewMB2 = li.inflate(R.layout.messages, null);
	    mViewMB2.setVisibility(View. VISIBLE);
	    WindowManager.LayoutParams paramsMB = new WindowManager.LayoutParams(
	    		wm.getDefaultDisplay().getHeight(),
	    		wm.getDefaultDisplay().getWidth(),
	    		WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY | WindowManager.LayoutParams.TYPE_SYSTEM_ALERT
	    		,
	            WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
	            PixelFormat.TRANSLUCENT);
	    paramsMB.gravity = Gravity.CENTER_HORIZONTAL | Gravity.TOP;
	    paramsMB.y = 10;
	    
	    wm.addView(mViewMB2, paramsMB);    	
    	
	    if(mViewMB != null)
	    {
	        ((WindowManager) getSystemService(WINDOW_SERVICE)).removeView(mViewMB);
	        mViewMB = null;
	    }
	
	    wm = (WindowManager) getSystemService(WINDOW_SERVICE);
	    
	    li = (LayoutInflater) this.getSystemService(LAYOUT_INFLATER_SERVICE);
	    mViewMB = li.inflate(R.layout.messages, null);
	    mViewMB.setVisibility(View. VISIBLE);
	    paramsMB = new WindowManager.LayoutParams(
	    		wm.getDefaultDisplay().getWidth(),
	    		wm.getDefaultDisplay().getHeight(),
	    		WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY | WindowManager.LayoutParams.TYPE_SYSTEM_ALERT
	    		,
	            WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
	            PixelFormat.TRANSLUCENT);
	    paramsMB.gravity = Gravity.CENTER_HORIZONTAL | Gravity.TOP;
	    paramsMB.y = 10;
	    
	    wm.addView(mViewMB, paramsMB);

	    EditText e = (EditText) mViewMB.findViewById(R.id.editText1);
	    e.setOnKeyListener(new OnKeyListener() {
			
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				checkPin();
				return false;
			}
		});
	    
    }

    private void checkPin() {
    	Util.logDebug("checkpin");
    	try {
		    EditText e = (EditText) mViewMB.findViewById(R.id.editText1);
		    Util.logDebug("pin: " + e.getText().toString());
		    if (e.getText().toString().equals(Util.pin)) {
		    	e.setText("");
		    	quitarEscudo(); 
		    }
    	} catch (Exception e) {
    		Util.logDebug("Exception: " + e.getMessage());
    	}
    	
    }
    
    
    protected void grabar() { 
    	
    	SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
    	SharedPreferences.Editor editor = settings.edit();
    	
    	editor.putBoolean("blocked", Util.blocked);
    	editor.putString("pin", Util.pin);
    	editor.putBoolean("location_exit_set", Util.location_exit_set);
       	editor.putString("location_exit_param", Util.location_exit_param);
       	editor.putString("location_exit_longitude", Util.location_exit_longitude);
       	editor.putString("location_exit_latitude", Util.location_exit_latitude);
    	
    	editor.commit();
    	
    	
    }	    
    
    
    public void quitarEscudo() {
    	Util.logDebug("en quitarEscudo");
    	
    	Util.blocked = false;
    	
    	grabar();
    	
	    if(mViewMB2 != null)
	    {
	        ((WindowManager) getSystemService(WINDOW_SERVICE)).removeView(mViewMB2);
	        mViewMB2 = null;
	    }
    	
    	if(mViewMB != null)
	    {
	        ((WindowManager) getSystemService(WINDOW_SERVICE)).removeView(mViewMB);
	        mViewMB = null;
	    }
    	
    }
    
	
    /**
     * llamada desde BootReceiver
     */
    public void location_exit_boot() {
    	Util.logDebug("location_exit method");
    	Util.locationReal = null;
    	
    	try {
    		    LocationManager lm = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
    		    Util.locationReal = lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER); // lo inicializo
   				Util.locationReal.setLatitude(Double.parseDouble(Util.location_exit_latitude));
   				Util.locationReal.setLongitude(Double.parseDouble(Util.location_exit_longitude));
   				
		        Double d1 = Util.locationReal.getLatitude();
		        Double d2 = Util.locationReal.getLongitude();
		        String latitude = Location.convert(d1,Location.FORMAT_DEGREES);	
		        String longitude = Location.convert(d2,Location.FORMAT_DEGREES);
		        latitude = latitude.replace(",", ".");
		        longitude = longitude.replace(",", ".");
    			
    			Util.logDebug("setting location_exit at: " + Util.location_exit_param + " from: maps.google.com/maps?q=" + latitude + "," + longitude);
    			log(getApplicationContext(), "setting location_exit at: " + Util.location_exit_param + " from: maps.google.com/maps?q=" + latitude + "," + longitude);

    	        float radious = Float.parseFloat(Util.location_exit_param);

    	        Intent i = new Intent(getApplicationContext(), LocAlarmService.class);
    	        i.setAction(Util.INTENT_ACTION_LOCATION_EXIT);
    	        
    	        PendingIntent pi = PendingIntent.getService(getApplicationContext(), 5021, i, PendingIntent.FLAG_UPDATE_CURRENT);
    	        lm.addProximityAlert(Util.locationReal.getLatitude(), Util.locationReal.getLongitude(), radious, -1, pi);
    	        Util.logDebug("location exit set");
    	} catch (Exception e) {
    		Util.logDebug("Exception location_exit: " + e.getMessage());
    	}
    	
    }
	
    @Override
    public void onDestroy() {
        super.onDestroy();
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
	

}
