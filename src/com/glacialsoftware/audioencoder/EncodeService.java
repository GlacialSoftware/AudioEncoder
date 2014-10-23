package com.glacialsoftware.audioencoder;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

public class EncodeService extends Service{
	
	private Looper looper;
	private ServiceHandler serviceHandler;
	//private Handler handler;
	private Process process=null;
	private Pattern durationPattern=Pattern.compile("(?<=Duration: )[^,]*");
	private Pattern timePattern = Pattern.compile("(?<=time=)[\\d:.]*");
	private Double duration =null;
	private boolean encodingLocal = false;
	private FfmpegController controller=null;
	private List<String> cmd=null;
	private boolean isCancelled=false;
	private NotificationCompat.Builder notificationBuilder=null;
	private NotificationManager notificationManager=null;
	private Messenger activity = null;
	private File outFile = null;
	private boolean deleteIntermediate=true;
	
	public Object mutexEncodeServiceCallbacks = new Object();
	
	//public enum HandlerCode{PROGRESS_UPDATE,AFFECT_CANCEL}
	public enum CommCodes{CANCEL,ATTACH,ENCODING,PROGRESS_UPDATE,UPDATE_INTERMEDIATE,ERROR}
	
	/*
	
	public interface EncodeServiceCallbacks{
		public void updateProgressBar(int progress);
		public void cancel();
	}
	
	
	private IBinder binder = new LocalBinder();
	
    public class LocalBinder extends Binder {
        EncodeService getService() {
            return EncodeService.this;
        }
    }
    */
	
	
    class IncomingHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
        	CommCodes code = CommCodes.values()[msg.what];

