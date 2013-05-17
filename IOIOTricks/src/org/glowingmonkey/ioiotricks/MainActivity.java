package org.glowingmonkey.ioiotricks;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Messenger;
import android.text.method.ScrollingMovementMethod;
import android.view.Menu;
import android.widget.TextView;

public class MainActivity extends Activity {
	private TextView _log;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		_log = (TextView) findViewById(R.id.log);
		_log.setMovementMethod(new ScrollingMovementMethod());

		Intent serviceIntent = new Intent(this, W5100Service.class);
		Messenger messenger = new Messenger(handler);
		serviceIntent.putExtra("MESSENGER", messenger);
		startService(serviceIntent);
	}

	@Override
	protected void onPause() {
		super.onPause();
		stopService(new Intent(this, W5100Service.class));
	}

	@Override
	protected void onResume() {
		super.onResume();
		Intent serviceIntent = new Intent(this, W5100Service.class);
		Messenger messenger = new Messenger(handler);
		serviceIntent.putExtra("MESSENGER", messenger);
		startService(serviceIntent);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	private Handler handler = new Handler() {
	    public void handleMessage(Message message) {
			switch(message.what) {
				case 1:
					_log.append((String)message.obj + "\n");
			}
	    };
	};

}
