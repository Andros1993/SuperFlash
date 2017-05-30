package et.superflash.view;

import android.app.AlertDialog;
import android.content.Context;

/**
 * Created by Et on 2017/5/29.
 */

public class ColorPickerDialog {

    private Context mContext;
    private ColorPicker mPicker;
    private AlertDialog mAlertDialog;

    public ColorPickerDialog(final Context context) {
        this.mContext = context;

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        mPicker = new ColorPicker(context);
        builder.setView(mPicker);
//        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialog, int which) {
//                Toast.makeText(context,"click yes",Toast.LENGTH_SHORT).show();
//            }
//        });
//        builder.setNegativeButton("取消", null);
        mAlertDialog = builder.create();
    }


    public void setPickerOnListener(ColorPicker.OnColorSelectListener colorSelectListener){
        mPicker.setOnColorSelectListener(colorSelectListener);
    }

    public void show(){
        if(mAlertDialog != null){
            mAlertDialog.show();
        }
    }
}
