# Px: A DSL for Live Coding in SuperCollider

An embedded domain-specific language (DSL) built on SuperCollider for live coding music. Px extends the Number and Symbol classes to provide a fluent, declarative syntax for creating patterns, drum machines, and effects on NodeProxy — replacing manual Pbind/Ndef construction with concise one-liners. Below is a basic example:

```js
// Play
1 i: \bd dur: 1

// Stop
\1 i:\bd dur: 1
```

Additional code examples can be found [here](/Examples/).

**📖 Table of Contents**

1. ⚡️ [Px: A Pattern Shortcuts Generator](#️-px-a-pattern-shortcuts-generator)
2. ✨ [Fx: A Nodeproxy Effects Handler](#-fx-a-nodeproxy-effects-handler)
3. 🛢️ [Dx: Drum Machines](#️-drum-machines)
4. 🔁 [Lx: Multi-Track Sample Looper](#-lx-multi-track-sample-looper)
5. 🌊 [Sx: A Sequenced Synth](#-sx-a-sequenced-synth)
6. 🎹 [Nx: Musical Chord Data](#-nx-musical-chord-data)
7. 💥 [Notes Handler with MIDI Support](#-notes-handler-with-midi-support)
8. 📡 [OSC Communication](#-osc-communication)
9. 🎚️ [Mixer](#️-mixer)
10. 🎛️ [TR08: A Roland TR-08 MIDI Controller](#️-tr08-a-roland-tr-08-midi-controller)

## 🛠️ Installation

Install `Quarks.install("https://github.com/retroriff/sc-px");` and recompile.

**Optional:**

- Load audio samples: `Px.loadSamples(<YOUR_SAMPLES_FOLDER_PATH>);`
- Install [Tidal Drum Machines](https://github.com/geikha/tidal-drum-machines) to use drum machines and `Dx`
- Install [VSTPlugin](https://github.com/Spacechild1/vstplugin) and set a presets path: `Fx.setVstPresetsPath("../VST/presets/".resolveRelative);`

## ⚡️ Px: A Pattern Shortcuts Generator

The superclass that generates the patterns from an array of events with a simplified syntax for a fast edition.

### Integer methods to play a pattern

| Name     | Arguments                                         | Description                                                                                                                                                                                  |
| -------- | ------------------------------------------------- | -------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| `amp`    | number \| number[] \| Pattern                     | Amplification. An array generates a Pseq                                                                                                                                                     |
| `beat`   | weight: range 0..1                                | Generates a random rhythm, or own rhythym defined by set                                                                                                                                     |
| `dur`    | number \| number[] \| Pattern                     | Duration. An array generates a Pseq                                                                                                                                                          |
| `euclid` | [hits: number, total: number]                     | Generates an Euclidian rhythm                                                                                                                                                                |
| `fill`   | weight: range 0..1                                | Fills the rests gap of its previous sequential pattern                                                                                                                                       |
| `gui`    | nil (open or update) \| 0 (close)                 | Open or refresh a patterns gui window. A 0 value closes the window. pattern                                                                                                                  |
| `human`  | delay: range 0..1                                 | Humanize the playback of an instrument                                                                                                                                                       |
| `in`     | seconds: integer                                  | Fades in the pattern.                                                                                                                                                                        |
| `length` | number                                            | Number of random degrees generated with `degree: \rand` (default: 1)                                                                                                                         |
| `off`    | beats: integer                                    | Offset value                                                                                                                                                                                 |
| `out`    | seconds: integer                                  | Fades out the pattern.                                                                                                                                                                       |
| `pan`    | range -1..1 \| \rand \| \rotate \| Pattern        | A pan controller                                                                                                                                                                             |
| `r`      | number \| \rand \| [\wrand, item1, item2, weight] | Rate value. The term rate was discarded because it was an existing Integer method                                                                                                            |
| `rest`   | beats: integer                                    | Rests muted for a specific amount of beats                                                                                                                                                   |
| `seed`   | seed: integer \| symbol \| \rand                  | Generate a specific random seed or a `Pxrand` using `\rand`                                                                                                                                  |
| `set`    | 1 (enable)                                        | Updates an existing pattern. Not needed for regular patterns (parameters can be set directly). Required for drum machines where the value must be the instrument.                            |
| `solo`   | 1 (enable)                                        | Mutes all patterns that don't contain a solo method                                                                                                                                          |
| `stop`   | beats: integer                                    | Stops and removes the pattern after the specified number of beats                                                                                                                            |
| `unsolo` | None                                              | Restore all patterns that have been muted by solo method                                                                                                                                     |
| `trim`   | startPosition?: range 0..1 \| number[]            | Plays a trimmed loop from a fixed position, a sequence from an array, or random when startPosition is nil                                                                                    |
| `weight` | range 0..1                                        | Generates a list of probabilities or weights. Value range from 0 to 1. Tenths change the probability of hits and rests while hundredths defines the probabilty of switching between 2 tenths |

### FX integer pattern methods

These methods add effects directly to a pattern's proxy via the Fx class. They accept the same arguments as the corresponding `Fx` methods. Effects are automatically disabled when removed from a full pattern declaration.

| Name         | Arguments                                      | Description                  |
| ------------ | ---------------------------------------------- | ---------------------------- |
| `blp`        | mix?: range 0..1                               | Band-limited low-pass filter |
| `compressor` | mix?: range 0..1, thresh?, ratio?, gain?       | Dynamic range compressor     |
| `crush`      | mix?: range 0..1, bits?: number, rate?: number | Bit crusher                  |
| `delay`      | mix?: range 0..1, delaytime?, delayfeedback?   | Delay effect                 |
| `distort`    | mix?: range 0..1, drive?: number               | Distortion                   |
| `duck`       | mix?: range 0..1, thresh?: number, src?: Symbol | Sidechain compression        |
| `flanger`    | mix?: range 0..1                               | Flanger effect               |
| `freqShift`  | mix?: range 0..1, freq?, phase?                | Frequency shifter            |
| `gverb`      | mix?: range 0..1, roomsize?, revtime?          | Granular reverb              |
| `hpf`        | mix?: range 0..1, freq?: number                | High pass filter             |
| `lpf`        | mix?: range 0..1, freq?: number                | Low pass filter              |
| `pan`        | pos?: range -1..1                              | Stereo panning               |
| `phaser`     | mix?: range 0..1, rate?, depth?                | Phaser effect                |
| `reverb`     | mix?: range 0..1, room?, size?                 | Reverb effect                |
| `space`      | mix?: range 0..1, fb?: number                  | Spatial reverb               |
| `tremolo`    | mix?: range 0..1, rate?: number                | Tremolo effect               |
| `vibrato`    | mix?: range 0..1, rate?, depth?                | Pitch modulation vibrato     |
| `vst`        | mix?: range 0..1, plugin?: string              | VST plugin effect            |
| `wah`        | mix?: range 0..1, rate?, depth?                | Auto-wah effect              |

### Instrument methods

| Name   | Arguments                                          | Description                            |
| ------ | -------------------------------------------------- | -------------------------------------- |
| `i`    | name: string                                       | Plays a Synthdef. Same as `instrument` |
| `loop` | [folder: string, file: number \| \jump \| \rand]\* | Plays a loop from a buffer             |
| `play` | [folder: string, file: number \| array \| \rand]\* | Plays a buffer                         |

`*` The array can be replaced by a string shortcut: `"folder:index"`.
`**` It also accepts a Buffer object: `Buffer.read(s, "chord.aiff".resolveRelative)`.

### Px class methods

- `control` (chan, ctlNum, value): Sends a MIDI CC message immediately.
- `chorus`: Plays a saved chorus.
- `chop`: (dur: Integer | Nil, drop: Integer)Slices and repeats part of the beat in short bursts. It can be disabled with a `0` or `Nil` value.
- `gui`: Opens or refresh a gui window with pattern sliders.
- `mixer`: Opens an `NdefMixer` instance, always on top, and assigns it to the `~mixer` variable.
- `pause` (id: symbol): Pauses a specific pattern.
- `release` (time: nil | number): Sets the release time. Accepts either nil or an integer value. To clear all instances use `\all`.
- `resume` (id: symbol): Resumes a paused pattern.
- `save`: Saves a chorus.
- `set`: Sets a parameter for all active patterns.
- `shuffle` (id: symbol): Generates a new random seed for a pattern, or all patterns when id is not provided.
- `stop`: Stops all patterns. It can stop specific patterns if a single id or an array of ids is provided.
- `synthDef`: Browses global synthDefs. If a synthDef name is provided, returns its arguments.
- `tempo` (bpm: nil | number): Sets the tempo if bpm is given; returns current tempo if nil.
- `trace`: Prints out the results of the streams for debugging purposes.
- `traceOff`: Disables trace.
- `vol`: Controls the volume of the nodeproxy.

### Pattern shortcuts

Array extension methods that create pattern objects:

| Method                         | Description                                                                          |
| ------------------------------ | ------------------------------------------------------------------------------------ |
| `[0, 1].pseg(\exp, 16)`        | Creates a `Pseg` curve. Args: curve (`\lin`/`\exp`), beats, repeats (`inf`/`\inf`)   |
| `[1, 2, 3].pseq`               | Creates a `Pseq` loop from the array                                                 |
| `[0, 1, 2].prand`              | Creates a `Prand` (random selection with repeats). Optional: repeats (default `inf`) |
| `[0, 1, 2].pxrand`             | Creates a `Pxrand` (random, no immediate repeats). Optional: repeats (default `inf`) |
| `[0, 1].pwhite`                | Creates a `Pwhite` (random range). Array must have exactly 2 elements `[lo, hi]`     |
| `[1, 2, 4].pwrand([10, 2, 3])` | Creates a `Pwrand` (weighted random). Weights array is required and auto-normalized  |
| `[0, 1, 2, 3].shuffle(71)`     | Scrambles the array with a random seed for reproducibility                           |

## ✨ Fx: A Nodeproxy Effects Handler

The Fx class facilitates the addition of effects to the Px set classes, as well as to any other Ndef. Effect parameters support Function-based modulation — pass a `{ }` block containing UGens to create dynamic, moving effects (e.g., `Fx(\px).lpf(1, { SinOsc.kr(0.5).range(200, 4000) })`).

To enable loading or saving of VST presets, initialize the class with the path to the presets folder:

```js
Fx.setVstPresetsPath(<path>);
```

### Fx class methods

It offers the same [class methods as Px](#px-class-methods), with the following additions:

- `activeEffects`: Checks the active proxy filters
- `clear`: Clears all effects
- `duck` (mix, thresh, bus): Sidechain compression — ducks from the master output or a specific Ndef
- `effectNames`: Returns a sorted array of all available effect names
- `loadEffects`: Allows to reload the effect files.
- `vstReadProgram` (preset: string): Loads a VST preset from the default presets folder
- `vstWriteProgram` (preset: string): Write a VST preset to the default presets folder

To open the VST plugin editor, use `Fx.vstController.editor`

Additionally, we can set parameter automations with `Fx.vstController.set(1, 1)`

## 💥 Notes Handler with MIDI Support

Custom pattern player designed to handle degrees, and can send MIDI messages based on incoming pattern data. It also helps to manage MIDI-related functionalities within SuperCollider, providing a way to control MIDI events and output.

### Event methods

| Name     | Arguments                                                           | Description                            |
| -------- | ------------------------------------------------------------------- | -------------------------------------- |
| `arp`    | None                                                                | Creates a very basic arpegio           |
| `degree` | `degree`: number \| array \| \rand, `scale`?: scale, `size`: number | Handle notes                           |
| `octave` | number \| array \| [\beats, octave: number]                         | Can create a sequence or a random beat |
| `root`   | number \| array                                                     | Sets the root value                    |

### MIDI

When the pattern contains `\chan`, it sends MIDI with MIDIOut class and the `\midi` event type. MIDI channels use 1-16 numbering to match DAWs and hardware. All the necessary default commands are added automatically, like `\midicmd`, `\allNotesOff`, `\control`, or `\noteOn`.

#### MIDI methods

- `Pmidi.init`: Initializes the MIDIClient. Latency can be passed as argument.
- `Px.panic`: Silences all active notes across channels, or channel-specific (`Px.panic(1)`).

#### MIDI event methods

| Name      | Arguments                 | Description                                                                                   |
| --------- | ------------------------- | --------------------------------------------------------------------------------------------- |
| `control` | number, number \| Pattern | Sends a MIDI CC message. Integer values are sent immediately; patterns create a separate Pdef |
| `hold`    | None                      | The note off message will not be sent and will keep the notes pressed                         |
| `holdOff` | None                      | Releases holded notes                                                                         |
| `panic`   | None                      | "Panic" message, kills all notes on the channel pattern                                       |

## 🛢️ Drum Machines

We can simplify the usage of drum machine using shortcodes. Short aliases (505, 606, 626, 707, 727, 808, 909) resolve to their RolandTR equivalents. Many more machines are available via `Dx.gui`. Here's an example:

```js
707 i: \bd dur: 1;
707 i: "sd:4" dur: 2 off: 1;

// Stop all
\707 i: \all
```

With `Dx` class we can use presets:

```
Dx.preset(\electro, 1);
```

### Dx class methods

| Name          | Arguments                                         | Description                            |
| ------------- | ------------------------------------------------- | -------------------------------------- |
| `delay`       | mix?: range 0..1                                  | Adds delay FX to the preset patterns   |
| `fill`        | instrument?: symbol, repeat?: integer             | One-shot random fill with crash accent |
| `gui`         | None                                              | Opens a drum machine bank GUI          |
| `instruments` | machine?: symbol                                  | Returns available instruments          |
| `loadPresets` | None                                              | Reloads presets from YAML files        |
| `preset`      | name?: string \| index: number \| amp: range 0..1 | Plays a [preset](/Presets/yaml/)       |
| `release`     | None                                              | Releases with fadeTime                 |
| `reverb`      | mix?: range 0..1 \|                               | Adds reverb FX to the preset patterns  |
| `shuffle`     | None                                              | Shuffles the drum machines bank        |
| `stop`        | None                                              | Same as `\808 i: \all`                 |
| `vol`         | amp: range 0..1                                   | Sets an amp for the preset patterns    |

## 🔁 Lx: Multi-Track Sample Looper

Lx extends Px to provide multi-track sample looping. Each subfolder in a given path becomes a loop channel, playing tempo-synced audio through the Px pattern system.

```js
// Load samples — each subfolder becomes a channel
Lx.loadSamples("~/Music/loops/");

// Play all channels
Lx.play;

// Stop all
Lx.stop;
```

### Lx class methods

| Name          | Arguments                          | Description                              |
| ------------- | ---------------------------------- | ---------------------------------------- |
| `amp`         | channel: integer, value?: number   | Sets amplitude for a channel             |
| `buf`         | channel: integer, index: integer   | Switches sample in a channel             |
| `dur`         | channel: integer, value?: number   | Sets duration (beats) for a channel      |
| `gui`         | None                               | Opens multi-channel control GUI          |
| `loadSamples` | path: string                       | Loads subfolders as loop channels        |
| `next`        | channel: integer                   | Next sample in channel (wraps)           |
| `play`        | channel?: integer, fadeTime?: number | Plays one or all channels               |
| `prev`        | channel: integer                   | Previous sample in channel (wraps)       |
| `rate`        | channel: integer, value?: number   | Sets playback rate for a channel         |
| `start`       | channel: integer, value?: number   | Sets start position (0-1) for a channel  |
| `stop`        | channel?: integer                  | Stops one or all Lx patterns             |
| `vol`         | value: number                      | Sets amplitude for all playing channels  |

## 🌊 Sx: A Sequenced Synth

A class designed for controlling a synthesizer equipped with a built-in sequencer. Unlike the Play class, Sx is limited to playing only a predefined synthesizer with integrated sequencers. Below is an example demonstrating the arguments it accepts:

```js
(
Sx(
    (
        amp: 1,
        dur: 1/4,
        euclid: [3, 5],
        degree: [0, 1, 2, 3],
        env: 1,
        octave: [0, 0, 0, 1],
        root: 0,
        scale: \dorian,
        vcf: 1,
        wave: \saw,
    )
);
)
```

The synth must be previously loaded with `Sx.loadSynth`.

We can update args independently: `Sx.set(\amp, 0.5, lag: 0)`

| Name      | Arguments          | Description                                |
| --------- | ------------------ | ------------------------------------------ |
| `in`      | fadeTime?: integer | Fades in the synthesizer. Default: 16      |
| `out`     | fadeTime?: integer | Fades out the synthesizer. Default: 16     |
| `release` | fadeTime?: integer | Fades out and frees the synth. Default: 10 |
| `set`     | key: symbol, value | Sets a parameter with optional lag         |
| `stop`    | fadeTime?: integer | Stops the Ndef playback                    |
| `vol`     | amp?: range 0..1   | Gets or sets the synth amplitude           |

**Tip**: The `shuffle` array method provides the capability to specify a random seed for the scramble method.

## 🎹 Nx: Musical Chord Data

A class for managing musical chord data. It dynamically builds chords by combining tonics (root notes) with chord qualities (intervals/scales). Supports octave transposition.

```js
Nx.set(\EmAdd9);
Nx.midinotes;    // -> [52, 55, 59, 66] (octave 3)
Nx.degrees;      // -> [0, 2, 4, 7]
Nx.scale;        // -> \minor

// Work with octaves
Nx.midinotes(4); // -> [64, 67, 71, 78] (octave 4, temporary)
Nx.octave = 5;
Nx.midinotes;    // -> [76, 79, 83, 90] (octave 5, permanent)
```

Chord data is stored in `Score/tonics.scd` (root notes) and `Score/chords.scd` (chord qualities).

### Nx class methods

| Name             | Arguments                   | Returns    | Description                                         |
| ---------------- | --------------------------- | ---------- | --------------------------------------------------- |
| `all`            | None                        | Dictionary | Returns all chord data in a single Dictionary       |
| `chord`          | None                        | Symbol     | Returns current chord name                          |
| `chordQualities` | None                        | Dictionary | Returns all loaded chord qualities                  |
| `degrees`        | None                        | Array      | Returns scale degrees array                         |
| `key`            | None                        | Integer    | Returns MIDI key (base note)                        |
| `loadChords`     | None                        | None       | Reloads chord data from `Score/`                    |
| `midinotes`      | octave?: Integer (-1 to 9)  | Array      | Returns MIDI notes, optionally transposed           |
| `octave`         | None                        | Integer    | Gets/sets current octave (default: 3, range: -1..9) |
| `root`           | None                        | Integer    | Returns root value (pitch class 0-11)               |
| `scale`          | None                        | Symbol     | Returns scale symbol                                |
| `set`            | chord: Symbol, octave?: Int | None       | Sets current chord, optionally changes octave       |
| `shuffle`        | tonic?: Symbol, scale?: Sym | None       | Randomly selects a chord with optional filters      |
| `tonics`         | None                        | Dictionary | Returns all loaded tonics (root notes)              |

## 📡 OSC Communication

Px also has methods to handle a OSC listener, useful for applications where remote control or interaction is needed, allowing real-time data to be sent and received via the network.

- `listen`: Creates a new OSC receiver to listen for OSC messages sent to a specific address and port (127.0.0.1 on port 57120). Once an OSC message is received at the specified address on the /px endpoint, the method extracts the message and evaluates it as code.

- `listenOff`: It frees the OSCdef instance and disconnects all network addresses, ensuring that no further messages are received or processed.

- `oscTest`: Sends a test message.

## 🎚️ Mixer

Straightforward crossfader utility classes that smoothly transitions audio from source A to source B over an optional specified duration (default is 20 seconds):

```js
Crossfader(\a, \b, 16);
FadeIn(\a, 16);
FadeOut(\a, 16);
```

They can be used directly with symbols methods and binary operator syntax:

```js
\a.in
\a in: 16
\a.out
\a out: 16
\a.to(\b)
\a to: \b
\a.play
\a.stop
```

And we can get and set synth controls:

```
\a.edit; // open NdefGui editor
\a.get; // show user-facing controls
\a.get(\amp); // get a specific control value
\a.set(\amp, 1); // non-quantified set
\a.qset(\amp, 1); // quantified set
```

## 🎛️ TR08: A Roland TR-08 MIDI Controller

It can send MIDI messages to a Roland TR08. if the device is not available, plays TR-808 SynthDefs instead:

| Symbol | Instrument          | MIDI Control |
| ------ | ------------------- | ------------ |
| `\bd`  | **B**ass**D**rum    | 36           |
| `\sd`  | **S**nare**D**rum   | 38           |
| `\lc`  | **L**ow**C**onga    | 64           |
| `\lt`  | **L**ow**T**om      | 43           |
| `\mc`  | **M**id**C**onga    | 63           |
| `\mt`  | **M**id**T**om      | 47           |
| `\hc`  | **H**i**C**onga     | 62           |
| `\ht`  | **H**i**T**om       | 50           |
| `\cl`  | **CL**aves          | 75           |
| `\rs`  | **R**im**S**hot     | 37           |
| `\ma`  | **MA**racas         | 70           |
| `\cp`  | Hand**C**la**P**    | 39           |
| `\cb`  | **C**ow**B**ell     | 56           |
| `\cy`  | **C**ymbal          | 49           |
| `\oh`  | Open**H**i**h**at   | 46           |
| `\hh`  | Closed**H**i**h**at | 42           |

### TR08 class methods

| Name          | Arguments                      | Description                          |
| ------------- | ------------------------------ | ------------------------------------ |
| `init`        | time?: number                  | Controls the latency. Default is 0.2 |
| `loadPresets` | None                           | Reloads presets from YAML files      |
| `preset`      | name?: string \| index: number | Plays a [preset](/Presets/yaml/)     |
| `stop`        | None                           | Same as `\808 i: \all`               |
