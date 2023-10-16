# Px

## Patterns builder class for SuperCollider

- **amp**: number | number[]
- **fade**: "in" | "out" | ["in" | "out", seconds: number]

## Event methods
| Feature   | Arguments      | Description               |
|-----------|----------------|---------------------------|
| **beat**  | seed?: integer | Generates a random rhythm |
| **delay** | None           | Adds a delay effect.      |
| **in**    | None           | (\fade: "in")             |
| **out**   | None           | (\fade: "out")            |
| **reverb**| None           | Adds a reverb effect      |
| **solo**  | None           | (\solo: true)             |
| **wah**   | None           | Adds a wah effect.        |

## Loops
- **loop**: [folder: string, file: number]
- **play**: [folder: string, file: number]

## Methods
- **chorus**: Plays a chorus
- **release**: nil | number
- **save**: Saves a chorus
