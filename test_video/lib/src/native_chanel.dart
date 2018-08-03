import 'dart:async';

import 'package:flutter/services.dart';
import 'package:flutter/material.dart';
import 'package:meta/meta.dart';
import 'video_param.dart';

const String _dart2Native = "com.hyz.myaliplayer/dart2native";
const String _nativeEvent = "com.hyz.myaliplayer/nativeEvent";

final MethodChannel _channel = const MethodChannel(_dart2Native)
  ..invokeMethod('init');


class DurationRange {
  DurationRange(this.start, this.end);

  final Duration start;
  final Duration end;

  double startFraction(Duration duration) {
    return start.inMilliseconds / duration.inMilliseconds;
  }

  double endFraction(Duration duration) {
    return end.inMilliseconds / duration.inMilliseconds;
  }

  @override
  String toString() => '$runtimeType(start: $start, end: $end)';
}

class _EventValue {
  ///刚刚初始化成功
  bool initSuccess;
  ///播放状态改变。PlayingState属性发生了变化
  bool playStateChange;
  ///发生错误
  bool catchError;
  ///播放进度刷新了
  bool posUpdate;
  ///缓冲进度刷新了
  bool cacheUpdate;
  
  ///卡住加载进度刷新了
  bool loadPercentUpdate;

  _EventValue({
    this.initSuccess:false,
    this.playStateChange:false,
    this.catchError:false,
    this.posUpdate:false,
    this.cacheUpdate:false,
    this.loadPercentUpdate:false,
  });

  void reset(){
    this.initSuccess = false;
    this.playStateChange = false;
    this.catchError = false;
    this.posUpdate = false;
    this.cacheUpdate = false;
    this.loadPercentUpdate = false;
  }

  _EventValue copyWith({
    bool initSuccess,
    bool playStateChange,
    bool catchError,
    bool posUpdate,
    bool cacheUpdate,
    bool loadPercentUpdate,
  }) {
    return new _EventValue(
      initSuccess:initSuccess??this.initSuccess,
      playStateChange:playStateChange??this.playStateChange,
      catchError:catchError??this.catchError,
      posUpdate:posUpdate??this.posUpdate,
      cacheUpdate:cacheUpdate??this.cacheUpdate,
      loadPercentUpdate:loadPercentUpdate??this.loadPercentUpdate,
    );
  }
}

enum PlayingState{
  ///准备了，还没播放
  prepared,
  ///正在播放
  playing,
  ///暂停了
  paused,
  ///播放完成
  completed,
  ///播放终止
  stoped,
}


class _StatusValue {
  ///屏幕亮度 [0-100]
  int brigtness;
  ///是否是静音状态
  bool isSlient;

  ///视频时长
  Duration duration;
  ///视频当前播放进度
  Duration position;

  List<DurationRange> buffered;
  ///是否循环
  bool isLooping;
  ///播放状态
  PlayingState playState;

  ///是否卡住了正在加载
  bool isLoading;
  ///卡住时的加载百分比
  int loadPercent;
  ///音量 [0-100]
  int volume;
  ///视频标题
  String title;

  String errorDescription;
  ///视频尺寸
  Size size;

  _StatusValue({
    @required this.duration,
    this.brigtness:80,
    this.size,
    this.position: const Duration(),
    this.buffered: const <DurationRange>[],
    this.playState: PlayingState.prepared,
    this.isLooping: false,
    this.loadPercent: 0,
    this.volume: 50,
    this.title: "",
    this.errorDescription,
    this.isSlient:false,
    this.isLoading:false
  });

  bool get isPlaying => playState==PlayingState.playing;
  bool get initialized => duration != null;
  double get aspectRatio => size.width / size.height;

  void setValue({
    Duration duration,
    Size size,
    Duration position,
    List<DurationRange> buffered,
    PlayingState playState,
    bool isLooping,
    int loadPercent,
    int volume,
    String title,
    String errorDescription,
    int brigtness,
    bool isSlient,
    bool isLoading,
  }) {
      this.duration = duration ?? this.duration;
      this.size = size ?? this.size;
      this.position = position ?? this.position;
      this.buffered = buffered ?? this.buffered;
      this.playState = playState ?? this.playState;
      this.isLooping = isLooping ?? this.isLooping;
      this.loadPercent = loadPercent ?? this.loadPercent;
      this.volume = volume ?? this.volume;
      this.title = title ?? this.title;
      this.errorDescription = errorDescription ?? this.errorDescription;
      this.brigtness = brigtness??this.brigtness;
      this.isSlient = isSlient??this.isSlient;
      this.isLoading = isLoading??this.isLoading;
  }

