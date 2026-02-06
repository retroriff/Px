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
