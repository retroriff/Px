n = NetAddr("192.168.0.16", 57120);
n = NetAddr.localAddr;
OSCFunc.trace(false);

(
// Receive messages
n = NetAddr("127.0.0.1", 57120);
OSCFunc({ Px.play; }, '/play');
OSCFunc({ Px.stop; }, '/stop');
m = NetAddr("127.0.0.1", NetAddr.langPort);
)

// Send messages
m.sendMsg("/play");
m.sendMsg("/stop");