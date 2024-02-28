# Px: A patterns player class for SuperCollider

## Px

### Class args

| Arg        | Value            | Description                                          |
| ---------- | ---------------- | ---------------------------------------------------- |
| `name`     | string \| symbol | A user defined name for the generated Pdef           |
| `patterns` | Event[]          | An array containing all the patterns in Event format |
| `trace`    | boolean          | Print out the results of the streams                 |

### Pattern controls

| Key      | Value                         | Description                              |
| -------- | ----------------------------- | ---------------------------------------- |
| `amp`    | number \| number[] \| Pattern | Amplification. An array generates a Pseq |
| `dur`    | number \| number[] \| Pattern | Duration. An array generates a Pseq      |
| `euclid` | [hits: number, total: number] | Generates an Euclidian ryhthm            |

### Event methods

| Name     | Arguments                                         | Description                                                                                                                                                                                  |
| -------- | ------------------------------------------------- | -------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| `amp`    | number \| number[] \| Pattern                     | Amplification                                                                                                                                                                                |
| `beat`   | seed?: integer, rest?: number, set?: number[]     | Generates a random rhythm, or own rhythym defined by set                                                                                                                                     |
| `dur`    | number \| number[] \| Pattern                     | Duration                                                                                                                                                                                     |
| `euclid` | hits: number, total: number                       | Generates an Euclidian rhythm                                                                                                                                                                |
| `fill`   | none                                              | Fills the rests gap of its previous pattern. Due to its dependency with the previous item, using solo can generate an error. We can mute patterns using `a: 0` instead                       |
| `human`  | delay: number                                     | Humanize the playback of an instrument                                                                                                                                                       |
| `in`     | seconds?: integer                                 | (\fade: "in")                                                                                                                                                                                |
| `out`    | seconds?: integer                                 | (\fade: "out")                                                                                                                                                                               |
| `pan`    | number \| \rand \| \rotate \| Pattern             | Pan                                                                                                                                                                                          |
| `rate`   | number \| \rand \| [\wrand, item1, item2, weight] | Rate value                                                                                                                                                                                   |
| `rotate` | none                                              | Creates a back-and-forth pan rotation between left and right channels                                                                                                                        |
| `seed`   | seed: integer                                     | Generate a specific seed                                                                                                                                                                     |
| `solo`   | none                                              | (\solo: true)                                                                                                                                                                                |
| `trim`   | startPosition?: 1.0 \| number[]                   | Plays a trimmed loop from a fixed position, a sequence from an array, or random when startPosition is nil                                                                                    |
| `weight` | number                                            | Generates a list of probabilities or weights. Value range from 0 to 1. Tenths change the probability of hits and rests while hundredths defines the probabilty of switching between 2 tenths |

### Event FX methods

| Name     | Arguments                                                            | Description              |
| -------- | -------------------------------------------------------------------- | ------------------------ |
| `delay`  | mix?: 1.0 \| \rand \| [\wrand, item1, item2, weight], args?: pairs[] | Adds a delay effect      |
| `hpf`    | mix?: 1.0 \| \rand \| [\wrand, item1, item2, weight], args?: pairs[] | Adds a high pass filter  |
| `lpf`    | mix?: 1.0 \| \rand \| [\wrand, item1, item2, weight], args?: pairs[] | Adds a lower pass filter |
| `reverb` | mix?: 1.0 \| \rand \| [\wrand, item1, item2, weight], args?: pairs[] | Adds a reverb effect     |
| `wah`    | mix?: 1.0 \| \rand \| [\wrand, item1, item2, weight], args?: pairs[] | Adds a wah effect        |

### Class methods

- `chorus`: Plays a chorus
- `delay`: Adds delay FX to all patterns
- `gui`: A gui showing all Pdefs
- `release`: nil | integer
- `reverb`: Adds reverb FX to all patterns
- `save`: Saves a chorus
- `shuffle`: Generates new random seeds
- `stop`: Stops the Pdef
- `synthDef`: Browse global synthDefs. If a synthDef name is provided, it returns its arguments
- `tempo`: Set a new tempo
- `trace`: Print out the results of the streams
- `wah`: Adds delay FX to all patterns

### Buf loopers

- `loop`: [folder: string, file: number | \jump | \rand]
- `play`: [folder: string, file: number | \rand]

## Play

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
