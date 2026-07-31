# Weak First Hit Investigation

## Problem

When playing a pattern, the first hit always sounds weak (lower amplitude) compared to subsequent hits. The issue is most noticeable with short percussive samples (drum kits, hi-hats) because they have sharp transients in the first few milliseconds.

Reproducible with:

```supercollider
909 i: \hh dur: [1, 0.5, 1, 0.5, 1] amp: 1;
1 play: "ki" dur: [1, 0.5, 1, 0.5, 1];
```

The waveform confirms the first hit is visibly quieter than all subsequent hits. Happens every time the pattern is started fresh (after CmdPeriod or stop).

## Debug Steps

We isolated each layer of the signal chain to find where the issue originates:

**Step 1: Raw Pbind (no Pdef, no Ndef)** — OK

```supercollider
Pbind(\instrument, \playbuf, \buf, Px.buf("ki", 0), \dur, Pseq([1, 0.5, 1, 0.5, 1], inf), \amp, 1).play;
```

**Step 2: Pdef with quant** — OK

```supercollider
Pdef(\test, Pbind(\instrument, \playbuf, \buf, Px.buf("ki", 0), \dur, Pseq([1, 0.5, 1, 0.5, 1], inf), \amp, 1)).quant_(4).play;
```

**Step 3: Pdef inside a single Ndef** — BROKEN (first hit is weak)

```supercollider
(
var pdef = Pdef(\test, Pbind(\instrument, \playbuf, \buf, Px.buf("ki", 0), \dur, Pseq([1, 0.5, 1, 0.5, 1], inf), \amp, 1)).quant_(4);
Ndef(\test, pdef).fadeTime_(0).quant_(4).play;
)
```

We also confirmed that `fadeTime` and `quant` are NOT the cause — removing either or both still produces the weak first hit.

## Root Cause

The issue is that **the Ndef monitor and the pattern source are set up simultaneously**. When `Ndef(\test, pdef).play` is called, the source is set and `.play` creates the monitor synth in the same execution. The server needs the monitor to be fully initialized before the first pattern event fires.

**Proof:** Pre-initializing the Ndef monitor before setting the source fixes the issue completely:

```supercollider
// Step 1: Pre-initialize (run this first)
Ndef(\test).fadeTime_(0).play;

// Step 2: Set the source (run this after ~1 second)
(
var pdef = Pdef(\test, Pbind(\instrument, \playbuf, \buf, Px.buf("ki", 0), \dur, Pseq([1, 0.5, 1, 0.5, 1], inf), \amp, 1)).quant_(4);
Ndef(\test, pdef);
)
```

With this approach, the first hit sounds identical to all subsequent hits.

## Suggested Fix

Pre-play `Ndef(\px)` so the monitor is already running before any pattern sets its source. This needs to happen:

1. **At server boot** — so the first pattern after boot works
2. **After CmdPeriod / clear** — so patterns after reset work

### Implementation Challenges

The pre-play must happen **after the server's node tree is fully initialized** (the ProxySpace group must exist). Timing options explored:

| Approach                           | Result                                                                                               |
| ---------------------------------- | ---------------------------------------------------------------------------------------------------- |
| `ServerBoot.add` (direct)          | Too early — server not running yet                                                                   |
| `ServerBoot.add` + `fork { sync }` | Too early — ProxySpace group (1001) doesn't exist                                                    |
| `ServerTree.add` (direct)          | Too early — ProxySpace group not created yet                                                         |
| `ServerTree.add` + `fork { sync }` | Still fires before group exists                                                                      |
| In `clear` method (direct)         | Works when server is running, but `clear` also fires when server is off (CmdPeriod during recompile) |

### Recommended Approach

The cleanest solution is to defer the pre-play until the ProxySpace has set up its group. Options to try:

**Option A: Use `defer` after ServerTree**

```supercollider
ServerTree.add {
  AppClock.sched(0.1, {
    Ndef(\px).fadeTime_(0).play;
    nil;
  });
};
```

**Option B: Use `doWhenBooted` in clear**

```supercollider
*clear {
  // ... clear dictionaries ...
  Ndef(\px).clear;

  Server.default.doWhenBooted {
    Ndef(\px).fadeTime_(0).play;
  };
}
```

**Option C: Pre-play in `Px.new` with fork + sync**

Instead of playing and setting the source in the same call, split them:

```supercollider
if (Ndef(\px).isPlaying.not) {
  Ndef(\px).fadeTime_(0).play;

  fork {
    Server.default.sync;

    if (isNewNdef)
    { Ndef(\px)[0] = { Mix.new(playList.values) } };
  };
};
```

This defers the source setting until after the server has processed the `.play`. The pattern's quant (4 beats) gives plenty of time for this to complete before the first event fires.

## Key Insight

The issue is NOT about `fadeTime` or `quant`. It's a timing issue: the Ndef monitor synth must exist on the server before the first pattern event writes audio to the Ndef's bus. When both are created simultaneously, the monitor misses the transient of the first hit.

## Additional Findings (2026-05-25)

### No programmatic delay works

Tested `fork { Server.default.sync }`, `defer(0.1)`, `defer(1)`, `1.wait`, `AppClock.sched` — none replicate separate manual evaluations. NodeProxy appears to batch all changes from the same top-level evaluation regardless of scheduling.

### Two-Ndef architecture amplifies the issue

Px routes through two Ndef layers: `Ndef(id, pdef)` → `Mix` → `Ndef(\px)`. Even pre-playing `Ndef(\px)` separately doesn't help because the inner `Ndef(id)` is created fresh in the same evaluation as the Mix assignment.

### Pre-warming the full chain works

Creating the inner Ndef with `Silent.ar(2)`, setting up the Mix, and playing the monitor (eval 1) — then swapping only the inner Ndef's source (eval 2) — produces a strong first hit. But this requires truly separate evaluations and knowing pattern IDs in advance.

### `\set` role avoids the issue

Using a persistent UGen function with `\set -> Pbind(...)` works because no new synths are spawned — but this architecture doesn't support sample playback, polyphony, or per-event instruments.

### Solution — Bus-based mixing (NOT SOLVED)

**Branch:** `fix/weak-first-hit`

Replaced the two-Ndef Mix architecture with a shared audio bus:

1. `Ndef(\px)` reads from a shared `mixBus` via `InFeedback.ar` — initialized at server boot and after CmdPeriod (via `prInitMasterNdef` deferred with `fork { Server.default.sync }`)
2. Each pattern's `Ndef(id)` plays directly to `mixBus` with `play(out: mixBus.index, fadeTime: 0)` — the single-Ndef case that works
3. `Mix.new(playList.values)` removed entirely — no more source reassignment on `Ndef(\px)`

This works because `Ndef(\px)` is always pre-initialized from a separate evaluation (CmdPeriod/ServerBoot), and each pattern only creates one Ndef layer to the bus. Per-pattern effects (`Ndef(id).filter(...)`) and master effects (`Ndef(\px).filter(...)`) both still work.

### Status

Not solved. Fix implemented on branch `fix/weak-first-hit`. Must be checked if the first beat is fixed after reboot.
