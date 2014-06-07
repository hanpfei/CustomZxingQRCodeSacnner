package com.wolfcs.qrcodescanner;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.MediaStore.Images;
import android.provider.MediaStore.Images.ImageColumns;
import android.util.Log;

public class PhotoManager {
    private static final String TAG = "PhotoManager";
    
    private static final String APP_DATA_DIR_NMAE = "irs";
    private static final String DEFAULT_IMAGE_DIR_NAME = "default";
    private static final String FILE_EXTENSION_SEPARATOR = ".";
    private static final String FILE_EXTENSION_JPEG = "jpg";

    public static final String MIME_TYPE_JPEG = "image/jpeg";
    private static final int DOWN_SAMPLE_FACTOR = 4;

    public static void saveThermalImage(Context context, Bitmap capturedBitmap,
            byte[] adData, int adDataOffset, long date) {
        saveThermalImage(context, null, capturedBitmap, adData, adDataOffset, date);
    }

    private static String generateThermalImageFileName (long date) {
        String rawFileName = getTimestamp(date);
        String fileName =  rawFileName + FILE_EXTENSION_SEPARATOR + FILE_EXTENSION_JPEG;
        return fileName;
    }

    private static String getRegionImageFolderPath (String region) {
        String path = Environment.getExternalStorageDirectory().toString();
        path = path + File.separator  + APP_DATA_DIR_NMAE;

        File appDir = new File(path);
        if (!appDir.exists()) {
            if (!appDir.mkdir()) {
                Log.w(TAG, "Create app data directory failed: " + path);
                return null;
            }
        }

        if(region == null) {
            region = DEFAULT_IMAGE_DIR_NAME;
        }
        path = path + File.separator  + region;
        File regionImageDir = new File(path);
        if (!regionImageDir.exists()) {
            if(!regionImageDir.mkdir()) {
                Log.w(TAG, "Create region image directory failed: " + path);
                return null;
            }
        }

        return path;
    }