  @override
  String toString() {
    return '$runtimeType('
        'duration: $duration, '
        'size: $size, '
        'position: $position, '
        'buffered: [${buffered.join(', ')}], '
        'playState: $playState, '
        'isLooping: $isLooping, '
        'loadPercent: $loadPercent'
        'volume: $volume, '
        'errorDescription: $errorDescription)'
        'brigtness: $brigtness)'
        'isSlient: $isSlient)';
  }
}


class VideoPlayerController {
  _StatusValue statusValue = new _StatusValue(duration: null);
  PlayingState get playState => statusValue.playState;
  bool get isPlaying => statusValue.isPlaying;
  ValueNotifier<_EventValue> eventNotify = new ValueNotifier<_EventValue>(new _EventValue());
  _EventValue get eventValue => eventNotify.value;
  int _textureId;

  String package;
  Timer timer;
  bool isDisposed = false;
  Completer<void> _creatingCompleter;
  StreamSubscription<dynamic> _eventSubscription;
  _VideoAppLifeCycleObserver _lifeCycleObserver;

  Future<void> initialize({
    @required PlayType type,
    ///url播放
    String url:"",

    ///sts播放
    String vid:"",
    String akId:"",
    String akSecre:"",
    String scuToken:"",
  }) async {
    _lifeCycleObserver = new _VideoAppLifeCycleObserver(this);
    _lifeCycleObserver.initialize();
    _creatingCompleter = new Completer<void>();
    
    Map<dynamic, dynamic> response;
    if(type==PlayType.url){
      response = await _channel.invokeMethod(
        'createWithUrl',
        <String, dynamic>{'url': url},
      );
    }else if(type==PlayType.sts){
      response = await _channel.invokeMethod(
        'createWithVid',
        <String, dynamic>{
          "vid":vid,
          "akId":akId,
          "akSecre":akSecre,
          "scuToken":scuToken,
        },
      );
    }else{
      return new Future<void>(null);
    }
      
    _textureId = response['textureId'];
    _creatingCompleter.complete(null);
    final Completer<void> initializingCompleter = new Completer<void>();

    void _initSuccess()async{
      statusValue.setValue(
        volume: await this._getVolume(),
        brigtness:  await this._getScreenBrigtness(),
        title: await this.getTitle(),
      );
      eventNotify.value = eventValue.copyWith(initSuccess:true);
      eventValue.reset();
    }

    void eventListener(dynamic event) {
      final Map<dynamic, dynamic> map = event;
      switch (map['event']) {
        case 'initialized':
          statusValue.setValue(
            duration: new Duration(milliseconds: map['duration']),
            size: new Size(map['width'].toDouble(), map['height'].toDouble()),
          );
          if(!initializingCompleter.isCompleted){
            initializingCompleter.complete(null);
          }
          _initSuccess();
          
          break;
        case 'completed':
          statusValue.setValue(playState: PlayingState.completed);
          eventNotify.value = eventValue.copyWith(playStateChange: true);
          eventValue.reset();
          timer?.cancel();
          timer = null;
          break;
        case 'stoped':
          statusValue.setValue(playState: PlayingState.stoped);
          eventNotify.value = eventValue.copyWith(playStateChange: true);
          eventValue.reset();
          timer?.cancel();
          timer = null;
          break;
        case 'loadStart':
          statusValue.setValue(isLoading:true);
          eventNotify.value = eventValue.copyWith(loadPercentUpdate: true);
          eventValue.reset();
          break;
        case 'loadEnd':
          statusValue.setValue(isLoading:false);
          eventNotify.value = eventValue.copyWith(loadPercentUpdate: true);
          eventValue.reset();
          break;
        case 'loadPersent':
          statusValue.setValue(isLoading:true);
          statusValue.setValue(loadPercent: map["persent"]);
          eventNotify.value = eventValue.copyWith(loadPercentUpdate: true);
          eventValue.reset();
          break;
        case 'catchError':
          // int errorCode = map["errorCode"];
          // int errorEvent = map["errorEvent"];
          String errorMsg = map["errorMsg"];

          statusValue.errorDescription = errorMsg;
          eventNotify.value = eventValue.copyWith(catchError:true);
          eventValue.reset();
          timer?.cancel();
          timer = null;
          break;
        case 'seekComplete':
          if(playState==PlayingState.paused||playState==PlayingState.completed){
            statusValue.setValue(playState: PlayingState.playing);
            eventNotify.value = eventValue.copyWith(playStateChange: true);
            eventValue.reset();
          }
          break;
      }
    }

    void errorListener(Object obj) {
      final PlatformException e = obj;
      statusValue.errorDescription = e.message;
      eventNotify.value = eventValue.copyWith(catchError:true);
      eventValue.reset();
      timer?.cancel();
      timer = null;
    }

    _eventSubscription = _eventChannelFor(_textureId)
        .receiveBroadcastStream()
        .listen(eventListener, onError: errorListener);
    return initializingCompleter.future;
  }

