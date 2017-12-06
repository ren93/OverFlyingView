package com.renny.contractgridview;

import android.graphics.Matrix;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;

public class MainActivity extends AppCompatActivity {
    ImageView imageView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        imageView = findViewById(R.id.image);


    }

    int m = 20;

    //平移
    private void testTranslate() {
        Matrix matrix = new Matrix();
        matrix.postRotate(m);
        imageView.setImageMatrix(matrix);
        m = m + 3;
    }

    /**
     * 显示底部Dialog
     *
     * @param view 视图
     */
    public void showBottomDialog(View view) {
        testTranslate();
       /* FragmentManager fm = getSupportFragmentManager();
        BottomDialogFragment editNameDialog = new BottomDialogFragment();
        editNameDialog.show(fm, "fragment_bottom_dialog");*/
    }
}
