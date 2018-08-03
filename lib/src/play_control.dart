import 'native_chanel.dart';
import 'video_indicator.dart';
import 'video_gesture.dart';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'dart:async';

class _PlayOperate extends StatefulWidget {
  final VideoPlayerController controller;
  final bool isFullScreen;
  final double tipSize;

  _PlayOperate(this.controller,{this.isFullScreen:false,this.tipSize:80.0});

  @override
  State createState() {
    return new _PlayerControlState();
  }
}

double _bottomHeight = 40.0;
double _topmHeight = 40.0;
class _PlayerControlState extends State<_PlayOperate> with TickerProviderStateMixin{
  VideoPlayerController get controller => widget.controller;
  Widget _stateTip;
  AnimationController _aniCtrl;
  Animation<double> _opacity;
  bool showBar = false;
  Timer _cancelTimer;
  void _listener() {
    ///播放状态改变
    if(controller.eventValue.playStateChange){
      if(controller.playState==PlayingState.playing){///开始或者恢复播放
        _stateTip = new FadeTransition(
          opacity: _opacity,
          child: new Icon(Icons.pause, size: widget.tipSize),
        );
        _aniCtrl.forward(from: 0.0);
        showBar = false;
        _closeBarSchedule();
      }
      else if(controller.playState==PlayingState.paused){///暂停
        _stateTip = new Icon(Icons.play_arrow, size: widget.tipSize);
      }
      else if(controller.playState==PlayingState.completed){///播放结束
        _stateTip = new Icon(Icons.replay, size: widget.tipSize);
      }
      setState(() {});
    }
    ///缓冲中
    else if(controller.eventValue.loadPercentUpdate){
      if(controller.statusValue.isLoading){
        int percent = controller.statusValue.loadPercent;
        print(percent);
      }else{

      }
      setState(() {});
    }
    
  }

  @override
  void initState() {
    super.initState();
    _aniCtrl = new AnimationController(
      vsync: this,
      duration: const Duration(milliseconds: 500),
    );
    _opacity = new Tween<double>(begin: 1.0,end: 0.0).animate(_aniCtrl);
    
    controller.eventNotify.addListener(_listener);
    if(controller.playState==PlayingState.paused){///暂停
      _stateTip = new Icon(Icons.play_arrow, size: widget.tipSize);
    }
    else if(controller.playState==PlayingState.completed){///播放结束
      _stateTip = new Icon(Icons.replay, size: widget.tipSize);
    }
  }

  @override
  void dispose() {
    controller.eventNotify.removeListener(_listener);
    _aniCtrl.dispose();
    super.dispose();
  }

  void _openBarSchedule(){
    _cancelTimer = new Timer(new Duration(seconds: 8), (){
      if(showBar){
        showBar = false;
        _cancelTimer = null;
        setState(() {});
      }
    });
  }
  void _closeBarSchedule(){
    _cancelTimer?.cancel();
    _cancelTimer = null;
  }

  //单击暂停或继续
  void onTap({bool force:false}){
    if (!controller.statusValue.initialized) {
      return;
    }
    if (controller.playState==PlayingState.playing) {
      if(force){
        controller.pause();
        _closeBarSchedule();
      }else{
        if(showBar){
          showBar = false;
          _closeBarSchedule();
        }else{
          showBar = true;
          _openBarSchedule();
        }
        setState(() {});
      }
    } else if(controller.playState==PlayingState.paused){
      controller.play();
    }else if(controller.playState==PlayingState.completed){
      controller.rePlay();
    }
  }

  @override
  Widget build(BuildContext context) {
    return new Column(
      children: <Widget>[
        new AnimatedOpacity(
          opacity: showBar?1.0:0.0,
          duration: new Duration(milliseconds: 200),
          child: new Container(
            height: _topmHeight,
            decoration: new BoxDecoration(
              gradient: new LinearGradient(
                begin: Alignment.bottomCenter,
                end: Alignment.topCenter,
                colors: [const Color.fromARGB(0, 0 , 0,0),const Color.fromARGB(200, 0 ,0,0) ]
              )
            ),
            child: new Row(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: <Widget>[
                new Container(
                  padding: new EdgeInsets.only(left: 10.0,top: 4.0),
                  child: new Text(
                    controller.statusValue.title,
                    style: new TextStyle(
                      color: Colors.white.withOpacity(0.7)
                    ),
                  ),
                )
              ],
            ),
          ),
        ),
        new Expanded(
          child: new Stack(
            fit: StackFit.passthrough,
            children: <Widget>[
              new Center(child: _stateTip),
              new Center(
                child: controller.statusValue.isLoading? const CircularProgressIndicator(): null
              ),
              new MyGesture(
                controller,
                isFullScreen: widget.isFullScreen,
                onTap: this.onTap,
                onDoubleTap: (){
                  if(widget.isFullScreen){
                    Navigator.of(context).pop();
                    return;
                  }else{
                    _showFullScreen(context,controller);
                  }
                },
              ),
            ],
          ),
        ),
        new AnimatedOpacity(
          opacity: showBar?1.0:0.0,
          duration: new Duration(milliseconds: 200),
          child: new Container(
            decoration: new BoxDecoration(
              gradient: new LinearGradient(
                begin: Alignment.topCenter,
                end: Alignment.bottomCenter,
                colors: [const Color.fromARGB(0, 0 , 0,0),const Color.fromARGB(200, 0 ,0,0) ]
              )
            ),
            height: _bottomHeight,
            child: new Row(
              crossAxisAlignment: CrossAxisAlignment.center,
              children: <Widget>[
                _buildPlayPause(),
                new DurationString(controller),
                new Expanded(
                  child: new Container(
                    height: 2.0,
                    child: new VideoProgressIndicator(
                      controller,
                    ),
                  ),
                ),
                _buildDuration(),
                _buildExpandButton()
              ],
            ),
          ),
        ),
      ],
    );
  }
  
