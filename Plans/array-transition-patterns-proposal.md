# Array transition patterns (proposal, not implemented)

## Surface

```supercollider
1 i: \piano note: [\c3 -> [\c3, \g3], \g3 -> [\a3, \c3], \a3 -> [\c3]].pfsm seed: 42;
2 i: \piano note: [\c3, \g3, \a3, \f3, \e3, \d3].markov seed: 42;
3 i: \piano note: [\c3, \g3, \a3, \f3, \e3, \d3].markov(stay: 0.12, home: 0.15, variety: 0.5);
```

Both methods return Patterns usable with `note:`. The array methods themselves do not take a seed; Px's `seed:` controls them when used in Px. Outside Px, they use SuperCollider's normal pattern randomness.

## `pfsm`

- The receiver is an ordered array of Associations. Each left side names one unique state and each right side is a nonempty array of successor states.
- The first rule's left side is the sole entry state. Emit its note before choosing a successor.
- Translate rules into a native `Pfsm` state table. Its successor selection remains uniform over entries: writing a target twice makes it twice as likely. A `nil` successor points to a terminal state and ends the stream when selected. Without a reachable terminal state, the stream continues indefinitely.
- Keep each rule's successor order and multiplicity. Reject duplicate left sides, unknown targets, empty successor arrays, and an empty rule list with a clear error. Do not silently add missing transitions or a return to the first state.
- The state value is a single note symbol or MIDI note number. Note resolution follows Px's existing `note:` behavior.

## `markov`

- The receiver is an ordered pool of distinct note symbols or MIDI note numbers. Preserve the supplied order. Reject empty pools and duplicate values so each note is one unambiguous state.
- Emit the first note immediately. Each following note depends on the current state. Keep one walk state per stream; the sequence has no fixed length or automatic restart.
- Array order defines a circular neighborhood. State `i` always has an outgoing edge to `(i + 1) % n`, which makes the directed graph strongly connected. This ring is a connectivity guarantee, not a playback sequence.
- For `n >= 3`, each state has `2 + floor(variety * (n - 3))` distinct nonself successors. Select the remaining successors without replacement using a seed, favoring smaller circular index distance. At the default `variety: 0.5`, a six-note pool has three nonself successors per state. For two notes there is one nonself successor; for one note the stream repeats that note.
- At each transition, repeat the current state with probability `stay`. Otherwise, when away from the first note, return to that note with probability `home`. If neither branch is taken, choose from the graph successors with weights proportional to the inverse of their circular index distance. The `home` branch is skipped at the first note; the graph choice can also land on the first note. All three controls accept values from 0 through 1.
- Defaults: `stay: 0.12`, `home: 0.15`, `variety: 0.5`. They give occasional repeated notes and returns while leaving most transitions to the graph. `variety: 0` still leaves two nonself choices when the pool has at least three notes, so it does not turn the method into a fixed loop.
- Very small pools have unavoidable limits: one note is constant; two notes can only alternate or stay. With three notes, every nonself edge is needed, so changing the seed changes the walk but cannot change the graph.

## Seed and lifecycle contract

- Resolve one effective seed for each Px pattern. An explicit `seed:` establishes the initial seed. Without one, generate and retain a seed for that pattern. `Px.shuffle` replaces the effective seed even when the user supplied `seed:`; restoring shuffle history restores the effective seed.
- Setting a new explicit `seed:` replaces any shuffle override. Reusing the same effective seed, ordered input, and controls recreates both graph and walk. Derive separate random streams for graph construction and walking so changes in one phase do not shift the other's random draws or disturb unrelated randomness.
- Build the graph lazily when a stream starts, after Px has resolved the effective seed. On a changed seed, pool, or control, the replacement stream begins again at the first note at Px's normal quantized update boundary. Ordinary uninterrupted playback remains an ongoing walk.
- Keep `seed: \rand` as Px's existing opt-out of fixed randomness. It starts a fresh graph and walk each time; `Px.shuffle` can still trigger a fresh stream.
- A new seed requests a new generated result; different seeds can coincidentally yield the same graph or opening notes, especially with small pools. `Px.shuffle` must choose a seed different from the current effective seed, although it cannot promise a different audible sequence in every case.

## Integration notes for implementation

- `Px.prGetPatternSeed` currently returns `pattern[\seed]` before consulting `Px.seeds[id]`, so `Px.shuffle` has no effect on an explicit seed. Review the shared seed resolver and shuffle history together before adding either method; beat and random-degree behavior should use the same effective-seed contract.
- `Px.prResolveNotes` currently wraps a Pattern in `Pcollect`. The new Patterns need to stay lazy through note resolution and seed binding. The transition graph must not be generated when `.markov` is first called, because `seed:` may only be known when Px processes the pattern.
- Px's cycle calculation recognizes `Pseq` but does not assign a phrase length to these Patterns. Their ongoing streams should use the regular quantization grid. Finite `pfsm` streams can end naturally at a terminal state.
- When implemented, update the Px help file, the README array-method table, and the px-agent reference docs. Do not add help files for the extended built-in Array class.
