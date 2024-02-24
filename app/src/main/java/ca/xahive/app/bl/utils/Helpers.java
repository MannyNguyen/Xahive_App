package ca.xahive.app.bl.utils;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Base64;
import android.util.Base64OutputStream;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import com.amazonaws.util.IOUtils;

import org.spongycastle.asn1.ASN1Integer;
import org.spongycastle.asn1.ASN1Sequence;

import org.spongycastle.util.io.pem.PemObject;
import org.spongycastle.util.io.pem.PemWriter;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.RSAPrivateKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;


import IAPUtils.Base64DecoderException;
import IAPUtils.Utils;
import ca.xahive.app.bl.local.Model;
import ca.xahive.app.bl.objects.HiveBasedSettings;
import ca.xahive.app.ui.activities.TabBarManagerActivity;


public class Helpers {
    public static final long AGES_AGO = -500L;
    public static final long FOREVER_ALREADY = 8223372036854775L;
    /**
     * 50 MB
     */
    private static int MAX_FILE_SIZE = 50000000;


    public static void hideSoftKeyboardInActivity(Activity activity) {
        View view = activity.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    public static void hideSoftKeyboardForEditText(EditText editText) {
        InputMethodManager inputMethodManager = (InputMethodManager) editText.getContext().getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(editText.getWindowToken(), 0);
    }

    public static void showSoftKeyboardForEditText(EditText editText) {
        //InputMethodManager imm = (InputMethodManager) editText.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        //imm.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT);
        final InputMethodManager keyboard = (InputMethodManager) editText.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        keyboard.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
    }

    public static String getEncryptedPassword(String password) {
        String encryptedPassword = "";
        EncryptableString encStr = new EncryptableString();
        encStr.setOriginalString(password);
        byte[] preparedData = encStr.getPreparedData();
        byte[] outData = Crypto.encryptData(Config.PASSWORD_CRYPTO_KEY, preparedData);

        encryptedPassword = Helpers.b64EncodedString(outData);

        return encryptedPassword;
    }

    public static PublicKey getPublicKeyFromPemFormat(String PEMString, boolean isFilePath) throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {

        BufferedReader pemReader = null;
        if (isFilePath) {
            pemReader = new BufferedReader(new InputStreamReader(
                    new FileInputStream(PEMString)));
        } else pemReader = new BufferedReader(new InputStreamReader(
                new ByteArrayInputStream(PEMString.getBytes("UTF-8"))));
        StringBuffer content = new StringBuffer();
        String line = null;
        while ((line = pemReader.readLine()) != null) {
            if (line.indexOf("-----BEGIN PUBLIC KEY-----") != -1) {
                while ((line = pemReader.readLine()) != null) {
                    if (line.indexOf("-----END PUBLIC KEY") != -1) {
                        break;
                    }
                    content.append(line.trim());
                }
                break;
            }
        }
        if (line == null) {
            throw new IOException("PUBLIC KEY" + " not found");
        }
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        return keyFactory.generatePublic(new X509EncodedKeySpec(Base64.decode(content.toString(), Base64.DEFAULT)));

    }

    static String TAG_RSA_PRIVATE_KEY_BEGIN = "-----BEGIN RSA PRIVATE KEY-----";
    static String TAG_RSA_PRIVATE_KEY_END = "-----END RSA PRIVATE KEY-----";

    /**
     * Turns a String representing a private key into a PrivateKey object
     *
     * @return PrivateKey representation of string private key
     * @throws Exception
     */
    public static PrivateKey getPrivateKeyWithPemFormat(String privateKeyString) {
        String privateKeyStringTemp = privateKeyString.replaceAll(TAG_RSA_PRIVATE_KEY_BEGIN, "").replaceAll(TAG_RSA_PRIVATE_KEY_END, "");
        byte[] encodedPrivateKey = Base64.decode(privateKeyStringTemp.getBytes(), 0);
        //byte [] encodedPrivateKey = Base64.decode(privateKeyString, Base64.DEFAULT);
        try {
            ASN1Sequence primitive = (ASN1Sequence) ASN1Sequence.fromByteArray(encodedPrivateKey);
            Enumeration<ASN1Integer> e = (Enumeration<ASN1Integer>) primitive.getObjects();

            e.nextElement().getValue();
            BigInteger modulus = e.nextElement().getValue();
            e.nextElement().getValue();
            BigInteger privateExponent = e.nextElement().getValue();

            RSAPrivateKeySpec spec = new RSAPrivateKeySpec(modulus, privateExponent);
            KeyFactory kf = KeyFactory.getInstance("RSA");
            return kf.generatePrivate(spec);
        } catch (Exception e) {
            //Flog.e(e);
        }
        return null;
    }

    public static String getPublicKeyToPem(PublicKey publicKey) {
        StringWriter writer = new StringWriter();
        PemWriter pemWriter = new PemWriter(writer);
        try {
            pemWriter.writeObject(new PemObject("PUBLIC KEY", publicKey.getEncoded()));
            pemWriter.flush();
            pemWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return writer.toString();
    }

    public static String getPrivateKeyToPem(PrivateKey publicKey) {
        StringWriter writer = new StringWriter();
        PemWriter pemWriter = new PemWriter(writer);
        try {
            pemWriter.writeObject(new PemObject("PRIVATE RSA KEY", publicKey.getEncoded()));
            pemWriter.flush();
            pemWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return writer.toString();
    }

    public static String extractStringFromTextView(TextView tv) {
        CharSequence cs = tv.getText();

        if (cs != null) {
            return cs.toString();
        }

        return null;
    }

    public static boolean stringIsNotNullAndMeetsMinLength(String s, int minLength) {
        return (s != null && s.length() >= minLength);
    }

    public static String stringOrEmptyString(String s) {
        return (s != null) ? s : "";
    }

    public static String stringOrEmptyInt(int s){
        return (s != 0) ? String.valueOf(s) : "";
    }

    public static boolean stringIsValidEmail(String s) {
        return (s != null && android.util.Patterns.EMAIL_ADDRESS.matcher(s).matches());
    }

    public static double maxBuzzFileSize() {
        HiveBasedSettings hiveBasedSettings = Model.getInstance().getHiveSettings().getHiveSettingsData();
        return hiveBasedSettings.getMaxBuzzFileSize();
    }

    public static double maxPersonalMessageFileSize() {
        HiveBasedSettings hiveBasedSettings = Model.getInstance().getHiveSettings().getHiveSettingsData();
        if (hiveBasedSettings == null) {
            return MAX_FILE_SIZE;
        }
        return hiveBasedSettings.getMaxMessageFileSize();

    }

    public static boolean fileWithinSizeLimitsForPersonalMessage(double fileSize) {

        if (fileSize > Helpers.maxPersonalMessageFileSize())
            return false;
        else
            return true;

    }

    public static String getDeviceID(Context mConText) {
        return Settings.Secure.getString(mConText.getContentResolver(),
                Settings.Secure.ANDROID_ID);

    }

    public static boolean fileWithinSizeLimitsForBuzz(double fileSize) {

        if (fileSize > Helpers.maxBuzzFileSize())
            return false;
        else
            return true;
    }

    public static String humanReadableDate(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat("EEE MMM d - hh:mm aaa");
        return sdf.format(date);
    }

    public static String humanReadableFileSize(double bytes) {
        int unit = 1024;
        if (bytes < unit) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(unit));
        String pre = "kMGTPE".charAt(exp - 1) + "";
        return String.format("%.1f %sB", bytes / Math.pow(unit, exp), pre);
    }


    public static boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();

        Boolean yup = Environment.MEDIA_MOUNTED.equals(state);

        return yup;
    }

    public static boolean isExitFile(String fileName) {
        String dirPath = Environment.getExternalStorageDirectory() + "/com.xahive.app";
        File dir = new File(dirPath);
        if (!dir.exists()) {
            dir.mkdir();
        }
        File file = new File(dirPath, fileName);
        if (file.exists()) {
            return true;
        }
        return false;
    }

    public static boolean clearFile(String fileName) {
        String dirPath = Environment.getExternalStorageDirectory() + "/com.xahive.app";
        File dir = new File(dirPath);
        if (!dir.exists()) {
            dir.mkdir();
        }
        File file = new File(dirPath, fileName);
        if (file.exists()) {
            file.delete();
        }
        return false;
    }

    public static boolean checkFileDownloadExits(String fileName) {
        String dirPath = Environment.getExternalStorageDirectory() + "/com.xahive.app/";
        File file = new File(dirPath + fileName);
        return file.exists();
    }

    public static boolean isExistFile(String path) {
        String dirPath = path;
        File dir = new File(dirPath);


        // File file = new File(dirPath, fileName);
        if (dir.isFile())

            return true;
        //  if (file.exists()) {
        //   file.delete();
        // }
        return false;
    }

    public static File fileFromPath(String path) {
        String dirPath = path;
        File dir = new File(dirPath);

        return dir;
    }

    public static File createNewFile(String fileName) {
        String dirPath = Environment.getExternalStorageDirectory() + "/com.xahive.app";
        File dir = new File(dirPath);
        if (!dir.exists()) {
            dir.mkdir();
        }
        File file = new File(dirPath, fileName);

        return file;
    }

    public static File getDownloadedFileNamed(Context mContext, String fileName) {
        String dirPath = Environment.getExternalStorageDirectory() + "/com.xahive.app";
        File dir = new File(dirPath);
        if (!dir.exists()) {
            dir.mkdir();
        }
        File file = new File(dirPath, fileName);

        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
            //file = new File(mContext.getFilesDir(), fileName);
        }
        //String dirPath = (Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath());

        return file;

    }

