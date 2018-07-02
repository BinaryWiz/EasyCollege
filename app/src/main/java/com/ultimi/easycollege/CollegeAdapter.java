package com.ultimi.easycollege;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

public class CollegeAdapter extends RecyclerView.Adapter<CollegeAdapter.CollegeViewHolder>{

    private Context mCtx;
    private ArrayList<CollegeModel> mCollegeModels;

    public CollegeAdapter(Context ctx, ArrayList<CollegeModel> collegeModels) {
        mCtx = ctx;
        mCollegeModels = collegeModels;
    }

    @NonNull
    @Override
    public CollegeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(mCtx);
        View view = inflater.inflate(R.layout.colleges_view, null);
        CollegeViewHolder holder = new CollegeViewHolder(view);
        return holder;

    }

    @Override
    public void onBindViewHolder(@NonNull CollegeViewHolder holder, int position) {

        CollegeModel college = mCollegeModels.get(position);
        holder.mGrade.setText(college.getNicheGrade());
        holder.mCollegeName.setText(college.getCollegeName());
        holder.mSatRange.setText(college.getSatRange());
        holder.mAcceptanceRate.setText(college.getAcceptanceRate());
        holder.mLocation.setText(college.getLocation());
        holder.mPrice.setText(college.getPrice());
        holder.mActRange.setText(college.getActRange());
    }

    @Override
    public int getItemCount() {
        return mCollegeModels.size();
    }

    class CollegeViewHolder extends RecyclerView.ViewHolder {

        TextView mGrade;
        TextView mCollegeName;
        TextView mSatRange;
        TextView mAcceptanceRate;
        TextView mLocation;
        TextView mPrice;
        TextView mActRange;

        public CollegeViewHolder(View itemView) {
            super(itemView);

            mGrade = itemView.findViewById(R.id.grade);
            mCollegeName = itemView.findViewById(R.id.college_name);
            mSatRange = itemView.findViewById(R.id.college_range);
            mAcceptanceRate = itemView.findViewById(R.id.college_acceptance_rate);
            mLocation = itemView.findViewById(R.id.college_location);
            mPrice = itemView.findViewById(R.id.college_price);
            mActRange = itemView.findViewById(R.id.college_act_range);

        }
    }
}
