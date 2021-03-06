package com.example.zjb.bamin.utils;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.zjb.bamin.R;

/**
 * Created by zjb on 2016/3/3.
 */
public class DialogShow{

    AlertDialog dialog;

    //一个按钮的dialog提示
    public static void setDialog(Context context, String messageTxt, String iSeeTxt) {
        final AlertDialog dialog;
        LayoutInflater localinflater =(LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View commit_dialog = localinflater.inflate(R.layout.commit_dialog, null);
        TextView message = (TextView) commit_dialog.findViewById(R.id.message);
        Button ISee = (Button) commit_dialog.findViewById(R.id.ISee);
        message.setText(messageTxt);
        ISee.setText(iSeeTxt);
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
            dialog = builder.setView(commit_dialog)
                    .create();
            dialog.setCancelable(false);
            dialog.show();
            commit_dialog.findViewById(R.id.ISee).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                }
            });
    }
}
