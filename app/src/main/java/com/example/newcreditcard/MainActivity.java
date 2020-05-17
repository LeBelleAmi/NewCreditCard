package com.example.newcreditcard;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;

import co.paystack.android.Paystack;
import co.paystack.android.PaystackSdk;
import co.paystack.android.Transaction;
import co.paystack.android.model.Card;
import co.paystack.android.model.Charge;


public class MainActivity extends AppCompatActivity {

    TextView tvName,tvValidity, tvNumber, tvCvv;
    TextInputEditText etCvv, etName, etNumber, etValidity, etEmail;
    String cardNumber, cardCVV, cardValidity, cardName, cardEmail;
    int expiryMonth, expiryYear, amountInKobo;
    ImageView ivType;
    Button btnNext;
    private boolean isDelete;
    private Card card;
    private Charge charge;
    LinearLayout main_content;
    // Toolbar
    Toolbar toolbar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //init paystack sdk
        PaystackSdk.initialize(getApplicationContext());


        tvName = findViewById(R.id.tv_member_name);
        tvNumber = findViewById(R.id.tv_card_number);
        tvValidity = findViewById(R.id.tv_validity);
        tvCvv = findViewById(R.id.tv_cvv);
        ivType = findViewById(R.id.ivType);
        btnNext = findViewById(R.id.next_button);
        etCvv = findViewById(R.id.et_cvv);
        etName = findViewById(R.id.et_name);
        etNumber = findViewById(R.id.et_number);
        etValidity = findViewById(R.id.et_validity);
        etEmail = findViewById(R.id.et_email);
        main_content = findViewById(R.id.main_content);
        toolbar = findViewById(R.id.toolbar);
        // Sets the Toolbar to act as the ActionBar for this Activity window.
        // Make sure the toolbar exists in the activity and is not null
        setSupportActionBar(toolbar);

        final Integer[] imageArray = { R.drawable.visa_white_logo, R.drawable.mastercard_black, R.drawable.discover_black_logo, R.drawable.american_express_logo, R.drawable.verve};


