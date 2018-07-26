import 'package:flutter/material.dart';
import 'dart:async';
import 'package:flutter/rendering.dart';
import 'package:flutter/painting.dart' show TextStyle;
import 'dart:ui' as ui;
import 'dart:typed_data';
import 'dart:convert';
import 'package:convert/convert.dart';
import 'package:crypto/crypto.dart' as crypto;
import 'dart:io';

Size _designResoulation = const Size(1920.0,1080.0);

enum MyFontSizeEnum{
  size_20,//20
  size_21,//21
  size_22,//22
  size_23,//23
  size_24,//24
  size_25,//25
  size_26,//26
  size_27,//27
  size_28,//28
  size_29,//29
  size_30,//30
  size_32,//32
  size_36,//36
  size_40,//40
  size_45,//45
  size_50,//50
  size_60,//60
  size_80,//80
  size_100,//100
}

///下面 _ftSizeMap、myUtils.createText 的字体大小，是根据乐视2(Le X620)的经验来的,所以实际字体，要换算一下
const double _designDevicePixelRatio = 2.625;
Map<MyFontSizeEnum,double> _ftSizeMap = {
  MyFontSizeEnum.size_20:20.0/_designDevicePixelRatio,
  MyFontSizeEnum.size_21:21.0/_designDevicePixelRatio,
  MyFontSizeEnum.size_22:22.0/_designDevicePixelRatio,
  MyFontSizeEnum.size_23:23.0/_designDevicePixelRatio,
  MyFontSizeEnum.size_24:24.0/_designDevicePixelRatio,
  MyFontSizeEnum.size_25:25.0/_designDevicePixelRatio,
  MyFontSizeEnum.size_26:26.0/_designDevicePixelRatio,
  MyFontSizeEnum.size_27:27.0/_designDevicePixelRatio,
  MyFontSizeEnum.size_28:28.0/_designDevicePixelRatio,
  MyFontSizeEnum.size_29:29.0/_designDevicePixelRatio,
  MyFontSizeEnum.size_30:30.0/_designDevicePixelRatio,
  MyFontSizeEnum.size_32:32.0/_designDevicePixelRatio,
  MyFontSizeEnum.size_36:36.0/_designDevicePixelRatio,
  MyFontSizeEnum.size_40:40.0/_designDevicePixelRatio,
  MyFontSizeEnum.size_45:45.0/_designDevicePixelRatio,
  MyFontSizeEnum.size_50:50.0/_designDevicePixelRatio,
  MyFontSizeEnum.size_60:60.0/_designDevicePixelRatio,
  MyFontSizeEnum.size_80:80.0/_designDevicePixelRatio,
  MyFontSizeEnum.size_100:100.0/_designDevicePixelRatio,
};

_Utils myUtils = new _Utils._();

class _Utils{
  _Utils._();
  final GlobalKey<ScaffoldState> _mainScaffoldKey = new GlobalKey<ScaffoldState>();

  GlobalKey<ScaffoldState> getMainScaffoldKey(){
    return _mainScaffoldKey;
  }
  double getLogicWidth(double physicalWidth){
    double logicWidth = ui.window.physicalSize.width/ui.window.devicePixelRatio;
    return physicalWidth*logicWidth/_designResoulation.width;
  }

  double getLogicHeight(double physicalHeight){
    double logicHeight = ui.window.physicalSize.height/ui.window.devicePixelRatio;
    return physicalHeight*logicHeight/_designResoulation.height;
  }

  Text createText(String str,{
    MyFontSizeEnum sizeEnum:MyFontSizeEnum.size_20,
    Color color:Colors.white,
    Key key,
    TextAlign textAlign, TextDirection textDirection, bool softWrap, TextOverflow overflow,
    double letterSpacing,
    int maxLines,
    TextBaseline textBaseline,
    TextDecoration decoration, 
    Color decorationColor,
    TextDecorationStyle decorationStyle,
    double wordSpacing,
    double lineHeight,
    FontWeight fontWeight,
    FontStyle fontStyle,
    String fontFamily
    })
  {
    TextStyle _style = new TextStyle(
      fontSize: _ftSizeMap[sizeEnum],
      color: color,
      letterSpacing:letterSpacing,
      textBaseline:textBaseline,
      decoration:decoration,
      decorationColor:decorationColor,
      decorationStyle:decorationStyle,
      wordSpacing:wordSpacing,
      height:lineHeight,
      fontStyle: fontStyle,
      fontWeight:fontWeight,
      fontFamily:fontFamily
      );
    double _textScaleFactor = (ui.window.physicalSize.width/_designResoulation.width)/(ui.window.devicePixelRatio/_designDevicePixelRatio);

    return new Text(str,
      style: _style,textScaleFactor: _textScaleFactor,maxLines:maxLines,
      key:key,textAlign: textAlign,textDirection: textDirection,softWrap: softWrap,overflow: overflow,);
  }

