# 功能
参考官方的video_player插件，集成阿里云视频播放器为flutter插件

![image](https://github.com/hyz1992/my_aliplayer/raw/master/preview/1.png)


![](https://github.com/hyz1992/my_aliplayer/raw/master/preview/2.png)
![](https://github.com/hyz1992/my_aliplayer/raw/master/preview/3.png)


## 注意
目前只有Android端。
调试android时，打开test_video\android\app\build.grade的
ndk{
	abiFilters 'armeabi-v7a'
}
调试dart时，注释该代码。

不然就会报缺失libflutter.so的崩溃，我Android不熟，不太明白原因。