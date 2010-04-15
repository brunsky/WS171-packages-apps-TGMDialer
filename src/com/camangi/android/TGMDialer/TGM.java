package com.camangi.android.TGMDialer;


import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;


import android.content.Context;
import android.net.LocalSocket;
import android.net.LocalSocketAddress;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.Parcel;
import android.os.RemoteException;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

public class TGM {

	private static TGM instance;
	
	public static TGM getInstance(Context context){
		
		if(instance==null){
			
			if(context!=null){
				
				instance =new TGM(context);
			
			}else{
			
				return null;
			
			}
		}
		
		return instance;
	}
	
	public static TGMDataStore dataStore;
	
	static final int MAX_TGM_BUFFER_SIZE = (1024 * 8);

	// -----------------------------
	
	private Context mContext;

	private TGMReceiver mReceiver;

	private Thread mReceiverThread;

	private TGMSender mSender;

	private HandlerThread mSenderThread;

	private LocalSocket mSocket;

	// How many requests still waiting to be processed
	private int mRequestMessagesPending;
	
	private OnTGMStateChangedListener mStateChangedListener;

	public void setTGMStateChangedListener(OnTGMStateChangedListener mStateChangedListener) {
		
		this.mStateChangedListener = mStateChangedListener;
		
	}

	// Message queue
	private ArrayList<TGMRequest> mRequestsList = new ArrayList<TGMRequest>();

	private static final String LOG_TAG = "TGMJ";

	private static final String TGM_SOCKET_NAME = "/tmp/tgmd";

	private static final int EVENT_SEND = 1;
	
	private static final int EVENT_WAKE_LOCK_TIMEOUT = 2;

	private static final int RESPONSE_SOLICITED = 0;
	
	private static final int RESPONSE_UNSOLICITED = 1;

	private static final int SOCKET_OPEN_RETRY_MILLIS = 4 * 1000; // 4 secs
	

	private TGM(Context context) {
	
		Log.d(LOG_TAG,"TGM.java initilized");

		this.mContext = context;

		//Start Sender Thread, 
		mSenderThread = new HandlerThread("TGM Sender");
		
		mSenderThread.start();

		Looper looper = mSenderThread.getLooper();

		mSender = new TGMSender(looper);

		mReceiver = new TGMReceiver();
		mReceiverThread = new Thread(mReceiver, "TGMReceiver");
		mReceiverThread.start();
		
		dataStore = new TGMDataStore();

		// TGM Check if this is needed

		// IntentFilter filter = new IntentFilter();
		// filter.addAction(Intent.ACTION_SCREEN_ON);
		// filter.addAction(Intent.ACTION_SCREEN_OFF);
		// context.registerReceiver(mIntentReceiver, filter);

	}

	private static int readTgmMessage(InputStream is, byte[] buffer)
			throws IOException {

		int countRead;
		int offset;
		int remaining;
		int messageLength;

		// First, read in the length of the message
		offset = 0;
		remaining = 4;

		do {
			
			countRead = is.read(buffer, offset, remaining);

			if (countRead < 0) {
				
				Log.e(LOG_TAG, "Hit EOS reading message length");
				
				return -1;
			}

			offset += countRead;
			remaining -= countRead;
			
		} while (remaining > 0);

		messageLength = ((buffer[0] & 0xff) << 24) | ((buffer[1] & 0xff) << 16)
				| ((buffer[2] & 0xff) << 8) | (buffer[3] & 0xff);

		// Then, re-use the buffer and read in the message itself
		offset = 0;
		remaining = messageLength;
		
		do {
			countRead = is.read(buffer, offset, remaining);

			if (countRead < 0) {
				Log.e(LOG_TAG, "Hit EOS reading message.  messageLength="
						+ messageLength + " remaining=" + remaining);
				return -1;
			}

			offset += countRead;
			remaining -= countRead;
			
		} while (remaining > 0);

		return messageLength;
	}

