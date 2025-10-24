# Initial Context

This file provides guidance to LLM agents when working with code in this repository.

**Note**: This document is for **development tasks only**. For music creation tasks, see `AGENTS.md` routing guide.

## Project Overview

**Px** is a SuperCollider extension for enhanced live coding. It provides a simplified, chainable syntax for pattern generation, effects processing, and real-time performance. This is a **Quark** (SuperCollider package) installed in the SuperCollider Extensions directory.

### Key Technologies

- **SuperCollider**: Audio synthesis platform
- **File types**: `.sc` (class definitions), `.scd` (code/effect definitions)
- **Dependencies**: Bjorklund, miSCellaneous_lib, VSTPlugin

## Architecture

### Core Class Hierarchy

The extension uses **method chaining on Integer and Symbol classes** to enable concise live coding syntax:

```supercollider
1 i: \bd dur: 1 reverb: 0.2  // Integer methods chain to create a pattern
\1 i: \bd                     // Symbol methods to stop the pattern
```

**Main Classes:**

1. **Px** (Classes/Px.sc) - Base pattern generator

   - Manages pattern state via class variables (`last`, `lastFormatted`, `ndefList`)
   - Creates Pbindefn patterns wrapped in NodeProxy (`Ndef(\px)`)
   - Each pattern has a unique ID and generates a separate Ndef

2. **Number** (Classes/Number.sc) - Integer method extensions

   - Uses `doesNotUnderstand` to dynamically route methods to pattern creation
   - All chainable methods (i, dur, amp, beat, etc.) update `Px.last` dictionary
   - Actual SuperCollider pattern creation happens when instrument methods are called

3. **Fx** (Classes/Fx.sc) - Effects handler

   - Loads effect definitions from `Effects/*.scd` files at class init
   - Effects are applied as NodeProxy slots: `Ndef(\px)[slot] = effect`
   - Maintains `activeEffects` dictionary to track applied effects

4. **Dx** (Classes/Dx.sc) - Drum machine presets (extends Px)

   - Loads YAML presets from `Presets/yaml/`
   - Shorthand syntax: `808 i: \bd dur: 1` routes through drum machine
   - Maps instrument symbols to PlayBuf-based sample playback

5. **Sx** (Classes/Sx.sc) - Sequenced synthesizer
   - Single instance polyphonic synth with built-in sequencer
   - Different from Px: plays one synth vs. multiple pattern instances

### Key Architectural Patterns

**Method Extensions Pattern:**

- Core functionality added via class extensions (`+ Number`, `+ Symbol`, `+ Array`)
- Enables DSL-like syntax: `1 i: \bd dur: 1`

**Dictionary-Based State:**

- Pattern state stored in class variables (Dictionary instances)
- `Px.last` = raw user input, `Px.lastFormatted` = processed patterns
- `ndefList` tracks active NodeProxy instances per pattern ID

**Effect Loading:**

- Effects defined in `.scd` files as Dictionary entries
- Loaded dynamically via `Fx.loadEffects` at init
- Structure: `(effectName: { |in, mix, ...args| synthDef })`

**Pattern Generation Flow:**

1. Integer methods accumulate parameters in `Px.last[id]`
2. Instrument method (`i:`, `play:`, `loop:`) triggers `Px.new()`
3. `Px.new()` processes the event through creation methods:
   - `prCreateBufInstruments` → `prCreateLoops` → `prCreateAmp` → `prCreateDur` → `prCreatePan` → `prCreateDegrees` → `prCreateOctaves` → `prCreateMidi` → `prCreateFx`
4. Result wrapped in Pbindef, then in Ndef

## Development Workflow

### Setup for Testing Changes

1. **Edit class files** in `Classes/*.sc`
2. **Recompile SuperCollider**: Cmd+K (macOS) or Language → Recompile Class Library
3. **Test changes** using Examples in the `Examples/` directory

### Common Tasks

**Loading SynthDefs and Samples:**

```supercollider
Px.loadSynthDefs;
Px.loadSamples(<path_to_samples>);
```

**Loading Effects:**

```supercollider
Fx.loadEffects;  // Reload effect files from Effects/
```

**Loading Presets:**

```supercollider
Dx.loadPresets;  // Reload YAML presets from Presets/yaml/
```

### File Organization

```
Classes/           - Core class definitions (.sc files)
  Px.sc           - Base pattern generator
  PxMethods.sc    - Px class methods (chop, chorus, stop, etc.)
  PxFx.sc         - Px effect methods
  PxBuf.sc        - Buffer/sample handling
  PxNotes.sc      - Note/degree pattern handling
  PxMidi.sc       - MIDI functionality
  Fx.sc           - Effects handler
  Dx.sc           - Drum machines
  Number.sc       - Integer method extensions
  Symbol.sc       - Symbol method extensions

Effects/          - Effect definitions (.scd files)
SynthDefs/        - Synth definitions (.scd files)
Presets/yaml/     - Drum machine presets (YAML)
Examples/         - Usage examples (.scd files)
```

### Adding New Features

**Adding a new effect:**

1. Create `Effects/NewEffect.scd` with Dictionary entry
2. Add method to `Fx` class in `Classes/Fx.sc`
3. Optionally add shorthand to `PxFx.sc` for pattern-level effects
4. Run `Fx.loadEffects` to reload

**Adding a new pattern method:**

1. Add method to `Number` class in `Classes/Number.sc`
2. Handle the parameter in `Px` class creation methods (e.g., `prCreateAmp`)
3. Test manually using Examples

**Adding a drum preset:**

1. Create YAML file in `Presets/yaml/`
2. Run `Dx.loadPresets` to reload
3. Test with `Dx.preset(\name)`

## Important Notes

- **NodeProxy architecture**: All audio routing through `Ndef(\px)` mixer
- **Quantization**: Patterns quantized to 4 beats by default (`quant_(4)`)
- **Pattern IDs**: Integer keys (1, 2, 3) create pattern IDs, symbols stop them
- **State management**: Heavy use of class variables - be careful with reinitialization
- **MIDI**: Requires `Pmidi.init` before MIDI operations
- **Effect slots**: NodeProxy slots start at index 1 (index 0 is the audio source)

## VST Support

Set VST presets path:

```supercollider
Fx.setVstPresetsPath("../VST/presets/".resolveRelative);
```

Open VST editor:

```supercollider
Fx.vstController.editor
```
