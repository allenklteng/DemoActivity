package com.vitalsigns.demoactivity.fragment;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.vitalsigns.demoactivity.R;

public class SleepMonitorFragment extends Fragment
{
  private OnSleepMonitorFragmentListener mListener;

  public interface OnSleepMonitorFragmentListener {
    void onConnectionFirst();
  }

  public SleepMonitorFragment() {
    // Required empty public constructor
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
    return inflater.inflate(R.layout.fragment_sleep_monitor, container, false);
  }

  /**
   * @brief SetCallback
   *
   * Set sleep monitor fragment callback
   *
   * @return NULL
   */
  public void SetCallback(SleepMonitorFragment.OnSleepMonitorFragmentListener callback)
  {
    mListener = callback;
  }

  @Override
  public void onResume() {
    super.onResume();

    if((!isAdded()) || (getArguments() == null))
    {
      return;
    }

    if(!getArguments().getBoolean(getString(R.string.device_connection_check), false))
    {
      if(mListener != null)
      {
        mListener.onConnectionFirst();
      }
    }
  }
}
