PresetsFromJSON {
    *new { |json|
        case
        { json.isString and: { json.every { |x| x.isDecDigit } } }
        { ^json.asInteger }

        { json.isString and: { "^[-]?(0|[1-9][0-9]*)(\\.[0-9]+)?([eE][+-]?[0-9]+)?$".matchRegexp(json.asString) } }
        { ^json.asFloat }
        
        { json == "true" }
        { ^true }
        
        { json == "false" }
        { ^false }
        
        { json == nil }
        { ^Ref(nil) }
        
        { json.isArray }
        { ^json.collect { |x| this.new(x) } }
        
        { json.isKindOf(Dictionary) }
        {
            var event = Event.new;
            json.pairsDo { |key, value|
                event.put(key.asSymbol, this.new(value));
            };
            if (event[\presets].isNil)
            { ^event }
            { ^event[\presets] }
        }

        { ^json }
    }
}
