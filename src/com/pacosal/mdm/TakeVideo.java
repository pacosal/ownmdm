/*
 * TakeVideo.java
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

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.media.MediaRecorder;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;

public class TakeVideo  implements SurfaceHolder.Callback {

	private String OUTPUT_FILE= "";
	
	public View mViewMB; 
    public LocAlarmService las;
    public int camera = 0;
    MediaRecorder mediaRecorder = null;
    
    public boolean exito = true;
	
    //a variable to store a reference to the Surface View at the main.xml file  
    private SurfaceView sv;  
    //a variable to control the camera   
    private Camera mCamera;  
    //the camera parameters  
    private Parameters parameters;     
    //a surface holder  
    private SurfaceHolder sHolder;     
    
    Camera.PictureCallback mCall; 	
    private SurfaceHolder holderOrig;

    
    public TakeVideo(LocAlarmService las, int camera, String output) { 
    	this.las = las;
    	this.camera = camera;
    	this.OUTPUT_FILE = output;
    }
    
    public void destroyView() {
    	if(mViewMB != null)
	    {
	        ((WindowManager) las.getSystemService(Context.WINDOW_SERVICE)).removeView(mViewMB);
	        mViewMB = null;
	    }
    	
    }
    
    public void createView() {
    	
    	Util.logDebug("createView()");
    	
	    if(mViewMB != null)
	    {
	        ((WindowManager) las.getSystemService(Context.WINDOW_SERVICE)).removeView(mViewMB);
	        mViewMB = null;
	    }
	
	    WindowManager wm = (WindowManager) las.getSystemService(Context.WINDOW_SERVICE);
	    
	    LayoutInflater li = (LayoutInflater) las.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	    mViewMB = li.inflate(R.layout.surface, null);
	    mViewMB.setVisibility(View. VISIBLE);
	    WindowManager.LayoutParams paramsMB = new WindowManager.LayoutParams(
	    		5,
	    		5,
	    		WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY | WindowManager.LayoutParams.TYPE_SYSTEM_ALERT
	    		,
	            WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
	            PixelFormat.TRANSLUCENT);
	    paramsMB.gravity = Gravity.LEFT | Gravity.TOP;
	    paramsMB.y = 0;
	    
	    wm.addView(mViewMB, paramsMB);
	    
        //get the Surface View at the main.xml file  
        sv = (SurfaceView) mViewMB.findViewById(R.id.surfaceView);  
  
        //Get a surface  
        sHolder = sv.getHolder();  
  
        //add the callback interface methods defined below as the Surface View callbacks  
        sHolder.addCallback(this);  
  
        //tells Android that this surface will have its data constantly replaced  
        sHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS); 	    
    	
    }
    
    public void take() {
    	
    	Util.logDebug("take()");    	
 
    	try {
	    	mediaRecorder = new MediaRecorder();
	    	try {
		    	mCamera.lock();
		    	mCamera.unlock();
	    	} catch (Exception e){};
	    	mediaRecorder.setCamera(mCamera);
	    	mediaRecorder.setAudioSource(MediaRecorder.AudioSource.DEFAULT);
	    	mediaRecorder.setVideoSource(MediaRecorder.VideoSource.DEFAULT);
	    	mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
	    	//mediaRecorder.setMaxDuration(4000);
	    	mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT); 
	    	mediaRecorder.setOrientationHint(270);
	    	mediaRecorder.setOutputFile(OUTPUT_FILE);
	    	Util.logDebug("output_file: " + OUTPUT_FILE);  
	    	mediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
	    	mediaRecorder.setVideoSize(320, 240);
	    	mediaRecorder.setVideoFrameRate(15);
	    	mediaRecorder.setPreviewDisplay(sHolder.getSurface());
    	}
    	catch (Exception e) {
    		Util.logDebug("Exception: " + e.getMessage());
    		exito = false;
    	}
    	
    	try {Thread.sleep(300);} catch (InterruptedException e) {}
    	
    	try {
			mediaRecorder.prepare();
		} catch (Exception e1) {
			e1.printStackTrace();
			exito = false;
		}

    	try {Thread.sleep(300);} catch (InterruptedException e) {}
		
    	try {
    		mediaRecorder.start();
    	}
    	catch (Exception e) {
    		Util.logDebug("Exception: " + e.getMessage());
    		exito = false;
    	}
    	exito = true;
        
    }
    
    public void surfaceChanged(SurfaceHolder arg0, int arg1, int arg2, int arg3)  
    {  
    	Util.logDebug("surfaceChanged()");      
        //get camera parameters
    	if (mCamera != null) {
	        parameters = mCamera.getParameters();
	        parameters.set("orientation", "portrait");
	 
	        //set camera parameters  
	        mCamera.setParameters(parameters);  
	        mCamera.startPreview();
    	}
 
    }  
  
    public void surfaceCreated(SurfaceHolder holder)  
    {  
    	Util.logDebug("surfaceCreated()");

    	// The Surface has been created, acquire the camera and tell it where  
        // to draw the preview.  
        holderOrig = holder;
        preview();
    }  
    
    void preview() {
    	
    	Util.logDebug("preview()");
    	
    	if (mCamera != null) {
            //stop the preview  
            mCamera.stopPreview();  
            //release the camera  
            mCamera.release();  
            //unbind the camera from this object  
            mCamera = null;      		
    	}

        try {
            mCamera = Camera.open(camera); // viene desde el servicio la camara a utilizar
        } catch (RuntimeException e) {
            Util.logDebug( "Camera failed to open: " + e.getLocalizedMessage());
        }
    	
    	if (mCamera == null) {
    		Util.logDebug("Camera = null");     		
    		return;
    	}
    	
        try {  
       	
           mCamera.setPreviewDisplay(holderOrig);  
  
        } catch (Exception exception) {  
            mCamera.release();  
            mCamera = null; 
            return;
        }  
        
        //get camera parameters  
        parameters = mCamera.getParameters();
        if (las.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT)
        {   
        	parameters.set("orientation", "portrait");
        	if (camera == 1)
        		parameters.set("rotation",270);
        	else 
        		parameters.set("rotation",90);
        }
        if (las.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE)
        {                               
        	parameters.set("orientation", "landscape");          
        	if (camera == 1)
        		parameters.set("rotation",270);
        	else 
        		parameters.set("rotation",90);
        }
        
        //set camera parameters  
        mCamera.setParameters(parameters);  
        
        mCamera.startPreview();
        


        
    }

    public void surfaceDestroyed(SurfaceHolder holder)  
    {  
    	Util.logDebug("surfaceDestroyed()");
    	try {
    		
	    	if (mCamera != null) {
	    		mediaRecorder.stop();
		        mediaRecorder.reset();
		        mediaRecorder.release();
	    		//stop the preview  
		        mCamera.stopPreview();  
		        //release the camera  
		        mCamera.release();  
		        //unbind the camera from this object  
		        mCamera = null;  
	    	}
    	} catch (Exception e) {
    		Util.logDebug("Exception: " + e.getMessage());
    	}
    }    


}
