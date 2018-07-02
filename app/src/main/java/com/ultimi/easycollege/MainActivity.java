package com.ultimi.easycollege;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.ArrayList;

import cz.msebera.android.httpclient.Header;

public class MainActivity extends AppCompatActivity {

    final String MYPREFS = "MyPrefs";
    RecyclerView mRecyclerView;
    CollegeAdapter mAdapter;
    ArrayList<CollegeModel> mCollegeModels;
    ArrayList<String> mFoundColleges;
    String mChosenCollege;
    ProgressDialog mProgressDialog;
    SharedPreferences mSharedPreferences;
    ArrayList<CollegeModel> mNewCollegeModels;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mRecyclerView = findViewById(R.id.colleges_recycler_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setHasFixedSize(true);
        mCollegeModels = new ArrayList<>();

        mAdapter = new CollegeAdapter(this, mCollegeModels);
        mRecyclerView.setAdapter(mAdapter);
        Log.d("EasyCollege", "From onCreate()");

        mFoundColleges = new ArrayList<>();
        mChosenCollege = "";

        mSharedPreferences = getSharedPreferences(MYPREFS, Context.MODE_PRIVATE);
        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setMessage("Gathering Data");
        mProgressDialog.setIndeterminate(false);
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);


        ItemTouchHelper mIth = new ItemTouchHelper(
                new ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP | ItemTouchHelper.DOWN, ItemTouchHelper.RIGHT) {
                    @Override
                    public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                        mAdapter.notifyItemMoved(viewHolder.getAdapterPosition(), target.getAdapterPosition());
                        return true;
                    }

                    @Override
                    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                        mCollegeModels.remove(viewHolder.getAdapterPosition());
                        mAdapter.notifyItemRemoved(viewHolder.getAdapterPosition());
                    }

                    @Override
                    public void onChildDraw(Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
                        if(actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
                            float alpha = 1 - (Math.abs(dX) / recyclerView.getWidth());
                            viewHolder.itemView.setAlpha(alpha);
                        }
                        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
                    }
                }
        );

        mIth.attachToRecyclerView(mRecyclerView);

        loadData();

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        try {
            getMenuInflater().inflate(R.menu.menu, menu);

            // Associate searchable configuration with the SearchView
            MenuItem searchItem = menu.findItem(R.id.search_button);
            SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);

            SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
            ComponentName componentName = new ComponentName(this, MainActivity.class);
            searchView.setSearchableInfo(searchManager.getSearchableInfo(componentName));
            searchView.setIconified(true);
            searchView.setSubmitButtonEnabled(true);
            return true;
        }
        catch(NullPointerException e){
            Log.d("EasyCollege", "NullPointerException in onCreateOptionsMenu from getSearchableInfo: " + e.toString());
        }

        return false;
    }

    public void createCollegeDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        ArrayAdapter arrayAdapter = new ArrayAdapter<>(
                MainActivity.this,
                android.R.layout.select_dialog_item,
                mFoundColleges

        );
        builder.setTitle(R.string.college_dialog_title).setAdapter(arrayAdapter, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Log.d("EasyCollege", mFoundColleges.get(which));
                mChosenCollege = mFoundColleges.get(which);
                getCollegeInformation();

            }
        });
        builder.create().show();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        setIntent(intent);
        if(Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            searchCollegeOptions(query);
        }
    }

    public void loadData() {
        Log.d("EasyCollege", "from loadData");
        Gson gson = new Gson();
        String json = mSharedPreferences.getString("college models", null);
        if(json == null){
            return;
        }
        Type type = new TypeToken<ArrayList<CollegeModel>>() {}.getType();
        mNewCollegeModels = gson.fromJson(json, type);
        Log.d("EasyCollege", mNewCollegeModels.toString());
        for (CollegeModel model : mNewCollegeModels){
            mCollegeModels.add(model);
            mAdapter.notifyDataSetChanged();
        }

    }

    public void searchCollegeOptions(String query) {
        mProgressDialog.show();
        AsyncHttpClient client = new AsyncHttpClient();
        client.get("https://mvk5rtvch8.execute-api.us-east-1.amazonaws.com/prod/search?search=" + query, null, new JsonHttpResponseHandler() {
           @Override
           public void onSuccess(int statusCode, Header[] headers, JSONObject response){
               try {
                   mFoundColleges.clear();
                   JSONArray colleges = response.getJSONArray("messages");
                   Log.d("EasyCollege", colleges.toString());
                   for(int i = 0; i < colleges.length(); i++) {
                       Log.d("EasyCollege", colleges.getString(i));
                       mFoundColleges.add(colleges.getString(i));
                   }
                   mProgressDialog.dismiss();
                   createCollegeDialog();

               }
               catch(JSONException e) {
                   mProgressDialog.dismiss();
                   Log.d("EasyCollege", e.toString());
                   Toast.makeText(MainActivity.this, "Couldn't gather data", Toast.LENGTH_LONG).show();
               }
           }
        });
    }

    public void getCollegeInformation() {
        mProgressDialog.show();
        AsyncHttpClient client = new AsyncHttpClient();
        client.get("https://1m468rdcpi.execute-api.us-east-1.amazonaws.com/prod/search?search=" + mChosenCollege, null, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response){
                try {
                    JSONObject data = response.getJSONObject("message");
                    Log.d("EasyCollege", (String) data.get("Name"));
                    mCollegeModels.add(new CollegeModel((String) data.get("Name"), (String) data.get("Niche_Grade"),
                            (String) data.get("Sat_Range"), (String) data.get("Acceptance_Rate"),
                            (String) data.get("Location"), (String) data.get("Net_Price"), (String) data.get("Act_Range")));
                    mAdapter.notifyDataSetChanged();
                    mProgressDialog.dismiss();

                }
                catch(JSONException e) {
                    mProgressDialog.dismiss();
                    Log.d("EasyCollege", e.toString());
                    Toast.makeText(MainActivity.this, "Couldn't gather data", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        Gson gson = new Gson();
        String json = gson.toJson(mCollegeModels);
        if(mSharedPreferences.contains("college models")) {
            editor.remove("college models");
            editor.apply();
        }
        editor.putString("college models", json);
        editor.apply();
    }
}