    public static String getDownloadedFileMime(String fileName) {
        //http://www.rgagnon.com/javadetails/java-0487.html  might be a better way research later
        if (fileName.endsWith("0.123")) {
            return "application/vnd.lotus-1-2-3";
        } else if (fileName.endsWith(".3dml")) {
            return "text/vnd.in3d.3dml";
        } else if (fileName.endsWith(".3g2")) {
            return "video/3gpp2";
        } else if (fileName.endsWith(".3gp")) {
            return "video/3gpp";
        } else if (fileName.endsWith(".a")) {
            return "application/octet-stream";
        } else if (fileName.endsWith(".aab")) {
            return "application/x-authorware-bin";
        } else if (fileName.endsWith(".aac")) {
            return "audio/x-aac";
        } else if (fileName.endsWith(".aam")) {
            return "application/x-authorware-map";
        } else if (fileName.endsWith(".aas")) {
            return "application/x-authorware-seg";
        } else if (fileName.endsWith(".abw")) {
            return "application/x-abiword";
        } else if (fileName.endsWith(".acc")) {
            return "application/vnd.americandynamics.acc";
        } else if (fileName.endsWith(".ace")) {
            return "application/x-ace-compressed";
        } else if (fileName.endsWith(".acu")) {
            return "application/vnd.acucobol";
        } else if (fileName.endsWith(".acutc")) {
            return "application/vnd.acucorp";
        } else if (fileName.endsWith(".adp")) {
            return "audio/adpcm";
        } else if (fileName.endsWith(".aep")) {
            return "application/vnd.audiograph";
        } else if (fileName.endsWith(".afm")) {
            return "application/x-font-type1";
        } else if (fileName.endsWith(".afp")) {
            return "application/vnd.ibm.modcap";
        } else if (fileName.endsWith(".ai")) {
            return "application/postscript";
        } else if (fileName.endsWith(".aif")) {
            return "audio/x-aiff";
        } else if (fileName.endsWith(".aifc")) {
            return "audio/x-aiff";
        } else if (fileName.endsWith(".aiff")) {
            return "audio/x-aiff";
        } else if (fileName.endsWith(".air")) {
            return "application/vnd.adobe.air-application-installer-package+zip";
        } else if (fileName.endsWith(".ami")) {
            return "application/vnd.amiga.ami";
        } else if (fileName.endsWith(".apk")) {
            return "application/vnd.android.package-archive";
        } else if (fileName.endsWith(".application")) {
            return "application/x-ms-application";
        } else if (fileName.endsWith(".apr")) {
            return "application/vnd.lotus-approach";
        } else if (fileName.endsWith(".asc")) {
            return "application/pgp-signature";
        } else if (fileName.endsWith(".asf")) {
            return "video/x-ms-asf";
        } else if (fileName.endsWith(".asm")) {
            return "text/x-asm";
        } else if (fileName.endsWith(".aso")) {
            return "application/vnd.accpac.simply.aso";
        } else if (fileName.endsWith(".asx")) {
            return "video/x-ms-asf";
        } else if (fileName.endsWith(".atc")) {
            return "application/vnd.acucorp";
        } else if (fileName.endsWith(".atom")) {
            return "application/atom+xml";
        } else if (fileName.endsWith(".atomcat")) {
            return "application/atomcat+xml";
        } else if (fileName.endsWith(".atomsvc")) {
            return "application/atomsvc+xml";
        } else if (fileName.endsWith(".atx")) {
            return "application/vnd.antix.game-component";
        } else if (fileName.endsWith(".au")) {
            return "audio/basic";
        } else if (fileName.endsWith(".avi")) {
            return "video/x-msvideo";
        } else if (fileName.endsWith(".aw")) {
            return "application/applixware";
        } else if (fileName.endsWith(".azf")) {
            return "application/vnd.airzip.filesecure.azf";
        } else if (fileName.endsWith(".azs")) {
            return "application/vnd.airzip.filesecure.azs";
        } else if (fileName.endsWith(".azw")) {
            return "application/vnd.amazon.ebook";
        } else if (fileName.endsWith(".bat")) {
            return "application/x-msdownload";
        } else if (fileName.endsWith(".bcpio")) {
            return "application/x-bcpio";
        } else if (fileName.endsWith(".bdf")) {
            return "application/x-font-bdf";
        } else if (fileName.endsWith(".bdm")) {
            return "application/vnd.syncml.dm+wbxml";
        } else if (fileName.endsWith(".bh2")) {
            return "application/vnd.fujitsu.oasysprs";
        } else if (fileName.endsWith(".bin")) {
            return "application/octet-stream";
        } else if (fileName.endsWith(".bmi")) {
            return "application/vnd.bmi";
        } else if (fileName.endsWith(".bmp")) {
            return "image/bmp";
        } else if (fileName.endsWith(".book")) {
            return "application/vnd.framemaker";
        } else if (fileName.endsWith(".box")) {
            return "application/vnd.previewsystems.box";
        } else if (fileName.endsWith(".boz")) {
            return "application/x-bzip2";
        } else if (fileName.endsWith(".bpk")) {
            return "application/octet-stream";
        } else if (fileName.endsWith(".btif")) {
            return "image/prs.btif";
        } else if (fileName.endsWith(".bz")) {
            return "application/x-bzip";
        } else if (fileName.endsWith(".bz2")) {
            return "application/x-bzip2";
        } else if (fileName.endsWith(".c")) {
            return "text/x-c";
        } else if (fileName.endsWith(".c4d")) {
            return "application/vnd.clonk.c4group";
        } else if (fileName.endsWith(".c4f")) {
            return "application/vnd.clonk.c4group";
        } else if (fileName.endsWith(".c4g")) {
            return "application/vnd.clonk.c4group";
        } else if (fileName.endsWith(".c4p")) {
            return "application/vnd.clonk.c4group";
        } else if (fileName.endsWith(".c4u")) {
            return "application/vnd.clonk.c4group";
        } else if (fileName.endsWith(".cab")) {
            return "application/vnd.ms-cab-compressed";
        } else if (fileName.endsWith(".car")) {
            return "application/vnd.curl.car";
        } else if (fileName.endsWith(".cat")) {
            return "application/vnd.ms-pki.seccat";
        } else if (fileName.endsWith(".cc")) {
            return "text/x-c";
        } else if (fileName.endsWith(".cct")) {
            return "application/x-director";
        } else if (fileName.endsWith(".ccxml")) {
            return "application/ccxml+xml";
        } else if (fileName.endsWith(".cdbcmsg")) {
            return "application/vnd.contact.cmsg";
        } else if (fileName.endsWith(".cdf")) {
            return "application/x-netcdf";
        } else if (fileName.endsWith(".cdkey")) {
            return "application/vnd.mediastation.cdkey";
        } else if (fileName.endsWith(".cdx")) {
            return "chemical/x-cdx";
        } else if (fileName.endsWith(".cdxml")) {
            return "application/vnd.chemdraw+xml";
        } else if (fileName.endsWith(".cdy")) {
            return "application/vnd.cinderella";
        } else if (fileName.endsWith(".cer")) {
            return "application/pkix-cert";
        } else if (fileName.endsWith(".cgm")) {
            return "image/cgm";
        } else if (fileName.endsWith(".chat")) {
            return "application/x-chat";
        } else if (fileName.endsWith(".chm")) {
            return "application/vnd.ms-htmlhelp";
        } else if (fileName.endsWith(".chrt")) {
            return "application/vnd.kde.kchart";
        } else if (fileName.endsWith(".cif")) {
            return "chemical/x-cif";
        } else if (fileName.endsWith(".cii")) {
            return "application/vnd.anser-web-certificate-issue-initiation";
        } else if (fileName.endsWith(".cil")) {
            return "application/vnd.ms-artgalry";
        } else if (fileName.endsWith(".cla")) {
            return "application/vnd.claymore";
        } else if (fileName.endsWith(".class")) {
            return "application/java-vm";
        } else if (fileName.endsWith(".clkk")) {
            return "application/vnd.crick.clicker.keyboard";
        } else if (fileName.endsWith(".clkp")) {
            return "application/vnd.crick.clicker.palette";
        } else if (fileName.endsWith(".clkt")) {
            return "application/vnd.crick.clicker.template";
        } else if (fileName.endsWith(".clkw")) {
            return "application/vnd.crick.clicker.wordbank";
        } else if (fileName.endsWith(".clkx")) {
            return "application/vnd.crick.clicker";
        } else if (fileName.endsWith(".clp")) {
            return "application/x-msclip";
        } else if (fileName.endsWith(".cmc")) {
            return "application/vnd.cosmocaller";
        } else if (fileName.endsWith(".cmdf")) {
            return "chemical/x-cmdf";
        } else if (fileName.endsWith(".cml")) {
            return "chemical/x-cml";
        } else if (fileName.endsWith(".cmp")) {
            return "application/vnd.yellowriver-custom-menu";
        } else if (fileName.endsWith(".cmx")) {
            return "image/x-cmx";
        } else if (fileName.endsWith(".cod")) {
            return "application/vnd.rim.cod";
        } else if (fileName.endsWith(".com")) {
            return "application/x-msdownload";
        } else if (fileName.endsWith(".conf")) {
            return "text/plain";
        } else if (fileName.endsWith(".cpio")) {
            return "application/x-cpio";
        } else if (fileName.endsWith(".cpp")) {
            return "text/x-c";
        } else if (fileName.endsWith(".cpt")) {
            return "application/mac-compactpro";
        } else if (fileName.endsWith(".crd")) {
            return "application/x-mscardfile";
        } else if (fileName.endsWith(".crl")) {
            return "application/pkix-crl";
        } else if (fileName.endsWith(".crt")) {
            return "application/x-x509-ca-cert";
        } else if (fileName.endsWith(".csh")) {
            return "application/x-csh";
        } else if (fileName.endsWith(".csml")) {
            return "chemical/x-csml";
        } else if (fileName.endsWith(".csp")) {
            return "application/vnd.commonspace";
        } else if (fileName.endsWith(".css")) {
            return "text/css";
        } else if (fileName.endsWith(".cst")) {
            return "application/x-director";
        } else if (fileName.endsWith(".csv")) {
            return "text/csv";
        } else if (fileName.endsWith(".cu")) {
            return "application/cu-seeme";
        } else if (fileName.endsWith(".curl")) {
            return "text/vnd.curl";
        } else if (fileName.endsWith(".cww")) {
            return "application/prs.cww";
        } else if (fileName.endsWith(".cxt")) {
            return "application/x-director";
        } else if (fileName.endsWith(".cxx")) {
            return "text/x-c";
        } else if (fileName.endsWith(".daf")) {
            return "application/vnd.mobius.daf";
        } else if (fileName.endsWith(".dataless")) {
            return "application/vnd.fdsn.seed";
        } else if (fileName.endsWith(".davmount")) {
            return "application/davmount+xml";
        } else if (fileName.endsWith(".dcr")) {
            return "application/x-director";
        } else if (fileName.endsWith(".dcurl")) {
            return "text/vnd.curl.dcurl";
        } else if (fileName.endsWith(".dd2")) {
            return "application/vnd.oma.dd2+xml";
        } else if (fileName.endsWith(".ddd")) {
            return "application/vnd.fujixerox.ddd";
        } else if (fileName.endsWith(".deb")) {
            return "application/x-debian-package";
        } else if (fileName.endsWith(".def")) {
            return "text/plain";
        } else if (fileName.endsWith(".deploy")) {
            return "application/octet-stream";
        } else if (fileName.endsWith(".der")) {
            return "application/x-x509-ca-cert";
        } else if (fileName.endsWith(".dfac")) {
            return "application/vnd.dreamfactory";
        } else if (fileName.endsWith(".dic")) {
            return "text/x-c";
        } else if (fileName.endsWith(".diff")) {
            return "text/plain";
        } else if (fileName.endsWith(".dir")) {
            return "application/x-director";
        } else if (fileName.endsWith(".dis")) {
            return "application/vnd.mobius.dis";
        } else if (fileName.endsWith(".dist")) {
            return "application/octet-stream";
        } else if (fileName.endsWith(".distz")) {
            return "application/octet-stream";
        } else if (fileName.endsWith(".djv")) {
            return "image/vnd.djvu";
        } else if (fileName.endsWith(".djvu")) {
            return "image/vnd.djvu";
        } else if (fileName.endsWith(".dll")) {
            return "application/x-msdownload";
        } else if (fileName.endsWith(".dmg")) {
            return "application/octet-stream";
        } else if (fileName.endsWith(".dms")) {
            return "application/octet-stream";
        } else if (fileName.endsWith(".dna")) {
            return "application/vnd.dna";
        } else if (fileName.endsWith(".doc")) {
            return "application/msword";
        } else if (fileName.endsWith(".docm")) {
            return "application/vnd.ms-word.document.macroenabled.12";
        } else if (fileName.endsWith(".docx")) {
            return "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
        } else if (fileName.endsWith(".dot")) {
            return "application/msword";
        } else if (fileName.endsWith(".dotm")) {
            return "application/vnd.ms-word.template.macroenabled.12";
        } else if (fileName.endsWith(".dotx")) {
            return "application/vnd.openxmlformats-officedocument.wordprocessingml.template";
        } else if (fileName.endsWith(".dp")) {
            return "application/vnd.osgi.dp";
        } else if (fileName.endsWith(".dpg")) {
            return "application/vnd.dpgraph";
        } else if (fileName.endsWith(".dsc")) {
            return "text/prs.lines.tag";
        } else if (fileName.endsWith(".dtb")) {
            return "application/x-dtbook+xml";
        } else if (fileName.endsWith(".dtd")) {
            return "application/xml-dtd";
        } else if (fileName.endsWith(".dts")) {
            return "audio/vnd.dts";
        } else if (fileName.endsWith(".dtshd")) {
            return "audio/vnd.dts.hd";
        } else if (fileName.endsWith(".dump")) {
            return "application/octet-stream";
        } else if (fileName.endsWith(".dvi")) {
            return "application/x-dvi";
        } else if (fileName.endsWith(".dwf")) {
            return "model/vnd.dwf";
        } else if (fileName.endsWith(".dwg")) {
            return "image/vnd.dwg";
        } else if (fileName.endsWith(".dxf")) {
            return "image/vnd.dxf";
        } else if (fileName.endsWith(".dxp")) {
            return "application/vnd.spotfire.dxp";
        } else if (fileName.endsWith(".dxr")) {
            return "application/x-director";
        } else if (fileName.endsWith(".ecelp4800")) {
            return "audio/vnd.nuera.ecelp4800";
        } else if (fileName.endsWith(".ecelp7470")) {
            return "audio/vnd.nuera.ecelp7470";
        } else if (fileName.endsWith(".ecelp9600")) {
            return "audio/vnd.nuera.ecelp9600";
        } else if (fileName.endsWith(".ecma")) {
            return "application/ecmascript";
        } else if (fileName.endsWith(".edm")) {
            return "application/vnd.novadigm.edm";
        } else if (fileName.endsWith(".edx")) {
            return "application/vnd.novadigm.edx";
        } else if (fileName.endsWith(".efif")) {
            return "application/vnd.picsel";
        } else if (fileName.endsWith(".ei6")) {
            return "application/vnd.pg.osasli";
        } else if (fileName.endsWith(".elc")) {
            return "application/octet-stream";
        } else if (fileName.endsWith(".eml")) {
            return "message/rfc822";
        } else if (fileName.endsWith(".emma")) {
            return "application/emma+xml";
        } else if (fileName.endsWith(".eol")) {
            return "audio/vnd.digital-winds";
        } else if (fileName.endsWith(".eot")) {
            return "application/vnd.ms-fontobject";
        } else if (fileName.endsWith(".eps")) {
            return "application/postscript";
        } else if (fileName.endsWith(".epub")) {
            return "application/epub+zip";
        } else if (fileName.endsWith(".es3")) {
            return "application/vnd.eszigno3+xml";
        } else if (fileName.endsWith(".esf")) {
            return "application/vnd.epson.esf";
        } else if (fileName.endsWith(".et3")) {
            return "application/vnd.eszigno3+xml";
        } else if (fileName.endsWith(".etx")) {
            return "text/x-setext";
        } else if (fileName.endsWith(".exe")) {
            return "application/x-msdownload";
        } else if (fileName.endsWith(".ext")) {
            return "application/vnd.novadigm.ext";
        } else if (fileName.endsWith(".ez")) {
            return "application/andrew-inset";
        } else if (fileName.endsWith(".ez2")) {
            return "application/vnd.ezpix-album";
        } else if (fileName.endsWith(".ez3")) {
            return "application/vnd.ezpix-package";
        } else if (fileName.endsWith(".f")) {
            return "text/x-fortran";
        } else if (fileName.endsWith(".f4v")) {
            return "video/x-f4v";
        } else if (fileName.endsWith(".f77")) {
            return "text/x-fortran";
        } else if (fileName.endsWith(".f90")) {
            return "text/x-fortran";
        } else if (fileName.endsWith(".fbs")) {
            return "image/vnd.fastbidsheet";
        } else if (fileName.endsWith(".fdf")) {
            return "application/vnd.fdf";
        } else if (fileName.endsWith(".fe_launch")) {
            return "application/vnd.denovo.fcselayout-link";
        } else if (fileName.endsWith(".fg5")) {
            return "application/vnd.fujitsu.oasysgp";
        } else if (fileName.endsWith(".fgd")) {
            return "application/x-director";
        } else if (fileName.endsWith(".fh")) {
            return "image/x-freehand";
        } else if (fileName.endsWith(".fh4")) {
            return "image/x-freehand";
        } else if (fileName.endsWith(".fh5")) {
            return "image/x-freehand";
        } else if (fileName.endsWith(".fh7")) {
            return "image/x-freehand";
        } else if (fileName.endsWith(".fhc")) {
            return "image/x-freehand";
        } else if (fileName.endsWith(".fig")) {
            return "application/x-xfig";
        } else if (fileName.endsWith(".fli")) {
            return "video/x-fli";
        } else if (fileName.endsWith(".flo")) {
            return "application/vnd.micrografx.flo";
        } else if (fileName.endsWith(".flv")) {
            return "video/x-flv";
        } else if (fileName.endsWith(".flw")) {
            return "application/vnd.kde.kivio";
        } else if (fileName.endsWith(".flx")) {
            return "text/vnd.fmi.flexstor";
        } else if (fileName.endsWith(".fly")) {
            return "text/vnd.fly";
        } else if (fileName.endsWith(".fm")) {
            return "application/vnd.framemaker";
        } else if (fileName.endsWith(".fnc")) {
            return "application/vnd.frogans.fnc";
        } else if (fileName.endsWith(".for")) {
            return "text/x-fortran";
        } else if (fileName.endsWith(".fpx")) {
            return "image/vnd.fpx";
        } else if (fileName.endsWith(".frame")) {
            return "application/vnd.framemaker";
        } else if (fileName.endsWith(".fsc")) {
            return "application/vnd.fsc.weblaunch";
        } else if (fileName.endsWith(".fst")) {
            return "image/vnd.fst";
        } else if (fileName.endsWith(".ftc")) {
            return "application/vnd.fluxtime.clip";
        } else if (fileName.endsWith(".fti")) {
            return "application/vnd.anser-web-funds-transfer-initiation";
        } else if (fileName.endsWith(".fvt")) {
            return "video/vnd.fvt";
        } else if (fileName.endsWith(".fzs")) {
            return "application/vnd.fuzzysheet";
        } else if (fileName.endsWith(".g3")) {
            return "image/g3fax";
        } else if (fileName.endsWith(".gac")) {
            return "application/vnd.groove-account";
        } else if (fileName.endsWith(".gdl")) {
            return "model/vnd.gdl";
        } else if (fileName.endsWith(".geo")) {
            return "application/vnd.dynageo";
        } else if (fileName.endsWith(".gex")) {
            return "application/vnd.geometry-explorer";
        } else if (fileName.endsWith(".ggb")) {
            return "application/vnd.geogebra.file";
        } else if (fileName.endsWith(".ggt")) {
            return "application/vnd.geogebra.tool";
        } else if (fileName.endsWith(".ghf")) {
            return "application/vnd.groove-help";
        } else if (fileName.endsWith(".gif")) {
            return "image/gif";
        } else if (fileName.endsWith(".gim")) {
            return "application/vnd.groove-identity-message";
        } else if (fileName.endsWith(".gmx")) {
            return "application/vnd.gmx";
        } else if (fileName.endsWith(".gnumeric")) {
            return "application/x-gnumeric";
        } else if (fileName.endsWith(".gph")) {
            return "application/vnd.flographit";
        } else if (fileName.endsWith(".gqf")) {
            return "application/vnd.grafeq";
        } else if (fileName.endsWith(".gqs")) {
            return "application/vnd.grafeq";
        } else if (fileName.endsWith(".gram")) {
            return "application/srgs";
        } else if (fileName.endsWith(".gre")) {
            return "application/vnd.geometry-explorer";
        } else if (fileName.endsWith(".grv")) {
            return "application/vnd.groove-injector";
        } else if (fileName.endsWith(".grxml")) {
            return "application/srgs+xml";
        } else if (fileName.endsWith(".gsf")) {
            return "application/x-font-ghostscript";
        } else if (fileName.endsWith(".gtar")) {
            return "application/x-gtar";
        } else if (fileName.endsWith(".gtm")) {
            return "application/vnd.groove-tool-message";
        } else if (fileName.endsWith(".gtw")) {
            return "model/vnd.gtw";
        } else if (fileName.endsWith(".gv")) {
            return "text/vnd.graphviz";
        } else if (fileName.endsWith(".gz")) {
            return "application/x-gzip";
        } else if (fileName.endsWith(".h")) {
            return "text/x-c";
        } else if (fileName.endsWith(".h261")) {
            return "video/h261";
        } else if (fileName.endsWith(".h263")) {
            return "video/h263";
        } else if (fileName.endsWith(".h264")) {
            return "video/h264";
        } else if (fileName.endsWith(".hbci")) {
            return "application/vnd.hbci";
        } else if (fileName.endsWith(".hdf")) {
            return "application/x-hdf";
        } else if (fileName.endsWith(".hh")) {
            return "text/x-c";
        } else if (fileName.endsWith(".hlp")) {
            return "application/winhlp";
        } else if (fileName.endsWith(".hpgl")) {
            return "application/vnd.hp-hpgl";
        } else if (fileName.endsWith(".hpid")) {
            return "application/vnd.hp-hpid";
        } else if (fileName.endsWith(".hps")) {
            return "application/vnd.hp-hps";
        } else if (fileName.endsWith(".hqx")) {
            return "application/mac-binhex40";
        } else if (fileName.endsWith(".htke")) {
            return "application/vnd.kenameaapp";
        } else if (fileName.endsWith(".htm")) {
            return "text/html";
        } else if (fileName.endsWith(".html")) {
            return "text/html";
        } else if (fileName.endsWith(".hvd")) {
            return "application/vnd.yamaha.hv-dic";
        } else if (fileName.endsWith(".hvp")) {
            return "application/vnd.yamaha.hv-voice";
        } else if (fileName.endsWith(".hvs")) {
            return "application/vnd.yamaha.hv-script";
        } else if (fileName.endsWith(".icc")) {
            return "application/vnd.iccprofile";
        } else if (fileName.endsWith(".ice")) {
            return "x-conference/x-cooltalk";
        } else if (fileName.endsWith(".icm")) {
            return "application/vnd.iccprofile";
        } else if (fileName.endsWith(".ico")) {
            return "image/x-icon";
        } else if (fileName.endsWith(".ics")) {
            return "text/calendar";
        } else if (fileName.endsWith(".ief")) {
            return "image/ief";
        } else if (fileName.endsWith(".ifb")) {
            return "text/calendar";
        } else if (fileName.endsWith(".ifm")) {
            return "application/vnd.shana.informed.formdata";
        } else if (fileName.endsWith(".iges")) {
            return "model/iges";
        } else if (fileName.endsWith(".igl")) {
            return "application/vnd.igloader";
        } else if (fileName.endsWith(".igs")) {
            return "model/iges";
        } else if (fileName.endsWith(".igx")) {
            return "application/vnd.micrografx.igx";
        } else if (fileName.endsWith(".iif")) {
            return "application/vnd.shana.informed.interchange";
        } else if (fileName.endsWith(".imp")) {
            return "application/vnd.accpac.simply.imp";
        } else if (fileName.endsWith(".ims")) {
            return "application/vnd.ms-ims";
        } else if (fileName.endsWith(".in")) {
            return "text/plain";
        } else if (fileName.endsWith(".ipk")) {
            return "application/vnd.shana.informed.package";
        } else if (fileName.endsWith(".irm")) {
            return "application/vnd.ibm.rights-management";
        } else if (fileName.endsWith(".irp")) {
            return "application/vnd.irepository.package+xml";
        } else if (fileName.endsWith(".iso")) {
            return "application/octet-stream";
        } else if (fileName.endsWith(".itp")) {
            return "application/vnd.shana.informed.formtemplate";
        } else if (fileName.endsWith(".ivp")) {
            return "application/vnd.immervision-ivp";
        } else if (fileName.endsWith(".ivu")) {
            return "application/vnd.immervision-ivu";
        } else if (fileName.endsWith(".jad")) {
            return "text/vnd.sun.j2me.app-descriptor";
        } else if (fileName.endsWith(".jam")) {
            return "application/vnd.jam";
        } else if (fileName.endsWith(".jar")) {
            return "application/java-archive";
        } else if (fileName.endsWith(".java")) {
            return "text/x-java-source";
        } else if (fileName.endsWith(".jisp")) {
            return "application/vnd.jisp";
        } else if (fileName.endsWith(".jlt")) {
            return "application/vnd.hp-jlyt";
        } else if (fileName.endsWith(".jnlp")) {
            return "application/x-java-jnlp-file";
        } else if (fileName.endsWith(".joda")) {
            return "application/vnd.joost.joda-archive";
        } else if (fileName.endsWith(".jpe")) {
            return "image/jpeg";
        } else if (fileName.endsWith(".jpeg")) {
            return "image/jpeg";
        } else if (fileName.endsWith(".jpg")) {
            return "image/jpeg";
        } else if (fileName.endsWith(".jpgm")) {
            return "video/jpm";
        } else if (fileName.endsWith(".jpgv")) {
            return "video/jpeg";
        } else if (fileName.endsWith(".jpm")) {
            return "video/jpm";
        } else if (fileName.endsWith(".js")) {
            return "application/javascript";
        } else if (fileName.endsWith(".json")) {
            return "application/json";
        } else if (fileName.endsWith(".kar")) {
            return "audio/midi";
        } else if (fileName.endsWith(".karbon")) {
            return "application/vnd.kde.karbon";
        } else if (fileName.endsWith(".kfo")) {
            return "application/vnd.kde.kformula";
        } else if (fileName.endsWith(".kia")) {
            return "application/vnd.kidspiration";
        } else if (fileName.endsWith(".kil")) {
            return "application/x-killustrator";
        } else if (fileName.endsWith(".kml")) {
            return "application/vnd.google-earth.kml+xml";
        } else if (fileName.endsWith(".kmz")) {
            return "application/vnd.google-earth.kmz";
        } else if (fileName.endsWith(".kne")) {
            return "application/vnd.kinar";
        } else if (fileName.endsWith(".knp")) {
            return "application/vnd.kinar";
        } else if (fileName.endsWith(".kon")) {
            return "application/vnd.kde.kontour";
        } else if (fileName.endsWith(".kpr")) {
            return "application/vnd.kde.kpresenter";
        } else if (fileName.endsWith(".kpt")) {
            return "application/vnd.kde.kpresenter";
        } else if (fileName.endsWith(".ksh")) {
            return "text/plain";
        } else if (fileName.endsWith(".ksp")) {
            return "application/vnd.kde.kspread";
        } else if (fileName.endsWith(".ktr")) {
            return "application/vnd.kahootz";
        } else if (fileName.endsWith(".ktz")) {
            return "application/vnd.kahootz";
        } else if (fileName.endsWith(".kwd")) {
            return "application/vnd.kde.kword";
        } else if (fileName.endsWith(".kwt")) {
            return "application/vnd.kde.kword";
        } else if (fileName.endsWith(".latex")) {
            return "application/x-latex";
        } else if (fileName.endsWith(".lbd")) {
            return "application/vnd.llamagraphics.life-balance.desktop";
        } else if (fileName.endsWith(".lbe")) {
            return "application/vnd.llamagraphics.life-balance.exchange+xml";
        } else if (fileName.endsWith(".les")) {
            return "application/vnd.hhe.lesson-player";
        } else if (fileName.endsWith(".lha")) {
            return "application/octet-stream";
        } else if (fileName.endsWith(".link66")) {
            return "application/vnd.route66.link66+xml";
        } else if (fileName.endsWith(".list")) {
            return "text/plain";
        } else if (fileName.endsWith(".list3820")) {
            return "application/vnd.ibm.modcap";
        } else if (fileName.endsWith(".listafp")) {
            return "application/vnd.ibm.modcap";
        } else if (fileName.endsWith(".log")) {
            return "text/plain";
        } else if (fileName.endsWith(".lostxml")) {
            return "application/lost+xml";
        } else if (fileName.endsWith(".lrf")) {
            return "application/octet-stream";
        } else if (fileName.endsWith(".lrm")) {
            return "application/vnd.ms-lrm";
        } else if (fileName.endsWith(".ltf")) {
            return "application/vnd.frogans.ltf";
        } else if (fileName.endsWith(".lvp")) {
            return "audio/vnd.lucent.voice";
        } else if (fileName.endsWith(".lwp")) {
            return "application/vnd.lotus-wordpro";
        } else if (fileName.endsWith(".lzh")) {
            return "application/octet-stream";
        } else if (fileName.endsWith(".m13")) {
            return "application/x-msmediaview";
        } else if (fileName.endsWith(".m14")) {
            return "application/x-msmediaview";
        } else if (fileName.endsWith(".m1v")) {
            return "video/mpeg";
        } else if (fileName.endsWith(".m2a")) {
            return "audio/mpeg";
        } else if (fileName.endsWith(".m2v")) {
            return "video/mpeg";
        } else if (fileName.endsWith(".m3a")) {
            return "audio/mpeg";
        } else if (fileName.endsWith(".m3u")) {
            return "audio/x-mpegurl";
        } else if (fileName.endsWith(".m4u")) {
            return "video/vnd.mpegurl";
        } else if (fileName.endsWith(".m4v")) {
            return "video/x-m4v";
        } else if (fileName.endsWith(".ma")) {
            return "application/mathematica";
        } else if (fileName.endsWith(".mag")) {
            return "application/vnd.ecowin.chart";
        } else if (fileName.endsWith(".maker")) {
            return "application/vnd.framemaker";
        } else if (fileName.endsWith(".man")) {
            return "text/troff";
        } else if (fileName.endsWith(".mathml")) {
            return "application/mathml+xml";
        } else if (fileName.endsWith(".mb")) {
            return "application/mathematica";
        } else if (fileName.endsWith(".mbk")) {
            return "application/vnd.mobius.mbk";
        } else if (fileName.endsWith(".mbox")) {
            return "application/mbox";
        } else if (fileName.endsWith(".mc1")) {
            return "application/vnd.medcalcdata";
        } else if (fileName.endsWith(".mcd")) {
            return "application/vnd.mcd";
        } else if (fileName.endsWith(".mcurl")) {
            return "text/vnd.curl.mcurl";
        } else if (fileName.endsWith(".mdb")) {
            return "application/x-msaccess";
        } else if (fileName.endsWith(".mdi")) {
            return "image/vnd.ms-modi";
        } else if (fileName.endsWith(".me")) {
            return "text/troff";
        } else if (fileName.endsWith(".mesh")) {
            return "model/mesh";
        } else if (fileName.endsWith(".mfm")) {
            return "application/vnd.mfmp";
        } else if (fileName.endsWith(".mgz")) {
            return "application/vnd.proteus.magazine";
        } else if (fileName.endsWith(".mht")) {
            return "message/rfc822";
        } else if (fileName.endsWith(".mhtml")) {
            return "message/rfc822";
        } else if (fileName.endsWith(".mid")) {
            return "audio/midi";
        } else if (fileName.endsWith(".midi")) {
            return "audio/midi";
        } else if (fileName.endsWith(".mif")) {
            return "application/vnd.mif";
        } else if (fileName.endsWith(".mime")) {
            return "message/rfc822";
        } else if (fileName.endsWith(".mj2")) {
            return "video/mj2";
        } else if (fileName.endsWith(".mjp2")) {
            return "video/mj2";
        } else if (fileName.endsWith(".mlp")) {
            return "application/vnd.dolby.mlp";
        } else if (fileName.endsWith(".mmd")) {
            return "application/vnd.chipnuts.karaoke-mmd";
        } else if (fileName.endsWith(".mmf")) {
            return "application/vnd.smaf";
        } else if (fileName.endsWith(".mmr")) {
            return "image/vnd.fujixerox.edmics-mmr";
        } else if (fileName.endsWith(".mny")) {
            return "application/x-msmoney";
        } else if (fileName.endsWith(".mobi")) {
            return "application/x-mobipocket-ebook";
        } else if (fileName.endsWith(".mov")) {
            return "video/quicktime";
        } else if (fileName.endsWith(".movie")) {
            return "video/x-sgi-movie";
        } else if (fileName.endsWith(".mp2")) {
            return "audio/mpeg";
        } else if (fileName.endsWith(".mp2a")) {
            return "audio/mpeg";
        } else if (fileName.endsWith(".mp3")) {
            return "audio/mpeg";
        } else if (fileName.endsWith(".mp4")) {
            return "video/mp4";
        } else if (fileName.endsWith(".mp4a")) {
            return "audio/mp4";
        } else if (fileName.endsWith(".mp4s")) {
            return "application/mp4";
        } else if (fileName.endsWith(".mp4v")) {
            return "video/mp4";
        } else if (fileName.endsWith(".mpa")) {
            return "video/mpeg";
        } else if (fileName.endsWith(".mpc")) {
            return "application/vnd.mophun.certificate";
        } else if (fileName.endsWith(".mpe")) {
            return "video/mpeg";
        } else if (fileName.endsWith(".mpeg")) {
            return "video/mpeg";
        } else if (fileName.endsWith(".mpg")) {
            return "video/mpeg";
        } else if (fileName.endsWith(".mpg4")) {
            return "video/mp4";
        } else if (fileName.endsWith(".mpga")) {
            return "audio/mpeg";
        } else if (fileName.endsWith(".mpkg")) {
            return "application/vnd.apple.installer+xml";
        } else if (fileName.endsWith(".mpm")) {
            return "application/vnd.blueice.multipass";
        } else if (fileName.endsWith(".mpn")) {
            return "application/vnd.mophun.application";
        } else if (fileName.endsWith(".mpp")) {
            return "application/vnd.ms-project";
        } else if (fileName.endsWith(".mpt")) {
            return "application/vnd.ms-project";
        } else if (fileName.endsWith(".mpy")) {
            return "application/vnd.ibm.minipay";
        } else if (fileName.endsWith(".mqy")) {
            return "application/vnd.mobius.mqy";
        } else if (fileName.endsWith(".mrc")) {
            return "application/marc";
        } else if (fileName.endsWith(".ms")) {
            return "text/troff";
        } else if (fileName.endsWith(".mscml")) {
            return "application/mediaservercontrol+xml";
        } else if (fileName.endsWith(".mseed")) {
            return "application/vnd.fdsn.mseed";
        } else if (fileName.endsWith(".mseq")) {
            return "application/vnd.mseq";
        } else if (fileName.endsWith(".msf")) {
            return "application/vnd.epson.msf";
        } else if (fileName.endsWith(".msh")) {
            return "model/mesh";
        } else if (fileName.endsWith(".msi")) {
            return "application/x-msdownload";
        } else if (fileName.endsWith(".msl")) {
            return "application/vnd.mobius.msl";
        } else if (fileName.endsWith(".msty")) {
            return "application/vnd.muvee.style";
        } else if (fileName.endsWith(".mts")) {
            return "model/vnd.mts";
        } else if (fileName.endsWith(".mus")) {
            return "application/vnd.musician";
        } else if (fileName.endsWith(".musicxml")) {
            return "application/vnd.recordare.musicxml+xml";
        } else if (fileName.endsWith(".mvb")) {
            return "application/x-msmediaview";
        } else if (fileName.endsWith(".mwf")) {
            return "application/vnd.mfer";
        } else if (fileName.endsWith(".mxf")) {
            return "application/mxf";
        } else if (fileName.endsWith(".mxl")) {
            return "application/vnd.recordare.musicxml";
        } else if (fileName.endsWith(".mxml")) {
            return "application/xv+xml";
        } else if (fileName.endsWith(".mxs")) {
            return "application/vnd.triscape.mxs";
        } else if (fileName.endsWith(".mxu")) {
            return "video/vnd.mpegurl";
        } else if (fileName.endsWith(".n-gage")) {
            return "application/vnd.nokia.n-gage.symbian.install";
        } else if (fileName.endsWith(".nb")) {
            return "application/mathematica";
        } else if (fileName.endsWith(".nc")) {
            return "application/x-netcdf";
        } else if (fileName.endsWith(".ncx")) {
            return "application/x-dtbncx+xml";
        } else if (fileName.endsWith(".ngdat")) {
            return "application/vnd.nokia.n-gage.data";
        } else if (fileName.endsWith(".nlu")) {
            return "application/vnd.neurolanguage.nlu";
        } else if (fileName.endsWith(".nml")) {
            return "application/vnd.enliven";
        } else if (fileName.endsWith(".nnd")) {
            return "application/vnd.noblenet-directory";
        } else if (fileName.endsWith(".nns")) {
            return "application/vnd.noblenet-sealer";
        } else if (fileName.endsWith(".nnw")) {
            return "application/vnd.noblenet-web";
        } else if (fileName.endsWith(".npx")) {
            return "image/vnd.net-fpx";
        } else if (fileName.endsWith(".nsf")) {
            return "application/vnd.lotus-notes";
        } else if (fileName.endsWith(".nws")) {
            return "message/rfc822";
        } else if (fileName.endsWith(".o")) {
            return "application/octet-stream";
        } else if (fileName.endsWith(".oa2")) {
            return "application/vnd.fujitsu.oasys2";
        } else if (fileName.endsWith(".oa3")) {
            return "application/vnd.fujitsu.oasys3";
        } else if (fileName.endsWith(".oas")) {
            return "application/vnd.fujitsu.oasys";
        } else if (fileName.endsWith(".obd")) {
            return "application/x-msbinder";
        } else if (fileName.endsWith(".obj")) {
            return "application/octet-stream";
        } else if (fileName.endsWith(".oda")) {
            return "application/oda";
        } else if (fileName.endsWith(".odb")) {
            return "application/vnd.oasis.opendocument.database";
        } else if (fileName.endsWith(".odc")) {
            return "application/vnd.oasis.opendocument.chart";
        } else if (fileName.endsWith(".odf")) {
            return "application/vnd.oasis.opendocument.formula";
        } else if (fileName.endsWith(".odft")) {
            return "application/vnd.oasis.opendocument.formula-template";
        } else if (fileName.endsWith(".odg")) {
            return "application/vnd.oasis.opendocument.graphics";
        } else if (fileName.endsWith(".odi")) {
            return "application/vnd.oasis.opendocument.image";
        } else if (fileName.endsWith(".odp")) {
            return "application/vnd.oasis.opendocument.presentation";
        } else if (fileName.endsWith(".ods")) {
            return "application/vnd.oasis.opendocument.spreadsheet";
        } else if (fileName.endsWith(".odt")) {
            return "application/vnd.oasis.opendocument.text";
        } else if (fileName.endsWith(".oga")) {
            return "audio/ogg";
        } else if (fileName.endsWith(".ogg")) {
            return "audio/ogg";
        } else if (fileName.endsWith(".ogv")) {
            return "video/ogg";
        } else if (fileName.endsWith(".ogx")) {
            return "application/ogg";
        } else if (fileName.endsWith(".onepkg")) {
            return "application/onenote";
        } else if (fileName.endsWith(".onetmp")) {
            return "application/onenote";
        } else if (fileName.endsWith(".onetoc")) {
            return "application/onenote";
        } else if (fileName.endsWith(".onetoc2")) {
            return "application/onenote";
        } else if (fileName.endsWith(".opf")) {
            return "application/oebps-package+xml";
        } else if (fileName.endsWith(".oprc")) {
            return "application/vnd.palm";
        } else if (fileName.endsWith(".org")) {
            return "application/vnd.lotus-organizer";
        } else if (fileName.endsWith(".osf")) {
            return "application/vnd.yamaha.openscoreformat";
        } else if (fileName.endsWith(".osfpvg")) {
            return "application/vnd.yamaha.openscoreformat.osfpvg+xml";
        } else if (fileName.endsWith(".otc")) {
            return "application/vnd.oasis.opendocument.chart-template";
        } else if (fileName.endsWith(".otf")) {
            return "application/x-font-otf";
        } else if (fileName.endsWith(".otg")) {
            return "application/vnd.oasis.opendocument.graphics-template";
        } else if (fileName.endsWith(".oth")) {
            return "application/vnd.oasis.opendocument.text-web";
        } else if (fileName.endsWith(".oti")) {
            return "application/vnd.oasis.opendocument.image-template";
        } else if (fileName.endsWith(".otm")) {
            return "application/vnd.oasis.opendocument.text-master";
        } else if (fileName.endsWith(".otp")) {
            return "application/vnd.oasis.opendocument.presentation-template";
        } else if (fileName.endsWith(".ots")) {
            return "application/vnd.oasis.opendocument.spreadsheet-template";
        } else if (fileName.endsWith(".ott")) {
            return "application/vnd.oasis.opendocument.text-template";
        } else if (fileName.endsWith(".oxt")) {
            return "application/vnd.openofficeorg.extension";
        } else if (fileName.endsWith(".p")) {
            return "text/x-pascal";
        } else if (fileName.endsWith(".p10")) {
            return "application/pkcs10";
        } else if (fileName.endsWith(".p12")) {
            return "application/x-pkcs12";
        } else if (fileName.endsWith(".p7b")) {
            return "application/x-pkcs7-certificates";
        } else if (fileName.endsWith(".p7c")) {
            return "application/pkcs7-mime";
        } else if (fileName.endsWith(".p7m")) {
            return "application/pkcs7-mime";
        } else if (fileName.endsWith(".p7r")) {
            return "application/x-pkcs7-certreqresp";
        } else if (fileName.endsWith(".p7s")) {
            return "application/pkcs7-signature";
        } else if (fileName.endsWith(".pas")) {
            return "text/x-pascal";
        } else if (fileName.endsWith(".pbd")) {
            return "application/vnd.powerbuilder6";
        } else if (fileName.endsWith(".pbm")) {
            return "image/x-portable-bitmap";
        } else if (fileName.endsWith(".pcf")) {
            return "application/x-font-pcf";
        } else if (fileName.endsWith(".pcl")) {
            return "application/vnd.hp-pcl";
        } else if (fileName.endsWith(".pclxl")) {
            return "application/vnd.hp-pclxl";
        } else if (fileName.endsWith(".pct")) {
            return "image/x-pict";
        } else if (fileName.endsWith(".pcurl")) {
            return "application/vnd.curl.pcurl";
        } else if (fileName.endsWith(".pcx")) {
            return "image/x-pcx";
        } else if (fileName.endsWith(".pdb")) {
            return "application/vnd.palm";
        } else if (fileName.endsWith(".pdf")) {
            return "application/pdf";
        } else if (fileName.endsWith(".pfa")) {
            return "application/x-font-type1";
        } else if (fileName.endsWith(".pfb")) {
            return "application/x-font-type1";
        } else if (fileName.endsWith(".pfm")) {
            return "application/x-font-type1";
        } else if (fileName.endsWith(".pfr")) {
            return "application/font-tdpfr";
        } else if (fileName.endsWith(".pfx")) {
            return "application/x-pkcs12";
        } else if (fileName.endsWith(".pgm")) {
            return "image/x-portable-graymap";
        } else if (fileName.endsWith(".pgn")) {
            return "application/x-chess-pgn";
        } else if (fileName.endsWith(".pgp")) {
            return "application/pgp-encrypted";
        } else if (fileName.endsWith(".pic")) {
            return "image/x-pict";
        } else if (fileName.endsWith(".pkg")) {
            return "application/octet-stream";
        } else if (fileName.endsWith(".pki")) {
            return "application/pkixcmp";
        } else if (fileName.endsWith(".pkipath")) {
            return "application/pkix-pkipath";
        } else if (fileName.endsWith(".pl")) {
            return "text/plain";
        } else if (fileName.endsWith(".plb")) {
            return "application/vnd.3gpp.pic-bw-large";
        } else if (fileName.endsWith(".plc")) {
            return "application/vnd.mobius.plc";
        } else if (fileName.endsWith(".plf")) {
            return "application/vnd.pocketlearn";
        } else if (fileName.endsWith(".pls")) {
            return "application/pls+xml";
        } else if (fileName.endsWith(".pml")) {
            return "application/vnd.ctc-posml";
        } else if (fileName.endsWith(".png")) {
            return "image/png";
        } else if (fileName.endsWith(".pnm")) {
            return "image/x-portable-anymap";
        } else if (fileName.endsWith(".portpkg")) {
            return "application/vnd.macports.portpkg";
        } else if (fileName.endsWith(".pot")) {
            return "application/vnd.ms-powerpoint";
        } else if (fileName.endsWith(".potm")) {
            return "application/vnd.ms-powerpoint.template.macroenabled.12";
        } else if (fileName.endsWith(".potx")) {
            return "application/vnd.openxmlformats-officedocument.presentationml.template";
        } else if (fileName.endsWith(".ppa")) {
            return "application/vnd.ms-powerpoint";
        } else if (fileName.endsWith(".ppam")) {
            return "application/vnd.ms-powerpoint.addin.macroenabled.12";
        } else if (fileName.endsWith(".ppd")) {
            return "application/vnd.cups-ppd";
        } else if (fileName.endsWith(".ppm")) {
            return "image/x-portable-pixmap";
        } else if (fileName.endsWith(".pps")) {
            return "application/vnd.ms-powerpoint";
        } else if (fileName.endsWith(".ppsm")) {
            return "application/vnd.ms-powerpoint.slideshow.macroenabled.12";
        } else if (fileName.endsWith(".ppsx")) {
            return "application/vnd.openxmlformats-officedocument.presentationml.slideshow";
        } else if (fileName.endsWith(".ppt")) {
            return "application/vnd.ms-powerpoint";
        } else if (fileName.endsWith(".pptm")) {
            return "application/vnd.ms-powerpoint.presentation.macroenabled.12";
        } else if (fileName.endsWith(".pptx")) {
            return "application/vnd.openxmlformats-officedocument.presentationml.presentation";
        } else if (fileName.endsWith(".pqa")) {
            return "application/vnd.palm";
        } else if (fileName.endsWith(".prc")) {
            return "application/x-mobipocket-ebook";
        } else if (fileName.endsWith(".pre")) {
            return "application/vnd.lotus-freelance";
        } else if (fileName.endsWith(".prf")) {
            return "application/pics-rules";
        } else if (fileName.endsWith(".ps")) {
            return "application/postscript";
        } else if (fileName.endsWith(".psb")) {
            return "application/vnd.3gpp.pic-bw-small";
        } else if (fileName.endsWith(".psd")) {
            return "image/vnd.adobe.photoshop";
        } else if (fileName.endsWith(".psf")) {
            return "application/x-font-linux-psf";
        } else if (fileName.endsWith(".ptid")) {
            return "application/vnd.pvi.ptid1";
        } else if (fileName.endsWith(".pub")) {
            return "application/x-mspublisher";
        } else if (fileName.endsWith(".pvb")) {
            return "application/vnd.3gpp.pic-bw-var";
        } else if (fileName.endsWith(".pwn")) {
            return "application/vnd.3m.post-it-notes";
        } else if (fileName.endsWith(".pwz")) {
            return "application/vnd.ms-powerpoint";
        } else if (fileName.endsWith(".py")) {
            return "text/x-python";
        } else if (fileName.endsWith(".pya")) {
            return "audio/vnd.ms-playready.media.pya";
        } else if (fileName.endsWith(".pyc")) {
            return "application/x-python-code";
        } else if (fileName.endsWith(".pyo")) {
            return "application/x-python-code";
        } else if (fileName.endsWith(".pyv")) {
            return "video/vnd.ms-playready.media.pyv";
        } else if (fileName.endsWith(".qam")) {
            return "application/vnd.epson.quickanime";
        } else if (fileName.endsWith(".qbo")) {
            return "application/vnd.intu.qbo";
        } else if (fileName.endsWith(".qfx")) {
            return "application/vnd.intu.qfx";
        } else if (fileName.endsWith(".qps")) {
            return "application/vnd.publishare-delta-tree";
        } else if (fileName.endsWith(".qt")) {
            return "video/quicktime";
        } else if (fileName.endsWith(".qwd")) {
            return "application/vnd.quark.quarkxpress";
        } else if (fileName.endsWith(".qwt")) {
            return "application/vnd.quark.quarkxpress";
        } else if (fileName.endsWith(".qxb")) {
            return "application/vnd.quark.quarkxpress";
        } else if (fileName.endsWith(".qxd")) {
            return "application/vnd.quark.quarkxpress";
        } else if (fileName.endsWith(".qxl")) {
            return "application/vnd.quark.quarkxpress";
        } else if (fileName.endsWith(".qxt")) {
            return "application/vnd.quark.quarkxpress";
        } else if (fileName.endsWith(".ra")) {
            return "audio/x-pn-realaudio";
        } else if (fileName.endsWith(".ram")) {
            return "audio/x-pn-realaudio";
        } else if (fileName.endsWith(".rar")) {
            return "application/x-rar-compressed";
        } else if (fileName.endsWith(".ras")) {
            return "image/x-cmu-raster";
        } else if (fileName.endsWith(".rcprofile")) {
            return "application/vnd.ipunplugged.rcprofile";
        } else if (fileName.endsWith(".rdf")) {
            return "application/rdf+xml";
        } else if (fileName.endsWith(".rdz")) {
            return "application/vnd.data-vision.rdz";
        } else if (fileName.endsWith(".rep")) {
            return "application/vnd.businessobjects";
        } else if (fileName.endsWith(".res")) {
            return "application/x-dtbresource+xml";
        } else if (fileName.endsWith(".rgb")) {
            return "image/x-rgb";
        } else if (fileName.endsWith(".rif")) {
            return "application/reginfo+xml";
        } else if (fileName.endsWith(".rl")) {
            return "application/resource-lists+xml";
        } else if (fileName.endsWith(".rlc")) {
            return "image/vnd.fujixerox.edmics-rlc";
        } else if (fileName.endsWith(".rld")) {
            return "application/resource-lists-diff+xml";
        } else if (fileName.endsWith(".rm")) {
            return "application/vnd.rn-realmedia";
        } else if (fileName.endsWith(".rmi")) {
            return "audio/midi";
        } else if (fileName.endsWith(".rmp")) {
            return "audio/x-pn-realaudio-plugin";
        } else if (fileName.endsWith(".rms")) {
            return "application/vnd.jcp.javame.midlet-rms";
        } else if (fileName.endsWith(".rnc")) {
            return "application/relax-ng-compact-syntax";
        } else if (fileName.endsWith(".roff")) {
            return "text/troff";
        } else if (fileName.endsWith(".rpm")) {
            return "application/x-rpm";
        } else if (fileName.endsWith(".rpss")) {
            return "application/vnd.nokia.radio-presets";
        } else if (fileName.endsWith(".rpst")) {
            return "application/vnd.nokia.radio-preset";
        } else if (fileName.endsWith(".rq")) {
            return "application/sparql-query";
        } else if (fileName.endsWith(".rs")) {
            return "application/rls-services+xml";
        } else if (fileName.endsWith(".rsd")) {
            return "application/rsd+xml";
        } else if (fileName.endsWith(".rss")) {
            return "application/rss+xml";
        } else if (fileName.endsWith(".rtf")) {
            return "application/rtf";
        } else if (fileName.endsWith(".rtx")) {
            return "text/richtext";
        } else if (fileName.endsWith(".s")) {
            return "text/x-asm";
        } else if (fileName.endsWith(".saf")) {
            return "application/vnd.yamaha.smaf-audio";
        } else if (fileName.endsWith(".sbml")) {
            return "application/sbml+xml";
        } else if (fileName.endsWith(".sc")) {
            return "application/vnd.ibm.secure-container";
        } else if (fileName.endsWith(".scd")) {
            return "application/x-msschedule";
        } else if (fileName.endsWith(".scm")) {
            return "application/vnd.lotus-screencam";
        } else if (fileName.endsWith(".scq")) {
            return "application/scvp-cv-request";
        } else if (fileName.endsWith(".scs")) {
            return "application/scvp-cv-response";
        } else if (fileName.endsWith(".scurl")) {
            return "text/vnd.curl.scurl";
        } else if (fileName.endsWith(".sda")) {
            return "application/vnd.stardivision.draw";
        } else if (fileName.endsWith(".sdc")) {
            return "application/vnd.stardivision.calc";
        } else if (fileName.endsWith(".sdd")) {
            return "application/vnd.stardivision.impress";
        } else if (fileName.endsWith(".sdkd")) {
            return "application/vnd.solent.sdkm+xml";
        } else if (fileName.endsWith(".sdkm")) {
            return "application/vnd.solent.sdkm+xml";
        } else if (fileName.endsWith(".sdp")) {
            return "application/sdp";
        } else if (fileName.endsWith(".sdw")) {
            return "application/vnd.stardivision.writer";
        } else if (fileName.endsWith(".see")) {
            return "application/vnd.seemail";
        } else if (fileName.endsWith(".seed")) {
            return "application/vnd.fdsn.seed";
        } else if (fileName.endsWith(".sema")) {
            return "application/vnd.sema";
        } else if (fileName.endsWith(".semd")) {
            return "application/vnd.semd";
        } else if (fileName.endsWith(".semf")) {
            return "application/vnd.semf";
        } else if (fileName.endsWith(".ser")) {
            return "application/java-serialized-object";
        } else if (fileName.endsWith(".setpay")) {
            return "application/set-payment-initiation";
        } else if (fileName.endsWith(".setreg")) {
            return "application/set-registration-initiation";
        } else if (fileName.endsWith(".sfd-hdstx")) {
            return "application/vnd.hydrostatix.sof-data";
        } else if (fileName.endsWith(".sfs")) {
            return "application/vnd.spotfire.sfs";
        } else if (fileName.endsWith(".sgl")) {
            return "application/vnd.stardivision.writer-global";
        } else if (fileName.endsWith(".sgm")) {
            return "text/sgml";
        } else if (fileName.endsWith(".sgml")) {
            return "text/sgml";
        } else if (fileName.endsWith(".sh")) {
            return "application/x-sh";
        } else if (fileName.endsWith(".shar")) {
            return "application/x-shar";
        } else if (fileName.endsWith(".shf")) {
            return "application/shf+xml";
        } else if (fileName.endsWith(".si")) {
            return "text/vnd.wap.si";
        } else if (fileName.endsWith(".sic")) {
            return "application/vnd.wap.sic";
        } else if (fileName.endsWith(".sig")) {
            return "application/pgp-signature";
        } else if (fileName.endsWith(".silo")) {
            return "model/mesh";
        } else if (fileName.endsWith(".sis")) {
            return "application/vnd.symbian.install";
        } else if (fileName.endsWith(".sisx")) {
            return "application/vnd.symbian.install";
        } else if (fileName.endsWith(".sit")) {
            return "application/x-stuffit";
        } else if (fileName.endsWith(".sitx")) {
            return "application/x-stuffitx";
        } else if (fileName.endsWith(".skd")) {
            return "application/vnd.koan";
        } else if (fileName.endsWith(".skm")) {
            return "application/vnd.koan";
        } else if (fileName.endsWith(".skp")) {
            return "application/vnd.koan";
        } else if (fileName.endsWith(".skt")) {
            return "application/vnd.koan";
        } else if (fileName.endsWith(".sl")) {
            return "text/vnd.wap.sl";
        } else if (fileName.endsWith(".slc")) {
            return "application/vnd.wap.slc";
        } else if (fileName.endsWith(".sldm")) {
            return "application/vnd.ms-powerpoint.slide.macroenabled.12";
        } else if (fileName.endsWith(".sldx")) {
            return "application/vnd.openxmlformats-officedocument.presentationml.slide";
        } else if (fileName.endsWith(".slt")) {
            return "application/vnd.epson.salt";
        } else if (fileName.endsWith(".smf")) {
            return "application/vnd.stardivision.math";
        } else if (fileName.endsWith(".smi")) {
            return "application/smil+xml";
        } else if (fileName.endsWith(".smil")) {
            return "application/smil+xml";
        } else if (fileName.endsWith(".snd")) {
            return "audio/basic";
        } else if (fileName.endsWith(".snf")) {
            return "application/x-font-snf";
        } else if (fileName.endsWith(".so")) {
            return "application/octet-stream";
        } else if (fileName.endsWith(".spc")) {
            return "application/x-pkcs7-certificates";
        } else if (fileName.endsWith(".spf")) {
            return "application/vnd.yamaha.smaf-phrase";
        } else if (fileName.endsWith(".spl")) {
            return "application/x-futuresplash";
        } else if (fileName.endsWith(".spot")) {
            return "text/vnd.in3d.spot";
        } else if (fileName.endsWith(".spp")) {
            return "application/scvp-vp-response";
        } else if (fileName.endsWith(".spq")) {
            return "application/scvp-vp-request";
        } else if (fileName.endsWith(".spx")) {
            return "audio/ogg";
        } else if (fileName.endsWith(".src")) {
            return "application/x-wais-source";
        } else if (fileName.endsWith(".srx")) {
            return "application/sparql-results+xml";
        } else if (fileName.endsWith(".sse")) {
            return "application/vnd.kodak-descriptor";
        } else if (fileName.endsWith(".ssf")) {
            return "application/vnd.epson.ssf";
        } else if (fileName.endsWith(".ssml")) {
            return "application/ssml+xml";
        } else if (fileName.endsWith(".stc")) {
            return "application/vnd.sun.xml.calc.template";
        } else if (fileName.endsWith(".std")) {
            return "application/vnd.sun.xml.draw.template";
        } else if (fileName.endsWith(".stf")) {
            return "application/vnd.wt.stf";
        } else if (fileName.endsWith(".sti")) {
            return "application/vnd.sun.xml.impress.template";
        } else if (fileName.endsWith(".stk")) {
            return "application/hyperstudio";
        } else if (fileName.endsWith(".stl")) {
            return "application/vnd.ms-pki.stl";
        } else if (fileName.endsWith(".str")) {
            return "application/vnd.pg.format";
        } else if (fileName.endsWith(".stw")) {
            return "application/vnd.sun.xml.writer.template";
        } else if (fileName.endsWith(".sus")) {
            return "application/vnd.sus-calendar";
        } else if (fileName.endsWith(".susp")) {
            return "application/vnd.sus-calendar";
        } else if (fileName.endsWith(".sv4cpio")) {
            return "application/x-sv4cpio";
        } else if (fileName.endsWith(".sv4crc")) {
            return "application/x-sv4crc";
        } else if (fileName.endsWith(".svd")) {
            return "application/vnd.svd";
        } else if (fileName.endsWith(".svg")) {
            return "image/svg+xml";
        } else if (fileName.endsWith(".svgz")) {
            return "image/svg+xml";
        } else if (fileName.endsWith(".swa")) {
            return "application/x-director";
        } else if (fileName.endsWith(".swf")) {
            return "application/x-shockwave-flash";
        } else if (fileName.endsWith(".swi")) {
            return "application/vnd.arastra.swi";
        } else if (fileName.endsWith(".sxc")) {
            return "application/vnd.sun.xml.calc";
        } else if (fileName.endsWith(".sxd")) {
            return "application/vnd.sun.xml.draw";
        } else if (fileName.endsWith(".sxg")) {
            return "application/vnd.sun.xml.writer.global";
        } else if (fileName.endsWith(".sxi")) {
            return "application/vnd.sun.xml.impress";
        } else if (fileName.endsWith(".sxm")) {
            return "application/vnd.sun.xml.math";
        } else if (fileName.endsWith(".sxw")) {
            return "application/vnd.sun.xml.writer";
        } else if (fileName.endsWith(".t")) {
            return "text/troff";
        } else if (fileName.endsWith(".tao")) {
            return "application/vnd.tao.intent-module-archive";
        } else if (fileName.endsWith(".tar")) {
            return "application/x-tar";
        } else if (fileName.endsWith(".tcap")) {
            return "application/vnd.3gpp2.tcap";
        } else if (fileName.endsWith(".tcl")) {
            return "application/x-tcl";
        } else if (fileName.endsWith(".teacher")) {
            return "application/vnd.smart.teacher";
        } else if (fileName.endsWith(".tex")) {
            return "application/x-tex";
        } else if (fileName.endsWith(".texi")) {
            return "application/x-texinfo";
        } else if (fileName.endsWith(".texinfo")) {
            return "application/x-texinfo";
        } else if (fileName.endsWith(".text")) {
            return "text/plain";
        } else if (fileName.endsWith(".tfm")) {
            return "application/x-tex-tfm";
        } else if (fileName.endsWith(".tgz")) {
            return "application/x-gzip";
        } else if (fileName.endsWith(".tif")) {
            return "image/tiff";
        } else if (fileName.endsWith(".tiff")) {
            return "image/tiff";
        } else if (fileName.endsWith(".tmo")) {
            return "application/vnd.tmobile-livetv";
        } else if (fileName.endsWith(".torrent")) {
            return "application/x-bittorrent";
        } else if (fileName.endsWith(".tpl")) {
            return "application/vnd.groove-tool-template";
        } else if (fileName.endsWith(".tpt")) {
            return "application/vnd.trid.tpt";
        } else if (fileName.endsWith(".tr")) {
            return "text/troff";
        } else if (fileName.endsWith(".tra")) {
            return "application/vnd.trueapp";
        } else if (fileName.endsWith(".trm")) {
            return "application/x-msterminal";
        } else if (fileName.endsWith(".tsv")) {
            return "text/tab-separated-values";
        } else if (fileName.endsWith(".ttc")) {
            return "application/x-font-ttf";
        } else if (fileName.endsWith(".ttf")) {
            return "application/x-font-ttf";
        } else if (fileName.endsWith(".twd")) {
            return "application/vnd.simtech-mindmapper";
        } else if (fileName.endsWith(".twds")) {
            return "application/vnd.simtech-mindmapper";
        } else if (fileName.endsWith(".txd")) {
            return "application/vnd.genomatix.tuxedo";
        } else if (fileName.endsWith(".txf")) {
            return "application/vnd.mobius.txf";
        } else if (fileName.endsWith(".txt")) {
            return "text/plain";
        } else if (fileName.endsWith(".u32")) {
            return "application/x-authorware-bin";
        } else if (fileName.endsWith(".udeb")) {
            return "application/x-debian-package";
        } else if (fileName.endsWith(".ufd")) {
            return "application/vnd.ufdl";
        } else if (fileName.endsWith(".ufdl")) {
            return "application/vnd.ufdl";
        } else if (fileName.endsWith(".umj")) {
            return "application/vnd.umajin";
        } else if (fileName.endsWith(".unityweb")) {
            return "application/vnd.unity";
        } else if (fileName.endsWith(".uoml")) {
            return "application/vnd.uoml+xml";
        } else if (fileName.endsWith(".uri")) {
            return "text/uri-list";
        } else if (fileName.endsWith(".uris")) {
            return "text/uri-list";
        } else if (fileName.endsWith(".urls")) {
            return "text/uri-list";
        } else if (fileName.endsWith(".ustar")) {
            return "application/x-ustar";
        } else if (fileName.endsWith(".utz")) {
            return "application/vnd.uiq.theme";
        } else if (fileName.endsWith(".uu")) {
            return "text/x-uuencode";
        } else if (fileName.endsWith(".vcd")) {
            return "application/x-cdlink";
        } else if (fileName.endsWith(".vcf")) {
            return "text/x-vcard";
        } else if (fileName.endsWith(".vcg")) {
            return "application/vnd.groove-vcard";
        } else if (fileName.endsWith(".vcs")) {
            return "text/x-vcalendar";
        } else if (fileName.endsWith(".vcx")) {
            return "application/vnd.vcx";
        } else if (fileName.endsWith(".vis")) {
            return "application/vnd.visionary";
        } else if (fileName.endsWith(".viv")) {
            return "video/vnd.vivo";
        } else if (fileName.endsWith(".vor")) {
            return "application/vnd.stardivision.writer";
        } else if (fileName.endsWith(".vox")) {
            return "application/x-authorware-bin";
        } else if (fileName.endsWith(".vrml")) {
            return "model/vrml";
        } else if (fileName.endsWith(".vsd")) {
            return "application/vnd.visio";
        } else if (fileName.endsWith(".vsf")) {
            return "application/vnd.vsf";
        } else if (fileName.endsWith(".vss")) {
            return "application/vnd.visio";
        } else if (fileName.endsWith(".vst")) {
            return "application/vnd.visio";
        } else if (fileName.endsWith(".vsw")) {
            return "application/vnd.visio";
        } else if (fileName.endsWith(".vtu")) {
            return "model/vnd.vtu";
        } else if (fileName.endsWith(".vxml")) {
            return "application/voicexml+xml";
        } else if (fileName.endsWith(".w3d")) {
            return "application/x-director";
        } else if (fileName.endsWith(".wad")) {
            return "application/x-doom";
        } else if (fileName.endsWith(".wav")) {
            return "audio/x-wav";
        } else if (fileName.endsWith(".wax")) {
            return "audio/x-ms-wax";
        } else if (fileName.endsWith(".wbmp")) {
            return "image/vnd.wap.wbmp";
        } else if (fileName.endsWith(".wbs")) {
            return "application/vnd.criticaltools.wbs+xml";
        } else if (fileName.endsWith(".wbxml")) {
            return "application/vnd.wap.wbxml";
        } else if (fileName.endsWith(".wcm")) {
            return "application/vnd.ms-works";
        } else if (fileName.endsWith(".wdb")) {
            return "application/vnd.ms-works";
        } else if (fileName.endsWith(".wiz")) {
            return "application/msword";
        } else if (fileName.endsWith(".wks")) {
            return "application/vnd.ms-works";
        } else if (fileName.endsWith(".wm")) {
            return "video/x-ms-wm";
        } else if (fileName.endsWith(".wma")) {
            return "audio/x-ms-wma";
        } else if (fileName.endsWith(".wmd")) {
            return "application/x-ms-wmd";
        } else if (fileName.endsWith(".wmf")) {
            return "application/x-msmetafile";
        } else if (fileName.endsWith(".wml")) {
            return "text/vnd.wap.wml";
        } else if (fileName.endsWith(".wmlc")) {
            return "application/vnd.wap.wmlc";
        } else if (fileName.endsWith(".wmls")) {
            return "text/vnd.wap.wmlscript";
        } else if (fileName.endsWith(".wmlsc")) {
            return "application/vnd.wap.wmlscriptc";
        } else if (fileName.endsWith(".wmv")) {
            return "video/x-ms-wmv";
        } else if (fileName.endsWith(".wmx")) {
            return "video/x-ms-wmx";
        } else if (fileName.endsWith(".wmz")) {
            return "application/x-ms-wmz";
        } else if (fileName.endsWith(".wpd")) {
            return "application/vnd.wordperfect";
        } else if (fileName.endsWith(".wpl")) {
            return "application/vnd.ms-wpl";
        } else if (fileName.endsWith(".wps")) {
            return "application/vnd.ms-works";
        } else if (fileName.endsWith(".wqd")) {
            return "application/vnd.wqd";
        } else if (fileName.endsWith(".wri")) {
            return "application/x-mswrite";
        } else if (fileName.endsWith(".wrl")) {
            return "model/vrml";
        } else if (fileName.endsWith(".wsdl")) {
            return "application/wsdl+xml";
        } else if (fileName.endsWith(".wspolicy")) {
            return "application/wspolicy+xml";
        } else if (fileName.endsWith(".wtb")) {
            return "application/vnd.webturbo";
        } else if (fileName.endsWith(".wvx")) {
            return "video/x-ms-wvx";
        } else if (fileName.endsWith(".x32")) {
            return "application/x-authorware-bin";
        } else if (fileName.endsWith(".x3d")) {
            return "application/vnd.hzn-3d-crossword";
        } else if (fileName.endsWith(".xap")) {
            return "application/x-silverlight-app";
        } else if (fileName.endsWith(".xar")) {
            return "application/vnd.xara";
        } else if (fileName.endsWith(".xbap")) {
            return "application/x-ms-xbap";
        } else if (fileName.endsWith(".xbd")) {
            return "application/vnd.fujixerox.docuworks.binder";
        } else if (fileName.endsWith(".xbm")) {
            return "image/x-xbitmap";
        } else if (fileName.endsWith(".xdm")) {
            return "application/vnd.syncml.dm+xml";
        } else if (fileName.endsWith(".xdp")) {
            return "application/vnd.adobe.xdp+xml";
        } else if (fileName.endsWith(".xdw")) {
            return "application/vnd.fujixerox.docuworks";
        } else if (fileName.endsWith(".xenc")) {
            return "application/xenc+xml";
        } else if (fileName.endsWith(".xer")) {
            return "application/patch-ops-error+xml";
        } else if (fileName.endsWith(".xfdf")) {
            return "application/vnd.adobe.xfdf";
        } else if (fileName.endsWith(".xfdl")) {
            return "application/vnd.xfdl";
        } else if (fileName.endsWith(".xht")) {
            return "application/xhtml+xml";
        } else if (fileName.endsWith(".xhtml")) {
            return "application/xhtml+xml";
        } else if (fileName.endsWith(".xhvml")) {
            return "application/xv+xml";
        } else if (fileName.endsWith(".xif")) {
            return "image/vnd.xiff";
        } else if (fileName.endsWith(".xla")) {
            return "application/vnd.ms-excel";
        } else if (fileName.endsWith(".xlam")) {
            return "application/vnd.ms-excel.addin.macroenabled.12";
        } else if (fileName.endsWith(".xlb")) {
            return "application/vnd.ms-excel";
        } else if (fileName.endsWith(".xlc")) {
            return "application/vnd.ms-excel";
        } else if (fileName.endsWith(".xlm")) {
            return "application/vnd.ms-excel";
        } else if (fileName.endsWith(".xls")) {
            return "application/vnd.ms-excel";
        } else if (fileName.endsWith(".xlsb")) {
            return "application/vnd.ms-excel.sheet.binary.macroenabled.12";
        } else if (fileName.endsWith(".xlsm")) {
            return "application/vnd.ms-excel.sheet.macroenabled.12";
        } else if (fileName.endsWith(".xlsx")) {
            return "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
        } else if (fileName.endsWith(".xlt")) {
            return "application/vnd.ms-excel";
        } else if (fileName.endsWith(".xltm")) {
            return "application/vnd.ms-excel.template.macroenabled.12";
        } else if (fileName.endsWith(".xltx")) {
            return "application/vnd.openxmlformats-officedocument.spreadsheetml.template";
        } else if (fileName.endsWith(".xlw")) {
            return "application/vnd.ms-excel";
        } else if (fileName.endsWith(".xml")) {
            return "application/xml";
        } else if (fileName.endsWith(".xo")) {
            return "application/vnd.olpc-sugar";
        } else if (fileName.endsWith(".xop")) {
            return "application/xop+xml";
        } else if (fileName.endsWith(".xpdl")) {
            return "application/xml";
        } else if (fileName.endsWith(".xpi")) {
            return "application/x-xpinstall";
        } else if (fileName.endsWith(".xpm")) {
            return "image/x-xpixmap";
        } else if (fileName.endsWith(".xpr")) {
            return "application/vnd.is-xpr";
        } else if (fileName.endsWith(".xps")) {
            return "application/vnd.ms-xpsdocument";
        } else if (fileName.endsWith(".xpw")) {
            return "application/vnd.intercon.formnet";
        } else if (fileName.endsWith(".xpx")) {
            return "application/vnd.intercon.formnet";
        } else if (fileName.endsWith(".xsl")) {
            return "application/xml";
        } else if (fileName.endsWith(".xslt")) {
            return "application/xslt+xml";
        } else if (fileName.endsWith(".xsm")) {
            return "application/vnd.syncml+xml";
        } else if (fileName.endsWith(".xspf")) {
            return "application/xspf+xml";
        } else if (fileName.endsWith(".xul")) {
            return "application/vnd.mozilla.xul+xml";
        } else if (fileName.endsWith(".xvm")) {
            return "application/xv+xml";
        } else if (fileName.endsWith(".xvml")) {
            return "application/xv+xml";
        } else if (fileName.endsWith(".xwd")) {
            return "image/x-xwindowdump";
        } else if (fileName.endsWith(".xyz")) {
            return "chemical/x-xyz";
        } else if (fileName.endsWith(".zaz")) {
            return "application/vnd.zzazz.deck+xml";
        } else if (fileName.endsWith(".zip")) {
            return "application/zip";
        } else if (fileName.endsWith(".zir")) {
            return "application/vnd.zul";
        } else if (fileName.endsWith(".zirz")) {
            return "application/vnd.zul";
        } else if (fileName.endsWith(".zmm")) {
            return "application/vnd.handheld-entertainment+xml}";
        } else {
            //if you want you can also define the intent type for any other file

            //additionally use else clause below, to manage other unknown extensions
            //in this case, Android will show all applications installed on the device
            //so you can choose which application to use
            return "*/*";
        }
    }

