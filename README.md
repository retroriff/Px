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
5. 💥 [Notes Handler with MIDI Support](#-notes-handler-with-midi-support)
6. 📡 [OSC Communication](#-osc-communication)
7. 🎚️ [Crossfader](#%EF%B8%8F-crossfader)
8. 🎛️ [TR08: A Roland TR-08 MIDI Controller](#%EF%B8%8F-tr08-a-roland-tr-08-midi-controller)
9. ✅ [Unit Tests](#-unit-tests)

**🛠️ Dependencies**:

- [MiSCellaneous](https://github.com/dkmayer/miSCellaneous_lib) (PbindFx)
- [VSTPlugin](https://github.com/Spacechild1/vstplugin)

## ⚡️ Px: A Pattern Shortcuts Generator

The superclass that generates the patterns from an array of events with a simplified syntax for a fast edition.

### Integer methods to play a pattern

| Name     | Arguments                                         | Description                                                                                                                                                                                  |
| -------- | ------------------------------------------------- | -------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| `amp`    | number \| number[] \| Pattern                     | Amplification. An array generates a Pseq                                                                                                                                                     |
| `args`   | Event                                             | Additional args                                                                                                                                                                              |
| `beat`   | 1 (enable)                                        | Generates a random rhythm, or own rhythym defined by set                                                                                                                                     |
| `dur`    | number \| number[] \| Pattern                     | Duration. An array generates a Pseq                                                                                                                                                          |
| `euclid` | [hits: number, total: number]                     | Generates an Euclidian rhythm                                                                                                                                                                |
| `fill`   | 1 (enable)                                        | Fills the rests gap of its previous sequential pattern                                                                                                                                       |
| `human`  | delay: range 0..1                                 | Humanize the playback of an instrument                                                                                                                                                       |
| `in`     | seconds: integer                                  | Fades in the pattern. Same as `fade: \in` "in")                                                                                                                                              |
| `off`    | beats: integer                                    | Offset value                                                                                                                                                                                 |
| `out`    | seconds: integer                                  | Fades out the pattern. Same as `fade: \out`                                                                                                                                                  |
| `pan`    | range -1..1 \| \rand \| \rotate \| Pattern        | A pan controller                                                                                                                                                                             |
| `r`      | number \| \rand \| [\wrand, item1, item2, weight] | Rate value. The term rate was discarded because it was an existing Integer method                                                                                                            |
| `rest`   | beats: integer                                    | Rests muted for a specific amount of beats                                                                                                                                                   |
| `rotate` | 1 (enable)                                        | Creates a back-and-forth pan rotation between left and right channels                                                                                                                        |
| `seed`   | seed: integer                                     | Generate a specific random seed                                                                                                                                                              |
| `set`    | 1 (enable)                                        | Allows to add more pairs to an existing pattern when `i`, `loop` and `play` are not present. present                                                                                         |
| `solo`   | 1 (enable)                                        | Mutes all patterns that don't contain a solo method                                                                                                                                          |
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

| Name   | Arguments                                        | Description                     |
| ------ | ------------------------------------------------ | ------------------------------- |
| `i`    | name: string                                     | Plays a Synthdef. Same as `ins` |
| `loop` | [folder: string, file: number \| \jump \| \rand] | Plays a loop from a buffer      |
| `play` | [folder: string, file: number \| array \| \rand] | Plays a buffer                  |

### Px class methods

- `chorus`: Plays a saved chorus.
- `play`: It is only needed when it has been stopped.
- `release` (time: nil | number): Sets the release time. Accepts either nil or an integer value. To clear all instances use `\all`.
- `root`: Sets a global root note to all patterns.
- `save`: Saves a chorus.
- `seed`: Sets a global seed for all patterns.
- `shuffle`: Generates new random seeds.
- `stop`: Stops the Pdef.
- `synthDef`: Browses global synthDefs. If a synthDef name is provided, returns its arguments.
- `tempo` (bpm: nil | number): Sets the tempo if bpm is given; returns current tempo if nil.
- `trace`: Prints out the results of the streams for debugging purposes.
- `traceOff`: Disables trace.
- `vol`: Controls the volume of the nodeproxy.

### FX class methods

Px has the same FX methods than Fx, but it is helpful as a shortcut.

| Name     | Arguments                                                   | Description                           |
| -------- | ----------------------------------------------------------- | ------------------------------------- |
| `blp`    | mix?: number \| Nil                                         | Adds a BLP filter to the proxy        |
| `delay`  | mix?: number \| Nil                                         | Adds a delay filter to the proxy      |
| `gverb`  | mix?: number \| Nil, delaytime?: number, decaytime?: number | Adds a gverb filter to the proxy      |
| `hpf`    | mix?: number \| Nil, wave?: boolean                         | Adds a HPF filter to the proxy        |
| `pan`    | number \| \wave                                             | Sets the balance                      |
| `reverb` | mix?: number \| Nil, room?: number, damp?: number           | Adds a reverb filter to the proxy     |
| `vst`    | mix?: number \| Nil, plugin?: string                        | Adds a VST plugin filter to the proxy |

### Pattern shortcuts

The following array shortcuts will be automacally converted to patterns:

| Shortcut             | Pattern conversion                    | Works with         | How                                                                            |
| -------------------- | ------------------------------------- | ------------------ | ------------------------------------------------------------------------------ |
| `[\lin, 0, 1, 8, 1]` | `Pseg([0.01, 1, 1], [5, 1], \linear)` | amp, ctf, env, res | Linear (`\lin`) or exponential (`\exp`), start, end, repeats? (omitted is inf) |

## ✨ Fx: A Nodeproxy Effects Handler

The Fx class facilitates the addition of effects to the Px set classes, as well as to any other Ndef.

To enable loading or saving of VST presets, initialize the class with the path to the presets folder:

```js
Fx.setPresetsPath(<path>);
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

#### MIDI event methods

| Name      | Arguments                                                                                                                   | Description                                                           |
| --------- | --------------------------------------------------------------------------------------------------------------------------- | --------------------------------------------------------------------- |
| `control` | number, number \| Pattern \| \rand \| \wrand \| [symbol: \rand \| \wrand, value1: number, value2?: number, weight?: number] | Sends a controller message                                            |
| `hold`    | None                                                                                                                        | The note off message will not be sent and will keep the notes pressed |
| `holdOff` | None                                                                                                                        | Releases holded notes                                                 |
| `panic`   | None                                                                                                                        | "Panic" message, kills all notes on the channel pattern               |

## 🛢️ Drum Machines

We can simplify the usage of drum machine using shortcodes. The available drum machines are 606, 707, 808 and 909. Here's an example:

```js
707 i: \bd dur: 1;
707 i: \sn dur: 2 off: 1;

// Stop all
\707 i: \all
```

With `Dx` class we can use presets:

```
Dx.preset(\electro, 1);
```

### Dx class methods

| Name          | Arguments                      | Description                      |
| ------------- | ------------------------------ | -------------------------------- |
| `loadPresets` | None                           | Reloads presets from YAML files  |
| `preset`      | name?: string \| index: number | Plays a [preset](/Presets/yaml/) |
| `release`     | None                           | Release with fadeTime            |
| `stop`        | None                           | Same as `\808 i: \all`           |

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

## 📡 OSC Communication

Px also has methods to handle a OSC listener, useful for applications where remote control or interaction is needed, allowing real-time data to be sent and received via the network.

- `listen`: Creates a new OSC receiver to listen for OSC messages sent to a specific address and port (127.0.0.1 on port 57120). Once an OSC message is received at the specified address on the /px endpoint, the method extracts the message and evaluates it as code.

- `listenOff`: It frees the OSCdef instance and disconnects all network addresses, ensuring that no further messages are received or processed.

## 🎚️ Crossfader

Straightforward crossfader utility classes that smoothly transitions audio from source A to source B over an optional specified duration (default is 20 seconds):

```js
Crossfader(\a, \b, 10);
FadeIn(\a, 10);
FadeOut(\a, 10);
```

They can be used directly with symbols methods and binary operator syntax:

```js
\a.in
\a in: 10
\a.out
\a out: 10
\a fadeTo: \b
\a.play
\a.stop
```

## 🎛️ TR08: A Roland TR-08 MIDI Controller

It can send MIDI messages to a Roland TR08. if the device is not available, plays TR-808 SynthDefs instead:

| Symbol | Instrument          | MIDI Control |
| ------ | ------------------- | ------------ |
| `\bd`  | **B**ass**D**rum    | 36           |
| `\sn`  | **S**nare**D**rum   | 38           |
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
| `\oh`  | **O**pen**H**ihat   | 46           |
| `\ch`  | **C**losed**H**ihat | 42           |

### TR08 class methods

| Name          | Arguments                      | Description                          |
| ------------- | ------------------------------ | ------------------------------------ |
| `init`        | time?: number                  | Controls the latency. Default is 0.2 |
| `loadPresets` | None                           | Reloads presets from YAML files      |
| `preset`      | name?: string \| index: number | Plays a [preset](/Presets/yaml/)     |
| `stop`        | None                           | Same as `\808 i: \all`               |

## ✅ Unit Tests

```js
// Runs all tests
PxTestAll.run

// Individual tests:
PxArrayTest.run
PxEventTest.run
PxTest.run
FxTest.run
SxTest.run

// Disables passing tests verbosity
UnitTest.reportPasses = false
```
