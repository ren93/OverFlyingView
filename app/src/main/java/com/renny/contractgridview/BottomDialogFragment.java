package com.renny.contractgridview;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Toast;

import static android.widget.Toast.LENGTH_SHORT;

/**
 * Created by Renny on 2017/12/8.
 */

public class BottomDialogFragment extends DialogFragment implements View.OnClickListener {


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_bottom, null);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        view.findViewById(R.id.sogo1).setOnClickListener(this);
        view.findViewById(R.id.sogo2).setOnClickListener(this);
        view.findViewById(R.id.sogo3).setOnClickListener(this);
        view.findViewById(R.id.qq1).setOnClickListener(this);
        view.findViewById(R.id.qq2).setOnClickListener(this);
        view.findViewById(R.id.qq3).setOnClickListener(this);
        view.findViewById(R.id.ie1).setOnClickListener(this);
        view.findViewById(R.id.ie2).setOnClickListener(this);
        view.findViewById(R.id.ie3).setOnClickListener(this);

    }

    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null && dialog.getWindow() != null) {
            Window window = dialog.getWindow();
            dialog.getWindow().setGravity(Gravity.BOTTOM);
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            window.setWindowAnimations(R.style.animate_dialog);
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            setCancelable(true);
        }
    }

    @Override
    public void onClick(View v) {
        v.setSelected(!v.isSelected());
        int id = v.getId();
        switch (id) {
            case R.id.sogo1:
            case R.id.sogo2:
            case R.id.sogo3:
                Toast.makeText(getActivity(), "SoGO", LENGTH_SHORT).show();
                break;
            case R.id.qq1:
            case R.id.qq2:
            case R.id.qq3:
                Toast.makeText(getActivity(), "QQ", LENGTH_SHORT).show();
                break;
            case R.id.ie1:
            case R.id.ie2:
            case R.id.ie3:
                Toast.makeText(getActivity(), "IE", LENGTH_SHORT).show();
                break;
        }
    }
}