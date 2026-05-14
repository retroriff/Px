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

Ndef(\pxSpeech, { |buf = 0, amp = 0.3, pitch = 1, rate = 1, reverb = 0.5|
	var sig, chorus, trig = \trig.tr(1);

	sig = PlayBuf.ar(
		numChannels: 1,
		bufnum: buf,
		rate: BufRateScale.kr(buf) * rate,
		trigger: trig,
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
	sig = sig * 4 * amp;
});

Ndef(\pxVocoder, { |buf = 0, amp = 0.3, pitch = 1, rate = 1, note = 48|
	var sig, trig = \trig.tr(1);
	var carrier, chainMod, chainCarr, chain, vocoded;
	var fund = note.midicps;

	sig = PlayBuf.ar(
		numChannels: 1,
		bufnum: buf,
		rate: BufRateScale.kr(buf) * rate,
		trigger: trig,
		startPos: 0,
		loop: 0,
		doneAction: 0
	);

	sig = PitchShift.ar(sig, pitchRatio: pitch);

	carrier = Saw.ar(fund);

	chainMod = FFT(LocalBuf(2048), sig);
	chainCarr = FFT(LocalBuf(2048), carrier);
	chain = PV_MagMul(chainCarr, chainMod);
	vocoded = IFFT(chain);

	vocoded = Limiter.ar(vocoded, 0.9);
	vocoded = Pan2.ar(vocoded) * amp;
});


~playSpeech = { |buf, mode = \synthdef, args|
	var validModes = [\synthdef, \speech, \vocoder];
	if(validModes.includes(mode).not) {
		"~playSpeech: invalid mode '%'. Valid modes: %".format(mode, validModes).warn;
	} {
		args = args ? [];
		Buffer.readChannel(s, buf, channels:[0], action: { |b|
			switch(mode,
				\synthdef, {
					~speech = Synth(\pxSpeech, [\buf, b] ++ args);
				},
				\speech, {
					Ndef(\pxSpeech).set(*[\buf, b.bufnum, \trig, 1] ++ args);
					Ndef(\pxSpeech).play;
				},
				\vocoder, {
					Ndef(\pxVocoder).set(*[\buf, b.bufnum, \trig, 1] ++ args);
					Ndef(\pxVocoder).play;
				}
			);
		});
	};
};
);