	private void processResponse(Parcel p) {
		
		int type;

		type = p.readInt();

		if (type == RESPONSE_UNSOLICITED) {

			processUnsolicited(p);

		} else if (type == RESPONSE_SOLICITED) {

			processSolicited(p);
		}

		releaseWakeLockIfDone();
	}

	private TGMRequest findAndRemoveRequestFromList(int serial) {
		
		synchronized (mRequestsList) {
			
			for (int i = 0, s = mRequestsList.size(); i < s; i++) {
				TGMRequest rr = mRequestsList.get(i);

				if (rr.mSerial == serial) {
					mRequestsList.remove(i);
					return rr;
				}
			}
		}

		return null;
	}

	private void releaseWakeLockIfDone() {
		
		// synchronized (mWakeLock) {
		// if (mWakeLock.isHeld() &&
		// (mRequestMessagesPending == 0) &&
		// (mRequestsList.size() == 0)) {
		// mSender.removeMessages(EVENT_WAKE_LOCK_TIMEOUT);
		// mWakeLock.release();
		// }
		// }
		
	}

	private void send(TGMRequest rr) {

		Message msg;

		msg = mSender.obtainMessage(EVENT_SEND, rr);

		// TODO Get wake lock;
		// acquireWakeLock();

		msg.sendToTarget();
	}

	private void processUnsolicited(Parcel p) {
		
		// TODO Implement this
		
		Log.i(LOG_TAG,"processUnsolicited");
		
		int response;
		
        Object ret;

        response = p.readInt();
        
        switch(response){
        
        case TGMConstants.TGM_UNSOLICITED_DEVICE_DETECTED:
        	
        	if(mStateChangedListener!=null){
        		
        		mStateChangedListener.onDeviceDetected();
        		
        	}
        	
        	Log.i(LOG_TAG,"TGM_UNSOLICITED_DEVICE_DETECTED");
        	
        	break;
        
        case TGMConstants.TGM_UNSOLICITED_DEVICE_REMOVED:
        	
        	if(mStateChangedListener!=null){
        		mStateChangedListener.onDeviceRemoved();
        	}
        	
        	Log.i(LOG_TAG,"TGM_UNSOLICITED_DEVICE_REMOVED");
        	
        	break;
        
        case TGMConstants.TGM_UNSOLICITED_SIGNAL_STRENGTH :
        	
        	if(mStateChangedListener!=null){
        		
        		ret = responseInts(p);
        		
				int rssi;

				int[] signalValues = (int[]) ret;

				if (signalValues.length > 0) {

					rssi = signalValues[0];

				} else {

					Log.e(LOG_TAG, "Bogus TGM_UNSOLICITED_SIGNAL_STRENGTH");
					
					rssi = 99;
				}

				
				mStateChangedListener.onSignalStrengthChanged(rssi);

        	}
        	
        	break;
        
        case TGMConstants.TGM_UNSOLICITED_RADIO_STATE_CHANGED:
        	
        	int radioState = p.readInt();
        	
        	if(mStateChangedListener!=null){
        		
        		Log.i(LOG_TAG,"Changing Radio: "+ radioState);
        		mStateChangedListener.onRadioStateChanged(radioState);
        		
        	}
        	
        	
        	break;
        
        case TGMConstants.TGM_UNSOLICITED_PPPD_CONNECTED :
        	
        	if(mStateChangedListener!=null){
        		
        		mStateChangedListener.onPPPDConnected();
        		
        	}
        	
        	Log.i(LOG_TAG,"TGM_UNSOLICITED_PPPD_CONNECTED");
        	
        	break;
        	
        case TGMConstants.TGM_UNSOLICITED_PPPD_DISCONNECTED:
        	
        	if(mStateChangedListener!=null){
        		
        		mStateChangedListener.onPPPDDisconnected();
        		
        	}
        	Log.i(LOG_TAG,"TGM_UNSOLICITED_PPPD_DISCONNECTED");
        	
        	break;
        	
        case TGMConstants.TGM_UNSOLICITED_PPPD_FAILED:
        	
        	if(mStateChangedListener!=null){
        		
        		mStateChangedListener.onPPPDFailed();
        		
        	}
        	
        	Log.i(LOG_TAG,"TGM_UNSOLICITED_PPPD_FAILED");
        	
        	break;
        	
        }
		
	}

