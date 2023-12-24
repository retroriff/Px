PresetsFromYAML {
    *new { |yaml|
        case
        { yaml.isString and: { yaml.every { |x| x.isDecDigit } } } { ^yaml.asInteger }
        { yaml.isString and: { "^[-]?(0|[1-9][0-9]*)(\\.[0-9]+)?([eE][+-]?[0-9]+)?$".matchRegexp(yaml.asString) } } { ^yaml.asFloat }
        { yaml == "true" } { ^true }
        { yaml == "false" } { ^false }
        { yaml == nil } { ^Ref(nil) }
        { yaml.isArray } { ^yaml.collect { |x| this.new(x) } }
        { yaml.isKindOf(Dictionary) } {
            var event = Event.new;
            yaml.pairsDo { |key, value|
                event.put(key.asSymbol, this.new(value));
            };
            if (event[\presets].isNil)
            { ^event }
            { ^event[\presets] }
        }
        { ^yaml }
    }
}
