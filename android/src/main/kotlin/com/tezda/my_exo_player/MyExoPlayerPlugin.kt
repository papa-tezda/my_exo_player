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
        val map = args as? Map<*, *>
        val url = map?.get("url") as? String
        val index = map?.get("index") as? Int ?: 0
        return ExoPlayerView(context!!, url, binaryMessenger, { aspectRatio ->
            MethodChannel(binaryMessenger, "my_exo_player_aspect_ratio_$id")
                .invokeMethod("onAspectRatioChanged", aspectRatio)
                .also { 
                    Log.d("MyExoPlayerPlugin", "Aspect ratio method call sent: $aspectRatio") 
                }
        }, index)
    }
}
 
class ExoPlayerView(
    context: Context,
    url: String?,
    private val binaryMessenger: BinaryMessenger,
    private val onVideoAspectRatioChanged: (Float) -> Unit,
    private val index: Int
) : PlatformView {
 
      private val playerView: PlayerView = PlayerView(context).apply {
        // Set initial layout parameters to fill the screen
        layoutParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.MATCH_PARENT
        )
    }
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
 playWhenReady = false
            addListener(object : Player.Listener {
                override fun onVideoSizeChanged(videoSize: VideoSize) {
                    super.onVideoSizeChanged(videoSize)
                    val aspectRatio = videoSize.width.toFloat() / videoSize.height.toFloat()
                    Log.d("MyExoPlayerPlugin", "Video size changed: ${videoSize.width}x${videoSize.height}, Aspect Ratio: $aspectRatio")
                    onVideoAspectRatioChanged(aspectRatio)
                    adjustLayoutParamsBasedOnAspectRatio(videoSize.width, videoSize.height)
                         
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
                    val playIndex = call.argument<Int>("index")
if (playIndex != null) {
    Log.d("MyExoPlayerPlugin", "Received playIndex: $playIndex")
    Log.d("MyExoPlayerPlugin", "Current index: $index")
    
    if (playIndex == index) {
        player?.seekTo(0)
        player?.playWhenReady = true
    } else {
        player?.playWhenReady = false
        Log.d("MyExoPlayerPlugin", "playIndex does not match index")
    }
} else {
    Log.d("MyExoPlayerPlugin", "playIndex is null")
}
                }
                "pause" -> {
                    val pauseIndex = call.argument<Int>("index")
                    if (pauseIndex == index) {
                        player?.playWhenReady = false
                    }
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
    private fun adjustLayoutParamsBasedOnAspectRatio(width: Int, height: Int) {
        // Calculate the aspect ratio
        val aspectRatio = width.toFloat() / height.toFloat()

        // Adjust layout parameters based on aspect ratio
        val layoutParams = playerView.layoutParams as FrameLayout.LayoutParams
        if (aspectRatio < 1) { // Width is smaller than height
            // Make the playerView occupy the whole screen
            layoutParams.width = FrameLayout.LayoutParams.MATCH_PARENT
            layoutParams.height = FrameLayout.LayoutParams.MATCH_PARENT
        } else {
            // Set the dimensions based on the aspect ratio
            // Here, you can decide to use other sizes or a default size
            layoutParams.width = FrameLayout.LayoutParams.MATCH_PARENT
            layoutParams.height = (layoutParams.width / aspectRatio).toInt()
        }
        playerView.layoutParams = layoutParams
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