	private void processSolicited(Parcel p) {

		Log.e(LOG_TAG, "round trip call back from tgm stack");

		int serial, error;

		boolean found = false;

		serial = p.readInt();
		error = p.readInt();

		TGMRequest rr;

		// Finding request from request list by serial
		rr = findAndRemoveRequestFromList(serial);

		if (rr == null) {
			Log.w(LOG_TAG, "Unexpected solicited response! sn: " + serial
					+ " error: " + error);
			return;
		}

		if (error != 0) {
			rr.onError(error);
			rr.release();
			return;
		}

		Object ret;

		try {

			switch (rr.mRequest) {

			case TGMConstants.TGM_REQUEST_SET_PDP_CONTEXT:

				ret = responseVoid(p);

				break;

			case TGMConstants.TGM_REQUEST_SET_PIN_CODE:

				ret = responseVoid(p);

				break;

			case TGMConstants.TGM_REQUEST_DIAL_DATA_NETWORK:

				ret = responseVoid(p);

				break;

			case TGMConstants.TGM_REQUEST_DISCONNECT_DATA_NETWORK:
				ret = responseVoid(p);

				break;

			case TGMConstants.TGM_REQUEST_QUERY_SIGNAL_STRENGTH:

				ret = responseInts(p);

				break;

			case TGMConstants.TGM_REQUEST_QUERY_OPERATOR:

				// Done

				ret = responseStrings(p);

				break;

			case TGMConstants.TGM_REQUEST_QUERY_IMEI:

				// Done

				Log.e(LOG_TAG, "TGM_REQUEST_QUERY_IMEI Result");

				ret = responseString(p);

				break;

			case TGMConstants.TGM_REQUEST_QUERY_NUMBER:

				Log.e(LOG_TAG, "TGM_REQUEST_QUERY_NUMBER Result");

				ret = responseString(p);

				break;

			case TGMConstants.TGM_REQUEST_QUERY_MANUFACTURER:

				// Done
				Log.e(LOG_TAG, "TGM_REQUEST_QUERY_MANUFACTURER Result");

				ret = responseString(p);

				break;

			case TGMConstants.TGM_REQUEST_QUERY_MODEM_MODEL:

				// Done
				Log.e(LOG_TAG, "TGM_REQUEST_QUERY_MODEM_MODEL Result");

				ret = responseString(p);

				break;

			case TGMConstants.TGM_REQUEST_QUERY_PDP_CONTEXT_LIST:

				ret = responseStrings(p);

				break;

			case TGMConstants.TGM_REQUEST_QUERY_SIM_STATUS:

				// Done
				ret = responseSimStatus(p);

				break;

			case TGMConstants.TGM_REQUEST_QUERY_IMSI:

				// Done
				ret = responseString(p);
				
				break;
				
			case TGMConstants.TGM_REQUEST_QUERY_STATE:
				
				ret=responseInts(p);
				
				break;
				
			case TGMConstants.TGM_REQUEST_RESET_STACK:
				
				ret=responseVoid(p);
				
				break;
				
			case TGMConstants.TGM_REQUEST_SET_PPPD_AUTH:
				
				ret=responseVoid(p);

			default:

				throw new RuntimeException("Unrecognized solicited response: "
						+ rr.mRequest);

			}

		} catch (Throwable tr) {

			Log.w(LOG_TAG, rr.serialString() + "< "
					+ requestToString(rr.mRequest)
					+ " exception, possible invalid TGM response", tr);

			if (rr.mResult != null) {

				AsyncResult.forMessage(rr.mResult, null, tr);

				rr.mResult.sendToTarget();
			}

			rr.release();
			return;

		}

		if (rr.mResult != null) {
			
			// set message obj to new AsyncResult
			AsyncResult.forMessage(rr.mResult, ret, null);
			
			rr.mResult.sendToTarget();
			
		}

		rr.release();

		// TODO Implement this

	}
	
