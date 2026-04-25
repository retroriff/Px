(
Dictionary[
    \gverb -> { |roomsize = 200, revtime = 0.7|
        \filterIn -> { |in|
            GVerb.ar(
                in,
                \gverb1.kr(roomsize, 0.5, spec: ControlSpec(1, 300, \lin)),
                \gverb2.kr(revtime, 0.5, spec: ControlSpec(0.5, 100, \exp))
            );
        }
    }
];
)
