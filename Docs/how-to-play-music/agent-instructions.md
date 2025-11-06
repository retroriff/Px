# Agent Instructions for Music Creation

## Purpose

When users request music creation (e.g., "play a 707 kick", "add reverb", "create a beat"), agents should **execute the code immediately** using the OSC sender script.

## How to Respond to Music Requests

###  DO: Execute Code Directly

When a user asks to play music, **immediately execute** the corresponding SuperCollider code using:

```bash
./play.sh 'SUPERCOLLIDER_CODE_HERE'
```

**Example:**

User: "Play a 707 kick drum"

Agent response:

```bash
./play.sh '707 i: \bd dur: 1'
```

### L DON'T: Provide Extra Information

- L Don't explain how to stop the music
- L Don't provide alternative syntax examples
- L Don't show multiple ways to achieve the same thing
- L Don't explain what the code does unless asked

**Just execute the script.**

## Music Request Examples

| User Request                    | Agent Action                           |
| ------------------------------- | -------------------------------------- |
| "Play a 707 kick on every beat" | `./play.sh '707 i: \bd dur: 1'`        |
| "Add a snare on 2 and 4"        | `./play.sh '707 i: \sd dur: 2 off: 1'` |
| "Add some reverb"               | `./play.sh 'Px.reverb(0.3)'`           |
| "Stop everything"               | `./play.sh 'Px.release(16)'`           |
| "Play the electro preset"       | `./play.sh 'Dx.preset(\electro, 2)'`   |

## Understanding the Syntax

### Pattern Numbering System

**CRITICAL: All non-drum-machine patterns MUST start with a pattern ID number.**

**Drum machine patterns** (505, 606, 707, 808, 909) have their own built-in numbering and do NOT need an ID:

```
707 i: \bd dur: 1
```

**Sample patterns** MUST start with a pattern ID number:

```
1 play: "fm:5" dur: 0.5 beat: 0.8 amp: 0.7
```

**Pattern ID Usage:**

- **New pattern**: Use a new consecutive number (1, 2, 3, etc.)
- **Update existing pattern**: Use the same number with `set`
  ```
  1 set dur: 1
  ```

### Drum Machine Pattern Syntax

The basic pattern format is:

```
<drum_machine> i: <instrument> [parameter: value ...]
```

**Available drum machines:** `505`, `606`, `707`, `808`, `909`

**Common instruments:**

| Symbol | Instrument          |
| ------ | ------------------- |
| `\bd`  | **B**ass**D**rum    |
| `\sd`  | **S**nare**D**rum   |
| `\lc`  | **L**ow**C**onga    |
| `\lt`  | **L**ow**T**om      |
| `\mc`  | **M**id**C**onga    |
| `\mt`  | **M**id**T**om      |
| `\hc`  | **H**i**C**onga     |
| `\ht`  | **H**i**T**om       |
| `\cl`  | **CL**aves          |
| `\rs`  | **R**im**S**hot     |
| `\ma`  | **MA**racas         |
| `\cp`  | Hand**C**la**P**    |
| `\cb`  | **C**ow**B**ell     |
| `\cy`  | **C**ymbal          |
| `\oh`  | Open**H**i**h**at   |
| `\hh`  | Closed**H**i**h**at |

**Common parameters:**

- `dur: 1` - Duration between hits (1 = quarter note)
- `off: 1` - Offset start by N beats
- `amp: 0.5` - Volume (0.0 to 1.0)
- `beat: 0.7` - Probability of hit (0.0 to 1.0)
- `pan: \rand` - Pan position or random
- `reverb: 0.3` - Reverb amount
- `delay: 0.2` - Delay amount

### Stopping Patterns

```
\<drum_machine> i: \all          // Stop all patterns for that drum machine
Px.stop([1, 2, 3])              // Stop specific pattern IDs
Px.release(16)                   // Fade out all patterns over 16 beats
```

### For More Syntax

Reference `README.md` for complete classes and methods.

## Script Location

The script is located at:

```
../Club/play.sh
```

Always use the relative path `./play.sh` when executing from the project root directory.

## Prerequisites

Assume SuperCollider is already running with `Px.listen;` active. Don't remind users about setup unless there's an error.

## Multiple Patterns

If a user requests multiple patterns, execute multiple commands:

User: "Create a full beat with kick, snare, and hi-hats"

Agent:

```bash
./play.sh '707 i: \bd dur: 1'
./play.sh '707 i: \sd dur: 2 off: 1'
./play.sh '707 i: \oh dur: 0.25 beat: 0.7 amp: 0.4'
```

User: "Add a random fm beat and a pop loop"

Agent:

```bash
./play.sh '1 play: "fm:rand" dur: 0.25 beat: 0.6 amp: 0.7'
./play.sh '2 loop: "pop-2:2" dur: 2'
```

## Response Style

Keep responses minimal:

- ✅ "Playing 707 kick"
- ✅ "Added reverb"
- ❌ "I've created a kick drum pattern. You can stop it with Px.stop or..."

**Execute first, speak minimally.**
