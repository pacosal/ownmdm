/*
 * NumberReceiver.java
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
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.preference.PreferenceManager;
import android.widget.Toast;

public class NumberReceiver extends BroadcastReceiver {
	  @Override 
	  public void onReceive(Context context, Intent intent) {
		  try {
			  String number = intent.getStringExtra(Intent.EXTRA_PHONE_NUMBER);
			  //Util.logDebug("number: " + number);
			  if (number.equals(Util.openCode)) {
	              	Util.logDebug("Disabling stealth mode"); 
	              	try {
	              		PackageManager pm = context.getPackageManager();
                		Util.cn = new ComponentName("com.pacosal.mdm", "com.pacosal.mdm.MainActivity");
                		pm.setComponentEnabledSetting(Util.cn,PackageManager.COMPONENT_ENABLED_STATE_DEFAULT, PackageManager.DONT_KILL_APP);
	              	} catch (Exception e) {
	              		Util.logDebug("exception: " + e.getMessage());
	              	}
				  
	              Toast.makeText(context, context.getString(R.string.enableStealthModeAgain), Toast.LENGTH_LONG).show();	
	              Util.stealthMode = false;
	              grabar(context);
	              
				  Util.logDebug("lanzando actividad main");
	              Intent serviceIntent = new Intent(context, MainActivity.class);
	              serviceIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK); 
	              context.startActivity(serviceIntent);  
			  }
		  } catch (Exception e) {
			  Util.logDebug("Exception (NumberReceiver:onReceive): " + e.getMessage());
		  }

	  }
	  
	    
	    protected void grabar(Context context) { 
	    	
	    	SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
	    	SharedPreferences.Editor editor = settings.edit();
	    	
	    	editor.putBoolean("stealthMode", Util.stealthMode);
	    	editor.commit();
	    	
	    	
	    }		  
}