package edu.cicese.android.ans;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

//import android.util.Log;

public class Server extends Thread {
	private Socket socket;
	private Handler messageHandler;
	private boolean threadDone = false;
//	private TTag tagThread;

	public Server(Handler messageHandler/*, TTag tagThread*/) {
		this.messageHandler = messageHandler;
//		this.tagThread = tagThread;
	}

	public void done() {
		threadDone = true;
	}

	@Override
	public void run() {
		try {
			ServerSocket serverSocket = new ServerSocket(Utilities.CLIENT_PORT);
			System.out.println("Waiting for connections...");
			while (!threadDone)
            {
				socket = serverSocket.accept();
                if (socket != null)
                {
                    uploadRepository();
// DataInputStream dis = new DataInputStream(socket.getInputStream());
                	System.out.println("Incoming connection, from " + socket.getInetAddress().getHostAddress() + ".");

	                BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
					JSONObject jo = (JSONObject) new JSONTokener(br.readLine()).nextValue();
        			
        			switch (jo.getInt("command")) {
        				
        				// <<<<< TAGS -----
        				case Command.SEND_IMG: //Send image to find tags
        					printLog("[" + Command.SEND_IMG + "] Image requested.");
        					sendImage("img01.jpg");
        					break;
        				case Command.TAGS_FOUND: //Tags found
        					printLog("[" + Command.TAGS_FOUND + "] Tags found.");
        					showTagsFound(new JSONArray(jo.getString("tags")), jo.getString("spaceID"));
        					break;
        				// ----- TAGS >>>>>
				        

				        case Command.USER_LOC: //User location
        					printLog("[" + Command.USER_LOC + "] User location.");
        					showUserLocation(jo.getString("location"), jo.getInt("strength"));
        					break;
        				case Command.IMG_ACK: //Image received, send next one
        					printLog("[" + Command.IMG_ACK + "] ACK: Image received.");
					        ackReceived();
        					break;
        					
        				/*// <<<<< USER LOCATION -----
        				case Command.SEND_IMG_LOC: //Send image to locate user
        					printLog("[105] Images requested. (to locate user)");
        					sendImage("img02.jpg");
        					
        				//	Utilities.imageNo = obj.getInt("imageNo");
//        					sendImage("img1" + Utilities.imageNo + ".jpg");
//        					Utilities.imageNo++;
        					break;
        				case Command.IMG_ACK: //Image received, send next one
        					printLog("[106] ACK: Image received.");
        					requestUserLocation(Command.IMG_LOC);
//        					if (Utilities.imageNo <= 3) {
//        					//	System.out.println("Sending next image information...");
//        						requestUserLocation(Utilities.imageNo);
//        					}
//        					else {
//        						Utilities.imageNo = 1;
//        					}
        					break;
        				case Command.USER_LOC: //User location
        					printLog("[115] User location.");
        					showUserLocation(jo.getString("location"));
        					break;
        				// ----- USER LOCATION >>>>>*/
        					
        				
        				// <<<<< REPOSITORY -----	
        				case Command.CHK_REP: //Send the repository version
        					printLog("[" + Command.CHK_REP + "] Repository check requested.");
        					checkRepository();
        					break;
        				case Command.TAG_TREE: //File repository structure
        					printLog("[" + Command.TAG_TREE + "] Repository structure received.");
        					performTreeChanges(new JSONArray(jo.getString("tag_tree")));
        					break;
        				case Command.FILE_INFO: //File info
        					printLog("[" + Command.FILE_INFO + "] File info received.");
        					requestTagFile(jo);
        					break;
        				case Command.REP_SYNCD: //Repository synchronized
        					printLog("[" + Command.REP_SYNCD + "] Repositories synchronized.");
					        Utilities.checkingRep = false;
        					/*Message msg = new Message();
        					Bundle bundle = new Bundle();
        					bundle.putInt("type", Command.MSG_INFO);
        					bundle.putString("msg", "Repositorio actualizado");
        					msg.setData(bundle);
        					messageHandler.sendMessage(msg);*/
        					break;
        				// ----- REPOSITORY >>>>>
        			}
        			
//        			dis.close();
	                br.close();
        			socket.close();
        			
             //       ServerThread serverThread = new ServerThread(socket);
             //       serverThread.start();
                }
            }
		}
		catch(IOException e) {
			e.printStackTrace();
		}
		catch(JSONException e) {
			e.printStackTrace();
		}
	}


