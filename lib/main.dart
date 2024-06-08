import 'package:flutter/material.dart';
import 'package:flutter/services.dart';

void main() {
  runApp(MyApp());
}

class MyApp extends StatelessWidget {
  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      home: VpnScreen(),
    );
  }
}

class VpnScreen extends StatefulWidget {
  @override
  _VpnScreenState createState() => _VpnScreenState();
}

class _VpnScreenState extends State<VpnScreen> {
  static const platform = MethodChannel('com.bytesoftlab.true_vpn/vpn');
  String _status = 'Disconnected';

  Future<void> _connectVpn() async {
    try {
      final String result = await platform.invokeMethod('connectVpn');
      setState(() {
        _status = result;
      });
    } catch (e) {
      setState(() {
        _status = 'Failed to connect: $e';
      });
    }
  }

  Future<void> _disconnectVpn() async {
    try {
      final String result = await platform.invokeMethod('disconnectVpn');
      setState(() {
        _status = result;
      });
    } catch (e) {
      setState(() {
        _status = 'Failed to disconnect: $e';
      });
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: Text('VPN Manager'),
      ),
      body: Padding(
        padding: const EdgeInsets.all(16.0),
        child: Column(
          children: [
            ElevatedButton(
              onPressed: _connectVpn,
              child: Text('Connect VPN'),
            ),
            ElevatedButton(
              onPressed: _disconnectVpn,
              child: Text('Disconnect VPN'),
            ),
            SizedBox(height: 20),
            Text('Status: $_status'),
          ],
        ),
      ),
    );
  }
}
