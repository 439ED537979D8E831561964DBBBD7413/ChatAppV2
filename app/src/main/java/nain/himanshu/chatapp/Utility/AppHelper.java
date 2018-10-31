package nain.himanshu.chatapp.Utility;

import android.content.Context;
import android.graphics.Bitmap;

import java.io.ByteArrayOutputStream;

/**
 * Created by vipul on 3/25/2018.
 */

public class AppHelper {

    /**
     * Turn drawable resource into byte array.
     *
     * @param context parent context
     * @param id      drawable resource id
     * @return byte array
     */
   /* public static byte[] getFileDataFromDrawable(Context context, int id) {
        Drawable drawable = ContextCompat.getDrawable(context, id);
        Bitmap bitmap = ((BitmapDrawable) drawable).getBitmap();
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 0, byteArrayOutputStream);
        return byteArrayOutputStream.toByteArray();
    }*/

    /**
     * Turn drawable into byte array.
     *
     * @return byte array
     */
    public static byte[] getFileDataFromDrawable(Context context, Bitmap bitmap) {
/*        Bitmap bitmap = ((BitmapDrawable) drawable).getBitmap();*/
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 20, byteArrayOutputStream);
        return byteArrayOutputStream.toByteArray();
    }
}
