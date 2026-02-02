# LLM Agent Task Routing

- Write clean, solid, and maintainable code.
- Avoid unnecessary comments. The code should be self-explanatory through clear naming and structure. Only add comments to explain non-obvious logic, edge cases, or complex behavior.

### Primary References (read in order)

1. **`Docs/init-context.md`** - Complete architecture and development workflow
   - Core class hierarchy
   - How the DSL pattern works
   - Pattern generation flow
   - Development workflow and testing
   - How to add features (effects, parameters, presets)

2. **`Docs/pattern-internals.md`** - Deep technical reference for pattern system
   - Pattern data structure (all dictionary keys)
   - ID systems (regular, manual drum, preset patterns)
   - Beat generation and fill mechanics
   - Drum machine architecture internals
   - Pattern lifecycle and relationships

3. **`Classes/*.sc`** - Source code to modify
   - `Classes/Number.sc` - Integer method extensions (DSL entry point)
   - `Classes/Px.sc` - Base pattern generator
   - `Classes/PxDebouncer.sc` - Internal class that batches method chain calls into a single pattern update
   - `Classes/Fx.sc` - Effects handler
   - `Classes/Dx.sc` - Drum machines
   - `Classes/Nx.sc` - Musical chord data manager
   - See `Docs/init-context.md` for full file organization

### Development Quick Reference

**Common development tasks:**

- Add effect → Edit `Effects/*.scd` + `Classes/Fx.sc`
- Add pattern parameter → Edit `Classes/Number.sc` + `Classes/Px.sc`
- Add drum preset → Create `Presets/yaml/*.yaml`
- Add chord → Edit `Score/*.scd`
- Add/modify class method → Update corresponding `HelpSource/Classes/<ClassName>.schelp`

**Help file conventions:**

- Help files are in `HelpSource/Classes/` using SCDoc format (.schelp)
- Private methods (starting with `pr`) are not documented
- When adding new public methods, update the corresponding help file
- Reference existing help files (Px.schelp, Fx.schelp, Sx.schelp) for format examples

**Data file conventions:**

- Dictionary entries must be in alphabetical order by key
- When adding new entries, insert at the correct alphabetical position

---

## File Type Reference

- `.sc` - SuperCollider class definitions (source code)
- `.scd` - SuperCollider data/scripts (executable code, effect definitions)
- `.schelp` - SuperCollider help files (SCDoc format documentation)
- `.yaml` - Drum machine preset data

## Class Extensions Across Files

SuperCollider's `+ ClassName { }` syntax allows extending classes across multiple files. When investigating a class, search ALL files for extensions:

**Number** (DSL entry point methods):
- `Number.sc` - Core methods (play, loop, amp, dur, beat, etc.)
- `PxNotes.sc` - Note methods (degree, arp, scale, sus)
- `PxFx.sc` - Effect methods
- `PxBuf.sc` - Buffer/sample methods
- `PxMidi.sc` - MIDI methods

**Px** (pattern generator):
- `Px.sc` - Core pattern creation
- `PxBeats.sc` - Beat generation
- `PxNotes.sc` - Degree/scale processing
- `PxFx.sc` - Effect processing
- `PxBuf.sc` - Buffer/loop handling
- `PxMidi.sc` - MIDI integration
- `PxGui.sc` - GUI methods
- `PxRand.sc` - Randomization
- `PxMethods.sc` - Utility methods

**Symbol**: `Symbol.sc`, `PxNotes.sc`, `PxBuf.sc`, `PxMidi.sc`

When debugging or modifying behavior, always grep for `^\+ ClassName` to find all extensions.

# Variable definitions

In SuperCollider, `var` is lexically scoped and each { ... } introduces a new lexical scope (function block).
Variables defined inside an if branch belong only to that branch’s scope and are not visible outside.
If a variable is used across branches or later in the function, it must be declared in the outer (top-level) scope.
