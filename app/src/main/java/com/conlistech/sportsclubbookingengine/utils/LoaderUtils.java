package com.conlistech.sportsclubbookingengine.utils;

import android.app.ProgressDialog;
import android.content.Context;

public class LoaderUtils {

    public static ProgressDialog progressDialog;


    public static void showProgressBar(Context ctx, String message){
        progressDialog = new ProgressDialog(ctx);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage(message);
        progressDialog.setCancelable(false);
        progressDialog.show();
    }

    public static void dismissProgress(){
        if(progressDialog.isShowing()){
            progressDialog.dismiss();
        }
    }


}
