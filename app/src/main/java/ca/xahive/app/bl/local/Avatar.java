package ca.xahive.app.bl.local;


import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.SparseArray;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.HashMap;

import ca.xahive.app.bl.utils.Helpers;
import ca.xahive.app.bl.utils.S3TaskParams;
import ca.xahive.app.bl.utils.S3TaskResult;


public class Avatar extends InstantObservable {
    public static final String AVATAR_FORMAT = "avatar_%d";
    public static final long AVATAR_RETRY_TIME = 600*1000;
    public static final long MAX_AVATAR_AGE = 86400*1000;

    private static SparseArray<Avatar> avatars;
    private static final Object avatarsSync = new Object();

    /* ivars */
    private AvatarLoader avatarLoader;
    private LocalFileOpenWorker openWorker;
    private int avatarId;
    private byte[] avatarData;
    private Date avatarFailureDate;
    private HashMap<XYDimension, Bitmap> bitmapCache;

    private static SparseArray<Avatar> getAvatars() {
        synchronized (avatarsSync) {
            if (avatars == null) {
                avatars = new SparseArray<Avatar>();
            }
            return avatars;
        }
    }

    private AvatarLoader getAvatarLoader() {
        if (avatarLoader == null) {
            avatarLoader = new AvatarLoader() {
                @Override
                protected void onPostExecute(S3TaskResult result) {
                    onDownloadComplete(result);
                }
            };
        }
        return avatarLoader;
    }

    public static Avatar avatarWithId(int avatarId) {
        Avatar avatar = getAvatars().get(avatarId);

        if (avatar == null) {
            avatar = new Avatar(avatarId);
            getAvatars().put(avatarId, avatar);
        }

        return avatar;
    }

    public boolean updateAvatarWithSelectedImage(Uri contentUri) {

            File avatarFile = null;

            String filename = LocalStorage.getInstance().getAvatarFilenameForUserId( Model.getInstance().getCurrentUser().getUserId());

            if(filename == null) {
                avatarFile = LocalStorage.generateCacheFile();
            } else {
                avatarFile = new File(filename);
            }

            String sourceFilename= Helpers.getRealPathFromURI(contentUri);
            String destinationFilename = avatarFile.getPath();

            BufferedInputStream bis = null;
            BufferedOutputStream bos = null;

            try {
                bis = new BufferedInputStream(new FileInputStream(sourceFilename));
                bos = new BufferedOutputStream(new FileOutputStream(destinationFilename, false));
                byte[] buf = new byte[1024];
                bis.read(buf);
                do {
                    bos.write(buf);
                } while(bis.read(buf) != -1);
            } catch (IOException e) {
                return false;
            } finally {
                try {
                    if (bis != null) bis.close();
                    if (bos != null) bos.close();
                } catch (IOException e) {
                    return false;
                }
            }

            LocalStorage.getInstance().saveAvatarFilename(avatarFile.getAbsolutePath(), Model.getInstance().getCurrentUser().getUserId());

            resetAvatarData();

            return true;

    }

    public void resetAvatarData() {
        avatarData = null;
        bitmapCache = null;

        /* Todo: This seems like the wrong place for this. Wouldn't we
         want to notify observers when we actually have something?
        */

        setChanged();
        notifyObservers();
    }

    private Avatar(int avatarId) {
        this.avatarId = avatarId;
    }

    public int getAvatarId() {
        return avatarId;
    }

    public byte[] getAvatarData() {

        if (avatarData == null) {
            File file = getFile();

            if (file != null && file.length() > 0) {

                openAvatarFile();

                if (isUpdateRequired()) {
                    download();
                }

            }
            else {
                download();
            }
        } else if (isUpdateRequired()) {
            download();
        }

        return avatarData;
    }

    public HashMap<XYDimension, Bitmap> getBitmapCache() {
        if (bitmapCache == null) {
            bitmapCache = new HashMap<XYDimension, Bitmap>();
        }
        return bitmapCache;
    }
    
