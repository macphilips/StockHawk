package com.udacity.stockhawk.sync;

import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.text.TextUtils;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.udacity.stockhawk.data.Contract;
import com.udacity.stockhawk.data.PrefUtils;

import java.io.IOException;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import timber.log.Timber;
import yahoofinance.Stock;
import yahoofinance.YahooFinance;
import yahoofinance.histquotes.HistoricalQuote;
import yahoofinance.quotes.stock.StockQuote;

import static com.udacity.stockhawk.Constants.ACTION_DATA_UPDATED;
import static com.udacity.stockhawk.Constants.DATA_UPDATED_ERROR_TEXT;

public final class QuoteSyncJob {
    private static final String TAG = QuoteSyncJob.class.getSimpleName();
    private static final int ONE_OFF_ID = 2;
    private static final int PERIOD = 300000;
    private static final int INITIAL_BACKOFF = 10000;
    private static final int PERIODIC_ID = 1;
    private static final int YEARS_OF_HISTORY = 2;

    private QuoteSyncJob() {
    }

    static void getQuotes(Context context) {

        Timber.d("Running sync job");

        Calendar from = Calendar.getInstance();
        Calendar to = Calendar.getInstance();
        from.add(Calendar.YEAR, -YEARS_OF_HISTORY);

        try {
            Set<String> stockPref = PrefUtils.getStocks(context);
            Set<String> stockCopy = new HashSet<>();
            stockCopy.addAll(stockPref);
            String[] stockArray = stockPref.toArray(new String[stockPref.size()]);

            Timber.d(stockCopy.toString());

            if (stockArray.length == 0) {
                return;
            }
            Log.d(TAG, stockPref.toString());
            Map<String, Stock> quotes = YahooFinance.get(stockArray);
            Iterator<String> iterator = stockCopy.iterator();

            Timber.d(quotes.toString());
            String value = "";
            ArrayList<ContentValues> quoteCVs = new ArrayList<>();

            while (iterator.hasNext()) {
                String symbol = iterator.next();
                Stock stock = quotes.get(symbol);
                if (stock == null || stock.getName() == null) {
               //     Toast.makeText(context, String.format("Symbol %s does not exists", symbol), Toast.LENGTH_LONG).show();
                    PrefUtils.removeStock(context, symbol);
                    value = value + symbol + "\n";
                    continue;
                }
                StockQuote quote = stock.getQuote();
                stock.print();
                BigDecimal quotePrice = quote.getPrice();
                float price = (quotePrice != null) ? quotePrice.floatValue() : 0;
                BigDecimal quoteChange = quote.getChange();
                float change = (quoteChange != null) ? quoteChange.floatValue() : 0;
                BigDecimal changeInPercent = quote.getChangeInPercent();
                float percentChange = (changeInPercent != null) ? changeInPercent.floatValue() : 0;

                Log.d(TAG, symbol);

                List<HistoricalQuote> history = MockUtils.getHistory();

                Gson gson = new Gson();
                Type type = new TypeToken<List<HistoricalQuote>>() {
                }.getType();
                String jsonHistory = gson.toJson(history, type);

                ContentValues quoteCV = new ContentValues();
                quoteCV.put(Contract.Quote.COLUMN_SYMBOL, symbol);
                quoteCV.put(Contract.Quote.COLUMN_PRICE, price);
                quoteCV.put(Contract.Quote.COLUMN_PERCENTAGE_CHANGE, percentChange);
                quoteCV.put(Contract.Quote.COLUMN_ABSOLUTE_CHANGE, change);
                quoteCV.put(Contract.Quote.COLUMN_HISTORY, jsonHistory);
                quoteCVs.add(quoteCV);
            }

            context.getContentResolver()
                    .bulkInsert(
                            Contract.Quote.URI,
                            quoteCVs.toArray(new ContentValues[quoteCVs.size()]));

            if (TextUtils.isEmpty(value)) {
                value = null;
            }
            sendUpdateBroadcast(context, value);


        } catch (IOException exception) {
            Timber.e(exception, "Error fetching stock quotes");
        }
    }

    public static void sendUpdateBroadcast(Context context, String value) {
        Intent dataUpdatedIntent = new Intent(ACTION_DATA_UPDATED);
        if (value != null)
            dataUpdatedIntent.putExtra(DATA_UPDATED_ERROR_TEXT, value);
        context.sendBroadcast(dataUpdatedIntent);
    }

    private static String getDay(Calendar date) {

        String day;
        int d = date.get(Calendar.DAY_OF_WEEK);
        if (d == Calendar.MONDAY) {
            day = "Monday";
        } else if (d == Calendar.TUESDAY) {
            day = "Tuesday";
        } else if (d == Calendar.WEDNESDAY) {
            day = "Wednesday";
        } else if (d == Calendar.THURSDAY) {
            day = "Thursday";
        } else if (d == Calendar.FRIDAY) {
            day = "Friday";
        } else if (d == Calendar.SATURDAY) {
            day = "Saturday";
        } else if (d == Calendar.SUNDAY) {
            day = "Sunday";
        } else {
            day = "";
        }
        return day;
    }

    private static void schedulePeriodic(Context context) {
        Timber.d("Scheduling a periodic task");
        JobInfo.Builder builder = new JobInfo.Builder(PERIODIC_ID, new ComponentName(context, QuoteJobService.class));
        builder.setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
                .setPeriodic(PERIOD)
                .setBackoffCriteria(INITIAL_BACKOFF, JobInfo.BACKOFF_POLICY_EXPONENTIAL);

        JobScheduler scheduler = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);

        scheduler.schedule(builder.build());
    }

    public static synchronized void initialize(final Context context) {
        schedulePeriodic(context);
        syncImmediately(context);
    }

    public static synchronized void syncImmediately(Context context) {
        ConnectivityManager cm =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnectedOrConnecting()) {
            Intent nowIntent = new Intent(context, QuoteIntentService.class);
            context.startService(nowIntent);
        } else {

            JobInfo.Builder builder = new JobInfo.Builder(ONE_OFF_ID, new ComponentName(context, QuoteJobService.class));

            builder.setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
                    .setBackoffCriteria(INITIAL_BACKOFF, JobInfo.BACKOFF_POLICY_EXPONENTIAL);

            JobScheduler scheduler = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
            scheduler.schedule(builder.build());


        }
    }


}
