library my_aliplayer;
export 'src/video_player.dart';
export 'src/video_param.dart';

import 'package:flutter/services.dart';
import 'src/video_param.dart';

const String _dart2Native = "com.hyz.myaliplayer/dart2native";
final MethodChannel _channel = const MethodChannel(_dart2Native);

void openFullscreenVideo(PlayType type,{
  String url:"",
  String vid:"",
  String akId:"",
  String akScere:"",
  String scuToken:"",
})async{
  if(type==PlayType.url){
    if(url.isEmpty){
      print("error,1播放视频参数错误，\nurl=$url");
      return;
    }
  }else if(type==PlayType.sts){
    if(vid.isEmpty||akId.isEmpty||akScere.isEmpty||scuToken.isEmpty){
      print("error,2播放视频参数错误,\nvid=$vid,akId=$akId,akScere=$akScere,scuToken=$scuToken");
      return;
    }
  }
  await _channel.invokeMethod('openVideoFullscreen',
    <String, dynamic>{
      "playType":type.index,
      "url":url,
      "vid":vid,
      "akId":akId,
      "akScere":akScere,
      "scuToken":scuToken,
    });
}