/*
 * ServerUtilities.java
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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import com.google.android.gcm.GCMRegistrar;

import android.content.Context;
import android.telephony.TelephonyManager;

/**
 * Helper class used to communicate with the demo server.
 */
public final class ServerUtilities {

   
	// always verify the host - dont check for certificate
	final static HostnameVerifier DO_NOT_VERIFY = new HostnameVerifier() {
		public boolean verify(String hostname, SSLSession session) {
			return true;
		}
	};

	/**
	 * Trust every server - dont check for any certificate
	 */
	private static void trustAllHosts() {
		// Create a trust manager that does not validate certificate chains
		TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
			public java.security.cert.X509Certificate[] getAcceptedIssuers() {
				return new java.security.cert.X509Certificate[] {};
			}

			public void checkClientTrusted(
					java.security.cert.X509Certificate[] chain, String authType)
					throws java.security.cert.CertificateException {
			}

			public void checkServerTrusted(
					java.security.cert.X509Certificate[] chain, String authType)
					throws java.security.cert.CertificateException {
			}
		} };

		// Install the all-trusting trust manager
		try {
			SSLContext sc = SSLContext.getInstance("TLS");
			sc.init(null, trustAllCerts, new java.security.SecureRandom());
			HttpsURLConnection
					.setDefaultSSLSocketFactory(sc.getSocketFactory());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}	
	
    /**
     * Register this account/device pair within the server.
     *
     * @return whether the registration succeeded or not.
     */
    static boolean register(final Context context, final String regId, final String model) {
        Util.logDebug("registering device (regId = " + regId + ")");
        String serverUrl = Util.SERVER_URL + "/mdm/register.php";
    	try {
	    	if (Util.imei.equals("")) {
	    		TelephonyManager tm2 = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
	            Util.imei = tm2.getDeviceId();
	    	}

    	} catch (Exception e) {
    		Util.logDebug("Exception al buscar imei terminal: " + e.getMessage());
    	}
        
        Map<String, String> params = new HashMap<String, String>();
        params.put("regId", regId);
        params.put("imei", Util.imei);
        params.put("model", model);
        params.put("key", Util.serverDataKey);
        // Once GCM returns a registration id, we need to register it in the
        // demo server. 
        try {
            post(serverUrl, params);
            return true;
        } catch (Exception e) {
            Util.logDebug("Failed to call: " +  e.getMessage());
        }
        return false;
    }

    /**
     * Unregister this account/device pair within the server.
     */
    static void unregister(final Context context, final String regId) {
        Util.logDebug("unregistering device (regId = " + regId + ")");
        String serverUrl = Util.SERVER_URL + "/mdm/unregister.php";
        Map<String, String> params = new HashMap<String, String>();
        params.put("regId", regId);
        params.put("key", Util.serverDataKey);
        
        try {
            post(serverUrl, params);
            GCMRegistrar.setRegisteredOnServer(context, false);
        } catch (IOException e) {
            // At this point the device is unregistered from GCM, but still
            // registered in the server.
            // We could try to unregister again, but it is not necessary:
            // if the server tries to send a message to the device, it will get
            // a "NotRegistered" error message and should unregister the device.
        }
    }

    static boolean mdmAdminEnabled(final Context context, final String imei) {
        Util.logDebug("llamando a adminEnabled");
        String serverUrl = Util.SERVER_URL + "/mdm/adminEnabled.php";
    	try {
	    	if (Util.imei.equals("")) {
	    		TelephonyManager tm2 = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
	            Util.imei = tm2.getDeviceId();
	    	}
    	} catch (Exception e) {
    		Util.logDebug("Exception al buscar imei terminal: " + e.getMessage());
    	}
        
        Map<String, String> params = new HashMap<String, String>();
        params.put("imei", imei);
        params.put("key", Util.serverDataKey);
        
        // Once GCM returns a registration id, we need to register it in the
        // demo server. 
        try {
            post(serverUrl, params);
            return true;
        } catch (Exception e) {
            Util.logDebug("Failed to call: " +  e.getMessage());
        }
        return false;
    }
    
    static boolean mdmAdminDisabled(final Context context, final String imei) {
        Util.logDebug("llamando a adminDisabled");
        String serverUrl = Util.SERVER_URL + "/mdm/adminDisabled.php";
    	try {
	    	if (Util.imei.equals("")) {
	    		TelephonyManager tm2 = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
	            Util.imei = tm2.getDeviceId();
	    	}
    	} catch (Exception e) { 
    		Util.logDebug("Exception al buscar imei terminal: " + e.getMessage());
    	}
        
        Map<String, String> params = new HashMap<String, String>();
        params.put("imei", imei);
        params.put("key", Util.serverDataKey);
        
        // Once GCM returns a registration id, we need to register it in the
        // demo server. 
        try {
            post(serverUrl, params);
            return true;
        } catch (Exception e) {
            Util.logDebug("Failed to call: " +  e.getMessage());
        }
        return false;
    }
    
    static boolean sendLocation(final Context context, final String location) {
        Util.logDebug("llamando a location");
        String serverUrl = Util.SERVER_URL + "/mdm/location.php";
    	try {
	    	if (Util.imei.equals("")) {
	    		TelephonyManager tm2 = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
	            Util.imei = tm2.getDeviceId();
	    	}
    	} catch (Exception e) {
    		Util.logDebug("Exception al buscar imei terminal: " + e.getMessage());
    	}
        
        Map<String, String> params = new HashMap<String, String>();
        params.put("imei", Util.imei);
        params.put("location", location);
        params.put("key", Util.serverDataKey);
        
        // Once GCM returns a registration id, we need to register it in the
        // demo server. 
        try {
            post(serverUrl, params);
            return true;
        } catch (Exception e) {
            Util.logDebug("Failed to call: " +  e.getMessage());
        }
        return false;
    }
    
    static boolean sendVersion(final Context context, final String v) {
        Util.logDebug("llamando a version");
        String serverUrl = Util.SERVER_URL + "/mdm/version.php";

        Map<String, String> params = new HashMap<String, String>();
        params.put("imei", Util.imei);
        params.put("version", v);
        params.put("key", Util.serverDataKey);
        
        // Once GCM returns a registration id, we need to register it in the
        // demo server. 
        try {
            post(serverUrl, params);
            return true;
        } catch (Exception e) {
            Util.logDebug("Failed to call: " +  e.getMessage());
        }
        return false;
    }
        
    
    static boolean sendPing(final Context context) {
        Util.logDebug("llamando a ping");
        String serverUrl = Util.SERVER_URL + "/mdm/ping.php";
    	try {
	    	if (Util.imei.equals("")) {
	    		TelephonyManager tm2 = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
	            Util.imei = tm2.getDeviceId();
	    	}
    	} catch (Exception e) {
    		Util.logDebug("Exception al buscar imei terminal: " + e.getMessage());
    	}
        
        Map<String, String> params = new HashMap<String, String>();
        params.put("imei", Util.imei);
        params.put("key", Util.serverDataKey);
        
        // Once GCM returns a registration id, we need to register it in the
        // demo server. 
        try {
            post(serverUrl, params);
            return true;
        } catch (Exception e) {
            Util.logDebug("Failed to call: " +  e.getMessage());
        }
        return false;
    }

    static String checkRegistered(final Context context, String url) {
        Util.logDebug("llamando a checkRegistered");
        String serverUrl = Util.SERVER_URL + "/mdm/registered.php";
        Map<String, String> params = new HashMap<String, String>();
        params.put("url", url);
        try {
            String response = postWithResponse(serverUrl, params);
            return response;
        } catch (Exception e) {
            Util.logDebug("Failed to call: " +  e.getMessage());
        }
        return null;
    }
    
    
    static boolean sendLog(final Context context, final String message) {
        Util.logDebug("llamando a log");
        String serverUrl = Util.SERVER_URL + "/mdm/log.php";
    	try {
	    	if (Util.imei.equals("")) {
	    		TelephonyManager tm2 = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
	            Util.imei = tm2.getDeviceId();
	    	}
    	} catch (Exception e) {
    		Util.logDebug("Exception al buscar imei terminal: " + e.getMessage());
    	}
        
        Map<String, String> params = new HashMap<String, String>();
        params.put("imei", Util.imei);
        params.put("message", message);
        params.put("key", Util.serverDataKey);
        
        // Once GCM returns a registration id, we need to register it in the
        // demo server. 
        try {
            post(serverUrl, params);
            return true;
        } catch (Exception e) {
            Util.logDebug("Failed to call: " +  e.getMessage());
        }
        return false;
    }    
    
    /**
     * Issue a POST request to the server.
     *
     * @param endpoint POST address.
     * @param params request parameters.
     *
     * @throws IOException propagated from POST.
     */
    private static void post(String endpoint, Map<String, String> params)
            throws IOException {
        URL url;
        try {
            url = new URL(endpoint);
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("invalid url: " + endpoint);
        }
        
        StringBuilder bodyBuilder = new StringBuilder();
        Iterator<Map.Entry<String, String>> iterator = params.entrySet().iterator();
        // constructs the POST body using the parameters
        while (iterator.hasNext()) {
            Map.Entry<String, String> param = iterator.next();
            bodyBuilder.append(param.getKey()).append('=')
                    .append(param.getValue());
            if (iterator.hasNext()) {
                bodyBuilder.append('&');
            }
        }
        String body = bodyBuilder.toString();
        Util.logDebug("Posting '" + body + "' to " + url);
        byte[] bytes = body.getBytes();
        HttpURLConnection conn = null;
        try {

            try {
            	if (url.getProtocol().toLowerCase().equals("https")) {
            		if (Util.aceptarCualquierCertificado) {
            			trustAllHosts();
            		}
            		HttpsURLConnection https = (HttpsURLConnection) url.openConnection();
            		if (Util.aceptarCualquierCertificado) {
            			https.setHostnameVerifier(DO_NOT_VERIFY);
            		}
            		conn = https;
            	}
            	else {
                    conn = (HttpURLConnection) url.openConnection();
            	}
            } catch (Exception e) {
                Util.logDebug("Exception: " + e.getMessage());
            }        	
        	
            conn.setDoOutput(true);
            conn.setUseCaches(false);
            conn.setFixedLengthStreamingMode(bytes.length);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type",
                    "application/x-www-form-urlencoded;charset=UTF-8");
            // post the request 
            OutputStream out = conn.getOutputStream();
            out.write(bytes);
            out.close();
            // handle the response
            int status = conn.getResponseCode();
            if (status != 200) {
              throw new IOException("Post failed with error code " + status);
            }
        } catch (Exception e) {
        	Util.logDebug("Exception: "+ e.getMessage());
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
      }

    /**
     * Issue a POST request to the server.
     *
     * @param endpoint POST address.
     * @param params request parameters.
     *
     * @throws IOException propagated from POST.
     */
    private static String postWithResponse(String endpoint, Map<String, String> params)
            throws IOException {
        URL url;
        try {
            url = new URL(endpoint);
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("invalid url: " + endpoint);
        }
        StringBuilder bodyBuilder = new StringBuilder();
        Iterator<Map.Entry<String, String>> iterator = params.entrySet().iterator();
        // constructs the POST body using the parameters
        while (iterator.hasNext()) {
            Map.Entry<String, String> param = iterator.next();
            bodyBuilder.append(param.getKey()).append('=')
                    .append(param.getValue());
            if (iterator.hasNext()) {
                bodyBuilder.append('&');
            }
        }
        String body = bodyBuilder.toString();
        Util.logDebug("Posting '" + body + "' to " + url);
        byte[] bytes = body.getBytes();
        HttpURLConnection conn = null;
        try {
        	
            try {
            	if (url.getProtocol().toLowerCase().equals("https")) {
            		if (Util.aceptarCualquierCertificado) {
            			trustAllHosts();
            		}
            		HttpsURLConnection https = (HttpsURLConnection) url.openConnection();
            		if (Util.aceptarCualquierCertificado) {
            			https.setHostnameVerifier(DO_NOT_VERIFY);
            		}
            		conn = https;
            	}
            	else {
                    conn = (HttpURLConnection) url.openConnection();
            	}
            } catch (Exception e) {
                Util.logDebug("Exception: " + e.getMessage());
            }           	
        	
            conn.setDoOutput(true);
            conn.setUseCaches(false);
            conn.setFixedLengthStreamingMode(bytes.length);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type",
                    "application/x-www-form-urlencoded;charset=UTF-8");
            // post the request 
            OutputStream out = conn.getOutputStream();
            out.write(bytes);
            out.close();
            // handle the response
            int status = conn.getResponseCode();
            if (status != 200) {
              throw new IOException("Post failed with error code " + status);
            }
            BufferedReader r = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder total = new StringBuilder();
            String line;
            while ((line = r.readLine()) != null) {
                total.append(line);
            }
            
            return total.toString();
            
        } catch (Exception e) {
        	Util.logDebug("Exception: "+ e.getMessage());
        	return null;
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
      }

}
