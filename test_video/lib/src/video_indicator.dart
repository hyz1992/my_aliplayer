import 'native_chanel.dart';
import 'package:flutter/material.dart';

class VideoProgressColors {
  final Color playedColor;
  final Color bufferedColor;
  final Color backgroundColor;

  VideoProgressColors({
    this.playedColor: const Color.fromRGBO(0, 200, 222, 1.0),
    this.bufferedColor: const Color.fromRGBO(50, 50, 200, 0.3),
    this.backgroundColor: const Color.fromRGBO(200, 200, 200, 0.7),
  });
}

/// Displays the play/buffering status of the video controlled by [controller].
///
/// [padding] allows to specify some extra padding around the progress indicator
/// that will also detect the gestures.
class VideoProgressIndicator extends StatefulWidget {
  final VideoPlayerController controller;
  final VideoProgressColors colors;
  final EdgeInsets padding;

  VideoProgressIndicator(
    this.controller, {
    VideoProgressColors colors,
    this.padding: const EdgeInsets.only(top: 0.0),
  }) : colors = colors ?? new VideoProgressColors();

  @override
  _VideoProgressIndicatorState createState() =>
      new _VideoProgressIndicatorState();
}

class _VideoProgressIndicatorState extends State<VideoProgressIndicator> {
  VoidCallback listener;

  _VideoProgressIndicatorState() {
    listener = () {
      if (!mounted) {
        return;
      }
      if(controller.eventValue.posUpdate){
        setState(() {});
      }
    };
  }

  VideoPlayerController get controller => widget.controller;
  VideoProgressColors get colors => widget.colors;

  @override
  void initState() {
    super.initState();
    controller.eventNotify.addListener(listener);
  }

  @override
  void deactivate() {
    controller.eventNotify.removeListener(listener);
    super.deactivate();
  }

  @override
  Widget build(BuildContext context) {
    Widget progressIndicator;
    if (controller.statusValue.initialized) {
      final int duration = controller.statusValue.duration.inMilliseconds;
      final int position = controller.statusValue.position.inMilliseconds;

      int maxBuffering = 0;
      for (DurationRange range in controller.statusValue.buffered) {
        final int end = range.end.inMilliseconds;
        if (end > maxBuffering) {
          maxBuffering = end;
        }
      }

      progressIndicator = new Stack(
        fit: StackFit.passthrough,
        children: <Widget>[
          new LinearProgressIndicator(
            value: maxBuffering / duration,
            valueColor: new AlwaysStoppedAnimation<Color>(colors.bufferedColor),
            backgroundColor: colors.backgroundColor,
          ),
          new LinearProgressIndicator(
            value: position / duration,
            valueColor: new AlwaysStoppedAnimation<Color>(colors.playedColor),
            backgroundColor: Colors.transparent,
          ),
        ],
      );
    } else {
      progressIndicator = new LinearProgressIndicator(
        value: null,
        valueColor: new AlwaysStoppedAnimation<Color>(colors.playedColor),
        backgroundColor: colors.backgroundColor,
      );
    }
    final Widget paddedProgressIndicator = new Padding(
      padding: widget.padding,
      child: progressIndicator,
    );
    
    return paddedProgressIndicator;
    
  }
}
