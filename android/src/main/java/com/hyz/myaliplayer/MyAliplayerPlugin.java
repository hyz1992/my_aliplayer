package com.hyz.myaliplayer;

import android.content.Intent;
import android.os.Environment;
import com.aliyun.vodplayer.media.IAliyunVodPlayer;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.flutter.plugin.common.EventChannel;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;
import io.flutter.plugin.common.PluginRegistry.Registrar;
import io.flutter.view.TextureRegistry;

/** MyAliplayerPlugin */
public class MyAliplayerPlugin implements MethodCallHandler {
    static final String Dart2Native = "com.hyz.myaliplayer/dart2native";
    static final String NativeEvent = "com.hyz.myaliplayer/nativeEvent";
    Registrar registrar;
    private final Map<Long, MyVideoPlayer> videoPlayers;

    /** Plugin registration. */
    public static void registerWith(Registrar registrar) {
        final MethodChannel channel = new MethodChannel(registrar.messenger(), Dart2Native);
        channel.setMethodCallHandler(new MyAliplayerPlugin(registrar));
    }
    MyAliplayerPlugin(Registrar registrar){
        this.registrar = registrar;
        this.videoPlayers = new HashMap<>();
    }

    @Override
    public void onMethodCall(MethodCall call, Result result) {
        TextureRegistry textures = registrar.textures();
        if (textures == null) {
            result.error("no_activity", "video_player plugin requires a foreground activity", null);
            return;
        }
        switch (call.method) {
            case "init":
                for (MyVideoPlayer player : videoPlayers.values()) {
                    player.dispose();
                }
                videoPlayers.clear();
                break;
            case "openVideoFullscreen":{
                try{
                    Intent intent = new Intent(registrar.activity(),MySimpleVideo.class);
                    Integer playType = (Integer) call.argument("playType");
                    if(playType==MySimpleVideo.PlayType.url.ordinal()){
                        String url = (String) call.argument("url");
                        intent.putExtra("url",url);
                    }else if(playType==MySimpleVideo.PlayType.sts.ordinal()){
                        String vid = (String) call.argument("videoVid");
                        String akId = (String) call.argument("akId");
                        String akScere = (String) call.argument("akSecre");
                        String scuToken = (String) call.argument("scuToken");
                        intent.putExtra("vid",vid);
                        intent.putExtra("akId",akId);
                        intent.putExtra("akScere",akScere);
                        intent.putExtra("scuToken",scuToken);
                    }else{
                        break;
                    }
                    intent.putExtra("playType",playType);
                    this.registrar.activity().startActivity(intent);
                }catch(Throwable e){
                    e.printStackTrace();
                }
                break;
            }
            case "createWithUrl"://
            {
                String url = (String) call.argument("url");

                final TextureRegistry.SurfaceTextureEntry textureEntry = textures.createSurfaceTexture();
                MyVideoPlayer player = createPlayer(textureEntry,result);

                player.setPlaySource(url);
                break;
            }
            case "createWithVid":
            {
                String vid = (String) call.argument("vid");
                String akId = (String) call.argument("akId");
                String akScere = (String) call.argument("akSecre");
                String scuToken = (String) call.argument("scuToken");

                final TextureRegistry.SurfaceTextureEntry textureEntry = textures.createSurfaceTexture();
                MyVideoPlayer player = createPlayer(textureEntry,result);

                player.setPlaySource(vid,akId,akScere,scuToken);
                break;
            }
            default:
            {
                long textureId = ((Number) call.argument("textureId")).longValue();
                MyVideoPlayer player = videoPlayers.get(textureId);
                if (player == null) {
                    result.error(
                            "Unknown textureId",
                            "No video player associated with texture id " + textureId,
                            null);
                    return;
                }
                onMethodCall(call, result, textureId, player);
                break;
            }
        }
    }

    MyVideoPlayer createPlayer(TextureRegistry.SurfaceTextureEntry textureEntry,Result result){
        EventChannel eventChannel = new EventChannel(registrar.messenger(), NativeEvent + textureEntry.id());

        final MyVideoPlayer player = new MyVideoPlayer(registrar.activeContext(),eventChannel,textureEntry);

        initPlayerCallback(player);
        player.initSurface(textureEntry.surfaceTexture());

        videoPlayers.put(textureEntry.id(), player);
        Map<String, Object> reply = new HashMap<>();
        reply.put("textureId", textureEntry.id());
        result.success(reply);
        return player;
    }

    private void sendInitialized(MyVideoPlayer player) {
        Map<String, Object> event = new HashMap<>();
        event.put("event", "initialized");
        event.put("duration", player.getDuration());
        event.put("width", player.getVideoWidth());
        event.put("height", player.getVideoHeight());

        player.sinkSuccess(event);
    }

    private void sendLoadStart(MyVideoPlayer player){
        Map<String, Object> event = new HashMap<>();
        event.put("event", "loadStart");
        player.sinkSuccess(event);
    }

    private void sendLoadgEnd(MyVideoPlayer player){
        Map<String, Object> event = new HashMap<>();
        event.put("event", "loadEnd");
        player.sinkSuccess(event);
    }

    private void sendLoadPersent(MyVideoPlayer player,int persent){
        Map<String, Object> event = new HashMap<>();
        event.put("event", "loadPersent");
        event.put("persent", persent);
        player.sinkSuccess(event);
    }

