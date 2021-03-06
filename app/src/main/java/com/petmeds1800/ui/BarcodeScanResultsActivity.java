package com.petmeds1800.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.petmeds1800.R;
import com.petmeds1800.ui.fragments.CommonWebviewFragment;
import com.petmeds1800.util.Constants;

/**
 * Created by Digvijay on 9/20/2016.
 */
public class BarcodeScanResultsActivity extends AbstractActivity{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        startScanner();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult result = IntentIntegrator
                .parseActivityResult(requestCode, resultCode, data);

        if (result != null) {
            if (result.getContents() == null) {
                finish();
            } else {
                String url = getString(R.string.server_endpoint) + "/search.jsp?Ns=product.salesvolume%7C1&Ntt=" + result.getContents();
               Intent intent=new Intent();
                intent.putExtra(CommonWebviewFragment.TITLE_KEY, getString(R.string.label_scan_results));
                intent.putExtra(CommonWebviewFragment.URL_KEY, url);
                setResult(Constants.BARCODE_SCANNER_REQUEST, intent);
                finish();

            }
        }
    }

    @Override
    protected int getLayoutResource() {
        return R.layout.activity_barcode_scan_results;
    }

    public void startScanner() {
        new IntentIntegrator(this)
                .setCaptureActivity(BarcodeScannerActivity.class)
                .initiateScan();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == android.R.id.home){
            super.onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }
}
