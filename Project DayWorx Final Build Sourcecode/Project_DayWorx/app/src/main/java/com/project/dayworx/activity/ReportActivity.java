package com.project.dayworx.activity;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.reflect.TypeToken;
import com.project.dayworx.R;
import com.project.dayworx.adapter.DayTimeAdapter;
import com.project.dayworx.database.DBAdapter;
import com.project.dayworx.model.DayTimeModel;
import com.project.dayworx.network.DataRequest;
import com.project.dayworx.network.IWebService;
import com.project.dayworx.util.CustomTimePickerDialog;
import com.project.dayworx.util.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.IllegalFormatWidthException;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by ubuntu on 1/9/17.
 */

public class ReportActivity extends AppCompatActivity {

    private static final String TAG = ReportActivity.class.getSimpleName();

    @BindView(R.id.edTxtSiteAddress)
    TextView edTxtSiteAddress;

    @BindView(R.id.edTxtProjectName)
    EditText edTxtProjectName;

    @BindView(R.id.edTxtOrderNo)
    EditText edTxtOrderNo;

    @BindView(R.id.edtTxtWorkRequired)
    EditText edtTxtWorkRequired;

    @BindView(R.id.edtTxtWorkSpecificArea)
    EditText edtTxtWorkSpecificArea;

    @BindView(R.id.edtTxtRepairingDamages)
    EditText edtTxtRepairingDamages;

    @BindView(R.id.edtTxtMaterialUsed)
    EditText edtTxtMaterialUsed;

    @BindView(R.id.txtViewDay)
    TextView txtViewDay;

    @BindView(R.id.txtViewDate)
    TextView txtViewDate;

    @BindView(R.id.txtViewHrs)
    TextView txtViewHrs;

    @BindView(R.id.txtViewMinutes)
    TextView txtViewMinutes;

    @BindView(R.id.totalWorkingHours)
    TextView totalWorkingHours;

    @BindView(R.id.btnAddAnotherDay)
    LinearLayout btnAddAnotherDay;

//    @BindView(R.id.lytAddView)
//    LinearLayout lytAddView;

    @BindView(R.id.recycleView)
    RecyclerView recyclerView;

    @BindView(R.id.rlvGlobalProgressbar)
    RelativeLayout rlvGlobalProgressbar;

    @BindView(R.id.imgViewUser)
    ImageView imgViewUser;

    @BindView(R.id.spinnerTrade)
    Spinner spinnerTrade;

    @BindView(R.id.btnNext)
    Button btnNext;


    private ArrayList<DayTimeModel> dayTimeArrayList = new ArrayList<>();
    private DayTimeAdapter dayTimeAdapter;

    ArrayList<String> tradeList;

    private static String REPORT_ID;
    private static Boolean isReportDraft = false;

    private static final String PLACES_API_BASE = "https://maps.googleapis.com/maps/api/place";
    private static final String TYPE_AUTOCOMPLETE = "/autocomplete";
    private static final String OUT_JSON = "/json";

    private static final String API_KEY = "AIzaSyDWP_rTb4brf1BLXK0VKazZMZ62AGYwfaY";


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report);
        ButterKnife.bind(this);

        String userImageUrl = DBAdapter.getMapKeyValueString(ReportActivity.this, IWebService.KEY_RES_PROFILE_PIC);
        Log.d(TAG, "userImageUrl ==> " + userImageUrl);

        Glide.with(ReportActivity.this).load(userImageUrl).placeholder(getResources().getDrawable(R.drawable.placeholder)).into(imgViewUser);

        dayTimeAdapter = new DayTimeAdapter(this, dayTimeArrayList, recyclerView, ReportActivity.this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        recyclerView.setAdapter(dayTimeAdapter);

        loadData();

        if (getIntent() != null) {
            REPORT_ID = getIntent().getStringExtra(IWebService.KEY_RES_REPORT_ID);
            isReportDraft = getIntent().getBooleanExtra(IWebService.KEY_CONSTANT_DRAFT, false);
            if (isReportDraft) {
                loadDraftData(REPORT_ID);
                btnNext.setText("UPDATE");
            } else {
                btnNext.setText("NEXT");
            }
        }


        btnAddAnotherDay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (dayTimeArrayList.size() < 7) {
                    openDatePicker(-1);
                } else {
                    Utils.showAlert(ReportActivity.this, "", "Working week maximum of 7 days");
                }
            }
        });

