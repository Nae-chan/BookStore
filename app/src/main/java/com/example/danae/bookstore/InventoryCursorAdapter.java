package com.example.danae.bookstore;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.example.danae.bookstore.data.InventoryContract.InventoryEntry;

/**
 * {@link InventoryCursorAdapter} is an adapter for a list or grid view
 * that uses a {@link Cursor} of item data as its data source. This adapter knows
 * how to create list items for each row of item data in the {@link Cursor}.
 */

public class InventoryCursorAdapter extends CursorAdapter {

    /**
     * Constructs a new {@link InventoryCursorAdapter}.
     *
     * @param context The context
     * @param c       The cursor from which to get the data.
     */
    public InventoryCursorAdapter(Context context, Cursor c) {
        super(context, c, 0 /*flag*/);
    }

    /**
     * Makes a new blank list item view. No data is set (or bound) to the views yet.
     *
     * @param context app context
     * @param cursor  The cursor from which to get the data. The cursor is already
     *                moved to the correct position.
     * @param parent  The parent to which the new view is attached to
     * @return the newly created list item view.
     */
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        //Inflate a list item view using the layout specified in the list_item.xml
        return LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
    }

    /**
     * This method binds the inventory data (in the current row pointed to by cursor) to the given
     * list item layout. For example, the name for the current product can be set on the name TextView
     * in the list item layout.
     *
     * @param view    Existing view, returned earlier by newView() method
     * @param context app context
     * @param cursor  The cursor from which to get the data. The cursor is already moved to the
     *                correct row.
     */
    @Override
    public void bindView(View view, final Context context, final Cursor cursor) {

        // Find individual views to modify in the list item layout
        TextView nameTextView = view.findViewById(R.id.name);
        TextView priceTextView = view.findViewById(R.id.price);
        TextView quantityTextView = view.findViewById(R.id.quantity);
        Button quickSaleButton = view.findViewById(R.id.quick_sale_button);


        //------------------- Implementing the TextViews -------------------------//

        //Find the columns for the info needed to display
        int nameColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_PRODUCT_NAME);
        int priceColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_PRICE);
        int quantityColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_QUANTITY);

        //Get the info from the current Cursor
        String itemName = cursor.getString(nameColumnIndex);
        String itemPrice = cursor.getString(priceColumnIndex);
        String itemQuantity = cursor.getString(quantityColumnIndex);

        //If there's no Quantity, show zero
        if (TextUtils.isEmpty(itemQuantity)) {
            itemQuantity = "0";
        }

        // Update the TextViews with the info for the current item
        nameTextView.setText(itemName);
        priceTextView.setText(itemPrice);
        quantityTextView.setText(itemQuantity);

        // ---------------- Implementing the QuickSaleButton -------------------//

        // Find the cursor position to identify the current item
        final int position = cursor.getPosition();

        // Set an onClickListener for the button to decrease quantity for that item
        quickSaleButton.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Set the cursor to the position of the button clicked
                cursor.moveToPosition(position);

                //Get the item ID of the current row
                int itemId = cursor.getInt(cursor.getColumnIndex(InventoryEntry._ID));
                int quantityValue = cursor.getInt(cursor.getColumnIndex(InventoryEntry.COLUMN_QUANTITY));

                // If quantity is greater than 0, decrease the quantity by 1 and update the db and swap the cursor
                if (quantityValue == 0) {
                    Toast.makeText(context, R.string.invalid, Toast.LENGTH_SHORT).show();
                } else {
                    if (quantityValue > 0) {
                        quantityValue--;
                        ContentValues values = new ContentValues();
                        values.put(InventoryEntry.COLUMN_QUANTITY, quantityValue);

                        Toast.makeText(context, R.string.complete, Toast.LENGTH_SHORT).show();

                        Uri updateUri = ContentUris.withAppendedId(InventoryEntry.CONTENT_URI, itemId);
                        context.getContentResolver().update(updateUri, values, null, null);
                        swapCursor(cursor);
                    }
                }
            }
        });
    }
}