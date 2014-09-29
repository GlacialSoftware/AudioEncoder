package com.glacialsoftware.audioencoder;

import java.io.IOException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


import org.ffmpeg.android.FfmpegController;
import org.ffmpeg.android.ShellUtils;
import org.ffmpeg.android.ShellUtils.ShellCallback;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

public class EncodeTask implements Runnable{
	
	private Thread thread=null;
	private Process process=null;
	private List<String> cmd;
	private ShellCallback sc;
	private FfmpegController controller;
	private Handler handler;
	
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
		ShellCallback sc = new ShellUtils.ShellCallback() {
			
			private Pattern durationPattern=Pattern.compile("(?<=Duration: )[^,]*");
			private Pattern timePattern = Pattern.compile("(?<=time=)[\\d:.]*");
			private Double duration =null;
			
			@Override
			public void shellOut(String shellLine) {
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
			
			@Override
			public void processComplete(int exitValue) {
			
				Log.d("processComplete",Integer.toString(exitValue));
		}};
		
		try {
			process=controller.execFFMPEG(cmd,sc);
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		} catch (InterruptedException e) {
			e.printStackTrace();
			return false;
		}
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
