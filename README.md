# Px

## Patterns builder class for SuperCollider

- **amp**: number | number[]
- **fade**: "in" | "out" | ["in" | "out", seconds: number]

## Event methods
| Feature   | Arguments      | Description               |
|-----------|----------------|---------------------------|
| **beat**  | seed?: integer | Generates a random rhythm |
| **delay** | mix?: 1.0      | Adds a delay effect.      |
| **in**    | None           | (\fade: "in")             |
| **out**   | None           | (\fade: "out")            |
| **reverb**| mix?: 1.0      | Adds a reverb effect      |
| **solo**  | None           | (\solo: true)             |
| **wah**   | mix?: 1.0      | Adds a wah effect.        |

## Loops
- **loop**: [folder: string, file: number]
- **play**: [folder: string, file: number]

## Methods
- **chorus**: Plays a chorus
- **release**: nil | integer
- **save**: Saves a chorus
- **trace**: print out the results of the streams
