package com.example.gg1_0;

import java.io.InputStream;
import java.util.Timer;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.text.TextUtils;
import android.util.Log;

import com.iflytek.speech.ErrorCode;
import com.iflytek.speech.GrammarListener;
import com.iflytek.speech.ISpeechModule;
import com.iflytek.speech.InitListener;
import com.iflytek.speech.RecognizerListener;
import com.iflytek.speech.RecognizerResult;
import com.iflytek.speech.SpeechConstant;
import com.iflytek.speech.SpeechRecognizer;

public class RecogService extends Service {
	// ����ʶ�����
	private SpeechRecognizer mRecognizer;
	private String mLocalGrammar = null;
	private static final String GRAMMAR_TYPE = "bnf";
	// ����
	private SharedPreferences mSharedPreferences;
	private static final String KEY_GRAMMAR_ABNF_ID = "grammar_abnf_id";
	private String mEngineType = "local";
	private String TAG = "TAG";

	Handler handler;

	Intent soundIntent = new Intent("com.example.gg1_0.SoundService");

	String recogState = "";
	public static String REC_STATE_BEGIN = "0";
	public static String REC_STATE_VOLUMNCHANGED = "1";
	public static String REC_STATE_END = "2";

	int buildFlag = -1;

	private Timer toasttimer;

	String toastStr = "";

	CommandReceiver cmdReceiver;
	boolean flag;

	int curVolumn = -1;

