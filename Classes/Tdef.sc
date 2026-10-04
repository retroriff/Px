+ Tdef {
    *playing {
        var playing = Tdef.all.select { |tdef| tdef.isPlaying }.keys.asArray;

        ^if (playing.isEmpty) {
          "No Tdefs playing";
        } {
          "Playing:" + playing.collect { |name| "\\" ++ name }.join(" ");
        };
    }
}
