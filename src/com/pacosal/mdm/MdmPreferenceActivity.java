/*
 * MdmPreferenceActivity.java
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

import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

public class MdmPreferenceActivity extends PreferenceActivity {

	Context context = null;
    AsyncTask<Void, Void, Void> mCheckTask;    
    
    @SuppressWarnings("deprecation")
	@Override
    public void onCreate(Bundle savedInstanceState) { 
        super.onCreate(savedInstanceState); 

        context = this.getApplicationContext();
        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.preferences);

        CheckBoxPreference preference2 = (CheckBoxPreference)findPreference("debug");
        preference2.setChecked(Util.debugMode);
        preference2.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {

            public boolean onPreferenceChange(Preference preference, Object newValue) {
                Util.debugMode = (Boolean)newValue;
                if (Util.debugMode)
                	Util.logDebug("Debug activado");
                
                grabar();
                
                return true;
            }
        });


        CheckBoxPreference prefAnyCert = (CheckBoxPreference)findPreference("acceptAny");
        prefAnyCert.setChecked(Util.aceptarCualquierCertificado);
        prefAnyCert.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {

            public boolean onPreferenceChange(Preference preference, Object newValue) {
                Util.aceptarCualquierCertificado = (Boolean)newValue;
                if (Util.aceptarCualquierCertificado)
                	Util.logDebug("Se acepta cualquier certificado");
                
                grabar();
                
                return true;
            }
        });

        
        final CheckBoxPreference preference1 = (CheckBoxPreference)findPreference("stealthMode");
        preference1.setChecked(Util.stealthMode);
        preference1.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {

            public boolean onPreferenceChange(Preference preference, Object newValue) {
                Util.stealthMode = (Boolean)newValue;
                if (Util.stealthMode) {
                	Util.logDebug("Checking stealth mode");

	            	try {
	            		PackageManager pm = getPackageManager();
	            		Util.cn = new ComponentName("com.pacosal.mdm", "com.pacosal.mdm.MainActivity");
	            		pm.setComponentEnabledSetting(Util.cn, PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
	            	} catch (Exception e) {
	            		Util.logDebug("exception: " + e.getMessage());
	            	}
                	
                }
                else {
                	Util.logDebug("Disabling stealth mode");
                	try {
                		PackageManager pm = getPackageManager();
                		Util.cn = new ComponentName("com.pacosal.mdm", "com.pacosal.mdm.MainActivity");
                		pm.setComponentEnabledSetting(Util.cn,PackageManager.COMPONENT_ENABLED_STATE_DEFAULT, PackageManager.DONT_KILL_APP);
                	} catch (Exception e) {
                		Util.logDebug("exception: " + e.getMessage());
                	}
                }
                
                grabar();
                
                return true;
            }
        });
        
        
        CheckBoxPreference preferenceServerOwnMail = (CheckBoxPreference)findPreference("serverOwnMail"); 
        preferenceServerOwnMail.setChecked(Util.serverOwnMail);
        preferenceServerOwnMail.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                Util.serverOwnMail = (Boolean)newValue;
                
                if (Util.serverOwnMail) { // preguntar resto de info
                	LayoutInflater li = LayoutInflater.from(MdmPreferenceActivity.this);
    				View promptsView = li.inflate(R.layout.mail_prompt, null);
     
    				AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(MdmPreferenceActivity.this);
     
    				// set prompts.xml to alertdialog builder
    				alertDialogBuilder.setView(promptsView);
     
    				final EditText editTextMailUser = (EditText) promptsView.findViewById(R.id.editTextMailUser);
    				editTextMailUser.setText(Util.serverOwnMailUser);
    				final EditText editTextMailPass = (EditText) promptsView.findViewById(R.id.editTextMailPass);
    				editTextMailPass.setText(Util.serverOwnMailPass); 
     
    				// set dialog message
    				alertDialogBuilder
    					.setCancelable(false)
    					.setPositiveButton("OK",
    					  new DialogInterface.OnClickListener() {
    					    public void onClick(DialogInterface dialog,int id) {
	    						Util.serverOwnMailUser = editTextMailUser.getText().toString();
	    						Util.serverOwnMailPass = editTextMailPass.getText().toString();
	    						grabar();
    					    }
    					  })
    					.setNegativeButton("Cancel",
    					  new DialogInterface.OnClickListener() {
    					    public void onClick(DialogInterface dialog,int id) {
    						dialog.cancel();
    					    }
    					  });
     
    				// create alert dialog
    				AlertDialog alertDialog = alertDialogBuilder.create();
     
    				// show it
    				alertDialog.show();                	
                }
                grabar();
                return true;
            }
        });                
        
        CheckBoxPreference preferenceServerData = (CheckBoxPreference)findPreference("serverData"); 
        preferenceServerData.setChecked(Util.serverData);
        preferenceServerData.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                Util.serverData = (Boolean)newValue;
                
                if (Util.serverData) { // preguntar resto de info
                	LayoutInflater li = LayoutInflater.from(MdmPreferenceActivity.this);
    				View promptsView = li.inflate(R.layout.prompt, null);
     
    				AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(MdmPreferenceActivity.this);
     
    				// set prompts.xml to alertdialog builder
    				alertDialogBuilder.setView(promptsView);
     
    				final EditText editTextServerDataUrl = (EditText) promptsView.findViewById(R.id.editTextUrl);
    				editTextServerDataUrl.setText(Util.serverDataUrl);
    				final EditText editTextServerDataKey = (EditText) promptsView.findViewById(R.id.editTextKey);
    				editTextServerDataKey.setText(Util.serverDataKey);
     
    				// set dialog message
    				alertDialogBuilder
    					.setCancelable(false)
    					.setPositiveButton("OK",
    					  new DialogInterface.OnClickListener() {
    					    public void onClick(DialogInterface dialog,int id) {
	    						Util.serverDataUrl = editTextServerDataUrl.getText().toString();
	    						Util.serverDataKey = editTextServerDataKey.getText().toString();
	    				    	Util.SERVER_URL = Util.serverDataUrl;
	    						grabar();
	    						try {
	    							Util.activity.inicio();
	    						}
	    						catch (Exception e) {
	    							
	    						}
    					    }
    					  })
    					.setNegativeButton("Cancel",
    					  new DialogInterface.OnClickListener() {
    					    public void onClick(DialogInterface dialog,int id) {
    						dialog.cancel();
    					    }
    					  });
     
    				// create alert dialog
    				AlertDialog alertDialog = alertDialogBuilder.create();
     
    				// show it
    				alertDialog.show();                	
                }
                
                grabar();
                
                
                return true;
            }
        });        
        
        
        
    }

    protected void grabar() { 
    	
    	SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
    	SharedPreferences.Editor editor = settings.edit();
    	
    	editor.putBoolean("debug", Util.debugMode);
    	editor.putBoolean("acceptAny", Util.aceptarCualquierCertificado);
    	editor.putBoolean("stealthMode", Util.stealthMode);
    	editor.putBoolean("serverData", Util.serverData);
    	editor.putString("serverDataUrl", Util.serverDataUrl);
    	editor.putString("serverDataKey", Util.serverDataKey); 
    	editor.putBoolean("serverOwnMail", Util.serverOwnMail);
    	editor.putString("serverOwnMailUser", Util.serverOwnMailUser);
    	editor.putString("serverOwnMailPass", Util.serverOwnMailPass);

    	editor.commit();
    	
    	Util.SERVER_URL = Util.serverDataUrl;
    	
    }	



}
