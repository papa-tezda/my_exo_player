import 'package:flutter_test/flutter_test.dart';
import 'package:my_exo_player/my_exo_player.dart';
import 'package:my_exo_player/my_exo_player_platform_interface.dart';
import 'package:my_exo_player/my_exo_player_method_channel.dart';
import 'package:plugin_platform_interface/plugin_platform_interface.dart';

class MockMyExoPlayerPlatform
    with MockPlatformInterfaceMixin
    implements MyExoPlayerPlatform {

  @override
  Future<String?> getPlatformVersion() => Future.value('42');
}

void main() {
  final MyExoPlayerPlatform initialPlatform = MyExoPlayerPlatform.instance;

  test('$MethodChannelMyExoPlayer is the default instance', () {
    expect(initialPlatform, isInstanceOf<MethodChannelMyExoPlayer>());
  });

  test('getPlatformVersion', () async {
    MyExoPlayer myExoPlayerPlugin = MyExoPlayer();
    MockMyExoPlayerPlatform fakePlatform = MockMyExoPlayerPlatform();
    MyExoPlayerPlatform.instance = fakePlatform;

    expect(await myExoPlayerPlugin.getPlatformVersion(), '42');
  });
}
