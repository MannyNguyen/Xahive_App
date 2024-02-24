package ca.xahive.app.bl.local;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import org.spongycastle.asn1.ASN1Encodable;
import org.spongycastle.asn1.ASN1Integer;
import org.spongycastle.asn1.ASN1Primitive;
import org.spongycastle.asn1.ASN1Sequence;
import org.spongycastle.asn1.pkcs.PrivateKeyInfo;
import org.spongycastle.asn1.x509.SubjectPublicKeyInfo;
import org.spongycastle.util.encoders.Base64;
import org.spongycastle.util.io.pem.PemObject;
import org.spongycastle.util.io.pem.PemWriter;

import java.io.IOException;
import java.io.StringWriter;
import java.math.BigInteger;
import java.security.GeneralSecurityException;
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
import java.util.Arrays;
import java.util.Enumeration;

import ca.xahive.app.bl.utils.Crypto;
import ca.xahive.app.bl.utils.Helpers;


public class UserDefaults {
    private static final String PREFS_KEY = "XAHPrefs";
    private static final String USERNAME_KEY = "username";
    private static final String PASSWORD_KEY = "password";
    private static final String HIVE_KEY = "hiveId";
    private static final String ADS_KEY = "ads";
    private static final String DECRYPT_SAVE_KEY = "decryptSave";
    private static final String PUBLICKEY = "PublicKey";
    private static final String PRIVATEKEY = "PrivateKey";
    private static final String SECRECT = "Secrect";
    private static final String USERID = "UserId";

    private static final String DECRYPT_APPLY_ALL_KEY = "decryptApplyAll";
    private static final String LATITUDE_KEY = "latitude";
    private static final String LONGITUDE_KEY = "longitude";

    private static final String ATTACHMENT_ID = "attachment_";


    private static SharedPreferences.Editor _editor;
    public static Context mContext;

    public static void setContext(Context context) {
        mContext = context;
    }