    public boolean isUpdateRequired() {

        long timeDiff = (new Date()).getTime() - getFile().lastModified();
        
        return (timeDiff >= MAX_AVATAR_AGE);
    }

    public Bitmap getBitmapWithDimensions(XYDimension dimension) {
        Bitmap bitmap = null;

        for (XYDimension xyd : getBitmapCache().keySet()) {
            if (xyd.equals(dimension)) {
                bitmap = getBitmapCache().get(xyd);
                break;
            }
        }

        if (bitmap == null) {
            bitmap = buildBitmapWithDimensions(dimension);
            if (bitmap != null) {
                getBitmapCache().put(dimension, bitmap);
            }
        }

        return bitmap;
    }

    private Bitmap buildBitmapWithDimensions(XYDimension dimension) {

        byte[] rawData = getAvatarData();

        Bitmap bmp = null;

        if (rawData != null) {
            InputStream is = new ByteArrayInputStream(rawData);

            BitmapFactory.Options bitmapFactoryOptions = new BitmapFactory.Options();
            //get the bitmap data before encoding it
            bitmapFactoryOptions.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(is, null, bitmapFactoryOptions);

            int imageWidth = bitmapFactoryOptions.outWidth;
            int imageHeight = bitmapFactoryOptions.outHeight;

            int imageScaleFactor = Math.min(imageWidth / dimension.getX(),
                    imageHeight / dimension.getY());

            //scale the bitmap created with the inputstream to approximately match the target size
            bitmapFactoryOptions.inJustDecodeBounds = false;
            bitmapFactoryOptions.inSampleSize = imageScaleFactor;
            bitmapFactoryOptions.inPurgeable = true;

            is = new ByteArrayInputStream(rawData);

            bmp = BitmapFactory.decodeStream(is, null, bitmapFactoryOptions);
        }

        return bmp;
    }

    private File getFile() {
        File file = null;
        String savedFilename = LocalStorage.getInstance().getAvatarFilenameForUserId(getAvatarId());

        if (savedFilename != null) {
            file = new File(savedFilename);
        }
        else {
            file = LocalStorage.generateCacheFile();
            LocalStorage.getInstance().saveAvatarFilename(file.getAbsolutePath(), getAvatarId());
        }

        return file;
    }

    public String getKey() {
        return String.format(AVATAR_FORMAT, getAvatarId());
    }

    private LocalFileOpenWorker getOpenWorker() {
        if (openWorker == null) {
            LocalFileOpenWorker.LocalFileOpenSuccess onSuccess = new LocalFileOpenWorker.LocalFileOpenSuccess() {
                @Override
                public void run() {
                    super.run();
                    onFileOpenComplete(getData());
                }
            };

            Runnable onFail = new Runnable() {
                @Override
                public void run() {
                    onFileOpenComplete(null);
                }
            };

            openWorker = new LocalFileOpenWorker(getFile(), onSuccess, onFail);
        }

        return openWorker;
    }

    private boolean isDownloading() {
        return (avatarLoader != null);
    }

    private boolean isOpening() {
        return (openWorker != null);
    }

    private void download() {
        synchronized (this) {
            if (isDownloading() || isOpening()) {
                return; // Avoiding dupes.
            }

            if (avatarFailureDate != null) {
                if ((new Date()).getTime() - avatarFailureDate.getTime() > AVATAR_RETRY_TIME) {
                    avatarFailureDate = null;
                } else {
                    // Too soon to retry avatar download.
                    return;
                }
            }

            getAvatarLoader().execute(new S3TaskParams(getFile(), getKey()));
        }
    }

    private void onDownloadComplete(S3TaskResult result) {
        if (result.getErrorMessage() != null) {
            openAvatarFile();
        }
        else {
            avatarFailureDate = new Date();
        }
    }

    private void openAvatarFile() {
        synchronized (this) {
            if (!isOpening()) {
                getOpenWorker().execute();
            }
        }
    }

    private void onFileOpenComplete(byte[] data) {
        avatarLoader = null;
        openWorker = null;

        if (data != null) {
            avatarData = data;
        }

        setChanged();
        notifyObservers();
    }
}