    public static byte[] fileToByteArray(File f) {


        /**Bitmap bitmap=null;
         byte[] byteArray = null;
         BitmapFactory.Options options = new BitmapFactory.Options();
         options.inPreferredConfig = Bitmap.Config.ARGB_8888;
         try {
         bitmap = BitmapFactory.decodeStream(new FileInputStream(f), null, options);
         } catch (FileNotFoundException e) {
         e.printStackTrace();
         }

         ByteArrayOutputStream baos = new ByteArrayOutputStream();
         bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos); // bm is the bitmap object

         return  baos.toByteArray();**/

        byte[] byteArray = null;
        try {
            byte[] tempBytes = readFile(f);
            //String tempX = new String(tempBytes,"utf-8");
            return Helpers.b64EncodedString(tempBytes).getBytes("utf-8");

            /**
             InputStream inputStream = new FileInputStream(f);
             ByteArrayOutputStream bos = new ByteArrayOutputStream();
             byte[] b = new byte[1024*8];
             int bytesRead =0;

             while ((bytesRead = inputStream.read(b)) != -1)
             {

             // byte[] bytesTemp =  Helpers.b64EncodedString(b).getBytes("utf-8") ;


             //bos.write(bytesTemp, 0, bytesTemp.length);
             bos.write(b, 0, bytesRead);
             }

             byteArray = bos.toByteArray();**/
        } catch (IOException e) {
            e.printStackTrace();
        }

        return byteArray;

    }

