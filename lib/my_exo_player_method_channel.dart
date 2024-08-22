import 'package:flutter/foundation.dart';
import 'package:flutter/services.dart';

import 'my_exo_player_platform_interface.dart';

/// An implementation of [MyExoPlayerPlatform] that uses method channels.
class MethodChannelMyExoPlayer extends MyExoPlayerPlatform {
  /// The method channel used to interact with the native platform.
  @visibleForTesting
  final methodChannel = const MethodChannel('my_exo_player');

  @override
  Future<String?> getPlatformVersion() async {
    final version = await methodChannel.invokeMethod<String>('getPlatformVersion');
    return version;
  }
}
