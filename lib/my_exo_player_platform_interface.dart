import 'package:plugin_platform_interface/plugin_platform_interface.dart';

import 'my_exo_player_method_channel.dart';

abstract class MyExoPlayerPlatform extends PlatformInterface {
  /// Constructs a MyExoPlayerPlatform.
  MyExoPlayerPlatform() : super(token: _token);

  static final Object _token = Object();

  static MyExoPlayerPlatform _instance = MethodChannelMyExoPlayer();

  /// The default instance of [MyExoPlayerPlatform] to use.
  ///
  /// Defaults to [MethodChannelMyExoPlayer].
  static MyExoPlayerPlatform get instance => _instance;

  /// Platform-specific implementations should set this with their own
  /// platform-specific class that extends [MyExoPlayerPlatform] when
  /// they register themselves.
  static set instance(MyExoPlayerPlatform instance) {
    PlatformInterface.verifyToken(instance, _token);
    _instance = instance;
  }

  Future<String?> getPlatformVersion() {
    throw UnimplementedError('platformVersion() has not been implemented.');
  }
}
