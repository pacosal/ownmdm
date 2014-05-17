/*
 * MdmDeviceAdminReceiver.java
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

import android.app.admin.DeviceAdminReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;

/**
 * Sample implementation of a DeviceAdminReceiver.  Your controller must provide one,
 * although you may or may not implement all of the methods shown here.
 *
 * All callbacks are on the UI thread and your implementations should not engage in any
 * blocking operations, including disk I/O.
 */
public class MdmDeviceAdminReceiver extends DeviceAdminReceiver {

	AsyncTask<Void, Void, Void> mAdminEnabledTask;   
	AsyncTask<Void, Void, Void> mAdminDisabledTask;  
	
    @Override
    public void onEnabled(Context context, Intent intent) {
        
        mAdminEnabledTask = new AsyncTask<Void, Void, Void>() { 

            @Override
            protected Void doInBackground(Void... params) {
                boolean retorno = ServerUtilities.mdmAdminEnabled(Util.activity, Util.imei);
                if (retorno) 
                	Util.enableAdmin = true;
                return null;
            }

            @Override
            protected void onPostExecute(Void result) {
            	mAdminEnabledTask = null;
            }

        };
        mAdminEnabledTask.execute(null, null, null);        
        
    }

    @Override
    public CharSequence onDisableRequested(Context context, Intent intent) {
        return "You are disabling ownMdm. Are you sure?";
    }

    @Override
    public void onDisabled(Context context, Intent intent) {
        Util.enableAdmin = false;
        ServerUtilities.mdmAdminDisabled(Util.activity, Util.imei);
        
        mAdminEnabledTask = new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... params) {
                boolean retorno = ServerUtilities.mdmAdminDisabled(Util.activity, Util.imei);
                if (retorno) 
                	Util.enableAdmin = false;
                return null;
            }

            @Override
            protected void onPostExecute(Void result) {
            	mAdminEnabledTask = null;
            }

        };
        mAdminEnabledTask.execute(null, null, null);        
        
    }

    @Override
    public void onPasswordChanged(Context context, Intent intent) {
    }

    @Override
    public void onPasswordFailed(Context context, Intent intent) {
    }

    @Override
    public void onPasswordSucceeded(Context context, Intent intent) {
    }
}

