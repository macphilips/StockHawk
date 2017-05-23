package com.udacity.stockhawk.widget;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Binder;
import android.support.v4.content.ContentResolverCompat;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.udacity.stockhawk.R;
import com.udacity.stockhawk.data.Contract;
import com.udacity.stockhawk.data.PrefUtils;
import com.udacity.stockhawk.data.StockItem;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Created by MOROLANI on 5/16/2017
 * <p>
 * owm
 * .
 */

  class WidgetRemoteViewsFactory implements RemoteViewsService.RemoteViewsFactory {
    private DecimalFormat dollarFormatWithPlus;
    private DecimalFormat dollarFormat;
    private DecimalFormat percentageFormat;
    private Context mContext;
    private List<StockItem> items;

    WidgetRemoteViewsFactory(Context context) {
        mContext = context;
    }


    @Override
    public void onCreate() {
        dollarFormat = (DecimalFormat) NumberFormat.getCurrencyInstance(Locale.US);
        dollarFormatWithPlus = (DecimalFormat) NumberFormat.getCurrencyInstance(Locale.US);
        dollarFormatWithPlus.setPositivePrefix("+$");
        percentageFormat = (DecimalFormat) NumberFormat.getPercentInstance(Locale.getDefault());
        percentageFormat.setMaximumFractionDigits(2);
        percentageFormat.setMinimumFractionDigits(2);
        percentageFormat.setPositivePrefix("+");
    }

    @Override
    public void onDataSetChanged() {
        Cursor cursor = null;
        final long identityToken = Binder.clearCallingIdentity();
        try {
            cursor = ContentResolverCompat.query(
                    mContext.getContentResolver(),
                    Contract.Quote.URI,
                    Contract.Quote.QUOTE_COLUMNS.toArray(new String[]{}),
                    null,
                    null,
                    Contract.Quote.COLUMN_SYMBOL, null);
            if (cursor == null || isCursorEmpty(cursor)) return;

            cursor.moveToFirst();
            items = new ArrayList<>();
            while (true) {
                StockItem emp = StockItem.buildFrom(cursor);
                items.add(emp);
                if (!cursor.moveToNext())
                    break;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {

            Binder.restoreCallingIdentity(identityToken);
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    private boolean isCursorEmpty(Cursor cursor) {
        if (!cursor.moveToFirst() || cursor.getCount() == 0) {
            cursor.close();
            return true;
        }
        return false;
    }

    public void onDestroy() {

    }

    public int getCount() {
        int count = 0;
        if (items != null) {
            count = items.size();
        }
        return count;
    }

    public RemoteViews getViewAt(int position) {

        StockItem item = items.get(position);
        float rawAbsoluteChange = item.getRawAbsoluteChange();
        float percentageChange = item.getPercentageChange();

        String change = dollarFormatWithPlus.format(rawAbsoluteChange);
        String percentage = percentageFormat.format(percentageChange / 100);


        // We construct a remote views item based on our widget item xml file, and set the
        // text based on the position.
        RemoteViews rv = new RemoteViews(mContext.getPackageName(), R.layout.list_item_quote);
        CharSequence symbol = item.getSymbol();
        rv.setTextViewText(R.id.symbol, symbol);
        float price = item.getPrice();
        rv.setTextViewText(R.id.price, dollarFormat.format(price));


        if (PrefUtils.getDisplayMode(mContext)
                .equals(mContext.getString(R.string.pref_display_mode_absolute_key))) {
            rv.setTextViewText(R.id.change, change);

        } else {
            rv.setTextViewText(R.id.change, percentage);
        }

        if (rawAbsoluteChange > 0) {
            rv.setInt(R.id.change, "setBackgroundResource", R.drawable.percent_change_pill_green);
        } else {
            rv.setInt(R.id.change, "setBackgroundResource", R.drawable.percent_change_pill_red);
        }

        // Next, set a fill-intent, which will be used to fill in the pending intent template
        // that is set on the collection view in StackWidgetProvider.

        Intent fillInIntent = new Intent();
        fillInIntent.setData(Contract.Quote.makeUriForStock(item.getSymbol()));

        rv.setOnClickFillInIntent(R.id.symbol, fillInIntent);
        rv.setOnClickFillInIntent(R.id.price, fillInIntent);
        rv.setOnClickFillInIntent(R.id.change, fillInIntent);

        return rv;
    }

    public RemoteViews getLoadingView() {
        // You can create a custom loading view (for instance when getViewAt() is slow.) If you
        // return null here, you will get the default loading view.
        return null;
    }

    public int getViewTypeCount() {
        return 1;
    }

    public long getItemId(int position) {
        return position;
    }

    public boolean hasStableIds() {
        return true;
    }
}
