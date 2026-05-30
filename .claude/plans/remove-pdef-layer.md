# Remove Pdef layer

## Context

Pdef wraps Pbind unnecessarily since Ndef already acts as the named pattern container. Removing Pdef simplifies the architecture: Pbind goes directly to Ndef, and all stop/trace operations use Ndef instead of Pdef.

**Prerequisite:** The bus-based mixing refactor on `fix/weak-first-hit` must be tested and stable first.

## Changes

### 1. Rename `prCreatePdef` to `prCreatePbind` — `Px.sc`

Remove the `Pdef` wrapper. Return the Pbind directly:

```supercollider
*prCreatePbind { |pattern|
  var pbind;
  var stopBeats = pattern[\stop];

  pattern.removeAt(\repeat);
  pattern.removeAt(\stop);

  pbind = Pbind(*pattern.asPairs);

  if (pattern[\midiControl] != 1)
  { pbind = this.prCreateFade(pbind, pattern[\fade]) };

  pbind = this.prCreateChop(pattern, pbind);

  if (stopBeats.notNil)
  { pbind = Pfindur(stopBeats, pbind) };

  ^pbind;
}
```

Update the call site in `Px.new` accordingly.

### 2. Update `prCreatePlayList` — `Px.sc`

When an Ndef already exists, update its source directly:

```supercollider
*prCreatePlayList { |id, pbind|
  if (ndefList[id].isNil)
  { ndefList.put(id, Ndef(id, pbind).quant_(quant)) }
  { Ndef(id, pbind) };
}
```

### 3. Replace `Pdef` in stop operations — `PxMethods.sc`

- `stop` (no args): Remove the `Pdef.all do:` block (ndefList stop handles it)
- `stop(id)`: Replace `Pdef(id).source = nil` with `Ndef(id).stop`
- `release`: Remove the `Pdef.all do:` block in the fork

### 4. Replace `Pdef` in trace — `PxMethods.sc`

Trace wraps the Pbind (a Patterns feature), so use `Ndef(id).source`:

```supercollider
*trace { |id, key|
  if (id.isNil)
  { last.keys.do { |k| this.new(last[k]) } }
  {
    this.new(last[id]);

    if (key.notNil)
    { Ndef(id).source = Ndef(id).source.collect { |ev| ev[key].postln; ev } }
    { Ndef(id).source = Ndef(id).source.trace };
  };
}
```

### 5. Replace `Pdef` in PxMidi — `PxMidi.sc`

- `control` class method: Replace `Pdef(key).stop` with `Ndef(key).stop`
- `control` Number method: Replace `Pdef(controlId).stop` with `Ndef(controlId).stop`
- Remove stale `Ndef(\px)[0] = { Mix.new(...) }` lines (already invalid with bus architecture)

### 6. Replace `Pdef` in Dx — `Dx.sc`

- `solo`: Replace `Pdef(pattern[\id]).source = nil` with `Ndef(pattern[\id]).stop`
- `prStopRemovedPatterns`: Same replacement
- `prStopPreset`: Same replacement

## Files to modify

1. `Classes/Px.sc` — rename method, update call site, update prCreatePlayList
2. `Classes/PxMethods.sc` — stop, release, trace
3. `Classes/PxMidi.sc` — control methods
4. `Classes/Dx.sc` — solo, prStopRemovedPatterns, prStopPreset

## Verification

1. Recompile class library
2. Play a pattern, re-evaluate it (source update works)
3. Stop individual pattern: `Px.stop(\ki)`
4. Stop all: `Px.stop`
5. Trace: `Px.trace(\ki)` and `Px.trace(\ki, \amp)`
6. Drum presets: `Dx.preset(\core)`
7. Dx solo/stop
8. CmdPeriod and replay
