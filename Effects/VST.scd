(
Dictionary[
    \vst -> {
        \vstFilter -> { |in, vstBypass = 1|
            VSTPlugin.ar(in, 2, vstBypass);
        }
    };
]
)