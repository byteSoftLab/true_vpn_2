package com.bytesoftlab.true_vpn

import android.content.Intent
import android.net.VpnService
import android.os.Bundle
import io.flutter.embedding.android.FlutterActivity
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.plugin.common.MethodChannel

class MainActivity : FlutterActivity() {
    private val CHANNEL = "com.bytesoftlab.true_vpn/vpn"

    override fun configureFlutterEngine(flutterEngine: FlutterEngine) {
        super.configureFlutterEngine(flutterEngine)
        MethodChannel(flutterEngine.dartExecutor.binaryMessenger, CHANNEL).setMethodCallHandler { call, result ->
            when (call.method) {
                "connectVpn" -> {
                    val intent = VpnService.prepare(this)
                    if (intent != null) {
                        startActivityForResult(intent, 0)
                    } else {
                        onActivityResult(0, RESULT_OK, null)
                    }
                    result.success("VPN started")
                }
                "disconnectVpn" -> {
                    stopVpnService()
                    result.success("VPN stopped")
                }
                else -> result.notImplemented()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 0 && resultCode == RESULT_OK) {
            val intent = Intent(this, MyVpnService::class.java)
            startService(intent)
        }
    }

    private fun stopVpnService() {
        val intent = Intent(this, MyVpnService::class.java)
        stopService(intent)
    }
}