  EventChannel _eventChannelFor(int textureId) {
    return new EventChannel('$_nativeEvent$textureId');
  }

  Future<void> dispose() async {
    if (_creatingCompleter != null) {
      await _creatingCompleter.future;
      if (!isDisposed) {
        isDisposed = true;
        timer?.cancel();
        timer = null;
        await _eventSubscription?.cancel();
        await _channel.invokeMethod(
          'dispose',
          <String, dynamic>{'textureId': _textureId},
        );
      }
      _lifeCycleObserver.dispose();
    }
    isDisposed = true;
    eventNotify.dispose();
  }

  void setLooping(bool looping) {
    statusValue.setValue(isLooping: looping);
    if (!statusValue.initialized || isDisposed) {
      return;
    }
    _channel.invokeMethod(
      'setLooping',
      <String, dynamic>{'textureId': _textureId, 'looping': statusValue.isLooping},
    );
  }

  void play() {
    if (!statusValue.initialized || isDisposed || playState==PlayingState.completed||playState==PlayingState.playing) {
      return;
    }
    statusValue.setValue(playState: PlayingState.playing);
    eventNotify.value = eventValue.copyWith(playStateChange: true);
    eventValue.reset();

    _channel.invokeMethod(
      'play',
      <String, dynamic>{'textureId': _textureId},
    );
    _createTimer();
    
  }

  void pause() {
    if (isDisposed ||playState!=PlayingState.playing) {
      return;
    }
    statusValue.setValue(playState: PlayingState.paused);
    eventNotify.value = eventValue.copyWith(playStateChange: true);
    eventValue.reset();
    timer?.cancel();
    timer = null;
    _channel.invokeMethod(
      'pause',
      <String, dynamic>{'textureId': _textureId},
    );
  }

  void rePlay(){
    if (isDisposed ||playState!=PlayingState.completed) {
      return;
    }
    statusValue.setValue(playState: PlayingState.playing);
    eventNotify.value = eventValue.copyWith(playStateChange: true);
    eventValue.reset();
    _createTimer();
    _channel.invokeMethod(
      'rePlay',
      <String, dynamic>{'textureId': _textureId},
    );
  }

  ///设置是否静音
  void setSlient(bool isSlient){
    statusValue.setValue(isSlient: isSlient);
    if(isSlient){
      if (!statusValue.initialized || isDisposed) {
        return;
      }
      _channel.invokeMethod(
        'setVolume',
        <String, dynamic>{'textureId': _textureId, 'volume': 0},
      );
    }else{
      setVolume(statusValue.volume);
    }
  }

  void _createTimer(){
    timer?.cancel();
    timer = new Timer.periodic(
      const Duration(milliseconds: 500),
      (Timer timer) async {
        if (isDisposed) {
          return;
        }
        final Duration newPosition = await _getPosition();
        if (isDisposed) {
          return;
        }
        statusValue.setValue(position: newPosition);
        eventNotify.value = eventValue.copyWith(posUpdate: true);
        eventValue.reset();

        final List<DurationRange> buffered = await _getBufferPosition();
        statusValue.setValue(buffered: buffered);
        eventNotify.value = eventValue.copyWith(cacheUpdate:true);
        eventValue.reset();
      },
    );
  }

