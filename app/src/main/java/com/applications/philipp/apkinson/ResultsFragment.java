package com.applications.philipp.apkinson;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Shader;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.androidplot.xy.LineAndPointFormatter;
import com.androidplot.xy.SimpleXYSeries;
import com.androidplot.xy.XYPlot;
import com.androidplot.xy.XYSeries;
import com.androidplot.xy.XYStepMode;
import com.applications.philipp.apkinson.Lists.ArrayListAdapter;
import com.applications.philipp.apkinson.Lists.ListItemDialog;
import com.applications.philipp.apkinson.database.ApkinsonSQLiteHelper;
import com.applications.philipp.apkinson.database.Call;

import java.io.File;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by Philipp on 18.06.2016.
 */
public class ResultsFragment extends Fragment {
    private Button updateButton;
    private XYPlot plot;
    private LineAndPointFormatter series1Format;
    ListView listView;
    ArrayListAdapter adapter;
    ApkinsonSQLiteHelper database;

    public ResultsFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_results, container, false);
        database = new ApkinsonSQLiteHelper(getActivity().getApplicationContext());
        return rootView;
    }

    @Override
    public void onResume(){
        super.onResume();
        try {
            final ArrayList<Call> calls = database.getAllCalls();
            if(adapter.getCount() != calls.size()){
                adapter = new ArrayListAdapter(getActivity(), calls);
                listView.setAdapter(adapter);
                listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        FragmentTransaction ft = getFragmentManager().beginTransaction();
                        Fragment prev = getFragmentManager().findFragmentByTag("dialog");
                        if (prev != null) {
                            ft.remove(prev);
                        }
                        ft.addToBackStack(null);

                        // Create and show the dialog.
                        ListItemDialog newFragment = ListItemDialog.newInstance(getActivity(),calls.get(calls.size()-1-position));
                        newFragment.show(ft, "dialog");
                    }
                });
            }
        } catch (NullPointerException e){
            e.printStackTrace();
        }
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        listView = (ListView) view.findViewById(R.id.listView);

        final ArrayList<Call> calls = database.getAllCalls();

        adapter = new ArrayListAdapter(getActivity(), calls);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                FragmentTransaction ft = getFragmentManager().beginTransaction();
                Fragment prev = getFragmentManager().findFragmentByTag("dialog");
                if (prev != null) {
                    ft.remove(prev);
                }
                ft.addToBackStack(null);

                // Create and show the dialog.
                ListItemDialog newFragment = ListItemDialog.newInstance(getActivity(),calls.get(calls.size()-1-position));
                newFragment.show(ft, "dialog");
            }
        });

        /*
        addButton();
        File fileRMS = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC), "rmsValues.data");
        RandomAccessFile rafRMS = null;
        try {
            rafRMS = new RandomAccessFile(fileRMS.getAbsolutePath(), "r");
            FileChannel channel = rafRMS.getChannel();
            ByteBuffer head = ByteBuffer.allocate(8);
            channel.read(head);
            head.rewind();
            double numberOfSamples[] = new double[1];
            head.asDoubleBuffer().get(numberOfSamples);
            int size = (int) Math.round(numberOfSamples[0]);
            double values[] = new double[size];
            ByteBuffer data = ByteBuffer.allocate(8 * size);
            channel.read(data);
            data.rewind();
            data.asDoubleBuffer().get(values);
            channel.close();
            Number xValues[] = new Number[size];
            Number yValues[] = new Number[size];
            for (int i = 0; i < size; i++) {
                xValues[i] = 0.02d * i;
                yValues[i] = values[i];
            }
            XYSeries series = new SimpleXYSeries(Arrays.asList(xValues), Arrays.asList(yValues), "RootMeanSquare");
            plot = (XYPlot) getView().findViewById(R.id.plot);
            plot.setDomainStep(XYStepMode.INCREMENT_BY_VAL, 1);
            plot.setDomainValueFormat(new DecimalFormat("#"));
            plot.setRangeStep(XYStepMode.SUBDIVIDE, 4);
            plot.setRangeValueFormat(new DecimalFormat("#"));
            plot.setDomainLabel("Time in seconds");
            plot.setRangeLabel("Amplitude");
            plot.getLegendWidget().setVisible(false);
            plot.setPlotMarginBottom(0);
            plot.setPlotMarginLeft(0);
            plot.setPlotMarginRight(0);
            plot.setPlotMarginTop(0);
            series1Format = new LineAndPointFormatter(
                    Color.rgb(0, 152, 136),                   // line color
                    null,                   // point color
                    Color.WHITE,                                   // fill color (none)
                    null);
            plot.addSeries(series, series1Format);
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
        */
    }

    private void addButton() {
        /*
        updateButton = (Button) getActivity().findViewById(R.id.button);
        updateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                File fileRMS = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC), "rmsValues.data");
                RandomAccessFile rafRMS = null;
                ApkinsonSQLiteHelper database = new ApkinsonSQLiteHelper(getActivity().getApplicationContext());
                ArrayList<Call> calls = database.getAllCalls();

                TextView textView = (TextView) getActivity().findViewById(R.id.textView);
                if (calls.size() != 0) {
                    textView.setText(calls.get(calls.size() - 1).toString());
                }
                try {
                    rafRMS = new RandomAccessFile(fileRMS.getAbsolutePath(), "r");
                    FileChannel channel = rafRMS.getChannel();
                    ByteBuffer head = ByteBuffer.allocate(8);
                    channel.read(head);
                    head.rewind();
                    double numberOfSamples[] = new double[1];
                    head.asDoubleBuffer().get(numberOfSamples);
                    int size = (int) Math.round(numberOfSamples[0]);
                    double values[] = new double[size];
                    ByteBuffer data = ByteBuffer.allocate(8 * size);
                    channel.read(data);
                    data.rewind();
                    data.asDoubleBuffer().get(values);
                    channel.close();
                    Number xValues[] = new Number[size];
                    Number yValues[] = new Number[size];
                    for (int i = 0; i < size; i++) {
                        xValues[i] = 0.02d * i;
                        yValues[i] = values[i];
                    }
                    plot.clear();
                    XYSeries series = new SimpleXYSeries(Arrays.asList(xValues), Arrays.asList(yValues), "RootMeanSquare");
                    plot.addSeries(series, series1Format);
            /*
            series1Format.setFillPaint(lineFill);
            series1Format.setPointLabeler(null);
            series1Format.getLinePaint().setPathEffect(
                    new DashPathEffect(new float[] {

                            // always use DP when specifying pixel sizes, to keep things consistent across devices:
                            PixelUtils.dpToPix(20),
                            PixelUtils.dpToPix(15)}, 0));

                    //series1Format.setInterpolationParams(
                    //        new CatmullRomInterpolator.Params(10, CatmullRomInterpolator.Type.Centripetal));
                    //series1Format.setPointLabelFormatter(new PointLabelFormatter());
                    //series1Format.configure(getActivity().getApplicationContext(),
                    //        R.xml.line_point_formatter_with_labels);
                    plot.redraw();

                } catch (Exception e) {
                    e.printStackTrace();
                    return;
                }
            }
        });
    }
                */
    }
}
