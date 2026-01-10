# A Tailored Class Set for Enhanced Live Coding in SuperCollider

A set of classes designed to generate patterns on a NodeProxy and streamline the integration of effects. These classes prioritize ease of use, offering a straightforward solution for creating pattern shortcuts and enhancing them with effects. Below is a basic example:

```js
// Play
1 i: \bd dur: 1

// Stop
\1 i:\bd dur: 1
```

Additional code examples can be found [here](/Examples/).

**📖 Table of Contents**

1. ⚡️ [Px: A Pattern Shortcuts Generator](#%EF%B8%8F-px-a-pattern-shortcuts-generator)
2. ✨ [Fx: A Nodeproxy Effects Handler](#-fx-a-nodeproxy-effects-handler)
3. 🛢️ [Dx: Drum Machines](#%EF%B8%8F-drum-machines)
4. 🌊 [Sx: A Sequenced Synth](#-sx-a-sequenced-synth)
5. 🎹 [Nx: Musical Chord Data](#-nx-musical-chord-data)
6. 💥 [Notes Handler with MIDI Support](#-notes-handler-with-midi-support)
7. 📡 [OSC Communication](#-osc-communication)
8. 🎚️ [Crossfader](#%EF%B8%8F-crossfader)
9. 🎛️ [TR08: A Roland TR-08 MIDI Controller](#%EF%B8%8F-tr08-a-roland-tr-08-midi-controller)

## 🛠️ Installation

1. Install `Quarks.install("https://github.com/retroriff/sc-px");` and recompile.
2. Install the [MiSCellaneous](https://github.com/dkmayer/miSCellaneous_lib) extension (needed for `PbindFx`)
3. Install [Tidal Drum Machines](https://github.com/geikha/tidal-drum-machines)
4. Load SynthDefs and audio files by running:

```
Px.loadSynthDefs;
Px.loadSamples(<YOUR_SAMPLES_FOLDER_PATH>);
```

**VST Plugins:**

1. Install the [VSTPlugin](https://github.com/Spacechild1/vstplugin) extension
2. Set a presets folder by running:

```
Fx.setVstPresetsPath("../VST/presets/".resolveRelative);
```

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
| `off`    | beats: integer                                    | Offset value                                                                                                                                                                                 |
| `out`    | seconds: integer                                  | Fades out the pattern.                                                                                                                                                                       |
| `pan`    | range -1..1 \| \rand \| \rotate \| Pattern        | A pan controller                                                                                                                                                                             |
| `r`      | number \| \rand \| [\wrand, item1, item2, weight] | Rate value. The term rate was discarded because it was an existing Integer method                                                                                                            |
| `rest`   | beats: integer                                    | Rests muted for a specific amount of beats                                                                                                                                                   |
| `seed`   | seed: integer \| symbol \| \rand                  | Generate a specific random seed or a `Pxrand` using `\rand`                                                                                                                                  |
| `set`    | 1 (enable)                                        | Allows to add more pairs to an existing pattern when `i`, `loop` and `play` are not present. When we use drum machines, the value must be the instrument.                                    |
| `solo`   | 1 (enable)                                        | Mutes all patterns that don't contain a solo method                                                                                                                                          |
| `unsolo` | None                                              | Restore all patterns that have been muted by solo method                                                                                                                                     |
| `trim`   | startPosition?: range 0..1 \| number[]            | Plays a trimmed loop from a fixed position, a sequence from an array, or random when startPosition is nil                                                                                    |
| `weight` | range 0..1                                        | Generates a list of probabilities or weights. Value range from 0 to 1. Tenths change the probability of hits and rests while hundredths defines the probabilty of switching between 2 tenths |

### FX integer pattern methods

| Name     | Arguments                                                                   | Description              |
| -------- | --------------------------------------------------------------------------- | ------------------------ |
| `delay`  | mix?: range 0..1 \| \rand \| [\wrand, item1, item2, weight], args?: pairs[] | Adds a delay effect      |
| `hpf`    | mix?: range 0..1 \| \rand \| [\wrand, item1, item2, weight], args?: pairs[] | Adds a high pass filter  |
| `lpf`    | mix?: range 0..1 \| \rand \| [\wrand, item1, item2, weight], args?: pairs[] | Adds a lower pass filter |
| `reverb` | mix?: range 0..1 \| \rand \| [\wrand, item1, item2, weight], args?: pairs[] | Adds a reverb effect     |
| `wah`    | mix?: range 0..1 \| \rand \| [\wrand, item1, item2, weight], args?: pairs[] | Adds a wah effect        |

### Instrument methods

| Name   | Arguments                                          | Description                            |
| ------ | -------------------------------------------------- | -------------------------------------- |
| `i`    | name: string                                       | Plays a Synthdef. Same as `instrument` |
| `loop` | [folder: string, file: number \| \jump \| \rand]\* | Plays a loop from a buffer             |
| `play` | [folder: string, file: number \| array \| \rand]\* | Plays a buffer                         |

`*` The array can be replaced by a string shortcut: `"folder:index"`.
`**` It also accepts a Buffer object: `Buffer.read(s, "chord.aiff".resolveRelative)`.

### Px class methods

- `chorus`: Plays a saved chorus.
- `chop`: (dur: Integer | Nil, drop: Integer)Slices and repeats part of the beat in short bursts. It can be disabled with a `0` or `Nil` value.
- `gui`: Opens or refresh a gui window with pattern sliders.
- `mixer`: Opens an `NdefMixer` instance, always on top, and assigns it to the `~mixer` variable.
- `pause` (id: symbol): Pauses a specific pattern.
- `release` (time: nil | number): Sets the release time. Accepts either nil or an integer value. To clear all instances use `\all`.
- `resume` (id: symbol): Resumes a paused pattern.
- `root`: Sets a global root note to all patterns.
- `save`: Saves a chorus.
- `seed`: Sets a global seed for all patterns.
- `shuffle` (id: symbol): Generates a new random seed for a pattern, or all patterns when id is not provided.
- `stop`: Stops all patterns. It can stop specific patterns if a single id or an array of ids is provided.
- `synthDef`: Browses global synthDefs. If a synthDef name is provided, returns its arguments.
- `tempo` (bpm: nil | number): Sets the tempo if bpm is given; returns current tempo if nil.
- `trace`: Prints out the results of the streams for debugging purposes.
- `traceOff`: Disables trace.
- `vol`: Controls the volume of the nodeproxy.

### FX class methods

Px has the same FX methods than Fx, but it is helpful as a shortcut.

| Name      | Arguments                                                   | Description                                |
| --------- | ----------------------------------------------------------- | ------------------------------------------ |
| `blp`     | mix?: number \| Nil                                         | Adds a BLP filter to the proxy             |
| `delay`   | mix?: number \| Nil                                         | Adds a delay filter to the proxy           |
| `flanger` | mix?: number \| Nil                                         | Adds a flanger filter to the proxy         |
| `gverb`   | mix?: number \| Nil, delaytime?: number, decaytime?: number | Adds a gverb filter to the proxy           |
| `hpf`     | mix?: number \| Nil, wave?: boolean                         | Adds a HPF filter to the proxy             |
| `pan`     | number \| \wave                                             | Sets the balance                           |
| `reverb`  | mix?: number \| Nil, room?: number, damp?: number           | Adds a reverb filter to the proxy          |
| `space`   | mix?: number \| Nil, fb?: 0 to 0.99 \| inf                  | Adds a spatial allpass reverb to the proxy |
| `tremolo` | mix?: number \| Nil, rate?: number                          | Adds a tremolo effect to the proxy         |
| `vst`     | mix?: number \| Nil, plugin?: string                        | Adds a VST plugin filter to the proxy      |

### Pattern shortcuts

The following array shortcuts will be automacally converted to patterns:

| Shortcut             | Pattern conversion                    | Works with         | How                                                                            |
| -------------------- | ------------------------------------- | ------------------ | ------------------------------------------------------------------------------ |
| `[\lin, 0, 1, 8, 1]` | `Pseg([0.01, 1, 1], [5, 1], \linear)` | amp, ctf, env, res | Linear (`\lin`) or exponential (`\exp`), start, end, repeats? (omitted is inf) |

## ✨ Fx: A Nodeproxy Effects Handler

The Fx class facilitates the addition of effects to the Px set classes, as well as to any other Ndef.

To enable loading or saving of VST presets, initialize the class with the path to the presets folder:

```js
Fx.setVstPresetsPath(<path>);
```

### Fx class methods

It offers the same [class methods as Px](#px-class-methods), with the following additions:

- `activeEffects`: Checks the active proxy filters
- `clear`: Clears all effects
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

When the pattern contains `\chan`, it sends MIDI with MIDIOut class and the `\midi` event type. All the necessary default commands are added automatically, like `\midicmd`, `\allNotesOff`, `\control`, or `\noteOn`.

#### MIDI methods

- `Pmidi.init`: Initializes the MIDIClient. Latency can be passed as argument.
- `Px.panic`: Silences all active notes across channels, or channel-specific (`Px.panic(0)`).

#### MIDI event methods

| Name      | Arguments                                                                                                                   | Description                                                           |
| --------- | --------------------------------------------------------------------------------------------------------------------------- | --------------------------------------------------------------------- |
| `control` | number, number \| Pattern \| \rand \| \wrand \| [symbol: \rand \| \wrand, value1: number, value2?: number, weight?: number] | Sends a controller message                                            |
| `hold`    | None                                                                                                                        | The note off message will not be sent and will keep the notes pressed |
| `holdOff` | None                                                                                                                        | Releases holded notes                                                 |
| `panic`   | None                                                                                                                        | "Panic" message, kills all notes on the channel pattern               |

## 🛢️ Drum Machines

We can simplify the usage of drum machine using shortcodes. The available drum machines are 505, 606, 707, 808 and 909. Here's an example:

```js
707 i: \bd dur: 1;
707 i: \sd dur: 2 off: 1;

// Stop all
\707 i: \all
```

With `Dx` class we can use presets:

```
Dx.preset(\electro, 1);
```

### Dx class methods

| Name          | Arguments                                         | Description                           |
| ------------- | ------------------------------------------------- | ------------------------------------- |
| `delay`       | mix?: range 0..1 \|                               | Adds delay FX to the preset patterns  |
| `gui`         | None                                              | Opens a drum machine bank GUI         |
| `loadPresets` | None                                              | Reloads presets from YAML files       |
| `preset`      | name?: string \| index: number \| amp: range 0..1 | Plays a [preset](/Presets/yaml/)      |
| `release`     | None                                              | Releases with fadeTime                |
| `reverb`      | mix?: range 0..1 \|                               | Adds reverb FX to the preset patterns |
| `shuffle`     | None                                              | Shuffles the drum machines bank       |
| `stop`        | None                                              | Same as `\808 i: \all`                |
| `vol`         | amp: range 0..1                                   | Sets an amp for the preset patterns   |

## 🌊 Sx: A Sequenced Synth

A class designed for controlling a synthesizer equipped with a built-in sequencer. Unlike the Play class, Sx is limited to playing only a predefined synthesizer with integrated sequencers. Below is an example demonstrating the arguments it accepts:

```js
(
Sx(
    (
        amp: 1,
        chord: [0, 2, 4],
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

**Tip**: The `shuffle` array method provides the capability to specify a random seed for the scramble method.

## 🎹 Nx: Musical Chord Data

A class for managing musical chord data. It loads chord definitions from external files in `Score/` and provides accessor methods for chord properties.

```js
Nx.set(\emAdd9);
Nx.midinotes;   // -> [52, 55, 59, 66]
Nx.degrees;     // -> [0, 2, 4, 7]
Nx.scale;       // -> \minor
```

Chord data files are organized by root note: `Score/e.scd`, `Score/a.scd`, `Score/c.scd`.

### Nx class methods

| Name           | Returns              | Description                                      |
| -------------- | -------------------- | ------------------------------------------------ |
| `chord`        | Array                | Returns intervals array                          |
| `chords`       | Dictionary           | Returns all loaded chords                        |
| `degrees`      | Array                | Returns scale degrees array                      |
| `key`          | Integer              | Returns MIDI key (base note)                     |
| `loadChords`   | None                 | Reloads chord files from `Score/`                |
| `midinotes`    | Array                | Returns computed MIDI notes (key + intervals)    |
| `name`         | Symbol               | Returns current chord name                       |
| `root`         | Integer              | Returns root value (pitch class 0-11)            |
| `scale`        | Symbol               | Returns scale symbol                             |
| `set`          | None                 | Sets the current chord by name                   |

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

And we can set synth controls:

```
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
