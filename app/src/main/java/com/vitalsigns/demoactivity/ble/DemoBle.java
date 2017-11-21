package com.vitalsigns.demoactivity.ble;
import android.bluetooth.BluetoothAdapter;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;

import com.vitalsigns.demoactivity.Utility;
import com.vitalsigns.sdk.ble.BleAlertData;
import com.vitalsigns.sdk.ble.BleCmdService;
import com.vitalsigns.sdk.ble.BlePedometerData;
import com.vitalsigns.sdk.ble.BleService;
import com.vitalsigns.sdk.ble.BleSleepData;
import com.vitalsigns.sdk.ble.BleSwitchData;

import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import static android.content.Context.BIND_AUTO_CREATE;

/**
 * Created by coge on 2017/8/21.
 */

public class DemoBle implements BleCmdService.OnDataListener,
                                BleCmdService.OnErrorListener,
                                BleCmdService.OnStatusListener,
                                BleCmdService.OnAckListener,
                                BleCmdService.OnBleRawListener
{
  private static final String LOG_TAG = "DemoBle:";
  private static final int BLE_DATA_QUEUE_SIZE = 128;
  public static BlockingQueue<int []> mBleIntDataQueue = new ArrayBlockingQueue<>(BLE_DATA_QUEUE_SIZE);

  private Context mContext = null;
  private DemoBleEvent mDemoBleEvent = null;
  private boolean mBleServiceBind = false;
  private BleService mBleService = null;

  public interface DemoBleEvent
  {
    void onDisconnect();
    void onConnect(String strName);
  }

  /**
   * @brief DemoBle
   *
   * Constructor of DemoBle class
   *
   * @param context
   * @param bleEvent
   */
  public DemoBle(@NotNull Context context, @NotNull DemoBleEvent bleEvent)
  {
    mContext = context;
    mDemoBleEvent = bleEvent;

    Intent intent = new Intent(context, BleService.class);
    mBleServiceBind = context.bindService(intent, mBleServiceConnection, BIND_AUTO_CREATE);
  }

  private ServiceConnection mBleServiceConnection = new ServiceConnection()
  {
    @Override
    public void onServiceConnected(ComponentName componentName, IBinder iBinder)
    {
      mBleService = ((BleService.LocalBinder)iBinder).getService();
      mBleService.Initialize(mBleIntDataQueue, BleCmdService.HW_TYPE.SENSE);
      mBleService.RegisterClient(DemoBle.this, DemoBle.this, DemoBle.this, DemoBle.this, DemoBle.this);
    }

    @Override
    public void onServiceDisconnected(ComponentName componentName)
    {
    }
  };

  /**
   * @brief connect
   *
   * Connect to BLE device
   *
   * @param strMacAddr Mac address
   *
   * @return NULL
   */
  public void connect(String strMacAddr)
  {
    if(mBleService != null)
    {
      mBleService.SetBleDevice(BluetoothAdapter.getDefaultAdapter().getRemoteDevice(strMacAddr));
      mBleService.Connect();
    }
  }

  /**
   * @brief isConnect
   *
   * Check device is connected or not
   *
   * @return true if connected
   */
  public boolean isConnect()
  {
    return ((mBleService != null) && mBleService.IsBleConnected());
  }

  /**
   * @brief disconnect
   *
   * Disconnect to BLE device
   *
   * @return NULL
   */
  public void disconnect()
  {
    if(mBleService != null)
    {
      mBleService.Disconnect();
    }
  }

  /**
   * @brief destroy
   *
   * Disconnect to BLE device
   *
   * @return NULL
   */
  public void destroy()
  {
    if(mBleServiceBind)
    {
      mContext.unbindService(mBleServiceConnection);
      if(mDemoBleEvent != null)
      {
        mDemoBleEvent = null;
      }
    }
  }

  /**
   * @brief getPedometerData
   *
   * Get pedometer data from device
   *
   * @return pedometer data array
   */
  public ArrayList<BlePedometerData> getPedometerData()
  {
    if((mBleService != null) &&
       (mBleService.IsBleConnected()) &&
       (mBleService.GetBleDevice().getName() != null) &&
       ((mBleService.GetBleDevice().getName().contains("VSW"))))
    {
      /// [AT-PM] : Get data started from five days ago ; 11/13/2017
      Calendar calendar = Calendar.getInstance();
      calendar.add(Calendar.DAY_OF_YEAR, -5);
      Log.d(LOG_TAG, String.format("getPedometerData() from %d/%d %d:%d",
                                   calendar.get(Calendar.MONTH),
                                   calendar.get(Calendar.DAY_OF_MONTH),
                                   calendar.get(Calendar.HOUR_OF_DAY),
                                   calendar.get(Calendar.MINUTE)));
      mBleService.CmdSyncPedometer(calendar.get(Calendar.MONTH),
                                   calendar.get(Calendar.DAY_OF_MONTH),
                                   calendar.get(Calendar.HOUR_OF_DAY),
                                   calendar.get(Calendar.MINUTE));

      /// [AT-PM] : Wait data ready ; 11/15/2017
      ArrayList<BlePedometerData> pedometerData = null;
      while(pedometerData == null)
      {
        com.vitalsigns.sdk.utility.Utility.SleepSomeTime(100);
        pedometerData = mBleService.GetPedometerData();
        Log.d(LOG_TAG, String.format("getPedometerData() -> %s",
                                     pedometerData == null ? "NULL" : Integer.toString(pedometerData.size())));
      }
      return (pedometerData);
    }
    return (null);
  }

  /**
   * @brief getSleepMonitorData
   *
   * Get sleep monitor data from device
   *
   * @return BleSleepData array
   */
  public ArrayList<BleSleepData> getSleepMonitorData()
  {
    if((mBleService != null) &&
       (mBleService.IsBleConnected()) &&
       (mBleService.GetBleDevice().getName() != null) &&
       ((mBleService.GetBleDevice().getName().contains("VSW"))))
    {
      /// [AT-PM] : Get data started from five days ago ; 11/13/2017
      Calendar calendar = Calendar.getInstance();
      calendar.add(Calendar.DAY_OF_YEAR, -5);
      Log.d(LOG_TAG, String.format("getPedometerData() from %d/%d %d:%d",
                                   calendar.get(Calendar.MONTH),
                                   calendar.get(Calendar.DAY_OF_MONTH),
                                   calendar.get(Calendar.HOUR_OF_DAY),
                                   calendar.get(Calendar.MINUTE)));
      mBleService.CmdSyncSleep(calendar.get(Calendar.MONTH),
                               calendar.get(Calendar.DAY_OF_MONTH),
                               calendar.get(Calendar.HOUR_OF_DAY),
                               calendar.get(Calendar.MINUTE));

      /// [AT-PM] : Wait data ready ; 11/15/2017
      ArrayList<BleSleepData> sleepData = null;
      while(sleepData == null)
      {
        com.vitalsigns.sdk.utility.Utility.SleepSomeTime(100);
        sleepData = mBleService.GetSleepData();
        Log.d(LOG_TAG, String.format("getSleepMonitorData() -> %s",
                                     sleepData == null ? "NULL" : Integer.toString(sleepData.size())));
      }
      return (sleepData);
    }
    return (null);
  }

  /**
   * @brief getDeviceName
   *
   * Get device name
   *
   * @return device name
   */
  public String getDeviceName()
  {
    if(mBleService != null)
    {
      return (mBleService.GetBleDevice().getName());
    }
    else
    {
      return (null);
    }

  }

  @Override
  public void chartNumberConfig(int i, int i1, int[] ints) {
    Log.d(LOG_TAG, "chartNumberConfig()");
  }

  @Override
  public void pedometerData(int i, ArrayList<BlePedometerData> arrayList) {
    Log.d(LOG_TAG, "pedometerData()");
  }

  @Override
  public void sleepData(int i, int i1, ArrayList<BleSleepData> arrayList) {
    Log.d(LOG_TAG, "sleepData()");
  }

  @Override
  public void todayStep(int i)
  {
  }

  @Override
  public void bleConnectionLost(String s) {
    Log.d(LOG_TAG, "bleConnectionLost()");
    if(mDemoBleEvent != null)
    {
      mDemoBleEvent.onDisconnect();
    }
  }

  @Override
  public void bleReadyToGetData() {
    Log.d(LOG_TAG, "bleReadyToGetData()");
    if(mDemoBleEvent != null)
    {
      mDemoBleEvent.onConnect(mBleService.GetBleDevice().getName());
    }
  }

  @Override
  public void bleGattState() {
    Log.d(LOG_TAG, "bleGattState()");
    if(mDemoBleEvent != null)
    {
      mDemoBleEvent.onDisconnect();
    }
  }

  @Override
  public void bleOtaAck() {
    Log.d(LOG_TAG, "bleOtaAck()");
  }

  @Override
  public void bleStopAck()
  {

  }

  @Override
  public void bleEcgReady()
  {

  }

  @Override
  public void bleTransmitTimeout() {
    Log.d(LOG_TAG, "bleTransmitTimeout()");
    if(mDemoBleEvent != null)
    {
      mDemoBleEvent.onDisconnect();
    }
  }

  @Override
  public void bleAckError(String s) {
    Log.d(LOG_TAG, "bleAckError()");
    if(mDemoBleEvent != null)
    {
      mDemoBleEvent.onDisconnect();
    }
  }

  @Override
  public void ackReceived(byte[] bytes) {
    Log.d(LOG_TAG, "ackReceived()");
  }

  @Override
  public void ackBleWatchSyncTime(boolean b) {
    Log.d(LOG_TAG, "ackBleWatchSyncTime()");
  }

  @Override
  public void ackTimeGet(int i, int i1, int i2) {
    Log.d(LOG_TAG, "ackTimeGet()");
  }

  @Override
  public void ackTimeCali(boolean b) {
    Log.d(LOG_TAG, "ackTimeCali()");
  }

  @Override
  public void ackSwitchGet(BleSwitchData bleSwitchData) {
    Log.d(LOG_TAG, "ackSwitchGet()");
  }

  @Override
  public void ackSwitchCnt(int i) {
    Log.d(LOG_TAG, "ackSwitchCnt()");
  }

  @Override
  public void ackSwitchSts(int i, boolean b) {
    Log.d(LOG_TAG, "ackSwitchSts()");
  }

  @Override
  public void ackAlertGet(BleAlertData bleAlertData) {
    Log.d(LOG_TAG, "ackAlertGet()");
  }

  /**
   * @brief getTodayStep
   *
   * Send command to get today steps
   *
   * @return today step
   */
  public int getTodayStep()
  {
    Log.d(LOG_TAG, "Request to read today steps");
    mBleService.CmdTodayStep();
    /// [AT-PM] : Wait data ready ; 11/15/2017
    int step = -1;
    while(step < 0)
    {
      com.vitalsigns.sdk.utility.Utility.SleepSomeTime(100);
      step = mBleService.GetTodayStep();
      Log.d(LOG_TAG, String.format("getTodayStep() = %d", step));
    }
    return (step);
  }

  @Override
  public void mfaData(byte[] bytes)
  {
    Log.d(LOG_TAG, "mfaData()");
  }

  @Override
  public void tick(long tick)
  {
    Log.d(LOG_TAG, String.format("tick(%ld)", tick));
  }

  @Override
  public void loopTick(long tick)
  {
    Log.d(LOG_TAG, String.format("loopTick(%ld)", tick));
  }

  @Override
  public void ackPointer(boolean ack)
  {
    Log.d(LOG_TAG, String.format("ackPointer(%s)", Boolean.toString(ack)));
  }

  @Override
  public void ackPasswordCheck(boolean pass)
  {
    Log.d(LOG_TAG, String.format("ackPasswordCheck(%s)", Boolean.toString(pass)));
  }

  @Override
  public void ackTemperatureGet(ArrayList<Integer> list)
  {
    Log.d(LOG_TAG, String.format("ackTemperatureGet(%d)", list.size()));
  }

  @Override
  public void ackNameGet(String name)
  {
    Log.d(LOG_TAG, String.format("ackNameGet(%s)", name));
  }

  public void resetPedometerData()
  {
    mBleService.CmdStepReset();
  }
}
