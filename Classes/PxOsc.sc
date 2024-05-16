/*
TODO: Unit tests
TODO: Update README
TODO: Update examples
*/

+Px {
    *listen {
        if (OSCdef.all[\px].isNil) {
            netAddress = NetAddr("127.0.0.1", 57120);
            this.prPrint("📡 Listening OSC");
            ^listener = OSCdef.new(\px, { |msg|
                var code = msg[1];
                code = code.asString;
                code.interpret;
                this.prPrint(("🤖 " ++ code));
            }, '/px');
        };
        ^this.prPrint("📡 Listener already enabled");
    }

    *listenOff {
        this.prPrint("🙉 Listener disabled");
        listener.free;
        ^netAddress.disconnect;
    }
}
