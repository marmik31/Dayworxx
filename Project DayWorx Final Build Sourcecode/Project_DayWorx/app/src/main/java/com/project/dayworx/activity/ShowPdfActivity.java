package com.project.dayworx.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.project.dayworx.R;
import com.project.dayworx.network.IWebService;
import com.project.dayworx.util.Utils;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by ubuntu on 9/12/17.
 */

public class ShowPdfActivity extends AppCompatActivity {

    private static final String TAG = ShowPdfActivity.class.getSimpleName();

    @BindView(R.id.webView)
    WebView webView;

    @BindView(R.id.btnBack)
    ImageView btnBack;

    @BindView(R.id.btnBuyReport)
    Button btnBuyReport;

    @BindView(R.id.rlvGlobalProgressbar)
    RelativeLayout rlvGlobalProgressbar;

    private static String PDF_URL, REPORT_ID, PURCHASED_STATUS;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_pdf);
        ButterKnife.bind(this);

        if (getIntent() != null) {
            REPORT_ID = getIntent().getStringExtra(IWebService.KEY_RES_REPORT_ID);
            PDF_URL = getIntent().getStringExtra("PDF_URL");
            PURCHASED_STATUS = getIntent().getStringExtra(IWebService.KEY_RES_REPORT_PURCHASED_STATUS);
            Log.d(TAG, "PDF_URL ==> " + PDF_URL);
            Log.d(TAG, "PURCHASED_STATUS ==> " + PURCHASED_STATUS);


        }

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        btnBuyReport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (PURCHASED_STATUS.equalsIgnoreCase("1")) {
                    Utils.showAlert(ShowPdfActivity.this, "", "Report is already purchased");
                } else {
                    switchToReportPurchaseActivity();
                }
            }
        });


//        init();

        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setPluginState(WebSettings.PluginState.ON);
//        String googleDocs = "https://docs.google.com/viewer?url=";
//        String googleDocs = "http://drive.google.com/viewerng/viewer?embedded=true&url=";
        webView.loadUrl(PDF_URL);
//        webView.loadUrl("https://www.google.co.in/");
//        webView.setWebChromeClient(new MyWebViewClient());
        webView.setWebViewClient(new WebViewClient() {

            // Show progressBar
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
//                rlvGlobalProgressbar.setVisibility(view.VISIBLE);
            }

            // Hide progressBar
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
//                rlvGlobalProgressbar.setVisibility(view.GONE);
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setDataAndType(Uri.parse(url), "application/pdf");
                view.loadUrl(url);

                return true;
            }


        });


    }


    private class MyWebViewClient extends WebChromeClient {

        public void onProgressChanged(WebView view, int newProgress) {
//            ShowPdfActivity.this.setValue(newProgress);
            super.onProgressChanged(view, newProgress);

        }

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.left_to_right, R.anim.right_to_left);
        finish();
    }

    //    @OnClick(R.id.btnDone)
//    public void switchToReportActivity() {
////        Intent intent = new Intent(ReportVerificationActivity.this, GalleryActivity.class);
////        startActivity(intent);
////        Toast.makeText(this, "Report Saved", Toast.LENGTH_SHORT).show();
//        Intent intent = new Intent(ShowPdfActivity.this, ReportActivity.class);
//        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
//        startActivity(intent);
//        overridePendingTransition(R.anim.enter, R.anim.exit);
//        finish();
//    }

    public void switchToReportPurchaseActivity() {
        Intent intent = new Intent(ShowPdfActivity.this, InAppPurchaseActivity.class);
        intent.putExtra(IWebService.KEY_RES_REPORT_ID, REPORT_ID);
//        intent.putExtra("PDF_URL", url);
        startActivity(intent);
        overridePendingTransition(R.anim.enter, R.anim.exit);
        finish();
    }

}
