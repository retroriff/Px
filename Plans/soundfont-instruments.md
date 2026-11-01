# SoundFont Instruments for Px

## Status

Proposal for discussion. No playback or conversion code has been implemented.

## Goal

Make selected Strudel `gm_*` instruments usable through Px's existing `i:` syntax, with the same zero-based variant numbers and normal Px note, duration, sustain, amplitude, pan, pattern, and effects behavior.

```supercollider
Px.loadSoundfonts("/path/to/converted-soundfonts");
1 i: "gm_synth_bass_1:3" note: \c2 dur: 1;
2 i: "gm_epiano1:0" note: [\c3, \eb3, \g3].pseq dur: 0.5 sustain: 0.4;
```

The public name is a soundfont instrument. Internally, Px resolves each note to a sample zone and uses a shared sample-voice SynthDef. Loading and selecting the instrument must happen before the hit; a hit must never fetch or decode audio.

## Why Convert Presets

SuperCollider buffers can play decoded audio but a normal SynthDef does not parse `.sf2` or WebAudioFont `.js` presets. For the first implementation, convert only chosen WebAudioFont presets to audio files plus a manifest. This keeps playback inside scsynth and the existing Px audio and effects routing. A native SF2 parser or server plugin is a later option if direct use of arbitrary SF2 files becomes necessary.

The WebAudioFont preset is the reference for matching Strudel. Loading the original SF2 through a different player could produce a different result because zone selection, loops, envelopes, and gain are rendered by a different engine.

## Source and Conversion

1. Pin the Strudel `gm_*` registry version used for conversion. Its array order defines `:0`, `:1`, and so on; commented-out source presets are not variants. Store the original WebAudioFont preset identifier for each variant.
2. Convert selected preset files from the WebAudioFont sound collection offline. Decode each playable zone into a WAV file and write an alphabetized-key YAML manifest.
3. Preserve the source identifier and URL, sample rate, key range, original pitch, coarse and fine tuning, loop start and end, and available envelope data for each zone. Preserve source provenance and license information with the converted instrument.
4. Validate that every manifest entry references a readable sample and that ranges, loop points, and variant indexes are valid. Reject incomplete presets with a useful error.
5. Keep third-party audio out of the repository by default. The user chooses and stores converted presets in a local library path.

The converter should work from explicitly selected preset IDs so the first version does not download or load an entire General MIDI library. If a source uses a compressed sample encoding, decoding is an offline converter concern, not a runtime dependency of Px.

## Px Runtime Design

- Add a dedicated `Px.loadSoundfonts(path)` catalog and buffer cache. Keep it separate from `Px.loadSamples`, whose current folder scanner loads ordinary WAV/AIFF files without instrument metadata.
- Recognize soundfont names only when they exist in that catalog. Keep the current interpretation of `i:` and `:number` for SynthDefs and drum samples. `Number.prPlay` already puts the suffix in `\file`; the soundfont resolver can treat that value as the variant index only for catalog entries.
- Resolve the final MIDI pitch and variant for every event, after `note:` and other pitch patterns have produced their values. Select the matching sample zone and playback rate using the preset's original pitch and tuning. Support repeated notes, note sequences, chords, and per-event instrument patterns.
- Feed `\buf`, playback rate, envelope, loop, amp, pan, gate, and output controls to one internal sample-voice SynthDef. Create one synth node per note so overlapping notes remain polyphonic. Respect Px `sustain:` in beats, including release after note-off.
- Handle one-shot zones and sustain loops correctly. Loop only the declared region while the note is held; let the release finish cleanly when the gate closes. Preserve mono and stereo source channels.
- Use the existing Ndef/Px mixer and effect path. Loading, stopping, reloading, and CmdPeriod must release buffers and voices cleanly. Report missing instruments, variants, and pitch zones without substituting an unrelated sound.
- Preload selected instruments before patterns start. Permit unloading unused presets and reloading a converted library without restarting the server.

## Integration Order

1. Build the offline converter and manifest validator for one representative bass preset. Confirm zone and variant metadata against Strudel's registry and WebAudioFont source.
2. Implement catalog loading and the generic voice SynthDef. Exercise basic notes, sustain, release, loops, and overlapping voices with a fixed preset.
3. Add the Px `i:` resolver, preserving existing SynthDef, drum, and sample behavior. Cover `\note` sequences, chords, `:variant`, and normal Px effects.
4. Add selective preload/reload and clear missing-asset diagnostics.
5. Document `Px.loadSoundfonts`, the `i: "name:variant"` form, source conversion, and limitations in `HelpSource/Classes/Px.schelp`, `README.md`, and the px-agent reference docs.

## Verification

- Compare the same notes, timing, sustain, and variant in Strudel and Px, starting with `gm_synth_bass_1` and `gm_epiano1`. Compare attacks, sustained portions, release, pitch, and loop boundaries by ear and recording.
- Check all key-range boundaries and at least one pitch between original sample pitches. Check short and long notes, overlapping notes, chords, repeated hits, and stopping a pattern during sustain.
- Check variant indexes against the pinned Strudel array, including a preset omitted from Strudel's mapping. Verify missing assets produce clear errors.
- Check that existing `i: \saw`, drum `:index`, and `play:` patterns still resolve as before, and that Fx routing and master metering work.
- Perform code and manifest validation without launching `sclang`. The user runs live SuperCollider listening checks when ready, because automated server boot or library recompilation would interrupt the performance session.

## Decisions to Set Before Implementation

- Choose the first one or two Strudel variants to convert and compare.
- Decide whether to use Strudel's current registry or a pinned historical version for stable variant numbers.
- Decide whether the local converted library should live under the existing samples path or a separate user-selected path.
- Define the acceptable similarity target: identical selected sample data and note mapping, or also a close match for Strudel's envelopes and output level.

## References

- Strudel SoundFont registry: https://codeberg.org/uzu/strudel/src/branch/main/packages/soundfonts/gm.mjs
- Strudel SoundFont loader: https://codeberg.org/uzu/strudel/src/branch/main/packages/soundfonts/fontloader.mjs
- WebAudioFont source collection: https://github.com/felixroos/webaudiofontdata
- SuperCollider buffer playback: https://doc.sccode.org/Classes/Buffer.html and https://doc.sccode.org/Classes/BufRd.html
