package com.vitalsigns.demoactivity;

import android.bluetooth.BluetoothDevice;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.vitalsigns.demoactivity.ble.DemoBle;
import com.vitalsigns.demoactivity.fragment.ScanBleFragment;
import com.vitalsigns.sdk.ble.scan.DeviceListFragment;

public class MainActivity extends AppCompatActivity
  implements DeviceListFragment.OnEvent
{
  private static final String LOG_TAG = "MainActivity:";
  private TextView mTvScanBle;
  private TextView mTvPedometer;
  private TextView mTvSleepMonitor;
  private DemoBle mDemoBle;

  @Override
  protected void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    /// [CC] : Permission request ; 08/21/2017
    Utility.RequestPermissionAccessCoarseLocation(this,
                                                  getString(R.string.request_permission_coarse_location_title),
                                                  getString(R.string.request_permission_coarse_location_content),
                                                  null);

    Utility.RequestPermissionAccessExternalStorage(this,
                                                   getString(R.string.request_permission_access_storage_title),
                                                   getString(R.string.request_permission_access_storage_content),
                                                   null);
  }

  @Override
  protected void onStart()
  {
    super.onStart();

    setBottombar();

    /// [CC] : Ble module initial ; 08/21/2017
    bleInit();

    /// [AT-PM] : Set initial tab ; 08/14/2017
    mTvScanBle.setTextColor(getResources().getColor(R.color.colorAccent));
    showScanBle();
  }

  @Override
  protected void onStop()
  {
    super.onStop();

    removeBottombar();

    bleUnInit();
  }

  /**
   * Setup bottom bar
   */
  private void setBottombar()
  {
    LinearLayout bottombar = (LinearLayout)findViewById(R.id.bottombar);

    /// [AT-PM] : Create Scan BLE tab ; 08/14/2017
    mTvScanBle = new TextView(this);
    mTvScanBle.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                                                             ViewGroup.LayoutParams.MATCH_PARENT,
                                                             1.0f));
    mTvScanBle.setText(R.string.bottombar_ble_scan);
    mTvScanBle.setTextColor(getResources().getColor(R.color.colorWhite));
    mTvScanBle.setGravity(Gravity.CENTER);
    mTvScanBle.setTextAlignment(View.TEXT_ALIGNMENT_GRAVITY);
    mTvScanBle.setOnClickListener(mBottombarSelect);
    bottombar.addView(mTvScanBle);

    /// [AT-PM] : Create Pedomter tab ; 08/14/2017
    mTvPedometer = new TextView(this);
    mTvPedometer.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                                                               ViewGroup.LayoutParams.MATCH_PARENT,
                                                               1.0f));
    mTvPedometer.setText(R.string.bottombar_pedometer);
    mTvPedometer.setTextColor(getResources().getColor(R.color.colorWhite));
    mTvPedometer.setGravity(Gravity.CENTER);
    mTvPedometer.setTextAlignment(View.TEXT_ALIGNMENT_GRAVITY);
    mTvPedometer.setOnClickListener(mBottombarSelect);
    bottombar.addView(mTvPedometer);

    /// [AT-PM] : Create Sleep Monitor tab ; 08/14/2017
    mTvSleepMonitor = new TextView(this);
    mTvSleepMonitor.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                                                                  ViewGroup.LayoutParams.MATCH_PARENT,
                                                                  1.0f));
    mTvSleepMonitor.setText(R.string.bottombar_sleep_monitor);
    mTvSleepMonitor.setTextColor(getResources().getColor(R.color.colorWhite));
    mTvSleepMonitor.setGravity(Gravity.CENTER);
    mTvSleepMonitor.setTextAlignment(View.TEXT_ALIGNMENT_GRAVITY);
    mTvSleepMonitor.setOnClickListener(mBottombarSelect);
    bottombar.addView(mTvSleepMonitor);
  }

  /**
   * Remove bottom bar
   */
  private void removeBottombar()
  {
    LinearLayout bottombar = (LinearLayout)findViewById(R.id.bottombar);
    bottombar.removeView(mTvScanBle);
    bottombar.removeView(mTvPedometer);
    bottombar.removeView(mTvSleepMonitor);
    mTvScanBle = null;
    mTvPedometer = null;
    mTvSleepMonitor = null;
  }

  private TextView.OnClickListener mBottombarSelect = new View.OnClickListener()
  {
    @Override
    public void onClick(View view)
    {
      TextView tv = (TextView)view;

      /// [AT-PM] : Clear all tabs ; 08/14/2017
      mTvScanBle.setTextColor(getResources().getColor(R.color.colorWhite));
      mTvPedometer.setTextColor(getResources().getColor(R.color.colorWhite));
      mTvSleepMonitor.setTextColor(getResources().getColor(R.color.colorWhite));

      if(tv.equals(mTvScanBle))
      {
        mTvScanBle.setTextColor(getResources().getColor(R.color.colorAccent));
        showScanBle();
      }
      else if(tv.equals(mTvPedometer))
      {
        mTvPedometer.setTextColor(getResources().getColor(R.color.colorAccent));
        showPedometer();
      }
      else if(tv.equals(mTvSleepMonitor))
      {
        mTvSleepMonitor.setTextColor(getResources().getColor(R.color.colorAccent));
        showSleepMonitor();
      }
      else
      {
        mTvScanBle.setTextColor(getResources().getColor(R.color.colorAccent));
        showScanBle();
      }
    }
  };

  /**
   * @brief showScanBle
   *
   * Show Scan BLE fragment
   *
   * @return NULL
   */
  private void showScanBle()
  {
    ScanBleFragment fragment;
    fragment = new ScanBleFragment();

    fragment.SetCallback(scanBleFragmentListener);
    getFragmentManager().beginTransaction().replace(R.id.fragment_container_layout,
                                                    fragment,
                                                    getString(R.string.fragment_tag_scan_ble))
                                           .commitAllowingStateLoss();
  }

  private ScanBleFragment.OnScanBleFragmentListener scanBleFragmentListener = new ScanBleFragment.OnScanBleFragmentListener()
  {
    @Override
    public void onScanBleDevice()
    {
      if((mDemoBle != null) && (mDemoBle.isConnect()))
      {
        mDemoBle.disconnect();
        return;
      }

      /// [CC] : Scan device ; 08/21/2017
      DeviceListFragment fragment = DeviceListFragment.newInstance(DeviceListFragment.ACTION_SCAN_BLE_DEVICE,
        DeviceListFragment.STYLE_WHITE);
      getFragmentManager().beginTransaction()
        .add(fragment, getResources().getString(R.string.device_list_fragment_tag))
        .commitAllowingStateLoss();
    }
  };

  /**
   * @brief showScanBle
   *
   * Show Pedometer fragment
   *
   * @return NULL
   */
  private void showPedometer()
  {

  }

  /**
   * @brief showScanBle
   *
   * Show SleepMonitor fragment
   *
   * @return NULL
   */
  private void showSleepMonitor()
  {

  }

  /**
   * @brief bleInit
   *
   * Initialize BLE module
   *
   * @return NULL
   */
  private void bleInit()
  {
    mDemoBle = new DemoBle(MainActivity.this, mDemoBleEvent);
  }

  /**
   * @brief bleUnInit
   *
   * Un-initialize BLE module
   *
   * @return NULL
   */
  private void bleUnInit()
  {
    if(mDemoBle == null)
    {
      return;
    }

    if(mDemoBle.isConnect())
    {
      mDemoBle.disconnect();
    }

    mDemoBle.destroy();
    mDemoBle = null;
  }

  /**
   * @brief mDemoBleEvent
   *
   * Callback of DemoBle.DemoBleEvent
   *
   */
  private DemoBle.DemoBleEvent mDemoBleEvent = new DemoBle.DemoBleEvent()
  {
    @Override
    public void onDisconnect() {
      runOnUiThread(new Runnable() {
        @Override
        public void run() {
          Toast.makeText(getApplicationContext(), "Disconnection with device", Toast.LENGTH_LONG).show();
        }
      });
    }

    @Override
    public void onConnect() {
      runOnUiThread(new Runnable() {
        @Override
        public void run() {
          Toast.makeText(getApplicationContext(), "Connection with device", Toast.LENGTH_LONG).show();
        }
      });
    }
  };

  /**
   * @brief onBleDeviceSelected
   *
   * Callback of DeviceListFragment
   *
   */
  @Override
  public void onBleDeviceSelected(String strDeviceAddress)
  {
    if(strDeviceAddress == null)
    {
      Log.d(LOG_TAG, "Device address is null");
      return;
    }

    if(mDemoBle != null)
    {
      /// [CC] : Connect BLE device ; 08/21/2017
      mDemoBle.connect(strDeviceAddress);
    }
  }

  /**
   * @brief onDfuDeviceSelected
   *
   * Callback of DeviceListFragment
   *
   */
  @Override
  public void onDfuDeviceSelected(BluetoothDevice bluetoothDevice) {
    Log.d(LOG_TAG, "onDfuDeviceSelected");
  }

  /**
   * @brief onSendCrashMsg
   *
   * Callback of DeviceListFragment
   *
   */
  @Override
  public void onSendCrashMsg(String s, String s1) {
    Log.d(LOG_TAG, "onSendCrashMsg");
  }
}
