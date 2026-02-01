(
~sxBus = Bus.audio(s, 2);

SynthDef(\sx, {
  var chord, chordActive, chordSeq, deg, degreeFix, env, envPad, freq, freqDirect, freqSeq, outChord, outMono, sig = 0, sigMono, trig, vcf;
  var atk = \atk.kr(0.01);
  var chord = \chord.kr([0, 0, 0, 0]);
  var degree = \degree.kr(0 ! 256);
  var degreeSize = \degreeSize.kr(256);
  var dur = \dur.kr(0 ! 16);
  var durSize = \durSize.kr(16);
  var gate = \gate.kr(1);
  var lag = \lag.kr(0).clip(0, 5);
  var midinote = \midinote.kr(60);
  var rel = \rel.kr(0.5);
  var root = \root.kr(48);
  var scale = \scale.kr(Buffer.loadCollection(s, Scale.at(\scriabin)));
  var seq = \seq.kr(1);
  var tempo = \tempo.kr(0);

  // Sequencer (seq=1)
  dur = Dser(dur, durSize);
  tempo = Select.kr(tempo > 0, [TempoClock.default.tempo, tempo]);
  trig = Dseq([dur], inf) / tempo;
  trig = TDuty.ar(trig);

  degreeFix = degree.collect { |item| item.abs };
  degreeSize = Select.kr(degreeFix.sum > 0, [1, degreeSize]);

  deg = Dser(degree, degreeSize);
  deg = Demand.ar(trig, 0, Dseq([deg], inf));
  freqSeq = (DegreeToKey.ar(scale, deg + chord) + root).midicps;

  // Direct mode (seq=0)
  freqDirect = midinote.midicps;

  // Frequency selection
  freq = Select.ar(seq, [K2A.ar(freqDirect), freqSeq]);
  freq = freq * Rand(-0.1, 0.1).midiratio;
  freq = freq.lag2(lag);

  // Waveform
  sig = sig + (Pulse.ar(freq) * \pulse.kr(0));
  sig = sig + (Saw.ar(freq, mul: 2.dbamp) * \saw.kr(1));
  sig = sig + (SinOsc.ar(freq, mul: 4.dbamp) * \sine.kr(0));
  sig = sig + (LFTri.ar(freq, mul: 2.dbamp) * \triangle.kr(0));

  // Envelope
  envPad = EnvGen.kr(Env.asr(atk, 1, rel), gate);
  env = Select.kr(seq, [
    envPad,
    Select.kr(\env.kr(0) > 0, [1, envPad])
  ]);

  // Free synth only in pad mode when gate closes
  FreeSelf.kr(Done.kr(envPad) * (1 - seq));

  // Filters
  vcf = MoogFF.ar(sig, MouseX.kr(1000, 5000));
  sig = Select.ar(\vcf.kr(1) > 0, [sig * -9.dbamp, vcf]);

  // Output
  chordActive = chord.abs.sum > 0;
  chordSeq = chordActive * seq;
  sigMono = Mix.ar(sig) / 4;

  outMono = Pan2.ar(sigMono * env * \amp.kr(1) * 0.25, \pan.kr(0));
  outChord = Pan2.ar(sig * env * \amp.kr(1) * 0.25, \pan.kr(0));

  sigMono.poll(label: "mono");     // Should print 1 value
  sig.poll(label: "sig");          // Before output mixing - prints 4 values
  outMono.poll(label: "outMono");  // Prints 2 values (stereo)
  outChord.poll(label: "outChord"); // Prints 8 values (4 stereo pairs)

  sig = (outMono * (1 - chordSeq)) + (outChord * chordSeq);
  Out.ar(~sxBus, sig);
}).add;
)
