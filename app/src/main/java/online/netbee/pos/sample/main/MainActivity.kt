package online.netbee.pos.sample.main

import android.os.Bundle
import android.util.Base64
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.net.InetSocketAddress
import java.net.Socket
import java.util.concurrent.Executors
import online.netbee.pos.sample.PosProvider
import online.netbee.pos.sample.security.KeyManager
import online.netbee.pos.sample.security.SignatureManager
import online.netbee.pos.sample.ui.theme.PosSampleTheme
import org.json.JSONObject


class MainActivity : ComponentActivity() {

    private var outputStream: OutputStream? = null
    private var inputStream: InputStream? = null
    private var socket: Socket? = null

    private val writeExecutor = Executors.newSingleThreadExecutor()
    private val connectionExecutor = Executors.newSingleThreadExecutor()
    private val observerExecutor = Executors.newSingleThreadExecutor()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            PosSampleTheme {
                MainPage { amount, posProvider, payload, netbeePublicKey ->
                    if (socket?.isConnected == true && socket?.isClosed == false) {
                        sendToNetbeePos(amount, posProvider, payload)
                    } else {
                        connectToNetbeePos { socket ->
                            inputStream = socket.getInputStream()
                            outputStream = socket.getOutputStream()

                            observeMessages(netbeePublicKey)
                            sendToNetbeePos(amount, posProvider, payload)
                        }
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        closeSocket()

        writeExecutor.shutdownNow()
        observerExecutor.shutdownNow()
        connectionExecutor.shutdownNow()
    }

    private fun sendToNetbeePos(
        amount: String,
        posProvider: PosProvider,
        payload: String,
    ) {
        writeExecutor.execute {

            try {
                val sign = sign(amount, posProvider.type, payload)
                    ?: throw SecurityException("cannot generate sign!")

                val json =
                    """
                        {
                            "type": "payment_request",
                            "data": {"amount":$amount,"provider":"${posProvider.type}","payload":"$payload","sign":"$sign","entity_type":"payment_request"}
                        }
                    """.replace("\n", "").trimIndent()
                println("writing: $json")
                outputStream?.write(json.plus("\n").toByteArray())
                outputStream?.flush()
                println("wrote")
            } catch (e: IOException) {
                e.printStackTrace()

                closeSocket()
                showToast("خطایی در ارسال اطلاعات به نت بی پوز رخداده است. مجددا تلاش کنید.")
            }
        }
    }

    private fun closeSocket() {
        inputStream?.close()
        outputStream?.close()
        socket?.close()

        inputStream = null
        outputStream = null
        socket = null
    }

    private fun connectToNetbeePos(onConnected: (Socket) -> Unit) {
        connectionExecutor.execute {
            socket = Socket().apply {
                keepAlive = true
                soTimeout = 0
            }

            try {
                val host = "127.0.0.1"
                val port = 2448
                val address = InetSocketAddress(host, port)

                socket?.connect(address)
                onConnected(socket!!)
            } catch (e: Exception) {
                e.printStackTrace()
                showToast("خطایی در اتصال به نت بی پوز رخداده است.")
            }
        }
    }

    private fun showToast(message: String) {
        runOnUiThread {
            Toast.makeText(
                this,
                message,
                Toast.LENGTH_LONG
            )?.show()
        }
    }

    private fun observeMessages(netbeePublicKey: String) {
        observerExecutor.execute {
            try {
                val bufferedReader = inputStream!!.bufferedReader()
                while (true) {
                    val json = bufferedReader.readLine()

                    if (!json.isNullOrBlank()) {
                        println(json)

                        val jsonObject = JSONObject(json)
                        val eventType = jsonObject.getString("type")

                        when (eventType) {
                            "payment_failed" -> {
                                val failedJsonObject = jsonObject.getJSONObject("data")
                                val error = failedJsonObject.getString("error")
                                val sign = failedJsonObject.getString("sign")

                                val verified = verify(netbeePublicKey, sign, error)

                                if (!verified) throw SecurityException("cannot verify data")

                                showToast(error)
                            }
                        }
                    }
                }

            } catch (e: IOException) {
                e.printStackTrace()

                showToast("خطایی در ورودی یا خروجی نت بی پوز رخداده است.")
            } catch (e: SecurityException) {
                e.printStackTrace()

                showToast("خطایی در ساخت یا اعتبارسنجی امضا رخداده است")
            } catch (e: Exception) {
                e.printStackTrace()

                showToast("خطایی ناشناخته رخداده است")
            }
        }
    }

    private fun createTemplate(vararg data: String): String {
        return "#${data.joinToString(",")}#"
    }

    private fun verify(
        publicKey: String,
        sign: String,
        vararg data: String
    ): Boolean {
        try {
            val netbeePosPublicKey =
                KeyManager.initializePublicKey(publicKey)
            val template = createTemplate(data = data)
            println("template: $template")

            return SignatureManager.verify(
                netbeePosPublicKey,
                Base64.decode(sign, Base64.NO_WRAP),
                template.toByteArray()
            )
        } catch (e: Exception) {
            e.printStackTrace()
            throw SecurityException("cannot verify data")
        }

    }

    private fun sign(
        amount: String,
        posProvider: String,
        payload: String
    ): String? {
        try {
            val privateKey = KeyManager.initializePrivateKey(
                KeyManager.fakePrivateKey
            )

            val template = createTemplate(amount, posProvider, payload)
            println("template: $template")

            val sign = SignatureManager.sign(
                privateKey,
                template.toByteArray()
            )

            return Base64.encodeToString(sign, Base64.NO_WRAP)
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }
}