	public void getState(Message result){
		
		TGMRequest rr = TGMRequest.obtain(
				TGMConstants.TGM_REQUEST_QUERY_STATE, result);

		send(rr);
		
	}

	public void getSIMStatus(Message result) {

		TGMRequest rr = TGMRequest.obtain(
				TGMConstants.TGM_REQUEST_QUERY_SIM_STATUS, result);

		send(rr);

	}

	public void getOperator(Message result) {

		TGMRequest rr = TGMRequest.obtain(
				TGMConstants.TGM_REQUEST_QUERY_OPERATOR, result);

		send(rr);

	}

	public void getIMEI(Message result) {

		TGMRequest rr = TGMRequest.obtain(TGMConstants.TGM_REQUEST_QUERY_IMEI,
				result);

		send(rr);
	}

	public void getIMSI(Message result) {

		TGMRequest rr = TGMRequest.obtain(TGMConstants.TGM_REQUEST_QUERY_IMSI,
				result);

		send(rr);
	}

	public void getManufacturer(Message result) {

		TGMRequest rr = TGMRequest.obtain(
				TGMConstants.TGM_REQUEST_QUERY_MANUFACTURER, result);

		// if (RILJ_LOGD) riljLog(rr.serialString() + "> " +
		// requestToString(rr.mRequest));

		send(rr);

	}

	public void getModemModel(Message result) {

		TGMRequest rr = TGMRequest.obtain(
				TGMConstants.TGM_REQUEST_QUERY_MODEM_MODEL, result);

		send(rr);
	}


	public void getSignalStrenth(Message result) {

		TGMRequest rr = TGMRequest.obtain(
				TGMConstants.TGM_REQUEST_QUERY_SIGNAL_STRENGTH, result);

		send(rr);

		// if pppd connected, then return cached signal strength

	}

	public void setPDPContext(String apn, String user, String password,
			Message result) {
		
		
		Log.d(LOG_TAG, "setPDPContext: APN: "+apn+" User: "+ user +" Password: "+password);

		TGMRequest rr = TGMRequest.obtain(
				TGMConstants.TGM_REQUEST_SET_PDP_CONTEXT, result);

		rr.mp.writeInt(3);
		rr.mp.writeString(apn);
		rr.mp.writeString(user);
		rr.mp.writeString(password);

		send(rr);

	}

	public void supplyPIN(String pin, Message result) {
		
		TGMRequest rr = TGMRequest.obtain( TGMConstants.TGM_REQUEST_SET_PIN_CODE, result);

		rr.mp.writeInt(1);
		rr.mp.writeString(pin);
		
		send(rr);

	}
	
	public void setPPPDAuth(String username, String password,Message result){
		
		TGMRequest rr = TGMRequest.obtain( TGMConstants.TGM_REQUEST_SET_PPPD_AUTH, result);
		
		rr.mp.writeInt(2);
		
		if(!TextUtils.isEmpty(username)){
		
			rr.mp.writeString(username);
		
		}else{
		
			rr.mp.writeString("");
		
		}
		
		if(!TextUtils.isEmpty(password)){
		
			rr.mp.writeString(password);
		
		}else{
		
			rr.mp.writeString("");
		
		}
		
		send(rr);
		
	}

	public void supplyPUK(String puk, Message result) {
		
		//TGMRequest rr = TGMRequest.obtain( TGMConstants.TGM_REQUEST_DIAL_DATA_NETWORK, result);

		//send(rr);

	}

