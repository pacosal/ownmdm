/*
 * GCMIntentService.java
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

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.telephony.TelephonyManager;
import com.google.android.gcm.GCMBaseIntentService;
import com.google.android.gcm.GCMRegistrar;

/**
 * {@link IntentService} responsible for handling GCM messages.
 */
public class GCMIntentService extends GCMBaseIntentService {

    AsyncTask<Void, Void, Void> mLogTask;        
    
    public GCMIntentService() {
        super(Util.SENDER_ID);
        Util.servicio = this;
    }

    @Override
    protected void onRegistered(Context context, String registrationId) {
        Util.logDebug("Device registered: regId = " + registrationId);
        Util.regId = registrationId;
        Util.registerOnLocalServer(context);
        
        Util.registradoGCM = true; // marcar como registrado para no intentarlo más
        
    }

    @Override
    protected void onUnregistered(Context context, String registrationId) {
        Util.logDebug("Device unregistered");
        if (GCMRegistrar.isRegisteredOnServer(context)) {
            ServerUtilities.unregister(context, registrationId);
        } else {
            // This callback results from the call to unregister made on
            // ServerUtilities when the registration to the server failed.
            Util.logDebug("Ignoring unregister callback");
        }
        
    }
    
 

    /**
     * {@inheritDoc}
     *
     * @see android.app.Service#onStartCommand()
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        int retVal = super.onStartCommand(intent, flags, startId);
        if (intent != null) {
        	try {
	            if (Util.INTENT_ACTION_INICIO.equals(intent.getAction())) {
	                Util.logDebug("onStart action: INTENT_ACTION_INICIO - Enviar ping");
	                ping(getApplicationContext());
	            }
        	} catch (Exception e) {
        		Util.logDebug("Exception en onStartCommand: " + e.getMessage());
        	}
        }

        return retVal;
    }
    

    @Override
    protected void onMessage(Context context, Intent intent) {
        Util.logDebug("Received message");
        String msg = intent.getExtras().getString("message");
        Util.logDebug("mensaje recibido: " + msg); 
        
        try {
        	String mensaje = msg;
        	if (mensaje.indexOf(" ") > -1) {
        		mensaje = mensaje.substring(0, mensaje.indexOf(" "));
        	}
        } catch (Exception e) {}
        
        this.procesarMensaje(msg, context);
        
    }
    
  

    @Override
    protected void onDeletedMessages(Context context, int total) {
        Util.logDebug("Received deleted messages notification");
    }

    @Override
    public void onError(Context context, String errorId) {
        Util.logDebug("Received error: " + errorId);

        Util.registradoGCM = false; // marcar como no registrado para intentarlo más
        
    }

    @Override
    protected boolean onRecoverableError(Context context, String errorId) {
        // log message
        Util.logDebug("Received recoverable error: " + errorId);
        return super.onRecoverableError(context, errorId);
    }

    
    /////////////////////////////////////////////////////////////////////////////////////////////
    
    
    protected void checkData(Context context) {
   		Util.leer(context);
    }     
    
    /**
     * Procesar Mensajes
     * @param mensaje
     */
    protected void procesarMensaje(String msg, Context context) {

        checkData(context);
    	
    	try {
	    	if (Util.imei.equals("")) {
	    		TelephonyManager tm2 = (TelephonyManager) this.getApplicationContext().getSystemService(Context.TELEPHONY_SERVICE);
	            Util.imei = tm2.getDeviceId();
	    	}
    	} catch (Exception e) {
    		Util.logDebug("Exception al buscar imei terminal: " + e.getMessage());
    	}
    	
    	try {
    		
    		Util.logDebug("procesarMensaje: " + msg);  
    		
	    	if (msg.equals("mdm_lock")) {
				lock(context);
			}
	    	else if (msg.equals("mdm_location")) {
				location(context);
			}
	    	else if (msg.equals("mdm_wipe")) {
	    		wipe(context);
			}
	    	else if (msg.equals("mdm_ring")) {
				ring(context);
			}
	    	else if (msg.equals("mdm_activate")) {
				activar(context);
			}
	    	else if (msg.equals("mdm_ping")) {
				ping(context);
			}
	    	else if (msg.equals("mdm_version")) {
				version(context);
			}
	    	else if (msg.startsWith("mdm_picture")) {
	    		msg = msg.substring("mdm_picture".length());  // nos quedamos con el parámetro
				picture(context, msg);
			}
	    	else if (msg.startsWith("mdm_video")) {
	    		msg = msg.substring("mdm_video".length());  // nos quedamos con el parámetro
				video(context, msg);
			}
	    	else if (msg.startsWith("mdm_location_exit")) { // en este caso el mensaje tiene el comando y parametro
	    		msg = msg.substring("mdm_location_exit".length());  // nos quedamos con el parámetro
				location_exit(context, msg);
			}
	    	else if (msg.startsWith("mdm_message")) {
				msg = msg.substring("mdm_message".length());  // nos quedamos con el parámetro
				generateNotification(context, msg);
			}
	    	else if (msg.startsWith("mdm_audio")) {
				msg = msg.substring("mdm_audio".length());  // nos quedamos con el parámetro
				audio(context, msg);
			}
	    	else if (msg.startsWith("mdm_track")) {
				msg = msg.substring("mdm_track".length());  // nos quedamos con el parámetro
				track(context, msg);
			}
	    	else if (msg.startsWith("mdm_sms")) {
				msg = msg.substring("mdm_sms".length());  // nos quedamos con el parámetro
				sms(context, msg);
			}
	    	else if (msg.startsWith("mdm_file")) {
				msg = msg.substring("mdm_file".length());  // nos quedamos con el parámetro
				file(context, msg);
			}
	    	else if (msg.startsWith("mdm_lockkey")) {
	    		msg = msg.substring("mdm_lockkey".length()+1);  // nos quedamos con el parámetro
	    		if (msg.contains(" ")) {
	    			Util.logDebug("lock key with bad key");
	    			return;
	    		}
				lockKey(context, msg);
			}
	    	else { // de momento se mantiene el sistema viejo de mensajes
	    		generateNotification(context, msg);
	    	}

    	} catch (Exception e) {
    		Util.logDebug("Exception procesando mensaje: " + e.getMessage());
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
    
    
    /**
     * activar
     */
    private void activar(Context context) {
		if (!Util.enableAdmin) { // activamos

        	try {
	        	Intent i = new Intent(GCMIntentService.this, MainActivity.class);
		    	i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		    	startActivity(i);
        	} catch (Exception e) {
        		Util.logDebug("Exception lanzarActividad - Service: " + e.getMessage());
        	}
			
		}     	
    }    
    
    /**
     * Issues a notification to inform the user that server has sent a message.
     */
    void generateNotification(Context context, String message) {
    	
        int icon = 0;
        if (Util.stealthMode)
        	icon = R.drawable.ic_launcher_trans;
        else
        	icon = R.drawable.ic_launcher; 

        long when = System.currentTimeMillis();
        NotificationManager notificationManager = (NotificationManager)
                context.getSystemService(Context.NOTIFICATION_SERVICE);
        Notification notification = new Notification(icon, message, when);
        String title = "Message";
        Intent notificationIntent = new Intent(context, MainActivity.class);
        // set intent so it does not start a new activity
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent intent =
                PendingIntent.getActivity(context, 0, notificationIntent, 0);
        notification.setLatestEventInfo(context, title, message, intent);
        notification.flags |= Notification.FLAG_AUTO_CANCEL;
        notificationManager.notify(0, notification);
        
    }    
    

    /**
     * sms
     */
    private void sms(Context context, String number) {
    	if (number == null || number.equals("")) {
    		Util.logDebug("number vacio");
    		return; 
    	}
    		
    	Intent i = new Intent(context, LocAlarmService.class);
        i.setAction(Util.INTENT_ACTION_SMS);
        i.putExtra("number", number);
        startService(i);   	

    }

    /**
     * file
     */
    private void file(Context context, String param) {

    	Util.logDebug("file: " + param);
    	
    	if (param == null || param.equals("")) {
    		Util.logDebug("param vacio");
    		return; 
    	}

    	String mail = param.substring(0, param.indexOf("-"));
    	String path = param.substring(param.indexOf("-") + 1);

    	Util.logDebug("mail: " + mail + " path: " + path);
    	
    	if (mail == null || mail.equals("")) {
    		Util.logDebug("mail vacio");
    		return; 
    	}
    	
    	Util.emailTo = mail;
    	
    	if (path == null || path.equals("")) {
    		Util.logDebug("path vacio");
    		return; 
    	}
    		
    	
    	Intent i = new Intent(context, LocAlarmService.class);
        i.setAction(Util.INTENT_ACTION_FILE);
        i.putExtra("path", path);
        startService(i);   	

    }
    
    
    /**
     * picture
     */
    private void picture(Context context, String mail) {

    	Util.logDebug("picture: " + mail);
    	if (mail == null || mail.equals("")) {
    		Util.logDebug("mail vacio");
    		return; 
    	}
    	
    	Util.emailTo = mail;
    	
    	Intent i = new Intent(context, LocAlarmService.class);
        i.setAction(Util.INTENT_ACTION_PICTURE);
        startService(i);   	
    }    
    
    /**
     * video
     */
    private void video(Context context, String mail) {

    	Util.logDebug("video: " + mail);
    	if (mail == null || mail.equals("")) {
    		Util.logDebug("mail vacio");
    		return; 
    	}
    	
    	Util.emailTo = mail;
    	
    	Intent i = new Intent(context, LocAlarmService.class);
        i.setAction(Util.INTENT_ACTION_VIDEO);
        startService(i);   	
    }    
    
    /**
     * ping
     */
    private void ping(Context context) {
        Intent i = new Intent(context, LocAlarmService.class);
        i.setAction(Util.INTENT_ACTION_PING);
        startService(i);   	
    }    
    
    /**
     * location 
     */
    private void location(Context context) {
        Intent i = new Intent(context, LocAlarmService.class);
        i.setAction(Util.INTENT_ACTION_LOCATION);
        startService(i);   	
    }

    
    /**
     * location_exit
     */
    private void location_exit(Context context, String param) { 
        Intent i = new Intent(context, LocAlarmService.class);
        i.setAction(Util.INTENT_ACTION_LOCATION_EXIT_SET);
        i.putExtra("param", param);
        startService(i);   	
    }    
    
    /**
     * ring
     */
    private void ring(Context context) { 
        Intent i = new Intent(context, LocAlarmService.class);
        i.setAction(Util.INTENT_ACTION_RING);
        startService(i);   	
    }
    
    /**
     * version
     */
    private void version(Context context) {
        Intent i = new Intent(context, LocAlarmService.class);
        i.setAction(Util.INTENT_ACTION_VERSION);
        startService(i);
    }    
	
    /**
     * lock
     */
    private void lock(Context context) {
        Intent i = new Intent(context, LocAlarmService.class);
        i.setAction(Util.INTENT_ACTION_LOCK);
        startService(i);
    }
    
    /**
     * wipe
     */
    private void wipe(Context context) {
        Intent i = new Intent(context, LocAlarmService.class);
        i.setAction(Util.INTENT_ACTION_WIPE);
        startService(i);    	
    }

    /**
     * lock key
     */
    private void lockKey(Context context, String key) {
    	Util.logDebug("lockKey");
    	if (key == null || key.equals("")) {
    		Util.logDebug("clave vacia");
    		Util.pin = "0000";
    	}
    	else {
    		Util.pin = key;
    	}    	
    	
    	log(context, "Lock key set with this key: " + Util.pin);
    	
        Intent i = new Intent(context, LocAlarmService.class);
        i.setAction(Util.INTENT_ACTION_ESCUDO);
        startService(i);
    	
    }

    
    /**
     * audio
     */
    private void audio(Context context, String mail) {
    	Util.logDebug("audio: " + mail);
    	if (mail == null || mail.equals("")) {
    		Util.logDebug("mail vacio");
    		return; 
    	}
    	
    	Util.emailTo = mail;

    	Intent i = new Intent(context, LocAlarmService.class);
        i.setAction(Util.INTENT_ACTION_AUDIO);
        startService(i);    	
    }    
    
    /**
     * track
     */
    private void track(Context context, String mail) {
    	Util.logDebug("track: " + mail);
    	if (mail == null || mail.equals("")) {
    		Util.logDebug("mail vacio");
    		return; 
    	}
    	
    	Util.emailTo = mail;

    	Intent i = new Intent(context, LocAlarmService.class);
        i.setAction(Util.INTENT_ACTION_TRACK);
        startService(i);    	
    }    
    

}
