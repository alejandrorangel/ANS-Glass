package edu.cicese.android.ans;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class Utilities {
	public static String sdcard;
	public static final String SERVER = "LAPEduardo";
//	public static final String SERVER_ADDR = "158.97.88.188"; //LAPEduardo
//	public static final String SERVER_ADDR = "158.97.91.13";
	public static String SERVER_ADDR;
	public static final int SERVER_PORT = 8000;
	public static final int CLIENT_PORT = 8001;
	public static String imageDirectory = "/sdcard/ans/pictures/";
	public static final String ansDir = "/sdcard/ans/";
	public static final String tagDir = "/sdcard/ans/tags/";

	public static final int NEXUSS_PREVIEW_WIDTH = 720;
	public static final int NEXUSS_PREVIEW_HEIGHT = 480;

	public static final int G2_PREVIEW_WIDTH = 800;
	public static final int G2_PREVIEW_HEIGHT = 480;

//	public static final int PREVIEW_WIDTH = NEXUSS_PREVIEW_WIDTH;
//	public static final int PREVIEW_HEIGHT = NEXUSS_PREVIEW_HEIGHT;
	public static final int PREVIEW_WIDTH = NEXUSS_PREVIEW_WIDTH;
	public static final int PREVIEW_HEIGHT = G2_PREVIEW_HEIGHT;

	public static String imageName;
	
	public static int imageNo = 1;
	
	public static String hostName;
	
	public static boolean /*findingTags = false, */displayingToast = false, playingClip = false,
			checkingRep = false/*, locatingUser = false*/, serverBusy = false;

	public static String APN = "X";
	public static Map<String, Integer> APs = new HashMap<String, Integer>();

	public static boolean menuClicked = false;

	public static String userLocation = "unknown";
	public static int userLocationStrength = 0;
	
	//! Deletes a directory and all its content
	public static void deleteDirectory(String path) {
		try {
			System.out.println("Deleting " + path);
			File dir = new File(path);
			File[] files = dir.listFiles();
			if (files != null) {
				for (File file : files) {
					if (file.isDirectory()) {
						deleteDirectory(file.getCanonicalPath());
					}
					if (!file.delete()) {
						System.out.println("Couldn't delete " + file);
					}
				}
			}
			if (!dir.delete()) {
				System.out.println("Couldn't delete " + dir);
			}
		}
		catch (IOException e) { e.printStackTrace(); }
		catch (NullPointerException e) { e.printStackTrace(); }
	}
	
	//! Checks if the server is busy
	public static boolean isServerBusy(Handler messageHandler) {
		return serverBusy;
		/*if (findingTags) {
			Message msg = new Message();
			Bundle bundle = new Bundle();
			bundle.putInt("type", 2);
			bundle.putString("msg", "Server busy");
			msg.setData(bundle);
			messageHandler.sendMessage(msg);
			return true;
		}
		else {
			return false;
		}*/
	}

	//! Checks if the server is already checking the repository
	public static boolean isServerCheckingRep(Handler messageHandler) {
		if (checkingRep) {
			Message msg = new Message();
			Bundle bundle = new Bundle();
			bundle.putInt("type", 2);
			bundle.putString("msg", "Wait please..");
			msg.setData(bundle);
			messageHandler.sendMessage(msg);
			return true;
		}
		else {
			return false;
		}
	}
	
	//! Loads a String from a file
	public static String loadString(String filename) {
		String string = "";
		try {
			BufferedReader br =  new BufferedReader(new FileReader(new File(filename)));
			try {
				String line = br.readLine();
				while (line != null){
					string += line;
					line = br.readLine();
					if (line != null) {
						string += System.getProperty("line.separator");
					}
				}
			}
			finally {
				br.close();
			}
		}
		catch (IOException e){
			System.out.println("File missing.");
			e.printStackTrace();
			return null;
		}
		return string;
	}

	//! Gets the text annotation from a given tag.
	public static String getTagAnnotation(String tagID, String spaceID) {
		if (tagID.equals("000")) {
			return "No se encontró.";
		}
		String tagPath = tagDir + spaceID + "/" + tagID + "/";
		if (new File(tagPath).exists()) {
			String annotationFilePath = tagPath + tagID + ".txt";
			if (new File(annotationFilePath).exists()) {
				return loadString(annotationFilePath);
			}
			return "Sin anotación textual.";
		}
		else {
			return "Repositorio desactualizado.";
		}
	}

	/**
	 * Checks the needed directories and creates them if necessary
	 */
	public static void checkDirs() {
//		deleteDirectory("/sdcard/ans/");
		new File(imageDirectory).mkdirs();
		new File(tagDir).mkdirs();
	}

	public static void resetTags() {
		deleteDirectory(tagDir);
		new File(tagDir).mkdirs();
	}
}
