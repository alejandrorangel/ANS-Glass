package edu.cicese.android.ans;

import android.os.Handler;

/**
 * Created by: Eduardo Quintana Contreras
 * Date: 17/05/11
 * Time: 11:31 AM
 */
public class TRepository extends Thread {
	private Handler messageHandler;
	private boolean threadDone = false;

	public TRepository(Handler messageHandler) {
		this.messageHandler = messageHandler;
	}

	public void done() {
		threadDone = true;
	}

	@Override
	public void run() {
//		try {
//			sleep(5000);
//		} catch (InterruptedException e) {
//			e.printStackTrace();
//		}
		while(!threadDone) {
			if (!Utilities.isServerCheckingRep(messageHandler)) {
				Server.checkRepository();
			}
//			else {
//				try {
//					sleep(50);
//				} catch (InterruptedException e) {
//					e.printStackTrace();
//				}
//			}

			try {
				sleep(60000);
			} catch (InterruptedException e) { e.printStackTrace(); }
		}
	}
}
