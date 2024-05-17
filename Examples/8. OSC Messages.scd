// We can listen OSC messages sent to 127.0.0.1 on port 57120

Px.listen;

// Messages sent to this netword address will be interpreted

n = NetAddr("127.0.0.1", NetAddr.langPort);
n.sendMsg("/px", "TR08.preset(\\electro, 1)");

// And disable the listener and disconnects all network addresses

Px.listenOff;
