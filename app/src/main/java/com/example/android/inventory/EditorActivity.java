package com.example.android.inventory;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.android.inventory.data.ProductContract;

import java.io.FileDescriptor;
import java.io.IOException;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by lilla on 27/07/17.
 */

public class EditorActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor> {

    private static final int IMAGE_READ_REQUEST_CODE = 0;

    @BindView(R.id.add_product_name)
    EditText productNameEditText;
    @BindView(R.id.add_product_price)
    EditText productPriceEditText;
    @BindView(R.id.add_product_stock)
    EditText productStockEditText;
    @BindView(R.id.add_product_supplier_name)
    EditText productSupplierNameEditText;
    @BindView(R.id.add_product_supplier_email)
    EditText productSupplierEmailEditText;
    @BindView(R.id.add_image_button)
    Button addImageButton;
    @BindView(R.id.newly_added_image)
    ImageView productImageView;

    String nameString;
    String priceString;
    String stockString;
    String supplierNameString;
    String supplierEmailString;
    String productImagePath;

    private Uri mProductImageUri;
    private Bitmap mBitmap;

    private boolean mProductHasChanged = false;
    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            mProductHasChanged = true;
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);
        ButterKnife.bind(this);

        setTitle(getString(R.string.editor_activity_title));

        productNameEditText.setOnTouchListener(mTouchListener);
        productPriceEditText.setOnTouchListener(mTouchListener);
        productStockEditText.setOnTouchListener(mTouchListener);
        productSupplierNameEditText.setOnTouchListener(mTouchListener);
        productSupplierEmailEditText.setOnTouchListener(mTouchListener);
        addImageButton.setOnTouchListener(mTouchListener);

        addImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openImagePicker(v);
            }
        });
    }

    /*Open image picker app (e.g. gallery) on device and let the user select an image*/
    public void openImagePicker(View view) {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/*");
        startActivityForResult(intent, IMAGE_READ_REQUEST_CODE);
    }

    /*Get the result of openImagePicker*/
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent resultData) {
        if (requestCode == IMAGE_READ_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            if (resultData != null) {
                mProductImageUri = resultData.getData();
                mBitmap = getBitmapFromUri(mProductImageUri);
                productImageView.setImageBitmap(mBitmap);
            }
        }
    }

    private Bitmap getBitmapFromUri(Uri uri) {
        try {
            ParcelFileDescriptor parcelFileDescriptor =
                    getContentResolver().openFileDescriptor(uri, "r");
            FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();
            Bitmap image = BitmapFactory.decodeFileDescriptor(fileDescriptor);
            parcelFileDescriptor.close();
            return image;
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    private void saveProduct() {
        ContentValues values = new ContentValues();
        values.put(ProductContract.ProductEntry.COLUMN_PRODUCT_NAME, nameString);
        values.put(ProductContract.ProductEntry.COLUMN_PRODUCT_PRICE, priceString);
        values.put(ProductContract.ProductEntry.COLUMN_PRODUCT_STOCK, stockString);
        values.put(ProductContract.ProductEntry.COLUMN_PRODUCT_SUPPLIER_NAME, supplierNameString);
        values.put(ProductContract.ProductEntry.COLUMN_PRODUCT_SUPPLIER_EMAIL, supplierEmailString);
        values.put(ProductContract.ProductEntry.COLUMN_PRODUCT_IMAGE, productImagePath);

        Uri newProductUri = getContentResolver().insert(ProductContract.ProductEntry.CONTENT_URI, values);

        if (newProductUri == null) {
            Toast.makeText(this, getString(R.string.editor_insert_product_failed),
                    Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, getString(R.string.editor_insert_product_successful),
                    Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_save:
                if (isFormFilledOut()) {
                    saveProduct();
                    finish();
                }
                return true;
            case android.R.id.home:
                if (!mProductHasChanged) {
                    NavUtils.navigateUpFromSameTask(EditorActivity.this);
                    return true;
                }
                DialogInterface.OnClickListener discardButtonClickListener =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                NavUtils.navigateUpFromSameTask(EditorActivity.this);
                            }
                        };

                showUnsavedChangesDialog(discardButtonClickListener);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /*Display warning message if there are unsaved changes when back button is pressed*/
    @Override
    public void onBackPressed() {
        if (!mProductHasChanged) {
            super.onBackPressed();
            return;
        }
        DialogInterface.OnClickListener discardButtonClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        finish();
                    }
                };
        showUnsavedChangesDialog(discardButtonClickListener);
    }

    /*Get the menu layout*/
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return true;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle bundle) {
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
    }

    /*Display a dialog message to the user that they have unsaved changes*/
    private void showUnsavedChangesDialog(
            DialogInterface.OnClickListener discardButtonClickListener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.unsaved_changes_dialog_msg);
        builder.setPositiveButton(R.string.discard, discardButtonClickListener);
        builder.setNegativeButton(R.string.keep_editing, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    /*Check if every product detail has been filled out and an images has been selected*/
    private boolean isFormFilledOut() {
        nameString = productNameEditText.getText().toString().trim();
        priceString = productPriceEditText.getText().toString().trim();
        stockString = productStockEditText.getText().toString().trim();
        supplierNameString = productSupplierNameEditText.getText().toString().trim();
        supplierEmailString = productSupplierEmailEditText.getText().toString().trim();
        productImagePath = mProductImageUri != null ? mProductImageUri.toString() : null;

        if (nameString.equals("") || priceString.equals("") || stockString.equals("") ||
                supplierNameString.equals("") || supplierEmailString.equals("")) {
            Toast.makeText(this, R.string.empty_field_message, Toast.LENGTH_SHORT).show();
            return false;
        } else if (productImagePath == null) {
            Toast.makeText(this, R.string.image_not_added_message, Toast.LENGTH_SHORT).show();
            return false;
        } else {
            return true;
        }
    }
}
