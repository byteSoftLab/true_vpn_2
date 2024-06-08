package com.bytesoftlab.true_vpn

import android.net.VpnService
import android.content.Intent
import android.os.ParcelFileDescriptor
import android.util.Log
import java.io.*
import java.net.InetSocketAddress
import java.net.Proxy
import java.net.Socket

class MyVpnService : VpnService() {
    private var vpnInterface: ParcelFileDescriptor? = null
    private val TAG = "MyVpnService"

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "VPN Service Created")
    }

    override fun onDestroy() {
        super.onDestroy()
        vpnInterface?.close()
        vpnInterface = null
        Log.d(TAG, "VPN Service Destroyed")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val proxyIp = "38.154.195.250" // Your HTTP proxy IP
        val proxyPort = 9338 // Your HTTP proxy port

        Log.d(TAG, "Starting VPN with proxy IP: $proxyIp and port: $proxyPort")
        startVpn(proxyIp, proxyPort)
        
        return START_STICKY
    }

    private fun startVpn(proxyIp: String, proxyPort: Int) {
        Log.d(TAG, "Setting up VPN interface")

        if (vpnInterface != null) {
            Log.d(TAG, "VPN interface already exists")
            return
        }

        val builder = Builder()
        builder.addAddress("10.0.0.2", 24) // Internal VPN IP
        builder.addRoute("0.0.0.0", 0) // Route all traffic through the VPN
        builder.addDnsServer("8.8.8.8") // Google Public DNS
        builder.addDnsServer("8.8.4.4") // Secondary DNS server

        vpnInterface = builder.setSession("InternalVPN")
            .setMtu(1500)
            .establish()

        if (vpnInterface == null) {
            Log.e(TAG, "Failed to establish VPN interface")
            return
        }

        Log.d(TAG, "VPN Interface Established")
        handleTrafficThroughHttpProxy(proxyIp, proxyPort)
    }

    private fun handleTrafficThroughHttpProxy(proxyIp: String, proxyPort: Int) {
        try {
            Log.d(TAG, "Connecting to HTTP proxy $proxyIp:$proxyPort")
            val proxy = Proxy(Proxy.Type.HTTP, InetSocketAddress(proxyIp, proxyPort))
            
            val socket = Socket(proxy)
            socket.connect(InetSocketAddress("8.8.8.8", 80), 10000) // Test connection

            Log.d(TAG, "Proxy connection established")

            val vpnInput: InputStream = FileInputStream(vpnInterface!!.fileDescriptor)
            val proxyOutput: OutputStream = socket.getOutputStream()
            val proxyInput: InputStream = socket.getInputStream()
            val vpnOutput: OutputStream = FileOutputStream(vpnInterface!!.fileDescriptor)

            val buffer = ByteArray(32767)
            var length: Int

            // Forward data from VPN to Proxy
            Thread {
                try {
                    while (vpnInput.read(buffer).also { length = it } > 0) {
                        proxyOutput.write(buffer, 0, length)
                        proxyOutput.flush()
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error forwarding data from VPN to Proxy: ${e.message}")
                }
            }.start()

            // Forward data from Proxy to VPN
            Thread {
                try {
                    while (proxyInput.read(buffer).also { length = it } > 0) {
                        vpnOutput.write(buffer, 0, length)
                        vpnOutput.flush()
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error forwarding data from Proxy to VPN: ${e.message}")
                }
            }.start()

            Log.d(TAG, "Traffic successfully routed through proxy")
        } catch (e: Exception) {
            Log.e(TAG, "Error routing traffic through proxy: ${e.message}")
            e.printStackTrace()
        }
    }
}
