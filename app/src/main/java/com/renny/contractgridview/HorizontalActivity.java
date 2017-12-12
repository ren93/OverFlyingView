package com.renny.contractgridview;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.OrientationHelper;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.renny.contractgridview.recyclerview.ItemAdapterHor;
import com.renny.contractgridview.recyclerview.OverFlyingLayoutManager;

import java.util.ArrayList;

public class HorizontalActivity extends AppCompatActivity {
    private RecyclerView mRecyclerView;
    private ItemAdapterHor mAdapter;
    private ArrayList<String> items = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_horizontal);
        initView();
    }

    private void initView() {
        mRecyclerView = findViewById(R.id.recyclerView);
        OverFlyingLayoutManager layoutManager = new OverFlyingLayoutManager(OrientationHelper.HORIZONTAL);
        mRecyclerView.setLayoutManager( new OverFlyingLayoutManager(OrientationHelper.HORIZONTAL));
        layoutManager.setViewEdgeListener(new OverFlyingLayoutManager.viewEdgeListener() {
            @Override
            public void onTop(View view, float offsetPercent) {
                Log.d("bbbb", "onTop  " + offsetPercent);
                CardView cardView = view.findViewById(R.id.card_view);
                //cardView.setCardElevation(10);
            }

            @Override
            public void onBottom(View view, float offsetPercent) {
                Log.d("bbbb", "onBottom  " + offsetPercent);
            }
        });
        //构建一个临时数据源
        for (int i = 0; i < 16; i++) {
            items.add("Item:第" + i + "项");
        }
        mAdapter = new ItemAdapterHor(items);
        mRecyclerView.setAdapter(mAdapter);
        mAdapter.setOnItemClickListerer(new ItemAdapterHor.onItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                Toast.makeText(HorizontalActivity.this, "position:" + position, Toast.LENGTH_SHORT).show();
            }
        });
    }

}
