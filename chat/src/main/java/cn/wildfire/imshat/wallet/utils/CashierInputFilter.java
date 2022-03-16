package cn.wildfire.imshat.wallet.utils;

import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class CashierInputFilter implements InputFilter {
    Pattern mPattern;

    
    private static Double MAX_VALUE = Double.MAX_VALUE;
    
    private static final int POINTER_LENGTH = 4;

    private static final String POINTER = ".";

    private static final String ZERO = "0";

    public CashierInputFilter() {
        mPattern = Pattern.compile("([0-9]|\\.)*");
    }

    public CashierInputFilter(Double max){

        MAX_VALUE=max;
        mPattern = Pattern.compile("([0-9]|\\.)*");
    }

   
    @Override
    public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
        String sourceText = source.toString();
        String destText = dest.toString();

        
        if (TextUtils.isEmpty(sourceText) || source.length()>1) {
            return "";
        }

        Matcher matcher = mPattern.matcher(source);
    
        if(destText.contains(POINTER)) {
            if (!matcher.matches()) {
                return "";
            } else {
                if (POINTER.equals(source.toString())) {  
                    return "";
                }
            }

            int index = destText.indexOf(POINTER);

            int length = destText.length() - index;

            
            if (dstart > index && length > POINTER_LENGTH) {
                return dest.subSequence(dstart, dend);
            }
        } else {
           
            if (!matcher.matches()) {
                return "";
            } else {
                if ((POINTER.equals(source.toString()))) {  
                    if(TextUtils.isEmpty(destText) || destText.contains(POINTER))
                        return "";

                    
                    int length = destText.length() - dstart;

                    
                    if (length > POINTER_LENGTH) {
                        return dest.subSequence(dstart, dend);
                    }
                } else if (!POINTER.equals(source.toString()) && ZERO.equals(destText)) { 
                    return "";
                }
            }
        }

     
        boolean destTextLengthBiggerThanZero = !TextUtils.isEmpty(destText) && (destText.length() > 0);
        if(ZERO.equals(sourceText)){
            if(destTextLengthBiggerThanZero){
                if(dstart==0 || ZERO.equals(String.valueOf(destText.charAt(0)))) {
                    return "";
                }
            }
        }



     
        double sumText = Double.parseDouble(destText + sourceText);
        if (sumText > MAX_VALUE) {
            return dest.subSequence(dstart, dend);
        }

        return dest.subSequence(dstart, dend) + sourceText;
    }
}