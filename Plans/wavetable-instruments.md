# Wavetable Instruments for Px

## Status

Proposal. No `wt:` method, `\wt` SynthDef, or wavetable preparation code exists yet.

## Goal and Surface

Turn a short region of a sample in the existing Px sample library into a pitched oscillator voice. Keep the sample's waveform choice separate from the note's frequency.

```supercollider
1 wt: "fm:2" note: \c2 dur: 2;
2 wt: "fm:seq" note: [\c2, \ds2, \fs3].pseq dur: 2;
3 wt: "fm:rand" degree: [0, 2, 4, 5].pseq scale: \minor octave: 3 dur: 0.5;
4 wt: "fm:2" wtRegion: [0.1, 0.15] note: \c2 dur: 2;
```

- `wt:` accepts the existing folder and zero-based sample index syntax. `:seq` chooses the next sample on each event; `:rand` follows Px's current seeded sample-selector behavior. The table choice and note sequence advance independently, one value of each per event.
- `note:` uses Px's current note symbols, MIDI numbers, note patterns, and chords. `degree:` uses the existing scale and octave behavior. Both produce the Event frequency passed to the wavetable voice. Do not apply the sample playback rate conversion that `play:` and `loop:` use for degrees.
- `dur:` schedules notes in beats. `legato:` and `sustain:` govern the voice gate and release. Do not infer note duration from the source file length; when omitted, use Px's normal duration rule.
- `wtRegion:` is a normalized `[start, end]` range within the source sample, with `[0, 0.05]` as the initial default. Validate `0 <= start < end <= 1`. The region is a sound-design choice, not a claim that the first 5% contains a natural waveform cycle.
- `play: "fm:2" wave: 1` is not part of the first API. Keep `play:` as ordinary sample playback and make the different oscillator behavior explicit through `wt:`.

## Pitch and Sound Contract

Prepare the selected region as **one oscillator cycle**, then play that cycle at the Event's `\freq`. Under Px's note naming, `\c2` resolves to MIDI 48, or about 130.81 Hz; the oscillator repeats the prepared cycle at that frequency. The source sample's original pitch and length must not determine the new note's frequency.

This guarantees the oscillator's repetition rate, not that every source region will sound like a pure, unambiguous `\c2`. A region containing silence, several internal cycles, noise, or a strong overtone can sound weak, noisy, or octave-dominant. Do not silently claim automatic fundamental detection. The user can choose a better region with `wtRegion:` or use a prepared single-cycle sample.

Resample the region to an oscillator-compatible power-of-two table, remove DC offset, and make its loop boundary continuous without wiping out its character. Normalize conservatively so different samples do not create large level jumps; preserve `amp:` as the user-facing gain control. Convert stereo sources to mono for this first oscillator voice and pan the result through Px's usual output path. Reject an empty or effectively silent region with a clear diagnostic.

## Runtime Design

1. Add `Number.wt` as an instrument entry point. Reuse the parser for `"folder:index"`, `:seq`, and `:rand`, then route the pattern to `\wt` with a prepared wavetable buffer. Add `wtRegion:` as a chainable parameter. Keep sample selection behavior consistent with `play:` and `loop:`, including `seed:` and missing-sample handling.
2. Extend the sample catalog to retain each audio file's source path alongside its ordinary playback Buffer. Prepare tables from the source file before an event needs them; do not read, decode, or build a table inside a note event or SynthDef. Cache tables by source file and region so repeated notes and selectors reuse them.
3. When `:seq` or `:rand` references a folder, prepare every selectable table before starting or replacing that pattern. Handle preparation asynchronously and wait for all server buffers to be ready. If preparation fails, report which sample failed and leave an already playing pattern intact.
4. Add a dedicated `SynthDefs/Wavetable.scd` with a `\wt` SynthDef. Use a wavetable oscillator such as `Osc` with `\buf` and `\freq`, plus the same envelope, gate, amp, pan, and output behavior expected of other Px instrument voices. One note event creates one voice so repeated notes, overlaps, and chords can sound together.
5. Route `wt:` through Px's existing Pbind, Ndef mixer, effects, metering, quantized update, pause, and stop paths. Ensure `prCreateLoops` does not reinterpret a wavetable's `degree:` as sample `rate:` or assign a loop's beat-synced duration.
6. Free derived wavetable buffers when samples are reloaded or the library is cleared, and rebuild them when the source files or `wtRegion:` change. Avoid freeing a table while an active voice still reads it; settle the old voices before releasing obsolete buffers.

The implementation should reuse sample lookup and selector logic where possible. It should not add a `wave` mode to `\playbuf`: its current one-shot playback buffer and rate behavior have different semantics from an oscillator's prepared table and frequency control.

## Implementation Order

1. Introduce source-path metadata and table preparation for one fixed sample and region. Check table format, buffer readiness, and cleanup through static review before connecting the DSL.
2. Add `\wt` and wire `wt: "fm:2" note: \c2 dur: 2` through Px without changing `play:` or `loop:`.
3. Add `wtRegion:` validation, `:seq`, `:rand`, and their cache preparation and seed behavior.
4. Cover note patterns, degree and scale patterns, chords, overlaps, envelopes, effects, missing samples, reloads, and source-region changes.
5. Document the feature in `HelpSource/Classes/Px.schelp`, the README instrument-method table, and the px-agent reference docs. Do not add a help file for the extended Number class.

## Verification

- Inspect the conversion path from `\c2` to `\midinote` to Event `\freq`, and confirm the `\wt` SynthDef consumes `\freq` directly. Inspect `degree:` and `scale:` to confirm they do not become a sample playback rate in `wt:` patterns.
- Check that fixed-index, `:seq`, and `:rand` patterns select the expected table per event, including seeded reevaluation and `seed: \rand` behavior.
- Check table preparation and cleanup for mono and stereo files, short and silent regions, missing files, reloading, and edits while a pattern is playing.
- Check that `play:`, `loop:`, drum sample selectors, ordinary SynthDefs, and their effects routing keep their current behavior.
- Perform static code review without launching `sclang`. The user can run live listening and pitch checks in their SuperCollider session when ready; compiling the class library or booting the server would interrupt that session.

## Current Code Touchpoints

- `Classes/Number.sc` parses sample selectors and provides instrument entry methods.
- `Classes/PxBuf.sc` loads sample buffers and resolves `play:` and `loop:` selectors.
- `Classes/PxMidi.sc` resolves `note:` symbols to MIDI notes.
- `Classes/Px.sc` assembles the pattern and creates the Pbind/Ndef voice path.
- `SynthDefs/PlayBuf.scd` and `SynthDefs/Loop.scd` show the existing, distinct sample playback behavior.

## References

- Strudel wavetable recipe: https://strudel.cc/recipes/recipes/#wavetable-synthesis
- Strudel synth guide: https://strudel.cc/learn/synths/#wavetable-synthesis
- SuperCollider `Osc`: https://doc.sccode.org/Classes/Osc.html
- SuperCollider `Event` pitch conversion: https://doc.sccode.org/Classes/Event.html
