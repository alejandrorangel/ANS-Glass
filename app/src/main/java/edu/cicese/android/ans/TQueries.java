package edu.cicese.android.ans;

import android.os.Handler;

/**
 * Created by: Eduardo Quintana Contreras
 * Date: 15/07/11
 * Time: 11:56 AM
 */
public class TQueries extends Thread {
	private Handler messageHandler;
	private boolean threadDone = false;

	public TQueries(Handler messageHandler) {
		this.messageHandler = messageHandler;
	}

	public void done() {
		threadDone = true;
	}

	@Override
	public void run() {
		try {
			sleep(3000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		while(!threadDone) {
//			System.err.println("WARN: " + Utilities.serverBusy + " " + Utilities.displayingToast + " " + Utilities.playingClip);
			if (!Utilities.serverBusy && !Utilities.displayingToast && !Utilities.playingClip) {
				MainActivity.query();
			}
			else {
				try {
					sleep(10);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}
}
