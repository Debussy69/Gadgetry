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

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.JsonRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.debussy69.gadgetry.MainActivity;
import com.debussy69.gadgetry.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class AlarmClockFragment extends Fragment {
    public AlarmTime[] alarmTimes;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_alarm_clock, null);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        getAlarmTimes();
    }

    public void getAlarmTimes() {
        alarmTimes = new AlarmTime[7];
//        String url = "https://raw.githubusercontent.com/Debussy69/Gadgetry/master/data/alarm_times.json";
//        RequestQueue requestQueue = Volley.newRequestQueue(requireContext());
//        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null, response -> {
//            if (response.length() != 7) return;
//            for (int i = 0; i < 7; i++) {
//                try {
//                    JSONObject alarmTimeJson = response.getJSONObject(String.valueOf(i));
//                    alarmTimes[i] = new AlarmTime(this, i, alarmTimeJson.getInt("h"), alarmTimeJson.getInt("m"), alarmTimeJson.getInt("e") == 1);
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                    alarmTimes[i] = new AlarmTime(this);
//                }
//            }
//
//            ((ListView) this.getView().findViewById(R.id.list_view_alarm_times)).setAdapter(new AlarmTimeAdapter());
//        }, error -> {
//            error.printStackTrace();
//        });
//        requestQueue.add(jsonObjectRequest);
        for (int i = 0; i < 7; i++) {
            alarmTimes[i] = new AlarmTime(this);
        }
        ((ListView) this.getView().findViewById(R.id.list_view_alarm_times)).setAdapter(new AlarmTimeAdapter());
    }

    public void putAlarmTimes() {
        String url = "https://api.github.com/repos/Debussy69/Gadgetry/contents/data/alarm_times.json";

        RequestQueue requestQueue = Volley.newRequestQueue(requireContext());

        try {
            JSONObject content = new JSONObject();
            for (int i = 0; i < 7; i++) {
                content.put(String.valueOf(i), alarmTimes[i].getJson());
            }

            JSONObject committer = new JSONObject();
            committer.put("name", "Debussy69");
            committer.put("email", "giessibljakob@gmail.com");

            JSONObject jsonRequest = new JSONObject();
            jsonRequest.put("message", "my message");
            jsonRequest.put("content", "my content");
            jsonRequest.put("sha", "98115b5e4c08d87b8862bdbe06189906bb0e1ad0");

            System.out.println(jsonRequest.toString(3));

            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.PUT, url, jsonRequest, response -> {
                try {
                    System.out.println(response.toString(1));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }, error -> error.printStackTrace()) {
                @Override
                public Map<String, String> getHeaders() {
                    Map<String, String> headers = new HashMap<>();
                    headers.put("Accept", "application/vnd.github.v3+json");
                    headers.put("Authorization", "token " + MainActivity.GIT_TOKEN);
                    return headers;
                }
            };

            requestQueue.add(jsonObjectRequest);
        } catch (JSONException e) {
            e.printStackTrace();
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
