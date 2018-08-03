
import 'package:meta/meta.dart';

enum PlayType{
  ///使用url播放
  url,
  ///使用vidsts播放
  sts,
}

///视频是网络的还是本地的
enum DataSourceType {
  asset, 
  network, 
  file 
}

class VideoParam{
  VideoParam({
    @required this.url,
    this.title:"",
    this.thumb:"",
    this.type:DataSourceType.network,
    this.isLooping:false,
    this.volume,
    this.brightness,
    this.startAt
  });
  ///url地址
  final String url;
  ///缩略图
  final String thumb;
  ///视频标题
  final String title;
  final DataSourceType type;
  ///是否循环播放
  final bool isLooping;
  ///默认音量
  final int volume;
  ///默认亮度
  final int brightness;
  ///从哪里开始播
  final Duration startAt;
}