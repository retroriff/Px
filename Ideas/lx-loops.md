# Lx (Loops) — New Class for Multi-Track Sample Looping

## Context

The user has a standalone Looper project (`.scd` scripts) that loads audio samples from subfolders and plays them with per-channel controls (volume, rate, start, length). The goal is to bring this into the Px ecosystem as a proper class called **Lx**, using the Px pattern system internally (like Dx does for drums). Each subfolder becomes a loop channel, and the number of channels is dynamic based on what's found.

## Architecture

**Lx extends Px** (same pattern as `Dx : Px`). Internally creates one Px `\loop` pattern per channel/subfolder. Patterns get IDs like `\lx0`, `\lx1`, `\lx2`, etc. This means loops integrate with Fx effects, quantization, and the Ndef mixer automatically.

## Files to Create

| File | Purpose |
|------|---------|
| `Classes/Lx.sc` | Main class (`Lx : Px`) |
| `Classes/LxGui.sc` | GUI extension (`+ Lx { *gui ... }`) |
| `HelpSource/Classes/Lx.schelp` | Help documentation |

## Files to Modify

| File | Change |
|------|--------|
| `README.md` | Add Lx section to class methods table |
| `AGENTS.md` | Add Lx to class extensions reference |
| `Docs/init-context.md` | Add Lx to class hierarchy |

## Existing Code to Reuse

- **`\loop` SynthDef** (`SynthDefs/loop.scd`) — tempo-synced PlayBuf with rate/start/beats/dur. No changes needed.
- **`Px.new(pattern)`** — pattern creation pipeline (inherited via `super.new()`)
- **`Px.stop(id)`** — stop individual patterns
- **`Px.loadSynthDefs`** — already loads `\loop` SynthDef automatically
- **`DxGui.sc` `prCreateKnob`** — GUI knob pattern to follow (but Lx gets its own copy since it's a separate class)

## Sample Loading

`Lx.loadSamples(path)` scans the given path for subfolders. Each subfolder = one channel. All `.wav`/`.aiff` files in each subfolder become the sample pool for that channel.

```
path/
  Channel-A/    → channel 0, samples [0..n]
  Channel-B/    → channel 1, samples [0..n]
  Channel-C/    → channel 2, samples [0..n]
```

Buffers stored in `Lx.bufs` (Dictionary keyed by channel index, each value is an Array of Buffers). Channel count determined dynamically by number of subfolders.

## Class Design — `Lx : Px`

### Classvars

```supercollider
classvar <bufs;           // Dictionary: channel index → Array of Buffers
classvar <channelCount;   // Number of channels (subfolders found)
classvar <channelNames;   // Array of subfolder names
classvar <folderPath;     // Loaded samples path
classvar <tracks;         // Array: current sample index per channel
classvar <window;         // GUI window reference
```

### Public Methods

| Method | Signature | Description |
|--------|-----------|-------------|
| `*initClass` | | Initialize classvars, CmdPeriod cleanup |
| `*loadSamples` | `\|path\|` | Scan subfolders, load buffers, set channelCount |
| `*play` | `\|channel, fadeTime\|` | Play one channel or all. Creates Px loop patterns. |
| `*stop` | `\|channel\|` | Stop one channel or all Lx patterns |
| `*amp` | `\|channel, value\|` | Set amplitude for a channel |
| `*buf` | `\|channel, index\|` | Switch sample for a channel |
| `*dur` | `\|channel, value\|` | Set duration (beats) for a channel |
| `*next` | `\|channel\|` | Next sample in channel |
| `*prev` | `\|channel\|` | Previous sample in channel |
| `*rate` | `\|channel, value\|` | Set playback rate |
| `*start` | `\|channel, value\|` | Set start position (0-1) |
| `*vol` | `\|value\|` | Set global Lx volume |
| `*gui` | | Open the Lx GUI |

### Private Methods

| Method | Signature | Description |
|--------|-----------|-------------|
| `*prCreateId` | `\|channel\|` | Returns `("lx" ++ channel).asSymbol` |
| `*prCreatePattern` | `\|channel\|` | Builds pattern dict for a channel and calls `super.new()` |
| `*prStopAll` | | Stop all Lx patterns |

### How `*play` Works

For each channel, creates a pattern like:
```supercollider
super.new((
  id: \lx0,
  loop: bufs[0][tracks[0]],
  amp: 0.3,
  dur: 4,
  lx: true,
));
```

This goes through the Px pipeline: `prCreateBufInstruments` converts `loop:` to `instrument: \loop`, then `prCreateLoops` processes it, and it plays via Pbindef/Ndef.

### How Parameter Changes Work

When `Lx.rate(0, 0.5)` is called:
1. Update the stored pattern in `Px.last[\lx0]`
2. Call `super.new(updatedPattern)` to recreate the pattern with new values
3. Pbindef updates seamlessly on next quantized beat

This is the same approach Dx uses — modify the pattern dict and call `super.new()`.

## GUI Design (`LxGui.sc`)

Follows DxGui.sc patterns: dark background (`Color.new255(26, 29, 34)`), cyan accent knobs, FlowLayout.

Layout: one column per channel, each containing:
- **Label**: subfolder name (StaticText)
- **Sample**: prev/next buttons + current index display
- **Knobs**: Amp, Rate, Start, Dur (using `prCreateKnob` pattern from DxGui)
- **Mute button** per channel

Bottom row:
- **Play All** / **Stop All** buttons

Window width scales with channel count. Each column ~100px wide.

## Implementation Order

1. `Classes/Lx.sc` — Core class with `initClass`, `loadSamples`, `play`, `stop`, parameter methods
2. `Classes/LxGui.sc` — GUI extension
3. `HelpSource/Classes/Lx.schelp` — Help file
4. Update `README.md`, `AGENTS.md`, `Docs/init-context.md`

## Verification

1. Recompile class library (Cmd+K)
2. Boot server, load SynthDefs:
   ```supercollider
   Px.loadSynthDefs;
   ```
3. Load samples:
   ```supercollider
   Lx.loadSamples("~/path/to/loop/folders/");
   ```
4. Test play/stop:
   ```supercollider
   Lx.play;        // all channels
   Lx.play(0);     // single channel
   Lx.stop;
   Lx.stop(0);
   ```
5. Test parameter control:
   ```supercollider
   Lx.rate(0, 0.5);
   Lx.amp(0, 0.8);
   Lx.next(0);
   Lx.start(0, 0.25);
   ```
6. Test GUI:
   ```supercollider
   Lx.gui;
   ```
7. Verify Fx integration works:
   ```supercollider
   Fx(\lx0).reverb(0.3);
   ```