    public static void generateKeys() throws InvalidKeySpecException {
        PublicKey pubKey;
        PrivateKey privKey;
        try {
            //KeyPairGenerator generator;
            KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
            kpg.initialize(2048);
            KeyPair kp = kpg.genKeyPair();
            pubKey = kp.getPublic();
            privKey = kp.getPrivate();
            try {
                Log.v("PUBLICKEY", savePublicKey(pubKey));
            } catch (IOException e) {
                e.printStackTrace();
            }

            Editor().putString(PUBLICKEY, savePublicKey(pubKey));
            Editor().putString(PRIVATEKEY, savePrivateKey(privKey));

            Editor().commit();

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (GeneralSecurityException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void generateKeys(int idUser, String hiveID) throws InvalidKeySpecException, IOException {
        PublicKey pubKey;
        PrivateKey privKey;
        try {
            KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
            kpg.initialize(2048);
            KeyPair kp = kpg.genKeyPair();
            pubKey = kp.getPublic();
            privKey = kp.getPrivate();

            Editor().putString(String.valueOf(idUser) + "_" + PUBLICKEY + "OFHive" + hiveID, savePublicKey(pubKey));
            Editor().putString(String.valueOf(idUser) + "_" + PRIVATEKEY + "OFHive" + hiveID, savePrivateKey(privKey));
            Editor().commit();
            //getPrivateKeyFromString(key64Private);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (GeneralSecurityException e) {
            e.printStackTrace();
        }
    }

    public static void savePubicKeyFromWeb(int idUser, String hiveID, PublicKey pubKey) {

        try {
            Editor().putString(String.valueOf(idUser) + "_" + PUBLICKEY + "OFHive" + hiveID, savePublicKey(pubKey));
            Editor().commit();

        } catch (GeneralSecurityException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void saveSecrectKey(int idUser, String hiveID, String secrectKey) {
        Editor().putString(String.valueOf(idUser) + "_" + SECRECT + "OFHive" + hiveID, secrectKey);
        Editor().commit();

    }

    public static String getSecrectKey(int idUser, String hiveID) {

        return Prefs().getString(String.valueOf(idUser) + "_" + SECRECT + "OFHive" + hiveID, "");
        // Editor().putString(String.valueOf(idUser)+"_" +SECRECT +"OFHive" + hiveID, secrectKey);
    }


    public static PublicKey getPublicKeyWithHive(String hiveID) {


        try {
            return loadPublicKey(Crypto.stripPublicKeyHeaders(getPublicKeyWithHiveAsString(Model.getInstance().getCurrentUser().getUserId(), hiveID)));
        } catch (Exception e) {

        }
        return null;
    }

    public static PrivateKey getPrivateKeyWithHive(String hiveID) {


        try {
            return loadPrivateKey(Crypto.stripPublicKeyHeaders(getPrivateKeyWithHiveAsString(Model.getInstance().getCurrentUser().getUserId(), hiveID)));
        } catch (Exception e) {

        }
        return null;
    }

    private static PrivateKey getPrivateKeyFromString(String privKeyStr) {
        byte[] encodedPrivateKey = Base64.decode(privKeyStr);

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

    public static PublicKey getPublicKey() {

        /**String pubKeyStr = Prefs().getString(PUBLICKEY, "");
         byte[] sigBytes = Base64.decode(pubKeyStr);
         X509EncodedKeySpec x509KeySpec = new X509EncodedKeySpec(sigBytes);
         KeyFactory keyFact = null;
         try {
         keyFact = KeyFactory.getInstance("RSA");

         //keyFact = KeyFactory.getInstance("RSA", "BC");
         } catch (NoSuchAlgorithmException e) {
         e.printStackTrace();
         }
         try {
         return  keyFact.generatePublic(x509KeySpec);
         } catch (InvalidKeySpecException e) {
         e.printStackTrace();
         }
         **/
        try {
            return loadPublicKey(Crypto.stripPublicKeyHeaders(getPublicKeyAsString()));
        } catch (Exception e) {

        }
        return null;
    }

    public static PrivateKey loadPrivateKey(String key64) throws GeneralSecurityException {
        byte[] clear = Base64.decode(key64);
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(clear);
        KeyFactory fact = KeyFactory.getInstance("RSA");
        PrivateKey priv = fact.generatePrivate(keySpec);
        Arrays.fill(clear, (byte) 0);
        return priv;
    }

    public static PublicKey loadPublicKey(String stored) throws GeneralSecurityException {
        byte[] data = Base64.decode(stored);
        X509EncodedKeySpec spec = new X509EncodedKeySpec(data);
        KeyFactory fact = KeyFactory.getInstance("RSA");
        return fact.generatePublic(spec);
    }

    static String TAG_RSA_PRIVATE_KEY_BEGIN = "-----BEGIN RSA PRIVATE KEY-----";
    static String TAG_RSA_PRIVATE_KEY_END = "-----END RSA PRIVATE KEY-----";

    public static String savePrivateKey(PrivateKey privateKey) throws GeneralSecurityException, IOException {

        PrivateKeyInfo pkInfo = PrivateKeyInfo.getInstance(privateKey.getEncoded());
        ASN1Encodable privateKeyPKCS1ASN1Encodable = pkInfo.parsePrivateKey();
        ASN1Primitive privateKeyPKCS1ASN1 = privateKeyPKCS1ASN1Encodable.toASN1Primitive();
        byte[] privateKeyPKCS1 = privateKeyPKCS1ASN1.getEncoded();
        StringWriter writer = new StringWriter();
        PemWriter pemWriter = new PemWriter(writer);
        pemWriter.writeObject(new PemObject("RSA PRIVATE KEY", privateKeyPKCS1));
        pemWriter.flush();
        pemWriter.close();
        return writer.toString();
        /*8
        KeyFactory fact = KeyFactory.getInstance("RSA");
        PKCS8EncodedKeySpec spec = fact.getKeySpec(priv,
                PKCS8EncodedKeySpec.class);
        byte[] packed = spec.getEncoded();
        String key64 = null;
        try {
            key64 = new String(Base64.encode(spec.getEncoded()),"utf-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        Arrays.fill(packed, (byte) 0);
        return key64;**/

    }


    public static String savePublicKey(PublicKey publicKey) throws GeneralSecurityException, IOException {
        /**SubjectPublicKeyInfo spkInfo = SubjectPublicKeyInfo.getInstance(publicKey.getEncoded());
         ASN1Primitive publicKeyPKCS1ASN1 = spkInfo.parsePublicKey();
         byte[] publicKeyPKCS1 = publicKeyPKCS1ASN1.getEncoded();**/
        StringWriter writer = new StringWriter();
        PemWriter pemWriter = new PemWriter(writer);
        pemWriter.writeObject(new PemObject("PUBLIC KEY", publicKey.getEncoded()));
        pemWriter.flush();
        pemWriter.close();
        return writer.toString();
    }

    public static String getPublicKeyAsString() {
        return Prefs().getString(PUBLICKEY, "");
    }


    public static void saveAttachmentID(String id, String local) {
        Editor().putString(ATTACHMENT_ID + id, local);
        Editor().commit();
    }

    public static String getAttachmentID(String id, String local) {

        return Prefs().getString(ATTACHMENT_ID + id, "");
    }

    public static String getPrivateKeyWithHiveAsString(int idUser, String hiveID) {
        Log.i("getPrivateKey", "" + Prefs().getString(String.valueOf(idUser) + "_" + PRIVATEKEY + "OFHive" + hiveID, ""));
        return Prefs().getString(String.valueOf(idUser) + "_" + PRIVATEKEY + "OFHive" + hiveID, "");
    }

    public static void savePrivateKeyFromWeb(int idUser, String hiveID, String privKey) {

        Log.i("savePrivateKey", "" + privKey);

        Editor().putString(String.valueOf(idUser) + "_" + PRIVATEKEY + "OFHive" + hiveID, privKey);
        Editor().commit();


    }

    public static String getPublicKeyWithHiveAsString(int idUser, String hiveID) {
        return Prefs().getString(String.valueOf(idUser) + "_" + PUBLICKEY + "OFHive" + hiveID, "");
    }

    public static PrivateKey getPublicKeyWithString(String key) {
        String privKeyStr = Prefs().getString(PRIVATEKEY, "");
        //Log.v("privKeyStr", privKeyStr + "***********");
        //return getPrivateKeyFromString(privKeyStr);
        try {
            return loadPrivateKey(privKeyStr);
        } catch (Exception e) {
            //e.printStackTrace();
            return null;
        }
    }

    public static PrivateKey getPrivateKey() {
        String privKeyStr = Prefs().getString(PRIVATEKEY, "");
        Log.v("privKeyStr", privKeyStr + "***********");
        //return getPrivateKeyFromString(privKeyStr);
        try {
            return loadPrivateKey(privKeyStr);
        } catch (Exception e) {
            //e.printStackTrace();
            return null;
        }
        /**
         byte[] sigBytes = Base64.decode(privKeyStr);
         try {
         PrivateKey privateKey = KeyFactory.getInstance("RSA").generatePrivate(
         new PKCS8EncodedKeySpec(sigBytes));
         return  privateKey;
         } catch (InvalidKeySpecException e) {
         e.printStackTrace();
         } catch (NoSuchAlgorithmException e) {
         e.printStackTrace();
         }
         PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(sigBytes);
         KeyFactory fact = null;
         try {
         fact = KeyFactory.getInstance("RSA");

         byte[] clear = Base64.decode(privKeyStr);
         PrivateKey priv = fact.generatePrivate(keySpec);
         Arrays.fill(clear, (byte) 0);
         return priv;
         } catch (NoSuchAlgorithmException e) {
         e.printStackTrace();
         } catch (InvalidKeySpecException e) {
         e.printStackTrace();
         }**/
        //return null;
    }

    public static String getPrivateKeyAsString() {
        return Prefs().getString(PRIVATEKEY, "");
    }

    private static SharedPreferences Prefs() {
        return mContext.getSharedPreferences(PREFS_KEY, Context.MODE_PRIVATE);
    }

    private static SharedPreferences.Editor Editor() {
        if (_editor == null) {
            _editor = Prefs().edit();
        }
        return _editor;
    }

    public static void setUsername(String username) {
        Editor().putString(USERNAME_KEY, username);
        Editor().commit();
    }

    public static String getUsername() {
        return Prefs().getString(USERNAME_KEY, null);
    }

    public static void setUserId(int userId) {
        Editor().putInt(USERID, userId);
        Editor().commit();
    }

    public static int getUserId() {
        return Prefs().getInt(USERID, 0);
    }

    public static void setPassword(String password) {
        Editor().putString(PASSWORD_KEY, password);
        Editor().commit();
    }

    public static String getPassword() {
        return Prefs().getString(PASSWORD_KEY, null);
    }

    public static boolean hasSavedUser() {
        return (Helpers.stringIsNotNullAndMeetsMinLength(getUsername(), 1) && Helpers.stringIsNotNullAndMeetsMinLength(getPassword(), 1));
    }

    public static void setSavedHiveId(int hiveId) {
        Editor().putInt(HIVE_KEY, hiveId);
        Editor().commit();
    }

    public static int getSavedHiveId() {
        return Prefs().getInt(HIVE_KEY, 0);
    }

    public static void setAdvertsDisabled(boolean disabled) {
        Editor().putBoolean(ADS_KEY, disabled);
        Editor().commit();
    }

    public static boolean getAdvertsDisabled() {
        return Prefs().getBoolean(ADS_KEY, false);
    }

    public static void setDecryptSave(boolean save) {
        Editor().putBoolean(DECRYPT_SAVE_KEY, save);
        Editor().commit();
    }

    public static boolean getDecryptSave() {
        return Prefs().getBoolean(DECRYPT_SAVE_KEY, true);
    }

    public static void setApplyPasswordToAll(boolean applyAll) {
        Editor().putBoolean(DECRYPT_APPLY_ALL_KEY, applyAll);
        Editor().commit();
    }

    public static boolean getApplyPasswordToAll() {
        return Prefs().getBoolean(DECRYPT_APPLY_ALL_KEY, true);
    }

    public static void setLatitude(double latitude) {
        Editor().putFloat(LATITUDE_KEY, (float) latitude);
        Editor().commit();
    }

    public static double getLatitude() {
        return (double) Prefs().getFloat(LATITUDE_KEY, 0.0f);
    }

    public static void setLongitude(double longitude) {
        Editor().putFloat(LONGITUDE_KEY, (float) longitude);
        Editor().commit();
    }

    public static double getLongitude() {
        return (double) Prefs().getFloat(LONGITUDE_KEY, 0.0f);
    }
}
