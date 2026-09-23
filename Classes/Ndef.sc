+ Ndef {
    *playing {
        var space = Ndef.all[\localhost];
        var playing = space !? {
          space.arProxyNames.select { |name| space[name].monitor.isPlaying }
        } ?? [];

        ^if (playing.isEmpty) {
          "No Ndefs playing";
        } {
          "Playing:" + playing.collect { |name| "\\" ++ name }.join(" ");
        };
    }
}
