package com.haseeb.crimeMapper;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import android.widget.CalendarView;


import android.widget.TextView;
import android.view.Gravity;
import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.ActionBar;
import android.graphics.drawable.ColorDrawable;
import android.graphics.Color;
import android.util.Log;


import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.NoConnectionError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.BasicNetwork;
import com.android.volley.toolbox.HurlStack;
import com.android.volley.toolbox.JsonArrayRequest;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.Cache;
import com.android.volley.toolbox.DiskBasedCache;
import com.android.volley.Network;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import android.os.Handler;
import android.widget.Spinner;
import android.widget.ArrayAdapter;
public class MainActivity extends AppCompatActivity {
    Boolean isCalenderOpen = false;
    RequestQueue requestQueue;
    int done = 0;
    int total = 0;


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

        getSupportActionBar().setCustomView(view, params);

        getSupportActionBar().setDisplayShowCustomEnabled(true); //show custom title
        getSupportActionBar().setDisplayShowTitleEnabled(false); //hide the default title
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#ffffff")));


        Spinner spinner = findViewById(R.id.area_spinner);

// Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.area_array, android.R.layout.simple_spinner_item);
// Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
// Apply the adapter to the spinner
        spinner.setAdapter(adapter);
        Cache cache = new DiskBasedCache(getCacheDir(), 1024 * 1024 * 50); // 50MB cap

        // Set up the network to use HttpURLConnection as the HTTP client.
        Network network = new BasicNetwork(new HurlStack());
        requestQueue = new RequestQueue(cache, network);

        // Start the queue
        requestQueue.start();


        String defaultArea = "city-of-london";
        final TextView textView = findViewById(R.id.textView2);

