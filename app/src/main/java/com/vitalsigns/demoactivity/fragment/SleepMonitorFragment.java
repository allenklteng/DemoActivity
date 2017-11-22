package com.vitalsigns.demoactivity.fragment;

import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import com.vitalsigns.demoactivity.R;
import com.vitalsigns.sdk.ble.BleSleepData;
import com.vitalsigns.sdk.utility.Utility;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class SleepMonitorFragment extends Fragment {
  private static final String LOG_TAG = "SleepMonitorFragment:";
  private static final int SLEEP_MODE_WAKE_UP = 0;
  private static final int SLEEP_MODE_SHALLOW = 1;
  private static final int SLEEP_MODE_DEEP = 2;
  private static final float TABLELAYOUT_TEXT_SIZE = (25f);
  private static final int TABLELAYOUT_TEXT_PADDING = (10);
  private OnSleepMonitorFragmentListener mListener;
  private ProgressDialog mProgressDialog;
  private Handler mUpdateSleepMonitorDataHandler = null;

  public interface OnSleepMonitorFragmentListener
  {
    ArrayList<BleSleepData> onGetSleepMonitorData();
    void onConnectionFirst();
    Looper onGetLooper();
    void onResetSleepData();
  }

  public SleepMonitorFragment() {
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState)
  {
    View view = inflater.inflate(R.layout.fragment_sleep_monitor, container, false);
    view.findViewById(R.id.sleep_data_reset).setOnClickListener(mOnClickResetData);
    return (view);
  }

  /**
   * @brief SetCallback
   *
   * Set sleep monitor fragment callback
   *
   * @param callback
   *
   * @return NULL
   */
  public void SetCallback(SleepMonitorFragment.OnSleepMonitorFragmentListener callback) {
    mListener = callback;
  }

  @Override
  public void onResume() {
    super.onResume();

    if ((!isAdded()) || (getArguments() == null)) {
      return;
    }

    if (!getArguments().getBoolean(getString(R.string.device_connection_check), false)) {
      if (mListener != null) {
        mListener.onConnectionFirst();
        return;
      }
    }

    getSleepMonitorData();
  }

  @Override
  public void onStop() {
    super.onStop();
    hideProgressDialog();
  }


  /**
   * @brief getSleepMonitorData
   *
   * Get sleep monitor data
   *
   * @return NULL
   */
  private void getSleepMonitorData()
  {
    if (mListener == null)
    {
      return;
    }

    showProgressDialog();

    /// [AT-PM] : Start a handler to read sleep monitor data ; 11/15/2017
    if(mUpdateSleepMonitorDataHandler != null)
    {
      mUpdateSleepMonitorDataHandler.removeCallbacksAndMessages(null);
      mUpdateSleepMonitorDataHandler = null;
    }
    mUpdateSleepMonitorDataHandler = new Handler(mListener.onGetLooper());
    mUpdateSleepMonitorDataHandler.post(mRunnableReadSleepMonitorData);
  }

  /**
   * @brief showProgressDialog
   *
   * Show Progress Dialog
   *
   * @return NULL
   */
  private void showProgressDialog() {
    if (mProgressDialog == null) {
      mProgressDialog = new ProgressDialog(getActivity(), R.style.DialogStyle);
      mProgressDialog.setMessage(getString(R.string.sleep_monitor_data_loading));
      mProgressDialog.setIndeterminate(true);
      mProgressDialog.setCanceledOnTouchOutside(false);

      mProgressDialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
        @Override
        public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
          if (mProgressDialog != null && mProgressDialog.isShowing()) {
            /// [CC] : Do nothing ; 07/10/2017
            return true;
          }
          return false;
        }
      });
    }

    mProgressDialog.show();
  }

  /**
   * @brief hideProgressDialog
   *
   * Hide Progress Dialog
   *
   * @return null
   */
  private void hideProgressDialog()
  {
    if (mProgressDialog != null && mProgressDialog.isShowing()) {
      getActivity().runOnUiThread(new Runnable() {
        @Override
        public void run() {
          mProgressDialog.dismiss();
        }
      });
    }
  }

  /**
   * @brief displayData
   *
   * Use table display data
   *
   * @param dataArrayList BleSleepData array
   * @return NULL
   */
  private void displayData(ArrayList<BleSleepData> dataArrayList)
  {
    /// [CC] Return if no data ; 08/22/2017
    if ((dataArrayList == null) || (dataArrayList.size() <= 0))
    {
      /// [CC] Hide Progress Dialog ; 08/22/2017
      hideProgressDialog();
      return;
    }

    if (!isAdded() || (getView() == null))
    {
      /// [CC] Hide Progress Dialog ; 08/22/2017
      hideProgressDialog();
      return;
    }

    /// [CC] : Save data ; 08/22/2017
    saveSleepMonitorDataToCsv(dataArrayList);

    /// [CC] : Show data ; 08/22/2017
    showSleepMonitorData(dataArrayList);

    /// [CC] Hide Progress Dialog ; 08/22/2017
    hideProgressDialog();
  }

  /**
   * @brief saveSleepMonitorDataToCsv
   *
   * Save sleep monitor data as csv file
   *
   * @param dataArrayList BleSleepData array
   *
   * @return NULL
   */
  private void saveSleepMonitorDataToCsv(ArrayList<BleSleepData> dataArrayList)
  {
    String strFilename;
    FileWriter fileWriter;
    BufferedWriter bufferedWriter;
    strFilename = Environment.getExternalStorageDirectory().getAbsolutePath() +
      File.separator + "SleepMonitorData." + Utility.GetFileDateTime() + ".csv";

    try
    {
      fileWriter = new FileWriter(strFilename, true);
      bufferedWriter = new BufferedWriter(fileWriter);
      bufferedWriter.write("Month, Day, Hour, Minute, Mode, G-Code, Remain day, Index");
      bufferedWriter.newLine();

      for (BleSleepData bleSleepData : dataArrayList)
      {
        bufferedWriter.write(String.format("%d, %d, %d, %d, %s, %d, %d, %d",
                             bleSleepData.getMonth(),
                             bleSleepData.getDay(),
                             bleSleepData.getHour(),
                             bleSleepData.getMinute(),
                             parseSleepMode(bleSleepData.getMode()),
                             bleSleepData.getGCode(),
                             bleSleepData.getRemainDay(),
                             bleSleepData.getIdx()));
        bufferedWriter.newLine();
      }

      bufferedWriter.close();
      fileWriter.close();
    }
    catch(IOException e)
    {
      e.printStackTrace();
    }
  }

  /**
   * @brief parseSleepMode
   *
   * Save sleep monitor data as csv file
   *
   * @para nMode sleep mode with integer
   *
   * @return sleep mode with string
   */
  private String parseSleepMode(int nMode)
  {
    String strMode;
    switch(nMode)
    {
      case SLEEP_MODE_WAKE_UP:
        strMode = getString(R.string.sleep_monitor_mode_wake_up);
        break;

      case SLEEP_MODE_SHALLOW:
        strMode = getString(R.string.sleep_monitor_mode_shallow);
        break;

      case SLEEP_MODE_DEEP:
        strMode = getString(R.string.sleep_monitor_mode_deep);
        break;

      default:
        strMode = getString(R.string.sleep_monitor_mode_wake_up);
        break;
    }

    return (strMode);
  }

  /**
   * @brief showSleepMonitorData
   *
   * Show pedometer data
   *
   * @para arrayList BleSleepData array
   *
   * @return NULL
   */
  private void showSleepMonitorData(ArrayList<BleSleepData> arrayList)
  {
    TableLayout tableLayout;
    String[] strTitle;
    int nDataCnt;

    tableLayout = (TableLayout) getView().findViewById(R.id.sleep_monitor_table_layout);
    strTitle = new String[]{"Month",
                            "Day",
                            "Hour",
                            "Minute",
                            "Mode",
                            "G-Code",
                            "Remain day",
                            "Index"};

    setTableRaw(tableLayout, strTitle);

    for (nDataCnt = 0; nDataCnt < arrayList.size(); nDataCnt++)
    {
      strTitle = new String[]{
        String.valueOf(arrayList.get(nDataCnt).getMonth()),
        String.valueOf(arrayList.get(nDataCnt).getDay()),
        String.valueOf(arrayList.get(nDataCnt).getHour()),
        String.valueOf(arrayList.get(nDataCnt).getMinute()),
        parseSleepMode(arrayList.get(nDataCnt).getMode()),
        String.valueOf(arrayList.get(nDataCnt).getGCode()),
        String.valueOf(arrayList.get(nDataCnt).getRemainDay()),
        String.valueOf(arrayList.get(nDataCnt).getIdx())};

      setTableRaw(tableLayout, strTitle);
    }
  }

  /**
   * @brief setTableRaw
   *
   * Set raw data to Tablelayout
   *
   * @param tableLayout TableLayout
   * @param strTitle string array
   *
   * @return NULL
   */
  private void setTableRaw(TableLayout tableLayout, String[] strTitle)
  {
    int nIdx;
    int nPadding;
    TableRow tableRow;
    TextView textView;

    tableRow = new TableRow(getView().getContext());
    nPadding = TABLELAYOUT_TEXT_PADDING;

    for (nIdx = 0; nIdx < strTitle.length; nIdx++)
    {
      textView = new TextView(getView().getContext());

      textView.setText(strTitle[nIdx]);
      textView.setTextSize(TABLELAYOUT_TEXT_SIZE);
      textView.setPadding(nPadding, nPadding, nPadding, nPadding);
      textView.setGravity(Gravity.CENTER);
      textView.setBackground(ContextCompat.getDrawable(getView().getContext(), R.drawable.table_frame));
      tableRow.addView(textView);
    }

    tableLayout.addView(tableRow);
  }

  private Runnable mRunnableReadSleepMonitorData = new Runnable()
  {
    @Override
    public void run()
    {
      /// [AT-PM] : Read sleep monitor data ; 11/15/2017
      final ArrayList<BleSleepData> sleepData = mListener.onGetSleepMonitorData();

      /// [AT-PM] : Update result ; 11/15/2017
      getActivity().runOnUiThread(new Runnable()
      {
        @Override
        public void run()
        {
          displayData(sleepData);
        }
      });
    }
  };

  @Override
  public void onDestroyView()
  {
    super.onDestroyView();
    if(mUpdateSleepMonitorDataHandler != null)
    {
      mUpdateSleepMonitorDataHandler.removeCallbacksAndMessages(null);
      mUpdateSleepMonitorDataHandler = null;
    }
  }

  private View.OnClickListener mOnClickResetData = new View.OnClickListener()
  {
    @Override
    public void onClick(View v)
    {
      Log.d(LOG_TAG, String.format("mOnClickResetData.onClick(%s)", v.toString()));
      if(mListener != null)
      {
        mListener.onResetSleepData();
      }
    }
  };
}