//        edTxtSiteAddress.setAdapter(new GooglePlacesAutocompleteAdapter(this, R.layout.list_item));
//
//        edTxtSiteAddress.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            @Override
//            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
//                String str = (String) adapterView.getItemAtPosition(i);
//                edTxtSiteAddress.setText(str);
//            }
//        });

//        getCurrentDate();

    }

    private boolean isValidData() {
        boolean isValid = true;

        edTxtSiteAddress.setError(null);
        edTxtProjectName.setError(null);
        edTxtOrderNo.setError(null);
        edtTxtWorkRequired.setError(null);
        edtTxtWorkSpecificArea.setError(null);
        edtTxtRepairingDamages.setError(null);
        edtTxtMaterialUsed.setError(null);

        if (Utils.isNullOrEmpty(edTxtSiteAddress.getText().toString())) {
            edTxtSiteAddress.setError(getString(R.string.error_required));
            edTxtSiteAddress.requestFocus();
            isValid = false;
        } else if (Utils.isNullOrEmpty(edTxtProjectName.getText().toString())) {
            edTxtProjectName.setError(getString(R.string.error_required));
            edTxtProjectName.requestFocus();
            isValid = false;
        } else if (spinnerTrade.getSelectedItem().toString().equalsIgnoreCase("Select Trade")) {
            Utils.showAlert(this, "", "Please select trade");
            spinnerTrade.requestFocus();
            isValid = false;
        } else if (Utils.isNullOrEmpty(edtTxtWorkRequired.getText().toString())) {
            edtTxtWorkRequired.setError(getString(R.string.error_required));
            edtTxtWorkRequired.requestFocus();
            isValid = false;
        } else if (Utils.isNullOrEmpty(edtTxtWorkSpecificArea.getText().toString())) {
            edtTxtWorkSpecificArea.setError(getString(R.string.error_required));
            edtTxtWorkSpecificArea.requestFocus();
            isValid = false;
        } else if (dayTimeArrayList.isEmpty()) {
            Utils.showAlert(ReportActivity.this, "", "Please add working time");
            isValid = false;
        }


        return isValid;
    }

    //    @OnClick(R.id.btnNext)
    public void switchToGallerySelectionActivity() {
        Intent intent = new Intent(ReportActivity.this, GalleryActivity.class);
        intent.putExtra(IWebService.KEY_RES_REPORT_ID, REPORT_ID);
        intent.putExtra(IWebService.KEY_CONSTANT_DRAFT, isReportDraft);
        startActivity(intent);
        overridePendingTransition(R.anim.enter, R.anim.exit);
        finish();
    }

    @OnClick(R.id.btnSetting)
    public void switchToSettingActivity() {
        Intent intent = new Intent(ReportActivity.this, MenuActivity.class);
        startActivity(intent);
        overridePendingTransition(R.anim.enter, R.anim.exit);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.left_to_right, R.anim.right_to_left);
    }

    private void getCurrentDate() {
//        dateFormater(new Date());
    }

    private void dateFormater(Date d, int position) {
        boolean isDateAlreadySelected = false;
        SimpleDateFormat format = new SimpleDateFormat("d");
        String date = format.format(d);

        if (date.endsWith("1") && !date.endsWith("11"))
            format = new SimpleDateFormat("d'st' MMM, yyyy");
        else if (date.endsWith("2") && !date.endsWith("12"))
            format = new SimpleDateFormat("d'nd' MMM, yyyy");
        else if (date.endsWith("3") && !date.endsWith("13"))
            format = new SimpleDateFormat("d'rd' MMM, yyyy");
        else
            format = new SimpleDateFormat("d'th' MMM, yyyy");

        String yourDate = format.format(d);

        SimpleDateFormat dayOfWeekFormat = new SimpleDateFormat("EE");
        String day = dayOfWeekFormat.format(d);

        Log.d(TAG, "CurrentDate ==> " + yourDate);


        String selectedDate = day + ", " + yourDate;
        Log.d(TAG, "selectedDate ==> " + selectedDate);
        if (position < 0) {

            for (int i = 0; i < dayTimeArrayList.size(); i++) {

                if (dayTimeArrayList.get(i).getDay().contains(selectedDate)) {
                    isDateAlreadySelected = true;
                    break;
                }


            }
        }
        if (isDateAlreadySelected) {
            Toast.makeText(this, "Already hours have been added for the selected date", Toast.LENGTH_SHORT).show();
        } else {
            isDateAlreadySelected = false;
            showCustomTimePicker(selectedDate, position);
        }

//        txtViewDate.setText(yourDate);
//        txtViewDay.setText(day);
//        textView.setText(day + ", " + yourDate);
    }

    //    @OnClick(R.id.btnAddAnotherDay)
    public void openDatePicker(final int position) {

        Log.d(TAG, "position ==> " + position);

        Calendar newCalendar = Calendar.getInstance();
        DatePickerDialog datePicker = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {

            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                Calendar newDate = Calendar.getInstance();
                newDate.set(year, monthOfYear, dayOfMonth);
                dateFormater(newDate.getTime(), position);
            }

        }, newCalendar.get(Calendar.YEAR), newCalendar.get(Calendar.MONTH), newCalendar.get(Calendar.DAY_OF_MONTH));

        datePicker.show();
    }

    //    @OnClick(R.id.timePicker)
    public void showCustomTimePicker(final String date, final int position) {
        CustomTimePickerDialog customTimePickerDialog = new CustomTimePickerDialog(this, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker timePicker, int i, int i1) {
                Log.d(TAG, "hrs ==> " + i);
                Log.d(TAG, "minute ==> " + i1);

//                txtViewHrs.setText("" + i + " Hrs");
//                txtViewMinutes.setText("" + i1 + " Minutes");
//                txtView.setText(i + " Hrs : " + i1 + " Minutes");

                int totalMinutes = (i * 60) + i1;

                String hrs = new DecimalFormat("00").format(i);
                String min = new DecimalFormat("00").format(i1);
                String time = hrs + " Hrs : " + min + " Mins";

                DayTimeModel dayTimeModel = new DayTimeModel();
                dayTimeModel.setDay(date);
                dayTimeModel.setTime(time);
                dayTimeModel.setTotal_minutes(String.valueOf(totalMinutes));

                if (position >= 0) {
                    dayTimeArrayList.set(position, dayTimeModel);

                } else {
                    dayTimeArrayList.add(dayTimeModel);
                }


                dayTimeAdapter.notifyDataSetChanged();
                setTotalTime();

            }
        }, 0, 0, true);
        customTimePickerDialog.show();


    }

    private void setTotalTime() {
        if (dayTimeArrayList.size() > 0) {
            int totalMinutes = 0;
            for (int i = 0; i < dayTimeArrayList.size(); i++) {
                totalMinutes = totalMinutes + Integer.parseInt(dayTimeArrayList.get(i).getTotal_minutes());
            }
            int hours = totalMinutes / 60; //since both are ints, you get an int
            int minutes = totalMinutes % 60;


            String hrs = new DecimalFormat("00").format(hours);
            String min = new DecimalFormat("00").format(minutes);

            Log.d(TAG, "totalHours ==> " + hrs);
            Log.d(TAG, "totalMinutes ==> " + min);
            totalWorkingHours.setText(hrs + " : " + min);
        }
    }


    @OnClick(R.id.btnNext)
    public void sendReport() {
        if (isValidData()) {
            String siteAddress = edTxtSiteAddress.getText().toString().trim();
            String projectName = edTxtProjectName.getText().toString().trim();
            String orderNo = edTxtOrderNo.getText().toString().trim();
            String workRequired = edtTxtWorkRequired.getText().toString().trim();
            String workSpecificArea = edtTxtWorkSpecificArea.getText().toString().trim();
            String repairingDamage = edtTxtRepairingDamages.getText().toString().trim();
            String materialUsed = edtTxtMaterialUsed.getText().toString().trim();
            String totalTime = totalWorkingHours.getText().toString().trim();

            JSONObject jsonObjectParams = new JSONObject();
            try {
                if (isReportDraft) {
                    jsonObjectParams.put(IWebService.KEY_REQ_ACTION, IWebService.KEY_ACTION_REPORT_UPDATE);
                    jsonObjectParams.put(IWebService.KEY_RES_REPORT_ID, REPORT_ID);
                } else {
                    jsonObjectParams.put(IWebService.KEY_REQ_ACTION, IWebService.KEY_ACTION_CREATE_REPORT);
                }
                jsonObjectParams.put(IWebService.KEY_USER_ID, DBAdapter.getMapKeyValueString(ReportActivity.this, IWebService.KEY_USER_ID));
                jsonObjectParams.put(IWebService.KEY_REQ_PAGE_NO, "1");
                jsonObjectParams.put(IWebService.KEY_REQ_SITE_ADDRESS, siteAddress);
                jsonObjectParams.put(IWebService.KEY_REQ_PROJECT_NAME, projectName);
                jsonObjectParams.put(IWebService.KEY_REQ_TRADE, spinnerTrade.getSelectedItem().toString());
                jsonObjectParams.put(IWebService.KEY_REQ_ORDER_NUMBER, orderNo);
                jsonObjectParams.put(IWebService.KEY_REQ_WORK_REQUIRED, workRequired);
                jsonObjectParams.put(IWebService.KEY_REQ_WORK_SPECIFIC_AREA, workSpecificArea);
                jsonObjectParams.put(IWebService.KEY_REQ_REPAIRING_DAMAGE, repairingDamage);
                jsonObjectParams.put(IWebService.KEY_REQ_MATERIAL_USED, materialUsed);
                jsonObjectParams.put(IWebService.KEY_REQ_WORKING_HOURS, getJsonArrayWorkTime());
                jsonObjectParams.put(IWebService.KEY_REQ_TOTAL_HOURS, totalTime);

            } catch (JSONException e) {
                e.printStackTrace();
            }

            DataRequest sendReportDataRequest = new DataRequest(ReportActivity.this);
            sendReportDataRequest.execute(IWebService.MAIN_URL, jsonObjectParams.toString(), new DataRequest.CallBack() {
                @Override
                public void onPreExecute() {
                    rlvGlobalProgressbar.setVisibility(View.VISIBLE);
                }

                @Override
                public void onPostExecute(String response) {

                    rlvGlobalProgressbar.setVisibility(View.GONE);
                    if (!DataRequest.hasError(ReportActivity.this, response, true)) {
                        Log.d(TAG, "response ==> " + response);

                        JSONObject jsonObject = DataRequest.getJObjWebdata(response);
                        try {
                            REPORT_ID = jsonObject.getString(IWebService.KEY_RES_REPORT_ID);
                            switchToGallerySelectionActivity();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
        }
    }

    public JSONArray getJsonArrayWorkTime() {
        JSONArray jsonArrayTime = new JSONArray();
        for (int i = 0; i < dayTimeArrayList.size(); i++) {
            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put(IWebService.KEY_REQ_DAY, dayTimeArrayList.get(i).getDay());
                jsonObject.put(IWebService.KEY_REQ_TIME, dayTimeArrayList.get(i).getTime());
                String totalMinutes = String.valueOf(dayTimeArrayList.get(i).getTotal_minutes());
                jsonObject.put(IWebService.KEY_REQ_TOTAL_MINUTES, totalMinutes);
                jsonArrayTime.put(jsonObject);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return jsonArrayTime;
    }

    private void loadData() {
        tradeList = new ArrayList<>();
        tradeList.add("Select Trade");
        tradeList.add("Acoustic panelling");
        tradeList.add("Aluminium joinery");
        tradeList.add("Appliances");
        tradeList.add("Asbestos removal");
        tradeList.add("Automatic doors");
        tradeList.add("Block laying");
        tradeList.add("Brick laying");
        tradeList.add("Cabinet installation");
        tradeList.add("Cabinet making");
        tradeList.add("Carpentry");
        tradeList.add("Ceiling grid");
        tradeList.add("Civil works");
        tradeList.add("Cleaning");
        tradeList.add("Concrete cutting");
        tradeList.add("Concrete repairs");
        tradeList.add("Concrete work");
        tradeList.add("Crane driving");
        tradeList.add("Data");
        tradeList.add("Demolition");
        tradeList.add("Dogman");
        tradeList.add("Drainage");
        tradeList.add("Drywall fixing");
        tradeList.add("Electrical");
        tradeList.add("Excavation");
        tradeList.add("Fabrication");
        tradeList.add("Fencing");
        tradeList.add("Fire protection");
        tradeList.add("Flashings");
        tradeList.add("Flooring");
        tradeList.add("Gas");
        tradeList.add("Gates");
        tradeList.add("Glazing");
        tradeList.add("Hardware");
        tradeList.add("HVAC");
        tradeList.add("Insulation");
        tradeList.add("Labouring");
        tradeList.add("Land surveying");
        tradeList.add("Landscaping");
        tradeList.add("Lifts");
        tradeList.add("Line marking");
        tradeList.add("Metalwork");
        tradeList.add("OTHER");
        tradeList.add("Painting");
        tradeList.add("Passive fire");
        tradeList.add("Paving");
        tradeList.add("Piling");
        tradeList.add("Plumbing");
        tradeList.add("Pool");
        tradeList.add("Precast concrete");
        tradeList.add("Precast panel joints");
        tradeList.add("Reinforcing steel");
        tradeList.add("Roller shutter doors");
        tradeList.add("Roofing");
        tradeList.add("Scaffolding");
        tradeList.add("Security");
        tradeList.add("Sheet metalwork");
        tradeList.add("Signage");
        tradeList.add("Skylights");
        tradeList.add("Solid plastering");
        tradeList.add("Specialist cladding");
        tradeList.add("Stone masonry");
        tradeList.add("Stopping");
        tradeList.add("Structural steel");
        tradeList.add("Tanking");
        tradeList.add("Tiling");
        tradeList.add("Timber joinery");
        tradeList.add("Toilet partitions");
        tradeList.add("Travelator");
        tradeList.add("Wallpaper hanging");
        tradeList.add("Wardrobes");
        tradeList.add("Waterproofing");


        ArrayAdapter<String> adapter = new ArrayAdapter<String>(ReportActivity.this, R.layout.spinner_style, tradeList) {

            public View getView(int position, View convertView, ViewGroup parent) {

                View v = super.getView(position, convertView, parent);

                ((TextView) v).setTextSize(16);

                return v;

            }

            public View getDropDownView(int position, View convertView, ViewGroup parent) {

                View v = super.getDropDownView(position, convertView, parent);
                return v;

            }

        };
        spinnerTrade.setAdapter(adapter);
    }


//    public static ArrayList<String> autocomplete(String input) {
//        ArrayList<String> resultList = null;
//
//        HttpURLConnection conn = null;
//        StringBuilder jsonResults = new StringBuilder();
//        try {
//            StringBuilder sb = new StringBuilder(PLACES_API_BASE + TYPE_AUTOCOMPLETE + OUT_JSON);
//            sb.append("?key=" + API_KEY);
//            sb.append("&components=country:gr");
//            sb.append("&input=" + URLEncoder.encode(input, "utf8"));
//
//            URL url = new URL(sb.toString());
//
//            System.out.println("URL: " + url);
//            conn = (HttpURLConnection) url.openConnection();
//            InputStreamReader in = new InputStreamReader(conn.getInputStream());
//
//            // Load the results into a StringBuilder
//            int read;
//            char[] buff = new char[1024];
//            while ((read = in.read(buff)) != -1) {
//                jsonResults.append(buff, 0, read);
//            }
//        } catch (MalformedURLException e) {
//            Log.e(TAG, "Error processing Places API URL", e);
//            return resultList;
//        } catch (IOException e) {
//            Log.e(TAG, "Error connecting to Places API", e);
//            return resultList;
//        } finally {
//            if (conn != null) {
//                conn.disconnect();
//            }
//        }
//
//        try {
//
//            // Create a JSON object hierarchy from the results
//            JSONObject jsonObj = new JSONObject(jsonResults.toString());
//            JSONArray predsJsonArray = jsonObj.getJSONArray("predictions");
//
//            // Extract the Place descriptions from the results
//            resultList = new ArrayList<String>(predsJsonArray.length());
//            for (int i = 0; i < predsJsonArray.length(); i++) {
//                System.out.println(predsJsonArray.getJSONObject(i).getString("description"));
//                System.out.println("============================================================");
//                resultList.add(predsJsonArray.getJSONObject(i).getString("description"));
//            }
//        } catch (JSONException e) {
//            Log.e(TAG, "Cannot process JSON results", e);
//        }
//
//        return resultList;
//    }

    @OnClick(R.id.edTxtSiteAddress)
    public void findPlace() {
        try {
            Intent intent =
                    new PlaceAutocomplete
                            .IntentBuilder(PlaceAutocomplete.MODE_OVERLAY)
                            .build(ReportActivity.this);
            startActivityForResult(intent, 1);
        } catch (GooglePlayServicesRepairableException e) {
            Log.d(TAG, "Error_1 ==> ");
            e.printStackTrace();
        } catch (GooglePlayServicesNotAvailableException e) {
            Log.d(TAG, "Error_2 ==> ");
            e.printStackTrace();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1) {
            if (resultCode == RESULT_OK) {
                // retrive the data by using getPlace() method.
                Place place = PlaceAutocomplete.getPlace(this, data);
                Log.e("Tag", "Place: " + place.getAddress() + place.getPhoneNumber());

//                edTxtSiteAddress
//                        .setText(place.getName() + "," +
//                                place.getAddress());
                edTxtSiteAddress.setText(place.getAddress());

            } else if (resultCode == PlaceAutocomplete.RESULT_ERROR) {
                Status status = PlaceAutocomplete.getStatus(this, data);
                // TODO: Handle the error.
                Log.e("Tag", status.getStatusMessage());

            } else if (resultCode == RESULT_CANCELED) {
                // The user canceled the operation.
            }
        }
    }

//    class GooglePlacesAutocompleteAdapter extends ArrayAdapter<String> implements Filterable {
//        private ArrayList<String> resultList;
//
//        public GooglePlacesAutocompleteAdapter(Context context, int textViewResourceId) {
//            super(context, textViewResourceId);
//        }
//
//        @Override
//        public int getCount() {
//            return resultList.size();
//        }
//
//        @Override
//        public String getItem(int index) {
//            return resultList.get(index);
//        }
//
//        @Override
//        public Filter getFilter() {
//            Filter filter = new Filter() {
//                @Override
//                protected FilterResults performFiltering(CharSequence constraint) {
//                    FilterResults filterResults = new FilterResults();
//                    if (constraint != null) {
//                        // Retrieve the autocomplete results.
//                        resultList = autocomplete(constraint.toString());
//
//                        // Assign the data to the FilterResults
//                        filterResults.values = resultList;
//                        filterResults.count = resultList.size();
//                    }
//                    return filterResults;
//                }
//
//                @Override
//                protected void publishResults(CharSequence constraint, FilterResults results) {
//                    if (results != null && results.count > 0) {
//                        notifyDataSetChanged();
//                    } else {
//                        notifyDataSetInvalidated();
//                    }
//                }
//            };
//            return filter;
//        }
//    }

    private void loadDraftData(String reportId) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put(IWebService.KEY_REQ_ACTION, IWebService.KEY_ACTION_REPORT_DRAFT);
            jsonObject.put(IWebService.KEY_USER_ID, DBAdapter.getMapKeyValueString(ReportActivity.this, IWebService.KEY_USER_ID));
            jsonObject.put(IWebService.KEY_REQ_PAGE_NO, "1");
            jsonObject.put(IWebService.KEY_RES_REPORT_ID, reportId);

            DataRequest dataRequest = new DataRequest(ReportActivity.this);
            dataRequest.execute(IWebService.MAIN_URL, jsonObject.toString(), new DataRequest.CallBack() {
                @Override
                public void onPreExecute() {
                    rlvGlobalProgressbar.setVisibility(View.VISIBLE);
                }

                @Override
                public void onPostExecute(String response) {
                    rlvGlobalProgressbar.setVisibility(View.GONE);

                    if (!DataRequest.hasError(ReportActivity.this, response, true)) {
                        Log.d(TAG, "response ==> " + response);

                        JSONObject jsonObject = DataRequest.getJObjWebdata(response);
                        try {
                            Log.d(TAG, "draftData ==> " + jsonObject.toString());
                            JSONArray jsonArray = jsonObject.getJSONArray("report_draft");
                            JSONObject jsonObjectDraft = jsonArray.getJSONObject(0);

                            loadData(jsonObjectDraft);

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            });

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void loadData(JSONObject jsonObject) {
        try {
            edTxtSiteAddress.setText(jsonObject.getString(IWebService.KEY_REQ_SITE_ADDRESS));
            edTxtProjectName.setText(jsonObject.getString(IWebService.KEY_REQ_PROJECT_NAME));
            String spinnerText = jsonObject.getString(IWebService.KEY_REQ_TRADE);

            int position = 0;
            for (int i = 0; i < tradeList.size(); i++) {
                if (tradeList.get(i).equalsIgnoreCase(spinnerText)) {
                    position = i;
                    break;
                }
            }
            spinnerTrade.setSelection(position);

            edTxtOrderNo.setText(jsonObject.getString(IWebService.KEY_REQ_ORDER_NUMBER));
            edtTxtWorkRequired.setText(jsonObject.getString(IWebService.KEY_REQ_WORK_REQUIRED));
            edtTxtWorkSpecificArea.setText(jsonObject.getString(IWebService.KEY_REQ_WORK_SPECIFIC_AREA));
            edtTxtRepairingDamages.setText(jsonObject.getString(IWebService.KEY_REQ_REPAIRING_DAMAGE));
            edtTxtMaterialUsed.setText(jsonObject.getString(IWebService.KEY_REQ_MATERIAL_USED));
            totalWorkingHours.setText(jsonObject.getString(IWebService.KEY_REQ_TOTAL_HOURS));

            Gson gson = new Gson();

            ArrayList<DayTimeModel> tmpDayTimeList = gson.fromJson(
                    jsonObject.getJSONArray(
                            IWebService.KEY_REQ_WORKING_HOURS).toString(),
                    new TypeToken<ArrayList<DayTimeModel>>() {
                    }.getType());
            if (tmpDayTimeList != null) {
                dayTimeArrayList.clear();
                dayTimeArrayList.addAll(tmpDayTimeList);
                dayTimeAdapter.notifyDataSetChanged();

                setTotalTime();

            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
