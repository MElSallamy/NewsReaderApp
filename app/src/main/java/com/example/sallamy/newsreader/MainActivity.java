package com.example.sallamy.newsreader;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class MainActivity extends AppCompatActivity {

    Map<Integer, String> mapurls = new HashMap<Integer, String>();
    Map<Integer, String> maptitle = new HashMap<Integer, String>();

    ArrayList<Integer> arrayofids = new ArrayList<Integer>();
    static ArrayList<String> titlesarr = new ArrayList<>();
    static ArrayList<String> urlsarr = new ArrayList<>();

    SQLiteDatabase db;

    ListView listView;
    String newsResult;
    JSONArray arrayofnews;
    ArrayAdapter arrayAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        listView = (ListView) findViewById(R.id.listView);

        arrayAdapter = new ArrayAdapter(this, R.layout.support_simple_spinner_dropdown_item, titlesarr);
        listView.setAdapter(arrayAdapter);


        // What happen when we tapped on any item
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                Intent intent = new Intent(getApplicationContext(), com.example.sallamy.newsreader.view.class);

                intent.putExtra("newsurl : ", urlsarr.get(position));

                startActivity(intent);

            }
        });

        // create DB
        db = this.openOrCreateDatabase("newsdatabase", MODE_PRIVATE, null);

        final DownloadTask task = new DownloadTask();

        try {
            newsResult = task.execute("https://hacker-news.firebaseio.com/v0/topstories.json?print=pretty").get();

            // creating JSON arr to extract unique numbers for top stories  ..
            arrayofnews = new JSONArray(newsResult);

            // we still on onCreate
            // DB create table
            db.execSQL("CREATE TABLE IF NOT EXISTS articlstable " +
                    "(id INTEGER PRIMARY KEY , articleid INTEGER , title VARCHAR , url VARCHAR )");
            // clear data from database every time on create
            db.execSQL("DELETE FROM articlstable");

            for (int i = 0; i < 20; i++) {

                String articlId = arrayofnews.getString(i);

                DownloadTask articletask = new DownloadTask();

                String article = articletask.execute("https://hacker-news.firebaseio.com/v0/item/" + articlId + ".json?print=pretty").get();

                // create JSON object
                JSONObject jsonObject = new JSONObject(article);

                String articletitle;

                if (jsonObject.has("title")) {
                    articletitle = jsonObject.getString("title");
                } else articletitle = "";

                String articleurl;

                if (jsonObject.has("url")) {
                    articleurl = jsonObject.getString("url");
                } else articleurl = "";

                int id = Integer.valueOf(articlId);

                // save ids in array of integers
                arrayofids.add(id);
                // save urls by key which is id
                mapurls.put(id, articleurl);
                // same in titles
                maptitle.put(id, articletitle);

                // prepaid statement
                String insertsql = "INSERT INTO articlstable (articleid , title ,url )VALUES (?,?,?)";

                SQLiteStatement prepairinsert = db.compileStatement(insertsql);

                prepairinsert.bindString(1, articlId);
                prepairinsert.bindString(2, articletitle);
                prepairinsert.bindString(3, articleurl);

                prepairinsert.execute();

            }

            upDateListView();

        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    public void upDateListView() {


        // note that we still in try
        // selecting data from DB
        // order by for give us the biggest id number which mean that its newer one
        Cursor c = db.rawQuery("SELECT * FROM articlstable ORDER BY articleid DESC ", null);

        // column indexes
        int titleindex = c.getColumnIndex("title");
        int urlindex = c.getColumnIndex("url");

        c.moveToFirst();

        // to clear arr list befor adding new values
        titlesarr.clear();
        urlsarr.clear();

        while (c != null) {
            titlesarr.add(c.getString(titleindex));
            urlsarr.add(c.getString(urlindex));
            c.moveToNext();
        }

        // update adapter after adding new values
        arrayAdapter.notifyDataSetChanged();

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);

    }

    // conn the internet and download task
    public class DownloadTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {

            String result = "";
            URL url;
            HttpURLConnection urlConnection = null;

            try {
                url = new URL(urls[0]);
                urlConnection = (HttpURLConnection) url.openConnection();
                InputStream in = urlConnection.getInputStream();
                InputStreamReader reader = new InputStreamReader(in);
                int data = reader.read();

                while (data != -1) {
                    char current = (char) data;
                    result += current;
                    data = reader.read();
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

            return result;
        }

    }


}
