package com.applications.philipp.apkinson.Lists;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.MediaController;
import android.widget.TextView;

import com.androidplot.xy.LineAndPointFormatter;
import com.androidplot.xy.SimpleXYSeries;
import com.androidplot.xy.XYPlot;
import com.androidplot.xy.XYSeries;
import com.androidplot.xy.XYStepMode;
import com.applications.philipp.apkinson.R;
import com.applications.philipp.apkinson.database.Call;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.text.DecimalFormat;
import java.util.Arrays;

/**
 * Created by Philipp on 18.08.2016.
 */
public class ListItemDialog extends DialogFragment {

    public static ListItemDialog newInstance(Context context, Call call) {
        ListItemDialog f = new ListItemDialog();

        // Supply num input as an argument.
        Bundle args = new Bundle();
        args.putInt("id", call.getCallID());
        String date = call.getCallDATE().toString();
        String[] elements = date.split(" ");
        date = elements[2]+ ". " + elements[1] + " " + elements[5] + " " + elements[3];
        args.putString("date", date);
        args.putInt("duration", call.getDurationSECONDS());
        if (call.isCallOUTGOING()) {
            args.putString("direction", context.getResources().getString(R.string.outgoing));
        } else {
            args.putString("direction", context.getResources().getString(R.string.incoming));
        }
        if (call.isInCONTACTLIST()) {
            args.putString("contact", context.getResources().getString(R.string.yes));
        } else {
            args.putString("contact", context.getResources().getString(R.string.no));
        }
        args.putString("file", call.getCallRECORD().getName());
        args.putString("filePath", call.getCallRECORD().toString());
        args.putString("result", call.getCallRESULT());
        f.setArguments(args);
        return f;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.list_item_dialog, container);
        TextView date = (TextView) view.findViewById(R.id.valueDate);
        TextView duration = (TextView) view.findViewById(R.id.valueDuration);
        TextView direction = (TextView) view.findViewById(R.id.valueDirection);
        TextView contacts = (TextView) view.findViewById(R.id.valueContacts);
        TextView file = (TextView) view.findViewById(R.id.valueFile);
        TextView result = (TextView) view.findViewById(R.id.valueResult);
        date.setText(getArguments().getString("date"));
        duration.setText(String.valueOf(getArguments().getInt("duration")) + " " + getActivity().getResources().getString(R.string.time_seconds));
        direction.setText(getArguments().getString("direction"));
        contacts.setText(getArguments().getString("contact"));
        file.setText(getArguments().getString("file"));
        result.setText(getArguments().getString("result"));
        getDialog().setTitle(getActivity().getResources().getString(R.string.call) + " " + getArguments().getInt("id"));

        WebView webView = (WebView) view.findViewById(R.id.webView);
        webView.getSettings().setLoadWithOverviewMode(true);
        //webView.getSettings().setUseWideViewPort(true);

        webView.loadUrl("file:///" + getArguments().getString("filePath"));

        return view;
    }
}
