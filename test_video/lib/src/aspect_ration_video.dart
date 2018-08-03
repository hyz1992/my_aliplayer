import 'package:flutter/material.dart';
import 'native_chanel.dart';
import 'play_control.dart';

class AspectRatioVideo extends StatefulWidget {
  final VideoPlayerController controller;
  final String thumb;

  AspectRatioVideo(this.controller,{this.thumb:""});

  @override
  _AspectRatioVideoState createState() => new _AspectRatioVideoState();
}

class _AspectRatioVideoState extends State<AspectRatioVideo> {
  VideoPlayerController get controller => widget.controller;
  bool initialized = false;
  @override
  void initState() {
    super.initState();
    controller.eventNotify.addListener(_listener);
  }
  @override
  dispose(){
    super.dispose();
    controller.eventNotify.removeListener(_listener);
  }
  void _listener() {
    if (!mounted) {
      return;
    }
    if (!initialized&&controller.eventValue.initSuccess) {
      initialized = true;
      setState(() {});
    }
  }

  @override
  Widget build(BuildContext context) {
    if (initialized) {
      final Size size = controller.statusValue.size;
      return new Center(
        child: new Hero(
          tag: controller,
          child: new AspectRatio(
            aspectRatio: size.width / size.height,
            child: new PlayOperate(controller)
          ),
        ),
      );
    } else {
      return new Container(
        // color: Colors.black38,
        child: new Stack(
          children: <Widget>[
            widget.thumb.isEmpty?new Container():new Center(
              child: Image.network(
                widget.thumb,
                fit: BoxFit.contain,
              ),
            ),
            new Center(
              child: new CircularProgressIndicator(),
            )
          ],
        )
      );
    }
  }
}