	@Override
	public void onCreate() {
		super.onCreate();

		// ��ʼ���������
		mSharedPreferences = getSharedPreferences(getPackageName(),
				MODE_PRIVATE);

		handler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				// TODO Auto-generated method stub

				startRecognize();
				super.handleMessage(msg);
			}
		};

		IntentFilter filter = new IntentFilter();// ����IntentFilter����
		filter.addAction("wyf.wpf.MyService");
		registerReceiver(cmdReceiver, filter);// ע��Broadcast Receiver
		doJob();// ���÷��������߳�

		IntentFilter filterTick = new IntentFilter(Intent.ACTION_TIME_TICK);

		MyBroadcastReceiver receiverTick = new MyBroadcastReceiver();
		registerReceiver(receiverTick, filterTick);

		// Ϊ��ǰ�̻߳��looper
		// handler = new Handler(Looper.getMainLooper());
		// ��ǰTask���̵߳�����Ϊmyservice
		// toasttimer = new Timer("myservice");
		// toasttimer.scheduleAtFixedRate(toasttask, 10, 10);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		toasttimer.cancel();
		this.unregisterReceiver(cmdReceiver);// ȡ��ע���CommandReceiver
		stopSelf();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// boolean playing = intent.getBooleanExtra("playing", true);
		// if (playing) {
		// } else {
		// Message message = new Message();
		// message.what = 1;
		// handler.sendMessage(message);
		// }
		if (mRecognizer == null) {
			// ��ʼ��ʶ�����
			mRecognizer = new SpeechRecognizer(this, mInitListener);
		} else {
			startRecognize();
		}
		return super.onStartCommand(intent, flags, startId);
	}

	// ������
	public void doJob() {
		new Thread() {
			public void run() {
				while (true) {
					try {// ˯��һ��ʱ��
						Thread.sleep(100);
					} catch (Exception e) {
						e.printStackTrace();
					}
					Intent intent = new Intent();// ����Intent����
					intent.setAction("wyf.wpf.Sample_3_6");
					String data = "";
					synchronized (RecogService.class) {
						if (curVolumn == -1) {
							data = "0##��ʼ��...";
						} else if (curVolumn == -2) {
							data = "1##������...";
						} else if (curVolumn == -3) {
							data = "2##˼����...";
						} else {
							data = "3##������...����" + curVolumn;
						}
					}
					intent.putExtra("data", "" + data);
					sendBroadcast(intent);// ���͹㲥
				}
			}

		}.start();
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	/**
	 * ���ڻ���������
	 */
	private InitListener mInitListener = new InitListener() {

		@Override
		public void onInit(ISpeechModule arg0, int code) {
			Log.d("TAG", "SpeechRecognizer init() code = " + code);
			if (code == ErrorCode.SUCCESS) {
				// findViewById(R.id.isr_recognize).setEnabled(true);

				int ret = buildGrammar();
				if (ret != ErrorCode.SUCCESS) {
					showTip("�﷨����ʧ�ܣ�" + ret);
				} else {
					startRecognize();
				}
			}
		}
	};

	public int buildGrammar() {
		// ȡ���﷨����
		String[] mGrammars = new String[0];
		for (String grammar : mGrammars) {
			Log.d("", grammar);
		}

		mLocalGrammar = readFile("call.bnf", "utf-8");
		String grammarContent = new String(mLocalGrammar);
		mRecognizer.setParameter(SpeechRecognizer.GRAMMAR_ENCODEING, "utf-8");

		mRecognizer.setParameter(SpeechConstant.ENGINE_TYPE, mEngineType);

		int ret = mRecognizer.buildGrammar(GRAMMAR_TYPE, grammarContent,
				grammarListener);
		return ret;
	}

	public void startRecognize() {
		mRecognizer.setParameter(SpeechConstant.ENGINE_TYPE, mEngineType);

		int bos = 3600 * 1000 * 60;
		mRecognizer.setParameter(SpeechConstant.VAD_BOS, String.valueOf(bos));
		mRecognizer.setParameter(SpeechConstant.VAD_EOS, "700");

		mRecognizer.setParameter(SpeechConstant.PARAMS,
				"local_grammar=call,mixed_threshold=40");
		int recode = mRecognizer.startListening(mRecognizerListener);
		if (recode != ErrorCode.SUCCESS)
			showTip("�﷨ʶ��ʧ�ܣ�" + recode);
	}

	private GrammarListener grammarListener = new GrammarListener.Stub() {

		@Override
		public void onBuildFinish(String grammarId, int errorCode)
				throws RemoteException {
			if (errorCode == ErrorCode.SUCCESS) {
				String grammarID = new String(grammarId);
				Editor editor = mSharedPreferences.edit();
				if (!TextUtils.isEmpty(grammarId))
					editor.putString(KEY_GRAMMAR_ABNF_ID, grammarID);
				editor.commit();
				showTip("�﷨�����ɹ���" + grammarId);
			} else {
				showTip("�﷨����ʧ�ܣ������룺" + errorCode);
			}
		}
	};

	/**
	 * ʶ��ص���
	 */
	private RecognizerListener mRecognizerListener = new RecognizerListener.Stub() {

		@Override
		public void onVolumeChanged(int v) throws RemoteException {
			showTip("onVolumeChanged��" + v);
			// threads.add(Thread.currentThread().getId());
			// Log.i(TAG, "ThreadsSize:" + threads.size());
			synchronized (RecogService.class) {
				curVolumn = v;
			}
		}

		@Override
		public void onResult(final RecognizerResult result, boolean isLast)
				throws RemoteException {
			if (null != result && isLast) {
				Log.i(TAG, "recognizer result��" + result.getResultString());
				// String text =
				// XmlParser.parseNluResult(result.getResultString());
				// ��ʾ

				synchronized (RecogService.class) {
					soundIntent.putExtra("playing", true);
					startService(soundIntent);
					curVolumn = -2;
				}

			} else {
				Log.d(TAG, "recognizer result : null");
			}
			// mUiHandler.sendMessageDelayed(new Message(), 2000);

		}

		@Override
		public void onError(int errorCode) throws RemoteException {
			showTip("onError Code��" + errorCode);
			// mUiHandler.sendMessageDelayed(new Message(), 2000);
			// Message message = new Message();
			// message.what = 1;
			// handler.sendMessage(message);
			synchronized (RecogService.class) {
				curVolumn = -3;
				handler.sendMessageDelayed(new Message(), 100);
			}
		}

		@Override
		public void onEndOfSpeech() throws RemoteException {
			recogState = REC_STATE_END;
			showTip("onEndOfSpeech");
		}

		@Override
		public void onBeginOfSpeech() throws RemoteException {
			recogState = REC_STATE_BEGIN;
			showTip("onBeginOfSpeech");
		}
	};

	private void showTip(final String str) {

		Log.d("mark", str);
	}

	/**
	 * ��ȡ�﷨�ļ���
	 * 
	 * @return
	 */
	private String readFile(String file, String code) {
		int len = 0;
		byte[] buf = null;
		String grammar = "";
		try {
			InputStream in = getAssets().open(file);
			len = in.available();
			buf = new byte[len];
			in.read(buf, 0, len);

			grammar = new String(buf, code);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return grammar;
	}

	private class CommandReceiver extends BroadcastReceiver {// �̳���BroadcastReceiver������
		@Override
		public void onReceive(Context context, Intent intent) {// ��дonReceive����
		// int cmd = intent.getIntExtra("cmd", -1);//��ȡExtra��Ϣ
		// if(cmd == Sample_3_6.CMD_STOP_SERVICE){//�����������Ϣ��ֹͣ����
		// flag = false;//ֹͣ�߳�
		// stopSelf();//ֹͣ����
		// }
		}
	}
}