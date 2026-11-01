# Px pattern lifecycle

Status: `Px.keep(ids)` implemented separately. The remaining lifecycle changes are deferred.

## Goal

Make pause, stop, release, and solo describe distinct actions with consistent one-pattern and all-pattern forms. Preserve predictable timing during live performance and keep audio, MIDI, GUI, and stored pattern state in sync.

## Current behavior and problems

- `Px.pause(id)` preserves the pattern and sets `Pdef(id).source = nil` on the next `Px.quant` boundary. `Px.resume(id)` re-evaluates it.
- `Px.stop(id)` removes a pattern and sets its `Pdef` source to `nil`, then clears its `Ndef` and effects after a tail wait. `Px.stop` without an ID instead preserves all active patterns in `stoppedPatterns`, for `Px.play` to restart. These two forms have different meanings.
- `Px.solo(ids)` moves other active patterns from `last` to `mutedPatterns` and silences their `Pdef` sources. `Px.unsolo` restores them. Stopping a muted ID must remove it from both active and muted state.
- `Px.release(seconds, id)` removes one pattern, sets its `Pdef` source to `nil`, and frees its `Ndef` with an audio fade. Without an ID, it fades the master `Ndef`, clears stored state, and later scans the then-current `ndefList` and `Pdef.all`. A pattern started during that delay can be caught by the old cleanup.
- The no-ID `release` path does not stop event sources or explicitly release held MIDI notes when the fade begins. An audio proxy fade does not fade an external MIDI instrument. The current `time.isInteger` check also turns a fractional fade duration into the 10-second default.
- `pause` and `solo` retain patterns differently, and `Px.play` clears `stoppedPatterns` but leaves `pausedPatterns` alone. These state paths need a single lifecycle contract.

## Proposed public contract

| Command | Event and audio action | Stored state and recovery |
| --- | --- | --- |
| `Px.pause(id)` | Silence one pattern at the next `Px.quant` boundary by default. Do not free its proxy. | Keep its declaration; `Px.resume(id)` restores it. |
| `Px.pause` | Pause the patterns currently active on the same grid. | Keep declarations; `Px.play` restores only patterns paused by this all-pattern action. Patterns paused individually beforehand remain paused. |
| `Px.stop(id)` | Stop new events and remove one pattern without an intentional fade. Allow existing audio and effect tails a bounded cleanup period. | Remove it from active, muted, formatted, and proxy tracking; a new declaration is required. |
| `Px.stop` | Apply the same remove behavior to a snapshot of all managed IDs, including solo-muted and still-sounding finite patterns. | No `Px.play` recovery; a new declaration is required. |
| `Px.release(seconds, id)` | Stop new events, fade that pattern's audio proxy, then free its resources. | Remove it; a new declaration is required. |
| `Px.release(seconds)` | Stop new events for a snapshot of all managed patterns, including still-sounding finite patterns, fade their audio, then free only those captured resources. | Remove them; a new declaration is required. |
| `Px.solo(ids)` | Temporarily silence other active patterns while the selected patterns continue. | Keep muted declarations; `Px.unsolo` restores them. |
| `Px.keep(ids)` (implemented) | Keep the selected active patterns in their current state and remove every other managed pattern through normal stop cleanup. | The selection is permanent; `Px.unsolo` cannot restore the removed patterns. |

Keep `Px.release` as the canonical name. Do not add `Px.out` in this change: the existing `out:` pattern method and `Dx.out` use beats, while `Px.release` uses seconds and frees resources. Leave `Dx.release`, `Dx.out`, `Sx.release`, and `Sx.out` unchanged until a separate cross-class naming review. The finite pattern parameter `stop:` is also outside this change.

## Timing and MIDI decisions

- Retain the current beat-grid default for `Px.pause(id)` and use it for `Px.pause` with no ID. Add an explicit immediate mode, such as `grid: 0`, for the current immediate silence use case of no-ID `Px.stop`. Treat zero as a special case rather than passing it to `timeToNextBeat`.
- `stop` removes patterns when invoked; it does not wait for the beat grid or apply an amp fade. Existing synth envelopes and effects can finish during a bounded tail wait. `release` uses seconds for its audio fade; accept finite, nonnegative numeric durations, including fractions.
- Every path that silences a MIDI pattern must stop new MIDI events and send the needed note-offs for held notes. An audio fade must never be described as a MIDI volume fade. Inspect `PxMidi` note ownership, output selection, and channel conversion before changing the current channel-wide `prChannelNoteOff` behavior; multiple patterns may share a channel.
- `Px.play` resumes only patterns paused by the all-pattern pause action. `Px.resume(id)` resumes that ID if it is individually or globally paused. Neither operation restores a stopped, released, or still solo-muted pattern.
- `Px.solo(1, 2); Px.keep(1, 2)` lets the player audition two patterns, then make that selection permanent. `Px.stop` with no ID currently silences the audible patterns instead, so it must not be used to commit a solo selection.
- `Px.keep(1, 2)` also makes the permanent selection directly. It resolves IDs and group targets like `Px.solo`, but validates the entire selection against active patterns before changing anything. An empty or partly invalid selection never stops the whole set by accident. It includes already solo-muted and still-tracked finite patterns among the unselected patterns to remove.

