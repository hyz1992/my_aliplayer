import 'package:flutter/material.dart';
// import 'package:my_aliplayer/my_aliplayer.dart';
import 'package:test_video/my_aliplayer.dart';

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
  int _index = 1;
  @override
  Widget build(BuildContext context) {
    
    return new Scaffold(
      appBar: new AppBar(
        
        title: new Text(widget.title),
      ),
      body: new Center(
        child: new Column(
          // mainAxisAlignment: MainAxisAlignment.center,
          children: <Widget>[
            new Container(
              margin: new EdgeInsets.only(bottom: 40.0),
              child: new RaisedButton(
                onPressed: (){
                  _index = 1-_index;
                  setState(() {});
                },
                child: new Text(
                  "阿里播放器控件"
                ),
              ),
            ),
            new Container(
              child:new SizedBox(
                height: 400.0,
                child: _buildContent()
              ),
              color: _index==1?Colors.green:Colors.yellow,
            )
          ],
        ),
      ),
      floatingActionButton: new FloatingActionButton(
        onPressed: (){
          ///直接打开Android端阿里云视频播放
          openFullscreenVideo(
            PlayType.url,
            url: "http://hxzhex.zstarpoker.com/sv/c3fa555-164b680cc94/c3fa555-164b680cc94.mp4",
            // url: "http://www.sample-videos.com/video/mp4/720/big_buck_bunny_720p_20mb.mp4",
          );
        },
        child: new Text("原生\n阿里"),
      ), 
    );
  }

  Widget _buildContent(){
    return _index==1?
        //模仿官方video_player控件，使用的阿里云视频api
        new VideoPlayer(new VideoParam(
            url: "http://hxzhex.zstarpoker.com/sv/c3fa555-164b680cc94/c3fa555-164b680cc94.mp4",
            // url: "http://www.sample-videos.com/video/mp4/720/big_buck_bunny_720p_20mb.mp4",
            thumb: "http://hxzhex.zstarpoker.com/1a7ccd9340b843a0ba00e41b26443363/covers/3a7c042b93b04a6f8425c5d1c0de43e2-00003.jpg",
            title: "戴眼镜的猫"
          )
        ):new Center();
      
  }
}
