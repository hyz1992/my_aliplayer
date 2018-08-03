import 'native_chanel.dart';
import 'package:flutter/material.dart';

class MyGesture extends StatefulWidget{
  MyGesture(this.controller,{this.child,this.onTap,this.onDoubleTap,this.isFullScreen:false});
  final VideoPlayerController controller;
  final Widget child;
  final GestureTapCallback onTap;
  final GestureTapCallback onDoubleTap;
  final isFullScreen;
  @override
  State<StatefulWidget> createState() {
    return new _MyGestureState();
  }
}

class _MyGestureState extends State<MyGesture>{
  VideoPlayerController get controller => widget.controller;
  ///快进拖拽值
  double _seekValue;
  ///声音拖拽数值
  double _volumeValue;
  ///亮度拖拽值
  double _brightnessValue;
  ///以上这些值，是增加的趋势还是减少的趋势
  bool _isIncrease = false;

  ///开始滑动的时候的数值
  int curMilliSeconds;
  int curVolume;
  int curBrightness;

  ///滑动时的目标值
  int targetMilliSeconds;
  int targetVolume;
  int targetBrightness;
  @override
  Widget build(BuildContext context) {
    return new GestureDetector(
      behavior: HitTestBehavior.opaque,
      child: new Stack(
        fit: StackFit.passthrough,
        alignment: Alignment.center,
        children: <Widget>[
          new Center(
            child: widget.child,
          ),
          new Center(
            child: _seekValue!=null?_buildSeekView(new Duration(milliseconds: targetMilliSeconds), _isIncrease):null,
          ),
          new Center(
            child: _volumeValue!=null?_buildVolumeView(targetVolume, _isIncrease):null
          ),
          new Center(
            child: _brightnessValue!=null?_buildBrightnessView(targetBrightness, _isIncrease):null,
          )
        ],
      ),
      onTap: widget.onTap,
      onDoubleTap: widget.onDoubleTap,
      onHorizontalDragStart: (DragStartDetails details){
        _seekValue = 0.0;
        curMilliSeconds = controller.statusValue.position.inMilliseconds;
      },
      onHorizontalDragUpdate: (DragUpdateDetails details){
        final RenderBox box = context.findRenderObject();
        _seekValue += details.primaryDelta;
        Duration duration = controller.statusValue.duration;
        double diffValue = _seekValue/box.size.width*duration.inMilliseconds;
        targetMilliSeconds = curMilliSeconds+diffValue~/1;
        targetMilliSeconds = targetMilliSeconds.clamp(0, duration.inMilliseconds);
        if(details.primaryDelta<0.1&&details.primaryDelta>-0.1){
          _isIncrease = _isIncrease;
        }else{
          _isIncrease = details.primaryDelta>0;
          setState(() {});
        }
      },
      onHorizontalDragEnd: (DragEndDetails details){
        _seekValue = null;
        controller.seekTo(new Duration(milliseconds: targetMilliSeconds));
        targetMilliSeconds = 0;
        setState(() {});
      },

      onVerticalDragStart: (DragStartDetails details){
        final RenderBox box = context.findRenderObject();
        Offset pos = box.globalToLocal(details.globalPosition);
        if(pos.dx<box.size.width/2-10){
          _brightnessValue = 0.0;
          curBrightness = controller.statusValue.brigtness;
        }else if(pos.dx>box.size.width/2+10){
          _volumeValue = 0.0;
          curVolume = controller.statusValue.volume;
        }else{
          _volumeValue = null;
          _brightnessValue = null;
        }
      },
      onVerticalDragUpdate: (DragUpdateDetails details){
        if(!widget.isFullScreen){
          return;
        }
        final RenderBox box = context.findRenderObject();
        if(_volumeValue!=null){
          _volumeValue += details.primaryDelta;
          double diffValue = _volumeValue/box.size.height*100;
          targetVolume = curVolume - diffValue~/1;
          targetVolume = targetVolume.clamp(0,100);
          controller.setVolume(targetVolume);
        }else if(_brightnessValue!=null){
          _brightnessValue += details.primaryDelta;
          double diffValue = _brightnessValue/box.size.height*100;
          targetBrightness = curBrightness - diffValue~/1;
          targetBrightness = targetBrightness.clamp(0,100);
          controller.setScreenBrigtness(targetBrightness);
        }else{
          return;
        }
        if(details.primaryDelta<0.1&&details.primaryDelta>-0.1){
          _isIncrease = _isIncrease;
        }else{
          _isIncrease = details.primaryDelta<0;
          setState(() {});
        }

      },
      onVerticalDragEnd: (DragEndDetails details){
        _volumeValue = null;
        _brightnessValue = null;
        setState(() {});
      },
    );
  }
  Widget _buildSeekView(Duration duration,bool isForward){
    return _getBox(
      new Column(
        mainAxisAlignment: MainAxisAlignment.center,
        mainAxisSize: MainAxisSize.min,
        children: <Widget>[
          new Icon(
            isForward?Icons.fast_forward:Icons.fast_rewind,
            size: 40.0,
          ),
          new Padding(
            padding: new EdgeInsets.only(top: 10.0),
            child: new Text(
              _formatDuration(duration),
              style: new TextStyle(
                fontSize: 20.0
              ),
            ),
          )
        ],
      )
    );
  }
  Widget _buildVolumeView(int volume,bool isUp){
    return _getBox(
      new Column(
        mainAxisAlignment: MainAxisAlignment.center,
        mainAxisSize: MainAxisSize.min,
        children: <Widget>[
          new Icon(
            volume<1?Icons.volume_off:(isUp?Icons.volume_up:Icons.volume_down),
            size: 40.0,
          ),
          new Padding(
            padding: new EdgeInsets.only(top: 10.0),
            child: new Text(
              "$volume%",
              style: new TextStyle(
                fontSize: 20.0
              ),
            ),
          )
        ],
      )
    );
  }

  Widget _buildBrightnessView(int brightness,bool isUp){
    return _getBox(
      new Column(
        mainAxisAlignment: MainAxisAlignment.center,
        mainAxisSize: MainAxisSize.min,
        children: <Widget>[
          new Icon(
            Icons.brightness_medium,
            size: 40.0,
          ),
          new Padding(
            padding: new EdgeInsets.only(top: 10.0),
            child: new Text(
              "$brightness%",
              style: new TextStyle(
                fontSize: 20.0
              ),
            ),
          )
        ],
      )
    );
  }

  Widget _getBox(Widget child,{Color bgColor:const Color.fromARGB(150, 255, 255, 255),Color borderColor:Colors.black}){
    return new Container(
      height: 120.0,
      width: 120.0,
      decoration: new BoxDecoration(
        color: bgColor,
        borderRadius: new BorderRadius.only(
          bottomLeft: new Radius.circular(10.0),
          bottomRight: new Radius.circular(10.0),
          topLeft: new Radius.circular(10.0),
          topRight: new Radius.circular(10.0)
        ),
        border: new Border.all(color: borderColor,width: 0.5),
      ),
      child: child,
    );
  }
}

String _formatDuration(Duration position) {
  final int seconds = position.inSeconds%60;
  final int hours = position.inHours%24;
  final int minutes = position.inMinutes%60;
  final String hoursString = hours >= 10 ? '$hours' : hours == 0 ? '00' : '0$hours';
  final String minutesString = minutes >= 10 ? '$minutes' : minutes == 0 ? '00' : '0$minutes';
  final String secondsString = seconds >= 10 ? '$seconds' : seconds == 0 ? '00' : '0$seconds';

  return hours==0?"$minutesString:$secondsString":"$hoursString:$minutesString:$secondsString";
}