## Implementation approach

1. Define lifecycle invariants for each ID: it belongs to at most one of the active and solo-muted sets; paused state records why it is silent; removed IDs cannot be restored by `play`, `resume`, or `unsolo`. Keep the GUI and `lastFormatted`, `cycleOrigins`, `ndefList`, meter mapping, and effects consistent with these states.
2. Add shared helpers for silencing an event source, releasing held MIDI notes, removing stored state, and scheduling proxy cleanup. Keep the distinction between retained pause/solo state and removed stop/release state visible in helper names.
3. Implement no-ID `Px.pause` and an immediate option. Preserve individually paused patterns when globally pausing and resuming. Make `Px.play` and `Px.resume` follow the contract above.
4. Make no-ID `Px.stop` remove a snapshot of active, muted, and still-sounding finite IDs through the same cleanup path as `Px.stop(id)`. Preserve safe tail handling when an ID is redeclared before delayed cleanup runs.
5. Rework both `Px.release` paths to capture their target IDs and proxy generation before the fade. Stop their event sources at fade start, clear only captured resources later, and never sweep `Pdef.all` or the live `ndefList` from an old release. Handle `Hush`/`\all` deliberately, including its wider Tdef and MIDI panic behavior.
6. Keep `solo` and `unsolo` on the same state rules. Redeclaring a muted ID must resolve its stale muted entry, and stopping or releasing a muted ID must prevent later restoration. Retain the implemented `Px.keep(ids)` behavior as the permanent-selection command; do not add a separate `Px.stopMuted` method unless a distinct need emerges.
7. Reconcile this work with `Plans/remove-pdef-layer.md` before implementation. That plan replaces `Pdef` operations with `Ndef` operations; the lifecycle contract here should hold with either architecture. Do not apply its old stop-operation instructions mechanically after this refactor.

## Compatibility and documentation

Changing `Px.stop` with no ID is a breaking change. First add and document `Px.pause` with no ID and its immediate mode. Update examples and live-coding references that use `Px.stop; Px.play` to use pause/play. Then change no-ID `Px.stop` to remove all patterns as part of an explicit migration, with a short release note. Keep the per-ID `Px.stop(id)` behavior.

When implementing, update `HelpSource/Classes/Px.schelp`, `README.md`, `Docs/pattern-internals.md`, and the px-agent docs, especially `01-basic-usage.md`, `04-solo.md`, and the MIDI guidance. Document the difference between beat-grid pause, immediate stop, audio fade in seconds, and pattern-level `out:` in beats.

## Verification when implementation is authorized

- Statically inspect each transition among active, individually paused, globally paused, solo-muted, stopped, and released states. Check `Px.play`, `Px.resume`, `Px.unsolo`, full redeclarations, and GUI refresh after each transition.
- Check one and all forms with audio patterns, regular MIDI notes, held MIDI notes, shared MIDI channels, drum patterns, finite `repeat`/`stop:` patterns, and effects with tails.
- Start a new pattern, including one with a reused ID, during a long `Px.release` fade. Delayed cleanup must not stop or free the new pattern. Check `Hush` separately.
- Confirm stopped muted patterns disappear from the GUI and cannot be revived by `unsolo`; no `amp Nil could not be resolved` warning should be caused by orphaned lifecycle state.
- Solo two of 20 patterns, run `Px.keep(1, 2)`, and confirm only those two keep playing. `Px.unsolo` must not restore the removed 18.
- Run `Px.keep(1, 2)` directly with 20 active patterns and confirm the same permanent result. An empty selection, an unknown ID, or a group target with no active matches must leave all patterns untouched.
- Check immediate and beat-grid pause timing, fractional release times, and the exact recovery behavior of `Px.play` and `Px.resume`.
- Do not launch `sclang` for verification without the user's explicit request. After static review, provide a manual live-session checklist for the user to run at a suitable time.
