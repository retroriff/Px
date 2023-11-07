# Px: A patterns builder class for SuperCollider

## Px

### Class args

| Arg        | Value            | Description                                          |
| ---------- | ---------------- | ---------------------------------------------------- |
| `patterns` | Event[]          | An array containing all the patterns in Event format |
| `name`     | string \| symbol | A user defined name for the generated Pdef           |
| `trace`    | boolean          | Print out the results of the streams                 |

### Pattern controls

| Key      | Value                                          | Description                              |
| -------- | ---------------------------------------------- | ---------------------------------------- |
| `amp`    | number \| number[] \| Pattern                  | Amplification. An array generates a Pseq |
| `euclid` | [hits: number, duration: number, time: number] | Generates an Euclidian ryhthm            |

### Event methods

| Name     | Arguments                               | Description                                                                                                                                                                                  |
| -------- | --------------------------------------- | -------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| `amp`    | number \| number[] \| Pattern           | Amplification                                                                                                                                                                                |
| `beat`   | seed?: integer                          | Generates a random rhythm                                                                                                                                                                    |
| `delay`  | mix?: 1.0, args?: pairs[]               | Adds a delay effect                                                                                                                                                                          |
| `dur`    | number \| number[] \| Pattern           | Duration                                                                                                                                                                                     |
| `in`     | Seconds?: integer                       | (\fade: "in")                                                                                                                                                                                |
| `out`    | Seconds?: integer                       | (\fade: "out")                                                                                                                                                                               |
| `pan`    | number \| "rand" \| "rotate" \| Pattern | Pan                                                                                                                                                                                          |
| `reverb` | mix?: 1.0, args?: pairs[]               | Adds a reverb effect                                                                                                                                                                         |
| `rotate` | None                                    | Creates a back-and-forth pan rotation between left and right channels                                                                                                                        |
| `seed`   | seed: integer                           | Generate a specific seed                                                                                                                                                                     |
| `solo`   | None                                    | (\solo: true)                                                                                                                                                                                |
| `trim`   | startPosition: 1.0                      | Plays a trimmed loop from a fixed position or random when startPosition is nil                                                                                                               |
| `weight` | number                                  | Generates a list of probabilities or weights. Value range from 0 to 1. Tenths change the probability of hits and rests while hundredths defines the probabilty of switching between 2 tenths |
| `wah`    | mix?: 1.0, args?: pairs[]               | Adds a wah effect                                                                                                                                                                            |

### Class methods

- `chorus`: Plays a chorus
- `gui`: A gui showing all Pdefs
- `release`: nil | integer
- `save`: Saves a chorus
- `shuffle`: Generates new random seeds
- `stop`: Stops the Pdef
- `trace`: print out the results of the streams

### Buf loopers

- `loop`: [folder: string, file: number | \jump | \rand]
- `play`: [folder: string, file: number | \rand]

## Pmidi
