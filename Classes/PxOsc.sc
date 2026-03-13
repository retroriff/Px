+Px {
  *listen {
    if (OSCdef.all[\px].isNil) {
      var printExcludedReceivers = ["animatron"];

      NetAddr("127.0.0.1", 57120);

      OSCdef.new(\px, { |msg|
        var code = msg[1].asString;
        var receiver = msg[2].asString;
        var replyPort = msg[3];

        {
            var result = code.interpret;

            if (replyPort.notNil) {
                NetAddr("127.0.0.1", replyPort.asInteger)
                    .sendMsg("/result", result.asString);
            };
        }.defer;

        if (printExcludedReceivers.includesEqual(receiver) == false)
        { this.prPrint(("🤖 " ++ code)) };
      }, '/px');

       ^this.prPrint("📡 Listening OSC");
    };

    ^("📡 Listener already enabled");
  }

  *listenOff {
    OSCdef.all[\px].free;
    NetAddr.disconnectAll;

    ^("🙉 Listener disabled");
  }

  *oscTest {
    if (OSCdef.all[\px].notNil) {
      var addr = NetAddr("127.0.0.1", 57120);
      addr.sendMsg("/px", "\"OSC received\"");

      ^("🚀 Test sent");
    }

    ^("🙉 Listener is disabled");
  }
}