        textView.setText("Getting Data");
        LineChart chart = (LineChart) findViewById(R.id.chart);
        getNeighbourhoods(defaultArea).then(res -> {

            Object object = res;
            ArrayList<String> ids = ((ArrayList<String>) object);
            Log.d("response", "onCreate: " + ids.size());
            total = ids.size();
            textView.setText("Total Number of neighbourhoods: " + total + ". Getting data...");
            Promise promises[] = new Promise[ids.size()];

            for (int i = 0; i < ids.size(); i++){

                int finalI = i;

                Promise thisPromise = getAllCrime(defaultArea, ids.get(finalI));
                promises[finalI] = thisPromise;

            }
            Log.d("promises lenght",promises.length+"");
            HashMap<String,Integer> chartData = new HashMap<>();
            Promise.all(promises).then(result -> {
                Object objects[] = ((Object[]) result);
                Log.d("objects",((Object[]) result).length+"");
                for (int i = 0; i < objects.length; i++){
                    HashMap<String, JSONArray> thisMap = (HashMap<String, JSONArray>) objects[i];
                    thisMap.forEach((k,v)->{
                        if(chartData.containsKey(k)){
                            chartData.put(k, chartData.get(k) + v.length());
                        } else {
                            chartData.put(k,v.length());
                        }
                    });
                }
                Log.d("charData",chartData.toString());
                List<Entry> entries=  new ArrayList<Entry>();
                entries.add(new Entry(0, chartData.get("2019-11")));
                entries.add(new Entry(1, chartData.get("2019-10")));
                entries.add(new Entry(2, chartData.get("2019-09")));
                entries.add(new Entry(3, chartData.get("2019-08")));
                entries.add(new Entry(4, chartData.get("2019-07")));
                entries.add(new Entry(5, chartData.get("2019-06")));

                final String[] quarters = new String[] { "2019-11", "2019-10", "2019-09", "2019-08", "2019-07", "2019-06" };

                ValueFormatter formatter = new ValueFormatter() {
                    @Override
                    public String getAxisLabel(float value, AxisBase axis) {
                        return quarters[(int) value];
                    }
                };
                XAxis xAxis = chart.getXAxis();
                xAxis.setGranularity(1f); // minimum axis-step (interval) is 1
                xAxis.setValueFormatter(formatter);
                xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
                xAxis.setDrawAxisLine(true);
                xAxis.setDrawGridLines(false);

                Description description = chart.getDescription();
                description.setEnabled(false);
                LineDataSet dataSet = new LineDataSet(entries,"Crime Data");
                LineData lineData = new LineData(dataSet);

                chart.setData(lineData);
                chart.invalidate();
                return 1;
            });
            return 1;
        });


    }

    private Promise getAllCrime(String force, String area) {
        Promise p = new Promise();

        final Handler handler = new Handler();

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                HashMap<String, JSONArray> crimes = new HashMap<>();
                List<String> months = new ArrayList<>(Arrays.asList("2019-11","2019-10","2019-09","2019-08","2019-07","2019-06"));

                Promise promises[] = new Promise[months.size()];
                for (int i = 0; i<months.size(); i++){
                    Promise thisPromise = getCrime(months.get(i),force,area);
                    promises[i] = thisPromise;
                }

                Promise.all(promises).then(results ->{
                    Object objects[] = (Object[]) results;
                    for (int i = 0; i < objects.length; i++){
                        JSONObject obj = (JSONObject) objects[i];
                        try {
                            if (obj == null){
                                Log.d("obj null","got null, when does this happen?");
                            } else {
                                crimes.put(obj.getString("month"), obj.getJSONArray("data"));
                            }
                        } catch(JSONException e){
                            p.reject(e.getMessage());
                        }
                    }
                    done++;
                    Log.d("Done",done+"");
                    final TextView textView = findViewById(R.id.textView2);

                    textView.setText("Done "+done+"/"+total);
                    p.resolve(crimes);
                    return 1;
                });
            }
        },500);


        return p;
    }
    private Promise getCrime(String month, String force, String area){
        Log.d("called",month+" "+force+" "+area);
        Promise p = new Promise();
        getBoundaries(force, area).then(boundary -> {
            String url = "https://data.police.uk/api/crimes-street/all-crime";
            VolleyMultipartRequest multipartRequest = new VolleyMultipartRequest(Request.Method.POST, url, new Response.Listener<NetworkResponse>() {
                @Override
                public void onResponse(NetworkResponse response) {
                    Log.d("got response",month+" "+force+" "+area);

                    String resultResponse = new String(response.data);
                    try {
                        JSONArray jsonArray = new JSONArray(resultResponse);
                        JSONObject obj = new JSONObject();
                        obj.put("month",month);
                        obj.put("data",jsonArray);

                        p.resolve(obj);
                    } catch(JSONException e) {
                        p.reject(e.getMessage());
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    p.reject(error.getMessage());
                }
            }) {
                @Override
                protected Map<String, String> getParams() {
                    Map<String, String> params = new HashMap<>();
                    params.put("poly", ((String) boundary));
                    params.put("date", month);
                    return params;
                }
            };
            requestQueue.add(multipartRequest);
            return 1;
        }).error(err ->{
            Log.d("boundary rejection", "getCrime: "+err.toString());
        });
        return p;
    }
    private Promise getBoundaries(String force, String area){
        Promise p = new Promise();
        String url = "https://data.police.uk/api/"+force+"/"+area+"/boundary";
        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(url, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                String boundary = "";
                for (int i = 0; i<response.length(); i++){
                    try {
                        JSONObject object = response.getJSONObject(i);
                        if (i == 0){
                            boundary += object.getString("latitude") + ",";
                        } else if (i == response.length() - 1){
                            boundary += object.getString("longitude");
                        } else {
                            boundary += object.getString("longitude") + ":" + object.getString("latitude") + ",";
                        }
                    } catch(JSONException e) {
                        Log.d("error", "onResponse: " + e.getMessage());
                    }
                }
                p.resolve(boundary);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                p.reject(error.getMessage());
            }
        });
        requestQueue.add(jsonArrayRequest);
        return p;
    }
    private Promise getNeighbourhoods(String area){
        Promise p = new Promise();
        String url = "https://data.police.uk/api/"+area+"/neighbourhoods";
        Log.d("url", "getCrimeData: "+ url);
        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(url, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                ArrayList<String> ids = new ArrayList<String>();
                for (int i = 0; i < response.length(); i++){
                    try {
                        String id = response.getJSONObject(i).getString("id");
                        ids.add(id);
                    } catch(JSONException e){
                        Log.d("error", "onResponse: "+e.getMessage());
                    }
                }
                p.resolve(ids);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("error", "onErrorResponse: "+error.getMessage());
                p.reject(error.getMessage());
            }
        });
        requestQueue.add(jsonArrayRequest);
        return p;

    }
}
