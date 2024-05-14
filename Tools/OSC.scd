(
var tr08, tr08Preset;

n = NetAddr("127.0.0.1", 57120);
m = NetAddr("127.0.0.1", NetAddr.langPort);

OSCFunc({ Px.play; }, '/play');
OSCFunc({ Px.stop; }, '/stop');

tr08 = OSCFunc({ |msg|
    var action = msg[1];
    var fadeTime = msg[2] ?? 10;
    action = action.asSymbol;

    case
    { action == \play } {
        TR08.play;
        ("🤖 TR08.play;").postln;
    }

    { action == \release } {
        Px.release(fadeTime, \tr08);
        ("🤖 Px.release(fadeTime: " ++ fadeTime ++ ", name: \\tr08)").postln;
    }
    { action == \stop } {
        TR08.stop;
        ("🤖 TR08.stop;").postln;
    };

}, '/tr08');

tr08Preset = OSCFunc({ |msg|
    var preset = msg[1];
    var index = msg[2];

    if (index.notNil and: (preset.notNil)) {
        TR08.preset(preset.asSymbol, index.asInteger);
        ("🤖 TR08.preset(" ++ preset ++ ", " ++ index ++ (");")).postln;
    } {
        "🔴 Missing params".postln;
    };
}, '/tr08/preset');
)

// Send messages
m.sendMsg("/play");
m.sendMsg("/stop");
m.sendMsg("/tr08/preset", \electro, 2);
m.sendMsg("/tr08", \play);
m.sendMsg("/tr08", \release, 5);
m.sendMsg("/tr08", \stop);
Px.release(10, \tr08);

TR08.preset(\electro, 2);
TR08.stop
TR08.play
Px([(i: \bd)]);
Px.nodeProxy
Px.nodeProxy(\px);
Px.nodeProxy(\px)[\px].isPlaying
Px.nodeProxy(\tr08)[\tr08].isPlaying
Px.play
Px.stop(\tr08)
Px.release
