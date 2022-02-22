package com.debussy69.gadgetry.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.debussy69.gadgetry.MainActivity;
import com.debussy69.gadgetry.R;

import java.util.Objects;

public class AlarmClockFragment extends Fragment {
    public AlarmTime[] alarmTimes;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_alarm_clock, null);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        updateAlarmTimes();
        ((ListView) view.findViewById(R.id.list_view_alarm_times)).setAdapter(new AlarmTimeAdapter());
    }

    public void updateAlarmTimes() {
        alarmTimes = new AlarmTime[7];

        //get alarm times from github
        RequestQueue requestQueue = Volley.newRequestQueue(requireContext());
        for (int i = 0; i < alarmTimes.length; i++) {
            alarmTimes[i] = new AlarmTime();
        }
    }

    private class AlarmTimeAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return alarmTimes.length;
        }

        @Override
        public Object getItem(int i) {
            return alarmTimes[i];
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            if (view == null) {
                view = getLayoutInflater().inflate(R.layout.list_item_alarm_time, viewGroup, false);
            }
            alarmTimes[i].attach(view);
            return view;
        }
    }
}
