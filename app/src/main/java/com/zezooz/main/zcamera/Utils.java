package com.zezooz.main.zcamera;

import android.content.Context;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Created by Nick on 2016/8/21.
 */

public class Utils {

    static Context context;

    public static String getShaderString (int id)
    {
        try
        {
            InputStream is = context.getResources().openRawResource(id);
            BufferedReader in = new BufferedReader(new InputStreamReader(is));
            StringBuffer buffer = new StringBuffer();
            String line = "";
            while ((line = in.readLine()) != null)
            {
                buffer.append(line + "\n");
            }
            return buffer.toString();
        } catch (Exception e)
        {
            e.printStackTrace();
        }
        return "";
    }


}
