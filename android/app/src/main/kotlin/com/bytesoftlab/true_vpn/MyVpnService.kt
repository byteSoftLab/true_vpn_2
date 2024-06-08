package com.bytesoftlab.true_vpn

import android.net.VpnService
import android.content.Intent
import android.os.ParcelFileDescriptor
import android.util.Log
import java.io.FileInputStream
import java.io.OutputStream
import java.net.InetSocketAddress
import java.net.Proxy
import java.net.Socket

class MyVpnService : VpnService() {
    private var vpnInterface: ParcelFileDescriptor? = null

    override fun onCreate() {
        super.onCreate()
        Log.d("MyVpnService", "VPN Service Created")
    }

    override fun onDestroy() {
        super.onDestroy()
        vpnInterface?.close()
        vpnInterface = null
        Log.d("MyVpnService", "VPN Service Destroyed")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val proxyIp = intent?.getStringExtra("proxy_ip") ?: ""
        val proxyPort = intent?.getIntExtra("proxy_port", 0) ?: 0

        if (proxyIp.isNotEmpty() && proxyPort != 0) {
            Log.d("MyVpnService", "Starting VPN with proxy IP: $proxyIp and port: $proxyPort")
            startVpn(proxyIp, proxyPort)
        } else {
            Log.e("MyVpnService", "Invalid proxy IP or port")
        }

        return START_STICKY
    }

    private fun startVpn(proxyIp: String, proxyPort: Int) {
        Log.d("MyVpnService", "Setting up VPN interface")

        if (vpnInterface != null) {
            Log.d("MyVpnService", "VPN interface already exists")
            return
        }

        val builder = Builder()
        builder.addAddress("10.0.0.2", 24) // This is a sample IP address
        builder.addRoute("0.0.0.0", 0) // Route all traffic through the VPN
        vpnInterface = builder.setSession("InternalVPN")
            .setMtu(1500)
            .establish()

        if (vpnInterface == null) {
            Log.e("MyVpnService", "Failed to establish VPN interface")
            return
        }

        Log.d("MyVpnService", "VPN Interface Established")
        routeTrafficThroughProxy(proxyIp, proxyPort)
    }

    private fun routeTrafficThroughProxy(proxyIp: String, proxyPort: Int) {
        try {
            val proxy = Proxy(Proxy.Type.HTTP, InetSocketAddress(proxyIp, proxyPort))
            val socket = Socket(proxy)
            socket.connect(InetSocketAddress("www.google.com", 80), 10000) // Test connection

            val vpnInput = FileInputStream(vpnInterface!!.fileDescriptor)
            val proxyOutput: OutputStream = socket.getOutputStream()

            val buffer = ByteArray(32767)
            var length: Int

            while (vpnInput.read(buffer).also { length = it } > 0) {
                proxyOutput.write(buffer, 0, length)
            }

            vpnInput.close()
            proxyOutput.close()
            socket.close()
            Log.d("MyVpnService", "Traffic successfully routed through proxy")
        } catch (e: Exception) {
            Log.e("MyVpnService", "Error while routing traffic through proxy: ${e.message}")
            e.printStackTrace()
        }
    }
}
