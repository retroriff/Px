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

2. **`Classes/*.sc`** - Source code to modify
   - `Classes/Number.sc` - Integer method extensions (DSL entry point)
   - `Classes/Px.sc` - Base pattern generator
   - `Classes/Fx.sc` - Effects handler
   - `Classes/Dx.sc` - Drum machines
   - See `Docs/init-context.md` for full file organization

### Development Quick Reference

**Common development tasks:**

- Add effect → Edit `Effects/*.scd` + `Classes/Fx.sc`
- Add pattern parameter → Edit `Classes/Number.sc` + `Classes/Px.sc`
- Add drum preset → Create `Presets/yaml/*.yaml`

---

## File Type Reference

- `.sc` - SuperCollider class definitions (source code)
- `.scd` - SuperCollider data/scripts (executable code, effect definitions)
- `.yaml` - Drum machine preset data

# Variable definitions

In SuperCollider, `var` is lexically scoped and each { ... } introduces a new lexical scope (function block).
Variables defined inside an if branch belong only to that branch’s scope and are not visible outside.
If a variable is used across branches or later in the function, it must be declared in the outer (top-level) scope.
