import 'package:flutter/material.dart';
import 'package:my_aliplayer/my_aliplayer.dart';
///插件代码没有代码语法提示
///直接拷贝插件源码到本地,写完再拷贝回去
// import 'package:test_video/my_aliplayer_xx.dart';
import 'package:test_video/util/utils.dart';

import 'dart:async';
import 'package:cached_network_image/cached_network_image.dart';

class _VideoPlayPause extends StatefulWidget {
  final VideoPlayerController controller;
  final bool autoPlay;
  final bool isFullScreen;

  _VideoPlayPause(this.controller,{this.autoPlay,this.isFullScreen:false});

  @override
  State createState() {
    return new _VideoPlayPauseState();
  }
}

class _VideoPlayPauseState extends State<_VideoPlayPause> {
  Widget imageFadeAnim;

  void listener() {
    if(!controller.value.isPlaying){
      imageFadeAnim = new Icon(Icons.play_arrow, size: myUtils.getLogicHeight(140.0));
    }
    // print("isBuffering,${controller.value.isBuffering}");
    setState(() {});
  }

  VideoPlayerController get controller => widget.controller;

  @override
  void initState() {
    super.initState();
    controller.addListener(listener);
    if(!widget.isFullScreen){
      // controller.setVolume(0.0);
    }
    if(widget.autoPlay){
      controller.play();
    }else{
      controller.pause();
    }
  }

  @override
  void dispose() {
    controller.removeListener(listener);
    super.dispose();
  }

  //单击暂停或继续
  void onTap(){
    if (!controller.value.initialized) {
      return;
    }
    if (controller.value.isPlaying) {
      controller.pause();
    } else {
      imageFadeAnim = new _FadeAnimation(
        child: new Icon(Icons.pause, size: myUtils.getLogicHeight(140.0))
      );
      controller.play();
    }
  }

  //双击全屏
  void onDoubleTap(BuildContext context){
    if(widget.isFullScreen){
      // controller.setVolume(0.0);
      Navigator.of(context).pop();
      gIsInFullScreen = false;
      return;
    }
    gIsInFullScreen = true;
    // controller.setVolume(1.0);
    Navigator.of(context).push(MaterialPageRoute<Null>(
      builder: (BuildContext context) {
        const colorStart = const Color.fromARGB(255, 26 , 129,233);
        const endStart = const Color.fromARGB(255, 0 ,90,183);
        return Scaffold(
          body: new Container(
              decoration: new BoxDecoration(
                gradient: new LinearGradient(
                  begin: Alignment.topCenter,
                  end: Alignment.bottomCenter,
                  colors: [colorStart,endStart ]
                )
              ),
              padding: const EdgeInsets.all(16.0),
              alignment: Alignment.topLeft,
              child: new Center(
                child: new Hero(
                  tag: controller.toString(),
                  child: new AspectRatio(
                    aspectRatio: controller.value.size.width / controller.value.size.height,
                    child: new _VideoPlayPause(controller,autoPlay: controller.value.isPlaying,isFullScreen:true),
                  ),
                ),
              ),
            )
        );
      }
    ));
  }

  @override
  Widget build(BuildContext context) {
    final List<Widget> children = <Widget>[
      new VideoPlayer(controller),
      new Align(
        alignment: Alignment.bottomCenter,
        child: new VideoProgressIndicator(
          controller,
          allowScrubbing: true,
        ),
      ),
      new Center(child: imageFadeAnim,),
      new Center(
        child: controller.value.isBuffering? const CircularProgressIndicator(): null
      ),
      new GestureDetector(
        onTap: this.onTap,
        // child: new Container(color: Colors.yellow.withOpacity(0.5),),
      ),
      new Align(
        alignment: Alignment.bottomRight,
        child: new GestureDetector(
          onTap: (){
            this.onDoubleTap(context);
          },
          child: new Icon(
            Icons.fullscreen,
            size: myUtils.getLogicHeight(widget.isFullScreen?80.0:40.0),
            color: Colors.white,
          ),
        ),
      )
    ];

    return new Stack(
      fit: StackFit.passthrough,
      children: children,
    );
  }
}

class _FadeAnimation extends StatefulWidget {
  final Widget child;
  final Duration duration;

  _FadeAnimation({this.child, this.duration: const Duration(milliseconds: 500)});

  @override
  _FadeAnimationState createState() => new _FadeAnimationState();
}

