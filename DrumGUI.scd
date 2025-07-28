(
var drumMachines, folders, machinePath;
var firstCol, secondCol, thirdCol, mainView, row;

var w = Window("🛢️ Dancing To The Drum Machine", Rect(left: 0, top: 0, width: 420, height: 350));
w.background = Color(0.46045410633087, 0.64358153343201, 0.82325148582458);

mainView = CompositeView(w, w.view.bounds);
mainView.decorator = FlowLayout(mainView.bounds);
mainView.decorator.gap = 0@0;

// Row container for both columns
row = CompositeView(mainView, 500@340);
row.decorator = FlowLayout(row.bounds);
row.decorator.gap = 10@10;

// Three columns inside the row
firstCol = CompositeView(row, 200@320);
firstCol.decorator = FlowLayout(firstCol.bounds);

secondCol = CompositeView(row, 90@320);
secondCol.decorator = FlowLayout(secondCol.bounds);

thirdCol = CompositeView(row, 100@320);
thirdCol.decorator = FlowLayout(thirdCol.bounds);

// Labels
StaticText(firstCol, 200@20).align_(\center).string_("Drum Machines");
StaticText(secondCol, 80@20).align_(\center).string_("Amp");
StaticText(thirdCol, 80@20).align_(\center).string_("");

// Drum machine list
machinePath = Quarks.folder +/+ "tidal-drum-machines/machines/";
folders = PathName(machinePath).folders.collect(_.folderName);

// 🥁 Drum machine list
drumMachines = EZListView(
  parentView: firstCol,
  bounds: 200@300,
  globalAction: { |ez| Dx.use(folders[ez.value]) },
  items: folders,
  initVal: folders[0],
  initAction: false
);


// 🎲 Random selection function
~chooseRandomMachine = {
  var randomIndex = folders.size.rand;
  Dx.use(folders[randomIndex]);
  drumMachines.value = randomIndex;
};

// Right column = make a vertical container

// 📶 Amp Knob
Knob(secondCol, 80@80)
.mode_(\vert)
.value_(Dx.vol)
.mouseUpAction_({ |v| Dx.vol(v.value) });

StaticText(secondCol, 80@20).align_(\center).string_("Delay");

Knob(secondCol, 80@80)
.mode_(\vert)
.value_(Dx.fx[\delay])
.mouseUpAction_({ |v| Dx.delay(v.value) });

StaticText(secondCol, 80@20).align_(\center).string_("Reverb");

Knob(secondCol, 80@80)
.mode_(\vert)
.value_(Dx.fx[\reverb])
.mouseUpAction_({ |v| Dx.reverb(v.value) });

// 🔀 Random button
Button(thirdCol, 80@145)
.states_([["Random", Color.white, Color.blue(0.27)]])
.action_({
  var randomIndex = folders.size.rand;
  Dx.use(folders[randomIndex]);
  drumMachines.value = randomIndex;
});

Button(thirdCol, 80@145)
.states_([["Stop", Color.white, Color.red(0.8)]])
.action_({ Px.stop; });

w.front;
)

707 i: \bd dur: 1 delay: 0.2
Dx.drumMachine
Dx.gui
Px.last
a = Dictionary.new
a.removeAt(\a)
Fx.clear
~chooseRandomMachine.()
Dx.drumMachine
Dx.preset(\powerage, 1);
Dx.shuffle
Dx.delay(0)
Dx.reverb(1)
Px.last
Dx.fx
Dx.vol(0.5)
Dx.use("505");
Dx.use("707");
Dx.use("AkaiLinn");
Dx.use("27");
Dx.vol(1)
Dx.use(909);
Dx.use(707);