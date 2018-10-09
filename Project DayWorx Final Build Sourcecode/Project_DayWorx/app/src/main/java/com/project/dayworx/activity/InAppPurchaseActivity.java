package com.project.dayworx.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.hootsuite.nachos.ChipConfiguration;
import com.hootsuite.nachos.NachoTextView;
import com.hootsuite.nachos.chip.Chip;
import com.hootsuite.nachos.chip.ChipSpan;
import com.hootsuite.nachos.chip.ChipSpanChipCreator;
import com.hootsuite.nachos.terminator.ChipTerminatorHandler;
import com.hootsuite.nachos.tokenizer.SpanChipTokenizer;
import com.hootsuite.nachos.validator.ChipifyingNachoValidator;

import com.project.dayworx.R;
import com.project.dayworx.database.DBAdapter;
import com.project.dayworx.inapp.IInAppConfig;
import com.project.dayworx.inapp.IabHelper;
import com.project.dayworx.inapp.IabResult;
import com.project.dayworx.inapp.Inventory;
import com.project.dayworx.inapp.Purchase;
import com.project.dayworx.network.DataRequest;
import com.project.dayworx.network.IWebService;
import com.project.dayworx.util.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by ubuntu on 5/3/18.
 */

public class InAppPurchaseActivity extends AppCompatActivity {

    private static final String TAG = InAppPurchaseActivity.class.getSimpleName();

    private static String[] SUGGESTIONS = new String[]{"Nachos", "Chip", "Tortilla Chips", "Melted Cheese", "Salsa", "Guacamole", "Cheddar", "Mozzarella", "Mexico", "Jalapeno"};

    @BindView(R.id.btnBuy)
    Button btnBuy;

    @BindView(R.id.btnBack)
    ImageView btnBack;

    @BindView(R.id.nacho_text_view)
    NachoTextView mNachoTextView;

    @BindView(R.id.rlvGlobalProgressbar)
    RelativeLayout rlvGlobalProgressbar;

    private static String REPORT_ID;

    // The helper object
    IabHelper mHelper;

    // Listener that's called when we finish querying the items and subscriptions we own
    IabHelper.QueryInventoryFinishedListener mGotInventoryListener = new IabHelper.QueryInventoryFinishedListener() {
        public void onQueryInventoryFinished(IabResult result, Inventory inventory) {
            Log.d(TAG, "Query inventory finished.");

            // Have we been disposed of in the meantime? If so, quit.
            if (mHelper == null) return;

            // Is it a failure?
            if (result.isFailure()) {
                Utils.showAlert(InAppPurchaseActivity.this, "Error : ", "Failed to query inventory: " + result);
                return;
            }

            Log.d(TAG, "Query inventory was successful.");

            /*
             * Check for items we own. Notice that for each purchase, we check
             * the developer payload to see if it's correct! See
             * verifyDeveloperPayload().
             */

            // Do we have the premium upgrade?
            Purchase premiumPurchase = inventory.getPurchase(IInAppConfig.SKU_PREMIUM);
            Boolean mIsPremium = (premiumPurchase != null && verifyDeveloperPayload(premiumPurchase));
            Log.d(TAG, "User is " + (mIsPremium ? "PREMIUM" : "NOT PREMIUM"));
            if (mIsPremium) {
                mHelper.consumeAsync(inventory.getPurchase(IInAppConfig.SKU_PREMIUM), mConsumeFinishedListener);
//                Utils.setPreferenceBoolean(SocialShareApplication.this, IPreference.isPremium, mIsPremium);
            }

        }
    };

    IabHelper.OnConsumeFinishedListener mConsumeFinishedListener =
            new IabHelper.OnConsumeFinishedListener() {
                public void onConsumeFinished(Purchase purchase, IabResult result) {
                    if (result.isSuccess()) {
                        // provision the in-app purchase to the user
                        // (for example, credit 50 gold coins to player's character)
                    } else {
                        // handle error
                    }
                }
            };

    /**
     * Verifies the developer payload of a purchase.
     */
    public static boolean verifyDeveloperPayload(Purchase p) {
        String payload = p.getDeveloperPayload();

        /*
         * TODO: verify that the developer payload of the purchase is correct. It will be
         * the same one that you sent when initiating the purchase.
         *
         * WARNING: Locally generating a random string when starting a purchase and
         * verifying it here might seem like a good approach, but this will fail in the
         * case where the user purchases an item on one device and then uses your app on
         * a different device, because on the other device you will not have access to the
         * random string you originally generated.
         *
         * So a good developer payload has these characteristics:
         *
         * 1. If two different users purchase an item, the payload is different between them,
         *    so that one user's purchase can't be replayed to another user.
         *
         * 2. The payload must be such that you can verify it even when the app wasn't the
         *    one who initiated the purchase flow (so that items purchased by the user on
         *    one device work on other devices owned by the user).
         *
         * Using your own server to store and verify developer payloads across app
         * installations is recommended.
         */

        return true;
    }

