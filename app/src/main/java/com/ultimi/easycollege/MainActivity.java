package com.ultimi.easycollege;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import cz.msebera.android.httpclient.Header;

public class MainActivity extends AppCompatActivity {

    RecyclerView mRecyclerView;
    CollegeAdapter mAdapter;
    ArrayList<CollegeModel> mCollegeModels;
    ArrayList<String> mFoundColleges;
    String mChosenCollege;
    ProgressDialog mProgressDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mRecyclerView = findViewById(R.id.colleges_recycler_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setHasFixedSize(true);
        Log.d("EasyCollege", "From onCreate()");

        mFoundColleges = new ArrayList<>();
        mChosenCollege = "";


        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setMessage("Gathering Data");
        mProgressDialog.setIndeterminate(false);
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
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

    public void createCollegeDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        ArrayAdapter arrayAdapter = new ArrayAdapter<String>(
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

    public void searchCollegeOptions(String query) {
        mProgressDialog.show();
        AsyncHttpClient client = new AsyncHttpClient();
        client.get("https://mvk5rtvch8.execute-api.us-east-1.amazonaws.com/prod/search?search=" + query, null, new JsonHttpResponseHandler() {
           @Override
           public void onSuccess(int statusCode, Header[] headers, JSONObject response){
               try {
                   mFoundColleges.clear();
                   JSONArray colleges = response.getJSONArray("messages");
                   for(int i = 0; i < colleges.length(); i++) {
                       Log.d("EasyCollege", colleges.getString(i));
                       mFoundColleges.add(colleges.getString(i));
                   }
                   mProgressDialog.dismiss();
                   createCollegeDialog(null);

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
                            (String) data.get("Location"), (String) data.get("Net_Price")));
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
    public void onStart() {
        super.onStart();
        mCollegeModels = new ArrayList<>();
        mAdapter = new CollegeAdapter(this, mCollegeModels);
        mRecyclerView.setAdapter(mAdapter);
    }
}
