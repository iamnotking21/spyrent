package com.example.spyrentv1;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.spyrentv1.R;

import java.util.ArrayList;

public class history_sites_adapter extends BaseAdapter {

    Context context;
    String domain[],domainid[];
    int parentid,childid;
    LayoutInflater inflater;

    public history_sites_adapter(Context acontext, ArrayList<String> domain, ArrayList<String> domainid, int parentid, int childid){
        this.context = acontext;
        this.parentid = parentid;
        this.childid = childid;
        this.domainid = domainid.toArray(new String[0]);
        this.domain = domain.toArray(new String[0]);
        inflater = (LayoutInflater.from(acontext));
    }


    @Override
    public int getCount() {
        return domain.length;
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(final int i, View view, ViewGroup parent) {
        view = inflater.inflate(R.layout.history_sites_content_main,null);

        final TextView app_name = (TextView) view.findViewById(R.id.webtextview1);
        final TextView app_dates = (TextView) view.findViewById(R.id.current_dates);

        app_name.setText(domain[i]);
        app_dates.setText(domainid[i]);

        return view;
    }
}
