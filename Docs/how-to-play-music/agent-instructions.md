# Agent Instructions for Music Creation

## Purpose

When users request music creation (e.g., "play a 707 kick", "add reverb", "create a beat"), agents should **execute the code immediately** using the OSC sender script.

## How to Respond to Music Requests

###  DO: Execute Code Directly

When a user asks to play music, **immediately execute** the corresponding SuperCollider code using:

```bash
Club/px.sh 'SUPERCOLLIDER_CODE_HERE'
```

**Example:**

User: "Play a 707 kick drum"

Agent response:

```bash
Club/px.sh '707 i: \bd dur: 1'
```

### L DON'T: Provide Extra Information

- L Don't explain how to stop the music
- L Don't provide alternative syntax examples
- L Don't show multiple ways to achieve the same thing
- L Don't explain what the code does unless asked

**Just execute the script.**

## Music Request Examples

| User Request                    | Agent Action                         |
| ------------------------------- | ------------------------------------ |
| "Play a 707 kick on every beat" | `Club/px.sh '707 i: \bd dur: 1'`        |
| "Add a snare on 2 and 4"        | `Club/px.sh '707 i: \sd dur: 2 off: 1'` |
| "Add some reverb"               | `Club/px.sh 'Px.reverb(0.3)'`           |
| "Stop everything"               | `Club/px.sh 'Px.release(16)'`           |
| "Play the electro preset"       | `Club/px.sh 'Dx.preset(\electro, 2)'`   |

## Script Location

The script is located at:

```
/Users/xavier.catchot@m10s.io/Library/Application Support/SuperCollider/Extensions/Px/Club/px.sh
```

Always use the relative path `Club/px.sh` when executing from the project root directory.

## Prerequisites

Assume SuperCollider is already running with `Px.listen;` active. Don't remind users about setup unless there's an error.

## Multiple Patterns

If a user requests multiple patterns, execute multiple commands:

User: "Create a full beat with kick, snare, and hi-hats"

Agent:

```bash
Club/px.sh '707 i: \bd dur: 1'
Club/px.sh '707 i: \sd dur: 2 off: 1'
Club/px.sh '707 i: \oh dur: 0.25 beat: 0.7 amp: 0.4'
```

## Response Style

Keep responses minimal:

-  " Playing 707 kick"
-  " Added reverb"
- L "I've created a kick drum pattern. You can stop it with Px.stop or..."

**Execute first, speak minimally.**
