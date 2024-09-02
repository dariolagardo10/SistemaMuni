package es.rcti.demoprinterplus.sistemamulta2;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
public class Other {
    public static Bitmap resizeImage(Bitmap bitmap, int width, int height) {
        return Bitmap.createScaledBitmap(bitmap, width, height, true);
    }

    public static Bitmap toGrayscale(Bitmap bmpOriginal) {
        int width = bmpOriginal.getWidth();
        int height = bmpOriginal.getHeight();
        Bitmap bmpGrayscale = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(bmpGrayscale);
        Paint paint = new Paint();
        ColorMatrix colorMatrix = new ColorMatrix();
        colorMatrix.setSaturation(0);
        ColorMatrixColorFilter f = new ColorMatrixColorFilter(colorMatrix);
        paint.setColorFilter(f);
        c.drawBitmap(bmpOriginal, 0, 0, paint);
        return bmpGrayscale;
    }

    public static byte[] thresholdToBWPic(Bitmap grayBitmap) {
        // Implementa la lógica para convertir a blanco y negro
        // Retorna un array de bytes
        return new byte[0]; // Ejemplo vacío, implementa tu lógica aquí
    }

    public static byte[] eachLinePixToCmd(byte[] dithered, int width, int mode) {
        // Implementa la lógica para convertir cada línea de píxeles a comandos
        // Retorna un array de bytes
        return new byte[0]; // Ejemplo vacío, implementa tu lógica aquí
    }
}
