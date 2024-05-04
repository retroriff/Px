(
a = Ndef(\ns);
a[0] = {
	var deg, degreeFix, env, freq, sig = 0, test, trig, vcf;
    var dur = \dur.kr(0!16);
    var durSize = \durSize.kr(16);
    var degree = \degree.kr(0!256);
    var degreeSize = \degreeSize.kr(256);
    var scale = \scale.kr(Buffer.loadCollection(s, Scale.at(\scriabin)));

    // Sequencer
    dur = Dser(dur, durSize);
    trig = Dseq([dur], inf) / TempoClock.tempo;
    trig = TDuty.ar(trig, 0, 1);

    // Avoids to start with initial 256 degree items
    degreeFix = degree.collect { |item| item.abs };
    degreeSize = Select.kr(degreeFix.sum > 0, [1, degreeSize]);

    // Frequency
    deg = Dser(degree, degreeSize);
    deg = Demand.ar(trig, 0, Dseq([deg], inf));
    freq = (DegreeToKey.ar(scale, deg + \chord.kr([0, 0, 0, 0])) + 48).midicps;
	freq = freq * { Rand(-0.1, 0.1).midiratio }.dup(4);

    // Waveform
    sig = sig + (Pulse.ar(freq) * \pulse.kr(0));
    sig = sig + (Saw.ar(freq, mul: 1.5) * \saw.kr(1));
    sig = sig + (SinOsc.ar(freq, mul: 0.8) * \sine.kr(0));
    sig = sig + (LFTri.ar(freq) * \triangle.kr(0));

    // Envelopes
    env = Env.perc(0.005, 0.5).ar(gate: trig) * 1.6;
    sig = Select.ar(\env.kr(0) > 0, [sig, sig * env]);

    // Filters
    vcf = MoogFF.ar(sig, MouseX.kr(1000, 5000));
    sig = Select.ar(\vcf.kr(1) > 0, [sig, vcf]);
    Splay.ar(sig * \amp.kr(1));
};
)