	public static void sendQuery(){
    	try {
    		System.out.println("Sending query.");

        	JSONObject jo = new JSONObject();
      		jo.put("command", Command.QUERY);
      		jo.put("fileSize", new File(Utilities.imageDirectory + "img01.jpg").length()); //image file size
      		sendPackage(jo.toString());
		}
    	catch (JSONException e) { e.printStackTrace(); }
    }
	
	
	// <<<<< TAGS [SEND_IMG][TAGS_FOUND] -----
	/*//! Requests tags.
	public static void requestTags(){
    	try {
//    		Utilities.findingTags = true;
    		System.out.println("Requesting tags.");
          	
        	JSONObject jo = new JSONObject();
      		jo.put("command", Command.FIND_TAGS);
      		jo.put("fileSize", new File(Utilities.imageDirectory + "img01.jpg").length()); //image file size
      		sendPackage(jo.toString());
		}
    	catch (JSONException e) { e.printStackTrace(); }
    }*/
	
	//! Shows the tag found by the server.
	private void showTagsFound(JSONArray jaTags, String spaceID) {
		try {
			for (int i = 0; i < jaTags.length(); i++) {
				String tagID = jaTags.getJSONObject(i).getString("tagID");

				Message msg = new Message();
				Bundle bundle = new Bundle();
				bundle.putInt("type", Command.MSG_TAG);
				bundle.putString("tagID", tagID);
				bundle.putString("spaceID", spaceID);

				if (!tagID.equals("000")) {
					JSONObject joArea = jaTags.getJSONObject(i).getJSONObject("area");
					JSONArray jaFPS = jaTags.getJSONObject(i).getJSONArray("fps");
					bundle.putString("area", joArea.toString());
					bundle.putString("fps", jaFPS.toString());
//					bundle.putFloat("x1", Float.parseFloat(joArea.getString("x1")));
//					bundle.putFloat("y1", Float.parseFloat(joArea.getString("y1")));
//					bundle.putFloat("x2", Float.parseFloat(joArea.getString("x2")));
//					bundle.putFloat("y2", Float.parseFloat(joArea.getString("y2")));
				}
				
				bundle.putString("msg", getTagAnnotation(tagID, spaceID));
				bundle.putString("imgPath", getImagePath(tagID, spaceID));
//				bundle.putString("msg", jaTags.getString(i));

				msg.setData(bundle);
				messageHandler.sendMessage(msg);
				
			//	playAudioClip(tagID, spaceID);
			}
			Utilities.serverBusy = false;
		}
		catch (JSONException e) { e.printStackTrace(); }
	}
	
	//! Gets the text annotation from a given tag.
	public static String getTagAnnotation(String tagID, String spaceID) {
		if (tagID.equals("000")) {
			return "No se encontró.";
		}
		String tagPath = Utilities.tagDir + spaceID + "/" + tagID + "/";
		if (new File(tagPath).exists()) {
			String annotationFilePath = tagPath + tagID + ".txt";
			if (new File(annotationFilePath).exists()) {
				return Utilities.loadString(annotationFilePath);
			}
			return "Sin anotación textual.";
		}
		else {
			// update repository
			checkRepository();
			return "Repositorio desactualizado.";
		}
	}
	
	private String getImagePath(String tagID, String spaceID) {
		return Utilities.tagDir + spaceID + "/" + tagID + "/" + tagID + ".png";
	}
	// ----- TAGS [SEND_IMG][TAGS_FOUND] >>>>>


