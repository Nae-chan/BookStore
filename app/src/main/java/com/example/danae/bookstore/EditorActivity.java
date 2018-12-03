package com.example.danae.bookstore;

import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.danae.bookstore.data.InventoryContract.InventoryEntry;
import com.example.danae.bookstore.data.InventoryDbHelper;

/**
 * Allows user to add or edit a product in the inventory
 */
public class EditorActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor> {

    public static final String LOG_TAG = InventoryDbHelper.class.getSimpleName();

    /**
     * Identifier for inventory loader
     */
    private static final int EXISTING_ITEM_LOADER = 0;

    //Content URI for the existing item (null if new item)
    private Uri mCurrentItemUri;

    /**
     * EditText fields for name, price, quantity, supplier name, and supplier number
     */
    private EditText mNameEditText;
    private EditText mPriceEditText;
    private EditText mQuantityEditText;
    private EditText mSupplierNameEditText;
    private EditText mSupplierNumberEditText;

    /**
     * Buttons to update quantity, call a supplier, and delete and item
     */
    private Button mIncreaseQuantity;
    private Button mDecreaseQuantity;
    private Button mCallSupplierButton;
    private Button mClearAllDataButton;

    /**
     * Boolean flag to track if items have been updated (true) or not (false)
     */
    private boolean mItemHasChanged = false;

    /**
     * Boolean to check if clearing data for an existing item
     */

    private boolean mClearAllSelected = false;

    /**
     * Boolean to check if user entered required fields
     */
    private boolean validEntry;

