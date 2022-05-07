package com.jakting.alterego

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.jakting.alterego.databinding.ActivityMainBinding
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import org.java_websocket.client.WebSocketClient
import org.java_websocket.handshake.ServerHandshake
import java.net.URI
import javax.net.ssl.SSLSocketFactory

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var clipboardSendAdapter: JsonAdapter<ClipboardSend>

    val WEB_SOCKET_URL = "ws://119.91.254.23:8000/ws/"
    val TAG = "Coinbase"
    private lateinit var webSocketClient: WebSocketClient
    private lateinit var clipboardManager: ClipboardManager

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityMainBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        clipboardSendAdapter = Moshi.Builder().build().adapter(ClipboardSend::class.java)

        clipboardManager = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

        binding.button.setOnClickListener {
            val clipboardSend = ClipboardSend(
                "send", 200,
                ClipboardData(System.currentTimeMillis(), "新内容" + System.currentTimeMillis())
            )
            webSocketClient.send(clipboardSendAdapter.toJson(clipboardSend))
        }
    }

    private fun initWebSocket() {
        val coinbaseUri: URI = URI(WEB_SOCKET_URL + android.os.Build.MODEL)
        createWebSocketClient(coinbaseUri)

        val socketFactory: SSLSocketFactory = SSLSocketFactory.getDefault() as SSLSocketFactory
//        webSocketClient.setSocketFactory(socketFactory)
        webSocketClient.connect()
    }

    private fun subscribe() {
        val clipboardSend = ClipboardSend(
            "subscribe", 200,
            ClipboardData(System.currentTimeMillis(), "心跳维持")
        )
        webSocketClient.send(clipboardSendAdapter.toJson(clipboardSend))
        runOnUiThread {
            binding.btcPriceTv.text = clipboardSendAdapter.toJson(clipboardSend)
        }
    }

    private fun setUpBtcPriceText(message: String?) {
        message?.let {
            val clipboard = clipboardSendAdapter.fromJson(message)


            runOnUiThread {
//                Toast.makeText(this@MainActivity,bitcoin.price,Toast.LENGTH_LONG).show()
                clipboard?.data?.content?.let { it1 -> Log.d(TAG, it1) }
                binding.btcPriceTv.text = clipboard?.data?.content
                val clip: ClipData = ClipData.newPlainText("simple text", clipboard?.data?.content)
                clipboardManager.setPrimaryClip(clip)
            }
        }
    }

    private fun unsubscribe() {
        webSocketClient.send(
            "{\n" +
                    "    \"type\": \"unsubscribe\",\n" +
                    "    \"channels\": [\"ticker\"]\n" +
                    "}"
        )
    }

    private fun createWebSocketClient(coinbaseUri: URI?) {
        webSocketClient = object : WebSocketClient(coinbaseUri) {
            override fun onOpen(handshakedata: ServerHandshake?) {
                Log.d(TAG, "onOpen")
                subscribe()
            }

            override fun onMessage(message: String?) {
                Log.d(TAG, "onMessage: $message")
                setUpBtcPriceText(message)
            }

            override fun onClose(code: Int, reason: String?, remote: Boolean) {
                Log.d(TAG, "onClose")
                unsubscribe()
            }

            override fun onError(ex: Exception?) {
                Log.e(TAG, "onError: ${ex?.message}")
            }

        }
    }

    override fun onResume() {
        super.onResume()
        initWebSocket()
    }

    override fun onPause() {
        super.onPause()
        webSocketClient.close()
    }
}