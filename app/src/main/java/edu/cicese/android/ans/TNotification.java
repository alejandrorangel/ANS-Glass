package edu.cicese.android.ans;

import android.os.Handler;
import android.os.Message;

/**
 * Created by: Eduardo Quintana Contreras
 * Date: 2/08/11
 * Time: 10:37 AM
 */
public class TNotification extends Thread {

	private Handler dialogHandler;
	private TDialog tDialog;
	private boolean threadDone = false;

	public TNotification(Handler dialogHandler, TDialog tDialog) {
		this.dialogHandler = dialogHandler;
		this.tDialog = tDialog;
	}

	public void done() {
		threadDone = true;
	}

	@Override
	public void run() {
		try {
			sleep(7000);
			if (!threadDone && !tDialog.isAlive()) {
				dialogHandler.sendMessage(new Message());
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}
