package cn.wildfire.imshat.kit.qrcode;

import android.text.TextUtils;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.DecodeHintType;
import com.google.zxing.EncodeHintType;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class CodeHints {
    private static Map<DecodeHintType, Object> DECODE_HINTS = new EnumMap<DecodeHintType, Object>(DecodeHintType.class);
    private static Map<EncodeHintType, Object> ENCODE_HINTS = new EnumMap<>(EncodeHintType.class);

    static {
        List<BarcodeFormat> formats = new ArrayList<BarcodeFormat>();
        formats.add(BarcodeFormat.QR_CODE);
        DECODE_HINTS.put(DecodeHintType.POSSIBLE_FORMATS, formats);
//      DECODE_HINTS.put(DecodeHintType.CHARACTER_SET, "UTF-8");

        ENCODE_HINTS.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.L);
//      ENCODE_HINTS.put(EncodeHintType.CHARACTER_SET, "UTF-8");
    }


    public static Map<DecodeHintType, Object> getDefaultDecodeHints() {
        return DECODE_HINTS;
    }


    public static Map<DecodeHintType, Object> getCustomDecodeHints(String characterSet) {
        Map<DecodeHintType, Object> decodeHints = new EnumMap<DecodeHintType, Object>(DecodeHintType.class);
        List<BarcodeFormat> formats = new ArrayList<BarcodeFormat>();
        formats.add(BarcodeFormat.QR_CODE);
        
        decodeHints.put(DecodeHintType.POSSIBLE_FORMATS, formats);
        
        if (TextUtils.isEmpty(characterSet)) {
            characterSet = "UTF-8";
        }
        decodeHints.put(DecodeHintType.CHARACTER_SET, characterSet);
        return decodeHints;
    }


    public static Map<EncodeHintType, Object> getDefaultEncodeHints() {
        return ENCODE_HINTS;
    }


    public static Map<EncodeHintType, Object> getCustomEncodeHints(ErrorCorrectionLevel level, Integer version,
                                                                   String characterSet) {
        Map<EncodeHintType, Object> encodeHints = new EnumMap<>(EncodeHintType.class);
        
        if (level != null) {
            encodeHints.put(EncodeHintType.ERROR_CORRECTION, level);
        }
        
        if (version >= 1 && version <= 40) {
            encodeHints.put(EncodeHintType.QR_VERSION, version);
        }
        
        if (!TextUtils.isEmpty(characterSet)) {
//          characterSet = "UTF-8";
            encodeHints.put(EncodeHintType.CHARACTER_SET, characterSet);
        }
        return encodeHints;
    }

}

