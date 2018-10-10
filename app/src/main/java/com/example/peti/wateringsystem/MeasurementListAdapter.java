package com.example.peti.wateringsystem;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;

public class MeasurementListAdapter extends RecyclerView.Adapter<MeasurementListAdapter.MeasurementViewHolder> {

    class MeasurementViewHolder extends RecyclerView.ViewHolder {
        private final TextView MeasurementItemView;

        private MeasurementViewHolder(View itemView) {
            super(itemView);
            MeasurementItemView = itemView.findViewById(R.id.textView);
        }
    }

        private DateFormat df = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");

        private final LayoutInflater mInflater;
        private List<Measurement> mMeasurements; // Cached copy of measurements

        MeasurementListAdapter(Context context) { mInflater = LayoutInflater.from(context); }

        @Override
        public MeasurementViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = mInflater.inflate(R.layout.recyclerview_item, parent, false);
            return new MeasurementViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(MeasurementViewHolder holder, int position) {
            if (mMeasurements != null) {
                Measurement current = mMeasurements.get(position);
                holder.MeasurementItemView.setText(
                        "Soil moisture was " +
                                String.valueOf(current.getWaterPercentage()) + "% on: "
                                    + df.format(current.getDate())
                );
            } else {
                // Covers the case of data not being ready yet.
                holder.MeasurementItemView.setText("No Percentage");
            }
        }

        void setMeasurements(List<Measurement> measurements){
            mMeasurements = measurements;
            notifyDataSetChanged();
        }

        // getItemCount() is called many times, and when it is first called,
        // mMeasurements has not been updated (means initially, it's null, and we can't return null).
        @Override
        public int getItemCount() {
            if (mMeasurements != null)
                return mMeasurements.size();
            else return 0;
        }
    }

