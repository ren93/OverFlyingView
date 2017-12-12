package com.renny.contractgridview.recyclerview;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.renny.contractgridview.R;

import java.util.ArrayList;

/**
 * Created by LuckyCrystal on 2017/6/6.
 */

public class ItemAdapterHor extends RecyclerView.Adapter<ItemAdapterHor.viewHolder> {

    private ArrayList<String> items = new ArrayList<>();
    private onItemClickListener mOnItemClickListerer;


    public ItemAdapterHor(ArrayList<String> items) {
        this.items = items;
    }

    public void setOnItemClickListerer(onItemClickListener onItemClickListerer) {
        mOnItemClickListerer = onItemClickListerer;
    }

    @Override
    public viewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_card_hor,
                viewGroup, false);

        return new viewHolder(view);
    }

    @Override
    public int getItemViewType(int position) {
        return super.getItemViewType(position);
    }

    @Override
    public void onBindViewHolder(final viewHolder viewHolder, final int position) {
        String info = items.get(position);
        viewHolder.textView.setText(info);
        if (mOnItemClickListerer != null) {
            viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mOnItemClickListerer.onItemClick(viewHolder.itemView, position);
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    class viewHolder extends RecyclerView.ViewHolder {
        TextView textView;

        public viewHolder(View itemView) {
            super(itemView);
            textView = itemView.findViewById(R.id.info_text_hor);
        }
    }

    public interface onItemClickListener {
        void onItemClick(View view, int position);
    }
}
