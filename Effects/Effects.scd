(
Dictionary[
    \gverb -> { |roomsize = 200, revtime = 0.7|
        \filterIn -> { |in|
            GVerb.ar(
                in,
                \gverb1.kr(roomsize),
                \gverb2.kr(revtime)
            );
        }
    }
];
)