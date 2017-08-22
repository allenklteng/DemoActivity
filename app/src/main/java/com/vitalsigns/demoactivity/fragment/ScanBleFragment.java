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
  private static final float GAIN_OF_BTN_TEXTVIEW_TEXT_SIZE = (0.5f);
  private static final float GAIN_OF_DEVICE_NAME_TEXTVIEW_TEXT_SIZE = (0.8f);
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
    final TextView tvDeviceName;

    tvScanBle = (TextView) getActivity().findViewById(R.id.textview_scan_ble);
    tvDeviceName = (TextView) getActivity().findViewById(R.id.textview_device_name);

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
          String strDeviceName = null;
          if(getArguments() != null)
          {
            strDeviceName = getArguments().getString(getString(R.string.connected_text), null);
          }

          /// [CC] : Set text size ; 08/21/2017
          tvScanBle.setTextSize(TypedValue.COMPLEX_UNIT_PX, (tvScanBle.getHeight() * GAIN_OF_BTN_TEXTVIEW_TEXT_SIZE));

          if(strDeviceName != null)
          {
            /// [CC] : Set text if connected ; 08/22/2017
            tvDeviceName.setText(getString(R.string.connected_text) + " " + strDeviceName);
            tvDeviceName.setTextSize(TypedValue.COMPLEX_UNIT_PX, tvScanBle.getTextSize() * GAIN_OF_DEVICE_NAME_TEXTVIEW_TEXT_SIZE);
            tvScanBle.setText(getString(R.string.disconnect_text));
          }
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

  /**
   * @brief showConnectedDeviceName
   *
   * Show device name if connected
   *
   * @param strName
   *
   * @return NULL
   */
  public void showConnectedDeviceName(final String strName)
  {
    if(!isAdded() && (getView() == null))
    {
      return;
    }

    getActivity().runOnUiThread(new Runnable() {
      @Override
      public void run() {
        TextView tvDeviceName;
        TextView tvScanBle;

        tvScanBle = (TextView) getActivity().findViewById(R.id.textview_scan_ble);
        tvDeviceName = (TextView) getActivity().findViewById(R.id.textview_device_name);
        if(strName != null)
        {
          tvDeviceName.setVisibility(View.VISIBLE);
          tvDeviceName.setText(getString(R.string.connected_text) + " " + strName);
          tvDeviceName.setTextSize(TypedValue.COMPLEX_UNIT_PX, tvScanBle.getTextSize() * GAIN_OF_DEVICE_NAME_TEXTVIEW_TEXT_SIZE);
        }
        else
        {
          tvDeviceName.setVisibility(View.INVISIBLE);
        }
      }
    });
  }

  /**
   * @brief setScanButtonText
   *
   * Set scan button text
   *
   * @param strBtn
   *
   * @return NULL
   */
  public void setScanButtonText(final String strBtn)
  {
    if(!isAdded() && (getView() == null))
    {
      return;
    }

    getActivity().runOnUiThread(new Runnable() {
      @Override
      public void run() {
        TextView tvScanBle;
        tvScanBle = (TextView) getActivity().findViewById(R.id.textview_scan_ble);
        tvScanBle.setText(strBtn);
      }
    });
  }

}
