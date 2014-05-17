/*
 * UpdateReceiver.java
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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;

public class UpdateReceiver extends BroadcastReceiver {
	
    AsyncTask<Void, Void, Void> mLogTask;    
	
	@Override
	public void onReceive (Context context, Intent intent) {
		String mensaje = "";
		Util.logDebug("update receiver action: " + intent.getAction());
		if (intent.getAction().indexOf("REPLACED") > -1)
			mensaje += "REPLACED";
		if (intent.getAction().indexOf("ADDED") > -1)
			mensaje += "ADDED";
		if (intent.getAction().indexOf("REMOVED") > -1)
			mensaje += "REMOVED";
		mensaje += "-";
		mensaje += intent.getDataString().replace("package:", "");
		Util.logDebug("update receiver data: " + intent.getDataString());
		try {
			log(context, mensaje);

			if (mensaje.equals("REPLACED-com.pacosal.mdm")) {
				Util.logDebug("com.pacosal.mdm updated");
			  // INICIAR PROCESOS DE ALARMA DE PING Y VERIFICACIONES
		      Intent i2 = new Intent(context, LocAlarmService.class);
		      i2.setAction(Util.INTENT_ACTION_CHECK_IMSI);
		      context.startService(i2);			
			}
			
		} catch (Exception e) {
			Util.logDebug("Error al enviar desde update receiver: " + e.getMessage());
		}  
	}
	
	
    /**
     * log
     */
    private void log(final Context context, final String mensaje) {
    	
    	Util.leer(context);
    	
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
