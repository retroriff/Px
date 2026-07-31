# Return informational values from user-facing methods

## Problem

Many Px classes print useful information via `postln` (through `prPrint` or directly) but return `this` (the class) or an unrelated value. When called from SCIDE this is invisible because the post window shows the printout. When called through the OSC shell (`./px.sh '<code>'`), only the return value comes back — the printouts stay in the SC post window and never reach the caller.

Example: before the fix, `./px.sh 'Fx(\dx).space;'` returned `"Fx"` even though the SC post window showed `✨ Enabled \space on dx mix: 0.2 fb: 0.95`.

## Root cause

Two overlapping issues:

1. **Class methods return `this` by default.** In SC, a method with no `^` returns `this`, not the last expression. So public DSL methods that end with a bare `this.prPrint(...)` or `this.prSomething(...)` lose whatever those calls produced.
2. **`prPrint` methods don't `^value`.** In `Px`, `Nx`, `Sx` the `prPrint` implementation is `value.postln;` with no `^`. So even `^this.prPrint("…")` returns `this` (the class), not the message string. Only `Fx.prPrint` was patched to `^value`.

## Fix pattern (already applied to Fx effect methods)

Applied in `Fx.sc` for effect methods (`space`, `reverb`, `delay`, …) and both branches of `prAddEffect`:

- `Fx.prPrint` now returns `^value`.
- Public effect methods prefix `^` on their final `prAddEffect` call.
- Group branch of `prAddEffect` (proxyName `\lx` / `\dx`) returns raw strings (no `postln`) so the interpreter's `->` echo is the sole output — avoids duplicate.
- Non-group branch uses a local `resultMsg`; `prActivateEffect` returns the enable message (via `^`) instead of printing it; `prAddEffect` returns `^resultMsg ?? ("✨ Enabled …")` so re-application still returns a useful message.

## Recommended first step: unify `prPrint`

Add `^value;` to `Px.prPrint`, `Nx.prPrint`, `Sx.prPrint` (mirror `Fx.prPrint`). This alone makes every existing `^this.prPrint(…)` site propagate the message string — no per-call edits. Then drop the `postln` from `prPrint` so `->` isn't duplicated (matching what we did for `Fx` group branch).

Files:
- `Classes/Px.sc:291`
- `Classes/Nx.sc:309`
- `Classes/Sx.sc:273`

## Findings — sites where a print is emitted but not returned

### High priority (user-facing methods)

| File:Line | Method | Currently prints | Currently returns | Should return |
|---|---|---|---|---|
| `Classes/Nx.sc:210` | `*shuffle` | `"Chord is <name>"` | `Nx` (via `^this.set(...)`) | the message or chord name |
| `Classes/Nx.sc:249` | `*fifth` | `"Chord is <name>"` | `Nx` | same |
| `Classes/Dx.sc:157` | `*shuffle` | `"🎲 Drum machine: <name>"` | `Dx` (implicit) | the message |
| `Classes/Dx.sc:350` | `*preset` (too high) | `"🧩 This set has N presets"` | `Dx` | the message |
| `Classes/Dx.sc:370` | `*preset` (loaded) | `"🎧 Preset: <name>"` | `Dx` | preset name / message |
| `Classes/Sx.sc:175` | `*vol` (no arg) | `"Sx vol is X"` | `Sx` | current volume |
| `Classes/Sx.sc:264` | `prGenerateWave` invalid | `"🔴 Wave not valid. Use: …"` | empty Array | error string |
| `Classes/Fx.sc:57` | `*clear` (group) | `"🌵 All effects disabled on <group>"` | `Fx` (via `^this;`) | the message |
| `Classes/Fx.sc:62` | `*clear` (single) | `"🌵 All effects have been disabled"` | `Fx` | the message |
| `Classes/Fx.sc:285` | `*vstReadProgram` success | `"🔥 Loaded preset: <name>"` | `Fx` (implicit) | the message |
| `Classes/PxBuf.sc:4` | `*buf` empty folder | `"🔴 Folder … empty: <folder>"` | folder size (int) | error string |
| `Classes/PxBuf.sc:14` | `*buf` out of range | `"🔴 Folder … maximum number is N"` | clipped file index | error or clipped value |
| `Classes/PxMethods.sc:233` | `*stop` missing id | `"🔴 Pattern <id> does not exist"` | `Px` | error string |
| `Classes/PxRand.sc:8` | `*shuffle` history missing | `"Shuffle history <key> not found"` | `Px` | error string |
| `Classes/PxRand.sc:11` | `*shuffle` restore | `"Shuffle restored: <key>"` | `Px` | the message |
| `Classes/PxRand.sc:40` | `prSaveShuffleHistory` | `"Shuffle history: <key>"` | `Px` | the key |
| `Classes/PxRand.sc:45` | `prCreateNewSeeds` | `"🎲 Shuffle \<id>: <seed>"` | `Px` | seed value |
| `Classes/PxRand.sc:51` | `prGenerateRandNumber` | `"🎲 Seed: <id> -> <seed>"` | already `^seed` | ok, but message is lost |
| `Classes/Cx.sc:35/42` | `*list` | full config listing via `.postln` | `Cx` (implicit) | multi-line string |

### Lower priority (setup / infra paths, rarely called from shell)

| File:Line | Method | Notes |
|---|---|---|
| `Classes/PxMidi.sc:14/23/26` | MIDIOut init | called during startup |
| `Classes/Lx.sc:90` | `*loadSamples` | `"🔄 Lx with N channels"` — startup info |
| `Classes/PxBuf.sc:69` | `*loadSamples` | path error during startup |
| `Classes/PxMethods.sc:125` | `*stop(\all)` | `"When the music is over …"` easter egg |

### Skip

| File:Line | Method | Why |
|---|---|---|
| `Classes/Fx.sc:435-446` | `prActivateVst` callback | inside `.defer(1)` — no caller to return to |
| `Classes/PxOsc.sc:34` | OSC receive echo | just logs incoming code |
| `Classes/PxGui.sc:117` | GUI-only postln | not part of the DSL |
| `Classes/Array.sc:69` | seed print inside `shuffle` extension | side-effect during pattern generation |

## Suggested execution order

1. Add `^value;` to `Px.prPrint`, `Nx.prPrint`, `Sx.prPrint`. Also drop the `postln` if you want the `->` echo alone (see "duplicate output" note below).
2. Sweep the **High priority** table: prefix `^` on the final expression of each method, or refactor to build a local `resultMsg` when the print isn't the last statement (see `prAddEffect` in `Fx.sc` for the pattern).
3. Verify each fixed method from the shell:
   ```
   ./px.sh 'Nx.shuffle;'
   ./px.sh 'Dx.preset(8);'
   ./px.sh 'Sx.vol;'
   ...
   ```
4. Optionally address the Lower-priority list.

## Duplicate output note

If both `postln` inside `prPrint` and the returned string are emitted, SCIDE / the interpreter shows the message twice:

```
✨ Enabled \reverb mix: 1 …        ← postln
-> ✨ Enabled \reverb mix: 1 …     ← return echo
```

Two ways to avoid this:
- Drop the `postln` from `prPrint` and rely purely on the `->` echo of the return (what `Fx` group branch does now).
- Keep `postln` but wrap the shell reply so it doesn't re-print in the SC post window (would touch `PxOsc.sc`).

The first is simpler and consistent with the recent `Fx` changes.
