package com.example.testopenxc;



import java.io.IOException;
import java.util.List;
import java.util.Locale;


import com.openxc.VehicleManager;


import com.openxc.measurements.EngineSpeed;
import com.openxc.measurements.FuelConsumed;
import com.openxc.measurements.FuelLevel;
import com.openxc.measurements.Latitude;
import com.openxc.measurements.Longitude;
import com.openxc.measurements.Measurement;
import com.openxc.measurements.Odometer;
import com.openxc.measurements.SteeringWheelAngle;
//import com.openxc.measurements.TurnSignalStatus;
//import com.openxc.measurements.TorqueAtTransmission;
import com.openxc.measurements.TransmissionGearPosition;
import com.openxc.measurements.UnrecognizedMeasurementTypeException;
import com.openxc.measurements.VehicleSpeed;
import com.openxc.remote.VehicleServiceException;

import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.provider.Settings;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;

import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;

import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.Menu;

import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {

	private VehicleManager mVehicleManager;
	private TextView mVehicleSpeedView;
    private boolean mIsBound;
    private final Handler mHandler = new Handler();

    
    private TextView mSteeringWheelAngleView;
    private TextView mFuelConsumedView;
    private TextView mFuelLevelView;
    private TextView mOdometerView;
    private TextView mVehicleEngineSpeedView;
    private TextView mTransmissionGearPosView;
   // private TextView mTorqueAtTransmissionView;
    
    private TextView mLatitudeView;
    private TextView mLongitudeView;
    private TextView mAndroidLatitudeView;
    private TextView mAndroidLongitudeView;
    private LocationManager mLocationManager;
    
    private boolean mGeocoderAvailable;
    private TextView mAddress;
    
    private Handler messageHandler;
    
    StringBuffer mBuffer;

	private ServiceConnection mConnection = new ServiceConnection() {
	    // Called when the connection with the service is established
	    public void onServiceConnected(ComponentName className,
	            IBinder service) {
	        Log.i("openxc", "Bound to VehicleManager");
	        mVehicleManager = ((VehicleManager.VehicleBinder)service).getService();
	        
	        try {
				mVehicleManager.addListener(VehicleSpeed.class, mSpeedListener);
				  mVehicleManager.addListener(SteeringWheelAngle.class,
	                        mSteeringWheelListener);
	            
	                mVehicleManager.addListener(FuelConsumed.class,
	                        mFuelConsumedListener);
	                mVehicleManager.addListener(FuelLevel.class,
	                        mFuelLevelListener);
	                mVehicleManager.addListener(Odometer.class,
	                        mOdometerListener);
	                mVehicleManager.addListener(EngineSpeed.class,
	                        mEngineSpeed);
	                mVehicleManager.addListener(TransmissionGearPosition.class,
	                        mTransmissionGearPos);
	          //      mVehicleManager.addListener(TorqueAtTransmission.class,mTorqueAtTransmission);
	                mVehicleManager.addListener(Latitude.class,
	                        mLatitude);
	                mVehicleManager.addListener(Longitude.class,
	                        mLongitude);
	                
			} catch (VehicleServiceException e) {
				// TODO Auto-generated catch block
				Log.w("openxc", "Couldn't add listeners for measurements", e);
				e.printStackTrace();
			} catch (UnrecognizedMeasurementTypeException e) {
				// TODO Auto-generated catch block
				Log.w("openxc", "Couldn't add listeners for measurements", e);
				e.printStackTrace();
			}
	        mIsBound = true;
	    }

	    // Called when the connection with the service disconnects unexpectedly
	    public void onServiceDisconnected(ComponentName className) {
	        Log.w("openxc", "VehicleService disconnected unexpectedly");
	        mVehicleManager = null;

            mIsBound = false;
	    }
	};
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		 Log.i("openxc", "Vehicle dashboard created");
		 
		mVehicleSpeedView = (TextView) findViewById(R.id.vehicle_speed);
		
		mSteeringWheelAngleView = (TextView) findViewById(
                R.id.steering_wheel_angle);
		
        
        mFuelConsumedView = (TextView) findViewById(
                R.id.fuel_consumed);
        mFuelLevelView = (TextView) findViewById(
                R.id.fuel_level);
        mOdometerView = (TextView) findViewById(
                R.id.odometer);
        mVehicleEngineSpeedView = (TextView) findViewById(
                R.id.engine_speed);
        mTransmissionGearPosView = (TextView) findViewById(
                R.id.transmission_gear_pos);
      //  mTorqueAtTransmissionView = (TextView) findViewById(R.id.torque_at_transmission);
        mLatitudeView = (TextView) findViewById(
                R.id.latitude);
        mLongitudeView = (TextView) findViewById(
                R.id.longitude);
        mAndroidLatitudeView = (TextView) findViewById(
                R.id.android_latitude);
        mAndroidLongitudeView = (TextView) findViewById(
                R.id.android_longitude);
        
        mAddress = (TextView) findViewById(R.id.address);
        
        // The isPresent() helper method is only available on Gingerbread or above.
        mGeocoderAvailable =
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD && Geocoder.isPresent();

        
                // Handler for updating text fields on the UI like the lat/long and address.
                messageHandler = new Handler() {
                    public void handleMessage(Message msg) {
                        switch (msg.what) {
                            case 1:
                                mAddress.setText((String) msg.obj);
                                
                                break;
                            case 2:
                               // mLatLng.setText((String) msg.obj);
                            	 mAddress.setText("null");
                                break;
                        }
                    }
                };
                
        mBuffer = new StringBuffer();
	    
	  //  Intent intent = new Intent(this, VehicleManager.class);
	   // bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
	}
	
    @Override
    public void onResume() {
        super.onResume();
        bindService(new Intent(this, VehicleManager.class),
                mConnection, Context.BIND_AUTO_CREATE);
        
         mLocationManager = (LocationManager)
                getSystemService(Context.LOCATION_SERVICE);
         final boolean gpsEnabled = mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

        // if (!gpsEnabled) {
             // Build an alert dialog here that requests that the user enable
             // the location services, then when the user clicks the "OK" button,
             // call enableLocationSettings()
       //      new EnableGpsDialogFragment().show(getSupportFragmentManager(), "enableGpsDialog");
       //  }
         
         
            try {
                mLocationManager.requestLocationUpdates(
                        VehicleManager.VEHICLE_LOCATION_PROVIDER, 0, 0,
                        mAndroidLocationListener);
            } catch(IllegalArgumentException e) {
                Log.w("openxc", "Vehicle location provider is unavailable");
            }
    }

    private void doReverseGeocoding(Location location) {
        // Since the geocoding API is synchronous and may take a while.  You don't want to lock
        // up the UI thread.  Invoking reverse geocoding in an AsyncTask.
        (new ReverseGeocodingTask(this)).execute(new Location[] {location});
    }

    private void updateUILocation(Location location) {
        // We're sending the update to a handler which then updates the UI with the new
        // location.;
    	String mGeoAvail = "false";
    	  if (mGeocoderAvailable) {
    		   mGeoAvail = "true";
    	  }

    	Log.i("openxc-update", ""+mGeoAvail);
        // Bypass reverse-geocoding only if the Geocoder service is available on the device.
        if (mGeocoderAvailable) doReverseGeocoding(location);
    }


    /**
     * Dialog to prompt users to enable GPS on the device.
     */
    @SuppressLint("ValidFragment")
	private class EnableGpsDialogFragment extends DialogFragment {

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            return new AlertDialog.Builder(getActivity())
                    .setTitle(R.string.enable_gps)
                    .setMessage(R.string.enable_gps_dialog)
                    .setPositiveButton(R.string.enable_gps, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            enableLocationSettings();
                        }
                    })
                    .create();
        }
    }
    
    // Method to launch Settings
    private void enableLocationSettings() {
        Intent settingsIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
        startActivity(settingsIntent);
    }

    // AsyncTask encapsulating the reverse-geocoding API.  Since the geocoder API is blocked,
    // we do not want to invoke it from the UI thread.
    private class ReverseGeocodingTask extends AsyncTask<Location, Void, Void> {
        Context mContext;

        public ReverseGeocodingTask(Context context) {
            super();
            mContext = context;
        }
        
    @Override
    protected Void doInBackground(Location... params) {
        Geocoder geocoder = new Geocoder(mContext, Locale.getDefault());

        Location loc = params[0];
        List<Address> addresses = null;
        try {
            addresses = geocoder.getFromLocation(loc.getLatitude(), loc.getLongitude(), 1);
        } catch (IOException e) {
            e.printStackTrace();
            // Update address field with the exception.
            Message.obtain(messageHandler, 1, e.toString()).sendToTarget();
           
        }
        if (addresses != null && addresses.size() > 0) {
            Address address = addresses.get(0);
            // Format the first line of address (if available), city, and country name.
            String addressText = String.format("%s, %s, %s",
                    address.getMaxAddressLineIndex() > 0 ? address.getAddressLine(0) : "",
                    address.getLocality(),
                    address.getCountryName());
         // Update address field on UI.
            Message.obtain(messageHandler, 1, addressText).sendToTarget();
            Log.i("openxc-address",""+ addressText);
            
        }
        return null;
    }
}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
		

	}
	
    public boolean onOptionsItemSelected(MenuItem item) {
    	
    	

            return super.onOptionsItemSelected(item);
        
    }

	
	public void onPause() {
	    super.onPause();
	    
        if(mIsBound) {
	    Log.i("openxc", "Unbinding from vehicle service");
	    unbindService(mConnection);
	    try {
			mLocationManager.removeUpdates(mAndroidLocationListener);
			mLocationManager =null;
			Log.i("openxc-log","location manager released");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    
	    
	    
	    
            mIsBound = false;
        }
	}
	
	VehicleSpeed.Listener mSpeedListener = new VehicleSpeed.Listener() {
	    private int count;

		public void receive(Measurement measurement) {
	        count = 0;
	        final VehicleSpeed speed = (VehicleSpeed) measurement;
	        if (0.0<speed.getValue().doubleValue() &&speed.getValue().doubleValue()  <10.0) count+=1;
	        MainActivity.this.runOnUiThread(new Runnable() {
	        	
	            public void run() {
	             	
	                mVehicleSpeedView.setText(
	                    "Vehicle speed (km/h): [" + count+"]"+ speed.getValue().doubleValue());
	               
	               
	                	
	                	if(count==1){
	                	Context context = getApplicationContext();
	                	CharSequence text ="["+ count+"]"+"OMG,Traffic is so slow here";
	                	
	                	Toast toast = Toast.makeText(context, text, 0);
	                	toast.show();
	                	
	                	//Toast.makeText(context, text , Toast.LENGTH_SHORT).show();
	                	count=0;
	                	CharSequence text2 = count+ "";
	                	// toast.cancel();
	                    // toast.setText(text2);
	                	}
	                	
	                
	            }
	        });
	    }
	    
	};
	
    SteeringWheelAngle.Listener mSteeringWheelListener =
            new SteeringWheelAngle.Listener() {
        public void receive(Measurement measurement) {
            final SteeringWheelAngle angle = (SteeringWheelAngle) measurement;
            mHandler.post(new Runnable() {
                public void run() {
                    mSteeringWheelAngleView.setText(
                        "" + angle.getValue().doubleValue());
                }
            });
        }
    };
	
	  FuelConsumed.Listener mFuelConsumedListener = new FuelConsumed.Listener() {
	        public void receive(Measurement measurement) {
	            final FuelConsumed fuel = (FuelConsumed) measurement;
	            mHandler.post(new Runnable() {
	                public void run() {
	                    mFuelConsumedView.setText(
	                        "" + fuel.getValue().doubleValue());
	                }
	            });
	        }
	    };

	    FuelLevel.Listener mFuelLevelListener = new FuelLevel.Listener() {
	        public void receive(Measurement measurement) {
	            final FuelLevel level = (FuelLevel) measurement;
	            mHandler.post(new Runnable() {
	                public void run() {
	                    mFuelLevelView.setText(
	                        "" + level.getValue().doubleValue());
	                }
	            });
	        }
	    };
	    EngineSpeed.Listener mEngineSpeed = new EngineSpeed.Listener() {
	        public void receive(Measurement measurement) {
	            final EngineSpeed status = (EngineSpeed) measurement;
	            mHandler.post(new Runnable() {
	                public void run() {
	                    mVehicleEngineSpeedView.setText(
	                        "" + status.getValue().doubleValue());
	                }
	            });
	        }
	    };


	    Odometer.Listener mOdometerListener = new Odometer.Listener() {
	        public void receive(Measurement measurement) {
	            final Odometer odometer = (Odometer) measurement;
	            mHandler.post(new Runnable() {
	                public void run() {
	                    mOdometerView.setText(
	                        "" + odometer.getValue().doubleValue());
	                }
	            });
	        }
	    };

	    TransmissionGearPosition.Listener mTransmissionGearPos =
	            new TransmissionGearPosition.Listener() {
	        public void receive(Measurement measurement) {
	            final TransmissionGearPosition status =
	                    (TransmissionGearPosition) measurement;
	            mHandler.post(new Runnable() {
	                public void run() {
	                    mTransmissionGearPosView.setText(
	                        "" + status.getValue().enumValue());
	                }
	            });
	        }
	    };
	    
	    /*
	    TorqueAtTransmission.Listener mTorqueAtTransmission =
	            new TorqueAtTransmission.Listener() {
	        public void receive(Measurement measurement) {
	            final TorqueAtTransmission status = (TorqueAtTransmission) measurement;
	            mHandler.post(new Runnable() {
	                public void run() {
	                    mTorqueAtTransmissionView.setText(
	                        "" + status.getValue().doubleValue());
	                }
	            });
	        }
	    };
	    
*/
	    Latitude.Listener mLatitude =
	            new Latitude.Listener() {
	        public void receive(Measurement measurement) {
	            final Latitude lat = (Latitude) measurement;
	            mHandler.post(new Runnable() {
	                public void run() {
	                    mLatitudeView.setText(
	                        "" + lat.getValue().doubleValue());
	                }
	            });
	        }
	    };

	    Longitude.Listener mLongitude =
	            new Longitude.Listener() {
	        public void receive(Measurement measurement) {
	            final Longitude lng = (Longitude) measurement;
	            mHandler.post(new Runnable() {
	                public void run() {
	                    mLongitudeView.setText(
	                        "" + lng.getValue().doubleValue());
	                }
	            });
	        }
	    };
	    
	    LocationListener mAndroidLocationListener = new LocationListener() {
	        public void onLocationChanged(final Location location) {
	            mHandler.post(new Runnable() {
	                public void run() {
	                    mAndroidLatitudeView.setText("" +
	                        location.getLatitude());
	                    mAndroidLongitudeView.setText("" +
	                        location.getLongitude());
	                    Log.i("openxc-lat",""+location.getLatitude());
	                    Log.i("openxc-log", ""+location.getLongitude());
	    
	                        // A new location update is received.  Do something useful with it.  Update the UI with
	                        // the location update.
	                        updateUILocation(location);
	                 

	                    
	                    
	                }
	            });
	        }

	        public void onStatusChanged(String provider, int status, Bundle extras) {}
	        public void onProviderEnabled(String provider) {}
	        public void onProviderDisabled(String provider) {}
	    };
}
