package com.camangi.android.TGMDialer;

import java.io.IOException;
import java.io.InvalidObjectException;
import java.net.URI;
import java.util.Calendar;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import com.android.internal.telephony.Phone;
import com.android.internal.telephony.TelephonyProperties;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.app.AlertDialog.Builder;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.ConnectivityManager;
import android.net.LocalSocket;
import android.net.LocalSocketAddress;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.NetworkInfo.State;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.telephony.ServiceState;
import android.telephony.TelephonyManager;
import android.text.Layout;
import android.text.TextUtils;
import android.text.method.DateTimeKeyListener;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import com.android.internal.telephony.ITelephonyRegistry;
import com.android.internal.telephony.gsm.GSMPhone;

public class Dialer extends Activity {

	/** Called when the activity is first created. */
	private ITelephonyRegistry mRegistry;

	// this also make ServiceState singleton
	private static ServiceState ss;

	private boolean isActivityValid;

	private TGM tgm;

	static {

		if (ss == null) {

			ss = new ServiceState();

		}
	}

	// private ProgressDialog pd;

	private ProgressDialog dialPd;

	private ProgressDialog enterPinPd;

	private ProgressDialog disconnectPd;

	private static final String LOG_TAG = "DIALER";

	// PAGE ID

	private static final int PLUG_IN_DONGLE_INFO_PAGE = 0;

	private static final int DIALER_PAGE = 1;

	private static final int CONNECTION_INFO_PAGE = 2;

	private static final int ENTER_PIN_PAGE = 3;

	private static final int ENTER_PUK_PAGE = 4;

	private static final int SIM_FAILURE_PAGE = 5;

	private static final int ABOUT_US_PAGE = 6;

	private static final int AIR_PLANE_MODE_PAGE = 7;

	// PREFERENCE KEY

	public static final String DIALER_PREFERENCE = "DialerPreference";

	public static final String APN_PREFERENCE_KEY = "ApnPreference";

	public static final String PPPD_AUTH_USERNAME_PREFERENCE_KEY = "PPPDAuthUsernamePreference";

	public static final String PPPD_AUTH_PASSWORD_PREFERENCE_KEY = "PPPDAuthPasswordPreference";

	public static final String LAST_CARRIER_CODE_PREFERENCE_KEY = "LastCarrierCodePreference";

	private View plugInDongleInfoPage;

	private View dialerPage;

	private View connectionInfoPage;

	private View enterPINPage;

	private View enterPUKPage;

	private View simFailuePage;

	private View aboutUsPage;

	private View airPlaneModePage;

	// DIALOG ID

	private static final int CHANGE_APN_DIALOG = 1;

	private static final int ENTER_PIN_DIALOG = 2;

	private static final int ENTER_PUK_DIALOG = 3;

	private static final int ENTER_PPPD_AUTH_DIALOG = 4;

	private static final int DIAL_FAILED_DIALOG = 5;

	// OPTION MENU

	private static final int MENU_CHANGE_APN_OPTION = 1;

	private static final int MENU_ABOUT_OPTION = 2;

	private static final int MENU_SET_PPPD_AUTH_OPTION = 3;

	private static final int MENU_FAQ_OPTION = 4;

	// DIALOG REFERENCE

	private Dialog changeApnDialog;

	private Dialog enterPUKDialog;

	private Dialog enterPINDialog;

	private Dialog dialFailedDialog;

	private TextView carrierText;

	private TextView dongleModelText;

	private TextView apnSettingText;

	private TextView connectionInfoCarrierText;

	private TextView connectionInfoDongleModelText;

	private TextView connectionTimeText;

	// private Button dialBtn;

	private ImageButton dialBtn;

	private ImageButton disconnectBtn;

	private Button leaveAboutUsBtn;

	// private Button disconnectBtn;

	// private String[] dongleModel = { "", "" };

	// private String imsi; //International Mobile Service XXXX //MCC + MNC

	// private String imei; //

	// private String apn;

	// private int pinCountRemain = 3;

	// private int currentRadioState = TGMConstants.TGM_STATE_OFF;

	private static final int SET_PDP_CONTEXT_DONE = 1;
	private static final int SET_PIN_CODE_DONE = 2;
	private static final int DIAL_DATA_NETWORK_DONE = 3;
	private static final int DISCONNECT_DATA_NETWORK_DONE = 4;
	private static final int QUERY_SIGNAL_STRENGTH_DONE = 5;
	private static final int QUERY_IMEI_DONE = 6;
	private static final int QUERY_MANUFACTURER_DONE = 7;
	private static final int QUERY_OPERATOR_DONE = 8;
	private static final int QUERY_PDP_CONTEXT_LIST_DONE = 9;
	private static final int QUERY_MODEM_MODEL_DONE = 10;
	private static final int QUERY_SIM_STATUS_DONE = 11;
	private static final int QUERY_IMSI_DONE = 12;
	private static final int QUERY_NUMBER_DONE = 13;
	private static final int QUERY_STATE_DONE = 14;
	private static final int RESET_STACK_DONE = 15;
	private static final int SET_PPPD_AUTH_DONE = 16;

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		mRegistry = ITelephonyRegistry.Stub.asInterface(ServiceManager
				.getService("telephony.registry"));

		setContentView(R.layout.main);

		plugInDongleInfoPage = (View) findViewById(R.id.plugInDongleInfoPage);

		dialerPage = (View) findViewById(R.id.dialerPage);

		connectionInfoPage = (View) findViewById(R.id.connectionInfoPage);

		simFailuePage = (View) findViewById(R.id.simFailurePage);

		enterPINPage = (View) findViewById(R.id.enterPINPage);

		enterPUKPage = (View) findViewById(R.id.enterPUKPage);

		aboutUsPage = (View) findViewById(R.id.aboutUsPage);

		airPlaneModePage = (View) findViewById(R.id.airPlaneModePage);

		carrierText = (TextView) findViewById(R.id.carrierText);

