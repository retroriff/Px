+ Ndef {
    *playing {
        var space = Ndef.all[\localhost];

        var playingProxies = space.arProxyNames.select { |name|
          var proxy = space[name];
          proxy.monitor.isPlaying
        };

        ^if (playingProxies.isEmpty) {
          "No Ndefs playing";
        } {
          "Playing:" + playingProxies.collect { |name| "\\" ++ name }.join(" ");
        };
    }
}
