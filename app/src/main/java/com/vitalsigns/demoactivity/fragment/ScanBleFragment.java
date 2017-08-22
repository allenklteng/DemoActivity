package com.vitalsigns.demoactivity.fragment;
import android.app.Fragment;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.TextView;

import com.vitalsigns.demoactivity.R;

public class ScanBleFragment extends Fragment
{
  private static final String LOG_TAG = "ScanBleFragment:";
  private static final float GAIN_OF_TEXTVIEW_TEXT_SIZE = (0.5f);
  private OnScanBleFragmentListener mListener;

  public interface OnScanBleFragmentListener {
    void onScanBleDevice();
  }

  public ScanBleFragment() {
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
  }

  @Override
  public View onCreateView(LayoutInflater inflater,
                           ViewGroup container,
                           Bundle savedInstanceState)
  {
    return inflater.inflate(R.layout.fragment_scan_ble, container, false);
  }

  @Override
  public void onResume()
  {
    super.onResume();
    final TextView tvScanBle;
    tvScanBle = (TextView) getActivity().findViewById(R.id.textview_scan_ble);

    if(isAdded() && getView() != null)
    {
      /// [CC] : Set textview click event ; 08/21/2017
      tvScanBle.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View view) {
          mListener.onScanBleDevice();
        }
      });

      tvScanBle.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
        @Override
        public void onGlobalLayout()
        {
          /// [CC] : Set text size ; 08/21/2017
          tvScanBle.setTextSize(TypedValue.COMPLEX_UNIT_PX, (tvScanBle.getHeight() * GAIN_OF_TEXTVIEW_TEXT_SIZE));
          tvScanBle.getViewTreeObserver().removeOnGlobalLayoutListener(this);
        }
      });
    }
  }

  /**
   * @brief SetCallback
   *
   * Set scanBle fragment callback
   *
   * @return NULL
   */
  public void SetCallback(ScanBleFragment.OnScanBleFragmentListener callback)
  {
    mListener = callback;
  }
}