    private void sendCompleted(MyVideoPlayer player){
        Map<String, Object> event = new HashMap<>();
        event.put("event", "completed");
        player.sinkSuccess(event);
    }

    private void sendStoped(MyVideoPlayer player){
        Map<String, Object> event = new HashMap<>();
        event.put("event", "stoped");
        player.sinkSuccess(event);
    }

    private void sendReplaySuccess(MyVideoPlayer player){
        Map<String, Object> event = new HashMap<>();
        event.put("event", "rePlaySuccess");
        player.sinkSuccess(event);
    }

    private void sendAutoPlayStarted(MyVideoPlayer player){
        Map<String, Object> event = new HashMap<>();
        event.put("event", "autoPlayStarted");
        player.sinkSuccess(event);
    }

    private void sendCatchError(MyVideoPlayer player,int errorCode, int errorEvent, String errorMsg){
        Map<String, Object> event = new HashMap<>();
        event.put("event", "catchError");
        event.put("errorCode", errorCode);
        event.put("errorEvent", errorEvent);
        event.put("errorMsg", errorMsg);
        player.sinkSuccess(event);
    }

    private void sendSeekComplete(MyVideoPlayer player){
        Map<String, Object> event = new HashMap<>();
        event.put("event", "seekComplete");
        player.sinkSuccess(event);
    }

    private void initPlayerCallback(final MyVideoPlayer player){
        String sdDir = Environment.getExternalStorageDirectory().getAbsolutePath() + "/test_save_cache";
        player.setPlayingCache(true, sdDir, 60 * 60 /*时长, s */, 300 /*大小，MB*/);
        player.setAutoPlay(true);
        player.setOnPreparedListener(new IAliyunVodPlayer.OnPreparedListener(){
            @Override
            public void onPrepared() {

            }
        });
        player.setOnFirstFrameStartListener(new IAliyunVodPlayer.OnFirstFrameStartListener(){
            @Override
            public void onFirstFrameStart() {
                player.resetSurfaceTextureSize();
                sendInitialized(player);
            }
        });
        player.setOnLoadingListener(new IAliyunVodPlayer.OnLoadingListener() {
            @Override
            public void onLoadStart() {
                sendLoadStart(player);
            }

            @Override
            public void onLoadEnd() {
                sendLoadgEnd(player);
            }

            @Override
            public void onLoadProgress(int percent) {
                sendLoadPersent(player,percent);
            }
        });
        player.setOnCompletionListener(new IAliyunVodPlayer.OnCompletionListener() {
            @Override
            public void onCompletion() {
                sendCompleted(player);
            }
        });
        player.setOnErrorListener(new IAliyunVodPlayer.OnErrorListener() {
            @Override
            public void onError(int i, int i1, String s) {
                sendCatchError(player,i,i1,s);
            }
        });
        player.setOnStoppedListener(new IAliyunVodPlayer.OnStoppedListener() {
            @Override
            public void onStopped() {
                sendStoped(player);
            }
        });
        player.setOnRePlayListener(new IAliyunVodPlayer.OnRePlayListener() {
            @Override
            public void onReplaySuccess() {
                sendReplaySuccess(player);
            }
        });
        player.setOnAutoPlayListener(new IAliyunVodPlayer.OnAutoPlayListener() {
            @Override
            public void onAutoPlayStarted() {
                sendAutoPlayStarted(player);
            }
        });
        player.setOnSeekCompleteListener(new IAliyunVodPlayer.OnSeekCompleteListener() {
            @Override
            public void onSeekComplete() {
                sendSeekComplete(player);
            }
        });
    }

    private void onMethodCall(MethodCall call, Result result, long textureId, MyVideoPlayer player) {
        switch (call.method) {
            case "setLooping":
                player.setCirclePlay((Boolean) call.argument("looping"));
                result.success(null);
                break;
            case "play":
                player.play();
                result.success(null);
                break;
            case "stop":
                player.stop();
                result.success(null);
                break;
            case "pause":
                player.pause();
                result.success(null);
                break;
            case "seekTo":
                int location = ((Number) call.argument("location")).intValue();
                player.seekTo(location);
                result.success(null);
                break;
            case "dispose":
                player.dispose();
                videoPlayers.remove(textureId);
                result.success(null);
                break;
            case "getPosition":
                result.success(player.getPosition());
                break;
            case "setVolume":
                player.setVolume((Integer) call.argument("volume"));
                result.success(null);
                break;
            case "getVolume":
                result.success(player.getVolume());
                break;
            case "setScreenBrigtness":
                player.setScreenBrigtness((Integer) call.argument("brigtness"));
                result.success(null);
                break;
            case "getScreenBrigtness":
                result.success(player.getScreenBrigtness());
                break;
            case "getBufferingPosition":
                List<Integer> range = Arrays.asList(0, player.getBufferingPosition());
                // iOS supports a list of buffered ranges, so here is a list with a single range.
                List<List<Integer>> ret = Collections.singletonList(range);
                result.success(ret);
                break;
            case "rePlay":
                player.rePlay();
                result.success(null);
                break;
            case "getTitle":
                String title = player.getTitle();
                result.success(title);
                break;
            default:
                result.notImplemented();
                break;
        }
    }

}