	private void ackReceived() {
		Utilities.serverBusy = false;
	}
	
	
	// <<<<< USER LOCATION [SEND_IMG_LOC][IMG_ACK][USER_LOC] -----
/*	//! Requests a user location.
	public static void requestUserLocation(int command) {
		try {
//			Utilities.findingTags = true;
			System.out.println("Sending image info.");
			MainActivity.capturePicture("img02.jpg");
	    	JSONObject jo = new JSONObject();
	  		jo.put("command", command);
	  		jo.put("fileSize", new File(Utilities.imageDirectory + "img02.jpg").length());
	  		sendPackage(jo.toString());
		}
    	catch (JSONException e) { e.printStackTrace(); }
	}*/
	
	//! Sends an image to the server.
	private void sendImage(String imageName) {
		try {
			System.out.println("Sending image " + imageName + "...");
			DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
			File file = new File(Utilities.imageDirectory + imageName);
			FileInputStream fis = new FileInputStream(file);
			byte[] outputBuffer = new byte[8192];
			long sendingCount = (long)Math.ceil((double)file.length() / 8192);
			int cont = 1;
			int size = 8192;
			while (cont <= sendingCount) {
				if (cont == sendingCount)
                {
                    size = (int)(file.length() - ((sendingCount - 1) * 8192));
                }
				fis.read(outputBuffer, 0, 8192);
				dos.write(outputBuffer, 0, size);
				cont++;
			}
			
			fis.close();
			dos.close();
		}
		catch(IOException e) { e.printStackTrace();	}
	}
	
	//! Shows the user location found by the server.
	private void showUserLocation(String location, int strength) {
		boolean userLocated = location.compareTo("00") != 0;
		Utilities.userLocationStrength = strength;
		Utilities.userLocation = location;
		MainActivity.updateLocation(userLocated);
		/*Message msg = new Message();
		Bundle bundle = new Bundle();
		bundle.putInt("type", Command.MSG_INFO);
		bundle.putString("msg", location);
		msg.setData(bundle);
		messageHandler.sendMessage(msg);*/
		
//		Utilities.findingTags = false;
//		Utilities.serverBusy = false;
	}
	// ----- USER LOCATION [SEND_IMG_LOC][IMG_ACK][USER_LOC] >>>>>

	
	
	
	// <<<<< REPOSITORY [CHK_REP][TAG_TREE][FILE_INFO][REP_SYNCD] -----
	//! Sends a request to check the repository changes
	public static void checkRepository() {
    	try {
//    		Utilities.findingTags = true;
		    Utilities.checkingRep = true;
	    	System.out.println("Sending repository checking request...");
	    	JSONObject jo = new JSONObject();
	  		jo.put("command", Command.CHK_REP);
	  		sendPackage(jo.toString());
		}
    	catch (JSONException e) { e.printStackTrace(); }
	}

	//! Sends a request to upload the tag repository
	public static void uploadRepository() {
    	try {
//    		Utilities.findingTags = true;
//		    Utilities.checkingRep = true;
		    Utilities.resetTags();
	    	System.out.println("Sending repository uploading request...");
	    	JSONObject jo = new JSONObject();
	  		jo.put("command", Command.SEND_REP);
	  		sendPackage(jo.toString());
		}
    	catch (JSONException e) { e.printStackTrace(); }
	}
	
