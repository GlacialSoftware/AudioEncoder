package com.glacialsoftware.audioencoder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

public class EncodeTask implements Runnable{
	
	private Thread thread=null;
	private Process process=null;
	private List<String> cmd;
	private FfmpegController controller;
	private Handler handler;
	
	private Pattern durationPattern=Pattern.compile("(?<=Duration: )[^,]*");
	private Pattern timePattern = Pattern.compile("(?<=time=)[\\d:.]*");
	private Double duration =null;
	
	private Object mutexEncodeTaskCallbacks = new Object();
	
	public enum HandlerCode{PROGRESS_UPDATE,AFFECT_CANCEL}
	
	public interface EncodeTaskCallbacks{
		public void updateProgressBar(int progress);
		public void cancel();
	}
	
	private EncodeTaskCallbacks encodeTaskCallbacks=null;
	
	public EncodeTask(List<String> cmd, FfmpegController controller,EncodeTaskCallbacks encodeTaskCallbacks){
		this.cmd=cmd;
		this.controller=controller;
		this.encodeTaskCallbacks=encodeTaskCallbacks;
	}

	@Override
	public void run() {
		android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND);
		thread=Thread.currentThread();
		
		handler=new Handler(Looper.getMainLooper()){
			
			@Override
	        public void handleMessage(Message inputMessage) {
				HandlerCode code = HandlerCode.values()[inputMessage.what];
				switch (code){
				case PROGRESS_UPDATE:
					synchronized (mutexEncodeTaskCallbacks) {
						if (encodeTaskCallbacks!=null){
							try{
								encodeTaskCallbacks.updateProgressBar(inputMessage.arg1);
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
		
		if (!setupProcess()){
			Message message = handler.obtainMessage(HandlerCode.AFFECT_CANCEL.ordinal());
			handler.sendMessage(message);
			return;
		}
		if (thread.isInterrupted()){
			return;
		}
		try {
			process.waitFor();
		} catch (InterruptedException e) {
			return;
		}
		Message message = handler.obtainMessage(HandlerCode.AFFECT_CANCEL.ordinal());
		handler.sendMessage(message);
	}
	
	private void affectCancel(){
		synchronized (mutexEncodeTaskCallbacks) {
			if (encodeTaskCallbacks!=null){
				try{
					encodeTaskCallbacks.cancel();
				} catch (Exception e){
					MainActivity.shouldCancel=true;
				}
			} else {
				MainActivity.shouldCancel=true;
			}
		}
	}
	
	private boolean setupProcess(){
		process = controller.execute(cmd);
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
								
								Message message = handler.obtainMessage(HandlerCode.PROGRESS_UPDATE.ordinal(),
										progress.intValue(),0);
								handler.sendMessage(message);
							}
						}
						Log.d("shellOut",shellLine);
		            }
	            } catch (Exception e){
	            	Log.e("AudioEncoder_errorStream", "error reading shell error stream");
	            }
			}
		};
		
		Thread thread = new Thread(errorStream);
		thread.start();
		
		return true;
	}
	
	public void cancel(){
		try{
			if (thread!=null){
				thread.interrupt();
			}
		} catch (Exception e){}
		try{
			if (process!=null){
				process.destroy();
			}
		} catch (Exception e){}
	}
	
	public void removeEncodeTaskCallbacks(){
		synchronized (mutexEncodeTaskCallbacks) {
			encodeTaskCallbacks=null;
		}
	}
	
	public void updateEncodeTaskCallbacks(EncodeTaskCallbacks encodeTaskCallbacks){
		synchronized (mutexEncodeTaskCallbacks) {
			this.encodeTaskCallbacks=encodeTaskCallbacks;
		}
	}

}