    // Callback for when a purchase is finished
    IabHelper.OnIabPurchaseFinishedListener mPurchaseFinishedListener = new IabHelper.OnIabPurchaseFinishedListener() {
        public void onIabPurchaseFinished(IabResult result, Purchase purchase) {
            Log.d(TAG, "Purchase finished: " + result + ", purchase: " + purchase);

            // if we were disposed of in the meantime, quit.
            if (mHelper == null) return;

            if (result.isFailure()) {
                Utils.showAlert(InAppPurchaseActivity.this, getString(R.string.error_title), "Error purchasing: " + result);
                return;
            }
            if (!verifyDeveloperPayload(purchase)) {
                Utils.showAlert(InAppPurchaseActivity.this, getString(R.string.error_title), "Error purchasing. Authenticity verification failed.");
                return;
            }

            Log.d(TAG, "Purchase successful.");


            if (purchase.getSku().equals(IInAppConfig.SKU_PREMIUM)) {
                // bought the premium upgrade!
                Log.d(TAG, "Purchase is premium upgrade. Congratulating user.");
                mHelper.consumeAsync(purchase, mConsumeFinishedListener);
//                Utils.showAlert(InAppPurchaseActivity.this, "Upgrade : ", "Thank you for upgrading to premium!");
            }

            sendReportBuyVerification();
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);
        ButterKnife.bind(this);

        mHelper = getIabInstance();

        setupChipTextView(mNachoTextView);

        if (getIntent() != null) {
            REPORT_ID = getIntent().getStringExtra(IWebService.KEY_RES_REPORT_ID);
            Log.d(TAG, "REPORT_ID ==> " + REPORT_ID);
        }

        btnBuy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mNachoTextView.getText().toString().isEmpty()) {
                    mNachoTextView.setError(getString(R.string.error_required));
                    mNachoTextView.requestFocus();
                } else if (!isValidEmailId()) {
                    Utils.showAlert(InAppPurchaseActivity.this, "", "Please enter valid email address");
                } else {
                    getPremium();
                }
            }
        });

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

    }

    //    @OnClick(R.id.btnBuy)
    public void getPremium() {

        mHelper = getIabInstance();
        if (mHelper != null) mHelper.flagEndAsync();
                        /* TODO: for security, generate your payload here for verification. See the comments on
         *        verifyDeveloperPayload() for more info. Since this is a SAMPLE, we just use
         *        an empty string, but on a production app you should carefully generate this. */
        String payload = "";


        mHelper.launchPurchaseFlow(InAppPurchaseActivity.this, IInAppConfig.SKU_PREMIUM, IInAppConfig.RC_REQUEST,
                mPurchaseFinishedListener, payload);


    }


    public IabHelper getIabInstance() {
        if (mHelper == null) {
            // Create the helper, passing it our context and the public key to verify signatures with
            Log.d(TAG, "Creating IAB helper.");
            mHelper = new IabHelper(this, IInAppConfig.base64EncodedPublicKey);

            // enable debug logging (for a production application, you should set this to false).
            mHelper.enableDebugLogging(true);

            // Start setup. This is asynchronous and the specified listener
            // will be called once setup completes.
            Log.d(TAG, "Starting setup.");
            mHelper.startSetup(new IabHelper.OnIabSetupFinishedListener() {
                public void onIabSetupFinished(IabResult result) {
                    Log.d(TAG, "Setup finished.");

                    if (!result.isSuccess()) {
                        // Oh noes, there was a problem.
                        Utils.showAlert(InAppPurchaseActivity.this, getString(R.string.error_title), "Problem setting up in-app billing : " + result);
                        return;
                    }

                    // Have we been disposed of in the meantime? If so, quit.
                    if (mHelper == null) return;

                    // IAB is fully set up. Now, let's get an inventory of stuff we own.
                    Log.d(TAG, "Setup successful. Querying inventory.");
//                mHelper.queryInventoryAsync(mGotInventoryListener);

                    List<String> skulist = new ArrayList<String>();
                    skulist.add(IInAppConfig.SKU_PREMIUM);
//                    try {
//                        mHelper.queryInventoryAsync(mGotInventoryListener);
//                    } catch (Exception e) {
//                        e.printStackTrace();
//
//                    }
//                    mHelper.queryInventoryAsync(mGotInventoryListener);
                    mHelper.queryInventoryAsync(true, skulist, mGotInventoryListener);
                }
            });
        }
        return mHelper;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "onActivityResult(" + requestCode + "," + resultCode + "," + data);

        // Pass on the activity result to the helper for handling
        if (!mHelper.handleActivityResult(requestCode, resultCode, data)) {
            Log.d(TAG, "in if");
            // not handled, so handle it ourselves (here's where you'd
            // perform any handling of activity results not related to in-app
            // billing...
            super.onActivityResult(requestCode, resultCode, data);
        } else {
            Log.d(TAG, "onActivityResult handled by IABUtil.");
        }
    }

    private void setupChipTextView(NachoTextView nachoTextView) {
//        set the adapter for hint
//        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, SUGGESTIONS);
//        nachoTextView.setAdapter(adapter);
        nachoTextView.setIllegalCharacters('\"');
        nachoTextView.addChipTerminator('\n', ChipTerminatorHandler.BEHAVIOR_CHIPIFY_ALL);
        nachoTextView.addChipTerminator(' ', ChipTerminatorHandler.BEHAVIOR_CHIPIFY_TO_TERMINATOR);
        nachoTextView.addChipTerminator(';', ChipTerminatorHandler.BEHAVIOR_CHIPIFY_CURRENT_TOKEN);
        nachoTextView.setNachoValidator(new ChipifyingNachoValidator());
        nachoTextView.enableEditChipOnTouch(true, true);
        nachoTextView.setOnChipClickListener(new NachoTextView.OnChipClickListener() {
            @Override
            public void onChipClick(Chip chip, MotionEvent motionEvent) {
                Log.d(TAG, "onChipClick: " + chip.getText());
            }
        });
        nachoTextView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
    }


    private boolean isValidEmailId() {

        boolean isValid = false;

        List<String> chipValues = mNachoTextView.getChipValues();

        for (int i = 0; i < chipValues.size(); i++) {
            if (Utils.isValidEmail(chipValues.get(i))) {
                isValid = true;
            } else {
                isValid = false;
                break;
            }
        }
        return isValid;
    }

    public void sendReportBuyVerification() {


        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put(IWebService.KEY_REQ_ACTION, IWebService.KEY_ACTION_REPORT_PURCHASE);
            jsonObject.put(IWebService.KEY_USER_ID, DBAdapter.getMapKeyValueString(InAppPurchaseActivity.this, IWebService.KEY_RES_USER_ID));
            jsonObject.put(IWebService.KEY_RES_REPORT_ID, REPORT_ID); //Please replce with variable REPORT_ID

            JSONArray jsonArray = new JSONArray();
            List<String> chipValues = mNachoTextView.getChipValues();

            for (int i = 0; i < chipValues.size(); i++) {
                jsonArray.put(chipValues.get(i));
            }
            jsonObject.put(IWebService.KEY_REQ_EMAIL_ID, jsonArray);


            DataRequest dataRequestVerfication = new DataRequest(InAppPurchaseActivity.this);
            dataRequestVerfication.execute(IWebService.MAIN_URL, jsonObject.toString(), new DataRequest.CallBack() {
                @Override
                public void onPreExecute() {
                    rlvGlobalProgressbar.setVisibility(View.VISIBLE);

                }

                @Override
                public void onPostExecute(String response) {
                    rlvGlobalProgressbar.setVisibility(View.GONE);
                    if (!DataRequest.hasError(InAppPurchaseActivity.this, response, true)) {
                        Log.d(TAG, "response ==> " + response);
                        switchToReportActivity();
//                        JSONObject jsonObj = DataRequest.getJObjWebdata(response);
//                        try {
//                            String url = jsonObj.getString("html_path");
//                        } catch (JSONException e) {
//                            e.printStackTrace();
//                        }
                    }

                }
            });
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.left_to_right, R.anim.right_to_left);
        finish();
    }

    public void switchToReportActivity() {
//        Intent intent = new Intent(ReportVerificationActivity.this, GalleryActivity.class);
//        startActivity(intent);
//        Toast.makeText(this, "Report Saved", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(InAppPurchaseActivity.this, ReportActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        overridePendingTransition(R.anim.enter, R.anim.exit);
        finish();
    }

}
