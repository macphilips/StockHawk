package com.udacity.stockhawk.sync;

import android.content.Intent;
import android.widget.RemoteViewsService;

/**
 * Created by MOROLANI on 5/16/2017
 * <p>
 * owm
 * .
 */

public class StackWidgetService extends RemoteViewsService {
    public StackWidgetService() {
    }

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new StackRemoteViewsFactory(this.getApplicationContext(), intent);
    }

}
