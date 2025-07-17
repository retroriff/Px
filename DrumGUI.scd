(
var w = Window("Dancing To The Drum Machine", Rect(100, 100, 380, 400));

var machinePath = Quarks.folder +/+ "tidal-drum-machines/machines/";
var folders = PathName(machinePath).folders.collect(_.folderName);

var ezList = EZListView(
  parentView: w,
  bounds: Rect(10, 10, 280, 370),
  label: "Select Machine",
  items: folders,
  globalAction: { |ez, val|
    var index = ez.value;
    if (index.notNil) {
      var selectedFolder = folders[index];
      "Selected: %".format(selectedFolder).postln;
      Dx.use(selectedFolder);
    } {
      "No item selected.".warn;
    }
  },
  initVal: folders[0],
  initAction: false
);


var ampLabel = StaticText(w, Rect(310, 0, 50, 40))
.align_(\center)
.string_("Amp");

var ampKnob = Knob(w, Rect(310, 35, 50, 50))
.mode_(\round)
.value_(0.5)
.action_({ |v, x, y, m|
  var amp = v.value;
  "Amp: %".format(amp).postln;
  Dx.vol(amp);
});

w.front;
)