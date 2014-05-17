/*
 * ScreenReceiver.java
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

public class ScreenReceiver extends BroadcastReceiver {

    public void onReceive(Context context, Intent intent) {
    	//Util.logDebug("on screen receiver");
        if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
            // do whatever you need to do here
            Util.pantallaEncendida = false;
        } else if (intent.getAction().equals(Intent.ACTION_SCREEN_ON)) {
            // and do whatever you need to do here
            Util.pantallaEncendida = true;

            if (Util.videoPendiente) {
            	Util.videoPendiente = false;
            	video(context, Util.emailTo);
            }
        
        }
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
        context.startService(i);   	
    }        
    
    
}