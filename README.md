# Px: A Tailored Class Set for Enhanced Live Coding in SuperCollider

A set of classes designed to generate patterns on a NodeProxy and streamline the integration of effects. These classes prioritize ease of use, offering a straightforward solution for creating pattern shortcuts and enhancing them with effects. Below is a basic example:

```
Px(i: \bd);
```

Additional code examples can be found [here](/Examples/).

**📖 Table of Contents**

1. ⚡️ [Px: A Pattern Shortcuts Generator](#px-a-pattern-shortcuts-generator)
2. ✨ [Nfx: A Nodeproxy Effects Handler](#nfx-a-nodeproxy-effects-handler)
3. 💥 [Play: A Notes Handler with MIDI Support](#play-a-notes-handler-with-midi-support)
4. 🎛️ [TR08: A Roland TR-08 MIDI Controller](#tr08-a-roland-tr-08-midi-controller)
5. 🔥 [Ns: A Sequenced Synth](#ns-a-sequenced-synth)
6. 📡 [OSC Communication](#osc-communication)
7. ✅ [Unit Tests](#unit-tests)

**🛠️ Dependencies**:

- [MiSCellaneous](https://github.com/dkmayer/miSCellaneous_lib) (PbindFx)
- [VSTPlugin](https://github.com/Spacechild1/vstplugin)

## ⚡️ Px: A Pattern Shortcuts Generator

The superclass that generates the patterns from an array of events with a simplified syntax for a fast edition.

### Px class arguments

| Arg        | Value            | Description                                          |
| ---------- | ---------------- | ---------------------------------------------------- |
| `patterns` | Event[]          | An array containing all the patterns in Event format |
| `name`     | string \| symbol | A user defined name for the generated Pdef           |
| `trace`    | boolean          | Print out the results of the streams                 |

### Px event methods

| Name     | Arguments                                         | Description                                                                                                                                                                                  |
| -------- | ------------------------------------------------- | -------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| `amp`    | number \| number[] \| Pattern                     | Amplification. An array generates a Pseq                                                                                                                                                     |
| `beat`   | seed?: integer, rest?: number, set?: number[]     | Generates a random rhythm, or own rhythym defined by set                                                                                                                                     |
| `dur`    | number \| number[] \| Pattern                     | Duration. An array generates a Pseq                                                                                                                                                          |
| `euclid` | hits: number, total: number                       | Generates an Euclidian rhythm                                                                                                                                                                |
| `fill`   | none                                              | Fills the rests gap of its previous pattern. Due to its dependency with the previous item, using solo can generate an error. We can mute patterns using `a: 0` instead                       |
| `human`  | delay: range 0..1                                 | Humanize the playback of an instrument                                                                                                                                                       |
| `in`     | seconds?: integer                                 | (\fade: "in")                                                                                                                                                                                |
| `out`    | seconds?: integer                                 | (\fade: "out")                                                                                                                                                                               |
| `pan`    | range -1..1 \| \rand \| \rotate \| Pattern        | Pan                                                                                                                                                                                          |
| `rate`   | number \| \rand \| [\wrand, item1, item2, weight] | Rate value                                                                                                                                                                                   |
| `rotate` | none                                              | Creates a back-and-forth pan rotation between left and right channels                                                                                                                        |
| `seed`   | seed: integer                                     | Generate a specific seed                                                                                                                                                                     |
| `solo`   | none                                              | (\solo: true)                                                                                                                                                                                |
| `trim`   | startPosition?: range 0..1 \| number[]            | Plays a trimmed loop from a fixed position, a sequence from an array, or random when startPosition is nil                                                                                    |
| `weight` | range 0..1                                        | Generates a list of probabilities or weights. Value range from 0 to 1. Tenths change the probability of hits and rests while hundredths defines the probabilty of switching between 2 tenths |

### FX event methods

| Name     | Arguments                                                                   | Description              |
| -------- | --------------------------------------------------------------------------- | ------------------------ |
| `delay`  | mix?: range 0..1 \| \rand \| [\wrand, item1, item2, weight], args?: pairs[] | Adds a delay effect      |
| `hpf`    | mix?: range 0..1 \| \rand \| [\wrand, item1, item2, weight], args?: pairs[] | Adds a high pass filter  |
| `lpf`    | mix?: range 0..1 \| \rand \| [\wrand, item1, item2, weight], args?: pairs[] | Adds a lower pass filter |
| `reverb` | mix?: range 0..1 \| \rand \| [\wrand, item1, item2, weight], args?: pairs[] | Adds a reverb effect     |
| `wah`    | mix?: range 0..1 \| \rand \| [\wrand, item1, item2, weight], args?: pairs[] | Adds a wah effect        |

### Event buf loopers

| Name   | Arguments                                        | Description                |
| ------ | ------------------------------------------------ | -------------------------- |
| `loop` | [folder: string, file: number \| \jump \| \rand] | Plays a loop from a buffer |
| `play` | [folder: string, file: number \| \rand]          | Plays a buffer             |

### Px class methods

- `release` (time: nil | number, name: string | \all) Sets the release time. Accepts either nil or an integer value. To clear all instances use `\all`.
- `save`: Saves a chorus
- `shuffle`: Generates new random seeds
- `stop`: Stops the Pdef
- `synthDef`: Browses global synthDefs. If a synthDef name is provided, returns its arguments
- `tempo`: Sets a new tempo
- `trace`: Prints out the results of the streams for debugging purposes.
- `vol`: Controls the volume of the nodeproxy

### FX class methods

| Name     | Arguments                                                   | Description                           |
| -------- | ----------------------------------------------------------- | ------------------------------------- |
| `blp`    | mix?: number \| Nil                                         | Adds a BLP filter to the proxy        |
| `delay`  | mix?: number \| Nil                                         | Adds a delay filter to the proxy      |
| `gverb`  | mix?: number \| Nil, delaytime?: number, decaytime?: number | Adds a gverb filter to the proxy      |
| `hpf`    | mix?: number \| Nil, wave?: boolean                         | Adds a HPF filter to the proxy        |
| `reverb` | mix?: number \| Nil, room?: number, damp?: number           | Adds a reverb filter to the proxy     |
| `vst`    | mix?: number \| Nil, plugin?: string                        | Adds a VST plugin filter to the proxy |

## ✨ Nfx: A Nodeproxy Effects Handler

The Nfx class facilitates the addition of effects to the Px set classes, as well as to any other Ndef.

To enable loading or saving of VST presets, initialize the class with the path to the presets folder:

```js
Nfx.setPresetsPath(<path>);
```

### Nfx class methods

It offers the same [class methods as Px](#px-class-methods), with the following additions:

- `activeEffects`: Checks the active proxy filters
- `clear`: Clears all effects
- `loadEffects`: Allows to reload the effect files.
- `vstReadProgram` (preset: string): Loads a VST preset from the default presets folder
- `vstWriteProgram` (preset: string): Write a VST preset to the default presets folder

To open the VST plugin editor, use `Nfx.vstController.editor`

Additionally, we can set parameter automations with `Nfx.vstController.set(1, 1)`

## 💥 Play: A Notes Handler with MIDI Support

Custom pattern player designed to handle degrees, and can send MIDI messages based on incoming pattern data. It also helps to manage MIDI-related functionalities within SuperCollider, providing a way to control MIDI events and output.

### Event methods

| Name     | Arguments                                                           | Description                  |
| -------- | ------------------------------------------------------------------- | ---------------------------- |
| `arp`    | None                                                                | Creates a very basic arpegio |
| `degree` | `degree`: number \| array \| \rand, `scale`?: scale, `size`: number | Handle notes                 |

### MIDI

When the pattern contains `\chan`, it sends MIDI with MIDIOut class and the `\midi` event type. All the necessary default commands are added automatically, like `\midicmd`, `\allNotesOff`, `\control`, or `\noteOn`.

#### MIDI methods

- `Pmidi.init`: Initializes the MIDIClient. Latency can be passed as argument.

#### MIDI event methods

| Name      | Arguments                                                                                                                   | Description                                                           |
| --------- | --------------------------------------------------------------------------------------------------------------------------- | --------------------------------------------------------------------- |
| `control` | number, number \| Pattern \| \rand \| \wrand \| [symbol: \rand \| \wrand, value1: number, value2?: number, weight?: number] | Sends a controller message                                            |
| `hold`    | None                                                                                                                        | The note off message will not be sent and will keep the notes pressed |
| `holdOff` | None                                                                                                                        | "Panic" message, kills all notes on the channel pattern               |

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

## 🔥 Ns: A Sequenced Synth

A class designed for controlling a synthesizer equipped with a built-in sequencer. Unlike the Play class, Ns is limited to playing only a predefined synthesizer with integrated sequencers. Below is an example demonstrating the arguments it accepts:

```js
(
Ns(
    (
        amp: 1,
        chord: [0, 2, 4],
        dur: 1/4,
        euclid: [3, 5],
        degree: [0, 1, 2, 3],
        octave: [0, 0, 0, 1],
        env: 1,
        scale: \dorian,
        vcf: 1,
        wave: \saw,
    )
);
)
```

The synth must be previously loaded with `Ns.loadSynth`;

**Tip**: The shuffle array method provides the capability to specify a random seed for the scramble method.

## OSC Communication

Px also has methods to handle a OSC listener, useful for applications where remote control or interaction is needed, allowing real-time data to be sent and received via the network.

- `listen`: Creates a new OSC receiver to listen for OSC messages sent to a specific address and port (127.0.0.1 on port 57120). Once an OSC message is received at the specified address on the /px endpoint, the method extracts the message and evaluates it as code.

- `listenOff`: It frees the OSCdef instance and disconnects all network addresses, ensuring that no further messages are received or processed.

## ✅ Unit Tests

```js
// Runs all tests
PxTestAll.run

// Individual tests:
PxArrayTest.run
PxEventTest.run
PxTest.run
NfxTest.run
NsTest.run

// Disables passing tests verbosity
UnitTest.reportPasses = false
```
