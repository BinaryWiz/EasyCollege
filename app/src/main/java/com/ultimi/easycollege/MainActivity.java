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
    // Some general terms:
    // Viewholder: essentially each seperate "card" in the recyclerView
    // What controls the move
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
        setContentView(R.layout.activity_main); // Sets the view to the main activity
        mRecyclerView = findViewById(R.id.colleges_recycler_view); // Gets the recycler view

        // Layout manager for  Recycler View
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Will not change physical size of the recycler view (will scroll instead)
        mRecyclerView.setHasFixedSize(true);
        mCollegeModels = new ArrayList<>();

        mAdapter = new CollegeAdapter(this, mCollegeModels);
        mRecyclerView.setAdapter(mAdapter);
        Log.d("EasyCollege", "From onCreate()");

        // ArrayList for the colleges found from getCollegeOptions
        mFoundColleges = new ArrayList<>();

        // College that user chooses from prompt
        mChosenCollege = "";

        // Defines the sharedPreferences for when the app enters the onPause stage and is
        // eventually destroyed, so data is preserved
        mSharedPreferences = getSharedPreferences(MYPREFS, Context.MODE_PRIVATE);

        // Defines progress dialog for the getCollegeOptions and getCollegeData
        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setMessage("Gathering Data");

        // Makes it clear that we don't know how long it will take the progress dialog to finish
        mProgressDialog.setIndeterminate(false);

        // Sets the progress dialog to be a spinner
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);


        ItemTouchHelper mIth = new ItemTouchHelper(
                new ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP | ItemTouchHelper.DOWN, ItemTouchHelper.RIGHT) {
                    @Override

                    // Notifies the viewHolder that the position of it changed
                    public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                        mAdapter.notifyItemMoved(viewHolder.getAdapterPosition(), target.getAdapterPosition());
                        return true;
                    }

                    // Notifies the viewHolder that it has been swiped and removes it
                    // Removes it from mCollegeModels as well
                    @Override
                    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                        mCollegeModels.remove(viewHolder.getAdapterPosition());
                        mAdapter.notifyItemRemoved(viewHolder.getAdapterPosition());
                    }

                    // Gives the animation when getting swiped
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

        // Attaches ItemTouchHandler to the RecyclerView
        mIth.attachToRecyclerView(mRecyclerView);

        // Loads data from the json file that is in the sharedPreferences
        loadData();

        // Sets the top toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        try {
            // Inflates the menu with the menu I created
            getMenuInflater().inflate(R.menu.menu, menu);

            // Associate searchable configuration with the SearchView
            MenuItem searchItem = menu.findItem(R.id.search_button);
            SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);

            SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
            ComponentName componentName = new ComponentName(this, MainActivity.class);

            // Associates the searchView with the searchManager
            searchView.setSearchableInfo(searchManager.getSearchableInfo(componentName));

            // Sets the searchView to have an icon and have a submit button
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
        // Creates the dialog that has the college options

        // Builds the AlertDialog and and the ArrayAdapter
        // Just inserts the values of the ArrayList (mFoundColleges) into the AlertDialog
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        ArrayAdapter arrayAdapter = new ArrayAdapter<>(
                MainActivity.this,
                android.R.layout.select_dialog_item,
                mFoundColleges
        );

        // Gets the college that the user clicked on and sets it to mChosenCollege
        // Used in the getCollegeInformation method
        builder.setTitle(R.string.college_dialog_title).setAdapter(arrayAdapter, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Log.d("EasyCollege", mFoundColleges.get(which));
                mChosenCollege = mFoundColleges.get(which);
                getCollegeInformation();

            }
        });

        // Shows the AlertDialog
        builder.create().show();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        // After searching for a college, sends the query to the searchCollegeOptions method
        setIntent(intent);
        if(Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            searchCollegeOptions(query);
        }
    }

    public void loadData() {
        // Loads the data from the Gson/json which has the college models in it
        Log.d("EasyCollege", "from loadData");
        Gson gson = new Gson();
        String json = mSharedPreferences.getString("college models", null);
        if(json == null){
            return;
        }

        // Essentially each college model is stored in a Type
        Type type = new TypeToken<ArrayList<CollegeModel>>() {}.getType();
        mNewCollegeModels = gson.fromJson(json, type);
        Log.d("EasyCollege", mNewCollegeModels.toString());
        for (CollegeModel model : mNewCollegeModels){
            mCollegeModels.add(model);
            mAdapter.notifyDataSetChanged();
        }

    }

    public void searchCollegeOptions(String query) {
        // Gets the college options by sending an AsyncHttp request to AWS Lambda function
        // Via the AWS Gateway link
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

                   // Displays the AlertDialog of colleges
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
        // Gets the college information for the selected college from AWS Lambda
        // Via AWS Gateway link
        mProgressDialog.show();
        AsyncHttpClient client = new AsyncHttpClient();
        client.get("https://1m468rdcpi.execute-api.us-east-1.amazonaws.com/prod/search?search=" + mChosenCollege, null, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response){
                try {
                    // Gets each individual piece of information from json
                    JSONObject data = response.getJSONObject("message");
                    mCollegeModels.add(new CollegeModel(
                            // Parses the JSON
                            data.getString("Name"),
                            data.getString("Niche_Grade"),
                            data.getString("Sat_Range"),
                            data.getString("Acceptance_Rate"),
                            data.getString("Location"),
                            data.getString("Net_Price"),
                            data.getString("Act_Range")));
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
        // Puts the college models inside of the mSharedPreferences via a Gson
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
