+Px {
  *listen {
    if (OSCdef.all[\px].isNil) {
      var silentReceivers = ["animatron"];

      NetAddr("127.0.0.1", 57120);

      OSCdef.new(\px, { |msg|
        var code = msg[1].asString;
        var receiver = msg[2].asString;
        var replyPort = msg[3];

        {
            var result;
            var func = code.compile;
            if (func.notNil) {
                try {
                    result = func.value;
                } { |err|
                    result = "ERROR: " ++ err.errorString;
                    err.reportError;
                };
            } {
                result = "COMPILE_ERROR: Code failed to compile (check for undefined classes or syntax errors)";
            };

            if (replyPort.notNil) {
                NetAddr("127.0.0.1", replyPort.asInteger)
                    .sendMsg("/result", result.asString);
            };
        }.defer;

        if (silentReceivers.includesEqual(receiver) == false) {
          this.prPrint(("🤖 " ++ code));
          this.prLogToHistory(code, msg[2]);
        };
      }, '/px').permanent_(true);

       ^("📡 Listening OSC");
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

  *prLogToHistory { |code, receiver|
    if (History.started)
    { History.enter(code, receiver !? { receiver.asSymbol } ?? \osc) };
  }
}