        etNumber.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int count) {
                if (charSequence.toString().trim().length() == 0) {
                    tvNumber.setText("XXXX XXXX XXXX XXXX");
                } else {
                    String number = CreditCardUtils.insertPeriodically(charSequence.toString().trim(), " ", 4);
                    tvNumber.setText(number);
                    //setCardType(type);
                    String cardNumber = charSequence.toString();

                    if(cardNumber.length()>=2)
                    {
                        for (int a = 0; a < CreditCardUtils.listOfPattern().size(); a++)
                        {
                            if (cardNumber.substring(0, 2).matches(CreditCardUtils.listOfPattern().get(a)))
                            {
                                ivType.setImageResource(imageArray[a]);

                                }
                        }
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

                if (!etNumber.getText().toString().equalsIgnoreCase(""))
                {
                    for (int i = 0; i < CreditCardUtils.listOfPattern().size(); i++)
                    {
                        if (etNumber.getText().toString().matches(CreditCardUtils.listOfPattern().get(i)))
                        {
                            ivType.setImageResource(imageArray[i]);
                        }
                    }
                }
                else
                {
                    ivType.setImageResource(android.R.color.transparent);
                }

            }
        });

        etValidity.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int count) {
                if(i1==0)
                    isDelete=false;
                else
                    isDelete=true;
            }

            @Override
            public void afterTextChanged(Editable editable) {
                String source = editable.toString();
                int length=source.length();

                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append(source);

                if(length>0 && length==3)
                {
                    if(isDelete)
                        stringBuilder.deleteCharAt(length-1);
                    else
                        stringBuilder.insert(length-1,"/");

                    etValidity.setText(stringBuilder);
                    etValidity.setSelection(etValidity.getText().length());

                    // Log.d("test"+s.toString(), "afterTextChanged: append "+length);
                }

                if(tvValidity!=null)
                {
                    if(length==0)
                        tvValidity.setText("MM/YY");
                    else
                        tvValidity.setText(stringBuilder);
                }
            }
        });


        etCvv.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int count) {
                if (charSequence.toString().trim().length() == 0) {
                    tvCvv.setText("XXX");
                } else {
                    tvCvv.setText(charSequence.toString().trim());
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        etName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int count) {
                if (charSequence.toString().trim().length() == 0) {
                    tvName.setText("CARD HOLDER");
                } else {
                    tvName.setText(charSequence.toString().trim());
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });


        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(!checkEntries()){
                    Toast.makeText(MainActivity.this, "Your card is not valid, please enter details", Toast.LENGTH_SHORT).show();
                }else {
                    cardName = etName.getText().toString().trim();
                    cardNumber = etNumber.getText().toString().trim();
                    expiryMonth = Integer.parseInt(etValidity.getText().toString().substring(0,2).trim());
                    expiryYear = Integer.parseInt(etValidity.getText().toString().substring(cardValidity.length() - 2).trim());
                    cardCVV = etCvv.getText().toString().trim();

                    /*String cardNumber = "4084084084084081";
                    int expiryMonth = 11; //any month in the future
                    int expiryYear = 20; // any year in the future
                    String cardCVV = "408";
                    String cardName = "Oma";
*/
                    card = new Card(cardNumber, expiryMonth, expiryYear, cardCVV, cardName);
                    //Toast.makeText(MainActivity.this, "Your card is added" + expiryMonth +" exY "+ expiryYear, Toast.LENGTH_SHORT).show();

                    if (card.isValid()) {
                        Toast.makeText(MainActivity.this, "Card is Valid", Toast.LENGTH_LONG).show();
                        performCharge();
                    } else {
                        Toast.makeText(MainActivity.this, "Card not Valid", Toast.LENGTH_LONG).show();
                    }
                }
            }
        });


    }


    private boolean checkEntries() {

        boolean check = true;

        cardName = etName.getText().toString();
        cardNumber = etNumber.getText().toString();
        cardValidity = etValidity.getText().toString();
        cardCVV = etCvv.getText().toString();
        cardEmail = etEmail.getText().toString();


        if (TextUtils.isEmpty(cardName)) {
            etName.setError("Field is required.");
            Toast.makeText(MainActivity.this, "Enter Valid Name", Toast.LENGTH_SHORT).show();
            check = false;
        } else {
            etName.setError(null);
        }

        if (TextUtils.isEmpty(cardNumber) || !CreditCardUtils.isValid(cardNumber.replace(" ",""))) {
            etNumber.setError("Field is required.");
            Toast.makeText(MainActivity.this, "Enter Valid Card Number", Toast.LENGTH_SHORT).show();
            check = false;
        } else {
            etNumber.setError(null);
        }

        if (TextUtils.isEmpty(cardValidity)||!CreditCardUtils.isValidDate(cardValidity)) {
            etValidity.setError("Field is required.");
            Toast.makeText(MainActivity.this, "Enter Correct Validity", Toast.LENGTH_SHORT).show();
            check = false;
        } else {
            etValidity.setError(null);
        }

        if (TextUtils.isEmpty(cardCVV)||cardCVV.length()<3) {
            etCvv.setError("Field is required.");
            Toast.makeText(MainActivity.this, "Enter Valid Security Number", Toast.LENGTH_SHORT).show();
            check = false;
        } else{
            etCvv.setError(null);
        }

        if (TextUtils.isEmpty(cardEmail)) {
            etEmail.setError("Field is required.");
            Toast.makeText(MainActivity.this, "Enter Valid Email Address", Toast.LENGTH_SHORT).show();
            check = false;
        } else {
            etEmail.setError(null);
        }


        return check;
    }

    // This is the subroutine you will call after creating the charge
    // adding a card and setting the access_code
    public void performCharge(){

        //create a Charge object
        Charge charge = new Charge();
        charge.setCard(card); //sets the card to charge

        //the amount(in KOBO eg 1000 kobo = 10 Naira) the customer is to pay for the product or service
        // basically add 2 extra zeros at the end of your amount to convert from kobo to naira.
        amountInKobo = 10000;
        charge.setAmount(amountInKobo);
        charge.setCurrency("NGN");
        charge.setEmail(cardEmail);
        charge.setCard(card);

        PaystackSdk.chargeCard(MainActivity.this, charge, new Paystack.TransactionCallback() {
            @Override
            public void onSuccess(Transaction transaction) {
                // This is called only after transaction is deemed successful.
                // Retrieve the transaction, and send its reference to your server
                // for verification.

                String paymentReference = transaction.getReference();
                /*Toast.makeText(MainActivity.this, "Transaction Successful! payment reference: "
                        + paymentReference, Toast.LENGTH_LONG).show();*/
                Snackbar snackbar =
                        Snackbar.make(main_content, "Transaction Successful! payment reference: " + paymentReference, Snackbar.LENGTH_INDEFINITE).
                                setAction("Dismiss", new View.OnClickListener(){
                                    @Override
                                    public void onClick(View view){}});

                snackbar.setActionTextColor(Color.WHITE);
                View snackbarView = snackbar.getView();
                snackbarView.setBackgroundColor(getResources().getColor(R.color.purple_800));
                snackbar.show();
            }

            @Override
            public void beforeValidate(Transaction transaction) {
                // This is called only before requesting OTP.
                // Save reference so you may send to server. If
                // error occurs with OTP, you should still verify on server.

                String paymentReference = transaction.getReference();/*
                Toast.makeText(MainActivity.this, "Transaction is being validated. Payment reference: "
                        + paymentReference, Toast.LENGTH_LONG).show();*/
                Snackbar snackbar =
                        Snackbar.make(main_content, "Transaction is being validated. Payment reference: " + paymentReference, Snackbar.LENGTH_LONG).
                                setAction("Dismiss", new View.OnClickListener(){
                                    @Override
                                    public void onClick(View view){}});

                snackbar.setActionTextColor(Color.WHITE);
                View snackbarView = snackbar.getView();
                snackbarView.setBackgroundColor(getResources().getColor(R.color.purple_800));
                snackbar.show();


            }

            @Override
            public void onError(Throwable error, Transaction transaction) {
                //handle error here
                String paymentReference = transaction.getReference();
/*                Toast.makeText(MainActivity.this, "Transaction not Successful! payment reference: "
                        + paymentReference + error.getMessage(), Toast.LENGTH_LONG).show();*/
                Snackbar snackbar =
                        Snackbar.make(main_content, "Transaction not Successful! error: " + error.getMessage(), Snackbar.LENGTH_INDEFINITE).
                                setAction("Dismiss", new View.OnClickListener(){
                                    @Override
                                    public void onClick(View view){}});

                snackbar.setActionTextColor(Color.WHITE);
                View snackbarView = snackbar.getView();
                snackbarView.setBackgroundColor(getResources().getColor(R.color.purple_800));
                snackbar.show();
            }

        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.rave:
                Toast.makeText(MainActivity.this, "Rave Payment Gateway", Toast.LENGTH_LONG).show();
                Intent startRave = new Intent(this, RaveActivity.class);
                this.startActivity(startRave);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


}

