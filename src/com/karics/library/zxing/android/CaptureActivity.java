package com.karics.library.zxing.android;

import org.ppkpub.ppkbrowser.R;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.DecodeHintType;
import com.google.zxing.Result;
import com.karics.library.zxing.android.BeepManager;
import com.karics.library.zxing.android.CaptureActivityHandler;
import com.karics.library.zxing.android.FinishListener;
import com.karics.library.zxing.android.InactivityTimer;
import com.karics.library.zxing.android.IntentSource;
import com.karics.library.zxing.camera.CameraManager;
import com.karics.library.zxing.view.ViewfinderView;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.Toast;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;

/**
 * 鏉╂瑤閲渁ctivity閹垫挸绱戦惄鍛婃簚閿涘苯婀崥搴″酱缁捐法鈻奸崑姘埗鐟欏嫮娈戦幍顐ｅ伎閿涙稑鐣犵紒妯哄煑娴滃棔绔存稉顏嗙波閺嬫抚iew閺夈儱搴滈崝鈺傤劀绾喖婀撮弰鍓с仛閺夆�宠埌閻緤绱濋崷銊﹀閹诲繒娈戦弮璺猴拷娆愭▔缁�鍝勫冀妫ｅ牅淇婇幁顖ょ礉
 * 閻掕泛鎮楅崷銊﹀閹诲繑鍨氶崝鐔烘畱閺冭泛锟芥瑨顩惄鏍ㄥ閹诲繒绮ㄩ弸锟�
 * 
 */
