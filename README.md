# my_aliplayer
集成阿里云视频播放器到flutter

调试android时，打开test_video\android\app\build.grade的
ndk{
    abiFilters 'armeabi-v7a'
}
调试dart时，注释该代码。

不然就会报缺失libflutter.so的崩溃，我Android不熟，不太明白原因。