	//! Performs changes in the repository structure and requests the missing tags.
	private void performTreeChanges(JSONArray jaChangelog) {
		System.out.println("Performing repository structure changes.");
		File file;
		JSONObject joSpace, joTag;
		JSONArray jaTags;
		String spaceID;
		try {
			for (int x = 0; x < jaChangelog.length(); x++) {
				joSpace = jaChangelog.getJSONObject(x);
				spaceID = joSpace.getString("spaceID");
				file = new File(Utilities.tagDir + spaceID);
				if (joSpace.getInt("action") != Command.DELETED) {
					if (joSpace.getInt("action") == Command.CREATED) {
						file.mkdirs();
					}
					jaTags = joSpace.getJSONArray("tags");
					if (jaTags != null) {
						for (int i = 0; i < jaTags.length(); i++) {
							joTag = jaTags.getJSONObject(i);
							file = new File(Utilities.tagDir + spaceID + "/" + joTag.getString("tagID"));
							if (joTag.getInt("action") == Command.CREATED ||
									joTag.getInt("action") == Command.MODIFIED) {
								file.mkdirs();
							}
							else {
								Utilities.deleteDirectory(file.getCanonicalPath());
							}
						}
					}
				}
				else {
					Utilities.deleteDirectory(file.getCanonicalPath());
				}
			}
		
			if (jaChangelog.length() > 0) {
				System.out.println("Requesting missing tags.");
		      	JSONObject jo = new JSONObject();
		  		jo.put("command", Command.SEND_TAGS);
		  		sendPackage(jo.toString());
			}
		} 
    	catch (IOException e) { e.printStackTrace(); }
    	catch (JSONException e) { e.printStackTrace(); }
	}
	
	//! Request a tag file (missing in the repository)
	@SuppressWarnings({"ConstantConditions"})
	private void requestTagFile(JSONObject fileInfo) {
		try {
			System.out.println("Requesting tag file " + 
					fileInfo.getString("filename") + " (" + fileInfo.getLong("fileSize") + ") bytes.");
			
			Socket socket = new Socket(Utilities.SERVER_ADDR, Utilities.SERVER_PORT);
			DataInputStream dis = new DataInputStream(socket.getInputStream());
			DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
			
	    	JSONObject jo = new JSONObject();
    		jo.put("command", Command.SEND_TAG_FILE);
            dos.writeBytes(jo.toString() + "\n");
            
            String tagID = fileInfo.getString("filename").substring(0, 3);
            String path = Utilities.tagDir + fileInfo.getString("spaceID") + "/" +
            		tagID + "/" + fileInfo.getString("filename");
            
			//Receive tag file
			downloadTagFile(dis, path, fileInfo.getLong("fileSize"));
			
			dis.close();
			dos.close();
			socket.close();
		}
		catch(Exception e) { e.printStackTrace(); }
	}
	
	//! Receives a tag file.
	private void downloadTagFile(DataInputStream dis, String path, long fileSize) {
		try {
			FileOutputStream fos = new FileOutputStream(new File(path));
				
			byte[] inputBuffer = new byte[8192];
	        long readBytes = 0;
			
			while (readBytes < fileSize) {
				int bytes = dis.read(inputBuffer);
				readBytes += bytes;
				fos.write(inputBuffer, 0, bytes);
			}
			
			fos.close();
			System.out.println("Tag file received.");
			sendACK();
		}
		catch(Exception e) { e.printStackTrace(); }
	}
	
	//! Sends and ACK to confirm the reception of tag file.
	private void sendACK() {
		try {
			System.out.println("Sending ACK...");
			JSONObject jo = new JSONObject();
    		jo.put("command", Command.TAG_FILE_ACK);
    		sendPackage(jo.toString());
		}
		catch(JSONException e) { e.printStackTrace(); }
	}
	// ----- REPOSITORY [CHK_REP][TAG_TREE][FILE_INFO][REP_SYNCD] >>>>>

	
	
	
	//! Sends a package to the client.
	private static void sendPackage(String msg) {
		try {
			Socket socket = new Socket(Utilities.SERVER_ADDR, Utilities.SERVER_PORT);
//			Socket socket = new Socket(getServerAddress(), Utilities.SERVER_PORT);
			DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
            dos.writeBytes(msg + "\n");
			dos.close();
			socket.close();
		}
		catch(IOException e) { e.printStackTrace();	}
	}
	
