package online.netbee.pos.sample.security

import android.util.Base64
import java.security.KeyFactory
import java.security.PrivateKey
import java.security.PublicKey
import java.security.spec.PKCS8EncodedKeySpec
import java.security.spec.X509EncodedKeySpec


object KeyManager {

    // TODO("generate or replace these with yours")
    val fakePrivateKey = "MIGHAgEAMBMGByqGSM49AgEGCCqGSM49AwEHBG0wawIBAQQgecauuASSa4zrQ7q7\n" +
            "OtVOjtVZVsfxPr5Yx/TggDdW0HWhRANCAARKn+TtijxV9FvGGWSzQua9tLXIQ/MX\n" +
            "97X6G/EWQaso0seq4lkmkLkAD4dtWptUaUCe/lxdfiDlmct3Ydq80wRq"
    val fakePublicKey = "MFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAESp/k7Yo8VfRbxhlks0LmvbS1yEPz\n" +
            "F/e1+hvxFkGrKNLHquJZJpC5AA+HbVqbVGlAnv5cXX4g5ZnLd2HavNMEag=="

    fun initializePublicKey(key: String): PublicKey {
        val keyFactory = KeyFactory.getInstance("EC", "BC")
        val publicKey = key
            .replace("-----BEGIN PUBLIC KEY-----", "")
            .replace("-----END PUBLIC KEY-----", "")
            .replace("\n", "")
            .trim()
        val decodedPublicKey = Base64.decode(publicKey, Base64.NO_WRAP)
        val encodedKeySpec = X509EncodedKeySpec(decodedPublicKey)
        return keyFactory.generatePublic(encodedKeySpec)
    }

    fun initializePrivateKey(privatePEMKey: String): PrivateKey {
        val keyFactory = KeyFactory.getInstance("EC", "BC")
        val privateKey = privatePEMKey
            .replace("-----BEGIN PRIVATE KEY-----", "")
            .replace("-----BEGIN EC PRIVATE KEY-----", "")
            .replace("-----END PRIVATE KEY-----", "")
            .replace("-----END EC PRIVATE KEY-----", "")
            .replace("\n", "")
            .trim()
        val encodedPrivateKey = Base64.decode(privateKey, Base64.NO_WRAP)
        val encodedKeySpec = PKCS8EncodedKeySpec(encodedPrivateKey)
        return keyFactory.generatePrivate(encodedKeySpec)
    }

}