    public static byte[] fileFromBase64ToByteArray(File f) {
        byte[] byteArray = null;
        /**
         Bitmap bitmap=null;
         //
         BitmapFactory.Options options = new BitmapFactory.Options();
         options.inPreferredConfig = Bitmap.Config.ARGB_8888;
         try {
         bitmap = BitmapFactory.decodeStream(new FileInputStream(f), null, options);
         } catch (FileNotFoundException e) {
         e.printStackTrace();
         }

         ByteArrayOutputStream baos = new ByteArrayOutputStream();
         bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos); // bm is the bitmap object
         return  baos.toByteArray();**/

        //byte[] b = baos.toByteArray();

        try {
            byte[] tempBytes = readFile(f);
            String tempX = new String(tempBytes, "utf-8");
            return Helpers.dataFromB64String(tempX);
            /**
             FileInputStream inputStream = new FileInputStream(f);
             ByteArrayOutputStream bos = new ByteArrayOutputStream();

             byte[] b = new byte[1024*8];
             int bytesRead =0;

             // byte[] bytesTemp = Helpers.dataFromB64String(new String(inputStream.,"utf-8"));

             //bos.write(bytesTemp);

             while ((bytesRead = inputStream.read(b)) != -1)
             {
             //byte[] bytesTemp = Utils.base64_decode(new String(b)).getBytes() ;
             //bos.write(bytesTemp, 0, bytesTemp.length);

             Log.v("byteStringBase64",new String(new String(b,"utf-8")));
             byte[] bytesTemp = Helpers.dataFromB64String(new String(b));
             // byte[] bytesTemp = Utils.base64StringToByteArray(new String (b,"utf-8"))  ;
             // String byteStringBase64 = new String(b,"utf-8");
             //Log.v("byteStringBase64",new String(bytesTemp));

             //Log.v("byteStringBase64",byteStringBase64);
             Log.v("byteStringBase64","-------------");
             //byteStringBase64 = byteStringBase64.substring(0,byteStringBase64.length());
             bos.write(bytesTemp, 0, bytesTemp.length);
             ///  bos.write(b, 0, bytesRead);

             }

             byteArray = bos.toByteArray();**/
        } catch (IOException e) {
            e.printStackTrace();
        }
        return byteArray;

    }

