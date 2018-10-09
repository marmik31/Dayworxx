package com.project.dayworx.adapter;

import android.content.Context;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.darsh.multipleimageselect.models.Image;
import com.project.dayworx.R;
import com.project.dayworx.activity.ReportActivity;
import com.project.dayworx.model.DayTimeModel;

import java.io.File;
import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by ubuntu on 12/8/16.
 */
public class DayTimeAdapter extends RecyclerView.Adapter<DayTimeAdapter.MyViewHolder> {

    private static final String TAG = DayTimeAdapter.class.getSimpleName();
    private ArrayList<DayTimeModel> dayTimeList = new ArrayList<>();
    private Context context;
    private RecyclerView rv;
    private ReportActivity reportActivity;


    public DayTimeAdapter(Context context, ArrayList<DayTimeModel> dayTimeList, RecyclerView rv, ReportActivity reportActivity) {
        this.dayTimeList = dayTimeList;
        this.context = context;
        this.rv = rv;
        this.reportActivity = reportActivity;
    }


    public class MyViewHolder extends RecyclerView.ViewHolder {


        @BindView(R.id.txtViewDay)
        TextView txtViewDay;

        @BindView(R.id.txtViewHours)
        TextView txtViewHours;

        @BindView(R.id.divider)
        View divider;

        @BindView(R.id.lyOutCellContainer)
        LinearLayout lyOutCellContainer;


        public MyViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }
    }


    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.cell_day_hours, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, final int position) {

//        holder.txtViewItemCount.setText(userDataList.get(position).getProductQuantity());

        holder.txtViewDay.setText(dayTimeList.get(position).getDay());
        holder.txtViewHours.setText(dayTimeList.get(position).getTime());

        if (position == (dayTimeList.size() - 1)) {
            holder.divider.setVisibility(View.GONE);
        } else {
            holder.divider.setVisibility(View.VISIBLE);
        }

        holder.lyOutCellContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                reportActivity.openDatePicker(position);

            }
        });


    }


    @Override
    public int getItemCount() {
        return dayTimeList.size();
    }

}

