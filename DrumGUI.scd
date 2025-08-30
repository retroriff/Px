(
var drumMachines, folders, machinePath;
var firstCol, secondCol, thirdCol, mainView, row, knob, knobColor;
var linkColor = Color.new255(31, 41, 55);

var w = Window(
  name: "🛢️ Dancing To The Drum Machine",
  bounds: Rect(left: 0, top: 0, width: 420, height: 350)
).front.alwaysOnTop_(true)
.background_(Color.new255(26, 29, 34));

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
StaticText(firstCol, 200@20).align_(\center).string_("Drum Machines").stringColor_(Color.white);
StaticText(secondCol, 80@20).align_(\center).string_("Amp").stringColor_(Color.white);
StaticText(thirdCol, 80@20).align_(\center).string_("").stringColor_(Color.white);

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
)
.listView
.background_(linkColor)
.stringColor_(Color.white);

// 🎲 Random selection function
~chooseRandomMachine = {
  var randomIndex = folders.size.rand;
  Dx.use(folders[randomIndex]);
  drumMachines.value = randomIndex;
};

// Right column = make a vertical container

// 📶 Amp Knob
knob = Knob(secondCol, 80@80)
.mode_(\vert)
.value_(Dx.vol)
.mouseUpAction_({ |v| Dx.vol(v.value) });
knobColor = knob.color;
knobColor[1] = Color.cyan;
knob.color = knobColor;

StaticText(secondCol, 80@20)
.align_(\center)
.string_("Delay")
.stringColor_(Color.white);

knob = Knob(secondCol, 80@80)
.mode_(\vert)
.value_(Dx.fx[\delay])
.mouseUpAction_({ |v| Dx.delay(v.value) });
knobColor = knob.color;
knobColor[1] = Color.cyan;
knob.color = knobColor;

StaticText(secondCol, 80@20)
.align_(\center)
.string_("Reverb")
.stringColor_(Color.white);

knob = Knob(secondCol, 80@80)
.mode_(\vert)
.value_(Dx.fx[\reverb])
.mouseUpAction_({ |v| Dx.reverb(v.value) });
knobColor = knob.color;
knobColor[1] = Color.cyan;
knob.color = knobColor;

// 🔀 Random button
Button(thirdCol, 80@145)
.states_([["Random", Color.white, linkColor]])
.action_({
  var randomIndex = folders.size.rand;
  Dx.use(folders[randomIndex]);
  drumMachines.value = randomIndex;
})
.mouseDownAction_({ |butt|
  butt.states = [["Random", Color.black, Color.cyan]];
  butt.refresh;
})
.mouseUpAction_({ |butt|
  butt.states = [["Random", Color.white, linkColor]];
  butt.refresh;
});

Button(thirdCol, 80@145)
.states_([["Stop", Color.white, Color.red(0.8)]])
.action_({ Px.stop; })
.mouseDownAction_({ |butt|
  butt.states = [["Stop", Color.black, Color.cyan]];
  butt.refresh;
})
.mouseUpAction_({ |butt|
  butt.states = [["Stop", Color.white, Color.red(0.8)]];
  butt.refresh;
});

w.front;
)
