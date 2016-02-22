package com.example.cedric.homeremote;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.Spinner;

/**
 * Created by cedric on 2/22/16.
 */

/*
 * Found at :
 * http://stackoverflow.com/questions/2390102/how-to-set-selected-item-of-spinner-by-value-not-by-position
 * Avoids to fire events in loop
 *
 */

public class SaneSpinner extends Spinner {
    public SaneSpinner(Context context) {
        super(context);
    }

    public SaneSpinner(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SaneSpinner(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    // set the ceaseFireOnItemClickEvent argument to true to avoid firing an event
    public void setSelection(int position, boolean animate, boolean ceaseFireOnItemClickEvent) {
        OnItemSelectedListener l = getOnItemSelectedListener();
        if (ceaseFireOnItemClickEvent) {
            setOnItemSelectedListener(null);
        }

        super.setSelection(position, animate);

        if (ceaseFireOnItemClickEvent) {
            setOnItemSelectedListener(l);
        }
    }
}
