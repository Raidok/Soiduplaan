package ee.tallinn.soiduplaan;

import java.io.File;

import org.apache.cordova.DroidGap;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Environment;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

public class MainActivity extends DroidGap {
	
	private ResponseReceiver receiver;
	private static final String INTERNAL = "file:///android_asset/www/index.html";
	private static final String EXTERNAL = "file://" + Environment.getExternalStorageDirectory().getAbsolutePath() + "/Android/data/ee.tallinn.soiduplaan/files/index.html";
	
    @Override
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (new File(EXTERNAL).exists()) {
        	super.loadUrl(EXTERNAL);
        } else {
        	super.loadUrl(INTERNAL);
        }
        IntentFilter filter = new IntentFilter(ResponseReceiver.ACTION_RESP);
        filter.addCategory(Intent.CATEGORY_DEFAULT);
        receiver = new ResponseReceiver();
        registerReceiver(receiver, filter);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }


	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
        case R.id.action_update:
            startUpdate(item);
            return true;
        case R.id.action_settings:
            showSettings();
            return true;
        default:
            return super.onOptionsItemSelected(item);
    }
	}


	private void startUpdate(MenuItem item) {
		Toast.makeText(getContext(), "Võtab natukene aega...", Toast.LENGTH_LONG).show();
        Intent intent = new Intent(this, UpdateService.class);
        startService(intent);
	}


	private void showSettings() {
		Toast.makeText(getContext(), "Siin pole midagi :)", Toast.LENGTH_LONG).show();
	}
    
	public class ResponseReceiver extends BroadcastReceiver {
		public static final String ACTION_RESP = "MESSAGE_PROCESSED";
		@Override
		public void onReceive(Context context, Intent intent) {
			String text = intent.getStringExtra(UpdateService.MSG);
			int duration = Toast.LENGTH_SHORT;

			Toast toast = Toast.makeText(context, text, duration);
			toast.show();
			
			MainActivity.this.loadUrl(EXTERNAL);
		}
	}
}
