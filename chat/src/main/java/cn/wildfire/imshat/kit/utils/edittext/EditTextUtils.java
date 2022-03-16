package cn.wildfire.imshat.kit.utils.edittext;

import android.text.InputFilter;
import android.text.Spanned;
import android.widget.EditText;
import android.widget.Toast;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cn.wildfire.imshat.kit.third.utils.UIUtils;
import cn.wildfirechat.imshat.R;

public class EditTextUtils {

    public static void setEditTextInputSpace(EditText editText) {
        InputFilter filter = (source, start, end, dest, dstart, dend) -> source.toString().replace("\n"," ");
        editText.setFilters(new InputFilter[]{filter});
    }


    
    public static void setEditTextInputSpeChat(EditText editText) {
        InputFilter filter = new InputFilter() {
            @Override
            public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
                String speChat = "[`~!@#$%^&*()+=|{}':;',\\[\\].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？]";
                Pattern pattern = Pattern.compile(speChat);
                Matcher matcher = pattern.matcher(source.toString());
                if (matcher.find()) {
                    return "";
                } else {
                    return null;
                }
            }
        };
        editText.setFilters(new InputFilter[]{filter});
    }

    


    public static void setNormalFormat(EditText editText){

        InputFilter inputFilter=new InputFilter() {

            Pattern pattern = Pattern.compile("[^a-zA-Z0-9\\u4E00-\\u9FA5_]");
            @Override
            public CharSequence filter(CharSequence charSequence, int i, int i1, Spanned spanned, int i2, int i3) {
                Matcher matcher=  pattern.matcher(charSequence);
                if(!matcher.find()){
                    return null;
                }else{
                    Toast.makeText(editText.getContext(), UIUtils.getString(R.string.str_notinput_specile),Toast.LENGTH_SHORT).show();
                    return "";
                }

            }
        };

        editText.setFilters(new InputFilter[]{inputFilter});

    }



}