	public void dialDataNetwork(Message result) {
		
		TGMRequest rr = TGMRequest.obtain( TGMConstants.TGM_REQUEST_DIAL_DATA_NETWORK, result);

		send(rr);
		

	}

	public void disconnectDataNetowrk(Message result) {
		
		TGMRequest rr = TGMRequest.obtain( TGMConstants.TGM_REQUEST_DISCONNECT_DATA_NETWORK, result);

		send(rr);

	}
	
	public void resetStack(Message result){
		
		TGMRequest rr = TGMRequest.obtain( TGMConstants.TGM_REQUEST_RESET_STACK, result);

		send(rr);
		
	}

	public void getNumber(Message result) {

		TGMRequest rr = TGMRequest.obtain(
				TGMConstants.TGM_REQUEST_QUERY_NUMBER, result);

		// if (RILJ_LOGD) riljLog(rr.serialString() + "> " +
		// requestToString(rr.mRequest));

		send(rr);
	}

	private Object responseVoid(Parcel p) {
		return null;
	}

	private Object responseString(Parcel p) {
		String response;

		response = p.readString();

		return response;
	}

	private Object responseStrings(Parcel p) {
		
		int num;
		
		String response[];

		response = p.createStringArray();

		// if (true) {
		// num = p.readInt();
		//
		// response = new String[num];
		// for (int i = 0; i < num; i++) {
		// response[i] = p.readString();
		// }
		// }

		return response;
	}

	private Object responseInts(Parcel p) {

		int numInts;
		int response[];

		numInts = p.readInt();

		response = new int[numInts];

		for (int i = 0; i < numInts; i++) {
			response[i] = p.readInt();
		}

		return response;
	}

	private Object responseRaw(Parcel p) {

		int num;

		byte response[];

		response = p.createByteArray();

		return response;
	}

	private Object responseSimStatus(Parcel p) {

		int status;

		status = ((int[]) responseInts(p))[0];

		switch (status) {

		case TGMConstants.TGM_SIM_ABSENT:
			return TGMConstants.SimStatus.SIM_ABSENT;
			
		case TGMConstants.TGM_SIM_NOT_READY:
			return TGMConstants.SimStatus.SIM_NOT_READY;
			
		case TGMConstants.TGM_SIM_READY:
			return TGMConstants.SimStatus.SIM_READY;
			
		case TGMConstants.TGM_SIM_PIN:
			return TGMConstants.SimStatus.SIM_PIN;
			
		case TGMConstants.TGM_SIM_PUK:
			return TGMConstants.SimStatus.SIM_PUK;

		case TGMConstants.TGM_SIM_NETWORK_PERSONALIZATION:
			return TGMConstants.SimStatus.SIM_NETWORK_PERSONALIZATION;

		default:
			// Unrecognized SIM status. Treat it like a missing SIM.
			Log.e(LOG_TAG, "Unrecognized RIL_REQUEST_GET_SIM_STATUS result: "
					+ status);
			return TGMConstants.SimStatus.SIM_ABSENT;
		}

	}

	public void responseSignalStrength() {

	}

	public void responseNetworkFlow() {

	}

