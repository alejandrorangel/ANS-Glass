package edu.cicese.android.ans;

import android.media.MediaPlayer;

import java.io.File;
import java.io.IOException;

/**
 * Created by: Eduardo Quintana Contreras
 * Date: 11/05/11
 * Time: 10:33 AM
 */
public class SoundManager extends Thread{

	private String resource;

	public SoundManager(String resource) {
		this.resource = resource;
	}

	@Override
	public void run() {
		if (!Utilities.playingClip) {
			MediaPlayer mediaPlayer = new MediaPlayer();
			try {
				if (new File(resource).exists()) {
					Utilities.playingClip = true;
					mediaPlayer.setDataSource(resource);
					mediaPlayer.prepare();
					mediaPlayer.start();
					try {
						sleep(mediaPlayer.getDuration());
						Utilities.playingClip = false;
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (IllegalStateException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
