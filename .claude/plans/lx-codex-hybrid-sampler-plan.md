# Lx Hybrid Sampler Redesign Plan

## Summary

Redesign Lx as a performance looper for loops/stems: keep the existing tempo-synced loop layer, then add slice sequencing, roll/groove variation, and a granular texture layer.

This targets the missing musical behaviors found in classic sampler workflows: SP-style chop/resampling/effects/groove and MPC-style note repeat/16-level variation, while preserving the current simple `Lx.loadSamples`, `Lx.play`, `Lx.gui` workflow.

## Key Changes

- Keep backward compatibility in `Classes/Lx.sc`: existing `loadSamples`, `play`, `stop`, `buf`, `next`, `prev`, `amp`, `dur`, `start`, `trim`, `vol`, `shuffle` continue to work.
- Fix the documented-but-missing `Lx.rate(channel, value = 1)` method and make `prCreatePattern` preserve `rate`.
- Add slice mode:
  - `Lx.slices(channel, countOrPoints = 16)` creates fixed regions for the current buffer.
  - `Lx.slice(channel, index)` plays/selects one region by mapping to `start` + `trim`.
  - `Lx.seq(channel, steps, dur = 0.25, repeats = inf)` creates a companion Px pattern like `\lx0Seq`, using `beats` to preserve original loop speed while sequencing slice starts.
  - `Lx.swing(channel, amount = 0.57)` applies alternating timing offsets to slice sequences.
  - `Lx.roll(channel, division = 0.125, repeats = 8, slice = \current)` creates a short finite repeat pattern for fills.
- Add texture mode:
  - Create `SynthDefs/lxGrain.scd` using SuperCollider granular buffer playback. Because granular buffer UGens poll grain args at grain creation time, density, grain size, position jitter, pitch, and pan can be modulated musically while running.
  - `Lx.texture(channel, mix = 0.35, density = 18, size = 0.08, jitter = 0.02, spray = 0.12, pitch = 0, freeze = false)` starts/updates a companion pattern like `\lx0Texture`.
  - `Lx.texture(channel, nil)` disables the texture layer.
- Add `Lx.mutate(channel = nil, amount = 0.5)` as a safer replacement for pure random shuffle: it nudges slice order, start, trim, rate, and texture parameters without changing loaded samples.

## GUI

- Replace the current one-column-per-channel GUI with a compact mixer + focused editor:
  - Left: channel rows with sample index, mute/solo, amp, play state.
  - Right: selected channel tabs for `Loop`, `Slice`, and `Texture`.
  - Bottom: performance buttons for `Play`, `Stop`, `Shuffle`, `Mutate`, `Roll`, and `Freeze`.
- Slice tab shows 16 region pads/steps by default, with controls for sequence duration, swing, roll division, and randomization amount.
- Texture tab exposes mix, density, grain size, jitter, spray, pitch, and freeze.

## Implementation Notes

- Store new state in alphabetized classvars such as `sequences`, `sliceSets`, and `textureState`; keep SuperCollider `var` declarations before executable statements.
- Companion patterns must be stopped, muted, soloed, and refreshed together with their parent channel.
- Changing sample with `buf`, `next`, or `prev` resets slices to the current count and updates texture buffers.
- Do not implement actual resampling/export in v1; design the state cleanly so `Lx.resample(channel, beats)` can be added later.

## Test Plan

- Recompile SuperCollider, run `Px.loadSynthDefs`, load a loop/stem folder, and verify existing Lx examples still work.
- Verify `Lx.rate`, `Lx.slices`, `Lx.slice`, `Lx.seq`, `Lx.swing`, `Lx.roll`, `Lx.texture`, and `Lx.mutate` from code and GUI.
- Verify `Lx.stop(channel)`, `Lx.stop`, CmdPeriod, mute, solo, and sample switching clean up base, sequence, and texture companion patterns.
- Update `HelpSource/Classes/Lx.schelp`, `README.md`, and the px-agent docs with concise examples for loop, slice, and texture workflows.

## Assumptions

- V1 optimizes loops/stems, not one-shot drum pad banks.
- V1 is performance-first: no file-writing resampling/export yet.
- No new external dependencies.
- Existing Px/Fx routing remains the audio backbone so channel effects continue to work.