	static String requestToString(int request) {

		switch (request) {

		case TGMConstants.TGM_REQUEST_SET_PDP_CONTEXT:
			return "SET PDP CONTEXT";

		case TGMConstants.TGM_REQUEST_SET_PIN_CODE:
			return "SET PIN CODE";

		case TGMConstants.TGM_REQUEST_DIAL_DATA_NETWORK:
			return "DIAL DATA NETWORK";

		case TGMConstants.TGM_REQUEST_DISCONNECT_DATA_NETWORK:
			return "DISCONNECT DATA NETWORK";

		case TGMConstants.TGM_REQUEST_QUERY_SIGNAL_STRENGTH:
			return "QUERY SIGNAL STRENGTH";

		case TGMConstants.TGM_REQUEST_QUERY_OPERATOR:
			return "QUERY OPERATOR";

		case TGMConstants.TGM_REQUEST_QUERY_IMEI:
			return "QUERY IMEI";

		case TGMConstants.TGM_REQUEST_QUERY_MANUFACTURER:
			return "QUERY MANUFACTURER";

		case TGMConstants.TGM_REQUEST_QUERY_PDP_CONTEXT_LIST:
			return "QUERY PDP CONTEXT";

		case TGMConstants.TGM_REQUEST_QUERY_MODEM_MODEL:
			return "QUERY MODEM MODEL";

		case TGMConstants.TGM_REQUEST_QUERY_SIM_STATUS:
			return "QUERY SIM STATUS";

		case TGMConstants.TGM_REQUEST_QUERY_IMSI:
			return "QUERY IMSI";

		case TGMConstants.TGM_REQUEST_QUERY_NUMBER:
			return "QUERY NUMBER";
			
		case TGMConstants.TGM_REQUEST_QUERY_STATE:
			return "QUERY STATE";
			
		case TGMConstants.TGM_REQUEST_RESET_STACK:
			return "RESET STACK";
			
		case TGMConstants.TGM_REQUEST_SET_PPPD_AUTH:
			return "SET PPPD AUTH";

		default:
			return "<unknown request>";

		}
	}

	// -----------------------------
	
	// TODO Not yet implement complete
	class TGMSender extends Handler{

		// Only allocated once
		byte[] dataLength = new byte[4];

		public TGMSender(Looper looper) {

			super(looper);

		}

		@Override
		public void handleMessage(Message msg) {

			TGMRequest tr = (TGMRequest) (msg.obj);

			TGMRequest req = null;

			switch (msg.what) {

			case EVENT_SEND:

				boolean alreadySubtracted = false;

				try {

					Log.e(LOG_TAG, "Sending message to tgm stack");
					
					LocalSocket sock;

					sock = mSocket;

					if (sock == null) {

						tr.onError(TGMConstants.RADIO_NOT_AVAILABLE);
						tr.release();

						mRequestMessagesPending--;
						alreadySubtracted = true;
						
						Log.e(LOG_TAG, "Socket Null");
						
						return;
					}

					synchronized (mRequestsList) {
						
						mRequestsList.add(tr);
						
					}

					mRequestMessagesPending--;
					alreadySubtracted = true;

					byte[] data;

					data = tr.mp.marshall();
					tr.mp.recycle();
					tr.mp = null;

					if (data.length > MAX_TGM_BUFFER_SIZE) {

						throw new RuntimeException(
								"Parcel larger than max bytes allowed! "
										+ data.length);
					}

					// parcel length in big endian
					dataLength[0] = dataLength[1] = 0;
					dataLength[2] = (byte) ((data.length >> 8) & 0xff);
					dataLength[3] = (byte) ((data.length) & 0xff);

					// Log.v(LOG_TAG, "writing packet: " + data.length +
					// " bytes");

					sock.getOutputStream().write(dataLength);
					sock.getOutputStream().write(data);

				} catch (IOException ex) {

					Log.e(LOG_TAG, "IOException", ex);

					req = findAndRemoveRequestFromList(tr.mSerial);
					// make sure this request has not already been handled,
					// eg, if RILReceiver cleared the list.
					if (req != null || !alreadySubtracted) {
						tr.onError(TGMConstants.RADIO_NOT_AVAILABLE);
						tr.release();
					}

				} catch (RuntimeException exc) {

					Log.e(LOG_TAG, "Uncaught exception ", exc);
					req = findAndRemoveRequestFromList(tr.mSerial);
					// make sure this request has not already been handled,
					// eg, if RILReceiver cleared the list.
					if (req != null || !alreadySubtracted) {
						tr.onError(TGMConstants.GENERIC_FAILURE);
						tr.release();
					}
				}

				if (!alreadySubtracted) {
					mRequestMessagesPending--;
				}

				break;
			
			case EVENT_WAKE_LOCK_TIMEOUT:

				 //Haven't heard back from the last request. Assume we're
				 //not getting a response and release the wake lock.
				 //TODO should we clean up mRequestList and mRequestPending
				 
//				synchronized (mWakeLock) {
//				 if (mWakeLock.isHeld()) {
//				 if (RILJ_LOGD) {
//				 synchronized (mRequestsList) {
//				 int count = mRequestsList.size();
//				                                
//				 Log.d(LOG_TAG, "WAKE_LOCK_TIMEOUT " +
//				 " mReqPending=" + mRequestMessagesPending +
//				 " mRequestList=" + count);
//				
//				 for (int i = 0; i < count; i++) {
//				 rr = mRequestsList.get(i);
//				 Log.d(LOG_TAG, i + ": [" + rr.mSerial + "] " +
//				 requestToString(rr.mRequest));
//				                                        
//				 }
//				 }
//				 }
//				 mWakeLock.release();
//				 }
//				 }

				break;

			}

		}

	}
	
//	private Object closeLock=new Object();
//	
//	private boolean isClosed = true;
//	
//	public void close(){
//		
//		synchronized (closeLock) {
//			
//			isClosed = true;
//			
//		}
//	}

