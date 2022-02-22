package com.debussy69.gadgetry.ui;

import android.view.View;
import android.widget.TextView;

import androidx.appcompat.widget.SwitchCompat;

import com.debussy69.gadgetry.R;

public class AlarmTime {
    public String dayOfWeek = "nice";
    public int hour = 5, minute = 3;
    public boolean enabled = true;

    public void attach(View view) {
        ((TextView) view.findViewById(R.id.tv_day)).setText(dayOfWeek);
        ((TextView) view.findViewById(R.id.tv_time)).setText(timeToString());
        ((SwitchCompat) view.findViewById(R.id.switch_enabled)).setChecked(enabled);
    }

    public String timeToString() {
        return (hour < 10 ? "0" + hour : hour) + " : " + (minute < 10 ? "0" + minute : minute);
    }
}
