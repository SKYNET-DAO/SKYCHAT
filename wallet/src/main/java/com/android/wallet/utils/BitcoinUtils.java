package com.android.wallet.utils;

import com.orhanobut.logger.Logger;

import org.apache.commons.codec.DecoderException;
import org.bitcoinj.core.Base58;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.core.Utils;
import org.bitcoinj.params.MainNetParams;
import org.bitcoinj.params.TestNet3Params;
import org.bouncycastle.crypto.digests.RIPEMD160Digest;
import org.bouncycastle.jce.ECNamedCurveTable;
import org.bouncycastle.jce.spec.ECNamedCurveParameterSpec;
import org.bouncycastle.math.ec.ECPoint;
import java.io.ByteArrayOutputStream;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;

public class BitcoinUtils {

    public static final String TAG = BitcoinUtils.class.getName();
    public static final boolean IS_PRODUCTION = false;

    public static NetworkParameters getNetParams() {
        return (IS_PRODUCTION ? MainNetParams.get() : TestNet3Params.get());
    }

    public static String generateRandomSeed(int seedLen) {
        SecureRandom secureRandom = new SecureRandom();
        byte[] seed = new byte[seedLen];
        secureRandom.nextBytes(seed);
        String seedHexStr = Utils.HEX.encode(seed);
        Logger.i(TAG, "==> seed is 0x" + seedHexStr);
        return seedHexStr;
    }

    public static String generatePublicKey(byte [] privateKey, boolean compressed) {
        ECNamedCurveParameterSpec spec = ECNamedCurveTable.getParameterSpec("secp256k1");
        ECPoint pointQ = spec.getG().multiply(new BigInteger(1, privateKey));
        byte [] publicKeyBytes = pointQ.getEncoded(compressed);
        String publicKeyHexStr =  Utils.HEX.encode(publicKeyBytes);
        Logger.i(TAG, "==> public key is 0x" + publicKeyHexStr);
        return publicKeyHexStr;
    }

    public static String generatePublicKey(String privateKeyHexStr, boolean compressed) throws DecoderException {
        byte [] privateKeyBytes = Utils.HEX.decode(privateKeyHexStr);
        return generatePublicKey(privateKeyBytes, compressed);
    }

    // RIPEMD160 ( SHA256 (publicKey) )
    public static String generateAddress(byte [] publicKey) throws NoSuchAlgorithmException {

        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte [] pubKeySha256 = digest.digest(publicKey);
        Logger.i(TAG, "==> sha256: 0x" +  Utils.HEX.encode(pubKeySha256));

        RIPEMD160Digest d = new RIPEMD160Digest();
        d.update(pubKeySha256, 0, pubKeySha256.length);
        byte [] bytesAddr = new byte[d.getDigestSize()];
        d.doFinal(bytesAddr, 0);
        Logger.i(TAG, "==> ripemd160: 0x" +  Utils.HEX.encode(bytesAddr));

        return  Utils.HEX.encode(bytesAddr);
    }

    public static String generateAddress(String publicKeyHex) throws DecoderException, NoSuchAlgorithmException {
        return generateAddress( Utils.HEX.decode(publicKeyHex));
    }

    static byte[] generateBase58CheckSum(byte[] data) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte [] dataOneHash = digest.digest(data);
        byte [] dataDoubleHash = digest.digest(dataOneHash);
        byte [] checkSum = Arrays.copyOf(dataDoubleHash, 4);
        Logger.i(TAG, "==> base58check sum: " +  Utils.HEX.encode(checkSum));
        return checkSum;
    }

    // Base58Check ( RIPEMD160 ( SHA256 (publicKey) )
    public static String generateAddressWithBase58Check(byte [] publicKey) throws NoSuchAlgorithmException, DecoderException {
        String addrHex = generateAddress(publicKey);
        byte [] addrBytes =  Utils.HEX.decode(addrHex);
        byte [] checksum = generateBase58CheckSum(addrBytes);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        baos.write(0);  // address version prefix
        baos.write(addrBytes, 0, addrBytes.length);
        baos.write(checksum, 0, checksum.length);
        String addressWithBase58Check = Base58.encode(baos.toByteArray());
        Logger.i(TAG, "==> address with base58check format: " + addressWithBase58Check);
        return addressWithBase58Check;
    }

    public static String generateAddressWithBase58Check(String publicKeyHex) throws DecoderException, NoSuchAlgorithmException {
        return generateAddressWithBase58Check( Utils.HEX.decode(publicKeyHex));
    }


}
