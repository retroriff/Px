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

- **chorus**: Play a chorus.
- **release**: nil | number
- **save**: Saves a chorus.