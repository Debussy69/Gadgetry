package com.debussy69.gadgetry.ui;

import android.view.View;
import android.widget.CompoundButton;
import android.widget.TextView;

import androidx.appcompat.widget.SwitchCompat;

import com.debussy69.gadgetry.MainActivity;
import com.debussy69.gadgetry.R;

import org.json.JSONException;
import org.json.JSONObject;

public class AlarmTime {
    public AlarmClockFragment parent;

    public SwitchCompat enabledSwitch;

    public String dayOfWeek = "nice";
    public int hour = 5, minute = 3;
    public boolean enabled = true;

    public AlarmTime(AlarmClockFragment parent, int dayOfWeek, int hour, int minute, boolean enabled) {
        this.parent = parent;
        this.dayOfWeek = MainActivity.weekdayToString(dayOfWeek);
        this.hour = hour;
        this.minute = minute;
        this.enabled = enabled;
    }

    public AlarmTime(AlarmClockFragment parent) {
        this.parent = parent;
        this.dayOfWeek = "Failed";
        this.hour = 0;
        this.minute = 0;
        this.enabled = false;
    }

    public void attach(View view) {
        ((TextView) view.findViewById(R.id.tv_day)).setText(dayOfWeek);
        ((TextView) view.findViewById(R.id.tv_time)).setText(getTimeString());
        enabledSwitch = view.findViewById(R.id.switch_enabled);
        enabledSwitch.setChecked(enabled);
        enabledSwitch.setOnCheckedChangeListener((CompoundButton compoundButton, boolean b) -> {
                enabled = b;
                parent.putAlarmTimes();
        });
    }

    public String getTimeString() {
        return (hour < 10 ? "0" + hour : hour) + " : " + (minute < 10 ? "0" + minute : minute);
    }

    public JSONObject getJson() {
        JSONObject json = new JSONObject();
        try {
            json.put("h", hour);
            json.put("m", minute);
            json.put("e", enabled ? 1 : 0);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return json;
    }
}
