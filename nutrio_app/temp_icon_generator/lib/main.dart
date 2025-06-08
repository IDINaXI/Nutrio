import 'package:flutter/material.dart';
import 'dart:ui' as ui;
import 'dart:html' as html;
import 'dart:typed_data';

void main() {
  runApp(const MyApp());
}

class MyApp extends StatelessWidget {
  const MyApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      home: Scaffold(
        body: Center(
          child: Column(
            mainAxisAlignment: MainAxisAlignment.center,
            children: [
              Icon(Icons.eco, size: 512, color: Colors.green),
              const SizedBox(height: 20),
              ElevatedButton(
                onPressed: () async {
                  await generateIcons();
                },
                child: const Text('Generate Icons'),
              ),
            ],
          ),
        ),
      ),
    );
  }

  Future<void> generateIcons() async {
    final sizes = [192, 512];
    final icon = Icons.eco;
    final iconData = IconData(icon.codePoint, fontFamily: icon.fontFamily);
    
    for (final size in sizes) {
      // Draw icon
      final recorder = ui.PictureRecorder();
      final canvas = Canvas(recorder);
      
      final textPainter = TextPainter(
        text: TextSpan(
          text: String.fromCharCode(iconData.codePoint),
          style: TextStyle(
            fontSize: size * 0.8,
            fontFamily: iconData.fontFamily,
            color: Colors.green,
          ),
        ),
        textDirection: TextDirection.ltr,
      );
      
      textPainter.layout();
      textPainter.paint(
        canvas,
        Offset(
          (size - textPainter.width) / 2,
          (size - textPainter.height) / 2,
        ),
      );
      
      final picture = recorder.endRecording();
      final img = await picture.toImage(size, size);
      final byteData = await img.toByteData(format: ui.ImageByteFormat.png);
      final buffer = byteData!.buffer;
      
      // Save regular icon
      final blob = html.Blob([buffer.asUint8List()]);
      final url = html.Url.createObjectUrlFromBlob(blob);
      final anchor = html.AnchorElement(href: url)
        ..setAttribute('download', 'Icon-$size.png')
        ..click();
      html.Url.revokeObjectUrl(url);
      
      // Create maskable icon (with padding)
      final maskableSize = size;
      final maskableRecorder = ui.PictureRecorder();
      final maskableCanvas = Canvas(maskableRecorder);
      
      // Draw background
      maskableCanvas.drawRect(
        Rect.fromLTWH(0, 0, maskableSize.toDouble(), maskableSize.toDouble()),
        Paint()..color = Colors.transparent,
      );
      
      // Draw icon with padding
      final iconSize = size * 0.6; // 60% of the total size for padding
      final textPainterMaskable = TextPainter(
        text: TextSpan(
          text: String.fromCharCode(iconData.codePoint),
          style: TextStyle(
            fontSize: iconSize * 0.8,
            fontFamily: iconData.fontFamily,
            color: Colors.green,
          ),
        ),
        textDirection: TextDirection.ltr,
      );
      
      textPainterMaskable.layout();
      textPainterMaskable.paint(
        maskableCanvas,
        Offset(
          (maskableSize - textPainterMaskable.width) / 2,
          (maskableSize - textPainterMaskable.height) / 2,
        ),
      );
      
      final maskablePicture = maskableRecorder.endRecording();
      final maskableImg = await maskablePicture.toImage(maskableSize, maskableSize);
      final maskableByteData = await maskableImg.toByteData(format: ui.ImageByteFormat.png);
      final maskableBuffer = maskableByteData!.buffer;
      
      // Save maskable icon
      final maskableBlob = html.Blob([maskableBuffer.asUint8List()]);
      final maskableUrl = html.Url.createObjectUrlFromBlob(maskableBlob);
      final maskableAnchor = html.AnchorElement(href: maskableUrl)
        ..setAttribute('download', 'Icon-maskable-$size.png')
        ..click();
      html.Url.revokeObjectUrl(maskableUrl);
    }
    
    // Generate favicon (32x32)
    final faviconRecorder = ui.PictureRecorder();
    final faviconCanvas = Canvas(faviconRecorder);
    
    final faviconTextPainter = TextPainter(
      text: TextSpan(
        text: String.fromCharCode(iconData.codePoint),
        style: TextStyle(
          fontSize: 32 * 0.8,
          fontFamily: iconData.fontFamily,
          color: Colors.green,
        ),
      ),
      textDirection: TextDirection.ltr,
    );
    
    faviconTextPainter.layout();
    faviconTextPainter.paint(
      faviconCanvas,
      Offset(
        (32 - faviconTextPainter.width) / 2,
        (32 - faviconTextPainter.height) / 2,
      ),
    );
    
    final faviconPicture = faviconRecorder.endRecording();
    final faviconImg = await faviconPicture.toImage(32, 32);
    final faviconByteData = await faviconImg.toByteData(format: ui.ImageByteFormat.png);
    final faviconBuffer = faviconByteData!.buffer;
    
    // Save favicon
    final faviconBlob = html.Blob([faviconBuffer.asUint8List()]);
    final faviconUrl = html.Url.createObjectUrlFromBlob(faviconBlob);
    final faviconAnchor = html.AnchorElement(href: faviconUrl)
      ..setAttribute('download', 'favicon.png')
      ..click();
    html.Url.revokeObjectUrl(faviconUrl);
  }
}