class _FadeAnimationState extends State<_FadeAnimation>
    with SingleTickerProviderStateMixin {
  AnimationController animationController;

  @override
  void initState() {
    super.initState();
    animationController =
        new AnimationController(duration: widget.duration, vsync: this);
    animationController.addListener(() {
      if (mounted) {
        setState(() {});
      }
    });
    animationController.forward(from: 0.0);
  }

  @override
  void deactivate() {
    animationController.stop();
    super.deactivate();
  }

  @override
  void didUpdateWidget(_FadeAnimation oldWidget) {
    super.didUpdateWidget(oldWidget);
    if (oldWidget.child != widget.child) {
      animationController.forward(from: 0.0);
    }
  }

  @override
  void dispose() {
    animationController.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return animationController.isAnimating
        ? new Opacity(
            opacity: 1.0 - animationController.value,
            child: widget.child,
          )
        : new Container();
  }
}

/// A widget connecting its life cycle to a [VideoPlayerController] using
/// a data source from the network.
class _NetWorkVideo extends StatefulWidget {
  _NetWorkVideo(this.dataSource,{this.autoPlay,this.videoThumb});
  final String dataSource;
  final bool autoPlay;
  final String videoThumb;

  @override
  _NetWorkVideoState createState() =>
      new _NetWorkVideoState();
}

class _NetWorkVideoState extends State<_NetWorkVideo> {
  VideoPlayerController controller;
  @override
  void initState(){
    super.initState();
  }

  void initControl()async{
    controller = await createVideoPlayerController();
    controller.addListener(() {
      if (controller.value.hasError) {
        print(controller.value.errorDescription);
      }else{
        if(controller.value.isPlaying){
          gVideoControl = controller;
        }
      }
    });
    controller.initialize();
    controller.setLooping(true);
    if(widget.autoPlay){
      controller.play();
    }
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
    controller?.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    if(controller==null){
      var bg = widget.videoThumb.isEmpty?new Container(color: Colors.black54,):GestureDetector(
        child: new CachedNetworkImage(
          imageUrl: widget.videoThumb,
          fit: BoxFit.contain,
        ),
      );
      return new Container(
        alignment: Alignment.center,
        child: Stack(
          alignment: Alignment.center,
          children: <Widget>[
            bg,
            new Icon(Icons.play_arrow,size: myUtils.getLogicHeight(140.0),),
            new GestureDetector(
              onTap: (){
                this.initControl();
              },
            ),
          ],
        ),
      );
    }
    return new AspectRatioVideo(controller,autoPlay:widget.autoPlay,videoThumb: widget.videoThumb,);
  }

  Future<VideoPlayerController> createVideoPlayerController()async {
    return new VideoPlayerController.network(widget.dataSource);
  }
}

class AspectRatioVideo extends StatefulWidget {
  final VideoPlayerController controller;
  final bool autoPlay;
  final String videoThumb;

  AspectRatioVideo(this.controller,{this.autoPlay:false,this.videoThumb:""});

  @override
  _AspectRatioVideoState createState() => new _AspectRatioVideoState();
}

class _AspectRatioVideoState extends State<AspectRatioVideo> {
  VideoPlayerController get controller => widget.controller;
  bool initialized = false;
  @override
  void initState() {
    super.initState();
    
    controller.addListener(listener);
  }
  @override
  dispose(){
    super.dispose();
    controller.removeListener(listener);
  }
  void listener() {
    if (!mounted) {
      return;
    }
    if (!initialized&&controller.value.initialized) {
      initialized = true;
      setState(() {});
    }
  }

  @override
  Widget build(BuildContext context) {
    if (initialized) {
      final Size size = controller.value.size;
      return new Center(
        child: new Hero(
          tag: controller.toString(),
          child: new AspectRatio(
            aspectRatio: size.width / size.height,
            child: new _VideoPlayPause(controller,autoPlay: widget.autoPlay),
          ),
        ),
      );
    } else {
      return new Container(
        color: Colors.black38,
        child: new Stack(
          children: <Widget>[
            widget.videoThumb.isEmpty?new Container():new Center(
              child: new CachedNetworkImage(
                imageUrl: widget.videoThumb,
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

///视频是网络的还是本地的
enum VideoType{
  asset,
  network,
}
class MyAliVideoPlayer extends StatelessWidget{
  MyAliVideoPlayer({
    @required this.url,
    this.type = VideoType.network,
    this.videoThumb = ""
  });
  final String url;
  final String videoThumb;
  ///type
  final VideoType type;
  @override
  Widget build(BuildContext context) {
    if(this.type==VideoType.network){
      return new _NetWorkVideo(
        this.url,
        autoPlay:true,
        videoThumb:this.videoThumb
      );
    }else{
      return new Center(
        child: myUtils.createText("暂无视频"),
      );
    }
  }
}

///正在播放的视频
VideoPlayerController gVideoControl;
bool gIsInFullScreen = false;