    public static byte[] readFile(File file) throws IOException {
        // Open file
        RandomAccessFile f = new RandomAccessFile(file, "r");
        try {
            // Get and check length
            long longlength = f.length();
            int length = (int) longlength;
            if (length != longlength)
                throw new IOException("File size >= 2 GB");
            // Read file and return data
            byte[] data = new byte[length];
            f.readFully(data);
            return data;
        } finally {
            f.close();
        }
    }

    public static File createFileWithDataAndFilename(byte[] data, String filename, Context mContext) {
        File file = null;
        try {
            file = Helpers.getDownloadedFileNamed(mContext, filename);
            FileOutputStream fos = new FileOutputStream(file);

            fos.write(data);
        } catch (IOException e) {
            return file;
        }

        return file;

    }

    public static File createFileWithDataAndFilenameFromBase64(byte[] data, String filename, Context mContext) {
        File file = null;
        try {
            if (filename.contains("0000000")) {
                filename = filename.substring(0, filename.indexOf("0000000"));
            }
            file = Helpers.createNewFile(filename);
            FileOutputStream fos = new FileOutputStream(file);
            String tempX = new String(data, "utf-8");
            //String tempFileBase64= Helpers.dataFromB64String(tempX) ;

            //String tempX = new String(tempBytes,"utf-8");
            //  Helpers.ba(tempBytes).getBytes("utf-8") ;

            //String dataTemp = new String(data);
            // byte[] dataTempBytes =  Utils.base64StringToByteArray(dataTemp);
            //byte[] dataTempBytes =  Helpers.dataFromB64String(dataTemp);

            fos.write(Helpers.dataFromB64String(tempX));
            fos.flush();
            // fos.close();
            //fos.write(dataTempBytes);
        } catch (IOException e) {
            return file;
        }

        return file;

    }