	//! Print in the console.
	private void printLog(String text) {
		System.out.println(text);
	}
	
	
//	public String getServerAddress() {
//		SharedPreferences preferences = 
//			PreferenceManager.getDefaultSharedPreferences(AmbientNotificationSystem.context);
//		return preferences.getString("serverPref", "n/a");
//	}
	
	
	
	// <<<<< TAG_TREE -----
	//! Performs changes in the repository structure.
/*	private void performTreeChanges(JSONArray jaTreeChanges) {
		System.out.println("Performing repository structure changes.");
		File file;
		JSONObject jo;
		try {
			for (int i = 0; i < jaTreeChanges.length(); i++) {
				jo = jaTreeChanges.getJSONObject(i);
				switch (jo.getInt("command")) {
					case Command.MK_SPACE:
						file = new File(Utilities.tagDir + jo.getString("space"));
						file.mkdir();
						break;
					case Command.DEL_SPACE:
						file = new File(Utilities.tagDir + jo.getString("space"));
						file.delete();
						break;
					case Command.MK_TAG:
						file = new File(Utilities.tagDir + jo.getString("space") + "/" + jo.getString("tag"));
						file.mkdir();
						break;
					case Command.DEL_TAG:
						file = new File(Utilities.tagDir + jo.getString("space") + "/" + jo.getString("tag"));
						file.delete();
						break;
				}
			}
		
	    	System.out.println("Requesting missing tags.");
	      	Socket socket = new Socket(Utilities.SERVER_ADDR, Utilities.SERVER_PORT);
	      	DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
	      	
	    	jo = new JSONObject();
	  		jo.put("command", Command.SEND_TAGS);
	  		dos.writeBytes(jo.toString() + "\n");  
			
	  		dos.close();
			socket.close();
		} 
    	catch (IOException e) { e.printStackTrace(); }
    	catch (JSONException e) { e.printStackTrace(); }
	}*/

	
	
	
	// <<<<< SEND_REP_VER -----
	//! Sends the repository version.
/*	public static void sendRepositoryVersion() {
    	try {
	    	System.out.println("Sending repository version...");
	      	Socket socket = new Socket(Utilities.SERVER_ADDR, Utilities.SERVER_PORT);
	      	DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
	      	
	    	JSONObject jo = new JSONObject();
	  		jo.put("command", Command.REP_VER);
	  		jo.put("repositoryVersion", Utilities.getConfigFile().getString("client_rep_version"));
	  		dos.writeBytes(jo.toString() + "\n");  
			
	  		dos.close();
			socket.close();
		}
    	catch (IOException e) {	e.printStackTrace(); }
    	catch (JSONException e) { e.printStackTrace(); }
	}*/
	// ----- SEND_REP_VER >>>>>
	
	//! Deletes a directory and all its content.
//	private void deleteDirectory(String path) {
//		try {
//			File dir = new File(path);
//			File[] files = dir.listFiles();
//			for (File file : files) {
//				if (file.isDirectory()) {
//					deleteDirectory(file.getCanonicalPath());
//				}
//				file.delete();
//			}
//			dir.delete();
//		}
//		catch (IOException e) { e.printStackTrace(); }
//		catch (NullPointerException e) { e.printStackTrace(); }
//	}
	// ----- TAG_TREE >>>>>
	


	/*
	public static void requestUserLocation(int imageNo) {
		System.out.println("Sending image info.");
    	try {
	    	System.out.println("Requesting user location.");
	      	Socket socket = new Socket(Utilities.SERVER_ADDR, Utilities.SERVER_PORT);
	      	DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
	      	
	      	File file = new File(Utilities.imageDirectory + "img1" + imageNo + ".jpg");
	      	
	    	JSONObject jo = new JSONObject();
	  		jo.put("command", Command.LOC_USER);
	  		jo.put("fileSize", file.length()); //image file size
	  		dos.writeBytes(jo.toString() + "\n");  

	  		dos.close();
			socket.close();
		}
    	catch (IOException e) {	e.printStackTrace(); }
    	catch (JSONException e) { e.printStackTrace(); }
	}*/
}
