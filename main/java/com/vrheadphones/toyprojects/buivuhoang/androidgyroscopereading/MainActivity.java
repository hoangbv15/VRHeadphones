package com.vrheadphones.toyprojects.buivuhoang.androidgyroscopereading;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import java.util.Vector;

/*
 * To-do
 * 
 * DNS lookup
 * arrow keys, esc, win key
 */

public class MainActivity extends Activity {
	private static final String TAG = "RemoteDroid";

	//
	private EditText tbIp;
	//
//	private DiscoverThread discover;
	private Handler handler;
	private SimpleAdapter adapter;
	private Vector<String> hostlist;

	public MainActivity() {
		super();
	}

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE); // added to save screen space, the Title was shown twice, in Standard Android bar, then below in Bolder larger text, this gets rid of the standard android bar
		setContentView(R.layout.activity_main);
		//
		this.handler = new Handler();
		// set some listeners
		Button but = (Button)this.findViewById(R.id.btnConnect);
		but.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				onConnectButton();
			}
		});

		// check SharedPreferences for IP
		Settings.init(this.getApplicationContext());

		//
		this.tbIp = (EditText)this.findViewById(R.id.etIp);
		if (Settings.ip != null) {
			this.tbIp.setText(Settings.ip);
		}
		//
		// discover some servers
		this.hostlist = new Vector<String>();
		((ListView)this.findViewById(R.id.lvHosts)).setOnItemClickListener(new AdapterView.OnItemClickListener() {
			public void onItemClick(AdapterView adapter, View v, int position, long id) {
				onHostClick(position);
			}
		});
	}

	private void updateHostList() {
		FoundHostsAdapter adapter = new FoundHostsAdapter(this.hostlist, this.getApplication());
		((ListView)this.findViewById(R.id.lvHosts)).setAdapter(adapter);
	}


	/** OS kills process */
	public void onDestroy() {
		super.onDestroy();
	}

	/** App starts anything it needs to start */
	public void onStart() {
		super.onStart();
	}

	/** App kills anything it started */
	public void onStop() {
		super.onStop();
	}

	/** App starts displaying things */
	public void onResume() {
		super.onResume();
//		this.discover = new DiscoverThread(new DiscoverThread.DiscoverListener() {
//			public void onAddressReceived(String address) {
//				hostlist.add(address);
//				Log.d(TAG, "Got host back, "+address);
//				handler.post(new Runnable() {
//					public void run() {
//						updateHostList();
//					}
//				});
//			}
//		});
//		this.discover.start();
	}


	/** App goes into background */
	public void onPause() {
		super.onPause();
//		this.discover.closeSocket();
	}

	private void onConnectButton() {
		String ip = this.tbIp.getText().toString();
		if (ip.matches("^[0-9]{1,4}\\.[0-9]{1,4}\\.[0-9]{1,4}\\.[0-9]{1,4}$")) {
			try {
				Settings.setIp(ip);
				//
				Intent i = new Intent(this, VRActivity.class);
				this.startActivity(i);
				this.finish();
			} catch (Exception ex) {
				Toast.makeText(this, this.getResources().getText(R.string.toast_invalidIP), Toast.LENGTH_LONG).show();
				Log.d(TAG, ex.toString());
			}
		} else {
			Toast.makeText(this, this.getResources().getText(R.string.toast_invalidIP), Toast.LENGTH_LONG).show();
		}
	}
	
	private void onHostClick(int item) {
		this.tbIp.setText(this.hostlist.get(item));
		this.onConnectButton();
	}
}
