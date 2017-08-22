package com.vitalsigns.demoactivity;

import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;

/**
 * Created by coge on 2017/8/18.
 */

public class Utility
{
  public static final int PERMISSION_REQUEST_EXTERNAL_STORAGE = 1000;
  public static final int PERMISSION_REQUEST_COARSE_LOCATION = 1001;

  public interface OnCancel
  {
    void Run();
  }

  /**
   * @brief requestPermissionAccessExternalStorage
   *
   * Request permission to access external storage
   *
   * @param activity Activity
   * @param title alert dialog title
   * @param message alert dialog message
   * @param onCancel callback if cancel clicked
   *
   * @return true if permission granted
   */
  public static boolean requestPermissionAccessExternalStorage(final Activity activity,
                                                               String title,
                                                               String message,
                                                               final OnCancel onCancel)
  {
    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
    {
      if((activity.checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) ||
        (activity.checkSelfPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED))
      {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.setPositiveButton(android.R.string.ok,
          new DialogInterface.OnClickListener()
          {
            @Override
            public void onClick(DialogInterface dialogInterface, int i)
            {
              ActivityCompat.requestPermissions(activity,
                new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                  android.Manifest.permission.READ_EXTERNAL_STORAGE},
                PERMISSION_REQUEST_EXTERNAL_STORAGE);
            }
          });
        builder.setNegativeButton(android.R.string.cancel,
          new DialogInterface.OnClickListener()
          {
            @Override
            public void onClick(DialogInterface dialogInterface, int i)
            {
              if(onCancel != null)
              {
                onCancel.Run();
              }
            }
          });
        builder.show();
        return (false);
      }
    }
    return (true);
  }

  /**
   * @brief requestPermissionAccessCoarseLocation
   *
   * Request permission to access coarse location
   *
   * @param activity Activity
   * @param title alert dialog title
   * @param message alert dialog message
   * @param onCancel callback if cancel clicked
   *
   * @return true if permission granted
   */
  public static boolean requestPermissionAccessCoarseLocation(final Activity activity,
                                                              String title,
                                                              String message,
                                                              final OnCancel onCancel)
  {
    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
    {
      /// Android M Permission check?
      if(activity.checkSelfPermission(android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
      {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.setPositiveButton(android.R.string.ok,
          new DialogInterface.OnClickListener()
          {
            @Override
            public void onClick(DialogInterface dialogInterface, int i)
            {
              ActivityCompat.requestPermissions(activity,
                new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                PERMISSION_REQUEST_COARSE_LOCATION);
            }
          });
        builder.setNegativeButton(android.R.string.cancel,
          new DialogInterface.OnClickListener()
          {
            @Override
            public void onClick(DialogInterface dialogInterface, int i)
            {
              if(onCancel != null)
              {
                onCancel.Run();
              }
            }
          });
        builder.show();
        return (false);
      }
    }
    return (true);
  }
}
