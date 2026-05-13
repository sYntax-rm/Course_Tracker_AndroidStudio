package com.example.dcfg_studyfi_casestuy;

import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {

    private List<Task> taskList;
    private OnTaskClickListener listener;

    // Interface para maipasa ang click events sa Activity
    public interface OnTaskClickListener {
        void onTaskStatusChanged(Task task, boolean isCompleted);
        void onTaskDeleted(Task task);
    }

    public TaskAdapter(List<Task> taskList, OnTaskClickListener listener) {
        this.taskList = taskList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_task, parent, false);
        return new TaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        Task currentTask = taskList.get(position);
        holder.tvTaskName.setText(currentTask.getTaskName());

        // Alisin ang listener pansamantala para hindi mag-trigger habang sineset ang status
        holder.cbTaskStatus.setOnCheckedChangeListener(null);
        holder.cbTaskStatus.setChecked(currentTask.isCompleted());

        // Strikethrough effect kapag completed na ang task
        if (currentTask.isCompleted()) {
            holder.tvTaskName.setPaintFlags(holder.tvTaskName.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            holder.tvTaskName.setTextColor(0xFF9E9E9E); // Gray color
        } else {
            holder.tvTaskName.setPaintFlags(holder.tvTaskName.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
            holder.tvTaskName.setTextColor(0xFF212121); // Dark color
        }

        // Listener para sa Checkbox
        holder.cbTaskStatus.setOnCheckedChangeListener((buttonView, isChecked) -> {
            listener.onTaskStatusChanged(currentTask, isChecked);
        });

        // Listener para sa Delete Button
        holder.btnDeleteTask.setOnClickListener(v -> {
            listener.onTaskDeleted(currentTask);
        });
    }

    @Override
    public int getItemCount() {
        return taskList.size();
    }

    public static class TaskViewHolder extends RecyclerView.ViewHolder {
        CheckBox cbTaskStatus;
        TextView tvTaskName;
        ImageButton btnDeleteTask;

        public TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            cbTaskStatus = itemView.findViewById(R.id.cb_task_status);
            tvTaskName = itemView.findViewById(R.id.tv_task_name);
            btnDeleteTask = itemView.findViewById(R.id.btn_delete_task);
        }
    }
}