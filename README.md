# Px

## Patterns builder class for SuperCollider

- **fade**: "in" | "out" | ["in" | "out", seconds: number]
- **solo**: any

## Event methods
- **delay**: Adds a delay effect.
- **in**: (\fade: "in")
- **out**: (\fade: "out")
- **reverb**: Adds a reverb effect.
- **solo**: (\solo: true) 
- **wah**: Adds a wah effect.

## Loops
- **loop**: [folder: string, file: number]
- **play**: [folder: string, file: number]

## Methods
- **chorus**: Plays a chorus.
- **release**: nil | number
- **save**: Saves a chorus.
