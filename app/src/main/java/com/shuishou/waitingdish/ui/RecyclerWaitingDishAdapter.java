package com.shuishou.waitingdish.ui;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.shuishou.waitingdish.R;
import com.shuishou.waitingdish.bean.WaitingDish;
import com.shuishou.waitingdish.bean.WaitingIntentDetail;

import java.util.List;

public class RecyclerWaitingDishAdapter extends RecyclerView.Adapter<RecyclerWaitingDishAdapter.ViewHolder> implements View.OnClickListener {
    private int resourceId;
    private MainActivity mainActivity;
    private List<WaitingDish> waitingDishes;
    public RecyclerWaitingDishAdapter(MainActivity mainActivity, int resourceId, List<WaitingDish> waitingDishes){
        this.resourceId = resourceId;
        this.mainActivity = mainActivity;
        this.waitingDishes = waitingDishes;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(resourceId, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        WaitingDish wd = waitingDishes.get(position);
        holder.tvDish.setText(wd.getDishName());
        String desks = "";
        int totalAmount = 0;
        for (int i = 0; i < wd.getIndentDetails().size(); i++) {
            WaitingIntentDetail detail = wd.getIndentDetails().get(i);
            if (i > 0) desks += "; ";
            desks += detail.getDesk();
            totalAmount += detail.getAmount();
            if (detail.getAmount() > 1)
                desks += "(" + detail.getAmount() + "份)";
            if (detail.getRequirement() != null && !detail.getRequirement().isEmpty()) {
                desks += "(" + detail.getRequirement() + ")";
            }
        }

        holder.tvDesks.setText(desks);
        holder.tvDeskNum.setText(String.valueOf(totalAmount));
        holder.btnDone.setTag(wd);
        holder.btnDone.setOnClickListener(this);
    }

    @Override
    public int getItemCount() {
        if (waitingDishes == null) return 0;
        return waitingDishes.size();
    }

    @Override
    public void onClick(View view) {
        Object o = view.getTag();
        if (o instanceof WaitingDish) {
            DoneDishDialog dlg = new DoneDishDialog(mainActivity, (WaitingDish)o);
            dlg.showDialog();
        }
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{
        final TextView tvDish;
        final TextView tvDesks;
        final TextView tvDeskNum;
        final Button btnDone;
        public ViewHolder(View view) {
            super(view);
            tvDish = (TextView) view.findViewById(R.id.tv_dish);
            tvDesks = (TextView) view.findViewById(R.id.tv_desks);
            tvDeskNum = (TextView) view.findViewById(R.id.tv_desknum);
            btnDone = (Button) view.findViewById(R.id.btn_dishdone);
        }
    }
}