public final class CaptureActivity extends Activity implements
		SurfaceHolder.Callback {

	private static final String TAG = CaptureActivity.class.getSimpleName();
	
	// 閻╁憡婧�閹貉冨煑
	private boolean isOpen = false;
	Parameters params;
    private Camera camera;
	private CameraManager cameraManager;
	private CaptureActivityHandler handler;
	private ViewfinderView viewfinderView;
	private boolean hasSurface;
	private IntentSource source;
	private Collection<BarcodeFormat> decodeFormats;
	private Map<DecodeHintType, ?> decodeHints;
	private String characterSet;
	// 閻㈢敻鍣洪幒褍鍩�
	private InactivityTimer inactivityTimer;
	// 婢逛即鐓堕妴渚�娓块崝銊﹀付閸掞拷
	private BeepManager beepManager;

	private ImageButton imageButton_back;
	
	private ImageButton imageButton_flashlight;

	public ViewfinderView getViewfinderView() {
		return viewfinderView;
	}

	public Handler getHandler() {
		return handler;
	}

	public CameraManager getCameraManager() {
		return cameraManager;
	}

	public void drawViewfinder() {
		viewfinderView.drawViewfinder();
	}

	/**
	 * OnCreate娑擃厼鍨垫慨瀣娑擄拷娴滄稖绶熼崝鈺冭閿涘苯顩nactivityTimer閿涘牅绱ら惇鐙呯礆閵嗕竻eep閿涘牆锛愰棅绛圭礆娴犮儱寮稟mbientLight閿涘牓妫崗澶屼紖閿涳拷
	 */
	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		// 娣囨繃瀵擜ctivity婢跺嫪绨崬銈夊晪閻樿埖锟斤拷
		Window window = getWindow();
		window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		setContentView(R.layout.activity_capture);

		hasSurface = false;

		inactivityTimer = new InactivityTimer(this);
		beepManager = new BeepManager(this);

		imageButton_back = (ImageButton) findViewById(R.id.capture_imageview_back);
		imageButton_back.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				finish();
			}
		});
		
		imageButton_flashlight = (ImageButton) findViewById(R.id.capture_imageview_flashlight);
		imageButton_flashlight.setOnClickListener(new View.OnClickListener() 
		{
			@Override
			public void onClick(View v) 
			{
				if(!isOpen)
				{
					openLight();
				}
				else
				{
					closeLight();
				}
			}
		});
	}
	
	private void openLight()
	{
		camera = CameraManager.getCamera();
        params = camera.getParameters();
        params.setFlashMode(Parameters.FLASH_MODE_TORCH);
        camera.setParameters(params);
        camera.startPreview(); // 寮�濮嬩寒鐏�
        isOpen = true;
	}
	
	private void closeLight()
	{
		params.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
		camera.setParameters(params); // 鍏虫帀浜伅
        isOpen = false;
	}
	
	@Override
	protected void onResume() {
		super.onResume();

		// CameraManager韫囧懘銆忛崷銊ㄧ箹闁插苯鍨垫慨瀣閿涘矁锟藉奔绗夐弰顖氭躬onCreate()娑擃厹锟斤拷
		// 鏉╂瑦妲歌箛鍛淬�忛惃鍕剁礉閸ョ姳璐熻ぐ鎾村灉娴狀剛顑囨稉锟藉▎陇绻橀崗銉︽闂囷拷鐟曚焦妯夌粈鍝勫簻閸斺晠銆夐敍灞惧灉娴狀剙鑻熸稉宥嗗厒閹垫挸绱慍amera,濞村鍣虹仦蹇撶婢堆冪毈
		// 瑜版挻澹傞幓蹇旑攱閻ㄥ嫬鏄傜�甸晲绗夊锝団�橀弮鏈电窗閸戣櫣骞嘼ug
		cameraManager = new CameraManager(getApplication());

		viewfinderView = (ViewfinderView) findViewById(R.id.viewfinder_view);
		viewfinderView.setCameraManager(cameraManager);

		handler = null;

		SurfaceView surfaceView = (SurfaceView) findViewById(R.id.preview_view);
		SurfaceHolder surfaceHolder = surfaceView.getHolder();
		if (hasSurface) {
			// activity閸︹暚aused閺冩湹绲炬稉宥勭窗stopped,閸ョ姵顒漵urface娴犲秵妫�涙ê婀敍锟�
			// surfaceCreated()娑撳秳绱扮拫鍐暏閿涘苯娲滃銈呮躬鏉╂瑩鍣烽崚婵嗩潗閸栨溈amera
			initCamera(surfaceHolder);
		} else {
			// 闁插秶鐤哻allback閿涘瞼鐡戝鍗籾rfaceCreated()閺夈儱鍨垫慨瀣camera
			surfaceHolder.addCallback(this);
		}

		beepManager.updatePrefs();
		inactivityTimer.onResume();

		source = IntentSource.NONE;
		decodeFormats = null;
		characterSet = null;
	}

	@Override
	protected void onPause() {
		if (handler != null) {
			handler.quitSynchronously();
			handler = null;
		}
		inactivityTimer.onPause();
		beepManager.close();
		cameraManager.closeDriver();
		if (!hasSurface) {
			SurfaceView surfaceView = (SurfaceView) findViewById(R.id.preview_view);
			SurfaceHolder surfaceHolder = surfaceView.getHolder();
			surfaceHolder.removeCallback(this);
		}
		super.onPause();
	}

	@Override
	protected void onDestroy() {
		inactivityTimer.shutdown();
		super.onDestroy();
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		if (!hasSurface) {
			hasSurface = true;
			initCamera(holder);
		}
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		hasSurface = false;
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {

	}

	/**
	 * 閹殿偅寮块幋鎰閿涘苯顦╅悶鍡楀冀妫ｅ牅淇婇幁锟�
	 * 
	 * @param rawResult
	 * @param barcode
	 * @param scaleFactor
	 */
	public void handleDecode(Result rawResult, Bitmap barcode, float scaleFactor) {
		inactivityTimer.onActivity();

		boolean fromLiveScan = barcode != null;
		//鏉╂瑩鍣锋径鍕倞鐟欙絿鐖滅�瑰本鍨氶崥搴ｆ畱缂佹挻鐏夐敍灞绢劃婢跺嫬鐨㈤崣鍌涙殶閸ョ偘绱堕崚鐧哻tivity婢跺嫮鎮�
		if (fromLiveScan) {
			beepManager.playBeepSoundAndVibrate();

//			Toast.makeText(this, "閹殿偅寮块幋鎰", Toast.LENGTH_SHORT).show();

			Intent intent = getIntent();
			intent.putExtra("codedContent", rawResult.getText());
			intent.putExtra("codedBitmap", barcode);
			setResult(RESULT_OK, intent);
			finish();
		}

	}

	/**
	 * 閸掓繂顫愰崠鏈嘺mera
	 * 
	 * @param surfaceHolder
	 */
	private void initCamera(SurfaceHolder surfaceHolder) {
		if (surfaceHolder == null) {
			throw new IllegalStateException("No SurfaceHolder provided");
		}
		if (cameraManager.isOpen()) {
			return;
		}
		try {
			// 閹垫挸绱慍amera绾兛娆㈢拋鎯ь槵
			cameraManager.openDriver(surfaceHolder);
			// 閸掓稑缂撴稉锟芥稉鐚ndler閺夈儲澧﹀锟芥０鍕潔閿涘苯鑻熼幎娑樺毉娑擄拷娑擃亣绻嶇悰灞炬瀵倸鐖�
			if (handler == null) {
				handler = new CaptureActivityHandler(this, decodeFormats,
						decodeHints, characterSet, cameraManager);
			}
		} catch (IOException ioe) {
			Log.w(TAG, ioe);
			displayFrameworkBugMessageAndExit();
		} catch (RuntimeException e) {
			Log.w(TAG, "Unexpected error initializing camera", e);
			displayFrameworkBugMessageAndExit();
		}
	}

	/**
	 * 閺勫墽銇氭惔鏇炵湴闁挎瑨顕ゆ穱鈩冧紖楠炲爼锟斤拷閸戝搫绨查悽锟�
	 */
	private void displayFrameworkBugMessageAndExit() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(getString(R.string.app_name));
		builder.setMessage(getString(R.string.msg_camera_framework_bug));
		builder.setPositiveButton(R.string.button_ok, new FinishListener(this));
		builder.setOnCancelListener(new FinishListener(this));
		builder.show();
	}

}
