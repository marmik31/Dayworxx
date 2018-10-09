package com.project.dayworx.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.project.dayworx.R;
import com.project.dayworx.activity.ReportActivity;
import com.project.dayworx.activity.ReportHistoryActivity;
import com.project.dayworx.model.DayTimeModel;
import com.project.dayworx.model.ReportHistoryModel;
import com.project.dayworx.util.Utils;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by ubuntu on 12/8/16.
 */
public class ReportHistoryAdapter extends RecyclerView.Adapter<ReportHistoryAdapter.MyViewHolder> {

    private static final String TAG = ReportHistoryAdapter.class.getSimpleName();
    private ArrayList<ReportHistoryModel> reportHistoryList = new ArrayList<>();
    private Context context;
    private RecyclerView rv;
    private ReportHistoryActivity reportHistoryActivity;


    public ReportHistoryAdapter(Context context, ArrayList<ReportHistoryModel> reportHistoryList, RecyclerView rv, ReportHistoryActivity reportHistoryActivity) {
        this.reportHistoryList = reportHistoryList;
        this.context = context;
        this.rv = rv;
        this.reportHistoryActivity = reportHistoryActivity;
    }


    public class MyViewHolder extends RecyclerView.ViewHolder {


        @BindView(R.id.txtViewReportId)
        TextView txtViewReportId;

        @BindView(R.id.txtViewOrderNo)
        TextView txtViewOrderNo;

        @BindView(R.id.txtViewProjectName)
        TextView txtViewProjectName;

        @BindView(R.id.txtViewSiteAddress)
        TextView txtViewSiteAddress;

        @BindView(R.id.txtViewDate)
        TextView txtViewDate;

        @BindView(R.id.txtViewDraft)
        TextView txtViewDraft;

        @BindView(R.id.btnDelete)
        ImageView btnDelete;

        @BindView(R.id.divider)
        View divider;

        @BindView(R.id.lyOutCellContainer)
        RelativeLayout lyOutCellContainer;


        public MyViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }
    }


    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.cell_report_history, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, final int position) {

//        holder.txtViewItemCount.setText(userDataList.get(position).getProductQuantity());

        holder.txtViewReportId.setText("Report No : " + reportHistoryList.get(position).report_id);
        holder.txtViewOrderNo.setText("Order No/SI : " + reportHistoryList.get(position).order_no);
        holder.txtViewProjectName.setText("Project Name : " + reportHistoryList.get(position).project_name);
        holder.txtViewSiteAddress.setText(reportHistoryList.get(position).site_address);
        holder.txtViewDate.setText(reportHistoryList.get(position).create_date);

        if (reportHistoryList.get(position).is_draft.equalsIgnoreCase("true")) {
            holder.txtViewDraft.setVisibility(View.VISIBLE);
            holder.lyOutCellContainer.setBackgroundColor(context.getResources().getColor(R.color.colorRed));
        } else {
            holder.txtViewDraft.setVisibility(View.GONE);
            holder.lyOutCellContainer.setBackgroundColor(context.getResources().getColor(R.color.colorGreen));
        }

        if (position == (reportHistoryList.size() - 1)) {
            holder.divider.setVisibility(View.GONE);
        } else {
            holder.divider.setVisibility(View.VISIBLE);
        }

        holder.lyOutCellContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                reportHistoryActivity.switchToShowPdfActivity(position);
            }
        });


        holder.btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Utils.showToastShort(context, "Delete clicked");
                reportHistoryActivity.showDeleteAlert(position);
            }
        });
    }


    @Override
    public int getItemCount() {
        return reportHistoryList.size();
    }

}

