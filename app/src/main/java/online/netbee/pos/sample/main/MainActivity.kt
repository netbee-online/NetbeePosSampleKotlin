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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import online.netbee.pos.sample.security.KeyManager
import online.netbee.pos.sample.security.SignatureManager
import online.netbee.pos.sample.ui.theme.PosSampleTheme
import org.json.JSONObject

/**
 * Purpose of these codes and application is that you find out how to connect to the NetbeePOS and
 * how to send and receive data. It has been developing in the simplest way. We have been avoiding
 * to use third party libraries for the sake of simplicity. So you must develop your app in your way.
 * If you are a C#, Dart or other language developer, this sample can give you a better view of how
 * you can develop your own. Before you proceed further, make sure that you have completely read and
 * understood the protocol document.
 *
 * @see <a href="https://github.com/netbee-online/NetbeePos>NetbeePos document</a>
 *
 * @author Mohammad Esteki
 */
class MainActivity : ComponentActivity() {

    private var outputStream: OutputStream? = null
    private var inputStream: InputStream? = null
    private var socket: Socket? = null

    private val jobs = mutableListOf<Job>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            PosSampleTheme {
                MainPage { amount, stanId, payload, netbeePublicKey ->
                    if (socket?.isConnected == true && socket?.isClosed == false) {
                        sendToNetbeePos(amount, stanId, payload)
                    } else {
                        connectToNetbeePos { socket ->
                            inputStream = socket.getInputStream()
                            outputStream = socket.getOutputStream()

                            observeMessages(netbeePublicKey)
                            sendToNetbeePos(amount, stanId, payload)
                        }
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        closeSocket()
    }

    private fun sendToNetbeePos(
        amount: String,
        stanId: String,
        payload: String,
    ) {
        val job = CoroutineScope(Dispatchers.IO).launch {
            try {
                val sign = sign(amount, stanId, payload)
                    ?: throw SecurityException("cannot generate sign!")

                val json =
                    """
                        {
                            "type": "payment_request",
                            "data": {"amount":$amount,"stan_id":$stanId,"payload":"$payload","sign":"$sign","entity_type":"payment_request"}
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
            } catch (e: SecurityException) {
                e.printStackTrace()

                showToast("خطایی در ساخت امضا رخداده است.")
            } catch (e: Exception) {
                e.printStackTrace()

                showToast("خطایی نامشخص رخداده است.")
            }
        }
        jobs.add(job)
    }

    private fun closeSocket() {
        inputStream?.close()
        outputStream?.close()
        socket?.close()

        inputStream = null
        outputStream = null
        socket = null

        jobs.forEach { it.cancel() }
        jobs.clear()
    }

    private fun connectToNetbeePos(onConnected: (Socket) -> Unit) {
        val job = CoroutineScope(Dispatchers.IO).launch {
            socket = Socket().apply {
                keepAlive = false
                soTimeout = 60_000
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
        jobs.add(job)
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
        val job = CoroutineScope(Dispatchers.IO).launch {
            try {
                val bufferedReader = inputStream!!.bufferedReader()
                while (true) {
                    val json = bufferedReader.readLine()

                    if (json == null) {
                        closeSocket()
                        return@launch
                    }

                    if (json.isNotEmpty()) {
                        println(json)

                        val jsonObject = JSONObject(json)
                        val eventType = jsonObject.getString("type")

                        when (eventType) {
                            "payment_failed" -> {
                                val failedJsonObject = jsonObject.getJSONObject("data")
                                val error = failedJsonObject.getString("error")
                                val stanId = failedJsonObject.getString("stan_id")
                                val payload = failedJsonObject.optString("payload") ?: ""
                                val sign = failedJsonObject.getString("sign")

                                val verified = verify(netbeePublicKey, sign, error, stanId, payload)

                                if (!verified) throw SecurityException("cannot verify data")

                                showToast(error)
                            }

                            "payment_success" -> {
                                val dataObject = jsonObject.getJSONObject("data")

                                val amount = dataObject.getLong("amount")
                                val rrn = dataObject.getString("rrn")
                                val serial = dataObject.getString("serial")
                                val trace = dataObject.getString("trace")
                                val cardNumber = dataObject.getString("card_number")
                                val dateTime = dataObject.getString("datetime")
                                val stanId = dataObject.getString("stan_id")
                                val payload = dataObject.getString("payload")
                                val sign = dataObject.getString("sign")

                                val verified = verify(
                                    publicKey = netbeePublicKey,
                                    sign = sign,
                                    amount.toString(),
                                    rrn,
                                    serial,
                                    trace,
                                    cardNumber,
                                    dateTime,
                                    stanId,
                                    payload
                                )

                                if (!verified) throw SecurityException("cannot verify data")

                                showToast("تراکنش موفق. کد مرجع: $rrn")
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
        jobs.add(job)
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
        stanId: String,
        payload: String
    ): String? {
        try {
            val privateKey = KeyManager.initializePrivateKey(
                KeyManager.fakePrivateKey
            )

            val template = createTemplate(amount, stanId, payload)
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