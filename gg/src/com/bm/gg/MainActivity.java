package com.bm.gg;

import java.util.List;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.SpannableString;
import android.text.style.StyleSpan;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.bm.gg.R;
import com.bm.gg.constant.GGConstant;
import com.bm.gg.util.ApkInstaller;
import com.iflytek.speech.SpeechUtility;

public class MainActivity extends Activity implements OnClickListener {

	private Intent soundIntent = new Intent(GGConstant.INTENT_SOUND_SERVICE);
	private Intent recogIntent = new Intent(GGConstant.INTENT_RECOG_SERVICE);

	private DataReceiver dataReceiver;// BroadcastReceiver对象
	private TextView statusTV;// TextView对象应用

	private Handler mHandler;
	private Dialog mLoadDialog;
	private Button btnStop;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		initComps();
		initLayout();
		mHandler = new Myhandler();
		if (!checkSpeechServiceInstall()) {
			statusTV.setText("安装插件中...");
			final Dialog dialog = new Dialog(this, R.style.dialog);

			LayoutInflater inflater = getLayoutInflater();
			View alertDialogView = inflater.inflate(
					R.layout.superman_alertdialog, null);
			dialog.setContentView(alertDialogView);
			Button okButton = (Button) alertDialogView.findViewById(R.id.ok);
			Button cancelButton = (Button) alertDialogView
					.findViewById(R.id.cancel);
			TextView comeText = (TextView) alertDialogView
					.findViewById(R.id.title);
			SpannableString spanString = new SpannableString(comeText.getText());
			spanString.setSpan(new StyleSpan(
					android.graphics.Typeface.BOLD_ITALIC), 0, spanString
					.length(), SpannableString.SPAN_EXCLUSIVE_INCLUSIVE);
			comeText.setTypeface(Typeface.MONOSPACE, Typeface.ITALIC);
			okButton.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					dialog.dismiss();
					mLoadDialog = new AlertDialog.Builder(MainActivity.this)
							.create();
					mLoadDialog.show();
					// 注意此处要放在show之后 否则会报异常
					mLoadDialog
							.setContentView(R.layout.loading_process_dialog_anim);
					Message message = new Message();
					message.what = 0;
					mHandler.sendMessage(message);
				}
			});
			cancelButton.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					dialog.dismiss();
				}
			});
			dialog.show();
			WindowManager windowManager = getWindowManager();
			Display display = windowManager.getDefaultDisplay();
			WindowManager.LayoutParams lp = dialog.getWindow().getAttributes();
			lp.width = (int) (display.getWidth()); // 设置宽度
			dialog.getWindow().setAttributes(lp);

			// mLoadDialog = new
			// AlertDialog.Builder(MainActivity.this).create();
			// mLoadDialog.show();
			// // 注意此处要放在show之后 否则会报异常
			// mLoadDialog.setContentView(R.layout.loading_process_dialog_anim);
			// Message message=new Message();
			// message.what=0;
			// mHandler.sendMessage(message);

			return;
		} else {
			ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
			boolean isServiceRunning = false;
			for (RunningServiceInfo service : manager
					.getRunningServices(Integer.MAX_VALUE)) {
				if (GGConstant.INTENT_RECOG_SERVICE.equals(service.service
						.getClassName()))

				{
					isServiceRunning = true;
					break;
				}
			}
			if (!isServiceRunning) {
				recogIntent.putExtra("playing", true);
				startService(recogIntent);
			}

		}

		// task = new TimerTask() {
		// @Override
		// public void run() {
		// // TODO Auto-generated method stub
		// Message message = new Message();
		// message.what = 1;
		// handler.sendMessage(message);
		// }
		// };
		//
		// timer.schedule(task, 10000, 5000);

	}

	private void initComps() {
		statusTV = (TextView) findViewById(R.id.status);
	}

	private void initLayout() {
		findViewById(R.id.isr_recognize).setOnClickListener(this);
		findViewById(R.id.isr_recognize).setEnabled(false);

		findViewById(R.id.isr_build_grammar).setOnClickListener(this);
		findViewById(R.id.isr_stop).setOnClickListener(this);
		findViewById(R.id.isr_cancel).setOnClickListener(this);

		Button btnPlay = (Button) findViewById(R.id.btnPlay);
		btnStop = (Button) findViewById(R.id.music_stop);
		btnPlay.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				soundIntent.putExtra("playing", true);
				startService(soundIntent);
			}
		});
		btnStop.setEnabled(false);
		btnStop.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// Intent intent = new
				// Intent(MainActivity.this,SoundService.class);
				soundIntent.putExtra("playing", false);
				stopService(soundIntent);

				recogIntent.putExtra("playing", true);
				startService(recogIntent);
			}
		});

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public void onClick(View view) {
		switch (view.getId()) {
		case R.id.isr_build_grammar:
			// buildGrammar();
			break;
		case R.id.isr_recognize:
			// startRecognize();
			break;
		case R.id.isr_stop:
			// mRecognizer.stopListening(mRecognizerListener);
			break;
		case R.id.isr_cancel:
			// mRecognizer.cancel(mRecognizerListener);
			break;
		}

	}

	private class DataReceiver extends BroadcastReceiver {// 继承自BroadcastReceiver的子类
		@Override
		public void onReceive(Context context, Intent intent) {// 重写onReceive方法
			String data = intent.getStringExtra("data");
			String[] ds = data.split("##");
			if ("1".equals(ds[0])) {
				btnStop.setEnabled(true);
			} else {
				if (btnStop.isEnabled()) {
					btnStop.setEnabled(false);
				}
			}
			statusTV.setText(ds[1]);
		}
	}

	@Override
	protected void onStart() {// 重写onStart方法
		dataReceiver = new DataReceiver();
		IntentFilter filter = new IntentFilter();// 创建IntentFilter对象
		filter.addAction("wyf.wpf.Sample_3_6");
		registerReceiver(dataReceiver, filter);// 注册Broadcast Receiver
		super.onStart();
	}

	@Override
	protected void onStop() {// 重写onStop方法
		unregisterReceiver(dataReceiver);// 取消注册Broadcast Receiver
		super.onStop();
	}

	class Myhandler extends Handler {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 0:
				String url = SpeechUtility.getUtility(MainActivity.this)
						.getComponentUrl();
				String assetsApk = "SpeechService.apk";
				if (processInstall(MainActivity.this, url, assetsApk)) {
					Message message = new Message();
					message.what = 1;
					mHandler.sendMessage(message);
				}
				break;
			case 1:
				if (mLoadDialog != null) {
					mLoadDialog.dismiss();
				}
				break;
			default:
				break;
			}
			super.handleMessage(msg);
		}

	}

	// 判断手机中是否安装了讯飞语音+
	private boolean checkSpeechServiceInstall() {
		String packageName = "com.iflytek.speechcloud";
		List<PackageInfo> packages = getPackageManager()
				.getInstalledPackages(0);
		for (int i = 0; i < packages.size(); i++) {
			PackageInfo packageInfo = packages.get(i);
			if (packageInfo.packageName.equals(packageName)) {
				return true;
			} else {
				continue;
			}
		}
		return false;
	}

	/**
	 * 如果服务组件没有安装，有两种安装方式。 1.直接打开语音服务组件下载页面，进行下载后安装。
	 * 2.把服务组件apk安装包放在assets中，为了避免被编译压缩，修改后缀名为mp3，然后copy到SDcard中进行安装。
	 */
	private boolean processInstall(Context context, String url, String assetsApk) {
		// 直接下载方式
		// ApkInstaller.openDownloadWeb(context, url);
		// 本地安装方式
		if (!ApkInstaller.installFromAssets(context, assetsApk)) {
			Toast.makeText(MainActivity.this, "安装失败", Toast.LENGTH_SHORT)
					.show();
			return false;
		}
		return true;
	}

}
