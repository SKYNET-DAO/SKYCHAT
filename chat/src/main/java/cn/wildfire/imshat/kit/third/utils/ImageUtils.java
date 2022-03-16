package cn.wildfire.imshat.kit.third.utils;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.PixelFormat;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.media.MediaScannerConnection;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.SystemClock;
import android.provider.MediaStore;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import static cn.wildfire.imshat.kit.third.utils.UIUtils.getPackageName;


public class ImageUtils {

    private static final String thumbImgDirPath = UIUtils.getContext().getCacheDir().getAbsolutePath();
    private static File thumbImgDir;

    public static Bitmap getLoacalBitmap(String url) {
        try {
            FileInputStream fis = new FileInputStream(url);
            return BitmapFactory.decodeStream(fis); 

        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }


    public static Bitmap doBlur(Bitmap sentBitmap, int radius,
                                boolean canReuseInBitmap) {
        Bitmap bitmap;
        if (canReuseInBitmap) {
            bitmap = sentBitmap;
        } else {
            bitmap = sentBitmap.copy(sentBitmap.getConfig(), true);
        }

        if (radius < 1) {
            return (null);
        }

        int w = bitmap.getWidth();
        int h = bitmap.getHeight();

        int[] pix = new int[w * h];
        bitmap.getPixels(pix, 0, w, 0, 0, w, h);

        int wm = w - 1;
        int hm = h - 1;
        int wh = w * h;
        int div = radius + radius + 1;

        int r[] = new int[wh];
        int g[] = new int[wh];
        int b[] = new int[wh];
        int rsum, gsum, bsum, x, y, i, p, yp, yi, yw;
        int vmin[] = new int[Math.max(w, h)];

        int divsum = (div + 1) >> 1;
        divsum *= divsum;
        int dv[] = new int[256 * divsum];
        for (i = 0; i < 256 * divsum; i++) {
            dv[i] = (i / divsum);
        }

        yw = yi = 0;

        int[][] stack = new int[div][3];
        int stackpointer;
        int stackstart;
        int[] sir;
        int rbs;
        int r1 = radius + 1;
        int routsum, goutsum, boutsum;
        int rinsum, ginsum, binsum;

        for (y = 0; y < h; y++) {
            rinsum = ginsum = binsum = routsum = goutsum = boutsum = rsum = gsum = bsum = 0;
            for (i = -radius; i <= radius; i++) {
                p = pix[yi + Math.min(wm, Math.max(i, 0))];
                sir = stack[i + radius];
                sir[0] = (p & 0xff0000) >> 16;
                sir[1] = (p & 0x00ff00) >> 8;
                sir[2] = (p & 0x0000ff);
                rbs = r1 - Math.abs(i);
                rsum += sir[0] * rbs;
                gsum += sir[1] * rbs;
                bsum += sir[2] * rbs;
                if (i > 0) {
                    rinsum += sir[0];
                    ginsum += sir[1];
                    binsum += sir[2];
                } else {
                    routsum += sir[0];
                    goutsum += sir[1];
                    boutsum += sir[2];
                }
            }
            stackpointer = radius;

            for (x = 0; x < w; x++) {

                r[yi] = dv[rsum];
                g[yi] = dv[gsum];
                b[yi] = dv[bsum];

                rsum -= routsum;
                gsum -= goutsum;
                bsum -= boutsum;

                stackstart = stackpointer - radius + div;
                sir = stack[stackstart % div];

                routsum -= sir[0];
                goutsum -= sir[1];
                boutsum -= sir[2];

                if (y == 0) {
                    vmin[x] = Math.min(x + radius + 1, wm);
                }
                p = pix[yw + vmin[x]];

                sir[0] = (p & 0xff0000) >> 16;
                sir[1] = (p & 0x00ff00) >> 8;
                sir[2] = (p & 0x0000ff);

                rinsum += sir[0];
                ginsum += sir[1];
                binsum += sir[2];

                rsum += rinsum;
                gsum += ginsum;
                bsum += binsum;

                stackpointer = (stackpointer + 1) % div;
                sir = stack[(stackpointer) % div];

                routsum += sir[0];
                goutsum += sir[1];
                boutsum += sir[2];

                rinsum -= sir[0];
                ginsum -= sir[1];
                binsum -= sir[2];

                yi++;
            }
            yw += w;
        }
        for (x = 0; x < w; x++) {
            rinsum = ginsum = binsum = routsum = goutsum = boutsum = rsum = gsum = bsum = 0;
            yp = -radius * w;
            for (i = -radius; i <= radius; i++) {
                yi = Math.max(0, yp) + x;

                sir = stack[i + radius];

                sir[0] = r[yi];
                sir[1] = g[yi];
                sir[2] = b[yi];

                rbs = r1 - Math.abs(i);

                rsum += r[yi] * rbs;
                gsum += g[yi] * rbs;
                bsum += b[yi] * rbs;

                if (i > 0) {
                    rinsum += sir[0];
                    ginsum += sir[1];
                    binsum += sir[2];
                } else {
                    routsum += sir[0];
                    goutsum += sir[1];
                    boutsum += sir[2];
                }

                if (i < hm) {
                    yp += w;
                }
            }
            yi = x;
            stackpointer = radius;
            for (y = 0; y < h; y++) {
                // Preserve alpha channel: ( 0xff000000 & pix[yi] )
                pix[yi] = (0xff000000 & pix[yi]) | (dv[rsum] << 16)
                        | (dv[gsum] << 8) | dv[bsum];

                rsum -= routsum;
                gsum -= goutsum;
                bsum -= boutsum;

                stackstart = stackpointer - radius + div;
                sir = stack[stackstart % div];

                routsum -= sir[0];
                goutsum -= sir[1];
                boutsum -= sir[2];

                if (x == 0) {
                    vmin[y] = Math.min(y + r1, hm) * w;
                }
                p = x + vmin[y];

                sir[0] = r[p];
                sir[1] = g[p];
                sir[2] = b[p];

                rinsum += sir[0];
                ginsum += sir[1];
                binsum += sir[2];

                rsum += rinsum;
                gsum += ginsum;
                bsum += binsum;

                stackpointer = (stackpointer + 1) % div;
                sir = stack[stackpointer];

                routsum += sir[0];
                goutsum += sir[1];
                boutsum += sir[2];

                rinsum -= sir[0];
                ginsum -= sir[1];
                binsum -= sir[2];

                yi += w;
            }
        }

        bitmap.setPixels(pix, 0, w, 0, 0, w, h);

        return (bitmap);
    }

    public static File genThumbImgFileEx(String srcImgPath) {
        // TODO luban
        try {
            FileInputStream is = new FileInputStream(srcImgPath);
            Bitmap bmpSource = BitmapFactory.decodeStream(is);
            int[] wh = calculateTargetThumbnailWidthAndHeight(bmpSource);
            Bitmap thumbnail = ThumbnailUtils.extractThumbnail(bmpSource, wh[0], wh[1]);

            String thumbImgName = SystemClock.currentThreadTimeMillis() + FileUtils.getFileNameFromPath(srcImgPath);
            File imageFileThumb = new File(ImageUtils.thumbImgDirPath, thumbImgName);
            imageFileThumb.createNewFile();
            FileOutputStream fosThumb = new FileOutputStream(imageFileThumb);

            thumbnail.compress(Bitmap.CompressFormat.JPEG, 100, fosThumb);
            return imageFileThumb;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    static int[] calculateTargetThumbnailWidthAndHeight(Bitmap bitmap) {
        // refer to http://cache.baiducontent.com/c?m=9f65cb4a8c8507ed19fa950d100b92235c43801478d7875f68d4e419ce3b46075a67e9ba2429141096983c310eb8382bfd833665410137c69cdf893acacae23f5b8f3045700bf04705d269b8ba3032b151875b99b869e1ad843384d8d0c4ac57&p=8b2a970686cc42ad59af8c2f1c4398&newp=9361dc16d9c102ec08e2977c0c4f91231610db2151d7d11f6b82c825d7331b001c3bbfb423251b0fd0c3786d02af435beafb3678310923a3dda5c91d9fb4c57479cb64&user=baidu&fm=sc&query=%CE%A2%D0%C5%CB%F5%C2%D4%CD%BC%B4%F3%D0%A1&qid=d4b57bde000284e2&p1=3
        float ratio = bitmap.getWidth() / bitmap.getHeight();
        float width = bitmap.getWidth(), height = bitmap.getHeight();
        if (ratio < 0.4) {
            width = 204;
            height = 510;
        } else if (ratio >= 0.4 && ratio <= 0.5) {
            width = 204;
            height = 204 / ratio;
        } else if (ratio > 0.5 && ratio < 1) {
            width = 405 * ratio;
            height = 405;
        } else if (ratio >= 1 && ratio < 1 / 0.5) { 
            height = 405 * (1 / ratio);
            width = 405;
        } else if (ratio >= 1 / 0.5 && ratio < 1 / 0.4) {
            height = 204;
            width = 204 / (1 / ratio);
        } else if (ratio >= 1 / 0.4) {
            height = 204;
            width = 510;
        }

        return new int[]{(int) width, (int) height};
    }


    public static File genThumbImgFile(String srcImgPath) {
        if (thumbImgDir == null)
            thumbImgDir = new File(thumbImgDirPath);
        if (!thumbImgDir.exists())
            thumbImgDir.mkdirs();
        String thumbImgName = SystemClock.currentThreadTimeMillis() + FileUtils.getFileNameFromPath(srcImgPath);
        File imageFileThumb = null;
        try {
            
            InputStream is = new FileInputStream(srcImgPath);
            Bitmap bmpSource = BitmapFactory.decodeStream(is);

            File imageFileSource = new File(srcImgPath);
            imageFileSource.createNewFile();

            FileOutputStream fosSource = new FileOutputStream(imageFileSource);

            
            bmpSource.compress(Bitmap.CompressFormat.JPEG, 100, fosSource);

            
            Matrix m = new Matrix();
            m.setRectToRect(new RectF(0, 0, bmpSource.getWidth(), bmpSource.getHeight()), new RectF(0, 0, 160, 160), Matrix.ScaleToFit.CENTER);

            
            Bitmap bmpThumb = Bitmap.createBitmap(bmpSource, 0, 0, bmpSource.getWidth(), bmpSource.getHeight(), m, true);

            imageFileThumb = new File(ImageUtils.thumbImgDirPath, thumbImgName);
            imageFileThumb.createNewFile();

            FileOutputStream fosThumb = new FileOutputStream(imageFileThumb);

            
            bmpThumb.compress(Bitmap.CompressFormat.JPEG, 60, fosThumb);

        } catch (IOException e) {
            e.printStackTrace();
        }
        return imageFileThumb;
    }

    
    public static Bitmap decodeSampledBitmapFromResource(Resources res, int resId, int reqWidth, int reqHeight) {
        
        final BitmapFactory.Options options = new BitmapFactory.Options();
        
        options.inJustDecodeBounds = true;
        
        BitmapFactory.decodeResource(res, resId, options);

        
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        
        options.inJustDecodeBounds = false;
        options.inScaled = false;
        
        return BitmapFactory.decodeResource(res, resId, options);
    }

   
    public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        
        final int height = options.outHeight;
        final int width = options.outWidth;
        
        int inSampleSize = 1;

        
        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            
            while ((halfHeight / inSampleSize) >= reqHeight
                    && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }


    public static Bitmap resizePortrait(Bitmap bitmap, int w) {
        Bitmap BitmapOrg = bitmap;
        int width = BitmapOrg.getWidth();
        int height = BitmapOrg.getHeight();
        int newWidth = w;
        int newHeight = w;

        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;

        float scale = Math.min(scaleHeight, scaleWidth);

        Matrix matrix = new Matrix();
        matrix.postScale(scale, scale);
        // if you want to rotate the Bitmap
        // matrix.postRotate(45);
        Bitmap resizedBitmap = Bitmap.createBitmap(BitmapOrg, 0, 0, width,
                height, matrix, true);
        return resizedBitmap;
    }


    public static Bitmap drawableToBitmap(Drawable drawable) {

        int w = drawable.getIntrinsicWidth();
        int h = drawable.getIntrinsicHeight();
        System.out.println("Drawable to Bitmap");
        Bitmap.Config config =
                drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888
                        : Bitmap.Config.RGB_565;
        Bitmap bitmap = Bitmap.createBitmap(w, h, config);
        
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, w, h);
        drawable.draw(canvas);

        return bitmap;
    }




    public static void saveImageToGallery(Context context,Bitmap bitmap) {
        
        File file = null;
        String fileName = System.currentTimeMillis() + ".jpg";
        File root = new File(Environment.getExternalStorageDirectory(), getPackageName());
        File dir = new File(root, "images");
        if (dir.mkdirs() || dir.isDirectory()) {
            file = new File(dir, fileName);
        }
        try {
            FileOutputStream fos = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.flush();
            fos.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
       
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            MediaScannerConnection.scanFile(context, new String[]{file.getAbsolutePath()}, null,
                    (path, uri) -> {
                        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                        mediaScanIntent.setData(uri);
                        context.sendBroadcast(mediaScanIntent);
                    });
        } else {
            String relationDir = file.getParent();
            File file1 = new File(relationDir);
            context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_MOUNTED, Uri.fromFile(file1.getAbsoluteFile())));
        }
    }

}
