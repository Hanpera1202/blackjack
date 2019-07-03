package com.ivoid.bj;

import android.util.Base64;
import android.util.Log;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

/**
 * Created on 2016/02/14.
 */
public class Crypt {

    String encodeKey = "yGfJrzEVfDmtbWZS";
    String value;

    Crypt(String val){
        value = val;
        Log.d("cryptdata",value);
    }

    public String encrypt() throws Exception{
        // キー生成
        SecretKey key = new SecretKeySpec(encodeKey.getBytes(), "AES");

        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, key);

        // エンコード処理
        byte[] encryptBin = cipher.doFinal(value.getBytes());
        // BASE64に変換
        String encodedValue = new String(Base64.encode(encryptBin, Base64.DEFAULT));
        return encodedValue;
    }
}
