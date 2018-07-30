import 'package:flutter/material.dart';
import 'package:my_aliplayer/my_aliplayer.dart';
import 'package:test_video/hero_video.dart';
import 'package:test_video/hero_video_ali.dart';
import 'package:test_video/util/utils.dart';

void main() => runApp(new MyApp());

class MyApp extends StatelessWidget {
  // This widget is the root of your application.
  @override
  Widget build(BuildContext context) {
    return new MaterialApp(
      title: 'Flutter Demo',
      theme: new ThemeData(
        primarySwatch: Colors.blue,
      ),
      home: new MyHomePage(title: 'Flutter Demo Home Page'),
    );
  }
}

class MyHomePage extends StatefulWidget {
  MyHomePage({Key key, this.title}) : super(key: key);

  final String title;

  @override
  _MyHomePageState createState() => new _MyHomePageState();
}

class _MyHomePageState extends State<MyHomePage> {
  int _index = 0;
  @override
  Widget build(BuildContext context) {
    
    return new Scaffold(
      appBar: new AppBar(
        
        title: new Text(widget.title),
      ),
      body: new Center(
        child: new Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: <Widget>[
            new Container(
              margin: new EdgeInsets.only(bottom: 40.0),
              child: new RaisedButton(
                onPressed: (){
                  _index = 1 - _index;
                  setState(() {});
                },
                child: new Text(
                  _index==0?"当前为官方播放器:":"当前为阿里播放器:"
                ),
              ),
            ),
            new Container(
              child:new LimitedBox(
                maxHeight: myUtils.getLogicHeight(700.0),
                child: _buildContent()
              ),
              color: _index==0?Colors.green:Colors.yellow,
              padding: new EdgeInsets.all(10.0),
            )
          ],
        ),
      ),
      floatingActionButton: new FloatingActionButton(
        onPressed: (){
          ///直接打开Android端阿里云视频播放
          MyAliplayer.openVideoActivity();
        },
        child: new Text("原生\n阿里"),
      ), 
    );
  }

  Widget _buildContent(){
    return _index==0?
        //官方video_player,使用的exoPlayer
        new MyVideoPlayer(
          url: "http://hxzhex.zstarpoker.com/sv/c3fa555-164b680cc94/c3fa555-164b680cc94.mp4",
          videoThumb: "http://hxzhex.zstarpoker.com/1a7ccd9340b843a0ba00e41b26443363/covers/3a7c042b93b04a6f8425c5d1c0de43e2-00003.jpg",
        ):
        //模仿官方video_player控件，使用的阿里云视频api
        new MyAliVideoPlayer(
          url: "http://hxzhex.zstarpoker.com/sv/c3fa555-164b680cc94/c3fa555-164b680cc94.mp4",
          videoThumb: "http://hxzhex.zstarpoker.com/1a7ccd9340b843a0ba00e41b26443363/covers/3a7c042b93b04a6f8425c5d1c0de43e2-00003.jpg",
        );
      
  }
}