        	switch (code){
        	case ATTACH:
        		activity=msg.replyTo;
        		
        		Message message = Message.obtain(null,CommCodes.ENCODING.ordinal());
        		Bundle bundle = new Bundle();
        		bundle.putBoolean("encoding", encodingLocal);
        		message.setData(bundle);
        		try{
        			activity.send(message);
        		} catch (Exception e){}
        		break;
        	case CANCEL:
        		cancel();
        		break;
        	case UPDATE_INTERMEDIATE:
        		deleteIntermediate = msg.getData().getBoolean("deleteIntermediate");
        		break;
        	default:
        		super.handleMessage(msg);
        	}
        }
    }    
    
    final Messenger messenger = new Messenger(new IncomingHandler());
    
	
	//public static EncodeServiceCallbacks encodeServiceCallbacks=null;

	  private final class ServiceHandler extends Handler {
		  
	      public ServiceHandler(Looper looper) {
	          super(looper);
	      }
	      
	      @Override
	      public void handleMessage(Message msg) {
	  		if (!setupProcess()){
				//Message message = handler.obtainMessage(HandlerCode.AFFECT_CANCEL.ordinal());
				//handler.sendMessage(message);
				Log.d("EncodeService","Failed setup");
				isCancelled=false;
				encodingLocal=false;
				affectCancel();
				return;
			}
			if (isCancelled){
				isCancelled=false;
				encodingLocal=false;
				return;
			}
			int exitVal=0;
			try {
				exitVal = process.waitFor();
			} catch (InterruptedException e) {
				isCancelled=false;
				encodingLocal=false;
				return;
			}
			
			if (exitVal!=0 && (notificationBuilder.build().flags & Notification.FLAG_ONGOING_EVENT)==
					  Notification.FLAG_ONGOING_EVENT){
        		Message message = Message.obtain(null,CommCodes.ERROR.ordinal());
        		try{
        			activity.send(message);
        		} catch (Exception e){}
    			isCancelled=false;
    			encodingLocal=false;
        		affectCancel();
			}
			
			//Message message = handler.obtainMessage(HandlerCode.AFFECT_CANCEL.ordinal());
			//handler.sendMessage(message);
			isCancelled=false;
			encodingLocal=false;
			if ((notificationBuilder.build().flags & Notification.FLAG_ONGOING_EVENT)==
					  Notification.FLAG_ONGOING_EVENT){
		        notificationBuilder.setContentTitle("Encode complete");
		        notificationBuilder.setProgress(0,0,false);
		        notificationBuilder.setOngoing(false);
		        notificationManager.notify(1, notificationBuilder.build());
			}
	        affectCancel();
	      }
	  	}
	  
	  public void sendProgressUpdate(int progress){
		  Message message = Message.obtain(null, CommCodes.PROGRESS_UPDATE.ordinal(), 
				  progress, 0);
		  try{
			  activity.send(message);
		  } catch (Exception e){}
	  }
	  
	  @Override
	  public void onCreate() {
	    HandlerThread handlerThread = new HandlerThread("EncodeServiceThread",
	    		android.os.Process.THREAD_PRIORITY_BACKGROUND);
	    handlerThread.start();
	    isCancelled=false;
	    encodingLocal=false;
	    /*
	    handler=new Handler(Looper.getMainLooper()){
			
			@Override
	        public void handleMessage(Message inputMessage) {
				HandlerCode code = HandlerCode.values()[inputMessage.what];
				switch (code){
				case PROGRESS_UPDATE:
					synchronized (mutexEncodeServiceCallbacks) {
						if (encodeServiceCallbacks!=null){
							try{
								encodeServiceCallbacks.updateProgressBar(inputMessage.arg1);
							} catch (Exception e){}
						}
					}
					break;
				case AFFECT_CANCEL: 
					affectCancel();
					break;
				}
			}
		};
		*/
	
	    looper = handlerThread.getLooper();
	    serviceHandler = new ServiceHandler(looper);
	  }
	  
	  @Override
	  public IBinder onBind(Intent intent) {
	  	  return messenger.getBinder();
	  }

	  @Override
	  public boolean onUnbind (Intent intent){
		  activity=null;
		  return false;
	  }
	  
      @Override
      public void onDestroy() {
    	  cancel();
    	  if (notificationBuilder!=null){
    		  notificationBuilder.setOngoing(false);
    	  }
    	  if (notificationManager!=null){
    		  notificationManager.notify(1, notificationBuilder.build());
    	  }
    }
      
      @Override
      public void onTaskRemoved(Intent rootIntent){
    	  Log.d("EncodeService","TaskRemoved");
    	  cancel();
      }
	  	  
	  @Override
	  public int onStartCommand(Intent intent, int flags, int startId) {
		  
		  if (controller==null){
			  controller=FfmpegController.newInstance(getApplicationContext());
		  }
		  
		  cmd=intent.getStringArrayListExtra("cmd");
		  cmd.add(0, controller.getBinaryPath());
		  
	      Message message = serviceHandler.obtainMessage();
	      message.arg1 = startId;
	      isCancelled=false;
	      encodingLocal=true;
	      
	      if (notificationManager==null){
	    	  notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
	      }
	      if (notificationBuilder==null){
	    	  notificationBuilder = new NotificationCompat.Builder(this);
	      }
	      
	      String input = (new File(cmd.get(3))).getName();
	      String output = (new File(cmd.get(cmd.size()-1))).getName();
	      try {
			outFile=(new File(cmd.get(cmd.size()-1))).getCanonicalFile();
	      } catch (IOException e) {
			outFile=new File(cmd.get(cmd.size()-1));
			e.printStackTrace();
	      }
	      
	      notificationBuilder.setContentTitle("Encoding...");
	      notificationBuilder.setContentText(input+" >> "+output);
	      notificationBuilder.setSmallIcon(R.drawable.ic_encoding_notification);
	      notificationBuilder.setOngoing(true);
	      
	      serviceHandler.sendMessage(message);
	      
	      return START_NOT_STICKY;
	  }
	  
	  /*
	public void removeEncodeServiceCallbacks(){
		synchronized (mutexEncodeServiceCallbacks) {
			encodeServiceCallbacks=null;
		}
	}
	
	public boolean getEncodingLocal(){
		return encodingLocal;
	}
	
	public void updateEncodeServiceCallbacks(EncodeServiceCallbacks encodeServiceCallbacks){
		synchronized (mutexEncodeServiceCallbacks) {
			EncodeService.encodeServiceCallbacks=encodeServiceCallbacks;
		}
	}
	  
	public void setController(FfmpegController controller){
		this.controller=controller;
	}
	
	public boolean needsController(){
		return controller==null;
	}
	*/
	
	private boolean setupProcess(){
		if (controller!=null){
			process = controller.execute(cmd);
		} else {
			return false;
		}
		if (process==null){
			return false;
		}
		
		Runnable errorStream = new Runnable() {
			
			@Override
			public void run() {
	            BufferedReader bufferedReader = new BufferedReader(
	            		new InputStreamReader(process.getErrorStream()));
	            
	            String shellLine=null;
	            try{
		            while ((shellLine = bufferedReader.readLine()) != null){
						Matcher matcher;
						if (duration==null){
							matcher = durationPattern.matcher(shellLine);
							if (matcher.find()){
								String[] hms = matcher.group().split(":");
						        duration = (Integer.parseInt(hms[0]) * 3600)
				                           + (Integer.parseInt(hms[1]) * 60)
				                           + (Double.parseDouble(hms[2]));
							}
							
						} else {
							matcher=timePattern.matcher(shellLine);
							if (matcher.find()){
								String[] hms = matcher.group().split(":");
								Double progress = (((Integer.parseInt(hms[0]) * 3600)
				                                  + (Integer.parseInt(hms[1]) * 60)
				                                  + (Double.parseDouble(hms[2])))
				                                  / duration) * 100;
								
								/*
								Message message = handler.obtainMessage(HandlerCode.PROGRESS_UPDATE.ordinal(),
										progress.intValue(),0);
								handler.sendMessage(message);
								*/
								sendProgressUpdate(progress.intValue());
								notificationBuilder.setProgress(100, progress.intValue(), false);
								notificationManager.notify(1,notificationBuilder.build());
							}
						}
						Log.d("EncodeService",shellLine);
		            }
	            } catch (Exception e){
	            	Log.d("EncodeService","error reading shell error stream");
	            }
			}
		};
		
		Thread thread = new Thread(errorStream);
		thread.start();
		
		return true;
	}
	
	private void affectCancel(){
		if (activity!=null){
			try{
	        	Message message = Message.obtain(null, CommCodes.CANCEL.ordinal());
	        	activity.send(message);
			} catch (Exception e){}
		} 
		cancel();
	}
	
	public void cancel(){
		Log.d("EncodeService","Cancel");
		try{
			  if ((notificationBuilder.build().flags & Notification.FLAG_ONGOING_EVENT)==
					  Notification.FLAG_ONGOING_EVENT){
				  notificationBuilder.setContentTitle("Encode cancelled");
		    	  notificationBuilder.setProgress(0,0,false);
		    	  notificationBuilder.setOngoing(false);
		    	  notificationManager.notify(1, notificationBuilder.build());
		    	  if (deleteIntermediate){
		    		  Log.d("EncodeService","Removing intermediate file");
		    		  if (outFile!=null){
		    			  outFile.delete();
		    			  outFile=null;
		    		  }
		    	  }
			  }
		} catch (Exception e){}
		try{
			if (!isCancelled){
				isCancelled=true;
			}
		} catch (Exception e){}
		try{
			if (process!=null){
				process.destroy();
			}
		} catch (Exception e){}
		try{
			stopSelf();
		} catch (Exception e){}
	}
}
	
	