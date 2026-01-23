/*
Chord Symbols Syntax:

Root:        A–G (uppercase)
Minor:       m
Major:       maj
Dominant:    7
Suspended:   sus2 / sus4
Added:       AddN
Altered:     SharpN / FlatN

Since a guitar is tuned to root 4 (E), we need to adjust the degrees accordingly
whenever we switch keys by updating the ~root value.

Below are the same chords transposed to start from degree 0
(relative to the chosen root = 4):

~guitarDegree = [0, 4, 8, 9b, 11, 15]; // emAdd9
~guitarDegree = [-3, 0, 3, 7, 9, 11]; // A major chord
~guitarDegree = [2, 6s, 9, 11, 13, 16]; // cMaj7

If we want to play using root 4:

~guitarDegree = [0, 4, 8, 9b, 11, 15]; // emAdd9
~guitarDegree = [0, 4s, 7, 8s, 11, 14]; // cMaj7
~guitarDegree = [0, 4, 8, 11, 12, 14]; // A major chord
*/

(
Dictionary[
	\A -> Dictionary[
		\degree -> [0, 2, 4],
		\guitarDegree -> [2, 6s, 9, 11, 13, 16],
		\intervals -> [0, 4, 7],
		\key -> 57,
		\root -> 9,
		\scale -> \major
	],
	\Am -> Dictionary[
		\degree -> [0, 2, 4],
		\intervals -> [0, 3, 7],
		\key -> 57,
		\root -> 9,
		\scale -> \minor
	],
	\Am7 -> Dictionary[
		\degree -> [0, 2, 4, 6],
		\intervals -> [0, 3, 7, 10],
		\key -> 57,
		\root -> 9,
		\scale -> \minor
	],
	\Am9 -> Dictionary[
		\degree -> [0, 2, 4, 6, 7],
		\intervals -> [0, 3, 7, 10, 14],
		\key -> 57,
		\root -> 9,
		\scale -> \minor
	],
	\A69 -> Dictionary[
		\degree -> [0, 1, 2, 4, 5],
		\intervals -> [0, 2, 4, 9, 11],
		\key -> 57,
		\root -> 9,
		\scale -> \major
	],
	\Amaj9Sharp11 -> Dictionary[
		\degree -> [0, 2, 4, 6, 8, 9],
		\intervals -> [0, 4, 7, 11, 14, 18],
		\key -> 57,
		\root -> 9,
		\scale -> \lydian
	]
]
)
