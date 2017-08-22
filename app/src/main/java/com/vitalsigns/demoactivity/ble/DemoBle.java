package com.vitalsigns.demoactivity.ble;
import android.bluetooth.BluetoothAdapter;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;

import com.vitalsigns.sdk.ble.BleAlertData;
import com.vitalsigns.sdk.ble.BleCmdService;
import com.vitalsigns.sdk.ble.BlePedometerData;
import com.vitalsigns.sdk.ble.BleService;
import com.vitalsigns.sdk.ble.BleSleepData;
import com.vitalsigns.sdk.ble.BleSwitchData;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import static android.content.Context.BIND_AUTO_CREATE;

/**
 * Created by coge on 2017/8/21.
 */

public class DemoBle  implements BleCmdService.OnServiceListener
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
    void onGetPedometerDataFinish(int nDataCnt, ArrayList<BlePedometerData> arrayList);
    void onGetSleepMonitorDataFinish(ArrayList<BleSleepData> arrayList);
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
      mBleService.RegisterClient(DemoBle.this);
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
   * @return true if request success
   */
  public boolean getPedometerData()
  {
    if((mBleService != null) &&
       (mBleService.IsBleConnected()) &&
       (mBleService.GetBleDevice().getName() != null) &&
       ((mBleService.GetBleDevice().getName().contains("VSW"))))
    {
      mBleService.CmdSyncPedometer();
      return (true);
    }

    return (false);
  }

  /**
   * @brief getSleepMonitorData
   *
   * Get sleep monitor data from device
   *
   * @return true if request success
   */
  public boolean getSleepMonitorData()
  {
    if((mBleService != null) &&
      (mBleService.IsBleConnected()) &&
      (mBleService.GetBleDevice().getName() != null) &&
      ((mBleService.GetBleDevice().getName().contains("VSW"))))
    {
      mBleService.CmdSyncSleep();
      return (true);
    }

    return (false);
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
    mDemoBleEvent.onGetPedometerDataFinish(i, arrayList);
  }

  @Override
  public void sleepData(int i, int i1, ArrayList<BleSleepData> arrayList) {
    Log.d(LOG_TAG, "sleepData()");
    mDemoBleEvent.onGetSleepMonitorDataFinish(arrayList);
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
}
