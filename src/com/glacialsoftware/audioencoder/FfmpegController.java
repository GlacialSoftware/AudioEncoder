package com.glacialsoftware.audioencoder;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import android.content.Context;

public class FfmpegController {
	// procedure to initialize static library based on 
	// guardianproject's android-ffmpeg-java library, heavily 
	// modified to fit this controller class

	private String binaryPath=null;
	
	public FfmpegController(Context context){
		
		File file = new File(context.getDir("bin", 0),"ffmpeg");
		if (file.exists()){
			file.delete();
		}
		
		
		FileOutputStream fileOutputStream;
		try {
			fileOutputStream = new FileOutputStream(file);
		} catch (FileNotFoundException e2) {
			e2.printStackTrace();
			return;
		}
		InputStream inputStream = context.getResources().openRawResource(R.raw.ffmpeg);
		byte buffer[] = new byte[1024];
		int len;
		try{
			while ((len = inputStream.read(buffer)) > 0) {
				fileOutputStream.write(buffer, 0, len);
			}
		}catch (Exception e){
			e.printStackTrace();
			try {
				fileOutputStream.close();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			try {
				inputStream.close();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			return;
		}
		try {
			fileOutputStream.close();
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
		try {
			inputStream.close();
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
		
		try {
			Runtime.getRuntime().exec("chmod 755 "+file.getAbsolutePath()).waitFor();
		} catch (InterruptedException e) {
			e.printStackTrace();
			return;
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
		try {
			binaryPath=file.getCanonicalPath();
		} catch (IOException e) {
			e.printStackTrace();
			binaryPath=null;
			return;
		}
		
	}
	
	public Process execute(List<String> cmd){
		try {
			Runtime.getRuntime().exec("chmod 700 " + binaryPath);
		} catch (IOException e1) {
			e1.printStackTrace();
			return null;
		}
		
		ProcessBuilder processBuilder = new ProcessBuilder(cmd);
		processBuilder.directory(new File(binaryPath).getParentFile());
		
		Process process;
		
		try {
			process = processBuilder.start();
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
		
		return process;
	}
	
	private boolean loadFailed(){
		return binaryPath==null;
	}
	
	public static FfmpegController newInstance(Context context){
		FfmpegController ffmpegController = new FfmpegController(context);
		if (ffmpegController.loadFailed()){
			return null;
		}
		return ffmpegController;		
	}
	
	public String getBinaryPath(){
		return binaryPath;
	}
	
}
