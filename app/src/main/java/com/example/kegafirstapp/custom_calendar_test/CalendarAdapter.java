package com.example.kegafirstapp.custom_calendar_test;

/**
 * Created by Kega on 9/20/2016.
 */
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;

public class CalendarAdapter extends BaseAdapter {
    private Context mContext;

    private java.util.Calendar month;
    public GregorianCalendar pmonth; // calendar instance for previous month
    /**
     * calendar instance for previous month for getting complete view
     */
    public GregorianCalendar pmonthmaxset;
    private GregorianCalendar selectedDate;
    int firstDay;
    int maxWeeknumber;
    int maxP;
    int calMaxP;
    int lastWeekDay;
    int leftDays;
    int mnthlength;
    int datelistsize;
    int cek;
    String itemvalue, curentDateString;
    DateFormat df;

    TextView fill_event;
    LinearLayout event_num;

    private ArrayList<String> items,dateList;
    private ArrayList<Integer> amount;
    public static List<String> dayString;
    private View previousView;

    public CalendarAdapter(Context c, GregorianCalendar monthCalendar, ArrayList<String> resultList, ArrayList<Integer> sum) {
        CalendarAdapter.dayString = new ArrayList<String>();
        Locale.setDefault( Locale.US );
        month = monthCalendar;
        dateList = new ArrayList<String>();
        dateList = resultList;
        amount = sum;
        //datelistsize = dateList.size();
        selectedDate = (GregorianCalendar) monthCalendar.clone();
        mContext = c;
        month.set(GregorianCalendar.DAY_OF_MONTH, 1);
        this.items = new ArrayList<String>();
        df = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        curentDateString = df.format(selectedDate.getTime());
        refreshDays(dateList,amount);
    }

    public void setItems(ArrayList<String> items) {
        for (int i = 0; i != items.size(); i++) {
            if (items.get(i).length() == 1) {
                items.set(i, "0" + items.get(i));
            }
        }
        this.items = items;
    }

    public int getCount() {
        return dayString.size();
    }

    public Object getItem(int position) {
        return dayString.get(position);
    }

    public long getItemId(int position) {
        return 0;
    }

    // create a new view for each item referenced by the Adapter
    public View getView(int position, View convertView, ViewGroup parent) {
        View v = convertView;
        TextView dayView;
        if (convertView == null) { // if it's not recycled, initialize some
            // attributes
            LayoutInflater vi = (LayoutInflater) mContext
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = vi.inflate(R.layout.custom_item_calendar, null);

        }
        dayView = (TextView) v.findViewById(R.id.date_text);
        fill_event = (TextView) v.findViewById(R.id.list_text);
        event_num = (LinearLayout) v.findViewById(R.id.layout_event);
        // separates daystring into parts.
        String[] separatedTime = dayString.get(position).split("-");
        // taking last part of date. ie; 2 from 2012-12-02
        String gridvalue = separatedTime[2].replaceFirst("^0*", "");
        // checking whether the day is in current month or not.

        System.out.println("ini datelist dari view : "+dateList);

        if ((Integer.parseInt(gridvalue) > 1) && (position < firstDay)) {
            // setting offdays to white color.
            dayView.setTextColor(Color.BLUE);
            dayView.setClickable(false);
            dayView.setFocusable(false);
            fill_event.setText("");
            fill_event.setBackgroundResource(R.drawable.border2);
        } else if ((Integer.parseInt(gridvalue) < 7) && (position > 28)) {
            dayView.setTextColor(Color.BLUE);
            dayView.setClickable(false);
            dayView.setFocusable(false);
            fill_event.setText("");
            fill_event.setBackgroundResource(R.drawable.border2);
        } else {
            // setting curent month's days in blue color.
            dayView.setTextColor(Color.BLACK);

            if(dateList.contains((gridvalue))){
                dayView.setText(gridvalue);
                //System.out.println("ini isi grid-nya : "+gridvalue);
                fill_event.setBackgroundResource(R.drawable.border_green);
                cek = dateList.indexOf(gridvalue);
                System.out.println("ini isi cek : "+cek);
                int value = amount.get(cek);
                fill_event.setText("Rv : "+value);
                //event_num.setBackgroundResource(R.drawable.border_green);
            }else {
                //dayView.setText(gridvalue);
                //System.out.println("ini isi grid-nya : "+gridvalue);
                fill_event.setText("");
                fill_event.setBackgroundResource(R.drawable.border2);
                //event_num.setBackgroundResource(R.drawable.border);
            }
        }

        if (dayString.get(position).equals(curentDateString)) {
            setSelected(v);
            previousView = v;
        } else {
            v.setBackgroundColor(Color.WHITE);
        }

        dayView.setText(gridvalue);
        //fill_event.setText("21");

        // create date string for comparison
        String date = dayString.get(position);

        if (date.length() == 1) {
            date = "0" + date;
        }
        String monthStr = "" + (month.get(GregorianCalendar.MONTH) + 1);
        if (monthStr.length() == 1) {
            monthStr = "0" + monthStr;
        }

        return v;
    }

    public View setSelected(View view) {
        if (previousView != null) {
            previousView.setBackgroundColor(Color.WHITE);
        }
        previousView = view;
        view.setBackgroundColor(Color.WHITE);
        return view;
    }

    public void refreshDays(ArrayList<String> changedate, ArrayList<Integer> changeSum) {

        // clear items
        //dateList.clear();
        items.clear();
        dayString.clear();
        Locale.setDefault( Locale.US );
        dateList = changedate;
        amount = changeSum;
        pmonth = (GregorianCalendar) month.clone();
        // month start day. ie; sun, mon, etc
        firstDay = month.get(GregorianCalendar.DAY_OF_WEEK);
        // finding number of weeks in current month.
        maxWeeknumber = month.getActualMaximum(GregorianCalendar.WEEK_OF_MONTH);
        // allocating maximum row number for the gridview.
        mnthlength = maxWeeknumber * 7;
        maxP = getMaxP(); // previous month maximum day 31,30....
        calMaxP = maxP - (firstDay - 1);// calendar offday starting 24,25 ...
        /**
         * Calendar instance for getting a complete gridview including the three
         * month's (previous,current,next) dates.
         */
        pmonthmaxset = (GregorianCalendar) pmonth.clone();
        /**
         * setting the start date as previous month's required date.
         */
        pmonthmaxset.set(GregorianCalendar.DAY_OF_MONTH, calMaxP + 1);

        /**
         * filling calendar gridview.
         */
        for (int n = 0; n < mnthlength; n++) {

            itemvalue = df.format(pmonthmaxset.getTime());
            pmonthmaxset.add(GregorianCalendar.DATE, 1);
            dayString.add(itemvalue);
            //System.out.println("ini isi itemnya : "+itemvalue);
        }
    }

    private int getMaxP() {
        int maxP;
        if (month.get(GregorianCalendar.MONTH) == month
                .getActualMinimum(GregorianCalendar.MONTH)) {
            pmonth.set((month.get(GregorianCalendar.YEAR) - 1),
                    month.getActualMaximum(GregorianCalendar.MONTH), 1);
        } else {
            pmonth.set(GregorianCalendar.MONTH,
                    month.get(GregorianCalendar.MONTH) - 1);
        }
        maxP = pmonth.getActualMaximum(GregorianCalendar.DAY_OF_MONTH);

        return maxP;
    }
}
