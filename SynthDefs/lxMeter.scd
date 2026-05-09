(
SynthDef(\lxMeter, { |bus, replyId = 0|
    SendPeakRMS.kr(In.ar(bus, 2), 20, 0.3, "/lxMeter", replyId);
}).add;
)
