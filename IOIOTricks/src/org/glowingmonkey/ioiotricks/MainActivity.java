package org.glowingmonkey.ioiotricks;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.text.method.ScrollingMovementMethod;
import android.view.Menu;
import android.view.View;
import android.widget.TextView;

public class MainActivity extends Activity {
	private TextView _log;
    /** Messenger for communicating with the service. */
    Messenger mService = null;

    /** Flag indicating whether we have called bind on the service. */
    boolean mBound;
    
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
	protected void onStart() {
		super.onStart();
        // Bind to the service
        bindService(new Intent(this, W5100Service.class), mConnection,
            Context.BIND_AUTO_CREATE);
	}
//
//	@Override
//	protected void onPause() {
//		super.onPause();
//		stopService(new Intent(this, W5100Service.class));
//	}
//
//	@Override
//	protected void onResume() {
//		super.onResume();
//		Intent serviceIntent = new Intent(this, W5100Service.class);
//		Messenger messenger = new Messenger(handler);
//		serviceIntent.putExtra("MESSENGER", messenger);
//		startService(serviceIntent);
//	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

    /**
     * Class for interacting with the main interface of the service.
     */
    private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            // This is called when the connection with the service has been
            // established, giving us the object we can use to
            // interact with the service.  We are communicating with the
            // service using a Messenger, so here we get a client-side
            // representation of that from the raw IBinder object.
            mService = new Messenger(service);
            mBound = true;
        }

        public void onServiceDisconnected(ComponentName className) {
            // This is called when the connection with the service has been
            // unexpectedly disconnected -- that is, its process crashed.
            mService = null;
            mBound = false;
        }
    };
	
	// Handler for messages from the service
	private Handler handler = new Handler() {
	    @Override
		public void handleMessage(Message message) {
			switch(message.what) {
				case 1:
					_log.append((String)message.obj + "\n");
			}
	    };
	};

	public void onDHCPClicked(View v){
		 if (!mBound) return;
		 Message msg = Message.obtain(null, W5100Service.MSG_DHCP, 0, 0);
        try {
            mService.send(msg);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
	}
}
