Poly {
	
	var <>tempo;
	
	var <>currentIndex;
	var <>rhythms;
	var <>freqs;
	var <>amps;
	
	var <>sBounds;
	var <>window;
	var <>faderContainer;
	var <>buttonContainer;
	var <>faderContRect;
	var <>buttonsContRect;
	
	var <>guiRect;
	var <>faderHeight;
	var <>channelWidth;
	var <>channelHeight;
	var <>initialGUIWidth;
	var <>addButtonHeight;
	var <>removeButtonHeight;
	var <>faders;
	
	*initClass {
		SynthDef(\polyBeep) { |freq=440, amp=0.1, out=0|
			var sine = Saw.ar(freq,amp);
			var env = EnvGen.ar(Env.perc(releaseTime:0.5), doneAction: 2);
			var sig = sine*env;

			Out.ar(out,
				Pan2.ar(sig, 0, 1);
			);
		}.load(Server.default);
		
	}
	
	*new {
		^super.new.init;
	}

	init { 
		currentIndex = 0;
		rhythms = List[];
		freqs = List[];
		amps = List[];
		faders = List[];
		tempo = 1;
		
		this.createGUI;
	}
	
	addRhythm {|aIndex, aDivision, aValue|
		var ret;
		var beat;

		freqs.add(440);
		amps.add(1);
		beat = (tempo/aDivision)*aValue;
		ret = Routine {
			inf.do {
				Synth(\polyBeep, [\freq, freqs[aIndex], \amp, amps[aIndex]]);
				beat.wait
			}
		};
		rhythms.add(ret);
		
		this.addGUIChannel(currentIndex, aDivision, aValue);
		currentIndex = currentIndex + 1;
	}
	
	play {
		rhythms.do { |item, i|
			item.reset;
		};
		rhythms.do { |item, i|
			item.play;
		};
	}
	
	stop {
		rhythms.do { |item, i|
			item.stop;
		};
	}
	
	removeRhythm {|index|
		this.removeGUIChannel(index);
		[faders, rhythms, amps, freqs].do { |item, i|
			item.removeAt(index);
		};
		currentIndex = currentIndex - 1;
		
	}
		
	createGUI {
		var addButton;
		var playButton;
		var stopButton;
		var labelBoxRect;
		var divisionLabel;
		var numberLabel;
		var divisionBox;
		var numberBox;
		var playButtonRoutine;
		
		sBounds = Window.screenBounds;
		faderHeight = 400;
		removeButtonHeight = 20;
		channelWidth = 50;
		channelHeight = faderHeight+removeButtonHeight;
		addButtonHeight = 50;
		initialGUIWidth = channelWidth * 5;
		
		faderContRect = Rect.new(0,0, initialGUIWidth, channelHeight);
		buttonsContRect = Rect.new(0,channelHeight, initialGUIWidth, addButtonHeight*3);
		guiRect = Rect.new(sBounds.width*0.33, sBounds.height*0.33, initialGUIWidth, channelHeight+buttonsContRect.height);
		window = Window.new("Poly", guiRect);
		faderContainer = CompositeView(window, faderContRect);
		buttonContainer = CompositeView(window, buttonsContRect);

		divisionLabel = StaticText(buttonContainer, Rect(0,0,80,20)).string = "Beat Division";
		divisionBox = NumberBox(buttonContainer, Rect(100,0,30,20)).clipLo_(1).value_(4);
		numberLabel = StaticText(buttonContainer, Rect(0,30,80,20)).string = "Sub-Divisions";
		numberBox = NumberBox(buttonContainer, Rect(100,30,30,20)).clipLo_(1).value_(4);
		addButton = Button(buttonContainer, Rect(150,0,addButtonHeight,addButtonHeight));

		addButton.states_([["Add", Color.white, Color.black]]);
		addButton.action_({ 
			this.addRhythm(currentIndex, divisionBox.value.asInteger,numberBox.value.asInteger);
		});
					
		playButton = Button(buttonContainer, Rect(0,60,addButtonHeight,addButtonHeight));
		playButton.states_([["Play", Color.black, Color.green], ["Stop", Color.black, Color.yellow]]);
		playButtonRoutine = Routine {			
			inf.do {
				this.play;
				0.yield;
				this.stop;
				0.yield
			};
		};
		playButton.action_({ 
			playButtonRoutine.value();
		});		
		window.onClose_({this.cleanUp});
		window.front;
	}
	
	addGUIChannel {|index, division, number|
		var xPos;
		var container;
		var removeButton;
		var fader;
			
		xPos = index*channelWidth;
		if(xPos>=initialGUIWidth) {			
			[guiRect, faderContRect, buttonsContRect].do { |item, i|
				item.width_(xPos);
			};
			window.bounds = guiRect;
		};
		
		container = CompositeView(faderContainer, Rect(xPos, 0, channelWidth, faderContRect.height));
		fader = EZSlider(container, Rect(0, 0, channelWidth, faderHeight), "D: "++ division.asString ++ " N: "++ number, \db.asSpec.step_(0.01), initVal:1, unitWidth:channelWidth, numberWidth:channelWidth, layout:\vert);
		fader.action_({|ez| 
			var val = ez.value.dbamp;
			amps[index] = val;
		});

		faders.add(container);
		
		removeButton = Button(container, Rect(0,faderHeight, channelWidth, removeButtonHeight));
		removeButton.states_([["Remove", Color.black, Color.red]]);
		removeButton.action_({ 
			this.removeRhythm(index);
		});
		
		
	}
	
	removeGUIChannel {|index|
		var xPos;
		xPos = currentIndex*channelWidth;

		faders[index].remove;
		if(xPos<=initialGUIWidth) {			
			[guiRect, faderContRect, buttonsContRect].do { |item, i|
				item.width_(initialGUIWidth);
			};
			window.bounds = guiRect;
		};
		window.refresh;	
	}
	
	cleanUp {
		this.stop();
		rhythms = List[];
		freqs = List[];
		amps = List[];
		faders = List[];
	}
}


/*
	TODO 
	-shuffle along faders when previous removed (flowlayout?)
	-tempo input
	-When adding rhythms the default freq/MIDI value should be different to current values...
	-Limiter
	-Divided Gain levels
	-Create with 1-9 pre-made
	-on close functionality
	-margin to containers
	-make standalone
*/