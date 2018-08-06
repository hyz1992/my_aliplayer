# 功能
参考官方的video_player插件，集成阿里云视频播放器为flutter插件

状态栏、标题栏；
垂直滑动控制音量、亮度、水平滑动快进；
适于用作阿里云点播系统播放器，或任何url视频播放器

![image](https://github.com/hyz1992/my_aliplayer/raw/master/preview/1.jpg)
![image](https://github.com/hyz1992/my_aliplayer/raw/master/preview/2.jpg)
![image](https://github.com/hyz1992/my_aliplayer/raw/master/preview/3.jpg)


## 注意
目前只有Android端。
工程的build.grade 要加上
android {
    packagingOptions {
        exclude '/lib/arm64-v8a/**'
    }
}
