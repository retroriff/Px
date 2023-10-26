# Px: A patterns builder class for SuperCollider

## Pattern controls

| Key      | Value                                          | Description                              |
| -------- | ---------------------------------------------- | ---------------------------------------- |
| `amp`    | number \| number[] \| Pattern                  | Amplification. An array generates a Pseq |
| `euclid` | [hits: number, duration: number, time: number] | Generates an Euclidian ryhthm            |

## Event methods

| Name     | Arguments                               | Description                                                           |
| -------- | --------------------------------------- | --------------------------------------------------------------------- |
| `amp`    | number \| number[] \| Pattern           | Amplification                                                         |
| `beat`   | seed?: integer                          | Generates a random rhythm                                             |
| `delay`  | mix?: 1.0, args?: pairs[]               | Adds a delay effect                                                   |
| `dur`    | number \| number[] \| Pattern           | Duration                                                              |
| `in`     | Seconds?: integer                       | (\fade: "in")                                                         |
| `out`    | Seconds?: integer                       | (\fade: "out")                                                        |
| `pan`    | number \| "rand" \| "rotate" \| Pattern | Pan                                                                   |
| `reverb` | mix?: 1.0, args?: pairs[]               | Adds a reverb effect                                                  |
| `rotate` | None                                    | Creates a back-and-forth pan rotation between left and right channels |
| `seed`   | seed: integer                           | Generate a specific seed                                              |
| `solo`   | None                                    | (\solo: true)                                                         |
| `wah`    | mix?: 1.0, args?: pairs[]               | Adds a wah effect                                                     |

## Loops

- `loop`: [folder: string, file: number | \rand]
- `play`: [folder: string, file: number | \rand]

## Class methods

- `chorus`: Plays a chorus
- `release`: nil | integer
- `save`: Saves a chorus
- `trace`: print out the results of the streams
