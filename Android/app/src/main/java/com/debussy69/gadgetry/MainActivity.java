package com.debussy69.gadgetry;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.os.Bundle;
import android.view.MenuItem;

import com.debussy69.gadgetry.ui.AlarmClockFragment;
import com.debussy69.gadgetry.ui.HabitTrackerFragment;
import com.debussy69.gadgetry.ui.ToDoListFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    ToDoListFragment toDoListFragment;
    HabitTrackerFragment habitTrackerFragment;
    AlarmClockFragment alarmClockFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        toDoListFragment = new ToDoListFragment();
        habitTrackerFragment = new HabitTrackerFragment();
        alarmClockFragment = new AlarmClockFragment();
        loadFragment(toDoListFragment);

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation_view);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            switch (item.getItemId()) {
                case R.id.navigation_to_do_list:
                    return loadFragment(toDoListFragment);
                case R.id.navigation_habit_tracker:
                    return loadFragment(habitTrackerFragment);
                case R.id.navigation_alarm_clock:
                    return loadFragment(alarmClockFragment);
            }
            return false;
        });
    }

    private boolean loadFragment(Fragment fragment) {
        if (fragment == null) return false;
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
        return true;
    }
}