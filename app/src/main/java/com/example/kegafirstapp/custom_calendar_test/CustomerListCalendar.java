package com.example.kegafirstapp.custom_calendar_test;

/**
 * Created by Kega on 9/22/2016.
 */
import android.app.ListActivity;
import android.graphics.Color;
import android.os.Bundle;

import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

/**
 * Created by Kega on 9/15/2016.
 */
public class CustomerListCalendar extends ListActivity {

    String passedVar, query;
    private static final String TAG_NAMA = "Nama";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.customer_list_calendar);

        ArrayList<String> dataList = new ArrayList<String>();
        dataList = getIntent().getStringArrayListExtra("data");

        fillList(dataList);


    }

    private void fillList(ArrayList<String> paramList) {
        ArrayList<HashMap<String, String>> customerList = new ArrayList<HashMap<String, String>>();
        for (int i = 0; i < paramList.size(); i++) {
            HashMap<String, String> map = new HashMap<String, String>();
            map.put(TAG_NAMA, paramList.get(i));
            customerList.add(map);
        }

        ListAdapter NoCoreAdapter = new SimpleAdapter(CustomerListCalendar.this, customerList,
                R.layout.custom_list, new String[]{TAG_NAMA}, new int[]{R.id.list_text});
        setListAdapter(NoCoreAdapter);

    }
}