	//Seems OK
	class TGMReceiver implements Runnable {

		byte[] buffer;

		LocalSocket sock;

		LocalSocketAddress sockAddress;

		TGMReceiver() {

			buffer = new byte[MAX_TGM_BUFFER_SIZE];

		}

		public void run() {

			try {

				int retryCount = 0;

				// Looper for reading message
				for (;;) {

					try {

						sock = new LocalSocket();

						sockAddress = new LocalSocketAddress(TGM_SOCKET_NAME,
								LocalSocketAddress.Namespace.FILESYSTEM);

						sock.connect(sockAddress);

					} catch (Exception e) {

						try {

							if (sock != null) {
								sock.close();
							}

						} catch (IOException ex2) {
							// ignore failure to close after failure to connect
						}

						if (retryCount == 8) {

							Log.e(LOG_TAG, "Couldn't find '" + TGM_SOCKET_NAME
									+ "' socket after " + retryCount
									+ " times, continuing to retry silently");

						}

						if (retryCount > 0 && retryCount < 8) {

							Log.w(LOG_TAG, "Couldn't find '" + TGM_SOCKET_NAME
									+ "' socket after " + retryCount
									+ " times, continuing to retry silently");

						}

						try {
							Thread.sleep(SOCKET_OPEN_RETRY_MILLIS);

						} catch (InterruptedException er) {

						}

						retryCount++;

						continue;
					}// end of try catch

					// reset retry count
					retryCount = 0;

					// if the local socket is open successfully just assign to
					// mSocket
					mSocket = sock;

					Log.i(LOG_TAG, "Connected to '" + TGM_SOCKET_NAME
							+ "' socket");

					int length = 0;

					try {

						InputStream is = mSocket.getInputStream();

						for (;;) {
							
							Parcel p;

							try{
								
								length = readTgmMessage(is, buffer);
							
							}catch(IOException ioe){
								//may due to tgm stack reset connection
								break;
							}
							

							if (length < 0) {
								// End-of-stream reached
								break;
							}

							p = Parcel.obtain();
							p.unmarshall(buffer, 0, length);
							p.setDataPosition(0);

							// Log.v(LOG_TAG, "Read packet: " + length +
							// " bytes");

							processResponse(p);
							p.recycle();
						}

					} catch (java.io.IOException ex) {

						Log.i(LOG_TAG, "'" + TGM_SOCKET_NAME
								+ "' socket closed", ex);

					} catch (Throwable tr) {

						Log.e(LOG_TAG, "Uncaught exception read length="
								+ length + "Exception:" + tr.toString());

					}

					Log.i(LOG_TAG, "Disconnected from '" + TGM_SOCKET_NAME
							+ "' socket");

					// TODO Do something here
					// setRadioState(RadioState.RADIO_UNAVAILABLE);

					// Close the socket gracefully
					try {

						mSocket.close();

					} catch (IOException ex) {

					}

					mSocket = null;

					TGMRequest.resetSerial();

					// Clear request list on close
					synchronized (mRequestsList) {

						for (int i = 0, sz = mRequestsList.size(); i < sz; i++) {

							TGMRequest rr = mRequestsList.get(i);

							rr.onError(TGMConstants.RADIO_NOT_AVAILABLE);
							rr.release();

						}

						mRequestsList.clear();
					}

				}// End of for loop

			} catch (Exception e) {

				Log.e("", e.getMessage());

				e.printStackTrace();

			}

		}

	}

}

