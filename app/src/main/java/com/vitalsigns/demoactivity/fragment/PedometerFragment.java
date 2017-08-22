package com.vitalsigns.demoactivity.fragment;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.vitalsigns.demoactivity.R;

public class PedometerFragment extends Fragment
{
  private OnPedometerFragmentListener mListener;

  public interface OnPedometerFragmentListener {
    void onGetPedometerData();
  }

  public PedometerFragment() {
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
    return inflater.inflate(R.layout.fragment_pedometer, container, false);
  }

  @Override
  public void onResume() {
    super.onResume();
  }

  @Override
  public void onStop() {
    super.onStop();
  }

  /**
   * @brief SetCallback
   *
   * Set pedometer fragment callback
   *
   * @return NULL
   */
  public void SetCallback(PedometerFragment.OnPedometerFragmentListener callback)
  {
    mListener = callback;
  }
}
