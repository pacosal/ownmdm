/*
 * WifiReceiver.java
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.text.format.Time;

public class WifiReceiver extends BroadcastReceiver {
	
    AsyncTask<Void, Void, Void> mLogTask;    
	
	@Override
	public void onReceive (Context context, Intent intent) {

		ArrayList<String> al = new ArrayList<String>();
		
        long now = 0;
        long timeDiff = 0;		

        Time t = new Time();
        t.setToNow();
        now = t.toMillis(false);

        timeDiff = now - Util.lastLocation;
        if (timeDiff > 60000) { // solo en 60 segundos
        	return;
        }
        
		try {
			al.clear();
	        WifiManager w = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
	        List<ScanResult> l = w.getScanResults();
	        for (ScanResult r : l) {
	            al.add(r.level + " : " + r.SSID);
	        }
	        Collections.sort(al);
        	Util.logDebug("Wifi: " + al.toString());
			log(context, al.toString());
			
			// dejar wifi como estaba
			if (Util.wifiApagado)
				w.setWifiEnabled(false);
			
		} catch (Exception e) {
			Util.logDebug("Error al enviar desde update receiver: " + e.getMessage());
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
	
	
}
