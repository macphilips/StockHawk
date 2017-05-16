package com.udacity.stockhawk;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.app.TaskStackBuilder;
import android.widget.RemoteViews;
import android.widget.Toast;

import com.udacity.stockhawk.sync.StackWidgetService;
import com.udacity.stockhawk.ui.Graph;

/**
 * Created by MOROLANI on 5/15/2017
 * <p>
 * owm
 * .
 */

public class MyWidgetProvider extends AppWidgetProvider {
    public static final String LAUNCH_GRAPH = "com.udacity.stockhawk.LAUNCH_GRAPH";
    public static final String EXTRA_ITEM = "com.udacity.stockhawk.EXTRA_ITEM";
    public static final String STOCK = "com.udacity.stockhawk.STOCK";

    @Override
    public void onReceive(Context context, Intent intent) {
        AppWidgetManager mgr = AppWidgetManager.getInstance(context);
        if (intent.getAction().equals(LAUNCH_GRAPH)) {
            int appWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
                    AppWidgetManager.INVALID_APPWIDGET_ID);
            String viewIndex = intent.getStringExtra(EXTRA_ITEM);
            Toast.makeText(context, "Touched view " + String.valueOf(viewIndex), Toast.LENGTH_SHORT).show();
        }
        super.onReceive(context, intent);
    }


    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager,
                         int[] appWidgetIds) {
        // Get all ids
        ComponentName thisWidget = new ComponentName(context, MyWidgetProvider.class);
        int[] allWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget);
        for (int widgetId : allWidgetIds) {

            Intent intent = new Intent(context, StackWidgetService.class);
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId);
            intent.setData(Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME)));

            RemoteViews rv = new RemoteViews(context.getPackageName(),
                    R.layout.widget_layout);
            rv.setRemoteAdapter(R.id.flipper, intent);
            rv.setEmptyView(R.id.flipper, R.id.empty_view);

          /*  Intent launchActivity = new Intent(context, MyWidgetProvider.class);
            launchActivity.setAction(MyWidgetProvider.LAUNCH_GRAPH);
            launchActivity.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId);
            intent.setData(Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME)));

          // PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, launchActivity, 0);

           PendingIntent toastPendingIntent = PendingIntent.getBroadcast(context, 0, launchActivity, PendingIntent.FLAG_UPDATE_CURRENT);
*/
            Intent launchGraph = new Intent(context, Graph.class);
            launchGraph.setAction(MyWidgetProvider.LAUNCH_GRAPH);

            PendingIntent click = TaskStackBuilder.create(context)
                    .addNextIntentWithParentStack(launchGraph)
                    .getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
            rv.setPendingIntentTemplate(R.id.flipper, click);

            appWidgetManager.updateAppWidget(widgetId, rv);
        }
        super.onUpdate(context, appWidgetManager, appWidgetIds);
    }


}
