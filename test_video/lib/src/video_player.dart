import 'package:flutter/material.dart';
import 'native_chanel.dart';
import 'aspect_ration_video.dart';
import 'video_param.dart';
import 'dart:async';


class _NetWorkVideo extends StatefulWidget {
  _NetWorkVideo(this.videoParam);
  final VideoParam videoParam;

  @override
  _NetWorkVideoState createState() =>
      new _NetWorkVideoState();
}

class _NetWorkVideoState extends State<_NetWorkVideo> {
  VideoParam get videoParam => widget.videoParam;
  VideoPlayerController controller;
  @override
  void initState(){
    super.initState();
  }

  void _listener(){
    if(controller.eventValue.catchError){
      print(controller.statusValue.errorDescription);
      return;
    }
    if(controller.eventValue.initSuccess){
      controller.setLooping(videoParam.isLooping);
      if(videoParam.volume!=null){
        controller.setVolume(videoParam.volume);
      }
      if(videoParam.brightness!=null){
        controller.setScreenBrigtness(videoParam.brightness);
      }
      if(videoParam.startAt!=null){
        controller.seekTo(videoParam.startAt);
      }
      if(videoParam.title.isNotEmpty){
        controller.setTitle(videoParam.title);
      }
      
      controller.play();
    }
    if(controller.isPlaying){
      gVideoControl = controller;
    }
  }

  void initControl()async{
    controller = await createVideoPlayerController();
    controller.eventNotify.addListener(_listener);
    controller.initialize(
      type: PlayType.url,
      url: videoParam.url
    );
    setState(() {
      
    });
  }

  @override
  void deactivate() {
    super.deactivate();
  }

  @override
  void dispose() {
    if(gVideoControl==controller){
      gVideoControl = null;
    }
    if(controller!=null){
      controller.eventNotify.removeListener(_listener);
      controller.dispose();
    }
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    if(controller==null){
      var bg = widget.videoParam.thumb.isEmpty?new Container(color: Colors.black54,):GestureDetector(
        child: Image.network(
          widget.videoParam.thumb,
          fit: BoxFit.contain,
        ),
      );
      return new Container(
        alignment: Alignment.center,
        child: Stack(
          alignment: Alignment.center,
          children: <Widget>[
            bg,
            new Icon(Icons.play_arrow,size: 80.0),
            new GestureDetector(
              onTap: (){
                this.initControl();
              },
            ),
          ],
        ),
      );
    }
    return new WillPopScope(
      onWillPop: (){
        if(controller!=null&&controller.isPlaying){
          controller.pause();
        }
        return new Future<bool>.value(true);
      },
      child: AspectRatioVideo(controller,thumb: widget.videoParam.thumb),
    );
  }

  Future<VideoPlayerController> createVideoPlayerController()async {
    return new VideoPlayerController();
  }
}


class VideoPlayer extends StatelessWidget{
  VideoPlayer(this.videoParam);
  final VideoParam videoParam;
  @override
  Widget build(BuildContext context) {
    if(this.videoParam.type==DataSourceType.network){
      return new _NetWorkVideo(this.videoParam);
    }else{
      return new Center(
        child: new Text("暂无视频"),
      );
    }
  }
}

///正在播放的视频
VideoPlayerController gVideoControl;