  /// The position in the current video.
  Future<Duration> _getPosition() async {
    if (isDisposed) {
      return null;
    }
    return new Duration(
      milliseconds: await _channel.invokeMethod(
        'getPosition',
        <String, dynamic>{'textureId': _textureId},
      ),
    );
  }

  DurationRange _toDurationRange(dynamic value) {
      final List<dynamic> pair = value;
      return new DurationRange(
        new Duration(milliseconds: pair[0]),
        new Duration(milliseconds: pair[1]),
      );
    }

  Future<List<DurationRange>> _getBufferPosition() async {
    if(isDisposed){
      return null;
    }
    final List<dynamic> values = await _channel.invokeMethod(
        'getBufferingPosition',
        <String, dynamic>{'textureId': _textureId},
      );
    
    List<DurationRange> buffered = values.map<DurationRange>(_toDurationRange).toList();
    return buffered;
  }

  Future<void> seekTo(Duration moment) async {
    if (isDisposed) {
      return;
    }
    if (moment > statusValue.duration) {
      moment = statusValue.duration;
    } else if (moment < const Duration()) {
      moment = const Duration();
    }
    await _channel.invokeMethod('seekTo', <String, dynamic>{
      'textureId': _textureId,
      'location': moment.inMilliseconds,
    });
    statusValue.setValue(position: moment);
  }

  Future<void> setVolume(int volume) async {
    statusValue.setValue(volume: volume.clamp(0, 100));
    if (!statusValue.initialized || isDisposed) {
      return;
    }
    await _channel.invokeMethod(
      'setVolume',
      <String, dynamic>{'textureId': _textureId, 'volume': statusValue.volume},
    );
  }

  Future<int>_getVolume()async{
    int ret = await _channel.invokeMethod(
      'getVolume',
      <String, dynamic>{'textureId': _textureId},
    );
    return ret;
  }

  ///视频源里面的标题
  Future<String>getTitle()async{
    String ret = await _channel.invokeMethod(
      'getTitle',
      <String, dynamic>{'textureId': _textureId},
    );
    return ret;
  }

  void setTitle(String title){
    statusValue.setValue(title: title??"");
  }

  Future<void> setScreenBrigtness(int brigtness) async {
    statusValue.setValue(brigtness: brigtness.clamp(0, 100));
    if (!statusValue.initialized || isDisposed) {
      return;
    }
    await _channel.invokeMethod(
      'setScreenBrigtness',
      <String, dynamic>{'textureId': _textureId, 'brigtness': statusValue.brigtness},
    );
  }

  Future<int>_getScreenBrigtness()async{
    int ret = await _channel.invokeMethod(
      'getScreenBrigtness',
      <String, dynamic>{'textureId': _textureId},
    );
    return ret;
  }
}

class _VideoAppLifeCycleObserver extends WidgetsBindingObserver {
  bool _wasPlayingBeforePause = false;
  final VideoPlayerController _controller;

  _VideoAppLifeCycleObserver(this._controller);

  void initialize() {
    WidgetsBinding.instance.addObserver(this);
  }

  @override
  void didChangeAppLifecycleState(AppLifecycleState state) {
    switch (state) {
      case AppLifecycleState.paused:
        _wasPlayingBeforePause = _controller.isPlaying;
        _controller.pause();
        break;
      case AppLifecycleState.resumed:
        if (_wasPlayingBeforePause) {
          _controller.play();
        }
        break;
      default:
    }
  }

  void dispose() {
    WidgetsBinding.instance.removeObserver(this);
  }
}

/// Displays the video controlled by [controller].
class VideoTexture extends StatelessWidget {
  final VideoPlayerController controller;

  VideoTexture(this.controller);

  @override
  Widget build(BuildContext context) {
    if(controller._textureId == null){
      return new Container(color: Colors.red,);
    }else{
      return new Texture(textureId: controller._textureId);
    }
  }
}

