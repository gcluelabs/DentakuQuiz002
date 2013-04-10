package com.example.dentaku;

import java.util.ArrayList;
import java.util.Random;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

public class Dentaku extends Activity implements View.OnClickListener {

	/**
	 * Buttonの配列
	 */
	Button mButton[];

	/**
	 * Idの配列
	 */
	int mId[] = { R.id.button0, R.id.button1, R.id.button2, R.id.button3,
			R.id.button4, R.id.button5, R.id.button6, R.id.button7,
			R.id.button8, R.id.button9, R.id.buttonPlus, R.id.buttonMinus,
			R.id.buttonEqual, R.id.buttonTen, R.id.buttonClear,
			R.id.buttonStart };

	/**
	 * キー
	 */
	private final int KEY_PLUS = 10;
	private final int KEY_MINUS = 11;
	private final int KEY_EQUAL = 12;
	private final int KEY_TEN = 13;
	private final int KEY_CLEAR = 14;
	private final int KEY_START = 15;
	/**
	 * TextView
	 */
	private TextView mTextView;
	private TextView mAnswer;
	private TextView mTimer;

	/**
	 * 答えのリスト
	 */
	private String[] answers = new String[4];
	/**
	 * 前の処理
	 */
	private int beforeStatus = 0;

	/**
	 * 計算中の値
	 */
	private ArrayList<String> calcArray;
	/**
	 * 計算する時の配列
	 */
	private ArrayList<String> signArray;
	/**
	 * 正解数
	 */
	private int successCount = 0;
	/**
	 * 回答時間を計測するタスククラス
	 */
	private AnswerTimerTask task;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.main);

		// 表示用TextView
		mTextView = (TextView) findViewById(R.id.display);
		mAnswer = (TextView) findViewById(R.id.answer);
		mTimer = (TextView) findViewById(R.id.timer);
		// Button
		mButton = new Button[mId.length];

		// Buttonの取り込みとイベントのはりつけ
		for (int i = 0; i < mId.length; i++) {
			// buttonを取り込む
			mButton[i] = (Button) findViewById(mId[i]);
			// buttonのイベント処理
			mButton[i].setOnClickListener(this);
		}
		calcArray = new ArrayList<String>();
		signArray = new ArrayList<String>();
	}

	@Override
	public void onClick(View view) {

		// 押されたボタンがどのボタンかを判定
		for (int i = 0; i < mId.length; i++) {
			if (view.equals(mButton[i])) {
				String nowValue = mTextView.getText().toString();
				// CLEAR
				if (i == KEY_CLEAR) {
					clear();
					beforeStatus = KEY_CLEAR;
				}
				// =
				else if (i == KEY_EQUAL && nowValue.length() > 0) {
					nowValue = checkDisplay(nowValue);
					calcArray.add(nowValue);
					double ans = calc();
					// テキストの文字を隠す
					mTextView.setTextColor(Color.parseColor("#999999"));
					showAlert(ans);
					mTextView.setText(Double.toString(ans));
					calcArray.clear();
					signArray.clear();
					beforeStatus = i;
				}

				// +
				else if (i == KEY_PLUS && nowValue.length() > 0) {
					calcArray.add(nowValue);
					signArray.add("+");
					beforeStatus = KEY_PLUS;
				}
				// -
				else if (i == KEY_MINUS && nowValue.length() > 0) {
					calcArray.add(nowValue);
					signArray.add("-");
					beforeStatus = KEY_MINUS;
				}
				// .
				else if (i == KEY_TEN) {
					// .キーを押した時、演算キーが押されていた場合
					nowValue = checkDisplay(nowValue);
					// いきなり.キーが押された場合
					if (nowValue.length() == 0) {
						nowValue = "0.";
					} else {
						nowValue = nowValue + ".";
					}
					mTextView.setText(nowValue);
					beforeStatus = i;
				}
				// スタートボタン
				else if (i == KEY_START) {
					successCount = 0;
					mButton[KEY_START].setVisibility(View.GONE);
					task = new AnswerTimerTask();
					task.execute(60); // 1分間カウントする
				}
				// 数字
				else if (i < 10) {
					nowValue = checkDisplay(nowValue);
					// 0しか入力されていない場合は0が２個以上並ばないようにする
					if (nowValue.equals("0") && i == 0) {
						return;
					} else if (nowValue.equals("0") && i != 0) {
						nowValue = "";
					}

					nowValue = nowValue + i;
					mTextView.setText(nowValue);
					beforeStatus = i;
				}
				break;
			}
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		if (task != null) {
			task.cancel(true);
		}
	}

	// ディスプレイをクリアする
	private void clear() {
		mTextView.setText("");
		calcArray.clear();
		signArray.clear();
	}

	// 演算が行われていた場合を確認する
	// 遅れた状態でディスプレイの値を初期化する
	private String checkDisplay(String now) {
		if (beforeStatus == KEY_PLUS || beforeStatus == KEY_MINUS
				|| beforeStatus == KEY_EQUAL) {
			mTextView.setText("");
			return "0";
		}
		return now;
	}

	// 計算する
	private double calc() {
		if (calcArray.size() == 0) {
			return 0.0;
		}
		if (calcArray.size() == 1) {
			return Double.parseDouble(calcArray.get(0));
		}
		double passive = Double.parseDouble(calcArray.get(0));
		double active;
		int j = 0;
		for (int i = 1; i < calcArray.size(); i++) {
			active = Double.parseDouble(calcArray.get(i));
			if (signArray.get(j).equals("+")) {
				passive += active;
			} else {
				passive -= active;
			}
			j++;
		}
		return passive;
	}

	/**
	 * 選択のダイアログを表示する
	 */
	private void showAlert(double ans) {
		// 選択肢を生成する
		Random random = new Random();
		for (int i = 0; i < answers.length; i++) {
			answers[i] = "";
		}
		// 正解をどこにもってくるか
		int success = (int) Math.abs(random.nextInt(4));
		for (int i = 0; i < answers.length; i++) {
			if (i == success) {
				answers[i] = Double.toString(ans);
				// 間違った答えの中に同じものがないかどうか
				for (int j = 0; j < answers.length; j++) {
					if (i != j && !answers[j].equals("")
							&& answers[i].equals(answers[j])) {
						answers[i] = Double.toString(ans + 3);
					}
				}
			} else {
				double wrong;
				boolean isWrong;
				do {
					isWrong = false;
					wrong = ans + random.nextInt(10);
					answers[i] = Double.toString(wrong);
					// 間違った答えの中に同じものがないかどうか
					for (int j = 0; j < answers.length; j++) {
						if (i != j && !answers[j].equals("")
								&& answers[i].equals(answers[j])) {
							isWrong = true;
						}
					}
				} while (isWrong);
				// 正しい答えと同じものを生成しないためのループ
			}
		}

		MyDialogListener listener = new MyDialogListener();
		AlertDialog.Builder myDialogBuilder = new AlertDialog.Builder(this);

		myDialogBuilder.setTitle("答えは？").setItems(answers, listener)
				.setCancelable(false);

		AlertDialog myAlertDialog = myDialogBuilder.create();
		myAlertDialog.show();
	}

	// 回答一覧を管理するリスナー
	private class MyDialogListener implements OnClickListener {

		public void onClick(DialogInterface dialog, int which) {
			// 回答判定
			if (mTextView.getText().toString().equals(answers[which])) {
				mAnswer.setText("○");
				mAnswer.setTextColor(Color.RED);
				successCount++;
			} else {
				mAnswer.setText("×");
				mAnswer.setTextColor(Color.BLUE);
			}
			// 正解を表示
			mTextView.setTextColor(Color.parseColor("#F39800"));

		}
	}

	// 回答時間をカウントするクラス
	private class AnswerTimerTask extends AsyncTask<Integer, Integer, Void> {

		/**
		 * バックグランドで行う処理
		 */
		@Override
		public Void doInBackground(Integer... sec) {
			int s = sec[0].intValue();
			try {
				for (int i = s; i >= 0; i--) {
					Thread.sleep(1000); // 1秒スリープ
					publishProgress(i);
				}
			} catch (Exception e) {

			}
			return null;
		}

		/**
		 * 進捗状況を表示する
		 */
		@Override
		public void onProgressUpdate(Integer... progress) {
			mTimer.setText(progress[0].toString());
		}

		/**
		 * バックグランド処理が完了し、UIスレッドに反映する
		 */
		@Override
		public void onPostExecute(Void result) {
			showDialog(successCount + "点です。");
		}

		/**
		 * ダイアログで文字列を表示する。
		 * 
		 * @param message
		 *            表示したい文字列
		 */
		private void showDialog(String message) {
			new AlertDialog.Builder(Dentaku.this)
					.setTitle("あなたの成績は")
					.setMessage(message)
					.setNeutralButton("OK",
							new DialogInterface.OnClickListener() {
								// この中に"YES"時の処理をいれる
								public void onClick(DialogInterface dialog,
										int whichButton) {
									// 表示などの初期化
									clear();
									successCount = 0;
									mTimer.setText("0");
									mAnswer.setText("");
									mButton[KEY_START]
											.setVisibility(View.VISIBLE);
								}
							}).show();
		}
	}
}