package ca.xahive.app.bl.utils;

import org.spongycastle.asn1.pkcs.RSAPublicKey;
import org.spongycastle.util.io.pem.PemObject;
import org.spongycastle.util.io.pem.PemReader;

import java.io.IOException;
import java.io.StringReader;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;

import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPublicKeySpec;

/**
 * Created by trantung on 10/30/15.
 */
public class RSAPublicKeyFromOpenSSL_PKCS1_PEM {
    private static final String PEM = "-----BEGIN RSA PUBLIC KEY-----\r\n"
            + "MIGJAoGBAKks62Itns2uU/dVZJ4kCkMinHgyeh/rdMD53a4Zu2a76OIJvdSZ8q4c\r\n"
            + "YTWvPj0giefVtMc7tV4c6AAw04jyIfmCTvcQUlHI+sspHxXDlQTagNoxCuA29b5L\r\n"
            + "9MKO6Ok0LwF9rGgTywC1heNEulZz9ISn9FQDazJT+Bd9cnNOrJRdAgMBAAE=\r\n"
            + "-----END RSA PUBLIC KEY-----\r\n";

    public static RSAPublicKey parsePEM(final String pem)
            throws IllegalArgumentException {

        // --- read PEM object
        final PemObject readPemObject;
        try (final PemReader reader = new PemReader(new StringReader(PEM))) {
            readPemObject = reader.readPemObject();
        } catch (final IOException e) {
            throw new IllegalArgumentException("Not a PEM object", e);
        }
        if (!readPemObject.getType().equalsIgnoreCase("RSA PUBLIC KEY")) {
            throw new IllegalArgumentException("Not a public key");
        }
        final byte[] pemContent = readPemObject.getContent();

        // --- create Bouncy Castle PKCS#1 public key
        RSAPublicKey pkcs1PublicKey;
        try {
            pkcs1PublicKey =RSAPublicKey
                    .getInstance(pemContent);
        } catch (final Exception e) {
            throw new IllegalArgumentException(
                    "Could not parse BER PKCS#1 public key structure", e);
        }

        // --- convert to JCE RSAPublicKey
        final RSAPublicKeySpec spec = new RSAPublicKeySpec(
                pkcs1PublicKey.getModulus(), pkcs1PublicKey.getPublicExponent());
        final KeyFactory rsaKeyFact;
        try {
            rsaKeyFact = KeyFactory.getInstance("RSA");
        } catch (final NoSuchAlgorithmException e) {
            throw new IllegalStateException("RSA KeyFactory should be available", e);
        }
        try {
            return (RSAPublicKey) rsaKeyFact.generatePublic(spec);
        } catch (InvalidKeySpecException e) {
            throw new IllegalArgumentException(
                    "Invalid RSA public key, modulus and/or exponent invalid", e);
        }
    }
    /**
    public static void main(final String ... args) throws Exception {
        final RSAPublicKey publicKey = parsePEM(PEM);
        System.out.println(publicKey);
    }**/
}
