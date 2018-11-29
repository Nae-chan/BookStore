package com.example.danae.bookstore;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import com.example.danae.bookstore.Data.InventoryContract.InventoryEntry;
import com.example.danae.bookstore.Data.InventoryDbHelper;

/**
 * Allows user to add or edit a product in the inventory
 */
public class EditorActivity extends AppCompatActivity {

    public static final String LOG_TAG = InventoryDbHelper.class.getSimpleName();
    //Check if user entered all required fields
    public boolean validEntry;
    /**
     * EditText field to enter the item's name
     */
    private EditText mNameEditText;
    /**
     * EditText field to enter the item's price
     */
    private EditText mPriceEditText;
    /**
     * EditText field to enter the item's quantity
     */
    private EditText mQuantityEditText;
    /**
     * EditText field to enter the item's supplier name
     */
    private EditText mSupplierNameEditText;
    /**
     * EditText field to enter the item's supplier phone number
     */
    private EditText mSupplierNumberEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        // Find all the views to get the user input
        mNameEditText = findViewById(R.id.edit_name);
        mPriceEditText = findViewById(R.id.edit_price);
        mQuantityEditText = findViewById(R.id.edit_quantity);
        mSupplierNameEditText = findViewById(R.id.edit_supplier_name);
        mSupplierNumberEditText = findViewById(R.id.edit_supplier_number);
    }

    private void insertInventory() {

        //Get user input from edittext fields
        String nameString = mNameEditText.getText().toString().trim();
        String priceString = mPriceEditText.getText().toString().trim();
        String quantityString = mQuantityEditText.getText().toString().trim();
        String supplierNameString = mSupplierNameEditText.getText().toString().trim();
        String supplierNumberString = mSupplierNumberEditText.getText().toString().trim();

        //Show error to user if empty field
        if (TextUtils.isEmpty(nameString)) {
            Toast.makeText(this, getString(R.string.name_required), Toast.LENGTH_SHORT).show();
        } else if (TextUtils.isEmpty(priceString)) {
            Toast.makeText(this, getString(R.string.price_required), Toast.LENGTH_SHORT).show();
        } else if (TextUtils.isEmpty(supplierNameString)) {
            Toast.makeText(this, getString(R.string.supplier_name_required), Toast.LENGTH_SHORT).show();
        } else if (TextUtils.isEmpty(supplierNumberString)) {
            Toast.makeText(this, getString(R.string.supplier_number_required), Toast.LENGTH_SHORT).show();
        } else {

            validEntry = true;

            //Create database helper
            InventoryDbHelper mDbHelper = new InventoryDbHelper(this);

            //Gets database in write mode
            SQLiteDatabase db = mDbHelper.getWritableDatabase();

            //Create ContentValues object with keys as column names and user input as values
            ContentValues values = new ContentValues();
            values.put(InventoryEntry.COLUMN_PRODUCT_NAME, nameString);
            values.put(InventoryEntry.COLUMN_PRICE, priceString);
            values.put(InventoryEntry.COLUMN_QUANTITY, quantityString);
            values.put(InventoryEntry.COLUMN_SUPPLIER_NAME, supplierNameString);
            values.put(InventoryEntry.COLUMN_SUPPLIER_NUMBER, supplierNumberString);

            //Insert a new row for each product, returning the ID of that new row.
            long newRowId = db.insert(InventoryEntry.TABLE_NAME, null, values);
            Log.i(LOG_TAG, "db value is:" + newRowId);

            //Display a toast stating if insertion was successful
            if (newRowId == -1) {
                Toast.makeText(this, "Error with saving product", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, "Product added! Row id:" + newRowId, Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu_editor options from the res/menu_editor/menu_editor.xml file.
        // This adds menu_editor items to the app bar.
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Perform action the user clicked on in the menu_editor options
        switch (item.getItemId()) {
            // Respond to a click on the "Save" menu_editor option
            case R.id.action_save:
                // Save product to database
                insertInventory();
                if (validEntry) {
                    finish();
                }
                return true;
            // Respond to a click on the "Delete" menu_editor option
            case R.id.action_delete:
                // Do nothing for now
                return true;
            // Respond to a click on the "Up" arrow button in the app bar
            case android.R.id.home:
                // Navigate back to parent activity (CatalogActivity)
                NavUtils.navigateUpFromSameTask(this);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
