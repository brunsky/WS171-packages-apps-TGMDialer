/*
 * Copyright (C) 2007 The Android Open Source Project
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

import android.util.Log;

/**
 * {@hide}
 */
public class CommandException extends RuntimeException
{
    private Error e;

    public enum Error {
        INVALID_RESPONSE,
        RADIO_NOT_AVAILABLE,
        GENERIC_FAILURE,
        PASSWORD_INCORRECT,
        SIM_PIN2,
        SIM_PUK2,
        REQUEST_NOT_SUPPORTED,
        OP_NOT_ALLOWED_DURING_VOICE_CALL,
        OP_NOT_ALLOWED_BEFORE_REG_NW,
        SMS_FAIL_RETRY,
    }

    public CommandException(Error e)
    {
        super(e.toString());
        this.e = e;
    }

    public static CommandException fromRilErrno(int tgm_errno)
    {
        switch(tgm_errno) {
        
            case TGMConstants.SUCCESS:  return null;
            
            case TGMConstants.TGM_ERRNO_INVALID_RESPONSE:    
                return new CommandException(Error.INVALID_RESPONSE);
            
            case TGMConstants.RADIO_NOT_AVAILABLE:           
                return new CommandException(Error.RADIO_NOT_AVAILABLE);
            
            case TGMConstants.GENERIC_FAILURE:               
                return new CommandException(Error.GENERIC_FAILURE);
            
            case TGMConstants.PASSWORD_INCORRECT:            
                return new CommandException(Error.PASSWORD_INCORRECT);
            
            case TGMConstants.SIM_PIN2:                      
                return new CommandException(Error.SIM_PIN2);
            
            case TGMConstants.SIM_PUK2:                      
                return new CommandException(Error.SIM_PUK2);
            
            case TGMConstants.REQUEST_NOT_SUPPORTED:         
                return new CommandException(Error.REQUEST_NOT_SUPPORTED);
            
            case TGMConstants.OP_NOT_ALLOWED_DURING_VOICE_CALL:
                return new CommandException(Error.OP_NOT_ALLOWED_DURING_VOICE_CALL);
            
            case TGMConstants.OP_NOT_ALLOWED_BEFORE_REG_NW:
                return new CommandException(Error.OP_NOT_ALLOWED_BEFORE_REG_NW);
            
            case TGMConstants.SMS_SEND_FAIL_RETRY:
                return new CommandException(Error.SMS_FAIL_RETRY);
            
            default:
                Log.e("GSM", "Unrecognized TGM errno " + tgm_errno);
                return new CommandException(Error.INVALID_RESPONSE);
                
        }
    }

    public Error getCommandError()
    {
        return e;
    }



}
