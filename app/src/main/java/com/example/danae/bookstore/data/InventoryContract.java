package com.example.danae.bookstore.data;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * API Contract for the BookStore App
 */
public final class InventoryContract {

    private InventoryContract() {
    }


    /**
     * The "Content authority" is a name for the entire content provider
     */
    public static final String CONTENT_AUTHORITY = "com.example.danae.bookstore";

    /**
     * Use CONTENT_AUTHORITY to create the base of all URI's which apps will use to contact
     * the content provider.
     */
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    /**
     * Possible path (appended to base content URI for possible URI's)
     */
    public static final String PATH_INVENTORY = "inventory";


    /**
     * Inner class to define the constant values for the inventory database table.
     * Each entry in the table represents a product in the inventory.
     */
    public static final class InventoryEntry implements BaseColumns {

        /**
         * The content URI to access the inventory data in the provider
         */
        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_INVENTORY);

        /**
         * The MIME type of the {@link #CONTENT_URI} for the inventory list.
         */
        public static final String CONTENT_LIST_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_INVENTORY;

        /**
         * The MIME type of the {@link #CONTENT_URI} for a single inventory item.
         */
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_INVENTORY;

        /**
         * Name of the database table
         */
        public static final String TABLE_NAME = "inventory";

        /**
         * Unique ID number for products
         * Type: INTEGER
         */
        public static final String _ID = BaseColumns._ID;

        /**
         * Product Name
         * Type: TEXT
         */
        public static final String COLUMN_PRODUCT_NAME = "name";

        /**
         * Product Price
         * Type: INTEGER
         */
        public static final String COLUMN_PRICE = "price";

        /**
         * Product Quantity
         * Type: INTEGER
         */
        public static final String COLUMN_QUANTITY = "quantity";

        /**
         * Supplier Name
         * Type: TEXT
         */
        public static final String COLUMN_SUPPLIER_NAME = "supplier";

        /**
         * Supplier Phone Number
         * Type: TEXT
         */
        public static final String COLUMN_SUPPLIER_NUMBER = "supplierNumber";
    }

}
