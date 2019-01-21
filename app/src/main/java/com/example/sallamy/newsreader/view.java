package com.example.sallamy.newsreader;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class view extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view);


        //  a back arrow appear to the left of the application icon
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        WebView view = (WebView) findViewById(R.id.view);

        view.getSettings().setJavaScriptEnabled(true);
        view.setWebViewClient(new WebViewClient());

        Intent geturl = getIntent();
        String url = geturl.getStringExtra("newsurl : ");

        view.loadUrl(url);

    }


    // For back icon ..
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }

}
