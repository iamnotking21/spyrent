package com.ax.childapp;

import android.app.Service;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.provider.Browser;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;

public class block extends Service {
    private WindowManager windowManager;
    private View view;


    public block() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate() {
        super.onCreate();




        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && Build.VERSION.SDK_INT <= Build.VERSION_CODES.Q){
            view = LayoutInflater.from(this).inflate(R.layout.block_site_lay,null);

            final WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                    WindowManager.LayoutParams.FILL_PARENT,
                    WindowManager.LayoutParams.FILL_PARENT,
                    WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                    PixelFormat.TRANSLUCENT
            );

            params.gravity = Gravity.CENTER;

            params.x=0;
            params.y=100;

            windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
            windowManager.addView(view,params);


            final WebView webView = (WebView) view.findViewById(R.id.webview1);
            webView.setWebViewClient(new WebViewClient());
            webView.getSettings().setJavaScriptEnabled(true);
            webView.getSettings().setDomStorageEnabled(true);
            webView.loadUrl("file:///android_asset/index2.html");


            webView.setWebViewClient(new WebViewClient(){
                @Override
                public void onPageFinished(WebView view, String url) {
                    super.onPageFinished(view, url);
                    Log.v("website"," url "+url);
                }

            });
            Image();
        }else{

            view = LayoutInflater.from(this).inflate(R.layout.block_site_lay,null);

            final WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                    WindowManager.LayoutParams.FILL_PARENT,
                    WindowManager.LayoutParams.FILL_PARENT,
                    WindowManager.LayoutParams.TYPE_PHONE,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                    PixelFormat.TRANSLUCENT
            );

            params.gravity = Gravity.CENTER;

            params.x=0;
            params.y=100;

            windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
            windowManager.addView(view,params);


            final WebView webView = (WebView) view.findViewById(R.id.webview1);
            webView.setWebViewClient(new WebViewClient());
            webView.getSettings().setJavaScriptEnabled(true);
            webView.getSettings().setDomStorageEnabled(true);
            webView.loadUrl("file:///android_asset/index2.html");


            webView.setWebViewClient(new WebViewClient(){
                @Override
                public void onPageFinished(WebView view, String url) {
                    super.onPageFinished(view, url);
                    Log.v("website"," url "+url);
                }

            });
            Image();
        }



    }

    public void Image(){

        Button btn = (Button) view.findViewById(R.id.button4);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String url = "http://www.google.com";

                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                intent.putExtra(Browser.EXTRA_APPLICATION_ID, "com.android.chrome");
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);

                /*Intent browserIntent = new Intent(Intent.ACTION_VIEW);
                browserIntent.setData(Uri.parse("http://www.google.com"));
                browserIntent.addFlags(FLAG_ACTIVITY_NEW_TASK);
                startActivity(browserIntent);*/

            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(view != null) windowManager.removeView(view);
    }
}
