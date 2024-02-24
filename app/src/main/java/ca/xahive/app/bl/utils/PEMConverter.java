package ca.xahive.app.bl.utils;

/**
 * Created by trantung on 10/19/15.
 */
import org.spongycastle.util.io.pem.PemObject;
import org.spongycastle.util.io.pem.PemObjectGenerator;
import org.spongycastle.util.io.pem.PemWriter;

import java.io.IOException;
import java.io.StringWriter;
import java.security.PublicKey;

class PEMConverter {

    public static String toPEM(PublicKey pubKey) throws IOException {
        StringWriter sw = new StringWriter();
        PemWriter pemWriter = new PemWriter(sw);
       /**
        try {
            pemWriter.writeObject((PemObjectGenerator) pubKey);
        } catch (IOException e) {
            e.printStackTrace();
        }**/
       PemObject pemObject =   new PemObject("PUBLIC KEY", pubKey.getEncoded());
        pemWriter.writeObject(pemObject);
        pemWriter.close();
        return sw.toString();
    }
}
