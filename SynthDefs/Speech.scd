(
SynthDef(\pxSpeech, {
	arg amp = 0.3, buf = 0, out = 0, pitch = 1, rate = 1, reverb = 0.5;
	var dur = BufDur.ir(buf) / rate;
	var env, sig, chorus, tail = 2;

	sig = PlayBuf.ar(
		numChannels: 1,
		bufnum: buf,
		rate: BufRateScale.ir(buf) * rate,
		trigger: 1,
		startPos: 0,
		loop: 0,
		doneAction: 0
	);

	sig = PitchShift.ar(sig, pitchRatio: pitch);
	sig = LPF.ar(sig, 4000);

	chorus = DelayC.ar(sig, maxdelaytime: 0.05, delaytime: SinOsc.kr(0.3).range(0.01, 0.03));
	sig = Pan2.ar(sig, pos: -0.2) + Pan2.ar(chorus, pos: 0.2, level: 0.8);

	sig = sig + CombL.ar(sig, maxdelaytime: 0.5, delaytime: 0.375, decaytime: 3, mul: 0.2);

	sig = FreeVerb.ar(sig, mix: reverb, room: 0.75, damp: 0.3);
	sig = sig * 4;

	env = EnvGen.kr(
		Env(levels: [1, 1, 0], times: [dur, tail], curve: \lin),
		doneAction: 2
	);

	Out.ar(out, sig * env * amp);
}).add;
)

