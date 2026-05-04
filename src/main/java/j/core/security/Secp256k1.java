package j.core.security;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.Security;
import java.security.Signature;

public class Secp256k1 {
    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    public static void main(String[] args) throws Exception {
        // 初始化KeyPairGenerator
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("ECDSA", "BC");
        keyPairGenerator.initialize(256);

        // 生成密钥对
        KeyPair keyPair = keyPairGenerator.genKeyPair();

        // 获取公钥和私钥
        java.security.PublicKey publicKey = keyPair.getPublic();
        java.security.PrivateKey privateKey = keyPair.getPrivate();

        // 对数据进行签名
        Signature sig = Signature.getInstance("SHA256withECDSA", "BC");
        sig.initSign(privateKey);
        sig.update("data to sign".getBytes());
        byte[] signature = sig.sign();

        // 验证签名
        sig.initVerify(publicKey);
        sig.update("data to sign".getBytes());
        boolean isValid = sig.verify(signature);

        System.out.println("Signature is valid: " + isValid);
    }
}