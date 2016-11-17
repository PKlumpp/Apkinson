package com.applications.philipp.apkinson.Lists;

import android.content.Context;
import android.content.res.Configuration;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.applications.philipp.apkinson.R;
import com.applications.philipp.apkinson.database.Call;

import java.util.ArrayList;

/**
 * Created by Philipp on 18.08.2016.
 */
public class ArrayListAdapter extends ArrayAdapter<Call> {
    private Context context;
    private ArrayList<Call> calls;

    public ArrayListAdapter(Context context, ArrayList<Call> calls){
        super(context, -1, calls);
        this.context = context;
        this.calls = calls;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent){
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View listItemView = inflater.inflate(R.layout.list_item_layout, parent, false);
        ImageView imageView = (ImageView) listItemView.findViewById(R.id.imageView);
        TextView title = (TextView) listItemView.findViewById(R.id.itemTitle);
        TextView date = (TextView) listItemView.findViewById(R.id.itemDate);
        int numberOfCalls = calls.size();
        if (calls.get(numberOfCalls - position - 1).isCallOUTGOING()){
            imageView.setImageDrawable(getContext().getResources().getDrawable(R.drawable.callout));
        } else {
            imageView.setImageDrawable(getContext().getResources().getDrawable(R.drawable.callin));
        }
        title.setText(context.getResources().getString(R.string.call) + " " + calls.get(numberOfCalls - position - 1).getCallID());
        date.setText(calls.get(numberOfCalls - position - 1).getDateAsString());
        return listItemView;
    }
}
