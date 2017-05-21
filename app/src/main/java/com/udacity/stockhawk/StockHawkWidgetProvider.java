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

import com.udacity.stockhawk.sync.StockHawkWidgetService;
import com.udacity.stockhawk.ui.Graph;

import static com.udacity.stockhawk.Constants.LAUNCH_GRAPH;
import static com.udacity.stockhawk.Constants.ACTION_DATA_UPDATED;

/**
 * Created by MOROLANI on 5/15/2017
 * <p>
 * owm
 * .
 */

public class StockHawkWidgetProvider extends AppWidgetProvider {

    @Override
    public void onReceive(Context context, Intent intent) {
        AppWidgetManager mgr = AppWidgetManager.getInstance(context);
        String action = intent.getAction();
        if (action.equals(ACTION_DATA_UPDATED)){
            AppWidgetManager manager = AppWidgetManager.getInstance(context);
            ComponentName thisWidget = new ComponentName(context, StockHawkWidgetProvider.class);
            int[] allWidgetIds = manager.getAppWidgetIds(thisWidget);
            manager.notifyAppWidgetViewDataChanged(allWidgetIds, R.id.flipper);
        }
        super.onReceive(context, intent);
    }


    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager,
                         int[] appWidgetIds) {
        // Get all ids
        ComponentName thisWidget = new ComponentName(context, StockHawkWidgetProvider.class);
        int[] allWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget);
        for (int widgetId : allWidgetIds) {

            Intent intent = new Intent(context, StockHawkWidgetService.class);
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId);
            intent.setData(Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME)));

            RemoteViews rv = new RemoteViews(context.getPackageName(),
                    R.layout.widget_layout);
            rv.setRemoteAdapter(R.id.flipper, intent);
            rv.setEmptyView(R.id.flipper, R.id.empty_view);

            Intent launchGraph = new Intent(context, Graph.class);
            launchGraph.setAction(LAUNCH_GRAPH);
            PendingIntent pendingIntent = TaskStackBuilder.create(context)
                    .addNextIntentWithParentStack(launchGraph)
                    .getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
            rv.setPendingIntentTemplate(R.id.flipper, pendingIntent);

            appWidgetManager.updateAppWidget(widgetId, rv);
        }
        super.onUpdate(context, appWidgetManager, appWidgetIds);
    }


}
