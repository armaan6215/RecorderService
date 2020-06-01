package com.example.recorderservice;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.Toast;

public class AppWidget extends AppWidgetProvider {
    public static String TOGGLE_WINET = "ToggleWiNetService";
    private static boolean serviceRunning = false;
    private static Intent serviceIntent;
    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        final int N = appWidgetIds.length;

        for (int i=0; i<N; i++) {
            int appWidgetId = appWidgetIds[i];
            RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget);
            Intent newIntent = new Intent(context, AppWidget.class);
            newIntent.setAction(TOGGLE_WINET);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, newIntent, 0);
            remoteViews.setOnClickPendingIntent(R.id.button, pendingIntent);

            appWidgetManager.updateAppWidget(appWidgetId, remoteViews);
            Log.i(TOGGLE_WINET, "updated");
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if(intent.getAction().equals(TOGGLE_WINET)) {
            RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget);
            if(serviceRunning) {
                Toast.makeText(context, "serviceStarted", Toast.LENGTH_SHORT).show();
            } else {
                context.startService(new Intent(context, RecorderService.class));
                Toast.makeText(context, "serviceStarted", Toast.LENGTH_SHORT).show();
            }
            serviceRunning=!serviceRunning;
            ComponentName componentName = new ComponentName(context, AppWidget.class);
            AppWidgetManager.getInstance(context).updateAppWidget(componentName, remoteViews);
        }
        super.onReceive(context, intent);
    }
}
