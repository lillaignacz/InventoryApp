
package com.example.android.inventory;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.inventory.data.ProductContract;


public class ProductCursorAdapter extends CursorAdapter {

    private static Context mContext;

    public ProductCursorAdapter(Context context, Cursor cursor) {
        super(context, cursor, 0);
        mContext = context;
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        TextView nameTextView = (TextView) view.findViewById(R.id.product_name);
        TextView priceTextView = (TextView) view.findViewById(R.id.product_price);
        TextView quantityTextView = (TextView) view.findViewById(R.id.product_quantity);
        Button saleButton = (Button) view.findViewById(R.id.sale_button);

        int nameColumnIndex = cursor.getColumnIndex(ProductContract.ProductEntry.COLUMN_PRODUCT_NAME);
        int priceColumnIndex = cursor.getColumnIndex(ProductContract.ProductEntry.COLUMN_PRODUCT_PRICE);
        int quantityColumnIndex = cursor.getColumnIndex(ProductContract.ProductEntry.COLUMN_PRODUCT_STOCK);

        String productName = cursor.getString(nameColumnIndex);
        Float productPrice = cursor.getFloat(priceColumnIndex);
        final Integer productQuantity = cursor.getInt(quantityColumnIndex);

        final Uri productUri = ContentUris.withAppendedId(ProductContract.ProductEntry.CONTENT_URI,
                cursor.getInt(cursor.getColumnIndexOrThrow(ProductContract.ProductEntry._ID)));


        nameTextView.setText(productName);
        priceTextView.setText(context.getString(R.string.currency) + " " + productPrice);
        quantityTextView.setText(productQuantity.toString());

        saleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int newProductQuantity;
                if (productQuantity > 0) {
                    newProductQuantity = productQuantity -1;
                    ContentValues contentValues = new ContentValues();
                    contentValues.put(ProductContract.ProductEntry.COLUMN_PRODUCT_STOCK, newProductQuantity);
                    mContext.getContentResolver().update(productUri, contentValues, null, null);
                }
                else {
                    Toast.makeText(mContext, mContext.getString(R.string.minimum_stock_toast), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
