# 目标
参考官方的video_player插件，集成阿里云视频播放器到flutter控件系统

## 注意
目前只有Android端。
调试android时，打开test_video\android\app\build.grade的
ndk{
	abiFilters 'armeabi-v7a'
}
调试dart时，注释该代码。

不然就会报缺失libflutter.so的崩溃。