    public static File createFileWithEncryptedData(byte[] data, Context mContext) {
        File tempFile = null;
        try {
            tempFile = File.createTempFile("encryptedFile", "");
            // tempFile = new File(fileName);
            // tempFile = new File(mContext.getCacheDir(), fileName);
            // tempFile = File.createTempFile("encryptedFile", "");
            FileOutputStream fos = new FileOutputStream(tempFile);
            //fos.write(data);
            System.gc();

            fos.write(Helpers.b64EncodedString(data).getBytes("utf-8"));
            System.gc();
            fos.flush();
            fos.close();

        } catch (IOException e) {
            return tempFile;
        }

        return tempFile;

    }

    public static File createFileWitDecryptData(byte[] data) {
        File tempFile = null;
        try {
            tempFile = File.createTempFile("decryptedFileTemp", "");

            FileOutputStream fos = new FileOutputStream(tempFile);
            //fos.write(data);
            fos.write(Helpers.b64EncodedString(data).getBytes("utf-8"));
            fos.flush();
            fos.close();

        } catch (IOException e) {
            return tempFile;
        }

        return tempFile;

    }


    public static String stringFromB64(String b64) {
        if (b64 == null) {
            return b64;
        }

        return new String(Helpers.dataFromB64String(b64));
    }

    public static String b64EncodedString(byte[] input) {
        return Base64.encodeToString(input, Base64.NO_WRAP);
    }

