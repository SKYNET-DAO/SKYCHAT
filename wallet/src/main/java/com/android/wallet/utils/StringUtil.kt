package com.android.wallet.utils

import android.text.Spanned
import android.text.SpannedString

class StringUtil{

    companion object {

        fun  hidmiddleStr(str: Spanned, depth:Int): Spanned {

            var address=str.subSequence(0, depth).toString() + "..." + str.subSequence(str.length-depth, str.length)

            return SpannedString(address)
        }


    }

}
