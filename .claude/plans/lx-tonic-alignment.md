# Lx Tonic Alignment

## Context

Lx is a multi-track sample looper. When playing alongside `Nx.tonic`, there's no way to pitch-align a sample to the current harmonic context. This feature adds auto-detection of a sample's root pitch and pitch-shifting to match `Nx.tonic` (or a custom target note). Only works reliably on simple/tonal sounds — by design.

## API

```supercollider
Lx.tonic(0)          // detect pitch of channel 0, align to Nx.tonic
Lx.tonic(0, \F)      // detect pitch, align to F specifically
Lx.tonic(0, nil)     // disable alignment
```

Switching samples (`next`, `prev`, `buf`) auto re-detects when alignment is enabled. Cached results are reused if the same buffer is revisited.

## Files to Modify

### 1. `SynthDefs/grainLoop.scd` — add `pitchRatio` arg

Add `pitchRatio = 1` to args. Multiply into `grainRate`:

```
grainRate = rate * BufRateScale.kr(buf) * pitchRatio * (1 + (spread * TRand.ar(-1, 1, trig)));
```

This works because grain position scanning is independent of grain playback rate — pitch shifts without affecting timing.

### 2. `SynthDefs/loop.scd` — add `pitchRatio` arg + PitchShift

Add `pitchRatio = 1` to args. Insert `PitchShift.ar` after PlayBuf, before envelope:

```
sig = Mix.ar(PlayBuf.ar(...));
sig = PitchShift.ar(sig, 0.2, pitchRatio);
sig = (sig * env) * amp;
```

Can't use `rate` here because it controls tempo-synced playback speed. `PitchShift.ar` preserves duration while shifting pitch. When `pitchRatio = 1`, it's effectively a pass-through (minimal CPU cost per channel).

### 3. `Classes/Lx.sc` — main implementation

**New classvars:**
- `detectedTonics` — Dictionary mapping `bufnum` to detected tonic Symbol
- `tonicTargets` — Dictionary mapping channel index to target (Symbol, `true` for Nx.tonic, or nil)

Initialize both as `Dictionary.new` in `*initClass` and reset in `*loadSamples`.

**New public method: `*tonic`**
- Validates channel, stores target in `tonicTargets[channel]`
- If `nil`, removes alignment and rebuilds pattern
- Otherwise calls `prDetectAndAlign`

**New private method: `*prDetectAndAlign`**
- Checks cache (`detectedTonics[bufnum]`) — if hit, rebuilds pattern immediately
- If miss, calls `prDetectPitch` with async callback that caches result + rebuilds

**New private method: `*prDetectPitch`**
- Creates a temporary analysis synth: `PlayBuf → Pitch.kr → SendReply`
- Uses mono buffer, caps analysis at 3 seconds
- `Pitch.kr` params: `ampThreshold: 0.02, median: 7`
- Collects frequencies via `OSCFunc('/lxPitch')`, filtered by confidence > 0.8 and range 50-2000 Hz
- After buffer plays through: takes median frequency, converts to pitch class via `freq.cpsmidi.round % 12`
- Maps to tonic symbol: `[\C, \Cs, \D, \Ds, \E, \F, \Fs, \G, \Gs, \A, \As, \B]`
- Falls back to `\C` with warning if detection fails (percussive/noisy sample)

**New private method: `*prCalcPitchRatio`**
- Returns `1` if alignment disabled, no cached detection, or Nx unavailable
- Resolves target: `Nx.tonic` if target is `true`, otherwise the stored Symbol
- Calculates: `semitones = targetRoot - sampleRoot` (shortest path, wraps at ±6)
- Returns `semitones.midiratio`

**Modify `*prCreatePattern`:**
- Add `pattern[\pitchRatio] = this.prCalcPitchRatio(channel)` before `super.new(pattern)`

**Modify `*next`, `*prev`, `*buf`:**
- When `tonicTargets[channel].notNil`, call `prDetectAndAlign` instead of `prCreatePattern` (avoids double rebuild; `prDetectAndAlign` calls `prCreatePattern` after detection)

### 4. `Classes/LxGui.sc` — tonic toggle button

Add a "T" toggle button per channel (after the mode buttons row):
- Off state: grey, tonic alignment disabled
- On state: yellow, shows detected tonic symbol
- Click toggles `Lx.tonic(channel)` / `Lx.tonic(channel, nil)`

### 5. Documentation

- `HelpSource/Classes/Lx.schelp` — new `tonic` method section with examples
- `README.md` — add `tonic` to Lx methods table

## Performance

- Pitch detection runs once per sample (async, ~0.5-3.5s), cached by bufnum
- Revisiting a previously analyzed buffer is instant (cache hit)
- `prCalcPitchRatio` is simple arithmetic, called once per pattern rebuild
- `PitchShift.ar` in loop SynthDef has small fixed CPU cost even at ratio 1.0 (SC SynthDefs can't conditionally skip UGens), acceptable for typical 4-8 Lx channels

## Verification

1. Load samples, play without tonic — sound unchanged (pitchRatio defaults to 1)
2. `Lx.tonic(0)` — observe detection message, hear pitch shift
3. Change `Nx.tonic`, call `Lx.play(0)` — hear updated pitch
4. `Lx.next(0)` with tonic enabled — auto re-detects
5. `Lx.tonic(0, nil)` — pitch returns to original
6. Test both `\loop` and `\grain` modes