    public static byte[] dataFromB64String(String s) {
        return Base64.decode(s.getBytes(), Base64.NO_WRAP);
    }

    public static String addressOfObject(Object object) {
        return Integer.toHexString(System.identityHashCode(object));
    }

    public static String formatDistance(double distance) {
        String units = "m";

        if (distance > 1000.01) {
            distance /= 1000.0;
            units = "km";
        }

        return String.format("%d %s", (int) (distance + 0.5), units);
    }

    public static long timestampFromDate(Date date) {
        long result = date.getTime() / 1000L;
        return result;
    }

    public static int compareInts(int i1, int i2) {
        if (i1 > i2) {
            return 1;
        } else if (i1 < i2) {
            return -1;
        } else {
            return 0;
        }
    }

//    public static String getRealPathFromURI(Uri contentURI) {
//        String result;
//        Cursor cursor = TabBarManagerActivity.getContext().getContentResolver().query(contentURI, null, null, null, null);
//        if (cursor == null) {
//            result = contentURI.getPath();
//        } else {
//            cursor.moveToFirst();
//            int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
//            try {
//                result = cursor.getString(idx);
//            } catch (IllegalStateException exception) {
//                result = contentURI.getPath();
//            }
//            cursor.close();
//        }
//        return result;
//    }

    public static String getRealPathFromURI(Uri contentUri) {
        Cursor cursor = null;
        try {
            String[] proj = {MediaStore.Images.Media.DATA};
            cursor = TabBarManagerActivity.getContext().getContentResolver().query(contentUri, proj, null, null, null);
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    public static void rotateSavedImage(File imageFile) {
        if (!fileIsJPEG(imageFile)) {
            return;
        }

        try {
            XADebug.d("Rotating image: " + imageFile);

            ExifInterface exif = new ExifInterface(imageFile.getPath());

            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);

            int angle = 0;

            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    angle = 90;
                    break;

                case ExifInterface.ORIENTATION_ROTATE_180:
                    angle = 180;
                    break;

                case ExifInterface.ORIENTATION_ROTATE_270:
                    angle = 270;
                    break;

                default:
                    break;
            }

            XADebug.d("Rotation angle is = " + angle);

            if (angle == 0) {
                return;
            }

            Matrix mat = new Matrix();
            mat.postRotate(angle);
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = 2;

            Bitmap bmp = BitmapFactory.decodeStream(new FileInputStream(imageFile), null, options);
            Bitmap bitmap = Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth(), bmp.getHeight(), mat, true);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);

