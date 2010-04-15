package com.camangi.android.TGMDialer;

import java.util.Date;

public class TGMDataStore {
	
	public String[] dongleModel = { "", "" };
	
	public String imsi;
	
	public String imei;
	
	public String apn;
	
	public String pppdUsername;
	
	public String pppdPassword;
	
	public int currentRadioState;
	
	public int pinCountRemain = 3;
	
	public Date connectionDate;
	
	public boolean previousWifiSetting;
	//public boolean isPPPDConnected = false;
	
	//public boolean isDialed = false;
	

}
