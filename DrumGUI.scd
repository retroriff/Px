(
var drumMachines, ampLabel, ampKnob;
var w = Window("Dancing To The Drum Machine", Rect(100, 100, 380, 400));
var machinePath = Quarks.folder +/+ "tidal-drum-machines/machines/";
var folders = PathName(machinePath).folders.collect(_.folderName);

// 🥁 Drum Machines List
drumMachines = EZListView(
  parentView: w,
  bounds: Rect(10, 10, 200, 370),
  label: "Select Machine",
  items: folders,
  globalAction: { |ez, val|
    var index = ez.value;
    Dx.use(folders[index]);
  },
  initVal: folders[0],
  initAction: false
);

if (Dx.drumMachine.notNil) {
  var idx = folders.detectIndex { |x| x == Dx.drumMachine.asString };
  if (idx.notNil) { idx.postln; drumMachines.value = idx };
} {
  folders[0]; // clears selection
};

// 📶 Amp Knob
ampLabel = StaticText(w, Rect(310, 0, 50, 40))
.align_(\center)
.string_("Amp");

ampKnob = Knob(w, Rect(310, 35, 50, 50))
.mode_(\round)
.value_(0.5)
.action_({ |v, x, y, m|
  var amp = v.value;
  "Amp: %".format(amp).postln;
  Dx.vol(amp);
});

// 🎲 Random selection function
~chooseRandomMachine = {
  var randomIndex = folders.size.rand;
  Dx.use(folders[randomIndex]);
  drumMachines.value = randomIndex;
};

// 🔀 Random button
Button(w, Rect(310, 100, 50, 30))
.states_([["Random", Color.white, Color.blue(0.27)]])
.action_({
  ~chooseRandomMachine.();
});

w.front;
)

Dx.drumMachine
Dx.preset(\break, 1);
Dx.use("505");
Dx.use("707");
Dx.use("AkaiLinn");
Dx.use("27");
Dx.vol(1)
Dx.use(909);
Dx.use(707);