    /**
     * OnTouchListener that listens for when user touches a View, implying modification.
     * Change mItemHasChanged boolean to true.
     */
    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent motionEvent) {
            mItemHasChanged = true;
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        //Get the intent used to launch activity
        Intent intent = getIntent();
        mCurrentItemUri = intent.getData();

        //If the intent doesn't have an inventory content URI, then add new inventory
        if (mCurrentItemUri == null) {

            setTitle(getString(R.string.editor_activity_title_new));

            //hide supplier call button, since this is a new inventory item
            mCallSupplierButton = findViewById(R.id.call_supplier);
            mCallSupplierButton.setVisibility(View.INVISIBLE);

            //hide delete option, since this is a new inventory item
            invalidateOptionsMenu();

        } else {
            //Otherwise the inventory already exists, set the app bar to edit
            setTitle(getString(R.string.editor_activity_title_edit));

            //Initialize loader to read and display the current inventory item
            getLoaderManager().initLoader(EXISTING_ITEM_LOADER, null, this);
        }

        // Find all the views to get the user input
        mNameEditText = findViewById(R.id.edit_name);
        mPriceEditText = findViewById(R.id.edit_price);
        mQuantityEditText = findViewById(R.id.edit_quantity);
        mSupplierNameEditText = findViewById(R.id.edit_supplier_name);
        mSupplierNumberEditText = findViewById(R.id.edit_supplier_number);
        mCallSupplierButton = findViewById(R.id.call_supplier);
        mClearAllDataButton = findViewById(R.id.clear_all);
        mIncreaseQuantity = findViewById(R.id.quantity_increase);
        mDecreaseQuantity = findViewById(R.id.quantity_decrease);

        //Setup OnTouchListeners on all the input fields to track if user has made modifications
        mNameEditText.setOnTouchListener(mTouchListener);
        mPriceEditText.setOnTouchListener(mTouchListener);
        mQuantityEditText.setOnTouchListener(mTouchListener);
        mSupplierNameEditText.setOnTouchListener(mTouchListener);
        mSupplierNumberEditText.setOnTouchListener(mTouchListener);


        //When clear all button is clicked, editText input is erased
        mClearAllDataButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //If adding a new item, clear all fields
                mNameEditText.setText("");
                mPriceEditText.setText("");
                mQuantityEditText.setText("");
                mSupplierNameEditText.setText("");
                mSupplierNumberEditText.setText("");
                if (mCurrentItemUri != null) {
                    //If editing an item, regular validations still apply
                    mItemHasChanged = true;
                    validEntry = false;
                    mClearAllSelected = true;
                }
            }
        });

        //Set an intent to call the supplier when phone button is clicked
        mCallSupplierButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String supplierNumberString = mSupplierNumberEditText.getText().toString().trim();
                //Check for valid number to call
                if (supplierNumberString.length() < 10 || supplierNumberString.length() > 10) {
                    Toast.makeText(EditorActivity.this,
                            getString(R.string.supplier_number_required), Toast.LENGTH_SHORT).show();
                    return;
                }
                Intent intent = new Intent(Intent.ACTION_DIAL, Uri.fromParts
                        ("tel", supplierNumberString, null));
                if (intent.resolveActivity(getPackageManager()) != null) {
                    startActivity(intent);
                }
            }
        });

        //When the increase quantity button is pressed, quantity increases
        mIncreaseQuantity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mQuantityEditText.setText(increaseQuantity());
            }
        });

        //When the decrease quantity button is pressed, quantity decreases
        mDecreaseQuantity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mQuantityEditText.setText(decreaseQuantity());
            }
        });
    }

    // Increase quantity
    public String increaseQuantity() {
        int currentQuantity;
        String currentValue = mQuantityEditText.getText().toString();
        if (currentValue.equalsIgnoreCase("")) {
            currentQuantity = 0;
        } else {
            currentQuantity = Integer.parseInt(currentValue);
        }
        currentQuantity++;
        mItemHasChanged = true;
        return String.valueOf(currentQuantity);
    }

    // Decrease quantity and validate that quantity does not go below zero
    public String decreaseQuantity() {
        String currentValue = mQuantityEditText.getText().toString();
        if (currentValue.equalsIgnoreCase("")) {
            currentValue = "0";
        }
        int currentQuantity = Integer.parseInt(currentValue);

        if (currentQuantity == 0) {
            Toast.makeText(this, R.string.invalid, Toast.LENGTH_SHORT).show();
            currentQuantity = 0;
        } else {
            currentQuantity--;
            mItemHasChanged = true;
        }
        return String.valueOf(currentQuantity);
    }

    /**
     * Get user input and save it into the inventory database.
     */
    private void saveInventory() {

        //Get user input from edittext fields and trim any white space
        String nameString = mNameEditText.getText().toString().trim();
        String priceString = mPriceEditText.getText().toString().trim();
        String quantityString = mQuantityEditText.getText().toString().trim();
        String supplierNameString = mSupplierNameEditText.getText().toString().trim();
        String supplierNumberString = mSupplierNumberEditText.getText().toString().trim();

        //Read priceString as a number instead of text
        int priceNumber = 0;
        if (!TextUtils.isEmpty(priceString)) {
            priceNumber = Integer.parseInt(priceString);
        }

        //Show catered error to user if there's an empty or invalid field
        if (TextUtils.isEmpty(nameString)) {
            Toast.makeText(this, getString(R.string.name_required),
                    Toast.LENGTH_SHORT).show();
        } else if (TextUtils.isEmpty(priceString)) {
            Toast.makeText(this, getString(R.string.price_required),
                    Toast.LENGTH_SHORT).show();
        } else if (priceNumber == 0) {
            Toast.makeText(this, getString(R.string.price_required),
                    Toast.LENGTH_SHORT).show();
        } else if (TextUtils.isEmpty(supplierNameString)) {
            Toast.makeText(this, getString(R.string.supplier_name_required),
                    Toast.LENGTH_SHORT).show();
        } else if (TextUtils.isEmpty(supplierNumberString)) {
            Toast.makeText(this, getString(R.string.supplier_number_required),
                    Toast.LENGTH_SHORT).show();
        } else if (supplierNumberString.length() < 10 || supplierNumberString.length() > 10) {
            Toast.makeText(this, getString(R.string.supplier_number_required),
                    Toast.LENGTH_SHORT).show();
        } else {
            validEntry = true;
        }

        //If No fields were updated, do not create Content Values
        if (mCurrentItemUri == null && validEntry == false) {
            return;
        }
        if (mCurrentItemUri != null && validEntry == false || mClearAllSelected == true) {
            return;
        }


        //Create ContentValues object with keys as column names and user input as values
        ContentValues values = new ContentValues();
        values.put(InventoryEntry.COLUMN_PRODUCT_NAME, nameString);
        values.put(InventoryEntry.COLUMN_PRICE, priceString);
        values.put(InventoryEntry.COLUMN_QUANTITY, quantityString);
        values.put(InventoryEntry.COLUMN_SUPPLIER_NAME, supplierNameString);
        values.put(InventoryEntry.COLUMN_SUPPLIER_NUMBER, supplierNumberString);

        //Determine if this is a new or existing item
        if (mCurrentItemUri == null) {
            // This is a NEW item; feed into provider to create content URI
            Uri newUri = getContentResolver().insert(InventoryEntry.CONTENT_URI, values);

            //Display a toast stating if insertion was successful
            if (newUri == null) {
                Toast.makeText(this, R.string.editor_insert_failed,
                        Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, R.string.editor_insert_successful,
                        Toast.LENGTH_SHORT).show();
            }
        } else {
            //Otherwise this item EXISTS; update the item with content URI and pass in new
            //ContentValues. Pass in null for selection and selection arg since mCurrentItemUri
            //already identifies row to modify.
            int rowsAffected = getContentResolver().update(mCurrentItemUri, values,
                    null, null);

            // Show a toast message depending on whether or not the update was successful.
            if (rowsAffected == 0) {
                // If no rows were affected, then there was an error with the update.
                Toast.makeText(this, getString(R.string.editor_update_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the update was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editor_update_successful),
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * This method is called after invalidateOptionsMenu(), so that the
     * menu can be updated (some menu items can be hidden or made visible).
     */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        // If this is a new item, hide the "Delete" menu item.
        if (mCurrentItemUri == null) {
            MenuItem menuItem = menu.findItem(R.id.action_delete);
            menuItem.setVisible(false);
        }
        return true;
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
                saveInventory();
                if (validEntry) {
                    finish();
                }
                return true;

            // Respond to a click on the "Delete" menu_editor option
            case R.id.action_delete:
                // Pop up dialog to confirm deletion
                showDeleteConfirmationDialog();
                return true;

            // Respond to a click on the "Up" arrow button in the app bar
            case android.R.id.home:
                // Navigate back to parent activity (CatalogActivity)
                if (!mItemHasChanged) {
                    NavUtils.navigateUpFromSameTask(EditorActivity.this);
                    return true;
                }

                // Otherwise if there are unsaved changes, confirm if user is certain
                DialogInterface.OnClickListener backButtonClickListener =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                // User clicked back button, navigate to parent activity.
                                NavUtils.navigateUpFromSameTask(EditorActivity.this);
                            }
                        };

                // Show a dialog that notifies the user about unsaved changes
                showUnsavedChangesDialog(backButtonClickListener);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * This method is called when the back button is pressed.
     */
    @Override
    public void onBackPressed() {
        // If the item hasn't changed, continue with handling back button press
        if (!mItemHasChanged) {
            super.onBackPressed();
            return;
        }

        // Otherwise if there are unsaved changes, confirm if user is certain
        DialogInterface.OnClickListener backButtonClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // User clicked back button, close the current activity.
                        finish();
                    }
                };

        // Show dialog that there are unsaved changes
        showUnsavedChangesDialog(backButtonClickListener);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if (mCurrentItemUri == null) {
            return null;
        }
        // Define a projection that contains all columns from the items table
        String[] projection = {
                InventoryEntry._ID,
                InventoryEntry.COLUMN_PRODUCT_NAME,
                InventoryEntry.COLUMN_PRICE,
                InventoryEntry.COLUMN_QUANTITY,
                InventoryEntry.COLUMN_SUPPLIER_NAME,
                InventoryEntry.COLUMN_SUPPLIER_NUMBER};

        // This loader will execute the ContentProvider's query method on a background thread.
        return new CursorLoader(this,       // Parent activity context
                mCurrentItemUri,                    // Query the content URI for the current item
                projection,                         // Columns to include in the resulting cursor
                null,                       // No selection clause
                null,                   // No selection arguments
                null);                       // Default sort order
    }

    /**
     * Show a warning dialog if the user tries to leave without saving
     *
     * @param backButtonClickListener
     */
    private void showUnsavedChangesDialog(
            DialogInterface.OnClickListener backButtonClickListener) {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.unsaved_changes);
        builder.setPositiveButton(R.string.unsaved_confirm, backButtonClickListener);
        builder.setNegativeButton(R.string.stay_here, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Keep editing" button, so dismiss the dialog
                // and continue editing the item.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }


    /**
     * Prompt the user to confirm that they want to delete this item.
     */
    private void showDeleteConfirmationDialog() {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog);
        builder.setPositiveButton(R.string.delete_confirm, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User selected "Delete" button, so delete the item.
                deleteItem();
            }
        });
        builder.setNegativeButton(R.string.stay_here, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User cancelled the delete, so dismiss the dialog and continue editor activity
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });
        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    /**
     * Perform the deletion of the item in the database.
     */
    private void deleteItem() {
        // Only perform the delete if this is an existing item.
        if (mCurrentItemUri != null) {
            // Call the ContentResolver to delete the item at the given content URI.
            // Pass in null for the selection and selection args because the mCurrentItemUri
            // content URI already identifies the item that we want.
            int rowsDeleted = getContentResolver().delete(mCurrentItemUri,
                    null, null);

            // Show a toast message depending on whether or not the delete was successful.
            if (rowsDeleted == 0) {
                // If no rows were deleted, then there was an error with the delete.
                Toast.makeText(this, getString(R.string.editor_delete_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the delete was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editor_delete_successful),
                        Toast.LENGTH_SHORT).show();
            }
        }
        // Close the activity
        finish();
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        // Bail early if the cursor is null or there is less than 1 row in the cursor
        if (cursor == null || cursor.getCount() < 1) {
            return;
        }

        // Proceed with moving to the first row of the cursor and reading the data
        if (cursor.moveToFirst()) {
            // Find the columns of item attributes that we're interested in
            int nameColumnIndex = cursor.getColumnIndex
                    (InventoryEntry.COLUMN_PRODUCT_NAME);
            int priceColumnIndex = cursor.getColumnIndex
                    (InventoryEntry.COLUMN_PRICE);
            int quantityColumnIndex = cursor.getColumnIndex
                    (InventoryEntry.COLUMN_QUANTITY);
            int supplierNameColumnIndex = cursor.getColumnIndex
                    (InventoryEntry.COLUMN_SUPPLIER_NAME);
            int supplierNumberColumnIndex = cursor.getColumnIndex
                    (InventoryEntry.COLUMN_SUPPLIER_NUMBER);

            // Extract out the value from the Cursor for the given column index
            String name = cursor.getString(nameColumnIndex);
            String price = cursor.getString(priceColumnIndex);
            String quantity = cursor.getString(quantityColumnIndex);
            String supplierName = cursor.getString(supplierNameColumnIndex);
            String supplierNumber = cursor.getString(supplierNumberColumnIndex);

            // Update the views on the screen with the values from the database
            mNameEditText.setText(name);
            mPriceEditText.setText(price);
            mQuantityEditText.setText(quantity);
            mSupplierNameEditText.setText(supplierName);
            mSupplierNumberEditText.setText(supplierNumber);

            //Format and set the supplier's phone number
            //String formattedNumber = PhoneNumberUtils.formatNumber(supplierNumber);
            //mSupplierNumberEditText.setText(formattedNumber);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // If the loader is invalidated, clear out all the data from the input fields.
        mNameEditText.setText("");
        mPriceEditText.setText("");
        mQuantityEditText.setText("");
        mSupplierNameEditText.setText("");
        mSupplierNumberEditText.setText("");
    }

}