class TGMRequest {

	static final String LOG_TAG = "TGMJ";

	// ***** Class Variables
	static int sNextSerial = 0;
	static Object sSerialMonitor = new Object();
	private static Object sPoolSync = new Object();
	private static TGMRequest sPool = null;
	private static int sPoolSize = 0;
	private static final int MAX_POOL_SIZE = 4;

	// ***** Instance Variables
	int mSerial;
	int mRequest;
	Message mResult;
	Parcel mp;
	TGMRequest mNext;

	/**
	 * Retrieves a new RILRequest instance from the pool.
	 * 
	 * @param request
	 *            RIL_REQUEST_*
	 * @param result
	 *            sent when operation completes
	 * @return a RILRequest instance from the pool.
	 */
	static TGMRequest obtain(int request, Message result) {

		TGMRequest tr = null;

		synchronized (sPoolSync) {
			if (sPool != null) {
				tr = sPool;
				sPool = tr.mNext;
				tr.mNext = null;
				sPoolSize--;
			}
		}

		if (tr == null) {
			tr = new TGMRequest();
		}

		synchronized (sSerialMonitor) {
			tr.mSerial = sNextSerial++;
		}
		tr.mRequest = request;
		tr.mResult = result;
		tr.mp = Parcel.obtain();

		if (result != null && result.getTarget() == null) {
			throw new NullPointerException("Message target must not be null");
		}

		// first elements in any RIL Parcel
		tr.mp.writeInt(request);
		tr.mp.writeInt(tr.mSerial);

		return tr;
	}

	/**
	 * Returns a TGMRequest instance to the pool.
	 * 
	 * Note: This should only be called once per use.
	 */
	
	void release() {

		synchronized (sPoolSync) {
			if (sPoolSize < MAX_POOL_SIZE) {
				this.mNext = sPool;
				sPool = this;
				sPoolSize++;
			}
		}
	}

	private TGMRequest() {
		
	}

	static void resetSerial() {
		
		synchronized (sSerialMonitor) {
			sNextSerial = 0;
		}
		
	}

	String serialString() {
		// Cheesy way to do %04d
		StringBuilder sb = new StringBuilder(8);
		String sn;

		sn = Integer.toString(mSerial);

		// sb.append("J[");
		sb.append('[');
		for (int i = 0, s = sn.length(); i < 4 - s; i++) {
			sb.append('0');
		}

		sb.append(sn);
		sb.append(']');
		return sb.toString();
	}

	void onError(int error) {

		CommandException ex;

		ex = CommandException.fromRilErrno(error);

		// if(TGM.RILJ_LOGD)
		// Log.d(LOG_TAG, serialString() + "< "
		// + RIL.requestToString(mRequest) + " error: " + ex);

		if (mResult != null) {
			AsyncResult.forMessage(mResult, null, ex);
			mResult.sendToTarget();
		}

		if (mp != null) {
			mp.recycle();
			mp = null;
		}
	}
}