  Widget createBgContainer({
    double width,
    double height,
    Alignment alignment,
    EdgeInsetsGeometry margin,
    EdgeInsetsGeometry padding,
    double radius,
    bool ignoreLB:false,//左下没有圆角
    bool ignoreRB:false,//右下
    bool ignoreLT:false,//左上
    bool ignoreRT:false,//右上
    Color color,
    VoidCallback onPressed,
    Border border,
    Widget child}){
      if(radius==null){
        radius = getLogicWidth(24.0);
      }
      if(color==null){
        color = new Color.fromARGB(60, 0, 0, 0);
      }

      Widget ret = new Container(
        alignment:alignment,
        margin: margin,
        padding: padding,
        width: width,
        height: height,
        child: child,
        decoration: new BoxDecoration(
          color: color,
          borderRadius: new BorderRadius.only(
            bottomLeft: new Radius.circular(ignoreLB?0.0:radius),
            bottomRight: new Radius.circular(ignoreRB?0.0:radius),
            topLeft: new Radius.circular(ignoreLT?0.0:radius),
            topRight: new Radius.circular(ignoreRT?0.0:radius)
          ),
          border: border,
        ),
        
      );
      if(onPressed!=null){
        ret = new InkWell(
          child: ret,
          onTap: onPressed,
        );
      }
      return ret;
  }

  Widget createLine(double length,{bool isVertical:false,double sideWidth = 0.6,Color color = Colors.white}){
    return new Container(
      height: isVertical?length:1.0,
      width: (!isVertical)?length:1.0,
      decoration: new BoxDecoration(
        border: new Border(
          left: isVertical?new BorderSide(
            color: color,
            width: sideWidth
          ):BorderSide.none,
          bottom: (!isVertical)?new BorderSide(
            color: color,
            width: sideWidth
          ):BorderSide.none,
        ),
      ),
    );
  }

  void showSnakeBar(String str){
    _mainScaffoldKey.currentState.showSnackBar(new SnackBar(
      content: new Container(
        height: getLogicHeight(80.0),
        alignment: Alignment.center,
        child: createText(
          str,sizeEnum: MyFontSizeEnum.size_30
        ),
      ),
    ));
  }


  Future<Uint8List> capturePng(BuildContext context) async {
    RenderRepaintBoundary boundary = context.findRenderObject();
    ui.Image image = await boundary.toImage();
    ByteData byteData = await image.toByteData(format: ui.ImageByteFormat.rawRgba);
    Uint8List pngBytes = byteData.buffer.asUint8List();
    print(pngBytes);
    return pngBytes;
  }

  ///显示对话框，背景透明
  Future<T> showDialogBlurBg<T>({BuildContext context,Widget dialog,bool barrierDismissible:true,bool bgTouchHide:true,bool backTouchHide:true}){
    double width = MediaQuery.of(context).size.width;
    double height = MediaQuery.of(context).size.height;
    // double _sigma = 1.5;
    if(!backTouchHide){
      dialog = new WillPopScope(
        onWillPop: (){
          return new Future<bool>.value(false);
        },
        child: dialog
      );
    }
    return showDialog<T>(
      barrierDismissible:barrierDismissible,
      context: context,
      builder:(BuildContext context){
        // return new GestureDetector(
        //   onTap: (){
        //     Navigator.pop(context);
        //   },
        //   child: new BackdropFilter(
        //     filter: new ui.ImageFilter.blur(sigmaX: _sigma,sigmaY: _sigma),
        //     child: new Container(
        //       width: width,
        //       height: height,
        //       child: new GestureDetector(
        //         onTap: (){},
        //         child: dialog,
        //       ),
        //       color: Colors.black.withAlpha(0),
        //     ),
        //   ),
        // );
        return new GestureDetector(
          onTap: (){
            if(bgTouchHide){
              Navigator.pop(context);
            }
          },
          child: new Container(
            width: width,
            height: height,
            child: new GestureDetector(
              onTap: (){},
              child: dialog,
            ),
            color: Colors.black.withAlpha(0),
          ),
        );
      }
    );
  }

  ///获取字符串md5
  String stringMd5(String data) {
    var content = new Utf8Encoder().convert(data);
    var md5 = crypto.md5;
    var digest = md5.convert(content);
    return hex.encode(digest.bytes);
  }

  
  void exitApp(){
    exit(0);
  }

  bool isUrl(String url)
  {
    var xx = new RegExp(r"^(((file|gopher|news|nntp|telnet|http|ftp|https|ftps|sftp)://)|(www\.))+(([a-zA-Z0-9\._-]+\.[a-zA-Z]{2,6})|([0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}))(/[a-zA-Z0-9\&%_\./-~-]*)?$");
    return xx.hasMatch(url);
  }

  String getPostfix(String fileName){
    int idx = fileName.lastIndexOf(".");
    String ret = fileName.substring(idx+1,fileName.length);
    return ret;
  }

  Future<String> imageToBase64(String filePath,{bool needDelete:false}) async {
    File imgFile = new File(filePath);

    List<int> imageBytes = imgFile.readAsBytesSync();
    String base64Image = const Base64Codec().encode(imageBytes);
    String postfix = getPostfix(imgFile.path);
    String head = "data:image/$postfix;base64,";
    if(needDelete){
      imgFile.delete();
    }
    return "$head$base64Image";
  }

  void myDeleteFile(String filePath){
    try{
      new File(filePath).delete();
    }catch(e){
      print(e);
    }
  }
}