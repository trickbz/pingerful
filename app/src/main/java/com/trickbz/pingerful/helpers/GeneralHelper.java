package com.trickbz.pingerful.helpers;

import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public final class GeneralHelper {

    private GeneralHelper() {}

    public static String toShortDateTimeFormat(Date date)
    {
        String formattedDate = "**-** **:**";
        if (date != null)
        {
            Format formatter = new SimpleDateFormat("MM-yy HH:mm");
            formattedDate = formatter.format(date);
        }
        return formattedDate;
    }

}
