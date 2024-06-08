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
  final TextEditingController _ipController = TextEditingController();
  final TextEditingController _portController = TextEditingController();
  String _status = 'Disconnected';

  Future<void> _connectVpn() async {
    try {
      final String result = await platform.invokeMethod('connectVpn', {
        'ip': _ipController.text,
        'port': int.parse(_portController.text),
      });
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
        title: Text('VPN Proxy Manager'),
      ),
      body: Padding(
        padding: const EdgeInsets.all(16.0),
        child: Column(
          children: [
            TextField(
              controller: _ipController,
              decoration: InputDecoration(
                labelText: 'Proxy IP',
                hintText: 'Enter Proxy IP',
              ),
            ),
            TextField(
              controller: _portController,
              decoration: InputDecoration(
                labelText: 'Proxy Port',
                hintText: 'Enter Proxy Port',
              ),
              keyboardType: TextInputType.number,
            ),
            SizedBox(height: 20),
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
