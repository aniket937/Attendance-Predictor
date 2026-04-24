package com.example.attendance.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.attendance.R;
import com.example.attendance.model.Subject;
import com.example.attendance.utils.AttendanceUtils;

import java.util.List;

/**
 * RecyclerView Adapter for displaying subject list.
 * Binds subject data to UI and handles item clicks.
 */
public class SubjectAdapter extends RecyclerView.Adapter<SubjectAdapter.SubjectViewHolder> {

    private List<Subject> subjectList;
    private Context context;
    private OnSubjectClickListener listener;

    public interface OnSubjectClickListener {
        void onSubjectClick(Subject subject);
        void onSubjectLongClick(Subject subject);
    }

    public SubjectAdapter(Context context, List<Subject> subjectList, OnSubjectClickListener listener) {
        this.context = context;
        this.subjectList = subjectList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public SubjectViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_subject, parent, false);
        return new SubjectViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SubjectViewHolder holder, int position) {
        Subject subject = subjectList.get(position);
        holder.bind(subject);
    }

    @Override
    public int getItemCount() {
        return subjectList.size();
    }

    public void updateData(List<Subject> newList) {
        this.subjectList = newList;
        notifyDataSetChanged();
    }

    class SubjectViewHolder extends RecyclerView.ViewHolder {
        private CardView cardView;
        private TextView tvSubjectName;
        private TextView tvCourseCode;
        private TextView tvAttendance;
        private TextView tvStatus;
        private TextView tvClasses;

        SubjectViewHolder(View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.cardView);
            tvSubjectName = itemView.findViewById(R.id.tvSubjectName);
            tvCourseCode = itemView.findViewById(R.id.tvCourseCode);
            tvAttendance = itemView.findViewById(R.id.tvAttendance);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            tvClasses = itemView.findViewById(R.id.tvClasses);
        }

        void bind(Subject subject) {
            double percentage = AttendanceUtils.calculateAttendance(
                    subject.getAttended(),
                    subject.getTotal()
            );

            tvSubjectName.setText(subject.getName());

            if (subject.getCourseCode() != null && !subject.getCourseCode().isEmpty()) {
                tvCourseCode.setText(subject.getCourseCode());
                tvCourseCode.setVisibility(TextView.VISIBLE);
            } else {
                tvCourseCode.setVisibility(TextView.GONE);
            }

            tvAttendance.setText(AttendanceUtils.formatPercentage(percentage));
            tvStatus.setText(AttendanceUtils.getStatus(percentage));
            tvStatus.setTextColor(AttendanceUtils.getStatusColor(percentage));
            tvClasses.setText(String.format("%d / %d", subject.getAttended(), subject.getTotal()));

            // Handle click events
            cardView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onSubjectClick(subject);
                }
            });

            cardView.setOnLongClickListener(v -> {
                if (listener != null) {
                    listener.onSubjectLongClick(subject);
                }
                return true;
            });
        }
    }
}