    private static void writeAdData(File file, byte[] adData, int adDataOffset) {
    	byte [] data;
    	int length;
        try {
        	RandomAccessFile fin = new RandomAccessFile(file, "r");
        	try {
        		length = (int) fin.length();
        	    data = new byte[length];
        	    fin.readFully(data);
        	} finally {
        		fin.close();
        	}
        	FileOutputStream fout = new FileOutputStream(file);
        	try {
				Log.d(TAG, "** NOVA ** adData.length " + adData.length);
        	    fout.write(data, 0, adDataOffset);
        	    fout.write(adData, 0, adData.length);
        	    fout.write(data, adDataOffset, length - adDataOffset);
        	} finally {
        		fout.close();
        	}
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static Uri insertImageToMediaStore(ContentResolver resolver,
            String title, long date, int orientation, int jpegLength,
            String path, int width, int height) {
        // Insert into MediaStore.
        ContentValues values = new ContentValues(7);
        values.put(ImageColumns.TITLE, title);
        values.put(ImageColumns.DISPLAY_NAME, title + ".jpg");
        values.put(ImageColumns.DATE_TAKEN, date);
        values.put(ImageColumns.MIME_TYPE, MIME_TYPE_JPEG);
        // Clockwise rotation in degrees. 0, 90, 180, or 270.
        values.put(ImageColumns.ORIENTATION, orientation);
        values.put(ImageColumns.DATA, path);
        values.put(ImageColumns.SIZE, jpegLength);
        
        Uri uri = null;
        try {
            uri = resolver.insert(Images.Media.EXTERNAL_CONTENT_URI, values);
        } catch (Throwable th)  {
            // This can happen when the external volume is already mounted, but
            // MediaScanner has not notify MediaProvider to add that volume.
            // The picture is still safe and MediaScanner will find it and
            // insert it into MediaProvider. The only problem is that the user
            // cannot click the thumbnail to review the picture.
            Log.e(TAG, "Failed to write MediaStore" + th);
        }

        return uri;
    }
    
    private static Uri insertImageToMediaStore(ContentResolver resolver,
            String title, long date, String path){
     // Insert into MediaStore.
        ContentValues values = new ContentValues(5);
        values.put(ImageColumns.TITLE, title);
        values.put(ImageColumns.DISPLAY_NAME, title);
        values.put(ImageColumns.DATE_TAKEN, date);
        values.put(ImageColumns.MIME_TYPE, MIME_TYPE_JPEG);
        // Clockwise rotation in degrees. 0, 90, 180, or 270.
        values.put(ImageColumns.DATA, path);

        Uri uri = null;
        try {
            uri = resolver.insert(Images.Media.EXTERNAL_CONTENT_URI, values);
        } catch (Throwable th)  {
            // This can happen when the external volume is already mounted, but
            // MediaScanner has not notify MediaProvider to add that volume.
            // The picture is still safe and MediaScanner will find it and
            // insert it into MediaProvider. The only problem is that the user
            // cannot click the thumbnail to review the picture.
            Log.e(TAG, "Failed to write MediaStore" + th);
        }

        return uri;
    }
    
    public static void saveThermalImage(Context context, String region,
            Bitmap capturedBitmap, byte[] adData, int adDataOffset, long date) {
        String path = getRegionImageFolderPath (region);
        if (path == null) {
            capturedBitmap.recycle();
            return ;
        }

        String fileName = generateThermalImageFileName(date);
        File file = new File(path + File.separator + fileName);

        OutputStream fOutputStream = null;
        try {
            if (!file.exists()) {
                file.delete();
                file.createNewFile();
            }
            fOutputStream = new FileOutputStream(file);

            capturedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, fOutputStream);

            fOutputStream.flush();
            fOutputStream.close();

            insertImageToMediaStore(context.getContentResolver(), file.getName(), date, file.getAbsolutePath());
        } catch (FileNotFoundException e) {
            Log.w(TAG, "Save failed for FileNotFoundException: ");
            e.printStackTrace();
            return;
        } catch (IOException e) {
            Log.w(TAG, "Save failed for IOException: ");
            e.printStackTrace();
            return;
        } finally {
            capturedBitmap.recycle();
        }

        writeAdData(file, adData, adDataOffset);
    }

    private static String getTimestamp(long date) {
        Date now = new Date(date);
        SimpleDateFormat format =   new SimpleDateFormat( "yy-MM-dd_HH-mm-ss" );
        String timestamp = format.format(now);
        return timestamp;
    }

    private static String generateCameraImageFileName(long date) {
        String rawFileName = getTimestamp(date);
        String fileName =  rawFileName + "_cam" + FILE_EXTENSION_SEPARATOR + FILE_EXTENSION_JPEG;
        return fileName;
    }

    public static Uri saveCameraJpegImage(Context context, byte[] jpegData,
            long date) {
        return saveCameraJpegImage(context, null, jpegData, date);
    }

    public static Uri saveCameraJpegImage(Context context, String region,
            byte[] jpegData, long date) {
        String path = getRegionImageFolderPath (region);

        String fileName = generateCameraImageFileName(date);
        path = path + File.separator + fileName;
        if (jpegData == null) {
            return null;
        }

        File file = new File(path);
        FileOutputStream stream = null;
        try {
            try {
                stream = new FileOutputStream(file);
                stream.write(jpegData);
                stream.flush();
            } catch (Exception e) {
                e.printStackTrace();
                // We didn't really need to save it anyway, did we?
            } finally {
                if (stream != null) {
                    stream.close();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        Uri uri = insertImageToMediaStore(context.getContentResolver(), fileName, date, path);
        uri = Uri.fromFile(file);
        return uri;
    }

    public static List<String> getRegions() {
        ArrayList<String> regions = new ArrayList<String>();
        String path = Environment.getExternalStorageDirectory().toString();
        path = path + File.separator  + APP_DATA_DIR_NMAE;

        File imageFolder = new File(path);
        File[] regionFolders = imageFolder.listFiles();

        if (regionFolders != null) {
            for (int i = 0; i < regionFolders.length; ++ i) {
                if (regionFolders[i].isDirectory()) {
                    regions.add(regionFolders[i].getName());
                }
            }
        }

        return regions;
    }

    public static Uri getRegionFirstPicture(String region) {
        Uri url = null;
        List<Uri> pictures = getRegionPictures(region);
        if (pictures.size() > 0) {
            url = pictures.get(0);
        }

        return url;
    }
    
    public static List<Uri> getRegionPictures(String region) {
        ArrayList<Uri> pictures = new ArrayList<Uri>();
        String path = Environment.getExternalStorageDirectory().toString();
        path = path + File.separator  + APP_DATA_DIR_NMAE;
        path = path + File.separator  + region;

        File imageFolder = new File(path);
        File[] regionImages = imageFolder.listFiles();

        for (int i = 0; i < regionImages.length; ++ i) {
            pictures.add(Uri.fromFile(regionImages[i]));
        }

        return pictures;
    }

    public static Bitmap getBitmap(ContentResolver cr, Uri url) {
        Bitmap bitmap = null;

        try {
            bitmap = MediaStore.Images.Media.getBitmap(cr, url);
        } catch (Exception e) {

        }
        if (url == null) {
            return null;
        }
        String imagePath = url.getPath();

        final BitmapFactory.Options opts = new BitmapFactory.Options();
        opts.inSampleSize = DOWN_SAMPLE_FACTOR;

        bitmap = BitmapFactory.decodeFile(imagePath, opts);
        return bitmap;
    }
    
    public static String getBitmapName(ContentResolver cr, Uri url) {
        String imagePath = url.getPath();
        File file = new File(imagePath);
        return file.getName();
    }
    
    public static String getPictureTitle(ContentResolver cr, Uri url) {
        String title = null;

//        try {
//            bitmap = MediaStore.Images.Media.getBitmap(cr, url);
//        } catch (Exception e) {
//
//        }
        String imagePath = url.getPath();
        File imageFile = new File(imagePath);
        return imageFile.getName();
    }
    
    public static void deleteImage(ContentResolver cr, Uri url) {
        String imagePath = url.getPath();
        File file = new File(imagePath);
        file.delete();
    }
}
