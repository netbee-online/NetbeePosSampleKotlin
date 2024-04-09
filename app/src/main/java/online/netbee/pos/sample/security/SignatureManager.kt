package online.netbee.pos.sample.security

import java.security.PrivateKey
import java.security.PublicKey
import java.security.Signature

object SignatureManager {
    private const val ALGORITHM = "SHA256withECDSA"
    private val signature = Signature.getInstance(ALGORITHM, "BC")

    fun sign(
        privateKey: PrivateKey,
        data: ByteArray
    ): ByteArray = signature.run {
        initSign(privateKey)
        update(data)
        sign()
    }

    fun verify(
        publicKey: PublicKey,
        sign: ByteArray,
        data: ByteArray,
    ) = signature.run {
        initVerify(publicKey)
        update(data)
        verify(sign)
    }
}