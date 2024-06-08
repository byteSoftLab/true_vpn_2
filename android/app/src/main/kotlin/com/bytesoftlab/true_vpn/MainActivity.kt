package com.bytesoftlab.true_vpn

import android.app.Activity
import android.content.Intent
import android.net.VpnService
import android.os.Bundle

class MainActivity : Activity() {
    private val VPN_REQUEST_CODE = 100

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        startVpn()
    }

    private fun startVpn() {
        val intent = VpnService.prepare(this)
        if (intent != null) {
            startActivityForResult(intent, VPN_REQUEST_CODE)
        } else {
            onActivityResult(VPN_REQUEST_CODE, RESULT_OK, null)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == VPN_REQUEST_CODE && resultCode == RESULT_OK) {
            val intent = Intent(this, MyVpnService::class.java)
            intent.putExtra("proxy_ip", "your.proxy.ip.address") // Replace with actual proxy IP
            intent.putExtra("proxy_port", 8080) // Replace with actual proxy port
            startService(intent)
        }
    }
}
