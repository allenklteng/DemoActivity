package com.vitalsigns.demoactivity.fragment;

import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.content.ContextCompat;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import com.vitalsigns.demoactivity.R;
import com.vitalsigns.sdk.ble.BlePedometerData;
import com.vitalsigns.sdk.utility.Utility;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class PedometerFragment extends Fragment
{
  private static final String LOG_TAG = "PedometerFragment:";
  private static final float TABLELAYOUT_TEXT_SIZE = (25f);
  private static final int TABLELAYOUT_TEXT_PADDING = (10);
  private static final int UPDATE_TODAY_STEP_INTERVAL = 1000;

  private OnPedometerFragmentListener mListener;
  private ProgressDialog mProgressDialog;
  private Handler mUpdateTodayStepHandler = null;
  private Handler mUpdatePedometerHandler = null;

  public interface OnPedometerFragmentListener
  {
    ArrayList<BlePedometerData> onGetPedometerData();
    void onConnectionFirst();
    int onGetTodayStep();
    Looper onGetLooper();
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

    if((!isAdded()) || (getArguments() == null))
    {
      return;
    }

    if(!getArguments().getBoolean(getString(R.string.device_connection_check), false))
    {
      if(mListener != null)
      {
        mListener.onConnectionFirst();
        return;
      }
    }

    /// [CC] : Get pedometer data ; 08/22/2017
    getPedometerData();
  }

  @Override
  public void onStop() {
    super.onStop();
    hideProgressDialog();

    if(mUpdateTodayStepHandler != null)
    {
      mUpdateTodayStepHandler.removeCallbacksAndMessages(null);
      mUpdateTodayStepHandler = null;
    }
  }

  /**
   * @brief SetCallback
   *
   * Set pedometer fragment callback
   *
   * @param callback
   *
   * @return NULL
   */
  public void SetCallback(PedometerFragment.OnPedometerFragmentListener callback)
  {
    mListener = callback;
  }

  /**
   * @brief getPedometerData
   *
   * Get pedometer data
   *
   * @return NULL
   */
  private void getPedometerData()
  {
    if(mListener == null)
    {
      return;
    }

    showProgressDialog();

    /// [AT-PM] : Start a thread to read pedometer data ; 11/15/2017
    mUpdatePedometerHandler = new Handler(mListener.onGetLooper());
    mUpdatePedometerHandler.post(mUpdatePedometerRunnable);
  }

  /**
   * @brief showProgressDialog
   *
   * Show Progress Dialog
   *
   * @return null
   */
  private void showProgressDialog()
  {
    if (mProgressDialog == null)
    {
      mProgressDialog = new ProgressDialog(getActivity(), R.style.DialogStyle);
      mProgressDialog.setMessage(getString(R.string.pedometer_data_loading));
      mProgressDialog.setIndeterminate(true);
      mProgressDialog.setCanceledOnTouchOutside(false);

      mProgressDialog.setOnKeyListener(new DialogInterface.OnKeyListener()
      {
        @Override
        public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event)
        {
          if(mProgressDialog != null && mProgressDialog.isShowing())
          {
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
          mProgressDialog.hide();
        }
      });
    }
  }

  /**
   * @brief displayData
   *
   * Use table display data
   *
   * @param dataArrayList BlePedometerData array
   * @return
   */
  private void displayData(ArrayList<BlePedometerData> dataArrayList)
  {
    /// [CC] Return if no data ; 08/22/2017
    if(dataArrayList == null)
    {
      /// [CC] Hide Progress Dialog ; 08/22/2017
      hideProgressDialog();
      return;
    }

    if(!isAdded() || (getView() == null))
    {
      /// [CC] Hide Progress Dialog ; 08/22/2017
      hideProgressDialog();
      return;
    }

    /// [CC] : Save data ; 08/22/2017
    savePedometerDataToCsv(dataArrayList);

    /// [CC] : Show data ; 08/22/2017
    showPedometerData(dataArrayList);

    /// [CC] Hide Progress Dialog ; 08/22/2017
    hideProgressDialog();
  }

  /**
   * @brief savePedometerDataToCsv
   *
   * Save pedometer data as csv file
   *
   * @param arrayList BlePedometerData array
   *
   * @return NULL
   */
  private void savePedometerDataToCsv(ArrayList<BlePedometerData> arrayList)
  {
    String strFilename;
    FileWriter fileWriter;
    BufferedWriter bufferedWriter;

    strFilename = Environment.getExternalStorageDirectory().getAbsolutePath() +
      File.separator + "PedometerData." + Utility.GetFileDateTime() + ".csv";

    try
    {
      fileWriter = new FileWriter(strFilename, true);
      bufferedWriter = new BufferedWriter(fileWriter);

      /// [CC] : Title ; 08/22/2017
      bufferedWriter.write("Day of week, Time region, Step Count, Run Count");

      bufferedWriter.newLine();
      for(BlePedometerData blePedometerData : arrayList)
      {
        bufferedWriter.write(String.format("%d/%d, %d, %d, %d,",
                             blePedometerData.getMonth(),
                             blePedometerData.getDay(),
                             blePedometerData.getTimeIndex(),
                             blePedometerData.getTotalStep(),
                             blePedometerData.getRunStep()));
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
   * @brief showPedometerData
   *
   * Show pedometer data
   *
   * @param arrayList BlePedometerData array
   *
   * @return NULL
   */
  private void showPedometerData(ArrayList<BlePedometerData> arrayList)
  {
    TableLayout tableLayout;
    String[] strTitle;
    int nDataCnt;

    tableLayout = (TableLayout) getView().findViewById(R.id.pedometer_table_layout);
    strTitle = new String[]{"Day of week", "Time region", "Step Count", "Run Count"};
    setTableRaw(tableLayout, strTitle);

    for (nDataCnt = 0; nDataCnt < arrayList.size(); nDataCnt++)
    {
      strTitle = new String[]{
        String.format("%d/%d", arrayList.get(nDataCnt).getMonth(), arrayList.get(nDataCnt).getDay()),
        String.valueOf(arrayList.get(nDataCnt).getTimeIndex()),
        String.valueOf(arrayList.get(nDataCnt).getTotalStep()),
        String.valueOf(arrayList.get(nDataCnt).getRunStep())};

      setTableRaw(tableLayout, strTitle);
    }
  }

  /**
   * @brief setTableRaw
   *
   * Set raw data to Tablelayout
   *
   * @param tableLayout TableLayout
   * @param strTitle
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

  private Runnable mUpdateTodayStepRunnable = new Runnable()
  {
    @Override
    public void run()
    {
      /// [AT-PM] : Get today steps ; 09/13/2017
      final int step = mListener.onGetTodayStep();

      /// [AT-PM} : Update result ; 11/15/2017
      getActivity().runOnUiThread(new Runnable()
      {
        @Override
        public void run()
        {
          View view = getView();
          if(view != null)
          {
            ((TextView)view.findViewById(R.id.pedometer_current_step)).setText(String.format("%d", step));
          }
        }
      });

      /// [AT-PM] : Read value every 1 second ; 09/13/2017
      if(mUpdateTodayStepHandler != null)
      {
        mUpdateTodayStepHandler.postDelayed(mUpdateTodayStepRunnable, UPDATE_TODAY_STEP_INTERVAL);
      }
    }
  };

  private Runnable mUpdatePedometerRunnable = new Runnable()
  {
    @Override
    public void run()
    {
      /// [AT-PM] : Get pedometer data ; 11/15/2017
      final ArrayList<BlePedometerData> pedometerData =  mListener.onGetPedometerData();

      /// [AT-PM] : Update result ; 11/15/2017
      getActivity().runOnUiThread(new Runnable()
      {
        @Override
        public void run()
        {
          displayData(pedometerData);
        }
      });

      /// [AT-PM] : Start a runnable to update today step ; 09/13/2017
      mUpdateTodayStepHandler = new Handler(mListener == null ? null : mListener.onGetLooper());
      mUpdateTodayStepHandler.post(mUpdateTodayStepRunnable);
    }
  };
}