		connectionInfoCarrierText = (TextView) findViewById(R.id.connectionInfoCarrierText);

		dongleModelText = (TextView) findViewById(R.id.dongleModelText);

		connectionInfoDongleModelText = (TextView) findViewById(R.id.connectionInfoDongleModelText);

		connectionTimeText = (TextView) findViewById(R.id.connectionTimeText);

		apnSettingText = (TextView) findViewById(R.id.apnSettingText);

		dialBtn = (ImageButton) findViewById(R.id.dialBtn);
		// dialBtn = (Button) findViewById(R.id.dialBtn);
		dialBtn.setOnClickListener(dialBtnClickListener);

		disconnectBtn = (ImageButton) findViewById(R.id.disconnectBtn);
		disconnectBtn.setOnClickListener(disconnectBtnClickListener);

		leaveAboutUsBtn = (Button) findViewById(R.id.leaveAboutUsBtn);
		leaveAboutUsBtn.setOnClickListener(leaveAboutUsBtnClickListener);

		// Setting up TGM onCreate will only called once
		tgm = TGM.getInstance(this);

		tgm.setTGMStateChangedListener(tgmStateListener);

		IntentFilter filter = new IntentFilter(
				Intent.ACTION_AIRPLANE_MODE_CHANGED);

		filter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);

		// IntentFilter.create(Intent.ACTION_AIRPLANE_MODE_CHANGED, null);

		registerReceiver(new BroadcastReceiver() {

			@Override
			public void onReceive(Context context, final Intent intent) {

				String action = intent.getAction();
				// Intent.ACTION_AIRPLANE_MODE_CHANGED;
				if (action.equals(Intent.ACTION_AIRPLANE_MODE_CHANGED)) {

					final boolean enabled = intent.getBooleanExtra("state",
							false);

					if (enabled) {

						if (TGM.dataStore.currentRadioState == TGMConstants.TGM_STATE_PPPD_READY) {

							tgm.disconnectDataNetowrk(handler
											.obtainMessage(DISCONNECT_DATA_NETWORK_DONE));

							Toast.makeText(
											context,
											"3G connection is turned off due to air plane mode",
											5000).show();
						}

						ss.setStateOff();

						notifyServiceStateChanges();

					} else {

						// Just Query State and it will do the rest
						tgm.getState(handler.obtainMessage(QUERY_STATE_DONE));

					}

				} else if (action.equals(WifiManager.WIFI_STATE_CHANGED_ACTION)) {

					WifiManager wifi = (WifiManager) context
							.getSystemService(WIFI_SERVICE);

					if (wifi.isWifiEnabled()) {

						if (TGM.dataStore.currentRadioState == TGMConstants.TGM_STATE_PPPD_READY) {

							tgm.disconnectDataNetowrk(handler
											.obtainMessage(DISCONNECT_DATA_NETWORK_DONE));

							Toast.makeText(context, "3G connection is turned off due to wifi connection",
									5000).show();
						}
					}

				}

			}

		}, filter);
	}

	@Override
	protected void onPostResume() {

		super.onPostResume();

		// Load Configuration here
		// SharedPreferences pref = getSharedPreferences(DIALER_PREFERENCE, 0);

		// TGM.dataStore.apn = pref.getString(APN_PREFERENCE_KEY, "internet");

		// apnSettingText.setText("Apn Setting: " + TGM.dataStore.apn);

		if (getAirPlaneModeOn() > 0) {

			switchView(AIR_PLANE_MODE_PAGE);

		} else {

			tgm.getState(handler.obtainMessage(QUERY_STATE_DONE));

		}

	}

	@Override
	protected void onRestart() {

		super.onRestart();

	}

	@Override
	protected void onResume() {

		super.onResume();

		loadPersistentData();

		isActivityValid = true;

		String connectionTimeLabelText = getResources().getString(
				R.string.tgm_connection_time_label);

		if (TGM.dataStore.connectionDate != null) {

			long diff = new Date().getTime()
					- TGM.dataStore.connectionDate.getTime();

			int hr = (int) diff / (1000 * 60 * 60);

			int min = (int) diff / (60 * 1000) % 60;

			int sec = ((int) diff / (1000)) % 60;

			String time = ((hr <= 0) ? "" : hr + " Hours : ")
					+ ((min <= 0) ? "" : min + " Minutes : ") + sec
					+ " Seconds";

			connectionTimeText.setText(connectionTimeLabelText + " " + time);
		}

	}

	@Override
	protected void onPause() {

		super.onPause();

		savePersistentData();

		isActivityValid = false;
	}

	@Override
	protected void onStart() {

		super.onStart();
	}

	@Override
	protected void onStop() {

		super.onStop();
	}

	@Override
	protected Dialog onCreateDialog(int id) {

		AlertDialog.Builder dialogBuilder;

		View layout;

		switch (id) {

		case ENTER_PIN_DIALOG:

			dialogBuilder = new Builder(Dialer.this);

			dialogBuilder.setTitle(getResources().getText(
					R.string.tgm_enter_pin_dialog_title));

			layout = getLayoutInflater().inflate(R.layout.pin_dialog, null);

			dialogBuilder.setView(layout);

			dialogBuilder.setPositiveButton(
					R.string.tgm_enter_pin_dialog_apply_btn_text,
					enterPINPositiveBtnClickHandler);

			return enterPINDialog = dialogBuilder.create();

		case ENTER_PUK_DIALOG:

			dialogBuilder = new Builder(Dialer.this);

			dialogBuilder.setTitle(getResources().getText(
					R.string.tgm_enter_puk_dialog_title));

			layout = getLayoutInflater().inflate(R.layout.puk_dialog, null);

			dialogBuilder.setView(layout);

			dialogBuilder.setPositiveButton(
					R.string.tgm_enter_puk_dialog_apply_btn_text,
					enterPUKPositiveBtnClickHandler);

			return enterPUKDialog = dialogBuilder.create();

		case CHANGE_APN_DIALOG:

			dialogBuilder = new Builder(Dialer.this);

			dialogBuilder.setTitle(getResources().getText(
					R.string.tgm_change_apn_dialog_title));

			layout = getLayoutInflater().inflate(R.layout.apn_dialog, null);

			dialogBuilder.setView(layout);

			dialogBuilder.setPositiveButton(
					R.string.tgm_change_apn_dialog_change_btn_text,
					changeApnPositiveBtnClickHandler);

			dialogBuilder.setNegativeButton(
					R.string.tgm_change_apn_dialog_cancel_btn_text,
					changeApnNegativeBtnClickHandler);

			return changeApnDialog = dialogBuilder.create();

			// case ENTER_PPPD_AUTH_DIALOG:
			//			
			// dialogBuilder=new Builder(Dialer.this);
			//			
			// dialogBuilder.setTitle(getResources().getText(R.string.tgm_set_pppd_auth_dialog_title_text));
			//			
			// layout = getLayoutInflater().inflate(R.layout.set_auth_dialog,
			// null);
			//			
			// dialogBuilder.setView(layout);
			//			
			// dialogBuilder.setPositiveButton(R.string.tgm_change_apn_dialog_change_btn_text,
			// changeAuthenticationPositiveBtnClickHandler);
			//			
			// dialogBuilder.setNegativeButton(R.string.tgm_change_apn_dialog_cancel_btn_text,
			// changeAuthenticationNegativeBtnClickHandler);
			//			
			// return changeApnDialog = dialogBuilder.create();

		case DIAL_FAILED_DIALOG:

			dialogBuilder = new Builder(Dialer.this);

			dialogBuilder.setTitle(getResources().getText(
					R.string.tgm_dial_failed_dialog_title));

			layout = getLayoutInflater().inflate(R.layout.dial_failed_dialog,
					null);

			dialogBuilder.setView(layout);

			dialogBuilder.setPositiveButton(
					R.string.tgm_dial_failed_ok_btn_text, null);

			return dialFailedDialog = dialogBuilder.create();

		default:

			return null;

		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		super.onCreateOptionsMenu(menu);

		menu.add(0, MENU_CHANGE_APN_OPTION, 0, getResources().getText(
				R.string.tgm_change_apn_option_menu_text));

		menu.add(0, MENU_FAQ_OPTION, 0, getResources().getText(
				R.string.tgm_faq_option_menu_text));

		menu.add(0, MENU_ABOUT_OPTION, 0, getResources().getText(
				R.string.tgm_about_option_menu_text));

		// menu.add(0, MENU_SET_PPPD_AUTH_OPTION, 0 ,
		// getResources().getText(R.string.tgm_set_pppd_auth_option_menu_text));

		return true;

	}

	@Override
	protected void onPrepareDialog(int id, Dialog dialog) {

		switch (id) {
		case CHANGE_APN_DIALOG:

			EditText editText;

			SharedPreferences pref = getSharedPreferences(DIALER_PREFERENCE, 0);

			// Retrieve Apn from preference
			editText = (EditText) dialog.findViewById(R.id.apnEditText);

			TGM.dataStore.apn = pref.getString(APN_PREFERENCE_KEY, "internet");

			if (!TextUtils.isEmpty(TGM.dataStore.apn)) {

				editText.setText(TGM.dataStore.apn);

			}

			// Retrieve Username from preference

			editText = (EditText) dialog
					.findViewById(R.id.pppd_auth_username_edittext);

			TGM.dataStore.pppdUsername = pref.getString(
					PPPD_AUTH_USERNAME_PREFERENCE_KEY, null);

			if (!TextUtils.isEmpty(TGM.dataStore.pppdUsername)) {

				editText.setText(TGM.dataStore.pppdUsername);

			}

			// Retrieve Password from preference

			editText = (EditText) dialog
					.findViewById(R.id.pppd_auth_password_edittext);

			TGM.dataStore.pppdPassword = pref.getString(
					PPPD_AUTH_PASSWORD_PREFERENCE_KEY, null);

			if (!TextUtils.isEmpty(TGM.dataStore.pppdPassword)) {

				editText.setText(TGM.dataStore.pppdPassword);

			}

			break;

		case ENTER_PIN_DIALOG:

			TextView pinMessageTextView = (TextView) dialog
					.findViewById(R.id.pinMessageTextView);

			if (TGM.dataStore.pinCountRemain < 3) {

				pinMessageTextView.setText(getResources().getString(
						R.string.tgm_enter_pin_dialog_incorrect_pin_message)
						+ "\nRemain count: " + TGM.dataStore.pinCountRemain);

			} else if (TGM.dataStore.pinCountRemain == 3) {

				pinMessageTextView.setText(getResources().getString(
						R.string.tgm_enter_pin_dialog_message));

			}

			break;

		}

	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {

		case MENU_CHANGE_APN_OPTION:

			showDialog(CHANGE_APN_DIALOG);

			break;

		case MENU_ABOUT_OPTION:

			switchView(ABOUT_US_PAGE);

			break;

		// case MENU_SET_PPPD_AUTH_OPTION:
		//			
		// showDialog(ENTER_PPPD_AUTH_DIALOG);
		//			
		// //tgm.setPPPDAuth("em", "em",
		// handler.obtainMessage(SET_PPPD_AUTH_DONE));
		//			
		// break;

		case MENU_FAQ_OPTION:

			Intent startFAQIntent = new Intent(Intent.ACTION_VIEW, Uri
					.parse("http://3gwizard.camangi.com/faq"));

			startActivity(startFAQIntent);

			break;
		default:
			break;
		}

		return true;
	}

	private DialogInterface.OnClickListener enterPINPositiveBtnClickHandler = new DialogInterface.OnClickListener() {

		public void onClick(DialogInterface dialog, int which) {

			EditText pinEditText = (EditText) ((AlertDialog) dialog)
					.findViewById(R.id.pinEditText);

			String pin = pinEditText.getText().toString();

			if (TextUtils.isEmpty(pin)) {

				CharSequence title = getResources().getText(
						R.string.tgm_enter_pin_empty_pin_alert_dialog_title);

				CharSequence message = getResources().getText(
						R.string.tgm_enter_pin_empty_pin_alert_dialog_message);

				new AlertDialog.Builder(Dialer.this)
						.setTitle(title)
						.setMessage(message)
						.

						setPositiveButton(
								R.string.tgm_enter_pin_empty_pin_alert_possitive_btn,
								new DialogInterface.OnClickListener() {

									public void onClick(DialogInterface dialog,
											int which) {

										showDialog(ENTER_PIN_DIALOG);

									}

								}).show();

				return;

			}

			tgm.supplyPIN(pin, handler
					.obtainMessage(TGMConstants.TGM_REQUEST_SET_PIN_CODE));

			// Toast.makeText(Dialer.this, "PIN you entered is "+pin,
			// 5000).show();

		}

	};

	private DialogInterface.OnClickListener enterPUKPositiveBtnClickHandler = new DialogInterface.OnClickListener() {

		public void onClick(DialogInterface dialog, int which) {

		}

	};

	private DialogInterface.OnClickListener changeApnPositiveBtnClickHandler = new DialogInterface.OnClickListener() {

		public void onClick(DialogInterface dialog, int which) {

			String value;
			EditText valueEditText;

			SharedPreferences pref = getSharedPreferences(DIALER_PREFERENCE, 0);

			Editor editor = pref.edit();

			valueEditText = (EditText) ((AlertDialog) dialog)
					.findViewById(R.id.apnEditText);

			if (valueEditText != null) {

				value = valueEditText.getText().toString();

				if (!TextUtils.isEmpty(value)) {

					TGM.dataStore.apn = value;

					apnSettingText.setText("Apn Setting: " + TGM.dataStore.apn);

					editor.putString(APN_PREFERENCE_KEY, TGM.dataStore.apn);

				}else{
					
					TGM.dataStore.apn = "";
					editor.putString(APN_PREFERENCE_KEY, "");
				}

			}

			valueEditText = (EditText) ((AlertDialog) dialog)
					.findViewById(R.id.pppd_auth_username_edittext);

			if (valueEditText != null) {

				value = valueEditText.getText().toString();

				if (!TextUtils.isEmpty(value)) {

					TGM.dataStore.pppdUsername = value;

					editor.putString(PPPD_AUTH_USERNAME_PREFERENCE_KEY, value);

				}else{
					
					TGM.dataStore.pppdUsername = "";

					editor.putString(PPPD_AUTH_USERNAME_PREFERENCE_KEY, "");
				}

			}

			valueEditText = (EditText) ((AlertDialog) dialog)
					.findViewById(R.id.pppd_auth_password_edittext);

			if (valueEditText != null) {

				value = valueEditText.getText().toString();

				if (!TextUtils.isEmpty(value)) {

					TGM.dataStore.pppdPassword = value;

					editor.putString(PPPD_AUTH_PASSWORD_PREFERENCE_KEY, value);

				}else{
					
					TGM.dataStore.pppdPassword = "";

					editor.putString(PPPD_AUTH_PASSWORD_PREFERENCE_KEY, "");
				}

			}

			editor.commit();

			tgm.setPDPContext(TGM.dataStore.apn, null, null, handler
					.obtainMessage(SET_PDP_CONTEXT_DONE));

			tgm.setPPPDAuth(TGM.dataStore.pppdUsername,
					TGM.dataStore.pppdPassword, handler
							.obtainMessage(SET_PPPD_AUTH_DONE));

		}
	};

	private DialogInterface.OnClickListener changeApnNegativeBtnClickHandler = new DialogInterface.OnClickListener() {

		public void onClick(DialogInterface dialog, int which) {

		}
	};

	// private DialogInterface.OnClickListener
	// changeAuthenticationPositiveBtnClickHandler =new
	// DialogInterface.OnClickListener() {
	//		
	// public void onClick(DialogInterface dialog, int which) {
	//			
	// }
	// };
	//
	// private DialogInterface.OnClickListener
	// changeAuthenticationNegativeBtnClickHandler =new
	// DialogInterface.OnClickListener() {
	//		
	// public void onClick(DialogInterface dialog, int which) {
	//			
	// }
	// };

	private OnClickListener dialBtnClickListener = new OnClickListener() {

		public void onClick(View v) {

			final WifiManager wifi = (WifiManager) Dialer.this
					.getSystemService(WIFI_SERVICE);

			if (TGM.dataStore.previousWifiSetting = wifi.isWifiEnabled()) {

				String title = getResources().getString(
						R.string.tgm_disable_wifi_dialog_title_text);

				String message = getResources().getString(
						R.string.tgm_disable_wifi_dialog_message_text);

				String yesText = getResources().getString(
						R.string.tgm_disable_wifi_dialog_positive_btn_text);

				String noText = getResources().getString(
						R.string.tgm_disable_wifi_dialog_negtive_btn_text);

				new AlertDialog.Builder(Dialer.this).setTitle(title)
						.setMessage(message).setNegativeButton(noText,
								new DialogInterface.OnClickListener() {

									public void onClick(DialogInterface dialog,
											int which) {

										return;

									}
								}).setPositiveButton(yesText,
								new DialogInterface.OnClickListener() {

									public void onClick(DialogInterface dialog,
											int which) {

										wifi.setWifiEnabled(false);

										dialBtn.setEnabled(false);

										tgm
												.dialDataNetwork(handler
														.obtainMessage(DIAL_DATA_NETWORK_DONE));

										CharSequence title = getResources()
												.getText(
														R.string.tgm_dial_to_network_progress_dialog_title);
										CharSequence message = getResources()
												.getText(
														R.string.tgm_dial_to_network_progress_dialog_message);

										if (dialPd != null) {

											dialPd.dismiss();

										}

										dialPd = ProgressDialog.show(
												Dialer.this, title, message,
												true);

									}

								}).show();

			} else {

				dialBtn.setEnabled(false);

				tgm.dialDataNetwork(handler
						.obtainMessage(DIAL_DATA_NETWORK_DONE));

				CharSequence title = getResources().getText(
						R.string.tgm_dial_to_network_progress_dialog_title);
				CharSequence message = getResources().getText(
						R.string.tgm_dial_to_network_progress_dialog_message);

				if (dialPd != null) {

					dialPd.dismiss();

				}

				dialPd = ProgressDialog.show(Dialer.this, title, message, true);

			}

		}
	};

	private OnClickListener disconnectBtnClickListener = new OnClickListener() {

		public void onClick(View v) {

			disconnectBtn.setEnabled(false);

			tgm.disconnectDataNetowrk(handler
					.obtainMessage(DISCONNECT_DATA_NETWORK_DONE));

		}
	};

	private OnClickListener leaveAboutUsBtnClickListener = new OnClickListener() {

		public void onClick(View v) {

			tgm.getState(handler.obtainMessage(QUERY_STATE_DONE));

		}
	};

	private Handler handler = new Handler() {

		@Override
		public void handleMessage(Message msg) {

			Log.e(LOG_TAG, "handleMessage:" + msg.what);

			AsyncResult ar;

			CharSequence title, message;

			switch (msg.what) {

			case SET_PDP_CONTEXT_DONE:

				ar = (AsyncResult) msg.obj;

				if (ar.exception != null) {
					break;
				}

				break;

			case SET_PIN_CODE_DONE:

				ar = (AsyncResult) msg.obj;

				if (ar.exception != null) {

					// Toast.makeText(Dialer.this,
					// "ENTER PIN ERROR: "+ar.exception, 5000).show();

					TGM.dataStore.pinCountRemain--;

					showDialog(ENTER_PIN_DIALOG);

					break;
				}

				TGM.dataStore.pinCountRemain = 3;

				title = getResources().getText(
						R.string.tgm_enter_pin_progress_dialog_title);

				message = getResources().getText(
						R.string.tgm_enter_pin_progress_dialog_message);

				if (enterPinPd != null) {

					enterPinPd.dismiss();

				}

				enterPinPd = ProgressDialog.show(Dialer.this, title, message,
						true);

				// Query SIM Status again the rest will do..
				// tgm.getSIMStatus(handler.obtainMessage(QUERY_SIM_STATUS_DONE));

				break;

			case DIAL_DATA_NETWORK_DONE:

				ar = (AsyncResult) msg.obj;

				if (ar.exception != null) {
					break;
				}

				// Toast.makeText(Dialer.this, "Dial to data network OK", 5000)
				// .show();

				break;

			case DISCONNECT_DATA_NETWORK_DONE:

				title = getResources()
						.getText(
								R.string.tgm_disconnect_from_network_progress_dialog_title);

				message = getResources()
						.getText(
								R.string.tgm_disconnect_from_network_progress_dialog_message);

				if (disconnectPd != null) {

					disconnectPd.dismiss();

				}

				disconnectPd = ProgressDialog.show(Dialer.this, title, message,
						true);

				break;

			case QUERY_SIGNAL_STRENGTH_DONE: {
				/**
				 * @return received signal strength in "asu", ranging from 0 -
				 *         31, or UNKNOWN_RSSI if unknown
				 * 
				 *         For GSM, dBm = -113 + 2*asu, 0 means
				 *         "-113 dBm or less" and 31 means "-51 dBm or greater"
				 */

				ar = (AsyncResult) msg.obj;

				if (ar.exception != null) {
					break;
				}

				int rssi;

				int[] signalValues = (int[]) ar.result;

				if (signalValues.length != 0) {

					rssi = signalValues[0];

				} else {

					Log.e(LOG_TAG, "Bogus signal strength response");

					// rssi = 99;
					rssi = 19;

				}

				try {

					mRegistry.notifySignalStrength(rssi);

				} catch (RemoteException re) {

				}

				break;

			}

			case QUERY_SIM_STATUS_DONE:

				Log.i(LOG_TAG, "QUERY_SIM_STATUS_DONE");

				ar = (AsyncResult) msg.obj;

				getSimStatusDone(ar);

				break;

			case QUERY_IMEI_DONE:

				ar = (AsyncResult) msg.obj;

				if (ar.exception != null) {
					break;
				}

				// Toast.makeText(Dialer.this, "IMEI:" + (String) ar.result,
				// 5000)
				// .show();

				break;

			case QUERY_NUMBER_DONE:

				ar = (AsyncResult) msg.obj;

				if (ar.exception != null) {
					break;
				}

				// Toast.makeText(Dialer.this, "Number:" + (String) ar.result,
				// 5000).show();

				break;

			case QUERY_IMSI_DONE: {

				ar = (AsyncResult) msg.obj;

				if (ar.exception != null) {

					Log.e(LOG_TAG, "Exception querying IMSI, Exception:"
							+ ar.exception);

					break;
				}

				TGM.dataStore.imsi = (String) ar.result;

				if (TGM.dataStore.imsi != null
						&& (TGM.dataStore.imsi.length() < 6 || TGM.dataStore.imsi
								.length() > 15)) {

					Log.e(LOG_TAG, "invalid IMSI " + TGM.dataStore.imsi);

					TGM.dataStore.imsi = null;
				}

				// Country Code
				int mcc = Integer.parseInt(TGM.dataStore.imsi.substring(0, 3));

				int mnc = Integer.parseInt(TGM.dataStore.imsi.substring(3,
						3 + MccTable.smallestDigitsMccForMnc(mcc)));

				SystemProperties.set(
						TelephonyProperties.PROPERTY_SIM_OPERATOR_ISO_COUNTRY,
						MccTable.countryCodeForMcc(Integer
								.parseInt(TGM.dataStore.imsi.substring(0, 3))));

				break;
			}

			case QUERY_MANUFACTURER_DONE:

				ar = (AsyncResult) msg.obj;

				if (ar.exception != null) {
					Log.i(LOG_TAG, "QUERY_MANUFACTURER_DONE, Exception");
					break;
				}

				String dongleModelLabelString = (String) getResources()
						.getText(R.string.tgm_modem_model_label);

				TGM.dataStore.dongleModel[0] = (String) ar.result;

				dongleModelText.setText(dongleModelLabelString
						+ TGM.dataStore.dongleModel[0] + " "
						+ TGM.dataStore.dongleModel[1]);

				connectionInfoDongleModelText.setText(dongleModelLabelString
						+ TGM.dataStore.dongleModel[0] + " "
						+ TGM.dataStore.dongleModel[1]);

				break;

			case QUERY_MODEM_MODEL_DONE:

				ar = (AsyncResult) msg.obj;

				if (ar.exception != null) {
					Log.i(LOG_TAG, "QUERY_MODEL_MODEL_DONE, Exception");
					break;
				}

				String modelLabelText = (String) getResources().getText(
						R.string.tgm_modem_model_label);

				TGM.dataStore.dongleModel[1] = (String) ar.result;

				dongleModelText.setText(modelLabelText
						+ TGM.dataStore.dongleModel[0] + " "
						+ TGM.dataStore.dongleModel[1]);

				connectionInfoDongleModelText.setText(modelLabelText
						+ TGM.dataStore.dongleModel[0] + " "
						+ TGM.dataStore.dongleModel[1]);

				break;

			case QUERY_OPERATOR_DONE:

				ar = (AsyncResult) msg.obj;

				String opNames[] = (String[]) ar.result;

				if (opNames != null) {

					String carrierLabelText = (String) getResources().getText(
							R.string.tgm_carrier_label);

					String carrierTextString;

					if (TextUtils.isEmpty(opNames[0])) {

						carrierTextString = "N/A";

						new Timer().schedule(new TimerTask() {

							@Override
							public void run() {

								tgm.getOperator(handler
										.obtainMessage(QUERY_OPERATOR_DONE));

							}
						}, 3000);

					} else {

						carrierTextString = opNames[0];

					}

					carrierText.setText(carrierLabelText + carrierTextString);

					connectionInfoCarrierText.setText(carrierLabelText
							+ carrierTextString);

					if (opNames.length == 3) {

						ss.setOperatorName(opNames[0], opNames[1], opNames[2]);

					}

				}

				notifyServiceStateChanges();

				// SystemProperties
				// .set(TelephonyProperties.PROPERTY_OPERATOR_ALPHA,
				// opNames[0]);
				//
				// SystemProperties.set(
				// TelephonyProperties.PROPERTY_BASEBAND_VERSION,
				// "Camangi 3G Dialer baseband");
				//
				// SystemProperties.set(
				// TelephonyProperties.PROPERTY_OPERATOR_NUMERIC,
				// opNames[2]);

				// if (opNames != null && opNames.length >= 3) {
				//                	
				// Toast.makeText(Dialer.this,""+opNames[0]+" "+opNames[1]+" "+opNames[2],5000).show();
				// }

				break;

			case QUERY_PDP_CONTEXT_LIST_DONE:

				break;

			case QUERY_STATE_DONE:

				Log.i(LOG_TAG, "QUERY_STATE_DONE");

				ar = (AsyncResult) msg.obj;

				int[] state = (int[]) ar.result;

				if (state != null && state.length >= 1) {

					tgmStateListener.onRadioStateChanged(state[0]);

				}

				break;

			case RESET_STACK_DONE:

				break;

			case SET_PPPD_AUTH_DONE:

				break;

			default:

				Log.e(LOG_TAG, "unknown handleMessage");
				break;
			}

		}

	};

	private void loadPersistentData() {

		SharedPreferences pref = getSharedPreferences(DIALER_PREFERENCE,
				MODE_PRIVATE);

		TGM.dataStore.apn = pref.getString(APN_PREFERENCE_KEY, "internet");

		TGM.dataStore.pppdUsername = pref.getString(
				PPPD_AUTH_USERNAME_PREFERENCE_KEY, null);

		TGM.dataStore.pppdPassword = pref.getString(
				PPPD_AUTH_PASSWORD_PREFERENCE_KEY, null);

	}

	private void savePersistentData() {

		SharedPreferences pref = getSharedPreferences(DIALER_PREFERENCE,
				MODE_PRIVATE);

		Editor editor = pref.edit();

		if (!TextUtils.isEmpty(TGM.dataStore.apn)) {
			editor.putString(APN_PREFERENCE_KEY, TGM.dataStore.apn);
		}

		if (!TextUtils.isEmpty(TGM.dataStore.pppdUsername)) {

			editor.putString(PPPD_AUTH_USERNAME_PREFERENCE_KEY,
					TGM.dataStore.pppdUsername);
		}

		if (!TextUtils.isEmpty(TGM.dataStore.pppdPassword)) {

			editor.putString(PPPD_AUTH_PASSWORD_PREFERENCE_KEY,
					TGM.dataStore.pppdPassword);

		}

		editor.commit();

	}

	private void getSimStatusDone(AsyncResult ar) {

		if (ar.exception != null) {

			Log.e(LOG_TAG, "Error getting SIM status. "
					+ "RIL_REQUEST_GET_SIM_STATUS should "
					+ "never return an error", ar.exception);
			return;
		}

		TGMConstants.SimStatus newStatus = (TGMConstants.SimStatus) ar.result;

		switch (newStatus) {

		case SIM_READY:

			// Toast.makeText(Dialer.this, "SIM_READY", 5000).show();

			break;

		case SIM_ABSENT:

			Log.e(LOG_TAG, "SIM_ABSENT");

			switchView(SIM_FAILURE_PAGE);

			break;

		case SIM_PIN:

			Log.e(LOG_TAG, "SIM_PIN");

			switchView(ENTER_PIN_PAGE);

			break;

		case SIM_PUK:

			Log.e(LOG_TAG, "SIM_PUK");

			switchView(ENTER_PUK_PAGE);

			break;

		case SIM_NOT_READY:

			break;

		case SIM_NETWORK_PERSONALIZATION:

			// Toast.makeText(Dialer.this, "SIM_NETWORK_PERSONALIZATION", 5000)
			// .show();

			break;

		default:

			// Toast.makeText(Dialer.this, "UNKNOWN SIM STATUS", 5000).show();
			break;

		}
	}

	private Timer refreshConnectionTimer;

	private OnTGMStateChangedListener tgmStateListener = new OnTGMStateChangedListener() {

		public void onSignalStrengthChanged(int rssi) {

			try {

				mRegistry.notifySignalStrength(rssi);

			} catch (RemoteException re) {

			}

		}

		public void onRadioStateChanged(int radioState) {

			Log.i(LOG_TAG, "Radio State Changes: " + radioState);

			TGM.dataStore.currentRadioState = radioState;

			ConnectivityManager cm = (ConnectivityManager) Dialer.this
					.getSystemService(CONNECTIVITY_SERVICE);

			// TelephonyManager tm = (TelephonyManager) Dialer.this
			// .getSystemService(TELEPHONY_SERVICE);

			NetworkInfo.State currentDataState = State.UNKNOWN;

			if (ConnectivityManager
					.isNetworkTypeValid(ConnectivityManager.TYPE_MOBILE)) {

				NetworkInfo mni = cm
						.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);

				currentDataState = mni.getState();
			}

			// Try to notify TelephonyRegistry that we are not currently
			// connected to data network
			if (radioState != TGMConstants.TGM_STATE_PPPD_READY) {

				switch (currentDataState) {

				case CONNECTED:
				case CONNECTING:
				case SUSPENDED:
				case DISCONNECTING:
				case UNKNOWN: {

					try {

						mRegistry.notifyDataConnection(
								TelephonyManager.DATA_DISCONNECTED, true,
								"Dialer just disconnect network connectivity",
								"internet", "Camangi 3G Dialer");

					} catch (RemoteException ex) {

						// system process is dead

					}

					break;

				}

				case DISCONNECTED:

					// Do nothing here
					break;
				}

			}

			switch (radioState) {

			case TGMConstants.TGM_STATE_OFF:

				switchView(PLUG_IN_DONGLE_INFO_PAGE);

				ss.setState(ServiceState.STATE_OUT_OF_SERVICE);

				notifyServiceStateChanges();

				break;

			case TGMConstants.TGM_STATE_UNAVAILABLE:

				Log.i(LOG_TAG, "TGM_STATE_UNAVAILABLE");

				ss.setState(ServiceState.STATE_OUT_OF_SERVICE);

				notifyServiceStateChanges();

				break;

			case TGMConstants.TGM_STATE_SIM_NOT_READY:

				// TODO:What kind of view show we show here
				ss.setState(ServiceState.STATE_OUT_OF_SERVICE);

				notifyServiceStateChanges();

				break;

			case TGMConstants.TGM_STATE_SIM_LOCKED_OR_ABSENT:

				Log.e(LOG_TAG, "TGM_STATE_SIM_LOCKED_OR_ABSENT");

				// Just query sim status, and the handler will handle
				// everything.

				tgm.getSIMStatus(handler.obtainMessage(QUERY_SIM_STATUS_DONE));

				ss.setState(ServiceState.STATE_OUT_OF_SERVICE);

				notifyServiceStateChanges();

				break;

			case TGMConstants.TGM_STATE_SIM_READY:

				tgm.getManufacturer(handler
						.obtainMessage(QUERY_MANUFACTURER_DONE));

				tgm.getModemModel(handler
								.obtainMessage(QUERY_MODEM_MODEL_DONE));

				// tgm.getIMEI(handler.obtainMessage(QUERY_IMEI_DONE));

				tgm.getOperator(handler.obtainMessage(QUERY_OPERATOR_DONE));

				tgm.getSignalStrenth(handler
						.obtainMessage(QUERY_SIGNAL_STRENGTH_DONE));

				tgm.setPDPContext(TGM.dataStore.apn, null, null, handler
						.obtainMessage(SET_PDP_CONTEXT_DONE));
				
				tgm.setPPPDAuth(TGM.dataStore.pppdUsername, TGM.dataStore.pppdPassword, handler
						.obtainMessage(SET_PPPD_AUTH_DONE));

				ss.setState(ServiceState.STATE_IN_SERVICE);

				notifyServiceStateChanges();

				switchView(DIALER_PAGE);

				break;

			case TGMConstants.TGM_STATE_PPPD:

				// State and Dialing and Connected

				break;

			case TGMConstants.TGM_STATE_PPPD_READY:

				ss.setState(ServiceState.STATE_IN_SERVICE);

				tgm.getOperator(handler.obtainMessage(QUERY_OPERATOR_DONE));

				tgm.getManufacturer(handler
						.obtainMessage(QUERY_MANUFACTURER_DONE));

				tgm
						.getModemModel(handler
								.obtainMessage(QUERY_MODEM_MODEL_DONE));

				if (currentDataState != NetworkInfo.State.CONNECTED) {

					try {

						mRegistry.notifyDataConnection(
								TelephonyManager.DATA_CONNECTED, true,
								"Dial to data network success!!!", "internet",
								"USB Dongle");

						mRegistry
								.notifyDataActivity(TelephonyManager.DATA_ACTIVITY_INOUT);

						mRegistry.notifyServiceState(ss);

					} catch (RemoteException ex) {

						// system process is dead

					}

				}

				switchView(CONNECTION_INFO_PAGE);

				break;

			}

		}

		public void onPPPDDisconnected() {

			Log.i(LOG_TAG, "onPPPDDisconnected");

			TGM.dataStore.connectionDate = null;

			refreshConnectionTimer.cancel();

			refreshConnectionTimer = null;

			WifiManager wifi = (WifiManager) getSystemService(WIFI_SERVICE);

			// TGM.dataStore.previousWifiSetting = wifi.isWifiEnabled();

			wifi.setWifiEnabled(TGM.dataStore.previousWifiSetting);

			// try {
			//
			// mRegistry.notifyDataConnection(
			// TelephonyManager.DATA_DISCONNECTED, true,
			// "Dialer just disconnect network connectivity",
			// "internet", "Camangi 3G Dialer");
			//
			// } catch (RemoteException ex) {
			// // system process is dead
			// }

		}

		public void onPPPDConnected() {

			TGM.dataStore.connectionDate = new Date();

			refreshConnectionTimer = new Timer();

			// this will update the connection time per 20 secs
			refreshConnectionTimer.schedule(new TimerTask() {

				@Override
				public void run() {

					handler.post(new Runnable() {

						public void run() {

							if (isActivityValid) {

								// Log.i(LOG_TAG,"update");

								String connectionTimeLabelText = getResources()
										.getString(
												R.string.tgm_connection_time_label);

								if (TGM.dataStore.connectionDate != null) {

									long diff = new Date().getTime()
											- TGM.dataStore.connectionDate
													.getTime();

									int hr = (int) diff / (1000 * 60 * 60);

									int min = (int) diff / (60 * 1000) % 60;

									int sec = ((int) diff / (1000)) % 60;

									String time = ((hr <= 0) ? "" : hr
											+ " Hours : ")
											+ ((min <= 0) ? "" : min
													+ " Minutes : ")
											+ sec
											+ " Seconds";

									connectionTimeText
											.setText(connectionTimeLabelText
													+ " " + time);
								}

							}

						}
					});
				}

			}, 0, 10 * 1000);

			// TODO MODOFIED
			// WifiManager wifi = (WifiManager) getSystemService(WIFI_SERVICE);

			// wifi.setWifiEnabled(false);

			switchView(CONNECTION_INFO_PAGE);

			// try {
			//				
			// mRegistry.notifyDataConnection(
			// TelephonyManager.DATA_CONNECTED, true,
			// "Dial to data network success!!!", "internet",
			// "USB Dongle");
			//
			// mRegistry.notifyDataActivity(TelephonyManager.DATA_ACTIVITY_INOUT);
			//
			// mRegistry.notifyServiceState(ss);
			//
			// } catch (RemoteException ex) {
			//				
			// // system process is dead
			//				
			// }
		}

		public void onDeviceRemoved() {

		}

		public void onDeviceDetected() {

		}

		public void onPPPDFailed() {

			showDialog(DIAL_FAILED_DIALOG);
			// TODO Auto-generated method stub

		}
	};

	// private boolean isPPPDConnected = false;

	private void switchView(final int which) {

		handler.post(new Runnable() {

			public void run() {

				if (!isActivityValid) {

					return;

				}

				Log.i(LOG_TAG, "Switch to View: " + which);

				if (dialPd != null && dialPd.isShowing()) {

					dialPd.dismiss();

					dialPd = null;

				}

				if (enterPinPd != null && enterPinPd.isShowing()) {

					enterPinPd.dismiss();

					enterPinPd = null;
				}

				if (disconnectPd != null && disconnectPd.isShowing()) {

					disconnectPd.dismiss();

					disconnectPd = null;
				}

				if (enterPINDialog != null && enterPINDialog.isShowing()) {

					enterPINDialog.dismiss();

				}

				if (enterPUKDialog != null && enterPUKDialog.isShowing()) {

					enterPUKDialog.dismiss();

				}

				airPlaneModePage.setVisibility(View.GONE);

				dialerPage.setVisibility(View.GONE);

				plugInDongleInfoPage.setVisibility(View.GONE);

				connectionInfoPage.setVisibility(View.GONE);

				simFailuePage.setVisibility(View.GONE);

				enterPINPage.setVisibility(View.GONE);

				enterPUKPage.setVisibility(View.GONE);

				aboutUsPage.setVisibility(View.GONE);

				if (getAirPlaneModeOn() > 0 && which != ABOUT_US_PAGE) {

					airPlaneModePage.setVisibility(View.VISIBLE);

					return;
				}

				switch (which) {

				case PLUG_IN_DONGLE_INFO_PAGE:

					plugInDongleInfoPage.setVisibility(View.VISIBLE);

					break;

				case ENTER_PIN_PAGE:

					showDialog(ENTER_PIN_DIALOG);

					enterPINPage.setVisibility(View.VISIBLE);

					break;

				case ENTER_PUK_PAGE:

					enterPUKPage.setVisibility(View.VISIBLE);

					break;

				case SIM_FAILURE_PAGE:

					simFailuePage.setVisibility(View.VISIBLE);

					break;

				case DIALER_PAGE:

					dialerPage.setVisibility(View.VISIBLE);

					dialBtn.setEnabled(true);

					break;

				case CONNECTION_INFO_PAGE:

					connectionInfoPage.setVisibility(View.VISIBLE);

					disconnectBtn.setEnabled(true);

					break;

				case ABOUT_US_PAGE:

					aboutUsPage.setVisibility(View.VISIBLE);

					break;

				case AIR_PLANE_MODE_PAGE:

					airPlaneModePage.setVisibility(View.VISIBLE);
					break;

				}

			}
		});

	}

	private int getAirPlaneModeOn() {

		try {

			return Settings.System.getInt(this.getContentResolver(),
					Settings.System.AIRPLANE_MODE_ON, 0);

		} catch (Exception ee) {

			Log.e(LOG_TAG, ee.toString());

		}

		return 0;
	}

	private void notifyServiceStateChanges() {

		try {

			if (getAirPlaneModeOn() > 0) {

				ss.setStateOff();

			}

			mRegistry.notifyServiceState(ss);

		} catch (RemoteException re) {

			Log.e(LOG_TAG, re.toString());

		}

	}

}