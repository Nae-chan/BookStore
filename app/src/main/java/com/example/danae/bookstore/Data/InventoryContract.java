package com.example.danae.bookstore.Data;

import android.provider.BaseColumns;

/**
 * API Contract for the BookStore App
 */
public final class InventoryContract {

    private InventoryContract() {
    }

    /**
     * Inner class to define the constant values for the inventory database table.
     * Each entry in the table represents a product in the inventory.
     */
    public static final class InventoryEntry implements BaseColumns {

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
