package com.project.dayworx.activity;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.project.dayworx.R;
import com.project.dayworx.adapter.ReportHistoryAdapter;
import com.project.dayworx.database.DBAdapter;
import com.project.dayworx.model.ReportHistoryModel;
import com.project.dayworx.network.DataRequest;
import com.project.dayworx.network.IWebService;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.IllegalFormatWidthException;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static java.lang.String.CASE_INSENSITIVE_ORDER;

/**
 * Created by ubuntu on 22/12/17.
 */

public class ReportHistoryActivity extends AppCompatActivity {

    private static final String TAG = ReportHistoryActivity.class.getSimpleName();

    @BindView(R.id.rlvGlobalProgressbar)
    RelativeLayout rlvGlobalProgressbar;

    @BindView(R.id.recycleView)
    RecyclerView recycleView;

    @BindView(R.id.btnBack)
    ImageView btnBack;

    @BindView(R.id.btnSort)
    ImageView btnSort;

    private ArrayList<ReportHistoryModel> reportHistoryList = new ArrayList<>();
    private ReportHistoryAdapter reportHistoryAdapter;

    private boolean isListSorted = true;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report_history);
        ButterKnife.bind(this);

        reportHistoryAdapter = new ReportHistoryAdapter(this, reportHistoryList, recycleView, ReportHistoryActivity.this);
        recycleView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        recycleView.setAdapter(reportHistoryAdapter);


        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        getReportHistory();


        btnSort.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isListSorted) {
                    sortInAscending();
                } else {
                    sortInDescending();
                }
            }
        });
    }


    public void switchToShowPdfActivity(int position) {
        if (reportHistoryList.get(position).is_draft.equalsIgnoreCase("true")) {
            Intent intent = new Intent(ReportHistoryActivity.this, ReportActivity.class);
            intent.putExtra(IWebService.KEY_RES_REPORT_ID, reportHistoryList.get(position).report_id);
            intent.putExtra(IWebService.KEY_RES_REPORT_PURCHASED_STATUS, reportHistoryList.get(position).purchased_status);
            intent.putExtra(IWebService.KEY_CONSTANT_DRAFT, true);
            startActivity(intent);
            overridePendingTransition(R.anim.enter, R.anim.exit);
        } else {

            Intent intent = new Intent(ReportHistoryActivity.this, ShowPdfActivity.class);
            intent.putExtra("PDF_URL", reportHistoryList.get(position).html_path);
            intent.putExtra(IWebService.KEY_RES_REPORT_ID, reportHistoryList.get(position).report_id);
            intent.putExtra(IWebService.KEY_RES_REPORT_PURCHASED_STATUS, reportHistoryList.get(position).purchased_status);
            startActivity(intent);
            overridePendingTransition(R.anim.enter, R.anim.exit);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.left_to_right, R.anim.right_to_left);
        finish();
    }

    private void sortInDescending() {
//        Collections.reverse(reportHistoryList);
        Collections.sort(reportHistoryList, new Comparator<ReportHistoryModel>() {
            public int compare(ReportHistoryModel v1, ReportHistoryModel v2) {
                return v2.site_address.compareTo(v1.site_address);
            }
        });
        reportHistoryAdapter.notifyDataSetChanged();
        isListSorted = true;
        btnSort.setImageResource(R.drawable.downarrow);
    }

    private void sortInAscending() {
//        Collections.reverse(reportHistoryList);

        Collections.sort(reportHistoryList, new Comparator<ReportHistoryModel>() {
            public int compare(ReportHistoryModel v1, ReportHistoryModel v2) {
                return v1.site_address.compareTo(v2.site_address);
            }
        });
        reportHistoryAdapter.notifyDataSetChanged();
        isListSorted = false;
        btnSort.setImageResource(R.drawable.uparrow);
    }


    private void getReportHistory() {
        JSONObject jsonObjectParams = new JSONObject();
        try {
            jsonObjectParams.put(IWebService.KEY_REQ_ACTION, IWebService.KEY_ACTION_REPORT_HISTORY);
            jsonObjectParams.put(IWebService.KEY_USER_ID, DBAdapter.getMapKeyValueString(ReportHistoryActivity.this, IWebService.KEY_USER_ID));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        DataRequest getReport = new DataRequest(ReportHistoryActivity.this);
        getReport.execute(IWebService.MAIN_URL, jsonObjectParams.toString(), new DataRequest.CallBack() {
            @Override
            public void onPreExecute() {
                rlvGlobalProgressbar.setVisibility(View.VISIBLE);
            }

            @Override
            public void onPostExecute(String response) {
                rlvGlobalProgressbar.setVisibility(View.GONE);

                try {
                    if (!DataRequest.hasError(ReportHistoryActivity.this, response, true)) {
                        Log.d(TAG, "response ==> " + response);

                        JSONObject dataJObject = DataRequest.getJObjWebdata(response);
                        Gson gson = new Gson();

                        ArrayList<ReportHistoryModel> tmpReportList = gson.fromJson(
                                dataJObject.getJSONArray(
                                        IWebService.KEY_RES_REPORT_HISTORY).toString(),
                                new TypeToken<ArrayList<ReportHistoryModel>>() {
                                }.getType());
                        if (tmpReportList != null) {
                            reportHistoryList.clear();
                            reportHistoryList.addAll(tmpReportList);
                            sortInDescending();
                            reportHistoryAdapter.notifyDataSetChanged();
                        }

                        sortInAscending();
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

    }

    private void deleteReport(String reportId) {
        JSONObject jsonObjectParams = new JSONObject();
        try {
            jsonObjectParams.put(IWebService.KEY_REQ_ACTION, IWebService.KEY_ACTION_DELETE_REPORT);
            jsonObjectParams.put(IWebService.KEY_USER_ID, DBAdapter.getMapKeyValueString(ReportHistoryActivity.this, IWebService.KEY_USER_ID));
            jsonObjectParams.put(IWebService.KEY_RES_REPORT_ID, reportId);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        DataRequest getReport = new DataRequest(ReportHistoryActivity.this);
        getReport.execute(IWebService.MAIN_URL, jsonObjectParams.toString(), new DataRequest.CallBack() {
            @Override
            public void onPreExecute() {
                rlvGlobalProgressbar.setVisibility(View.VISIBLE);
            }

            @Override
            public void onPostExecute(String response) {
                rlvGlobalProgressbar.setVisibility(View.GONE);

                try {
                    if (!DataRequest.hasError(ReportHistoryActivity.this, response, true)) {
                        Log.d(TAG, "response ==> " + response);
                        try {

                            JSONObject responseObj = new JSONObject(response);
                            Toast.makeText(ReportHistoryActivity.this, responseObj.getString(IWebService.KEY_RES_MESSAGE), Toast.LENGTH_SHORT).show();
                            getReportHistory();


                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

    }

    public void showDeleteAlert(final int position) {

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setMessage("Are you sure you want to delete this report?");

        alertDialogBuilder.setPositiveButton("Yes",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {
                        deleteReport(reportHistoryList.get(position).report_id);
                    }
                });

        alertDialogBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        Dialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }
}
