package com.xbeats.android.timereminder;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;

import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;

import com.xbeats.android.timereminder.databinding.FloatingTimeLayoutBinding;

/**
 * Created by Fring on 2020/12/24
 */
public class FloatingService extends Service {
    private static final String TAG = "FloatingService_TAG";
    private static final String PARAMS_COMMAND = "paramsCommand";

    public static void startFloatingView(Context context) {
        Intent intent = new Intent(context, FloatingService.class);
        intent.putExtra(PARAMS_COMMAND, Command.START);
        context.startService(intent);
    }

    public static void stopFloatingView(Context context) {
        Intent intent = new Intent(context, FloatingService.class);
        intent.putExtra(PARAMS_COMMAND, Command.STOP);
        context.startService(intent);
    }

    private FloatingTimeLayoutBinding mBinding;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private WindowManager.LayoutParams wmParams;
    private WindowManager mWindowManager;
    private int mStatusBarHeight;

    @Override
    public void onCreate() {
        super.onCreate();
        mStatusBarHeight = getStatusBarHeight(this);

        this.wmParams = new WindowManager.LayoutParams();
        this.mWindowManager = (WindowManager) getApplication().getSystemService(Context.WINDOW_SERVICE);
        this.wmParams.type = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ? WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY : WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
        this.wmParams.x = 100;
        this.wmParams.y = 100;
        this.wmParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        this.wmParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
        this.wmParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
        // set background as transparent
        this.wmParams.format = PixelFormat.RGBA_8888;
        this.wmParams.gravity = Gravity.START | Gravity.TOP;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        int command = intent.getIntExtra(PARAMS_COMMAND, 0);

        switch (command) {
            case Command.START:
                if (mBinding == null) {
                    startFloating();
                }
                break;
            case Command.STOP:
                if (mBinding != null) {
                    mBinding.textClock.stopTicking();
                    this.mWindowManager.removeView(mBinding.getRoot());
                }
                mBinding = null;
                this.stopForeground(true);
                stopSelf();
                break;
        }

        return super.onStartCommand(intent, flags, startId);
    }

    private static final String notificationId = "111";
    private static final String notificationName = "FloatingNotificationName";

    private void startFloating() {
        mBinding = DataBindingUtil.inflate(LayoutInflater.from(this), R.layout.floating_time_layout, null, false);
        this.mWindowManager.addView(mBinding.getRoot(), this.wmParams);
        mBinding.getRoot().setOnTouchListener(new View.OnTouchListener() {
            @SuppressLint("ClickableViewAccessibility")
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                Log.d(TAG, String.format("RawX = %f, X = %f, RawY = %f, Y = %f, ", motionEvent.getRawX(), motionEvent.getX(), motionEvent.getRawY(), motionEvent.getY()));
                int leftTopX = (int) (motionEvent.getRawX() - view.getMeasuredWidth() / 2);
                int leftTopY = (int) (motionEvent.getRawY() - view.getMeasuredHeight() / 2) - mStatusBarHeight;
                wmParams.x = leftTopX;
                wmParams.y = leftTopY;
                mWindowManager.updateViewLayout(mBinding.getRoot(), wmParams);

                return false;
            }
        });

        mBinding.getRoot().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(FloatingService.this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                FloatingService.this.startActivity(intent);
            }
        });

        Notification.Builder builder = new Notification.Builder(this)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(getString(R.string.app_name))
                .setContentText(getString(R.string.notification_content));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            NotificationChannel channel = new NotificationChannel(notificationId, notificationName, NotificationManager.IMPORTANCE_HIGH);
            notificationManager.createNotificationChannel(channel);
            builder.setChannelId(notificationId);
        }

        Notification notification = builder.build();

        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        notification.contentIntent = PendingIntent.getActivity(this, 0, new Intent(this, MainActivity.class), 0);

        startForeground(1, notification);
    }

    private int getStatusBarHeight(Context context) {
        final Resources resources = context.getResources();
        final int heightResId = resources.getIdentifier("status_bar_height", "dimen", "android");
        if (heightResId > 0) {
            return resources.getDimensionPixelSize(heightResId);
        } else {
            int heightFromDp = Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ? 24 : 25;
            return (int) TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP, heightFromDp, resources.getDisplayMetrics());
        }
    }

    private static final class Command {
        private static final int START = 1;
        private static final int STOP = 2;
    }
}
