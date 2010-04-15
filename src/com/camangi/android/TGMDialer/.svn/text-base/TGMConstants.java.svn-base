/*
 * Copyright (C) 2006 The Android Open Source Project
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

package com.camangi.android.TGMDialer;


/**
 * {@hide}
 */
interface TGMConstants
{
    // From the top of ril.cpp
    int TGM_ERRNO_INVALID_RESPONSE = -1;

    // from TGM_Errno
    int SUCCESS = 0;
    int RADIO_NOT_AVAILABLE = 1;     /* If radio did not start or is resetting */
    int GENERIC_FAILURE = 2;
    int PASSWORD_INCORRECT = 3;      /* for PIN/PIN2 methods only! */
    int SIM_PIN2 = 4;                /* Operation requires SIM PIN2 to be entered */
    int SIM_PUK2 = 5;                /* Operation requires SIM PIN2 to be entered */
    int REQUEST_NOT_SUPPORTED = 6;
    int REQUEST_CANCELLED = 7;
    int OP_NOT_ALLOWED_DURING_VOICE_CALL = 8; /* data operation is not allowed during voice call in class C */
    int OP_NOT_ALLOWED_BEFORE_REG_NW = 9;     /* request is not allowed before device registers to network */
    int SMS_SEND_FAIL_RETRY = 10;         /* send sms fail and need retry */
    int OP_NOT_ALLOWED_DURING_PPPD = 11;
    
    
    int TGM_STATE_OFF = 0;          /* Radio explictly powered off (eg CFUN=0) */
    int TGM_STATE_UNAVAILABLE = 1;  /* Radio unavailable (eg, resetting or not booted) */
    int TGM_STATE_SIM_NOT_READY = 2;      /* Radio is on, but the SIM interface is not ready */
    int TGM_STATE_SIM_LOCKED_OR_ABSENT = 3; /* SIM PIN locked, PUK required, network personalization locked, or SIM absent */
    int TGM_STATE_SIM_READY = 4;          /* Radio is on and SIM interface is available */
    int TGM_STATE_PPPD = 5;			/* Now the control is handover to pppd*/
    int TGM_STATE_PPPD_READY = 6;
    
    //SIM STATUS
    enum SimStatus{
    	
    	SIM_ABSENT,
    	SIM_NOT_READY,
    	SIM_READY,
    	SIM_PIN,
    	SIM_PUK,
    	SIM_NETWORK_PERSONALIZATION
    	
    }
    
    int TGM_SIM_ABSENT = 0;
    int TGM_SIM_NOT_READY = 1;
    int TGM_SIM_READY = 2;
    int TGM_SIM_PIN = 3;
    int TGM_SIM_PUK = 4;
    int TGM_SIM_NETWORK_PERSONALIZATION = 5;
    
    /** 
     * No restriction at all including voice/SMS/USSD/SS/AV64 
     * and packet data.
     */   
    int TGM_RESTRICTED_STATE_NONE = 0x00;    
    /** 
     * Block emergency call due to restriction. 
     * But allow all normal voice/SMS/USSD/SS/AV64. 
     */
    int TGM_RESTRICTED_STATE_CS_EMERGENCY = 0x01;
    /** 
     * Block all normal voice/SMS/USSD/SS/AV64 due to restriction. 
     * Only Emergency call allowed. 
     */
    int TGM_RESTRICTED_STATE_CS_NORMAL = 0x02;
    /** 
     * Block all voice/SMS/USSD/SS/AV64 
     * including emergency call due to restriction.
     */
    int TGM_RESTRICTED_STATE_CS_ALL = 0x04;
    /** 
     * Block packet data access due to restriction. 
     */  
    int TGM_RESTRICTED_STATE_PS_ALL = 0x10;
    
 
	
	int TGM_REQUEST_SET_PDP_CONTEXT = 1;
	
	int TGM_REQUEST_SET_PIN_CODE = 2;
	
	int TGM_REQUEST_DIAL_DATA_NETWORK = 3;
	
	int TGM_REQUEST_DISCONNECT_DATA_NETWORK = 4;
	
	int TGM_REQUEST_QUERY_SIGNAL_STRENGTH = 5;
	
	int TGM_REQUEST_QUERY_IMEI = 6;
	
	int TGM_REQUEST_QUERY_MANUFACTURER = 7;
	
	int TGM_REQUEST_QUERY_OPERATOR = 8;
	
	int TGM_REQUEST_QUERY_PDP_CONTEXT_LIST = 9;
    
    int TGM_REQUEST_QUERY_MODEM_MODEL = 10;
    
    int TGM_REQUEST_QUERY_SIM_STATUS = 11;
    
    int TGM_REQUEST_QUERY_IMSI = 12;
    
    int TGM_REQUEST_QUERY_NUMBER = 13;
    
    int TGM_REQUEST_QUERY_STATE = 14;
    
    int TGM_REQUEST_RESET_STACK = 15;
    
    int TGM_REQUEST_SET_PPPD_AUTH = 16;
    
    
    
    ///UNSOLICITED MESSAGE
    
    int TGM_UNSOLICITED_BASE = 1000;
    
    int TGM_UNSOLICITED_DEVICE_DETECTED = 1000;
    
    int TGM_UNSOLICITED_DEVICE_REMOVED = 1001;
    
    int TGM_UNSOLICITED_SIGNAL_STRENGTH = 1002;
    
    int TGM_UNSOLICITED_RADIO_STATE_CHANGED = 1003;
    
    int TGM_UNSOLICITED_PPPD_CONNECTED = 1004;
    
    int TGM_UNSOLICITED_PPPD_DISCONNECTED = 1005;
    
    int TGM_UNSOLICITED_PPPD_FAILED = 1006;
    
}
