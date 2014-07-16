package edu.cicese.android.ans;

import android.os.Handler;
import android.os.Message;

/**
 * Created by: Eduardo Quintana Contreras
 * Date: 25/07/11
 * Time: 03:38 PM
 */
public class TDialog extends Thread {
	private Handler dialogHandler;
	private boolean threadDone = false;

	public TDialog(Handler dialogHandler) {
		this.dialogHandler = dialogHandler;
	}

	public void done() {
		threadDone = true;
	}

	@Override
	public void run() {
		try {
			sleep(7000);
			if (!threadDone) {
				dialogHandler.sendMessage(new Message());
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}
