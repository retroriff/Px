# Code Conventions

Formatting rules to follow when writing or modifying SuperCollider code in this project.

## Blank lines around conditionals

Conditional blocks (`if`, `switch`, `case`) must be separated from surrounding code by blank lines, unless the conditional is the first statement in a scope.

**Bad:**

```supercollider
x = 10;
if (x > 5)
{ x = x + 1 };
y = x * 2;
```

**Good:**

```supercollider
x = 10;

if (x > 5)
{ x = x + 1 };

y = x * 2;
```

**Also good** (first statement in scope, no blank line needed before):

```supercollider
*myMethod { |x|
    if (x > 5)
    { x = x + 1 };

    y = x * 2;
}
```

## Alphabetical var declarations

Variables in a `classvar` or `var` statement must be listed in alphabetical order.

**Bad:**

```supercollider
var octave, chord;
```

**Good:**

```supercollider
var chord, octave;
```

## Prefer `if` as statement over expression

Use `if` to mutate an existing variable rather than assigning an inline `if` expression to a new variable.

**Bad:**

```supercollider
var q = if (quality == \rand) { aliases.keys.choose } { quality.asSymbol };
```

**Good:**

```supercollider
if (quality == \rand)
{ quality = aliases.keys.choose }
{ quality = quality.asSymbol };
```
