package com.android.king.xmppdemo.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

import com.android.king.xmppdemo.R;

/**
 * WebView
 *
 * @author：King
 * @time: 2018/9/16 11:29
 */
public class WebFragment extends BaseFragment {

    private WebView webView;
    private ProgressBar pbProgress;

    private String url;
    private String title;

    public static WebFragment newInstance(String title, String url) {
        WebFragment fragment = new WebFragment();
        Bundle b = new Bundle();
        b.putString("title", title);
        b.putString("url", url);
        fragment.setArguments(b);
        return fragment;
    }

    @Override
    protected int getContentView() {
        return R.layout.fragment_web;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        url = getArguments().getString("url");
        title = getArguments().getString("title");
    }

    @Override
    protected void initView() {
        setTitle(title);
        webView = rootView.findViewById(R.id.wv_show);
        pbProgress = rootView.findViewById(R.id.pb_progress);
    }

    @Override
    protected void initData() {
        WebSettings webSettings = webView.getSettings();

        webSettings.setUseWideViewPort(true);
        webSettings.setLoadWithOverviewMode(true);

        webSettings.setSupportZoom(true);
        webSettings.setBuiltInZoomControls(true);
        webSettings.setDisplayZoomControls(false);

        webSettings.setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
        webSettings.setAllowFileAccess(true);
        webSettings.setJavaScriptCanOpenWindowsAutomatically(true);
        webSettings.setLoadsImagesAutomatically(true);
        webSettings.setDefaultTextEncodingName("utf-8");

        pbProgress.setMax(100);

        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                pbProgress.setProgress(newProgress);
                if (newProgress == 100) {
                    // 网页加载完成
                    pbProgress.setVisibility(View.GONE);
                } else {
                    // 加载中
                    pbProgress.setVisibility(View.VISIBLE);
                }

            }

            @Override
            public void onReceivedTitle(WebView view, String t) {
                super.onReceivedTitle(view, t);
                if(TextUtils.isEmpty(title)) {
                    setTitle(t);
                }
            }
        });
        webSettings.setJavaScriptEnabled(true);
        webView.setWebViewClient(new WebViewClient() {

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return true;
            }

            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                view.loadUrl("file:///android_asset/error/index.html");
            }


        });

        webView.loadUrl(url);
    }


    @Override
    public boolean onBackPressedSupport() {
        if (webView.canGoBack()) {
            webView.goBack();
        } else {
            pop();
        }
        return true;
    }
}
