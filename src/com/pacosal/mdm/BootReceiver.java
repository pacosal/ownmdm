/*
 * BootReceiver.java
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

public class BootReceiver extends BroadcastReceiver {
	
	  @Override 
	  public void onReceive(Context context, Intent intent) {
		  try {
			  leer(context);
			  
			  Util.logDebug("Boot receiver");

			  // registrar en GCM
			  
			  
			  if (Util.blocked) { // bloqueo de pin
			      Intent i2 = new Intent(context, LocAlarmService.class);
			      i2.setAction(Util.INTENT_ACTION_ESCUDO);
			      context.startService(i2);
			  }

			  if (Util.location_exit_set) { // location exit
			      Intent i2 = new Intent(context, LocAlarmService.class);
			      i2.setAction(Util.INTENT_ACTION_LOCATION_EXIT_SET_BOOT);
			      context.startService(i2);
			  }
			  
			  // check sim
		      Intent i2 = new Intent(context, LocAlarmService.class);
		      i2.setAction(Util.INTENT_ACTION_CHECK_IMSI);
		      context.startService(i2);

		  } catch (Exception e) {
			  Util.logDebug("Exception (BootReceiver:onReceive): " + e.getMessage());
		  }

	  }
	  
	    protected void leer(Context context) {
	    	Util.leer(context);
	    }
	    	
	    	  
	  

}