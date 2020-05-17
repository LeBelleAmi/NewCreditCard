package com.example.newcreditcard;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.flutterwave.raveandroid.RaveConstants;
import com.flutterwave.raveandroid.RavePayActivity;
import com.flutterwave.raveandroid.RavePayManager;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;

import java.util.UUID;

public class RaveActivity extends AppCompatActivity {

    Button ravePay;
    RelativeLayout raveContent;
    TextInputEditText etFirstName, etLastName, etEmail;
    int amount = 1000;
    String email;
    String fName;
    String lName;
    String narration = "Test Pay";
    String country = "NG";
    String currency = "NGN";
    Toolbar toolbar;

    final String publicKey = "FLWPUBK-eac60a65582cd8188dcb8a97259ab33f-X";
    final String encryptionKey = "cbd808e85a079095eb1c7780";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rave);


        raveContent = findViewById(R.id.rave_maincontent);
        ravePay = findViewById(R.id.rave_button);
        etFirstName = findViewById(R.id.et_firstname);
        etLastName = findViewById(R.id.et_lastname);
        etEmail = findViewById(R.id.et_emailTwo);


        toolbar = findViewById(R.id.toolbarTwo);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        ravePay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!checkEntries()){
                    Toast.makeText(RaveActivity.this, "Your payment is not valid, please enter details", Toast.LENGTH_SHORT).show();
                }else {
                    chargePayment(amount);
                }
            }
        });
    }


    private boolean checkEntries() {

        boolean check = true;

        fName = etFirstName.getText().toString();
        lName = etLastName.getText().toString();
        email = etEmail.getText().toString();


        if (TextUtils.isEmpty(fName)) {
            etFirstName.setError("Field is required.");
            Toast.makeText(RaveActivity.this, "Enter Valid First Name", Toast.LENGTH_SHORT).show();
            check = false;
        } else {
            etFirstName.setError(null);
        }

        if (TextUtils.isEmpty(lName)) {
            etLastName.setError("Field is required.");
            Toast.makeText(RaveActivity.this, "Enter Valid Last Name", Toast.LENGTH_SHORT).show();
            check = false;
        } else {
            etLastName.setError(null);
        }


        if (TextUtils.isEmpty(email)) {
            etEmail.setError("Field is required.");
            Toast.makeText(RaveActivity.this, "Enter Valid Email Address", Toast.LENGTH_SHORT).show();
            check = false;
        } else {
            etEmail.setError(null);
        }

        return check;
    }


    private void chargePayment(int amount) {

        String transactionReference = email +" "+  UUID.randomUUID().toString();

        /*
        Create instance of RavePayManager
         */
        new RavePayManager(this).setAmount(amount)
                .setCountry(country)
                .setCurrency(currency)
                .setEmail(email)
                .setfName(fName)
                .setlName(lName)
                .setNarration(narration)
                .setPublicKey(publicKey)
                .setEncryptionKey(encryptionKey)
                .setTxRef(transactionReference)
                .acceptAccountPayments(true)
                .acceptCardPayments(true)
                .acceptMpesaPayments(false)
                .acceptGHMobileMoneyPayments(false)
                .onStagingEnv(true)
                .allowSaveCardFeature(true)
                .withTheme(R.style.DefaultTheme)
                .initialize();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        /*
         *  We advise you to do a further verification of transaction's details on your server to be
         *  sure everything checks out before providing service or goods.
         */
        if (requestCode == RaveConstants.RAVE_REQUEST_CODE && data != null) {
            String message = data.getStringExtra("response");
            if (resultCode == RavePayActivity.RESULT_SUCCESS) {
               // Toast.makeText(this, "SUCCESS " + message, Toast.LENGTH_SHORT).show();
                Snackbar snackbar =
                        Snackbar.make(raveContent, "Transaction is Successful! " + message, Snackbar.LENGTH_INDEFINITE).
                                setAction("Dismiss", new View.OnClickListener(){
                                    @Override
                                    public void onClick(View view){}});

                snackbar.setActionTextColor(Color.WHITE);
                View snackbarView = snackbar.getView();
                snackbarView.setBackgroundColor(getResources().getColor(R.color.purple_800));
                snackbar.show();
            }
            else if (resultCode == RavePayActivity.RESULT_ERROR) {
                //Toast.makeText(this, "ERROR " + message, Toast.LENGTH_SHORT).show();
                Snackbar snackbar =
                        Snackbar.make(raveContent, "Transaction is not Successful! error: " + message, Snackbar.LENGTH_INDEFINITE).
                                setAction("Dismiss", new View.OnClickListener(){
                                    @Override
                                    public void onClick(View view){}});

                snackbar.setActionTextColor(Color.WHITE);
                View snackbarView = snackbar.getView();
                snackbarView.setBackgroundColor(getResources().getColor(R.color.purple_800));
                snackbar.show();
            }
            else if (resultCode == RavePayActivity.RESULT_CANCELLED) {
                //Toast.makeText(this, "CANCELLED " + message, Toast.LENGTH_SHORT).show();
                Snackbar snackbar =
                        Snackbar.make(raveContent, "Transaction is cancelled! " + message, Snackbar.LENGTH_INDEFINITE).
                                setAction("Dismiss", new View.OnClickListener(){
                                    @Override
                                    public void onClick(View view){}});

                snackbar.setActionTextColor(Color.WHITE);
                View snackbarView = snackbar.getView();
                snackbarView.setBackgroundColor(getResources().getColor(R.color.purple_800));
                snackbar.show();
            }
        }
        else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            // This ID represents the Home or Up button. In the case of this
            // activity, the Up button is shown. Use NavUtils to allow users
            // to navigate up one level in the application structure. For
            // more details, see the Navigation pattern on Android Design:
            //
            // http://developer.android.com/design/patterns/navigation.html#up-vs-back
            //
            //navigateUpFromSameTask(this);
            onBackPressed(); // close this activity and return to preview activity (if there is any)
            //return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
