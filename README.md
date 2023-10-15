# Px

## Patterns class builder for SuperCollider

- **fade**: "in" | "out" | ["in" | "out", seconds: number]
- **solo**: any

## Event methods
- in: (\fade: "in")
- out: (\fade: "out")
- solo: (\solo: true) 

## Loops

- **loop**: [folder: string, file: number]
- **play**: [folder: string, file: number]
## Methods

- **delay**: Adds a delay effect.
- **chorus**: Plays a chorus.
- **release**: nil | number
- **reverb**: Adds a reverb effect.
- **save**: Saves a chorus.
- **wah**: Adds a wah effect.