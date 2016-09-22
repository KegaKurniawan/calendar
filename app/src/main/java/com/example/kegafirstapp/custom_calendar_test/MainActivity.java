package com.example.kegafirstapp.custom_calendar_test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {

    public GregorianCalendar month, itemmonth;// calendar instances.

    public CalendarAdapter adapter;// adapter instance
    public Handler handler;// for grabbing some event values for showing the dot
    // marker.
    public ArrayList<String> items; // container to store calendar items which
    // needs showing the event marker

    private ProgressDialog pDialog;
    ConnectionClass connectionClass;
    PreparedStatement stmt;
    ResultSet rs;
    String query, selectedGridDate;
    ArrayList<String> dateList;
    ArrayList<Integer> amount;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.calendar_custom);

        Locale.setDefault( Locale.US );
        month = (GregorianCalendar) GregorianCalendar.getInstance();
        itemmonth = (GregorianCalendar) month.clone();

        items = new ArrayList<String>();
        //new GetMonthFill().execute();
        getMonthEvent();
        adapter = new CalendarAdapter(this, month, dateList,amount);

        GridView gridview = (GridView) findViewById(R.id.calendar_grid);
        gridview.setAdapter(adapter);

        handler = new Handler();
        handler.post(calendarUpdater);



        TextView title = (TextView) findViewById(R.id.calendar_date_display);
        title.setText(android.text.format.DateFormat.format("MMMM yyyy", month));

        ImageView previous = (ImageView) findViewById(R.id.calendar_prev_button);
        previous.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                setPreviousMonth();
                new GetMonthFill().execute();
            }
        });

        ImageView next = (ImageView) findViewById(R.id.calendar_next_button);
        next.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                setNextMonth();
                new GetMonthFill().execute();
            }
        });

        gridview.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v,
                                    int position, long id) {

                ((CalendarAdapter) parent.getAdapter()).setSelected(v);
                selectedGridDate = CalendarAdapter.dayString
                        .get(position);
                String[] separatedTime = selectedGridDate.split("-");
                String gridvalueString = separatedTime[2].replaceFirst("^0*",
                        "");// taking last part of date. ie; 2 from 2012-12-02.
                int gridvalue = Integer.parseInt(gridvalueString);
                // navigate to next or previous month on clicking offdays.
                if ((gridvalue > 10) && (position < 8)) {
                    setPreviousMonth();
                    new GetMonthFill().execute();
                } else if ((gridvalue < 7) && (position > 28)) {
                    setNextMonth();
                    new GetMonthFill().execute();
                }
                //((CalendarAdapter) parent.getAdapter().setSelected(v));

                new GetCustomerListCalendar().execute();

            }
        });
    }

    protected void setNextMonth() {
        if (month.get(GregorianCalendar.MONTH) == month.getActualMaximum(GregorianCalendar.MONTH)) {
            month.set((month.get(GregorianCalendar.YEAR) + 1),month.getActualMinimum(GregorianCalendar.MONTH), 1);
        } else {
            month.set(GregorianCalendar.MONTH, month.get(GregorianCalendar.MONTH) + 1);
        }

    }

    protected void setPreviousMonth() {
        if (month.get(GregorianCalendar.MONTH) == month
                .getActualMinimum(GregorianCalendar.MONTH)) {
            month.set((month.get(GregorianCalendar.YEAR) - 1),
                    month.getActualMaximum(GregorianCalendar.MONTH), 1);
        } else {
            month.set(GregorianCalendar.MONTH,
                    month.get(GregorianCalendar.MONTH) - 1);
        }

    }

    protected void showToast(String string) {
        Toast.makeText(this, string, Toast.LENGTH_SHORT).show();

    }

    public void refreshCalendar() {
        TextView title = (TextView) findViewById(R.id.calendar_date_display);
        //new GetMonthFill().execute();
        //getMonthEvent();
        adapter.refreshDays(dateList,amount);
        adapter.notifyDataSetChanged();
        handler.post(calendarUpdater); // generate some calendar items

        title.setText(android.text.format.DateFormat.format("MMMM yyyy", month));
    }

    public Runnable calendarUpdater = new Runnable() {

        @Override
        public void run() {
            items.clear();

            // Print dates of the current week
            DateFormat df = new SimpleDateFormat("yyyy-MM-dd",Locale.US);
            String itemvalue;
            for (int i = 0; i < 7; i++) {
                itemvalue = df.format(itemmonth.getTime());
                itemmonth.add(GregorianCalendar.DATE, 1);
                items.add("2012-09-12");
                items.add("2012-10-07");
                items.add("2012-10-15");
                items.add("2012-10-20");
                items.add("2012-11-30");
                items.add("2012-11-28");
            }

            adapter.setItems(items);
            adapter.notifyDataSetChanged();
        }
    };

    public class GetMonthFill extends AsyncTask<Void, Void, ArrayList<String>> {
        String bulan, tahun;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            bulan = new String(android.text.format.DateFormat.format("MM", month).toString());
            tahun = new String(android.text.format.DateFormat.format("yyyy", month).toString());

            pDialog = new ProgressDialog(MainActivity.this);
            pDialog.setMessage("Loading Month Event. Please wait...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(true);
            pDialog.show();

        }

        @Override
        protected ArrayList<String> doInBackground(Void... params) {

            dateList = new ArrayList<String>();
            amount = new ArrayList<Integer>();

            connectionClass = new ConnectionClass();
            query = "Select substring(convert(varchar,[Tanggal Aktivitas],126),9, 2) as tanggal, count([Tanggal Aktivitas]) as jumlah  From\n" +
                    "(SELECT [Bill-to Name],[Tanggal Aktivitas] FROM [JOGJA BAY - Live].[dbo].[PT_ TAMAN WISATA JOGJA$Sales Invoice Header]\n" +
                    "UNION\n" +
                    "SELECT [Bill-to Name],[Tanggal Aktivitas] FROM [JOGJA BAY - Live].[dbo].[PT_ TAMAN WISATA JOGJA$Sales Header]) as t1\n" +
                    "where month(t1.[Tanggal Aktivitas]) = '"+bulan+"' and year(t1.[Tanggal Aktivitas]) = '"+tahun+"' group by t1.[Tanggal Aktivitas]";
            System.out.println(query);
            try {

                Connection con = connectionClass.CONN();
                stmt = con.prepareStatement(query);
                rs = stmt.executeQuery();
                while (rs.next()) {
                    int date_act = rs.getInt("tanggal");
                    String tanggal = null;
                    tanggal = String.valueOf(date_act);
                    dateList.add(tanggal);
                    int jumlah = rs.getInt("jumlah");
                    amount.add(jumlah);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return dateList;
        }

        @Override
        protected void onPostExecute(ArrayList<String> dateList) {
            pDialog.dismiss();
            System.out.println("ini isi date list : "+dateList);
            System.out.println("ini amount : "+amount);
            refreshCalendar();
        }
    }

    private void getMonthEvent(){
        dateList = new ArrayList<String>();
        amount = new ArrayList<Integer>();
        connectionClass = new ConnectionClass();
        String bulan = new String(android.text.format.DateFormat.format("MM", month).toString());
        String tahun = new String(android.text.format.DateFormat.format("yyyy", month).toString());
        /*query = "SELECT [Tanggal Aktivitas]\n" +
                "FROM [JOGJA BAY - Live].[dbo].[PT_ TAMAN WISATA JOGJA$Sales Header] " +
                "where month([Tanggal Aktivitas]) = '"+bulan+"' and year([Tanggal Aktivitas]) = '"+tahun+"'";
        */
        query = "Select substring(convert(varchar,[Tanggal Aktivitas],126),9, 2) as tanggal, count([Tanggal Aktivitas]) as jumlah  From\n" +
                "(SELECT [Bill-to Name],[Tanggal Aktivitas] FROM [JOGJA BAY - Live].[dbo].[PT_ TAMAN WISATA JOGJA$Sales Invoice Header]\n" +
                "UNION\n" +
                "SELECT [Bill-to Name],[Tanggal Aktivitas] FROM [JOGJA BAY - Live].[dbo].[PT_ TAMAN WISATA JOGJA$Sales Header]) as t1\n" +
                "where month(t1.[Tanggal Aktivitas]) = '"+bulan+"' and year(t1.[Tanggal Aktivitas]) = '"+tahun+"' group by t1.[Tanggal Aktivitas]";

        System.out.println(query);
        try{

            Connection con = connectionClass.CONN();
            stmt = con.prepareStatement(query);
            rs = stmt.executeQuery();
            while (rs.next()){
                int date_act = rs.getInt("tanggal");
                String tanggal = null;
                tanggal = String.valueOf(date_act);
                dateList.add(tanggal);
                int jumlah = rs.getInt("jumlah");
                amount.add(jumlah);
            }
        }catch(Exception e){
            e.printStackTrace();
        }

        System.out.println("ini isi listnya : "+dateList);
        System.out.println("ini isi amount : "+amount);
    }

    public class GetCustomerListCalendar extends AsyncTask<Void, Void, ArrayList<String>> {
        ArrayList<String> dateCustomerList;


        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(MainActivity.this);
            pDialog.setMessage("Loading customer data. Please wait...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(false);
            pDialog.show();

        }

        @Override
        protected ArrayList<String> doInBackground(Void... params) {

            dateCustomerList = new ArrayList<String>();

            connectionClass = new ConnectionClass();
            query = "Select [Bill-to Name] From\n" +
                    "(SELECT [Bill-to Name],[Tanggal Aktivitas] FROM [JOGJA BAY - Live].[dbo].[PT_ TAMAN WISATA JOGJA$Sales Invoice Header]\n" +
                    "UNION\n" +
                    "SELECT [Bill-to Name],[Tanggal Aktivitas] FROM [JOGJA BAY - Live].[dbo].[PT_ TAMAN WISATA JOGJA$Sales Header]) as t1\n" +
                    "where t1.[Tanggal Aktivitas] = '"+selectedGridDate+"'";
            try {
                Connection con = connectionClass.CONN();
                stmt = con.prepareStatement(query);
                rs = stmt.executeQuery();
                while (rs.next()) {
                    String date_act = rs.getString("Bill-to Name");
                    dateCustomerList.add(date_act);

                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return dateCustomerList;
        }

        @Override
        protected void onPostExecute(ArrayList<String> resultList) {
            pDialog.dismiss();
            Intent in = new Intent(getApplicationContext(), CustomerListCalendar.class);
            in.putStringArrayListExtra("data", resultList);
            startActivity(in);
        }

    }
}