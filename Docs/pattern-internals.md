# Pattern Internals

Deep technical reference for Px pattern system architecture. This document explains **concepts and design decisions** rather than code details - read the actual source files for implementation specifics.

## Pattern Data Structure

### Core Concept

Patterns are SuperCollider Event dictionaries stored in `Px.last[id]`. Each pattern is a key-value dictionary containing:
- **Identity** - id, drumMachine, instrument
- **Audio parameters** - amp, dur, pan, octave, degree, scale
- **Rhythm** - beat, beatSet, fill, weight, euclid, rest, repeat, human
- **Effects** - fx, fade, chop, reverb
- **MIDI** - chan, midiout, midicmd, midinote
- **Samples** - loop, play, file
- **Internal state** - beats, totalBeats, solo

### Key Pattern Dictionary Keys

**Identity:**
- `pattern[\id]` - Primary identifier (symbol)
- `pattern[\drumMachine]` - Drum machine number (505, 606, 808, 909)
- `pattern[\drumMachineIntegerId]` - Sequential numeric ID for fills (manual drum patterns only)
- `pattern[\instrument]` - Instrument name
- `pattern[\dx]` - Boolean flag for preset patterns

**Rhythm (beats and fills):**
- `pattern[\beat]` - Trigger beat generation
- `pattern[\beatSet]` - Custom 16-step array
- `pattern[\beats]` - Generated beat array (stored after creation)
- `pattern[\totalBeats]` - Combined beat used by fill patterns
- `pattern[\fill]` - Invert previous pattern's beat
- `pattern[\repeat]` - Number of times the beat pattern repeats (removed before Pdef creation)
- `pattern[\weight]` - Probability 0-1 for beat randomness

**Where to find:** Check source files for complete list - pattern keys are used throughout `Classes/Px.sc`, `Classes/Number.sc`, `Classes/PxBeats.sc`

## Pattern ID Systems

**Why this matters:** Understanding the three ID types is critical for debugging and implementing features that reference previous patterns (like fills).

### Three ID Types

**1. Regular Patterns (Px)** - `1 i: \sine`
- ID: Numeric symbol (`:1`, `:2`, `:3`)
- Created by: `Number.createId()` returns the number as symbol
- Simple, straightforward

**2. Manual Drum Patterns** - `808 i: \kick`
- **Primary ID:** String concatenation `kick808`, `snare808` (human-readable)
- **Secondary ID:** Sequential integer `80801`, `80802` (for fill logic)
- **Why dual IDs?** String ID is semantic, integer ID enables arithmetic for finding previous patterns
- Format: `drumMachine * 100 + sequentialNumber`

**3. Preset Drum Patterns** - `Dx.preset(\electro, 1)`
- ID: Numeric only (`80000`, `80001`, `80002`)
- No secondary ID needed - already numeric so fills work directly
- Format: `(drumMachine - drumMachine % 10) * 100 + index`

### Key Concept: Dual ID System

Manual drum patterns need **two IDs** because:
- **String ID** (`kick808`) - Used for pattern lookup, human-readable, debug-friendly
- **Integer ID** (`80801`) - Used by fill logic to calculate previous pattern via `currentId - 1`

Preset patterns don't need dual IDs because their primary ID is already numeric.

**Implementation:** See `Number.createId()`, `Number.prGenerateDrumMachineId()`, `Number.prGenerateDrumMachineIntegerId()` in `Classes/Number.sc`

## Beat Generation & Fill Mechanics

### Beat Generation

**Concept:** Beats are 16-step arrays that control amplitude over time, created with probabilistic randomness.

- **Input:** `pattern[\weight]` (0-1) determines probability each step is active
- **Output:** 16-step array stored in `pattern[\beats]`
- **Deterministic:** Uses seed for reproducible randomness (unless `\rand`)

**Example:** `beat: 0.7` creates pattern where ~70% of steps are active, ~30% silent.

**Implementation:** See `PxBeats.prCreateBeat()` in `Classes/PxBeats.sc`

### Fill Mechanics

**Concept:** Fills create **complementary rhythms** by inverting the previous pattern's beat.

