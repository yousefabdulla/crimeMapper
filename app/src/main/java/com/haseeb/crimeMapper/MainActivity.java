package com.haseeb.crimeMapper;

import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import android.widget.CalendarView;
import android.widget.LinearLayout;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.view.Gravity;
import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.ActionBar;
import android.graphics.drawable.ColorDrawable;
import android.graphics.Color;
import android.util.Log;
public class MainActivity extends AppCompatActivity {
    Boolean isCalenderOpen = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        View view = getLayoutInflater().inflate(R.layout.action_bar, null);
        ActionBar.LayoutParams params = new ActionBar.LayoutParams(
                ActionBar.LayoutParams.WRAP_CONTENT,
                ActionBar.LayoutParams.MATCH_PARENT,
                Gravity.CENTER);

        TextView Title = (TextView) view.findViewById(R.id.actionbar_title);
        Title.setText("Crime Mapper");

        getSupportActionBar().setCustomView(view,params);
        getSupportActionBar().setDisplayShowCustomEnabled(true); //show custom title
        getSupportActionBar().setDisplayShowTitleEnabled(false); //hide the default title
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#ffffff")));

        CalendarView calendarView = findViewById(R.id.calendarView);
        calendarView.setVisibility(View.INVISIBLE);

        Button button = (Button) findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CalendarView calendarView = findViewById(R.id.calendarView);
                if(isCalenderOpen){
                    calendarView.setVisibility(View.INVISIBLE);
                    isCalenderOpen = false;
                } else {
                    calendarView.setVisibility(View.VISIBLE);
                    isCalenderOpen = true;
                }
                Log.d("Hello","Here");
            }
        });

    }
}
