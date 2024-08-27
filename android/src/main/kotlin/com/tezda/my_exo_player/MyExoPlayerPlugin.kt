package com.tezda.my_exo_player
 
import android.content.Context
import android.net.Uri
import android.view.View
import android.widget.FrameLayout
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.ui.PlayerView
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.embedding.engine.plugins.activity.ActivityAware
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result
import io.flutter.plugin.platform.PlatformView
import io.flutter.plugin.platform.PlatformViewFactory
import io.flutter.plugin.common.StandardMessageCodec
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.video.VideoSize
import io.flutter.plugin.common.BinaryMessenger
import android.util.Log
 
 
class MyExoPlayerPlugin : FlutterPlugin, MethodChannel.MethodCallHandler, ActivityAware {
    private lateinit var channel: MethodChannel
    private var binaryMessenger: BinaryMessenger? = null
 
    override fun onAttachedToEngine(flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
        channel = MethodChannel(flutterPluginBinding.binaryMessenger, "my_exo_player")
        channel.setMethodCallHandler(this)
        binaryMessenger = flutterPluginBinding.binaryMessenger
 
        // Register the platform view factory with the binaryMessenger
        flutterPluginBinding
            .platformViewRegistry
            .registerViewFactory("my_exo_player_view", ExoPlayerFactory(flutterPluginBinding.applicationContext, binaryMessenger!!))
    }
 
    override fun onMethodCall(call: MethodCall, result: MethodChannel.Result) {
        // Handle other method calls if needed
        result.notImplemented()
    }
 
    override fun onDetachedFromEngine(binding: FlutterPlugin.FlutterPluginBinding) {
        channel.setMethodCallHandler(null)
        binaryMessenger = null
    }
 
    override fun onAttachedToActivity(binding: ActivityPluginBinding) {}
    override fun onDetachedFromActivityForConfigChanges() {}
    override fun onReattachedToActivityForConfigChanges(binding: ActivityPluginBinding) {}
    override fun onDetachedFromActivity() {}
}
 
class ExoPlayerFactory(
    private val context: Context,
    private val binaryMessenger: BinaryMessenger
) : PlatformViewFactory(StandardMessageCodec.INSTANCE) {
 
    override fun create(context: Context?, id: Int, args: Any?): PlatformView {
        val url = args as? String
        return ExoPlayerView(context!!, url, binaryMessenger) { aspectRatio ->
            MethodChannel(binaryMessenger, "my_exo_player_aspect_ratio_$id")
                .invokeMethod("onAspectRatioChanged", aspectRatio)
                .also { 
                    Log.d("MyExoPlayerPlugin", "Aspect ratio method call sent: $aspectRatio") 
                    Log.d("MyExoPlayerPlugin", "Kotlin hash code: ${url}")
                 }
        }
    }
}
 
class ExoPlayerView(
    context: Context,
    url: String?,
    private val binaryMessenger: BinaryMessenger,
    private val onVideoAspectRatioChanged: (Float) -> Unit
) : PlatformView {
 
    private val playerView: PlayerView = PlayerView(context)
    private var player: ExoPlayer? = ExoPlayer.Builder(context).build()
    private val methodChannel: MethodChannel = MethodChannel(binaryMessenger, "my_exo_player")
    init {
        playerView.player = player
        playerView.setUseController(false)

        player?.apply {
            url?.let {
                val mediaItem = MediaItem.fromUri(Uri.parse(it))
                setMediaItem(mediaItem)
                prepare()
            }
 
            addListener(object : Player.Listener {
                override fun onVideoSizeChanged(videoSize: VideoSize) {
                    super.onVideoSizeChanged(videoSize)
                    val aspectRatio = videoSize.width.toFloat() / videoSize.height.toFloat()
                    Log.d("MyExoPlayerPlugin", "Video size changed: ${videoSize.width}x${videoSize.height}, Aspect Ratio: $aspectRatio")
                    onVideoAspectRatioChanged(aspectRatio)
}
 
                override fun onPlaybackStateChanged(state: Int) {
                    super.onPlaybackStateChanged(state)
                    if (state == Player.STATE_ENDED) {
                        seekToDefaultPosition()
                        playWhenReady = true
                    }
                }
            })
        }
        methodChannel.setMethodCallHandler { call, result ->
            when (call.method) {
                "play" -> {
                    Log.d("MyExoPlayerPlugin", "Play called in method")
                    player?.playWhenReady = true
                    result.success(null)
                }
                "pause" -> {
                    player?.playWhenReady = false
                    result.success(null)
                }
                "dispose" -> {
                     // Handle dispose logic here
                    Log.d("MyExoPlayerPlugin", "Dispose method called")
        // Assume we have a reference to the player to dispose of
                    player?.release()
                    player = null
                    result.success(null)
      }
                else -> result.notImplemented()
            }
        }
    
        playerView.setOnClickListener {
            player?.let {
                if (it.isPlaying) {
                    it.pause()
                } else {
                    it.play()
                }
            }
        }
    }
 
    override fun getView(): View {
        return playerView
    }
 
    override fun dispose() {
        Log.d("MyExoPlayerPlugin", "Player disposed")
        player?.release()
        player = null
    }
}