package com.renny.contractgridview;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.OrientationHelper;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Toast;

import com.renny.contractgridview.recyclerview.ItemAdapterHor;
import com.renny.contractgridview.recyclerview.OverFlyingLayoutManager;

import java.util.ArrayList;

public class HorizontalActivity extends AppCompatActivity {
    private RecyclerView mRecyclerView, mRecyclerView2;
    private ArrayList<String> items = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_horizontal);
        initView();
    }

    OverFlyingLayoutManager layoutManager, layoutManager2;

    private void initView() {
        mRecyclerView = findViewById(R.id.recyclerView);
        mRecyclerView2 = findViewById(R.id.recyclerView2);

        layoutManager = new OverFlyingLayoutManager(OrientationHelper.HORIZONTAL);
        mRecyclerView.setLayoutManager(layoutManager);

        layoutManager2 = new OverFlyingLayoutManager(OrientationHelper.HORIZONTAL, false);
        mRecyclerView2.setLayoutManager(layoutManager2);

        //构建一个临时数据源
        for (int i = 0; i < 216; i++) {
            items.add("Item:第" + i + "项");
        }
        ItemAdapterHor adapter = new ItemAdapterHor(items);
        mRecyclerView.setAdapter(adapter);
        adapter.setOnItemClickListerer(new ItemAdapterHor.onItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                Toast.makeText(HorizontalActivity.this, "position:" + position, Toast.LENGTH_SHORT).show();
            }
        });

        ItemAdapterHor adapter2 = new ItemAdapterHor(items);
        mRecyclerView2.setAdapter(adapter2);
        adapter2.setOnItemClickListerer(new ItemAdapterHor.onItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                Toast.makeText(HorizontalActivity.this, "position:" + position, Toast.LENGTH_SHORT).show();
            }
        });
    }


}
