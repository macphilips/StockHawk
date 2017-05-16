package com.udacity.stockhawk.data;

import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by MOROLANI on 5/16/2017
 * <p>
 * owm
 * .
 */

public class StockItem implements Parcelable {
    public static final Creator<StockItem> CREATOR = new Creator<StockItem>() {
        @Override
        public StockItem createFromParcel(Parcel in) {
            return new StockItem(in);
        }

        @Override
        public StockItem[] newArray(int size) {
            return new StockItem[size];
        }
    };
    private float price;
    private String history;
    private float percentageChange;
    private float rawAbsoluteChange;
    private String symbol;

    protected StockItem(Parcel in) {
        price = in.readFloat();
        history = in.readString();
        percentageChange = in.readFloat();
        rawAbsoluteChange = in.readFloat();
        symbol = in.readString();
    }

    private StockItem() {

    }

    public static StockItem buildFrom(Cursor cursor) {
        StockItem item = new StockItem();
        float rawAbsoluteChange = cursor.getFloat(Contract.Quote.POSITION_ABSOLUTE_CHANGE);
        float percentageChange = cursor.getFloat(Contract.Quote.POSITION_PERCENTAGE_CHANGE);
        item.setPrice(cursor.getFloat(Contract.Quote.POSITION_PRICE));
        item.setSymbol(cursor.getString(Contract.Quote.POSITION_SYMBOL));
        item.setRawAbsoluteChange(rawAbsoluteChange);
        item.setPercentageChange(percentageChange);
        item.setHistory(cursor.getString(Contract.Quote.POSITION_HISTORY));

        return item;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeFloat(price);
        dest.writeString(history);
        dest.writeFloat(percentageChange);
        dest.writeFloat(rawAbsoluteChange);
        dest.writeString(symbol);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public float getPrice() {
        return price;
    }

    public void setPrice(float price) {
        this.price = price;
    }

    public float getPercentageChange() {
        return percentageChange;
    }

    public void setPercentageChange(float percentageChange) {
        this.percentageChange = percentageChange;
    }

    public float getRawAbsoluteChange() {
        return rawAbsoluteChange;
    }

    public void setRawAbsoluteChange(float rawAbsoluteChange) {
        this.rawAbsoluteChange = rawAbsoluteChange;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public String getHistory() {
        return history;
    }

    public void setHistory(String history) {
        this.history = history;
    }

    @Override
    public String toString() {
        return "StockItem{" +
                "price=" + price +
                ", history='" + history + '\'' +
                ", percentageChange=" + percentageChange +
                ", rawAbsoluteChange=" + rawAbsoluteChange +
                ", symbol='" + symbol + '\'' +
                '}';
    }
}
