(
Dictionary[
    \reverb -> { |room = 0.7, damp = 0.7|
        \filterIn -> { |in|
            FreeVerb.ar(
                in,
                mix: 0.6,
                room: \reverb1.kr(room),
                damp: \reverb2.kr(damp)
            );
        }
    };
]
)