            FileOutputStream fos = new FileOutputStream(imageFile);
            baos.writeTo(fos);
            baos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static boolean fileIsJPEG(File attachmentFile) {
        if (attachmentFile == null) {
            return false;
        }

        ArrayList<String> imageExtensions = new ArrayList<String>();
        imageExtensions.add("jpeg");
        imageExtensions.add("jpe");
        imageExtensions.add("jpg");

        String fileExtension = getFileExtension(attachmentFile);

        for (String ext : imageExtensions) {
            if (ext.equals(fileExtension.toLowerCase())) {
                return true;
            }
        }

        return false;
    }

    public static String getExtensionOfFileName(String name) {
        if (name == null || name.equals("")) {
            return "";
        }
        String suffix = "";
        int index = name.lastIndexOf(".");
        if (index != -1) {
            suffix = name.substring(index + 1);
        }
        return suffix;
    }

    public static File fileFromShareBundle(Activity activity, Bundle bundle) {
        Uri stream = (Uri) bundle.get(Intent.EXTRA_STREAM);

        ContentResolver contentResolver = activity.getContentResolver();
        String[] projection = {"_data"}; // Yay bareword.
        String path = "nothing";

        Cursor metaCursor = contentResolver.query(stream, projection, null, null, null);
        if (metaCursor != null) {
            try {
                if (metaCursor.moveToFirst()) {
                    path = metaCursor.getString(0);
                }
            } finally {
                metaCursor.close();
            }
        }

        File f = new File(path);

        return (f.exists()) ? f : null;
    }

    // From SO
    public static String getFileExtension(File f) {
        String ext = null;
        String s = f.getName();
        int i = s.lastIndexOf('.');

        if (i > 0 && i < s.length() - 1) {
            ext = s.substring(i + 1).toLowerCase();
        }
        return ext;
    }

    public static boolean isStoneAgeAPI() {
        return (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.JELLY_BEAN);
    }

    private static String TAG = "EncryptionRSA";

    public static String getEncryptionRSA() {
        // Generate key pair for 1024-bit RSA encryption and decryption
        Key publicKey = null;
        try {
            KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
            kpg.initialize(2048);
            KeyPair kp = kpg.genKeyPair();

            publicKey = kp.getPublic();

            return PEMConverter.toPEM((PublicKey) publicKey);
        } catch (Exception e) {
            Log.e(TAG, "RSA key pair error");
            return "";
        }

    }


    static String decodeUTF8(byte[] bytes) {
        final Charset UTF8_CHARSET = Charset.forName("UTF-8");
        return new String(bytes, UTF8_CHARSET);
    }


}
