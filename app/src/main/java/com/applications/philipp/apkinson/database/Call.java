package com.applications.philipp.apkinson.database;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Philipp on 09.07.2016.
 */
public class Call {
    private int callID;
    private Date callDATE;
    private boolean inCONTACTLIST;
    private int durationSECONDS;
    private boolean callOUTGOING;
    private File callRECORD;
    private String callRESULT = "";

    /**
     * @param date        Time and Date of the call.
     * @param contactList Is the number from the list of contacts?
     * @param outgoing    True for outgoing call.
     * @param record      The path to the recorded audio file.
     */
    public Call(Date date, boolean contactList, boolean outgoing, File record) {
        callID = 0;
        callDATE = date;
        inCONTACTLIST = contactList;
        callOUTGOING = outgoing;
        callRECORD = record;
    }


    public int getCallID() {
        return callID;
    }

    public void setCallID(int id) {
        callID = id;
    }

    public Date getCallDATE() {
        return callDATE;
    }

    public boolean isInCONTACTLIST() {
        return inCONTACTLIST;
    }

    public int getDurationSECONDS() {
        return durationSECONDS;
    }

    public boolean isCallOUTGOING() {
        return callOUTGOING;
    }

    public File getCallRECORD() {
        return callRECORD;
    }

    public String getDateAsString() {
        SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        return format.format(callDATE);
    }

    @Override
    public String toString() {
        String inOrOut = "";
        if (isCallOUTGOING()) {
            inOrOut = "Outgoing";
        } else {
            inOrOut = "Incoming";
        }
        String inContactList = "";
        if (isInCONTACTLIST()) {
            inContactList = "Yes";
        } else {
            inContactList = "No";
        }
        return getCallRESULT();
        //return inOrOut + " Call #" + callID + " from\n" + getDateAsString() + "\nDuration: "
        //        + getDurationSECONDS() + " Seconds" + "\nFrom Contact List: " + inContactList;
    }

    public String getCallRESULT() {
        return callRESULT;
    }

    public void setCallRESULT(String callRESULT) {
        this.callRESULT = callRESULT;
    }

    public void setDurationSECONDS(int durationSECONDS) {
        this.durationSECONDS = durationSECONDS;
    }
}
