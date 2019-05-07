/*
 * Copyright (C) 2010 ZXing authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.karics.library.zxing.android;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.AssetFileDescriptor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.util.Log;

import java.io.Closeable;
import java.io.IOException;

import org.ppkpub.ppkbrowser.R;
import com.karics.library.zxing.android.PreferencesActivity;

/**
 * Manages beeps and vibrations for {@link CaptureActivity}. 绠＄悊澹伴煶鍜岄渿鍔�
 */
public final class BeepManager implements MediaPlayer.OnCompletionListener,
		MediaPlayer.OnErrorListener, Closeable {

	private static final String TAG = BeepManager.class.getSimpleName();

	private static final float BEEP_VOLUME = 0.10f;
	private static final long VIBRATE_DURATION = 200L;

	private final Activity activity;
	private MediaPlayer mediaPlayer;
	private boolean playBeep;
	private boolean vibrate;

	public BeepManager(Activity activity) {
		this.activity = activity;
		this.mediaPlayer = null;
		updatePrefs();
	}

	public synchronized void updatePrefs() {
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(activity);
		playBeep = shouldBeep(prefs, activity);
		vibrate = prefs.getBoolean(PreferencesActivity.KEY_VIBRATE, false);
		if (playBeep && mediaPlayer == null) {
			// The volume on STREAM_SYSTEM is not adjustable, and users found it
			// too loud,
			// so we now play on the music stream.
			// 璁剧疆activity闊抽噺鎺у埗閿帶鍒剁殑闊抽娴�
			activity.setVolumeControlStream(AudioManager.STREAM_MUSIC);
			mediaPlayer = buildMediaPlayer(activity);
		}
	}

	/**
	 * 寮�鍚搷閾冨拰闇囧姩
	 */
	public synchronized void playBeepSoundAndVibrate() {
		if (playBeep && mediaPlayer != null) {
			mediaPlayer.start();
		}
		if (vibrate) {
			Vibrator vibrator = (Vibrator) activity
					.getSystemService(Context.VIBRATOR_SERVICE);
			vibrator.vibrate(VIBRATE_DURATION);
		}
	}

	/**
	 * 鍒ゆ柇鏄惁闇�瑕佸搷閾�
	 * 
	 * @param prefs
	 * @param activity
	 * @return
	 */
	private static boolean shouldBeep(SharedPreferences prefs, Context activity) {
		boolean shouldPlayBeep = prefs.getBoolean(
				PreferencesActivity.KEY_PLAY_BEEP, true);
		if (shouldPlayBeep) {
			// See if sound settings overrides this
			AudioManager audioService = (AudioManager) activity
					.getSystemService(Context.AUDIO_SERVICE);
			if (audioService.getRingerMode() != AudioManager.RINGER_MODE_NORMAL) {
				shouldPlayBeep = false;
			}
		}
		return shouldPlayBeep;
	}

	/**
	 * 鍒涘缓MediaPlayer
	 * 
	 * @param activity
	 * @return
	 */
	private MediaPlayer buildMediaPlayer(Context activity) {
		MediaPlayer mediaPlayer = new MediaPlayer();
		mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
		// 鐩戝惉鏄惁鎾斁瀹屾垚
		mediaPlayer.setOnCompletionListener(this);
		mediaPlayer.setOnErrorListener(this);
		// 閰嶇疆鎾斁璧勬簮
		try {
			AssetFileDescriptor file = activity.getResources()
					.openRawResourceFd(R.raw.beep);
			try {
				mediaPlayer.setDataSource(file.getFileDescriptor(),
						file.getStartOffset(), file.getLength());
			} finally {
				file.close();
			}
			// 璁剧疆闊抽噺
			mediaPlayer.setVolume(BEEP_VOLUME, BEEP_VOLUME);
			mediaPlayer.prepare();
			return mediaPlayer;
		} catch (IOException ioe) {
			Log.w(TAG, ioe);
			mediaPlayer.release();
			return null;
		}
	}

	@Override
	public void onCompletion(MediaPlayer mp) {
		// When the beep has finished playing, rewind to queue up another one.
		mp.seekTo(0);
	}

	@Override
	public synchronized boolean onError(MediaPlayer mp, int what, int extra) {
		if (what == MediaPlayer.MEDIA_ERROR_SERVER_DIED) {
			// we are finished, so put up an appropriate error toast if required
			// and finish
			activity.finish();
		} else {
			// possibly media player error, so release and recreate
			mp.release();
			mediaPlayer = null;
			updatePrefs();
		}
		return true;
	}

	@Override
	public synchronized void close() {
		if (mediaPlayer != null) {
			mediaPlayer.release();
			mediaPlayer = null;
		}
	}

}