**How it works:**
1. Find previous pattern (using `drumMachineIntegerId - 1` for drum patterns, or `id - 1` for regular)
2. Get previous pattern's beats or totalBeats
3. Invert: where previous was active → silent, where silent → active (with probability)
4. Combine inverted beat with previous totalBeats
5. Store as `pattern[\totalBeats]` for next fill to use

**Chain effect:** Each fill builds on previous fills via `totalBeats`, creating evolving patterns.

**Edge cases:**
- First fill (no previous): Returns unmodified amp
- Deleted previous pattern: Returns unmodified amp
- Multiple drum machines: Independent sequences per drum machine

**Implementation:** See `PxBeats.prCreateFillFromBeat()` in `Classes/PxBeats.sc`

## Drum Machine Architecture

### Two Ways to Create Drum Patterns

**Manual:** `808 i: \kick`
- String ID + integer ID
- Created via `Number` class methods
- Flexible, any instrument

**Preset:** `Dx.preset(\electro, 1)`
- Numeric ID only
- Loaded from YAML files
- Predefined rhythms per preset

### Key Differences

| Aspect | Manual (`808 i: \kick`) | Preset (`Dx.preset(\electro)`) |
|--------|------------------------|-------------------------------|
| ID type | String + Integer | Numeric only |
| Creation | Number → Px → Dx | Dx.preset → Dx.new |
| `pattern[\dx]` | nil | `true` |
| Flexibility | Any instrument/params | Predefined patterns |

### Sample Organization

Drum samples organized hierarchically:
- Folder structure: `drumMachine/drumMachine-instrument/` (e.g., `808/808-bd/`)
- `instrumentFolders` dictionary maps drum machines to available instruments
- `prHasInstrument()` validates instrument exists before creating pattern

**Implementation:** See `Dx` class in `Classes/Dx.sc`

## Pattern Lifecycle

### Creation Flow

**All patterns follow:** User input → Number methods → Pattern dictionary → Px/Dx processing → Ndef creation

**Key steps:**
1. User writes chainable syntax (`1 i: \sine amp: 0.5`)
2. Number methods accumulate parameters in dictionary
3. Instrument method triggers pattern creation
4. `prPlayClass()` determines Px vs Dx
5. Pattern processed through creation methods
6. Stored in `Px.last[id]` and wrapped in Ndef

### Pattern Updates (ID Reuse)

When same instrument/number called again, **IDs are reused** rather than creating new ones.

Example: `808 i: \kick amp: 0.5` then `808 i: \kick amp: 0.8` → Same pattern ID, dictionary updated in place.

### Pattern Storage

**Three class variables store pattern state:**
- `Px.last` - Pattern dictionaries
- `Px.lastFormatted` - Processed patterns for playback
- `ndefList` - NodeProxy instances

### Pattern Deletion

Stop methods remove patterns from all three storage locations. Integer IDs **do not get reused** - sequence continues with gaps (this is intentional).

Example: Create 80801, 80802, delete 80801, next is 80803 (not 80801).

## Pattern Relationships

### Fill Dependencies

Fills create **sequential dependencies** between patterns:
- Pattern B (fill) depends on Pattern A's beats
- Pattern C (fill) depends on Pattern B's totalBeats
- Chain builds: each fill references the previous pattern

**How lookup works:** Fill finds previous via arithmetic on integer IDs (`currentId - 1`)

### Shared State

**Same drum machine:** Patterns share `drumMachine` number and sequential integer ID range

**Same instrument:** Patterns with same drum machine + instrument share the same ID (updates not duplicates)

## Important Concepts

### Common Pitfalls

1. **ID type confusion:** Don't assume all IDs are numeric - manual drum patterns use strings
2. **Direct modification:** Don't modify `Px.last` directly - use pattern creation methods
3. **Fill edge cases:** First fill or missing previous returns unmodified amp (not an error)
4. **ID gaps:** Normal after deletion, IDs intentionally don't reuse
5. **Preset vs manual:** Check `pattern[\dx]` flag to differentiate

### Quick Checks

**Is this a drum pattern?** `pattern[\drumMachine].notNil`

**Is this a preset?** `pattern[\dx] == true`

**Does it have beats?** `pattern[\beats].notNil or: { pattern[\totalBeats].notNil }`
