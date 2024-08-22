
import 'my_exo_player_platform_interface.dart';

class MyExoPlayer {
  Future<String?> getPlatformVersion() {
    return MyExoPlayerPlatform.instance.getPlatformVersion();
  }
}
