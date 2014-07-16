package edu.cicese.android.ans;

import android.os.Vibrator;

class TVibratingNotification extends Thread {

	private Vibrator vibrator;
	private int type;

	public TVibratingNotification(Vibrator vibrator, int type) {
		this.vibrator = vibrator;
		this.type = type;
	}

	@Override
	public void run() {
		// Start immediately, vibrate for 500ms, sleep for 300ms, vibrate for 500ms
		//	v.vibrate(1000);
		if (type == 1) {
			long[] pattern = { 0, 500, 300, 500 };
			vibrator.vibrate(pattern, -1);
		}
		else {
			long[] pattern = { 0, 100};
			vibrator.vibrate(pattern, -1);
		}
	}
}
