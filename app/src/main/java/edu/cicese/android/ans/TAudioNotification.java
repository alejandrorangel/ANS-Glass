package edu.cicese.android.ans;

import android.media.MediaPlayer;

/**
 * Created by: Eduardo Quintana Contreras
 * Date: 16/05/11
 * Time: 03:20 PM
 */
class TAudioNotification extends Thread{

	private MediaPlayer mediaPlayer;

	public TAudioNotification(MediaPlayer mediaPlayer) {
		this.mediaPlayer = mediaPlayer;
	}

	@Override
	public void run(){
		mediaPlayer.start();
		try {
			sleep(mediaPlayer.getDuration());
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}
