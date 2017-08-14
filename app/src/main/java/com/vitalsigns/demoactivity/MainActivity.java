package com.vitalsigns.demoactivity;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity
{
  private TextView mTvScanBle;
  private TextView mTvPedometer;
  private TextView mTvSleepMonitor;

  @Override
  protected void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
  }

  @Override
  protected void onStart()
  {
    super.onStart();

    setBottombar();

    /// [AT-PM] : Set initial tab ; 08/14/2017
    mTvScanBle.setTextColor(getResources().getColor(R.color.colorAccent));
    showScanBle();
  }

  @Override
  protected void onStop()
  {
    super.onStop();

    removeBottombar();
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
   * Show Scan BLE fragment
   */
  private void showScanBle()
  {

  }

  /**
   * Show Pedomter fragment
   */
  private void showPedometer()
  {

  }

  /**
   * Show Sleep Monitor fragment
   */
  private void showSleepMonitor()
  {

  }
}
