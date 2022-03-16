/*
 * Copyright 2013-2015 the original author or authors.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.android.wallet.widgets;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Paint;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.text.style.ScaleXSpan;
import android.util.AttributeSet;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.android.wallet.R;
import com.android.wallet.constants.Constants;
import com.android.wallet.utils.MonetarySpannable;

import org.bitcoinj.core.Monetary;
import org.bitcoinj.utils.MonetaryFormat;


/**
 * @author Andreas Schildbach
 */
@SuppressLint("AppCompatCustomView")
public final class CurrencyTextView extends TextView {
    private Monetary amount = null;
    private MonetaryFormat format = null;
    private boolean alwaysSigned = false;
    private RelativeSizeSpan prefixRelativeSizeSpan = null;
    private ScaleXSpan prefixScaleXSpan = null;
    private ForegroundColorSpan prefixColorSpan = null;
    private RelativeSizeSpan insignificantRelativeSizeSpan = null;

    public CurrencyTextView(final Context context) {
        super(context);
    }

    public CurrencyTextView(final Context context, final AttributeSet attrs) {
        super(context, attrs);
    }

    public void setAmount(final Monetary amount) {
        this.amount = amount;
        updateView();
    }

    public void setFormat(@Nullable final MonetaryFormat format) {
        this.format = format != null ? format.codeSeparator(Constants.CHAR_HAIR_SPACE) : null;
        updateView();
    }

    public void setAlwaysSigned(final boolean alwaysSigned) {
        this.alwaysSigned = alwaysSigned;
        updateView();
    }

    public void setStrikeThru(final boolean strikeThru) {
        if (strikeThru)
            setPaintFlags(getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        else
            setPaintFlags(getPaintFlags() & ~Paint.STRIKE_THRU_TEXT_FLAG);
    }

    public void setInsignificantRelativeSize(final float insignificantRelativeSize) {
        if (insignificantRelativeSize != 1) {
            this.prefixRelativeSizeSpan = new RelativeSizeSpan(insignificantRelativeSize);
            this.insignificantRelativeSizeSpan = new RelativeSizeSpan(insignificantRelativeSize);
        } else {
            this.prefixRelativeSizeSpan = null;
            this.insignificantRelativeSizeSpan = null;
        }
    }

    public void setPrefixColor(final int prefixColor) {
        this.prefixColorSpan = new ForegroundColorSpan(prefixColor);
        updateView();
    }

    public void setPrefixScaleX(final float prefixScaleX) {
        this.prefixScaleXSpan = new ScaleXSpan(prefixScaleX);
        updateView();
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        setPrefixColor(ContextCompat.getColor(getContext(), R.color.fg_less_significant));
        setPrefixScaleX(1);
        setInsignificantRelativeSize(0.85f);
        setSingleLine();
    }

    private void updateView() {
        final MonetarySpannable text;

        if (amount != null)
            text = new MonetarySpannable(format, alwaysSigned, amount).applyMarkup(
                    new Object[] { prefixRelativeSizeSpan, prefixScaleXSpan, prefixColorSpan },
                    new Object[] { insignificantRelativeSizeSpan });
        else
            text = null;

        setText(text);
    }
}