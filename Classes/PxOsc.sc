+Px {
    *listen {
        if (OSCdef.all[\px].isNil) {
            NetAddr("127.0.0.1", 57120);

            OSCdef.new(\px, { |msg|
                var code = msg[1];
                code = code.asString;
                code.interpret;
                this.prPrint(("🤖 " ++ code));
            }, '/px');

            ^this.prPrint("📡 Listening OSC");
        };

        ^this.prPrint("📡 Listener already enabled");
    }

    *listenOff {
        OSCdef.all[\px].free;
        NetAddr.disconnectAll;

        ^this.prPrint("🙉 Listener disabled");
    }
}
