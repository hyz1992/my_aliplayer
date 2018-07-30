package com.hyz.myaliplayer;

import android.content.Intent;
import android.os.Environment;
import com.aliyun.vodplayer.media.IAliyunVodPlayer;

import java.util.HashMap;
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
            case "openVideoActivity":{
                Intent intent = new Intent(registrar.activity(),MySimpleVideo.class);
                this.registrar.activity().startActivity(intent);
                break;
            }
            case "create"://createWithUrl
            {
                String url = (String) call.argument("uri");

                final TextureRegistry.SurfaceTextureEntry textureEntry = textures.createSurfaceTexture();
                MyVideoPlayer player = createPlayer(textureEntry,result);

                player.setPlaySource(url);
                break;
            }
            case "createWithVid":
            {
                String vid = (String) call.argument("videoVid");
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

        final MyVideoPlayer player = new MyVideoPlayer(registrar.context(),eventChannel,textureEntry);

        initPlayerCallback(player);
        player.initSurface(textureEntry.surfaceTexture());

        videoPlayers.put(textureEntry.id(), player);
        Map<String, Object> reply = new HashMap<>();
        reply.put("textureId", textureEntry.id());
        result.success(reply);
        return player;
    }

    private void sendInitialized(MyVideoPlayer player,EventChannel.EventSink eventSink) {
        if (eventSink != null) {
            Map<String, Object> event = new HashMap<>();
            event.put("event", "initialized");
            event.put("duration", player.getDuration());
            event.put("width", player.getVideoWidth());
            event.put("height", player.getVideoHeight());

            eventSink.success(event);
        }
    }
    private void initPlayerCallback(final MyVideoPlayer player){
        String sdDir = Environment.getExternalStorageDirectory().getAbsolutePath() + "/test_save_cache";
        player.setPlayingCache(true, sdDir, 60 * 60 /*时长, s */, 300 /*大小，MB*/);
        player.setOnPreparedListener(new IAliyunVodPlayer.OnPreparedListener(){
            @Override
            public void onPrepared() {
                sendInitialized(player,player.eventSink);
            }
        });
    }

    private void onMethodCall(MethodCall call, Result result, long textureId, MyVideoPlayer player) {
        switch (call.method) {
            case "setLooping":
                player.setCirclePlay((Boolean) call.argument("looping"));
                result.success(null);
                break;
            case "setVolume":
                player.setCurrentVolume((Integer) call.argument("volume"));
                result.success(null);
                break;
            case "play":
                player.play();
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
            case "position":
                result.success(player.getPosition());
                break;
            case "dispose":
                player.dispose();
                videoPlayers.remove(textureId);
                result.success(null);
                break;
            default:
                result.notImplemented();
                break;
        }
    }

}
