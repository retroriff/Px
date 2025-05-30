(
~sxBus = Bus.audio(s, 2);

SynthDef(\sx, {
	var deg, degreeFix, env, freq, sig = 0, trig, vcf;
    var degree = \degree.kr(0 ! 256);
    var degreeSize = \degreeSize.kr(256);
    var dur = \dur.kr(0 ! 16);
    var durSize = \durSize.kr(16);
    var lag = \lag.kr(0).clip(0, 5);
    var scale = \scale.kr(Buffer.loadCollection(s, Scale.at(\scriabin)));
    var tempo = \tempo.kr(0);

    // Sequencer
    dur = Dser(dur, durSize);
    tempo = Select.kr(tempo > 0, [TempoClock.default.tempo, tempo]);
    trig = Dseq([dur], inf) / tempo;
    trig = TDuty.ar(trig);

    // Avoids to start with initial 256 degree items
    degreeFix = degree.collect { |item| item.abs };
    degreeSize = Select.kr(degreeFix.sum > 0, [1, degreeSize]);

    // Frequency
    deg = Dser(degree, degreeSize);
    deg = Demand.ar(trig, 0, Dseq([deg], inf));
    freq = (DegreeToKey.ar(scale, deg + \chord.kr([0, 0, 0, 0])) + 48).midicps;
	freq = freq * { Rand(-0.1, 0.1).midiratio }.dup(4);
    freq = freq.lag2(lag);

    // Waveform
    sig = sig + (Pulse.ar(freq) * \pulse.kr(0));
    sig = sig + (Saw.ar(freq, mul: 2.dbamp) * \saw.kr(1));
    sig = sig + (SinOsc.ar(freq, mul: 4.dbamp) * \sine.kr(0));
    sig = sig + (LFTri.ar(freq, mul: 2.dbamp) * \triangle.kr(0));

    // Envelopes
    env = Env.perc(0.005, 0.5).ar(gate: trig) * 1.6;
    sig = Select.ar(\env.kr(0) > 0, [sig, sig * env]);

    // Filters
    vcf = MoogFF.ar(sig, MouseX.kr(1000, 5000));
    sig = Select.ar(\vcf.kr(1) > 0, [sig * -9.dbamp, vcf]);

    sig = Splay.ar(sig * \amp.kr(1));
    Out.ar(~sxBus, sig);
}).add;
)
