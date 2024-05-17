+Px {
    *listen {
        if (OSCdef.all[\px].isNil) {
            NetAddr("127.0.0.1", 57120);
            this.prPrint("📡 Listening OSC");
            ^OSCdef.new(\px, { |msg|
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
        OSCdef.all[\px].free;
        ^NetAddr.disconnectAll;
    }
}