  GestureDetector _buildPlayPause() {
    return new GestureDetector(
      onTap: (){
        onTap(force: true);
      },
      child: new Container(
        padding: new EdgeInsets.only(
          left: 12.0,
        ),
        child: new Icon(
          controller.isPlaying ? Icons.pause : Icons.play_arrow,
          color: Colors.white,
        ),
      ),
    );
  }

  Widget _buildDuration(){
    final duration = controller.statusValue.duration;
    return new Padding(
      padding: new EdgeInsets.only(left: 12.0),
      child: new Text(
        _formatDuration(duration),
        style: new TextStyle(
          fontSize: 14.0,
          color: Colors.white,
        ),
      ),
    );
  }

  Widget _buildExpandButton() {
    return new GestureDetector(
      onTap: (){
        if(widget.isFullScreen){
          Navigator.of(context).pop();
          return;
        }else{
          _showFullScreen(context,controller);
        }
      },
      child: new Container(
        padding: new EdgeInsets.only(
          left: 8.0,
          right: 8.0,
        ),
        child:  new Icon(
          widget.isFullScreen ? Icons.fullscreen_exit : Icons.fullscreen,
          color: Colors.white,
        ),
      ),
    );
  }
}

//全屏播放
void _showFullScreen(BuildContext context,final VideoPlayerController controller) async{
  final scaffold = Scaffold(
    body: new Container(
      decoration: new BoxDecoration(
        gradient: new LinearGradient(
          begin: Alignment.topCenter,
          end: Alignment.bottomCenter,
          colors: [const Color.fromARGB(255, 26 , 129,233),const Color.fromARGB(255, 0 ,90,183) ]
        )
      ),
      child: new Center(
        child: new Hero(
          tag: controller,
          child: new AspectRatio(
            aspectRatio: controller.statusValue.size.width / controller.statusValue.size.height,
            child: new PlayOperate(controller,isFullScreen: true,)
          ),
        ),
      ),
    )
  );

  final TransitionRoute<Null> page = new PageRouteBuilder<Null>(
    settings: new RouteSettings(isInitialRoute: false),
    pageBuilder: (BuildContext context,Animation<double> animation,Animation<double> secondaryAnimation) {
      return new AnimatedBuilder(
        animation: animation,
        builder: (BuildContext context, Widget child) {
          return scaffold;
        },
      );
    }
  );
  SystemChrome.setEnabledSystemUIOverlays([]);
  Size size = controller.statusValue.size;
  if(size.width/size.height>1.2){//应该横屏
    SystemChrome.setPreferredOrientations([
      DeviceOrientation.landscapeLeft,
      DeviceOrientation.landscapeRight,
    ]);
  }
    
  await Navigator.of(context).push(page);

  SystemChrome.setEnabledSystemUIOverlays(SystemUiOverlay.values);
  SystemChrome.setPreferredOrientations([
    DeviceOrientation.portraitUp,
    DeviceOrientation.portraitDown,
    DeviceOrientation.landscapeLeft,
    DeviceOrientation.landscapeRight,
  ]);
}


class PlayOperate extends StatelessWidget{
  PlayOperate(this.controller,{this.isFullScreen:false});
  final VideoPlayerController controller;
  final bool isFullScreen;
  @override
  Widget build(BuildContext context) {
    return new Stack(
      children: <Widget>[
        new VideoTexture(controller),
        new _PlayOperate(
          controller,
          isFullScreen: isFullScreen,
        )
      ],
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

class DurationString extends StatefulWidget{
  DurationString(this.controller);
  final VideoPlayerController controller;
  @override
  State<StatefulWidget> createState() {
    return new _DurationStringState();
  }
}

class _DurationStringState extends State<DurationString>{
  @override
  void initState() {
    super.initState();
    widget.controller.eventNotify.addListener(_listener);
  }
  @override
  void dispose() {
    widget.controller.eventNotify.removeListener(_listener);
    super.dispose();
  }
  void _listener(){
    setState(() {});
  }
  @override
  Widget build(BuildContext context) {
    final position = widget.controller.statusValue.position;
    return new Padding(
      padding: new EdgeInsets.only(right: 12.0),
      child: new Text(
        _formatDuration(position),
        style: new TextStyle(
          fontSize: 14.0,
          color: Colors.white,
        ),
      ),
    );
  }
}
