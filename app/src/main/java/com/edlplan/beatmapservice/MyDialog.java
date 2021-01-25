package com.edlplan.beatmapservice;

import android.app.Dialog;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.widget.TextView;

public class MyDialog extends Dialog {

    private Util.RunnableWithParam<Dialog> onSure;

    private OnCancelListener onCancel;

    public MyDialog(@NonNull Context context) {
        super(context, R.style.Theme_MaterialComponents_Light_BottomSheetDialog);
        setContentView(R.layout.dialog_base);
        findViewById(R.id.cancel).setOnClickListener(v -> {
            if (onCancel != null) {
                onCancel.onCancel(this);
            } else {
                dismiss();
            }
        });
        findViewById(R.id.sure).setOnClickListener(v -> {
            if (onSure != null) {
                onSure.run(this);
            }
        });
    }

    @Override
    public void setTitle(int titleId) {
        ((TextView) findViewById(R.id.title)).setText(titleId);
    }

    @Override
    public void setTitle(@Nullable CharSequence title) {
        ((TextView) findViewById(R.id.title)).setText(title);
    }

    public void setDescription(CharSequence dsc) {
        ((TextView) findViewById(R.id.desc)).setText(dsc);
    }

    public void setDescription(int dsc) {
        ((TextView) findViewById(R.id.desc)).setText(dsc);
    }

    public void setOnSure(Util.RunnableWithParam<Dialog> onSure) {
        this.onSure = onSure;
    }

    @Override
    public void setOnCancelListener(@Nullable OnCancelListener listener) {
        onCancel = listener;
    }

    public static void showForTask(Context context, String title, String description, Util.RunnableWithParam<Dialog> run) {
        MyDialog dialog = new MyDialog(context);
        dialog.setTitle(title);
        dialog.setDescription(description);
        dialog.setOnSure(run);
        dialog.show();
    }

    public static void showForTask(Context context, String title, String description, Util.RunnableWithParam<Dialog> run, OnCancelListener onCancel) {
        MyDialog dialog = new MyDialog(context);
        dialog.setTitle(title);
        dialog.setDescription(description);
        dialog.setOnSure(run);
        dialog.setOnCancelListener(onCancel);
        dialog.show();
    }

    public static void showForTask(Context context, int title, int description, Util.RunnableWithParam<Dialog> run, OnCancelListener onCancel) {
        MyDialog dialog = new MyDialog(context);
        dialog.setTitle(title);
        dialog.setDescription(description);
        dialog.setOnSure(run);
        dialog.setOnCancelListener(onCancel);
        dialog.show();
    }


}
