Poly {
	
	var <>tempo;
	var <>tempoList;
	
	var <>currentIndex;
	var <>rhythms;
	var <>freqs;
	var <>amps;
	var <>beatDivisions;
	var <>divisionsPerNote;
	
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
	var <>removeButtons;
	var <>tempoBox;
	
	
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
		removeButtons = List[];
		tempoList = List[];
		beatDivisions = List[];
		divisionsPerNote = List[];
		
		tempo = 60;
		
		this.createGUI;
	}
	
	createRhythm {|aIndex, aDivision, aValue|
		var ret;
		var beat;

		freqs.add(440);
		amps.add(1);
		beat = ((60/tempo)/aDivision)*aValue;
		ret = Routine {
			inf.do {
				Synth(\polyBeep, [\freq, freqs[aIndex], \amp, amps[aIndex]]);
				beat.wait
			}
		};
		
		^ret;		
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
		[faders, removeButtons, rhythms, amps, freqs, beatDivisions, divisionsPerNote, tempoList].do { |item, i|
			item.removeAt(index);
		};
		
	}
		
	createGUI {
		var addButton;
		var playButton;
		var stopButton;
		var labelBoxRect;
		var divisionLabel;
		var numberLabel;
		var tempoLabel;
		var divisionBox;
		var numberBox;
		var playButtonRoutine;
		
		sBounds = Window.screenBounds;
		faderHeight = 400;
		removeButtonHeight = 20;
		channelWidth = 80;
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
		divisionBox = NumberBox(buttonContainer, Rect(100,0,30,20)).clipLo_(1).value_(4).scroll_(false);
		numberLabel = StaticText(buttonContainer, Rect(0,30,80,20)).string = "Sub-Divisions";
		numberBox = NumberBox(buttonContainer, Rect(100,30,30,20)).clipLo_(1).value_(4).scroll_(false);
		tempoLabel = StaticText(buttonContainer, Rect(0,60,80,20)).string_("Tempo");
		tempoBox = NumberBox(buttonContainer, Rect(100,60,30,20)).clipLo_(1).value_(60).scroll_(false);
		
		addButton = Button(buttonContainer, Rect(150,0,addButtonHeight,addButtonHeight));

		addButton.states_([["Add", Color.white, Color.black]]);
		addButton.action_({
			tempo = tempoBox.value;
			tempoList.add(tempo);
			this.correctTempos();
			rhythms.add(
				this.createRhythm(currentIndex, divisionBox.value, numberBox.value);
			);
			beatDivisions.add(divisionBox.value);
			divisionsPerNote.add(numberBox.value);
			this.addGUIChannel(currentIndex, divisionBox.value, numberBox.value);
			currentIndex = currentIndex + 1;
		}); 
					
		playButton = Button(buttonContainer, Rect(150,60,addButtonHeight,addButtonHeight));
		playButton.states_([["Play", Color.black, Color.green], ["Stop", Color.black, Color.yellow]]);
		playButtonRoutine = Routine {			
			inf.do {
				tempo = tempoBox.value;
				this.correctTempos();
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
		var volFader;
		var freqFader;
			
		xPos = index*channelWidth;

		if(xPos>=initialGUIWidth) {	
			[guiRect, faderContRect, buttonsContRect].do { |item, i|
				item.width_(item.width+channelWidth);
			};
			window.bounds = guiRect;
		};
		
		container = CompositeView(faderContainer, Rect(xPos, 0, channelWidth, faderContRect.height));
		volFader = EZSlider(container, Rect(0, 0, channelWidth/2, faderHeight), "d:" ++ division.asString ++ " " ++ "n:" ++ number.asString, \db.asSpec.step_(0.01), initVal:1, unitWidth:channelWidth/2, numberWidth:channelWidth/2, layout:\vert);
		volFader.action_({|ez| 
			var val = ez.value.dbamp;
			amps[index] = val;
		});
		freqFader = EZSlider(container, Rect(channelWidth/2, 0, channelWidth/2, faderHeight), "Freq", \freq, initVal:440, unitWidth:channelWidth/2, numberWidth:channelWidth/2, layout:\vert);
		freqFader.action_({|ez| 
			freqs[index] = ez.value;
		});
		

		faders.add(container);
		
		removeButton = Button(container, Rect(0,faderHeight, channelWidth, removeButtonHeight));
		removeButton.states_([["Remove", Color.black, Color.red]]);
		this.setRemoveButtonAction(removeButton, index);
		removeButtons.add(removeButton);
		
		window.refresh;
	}
	
	removeGUIChannel {|index|
		var xPos;
		xPos = currentIndex*channelWidth;

		if(xPos<=initialGUIWidth) {			
			[guiRect, faderContRect, buttonsContRect].do { |item, i|
				item.width_(initialGUIWidth);
			};
			window.bounds = guiRect;
		};

		if(index!=(faders.size-1)) {
			this.shiftGUIChannels(index);
		};
		faders[index].remove;
		window.refresh;	
	}
	
	shiftGUIChannels {|index|
		var positions = List[];
		var addFunc;
		var changeFunc;
				
		
		faders[index..(faders.size-2)].do { |item, i|
			positions.add(item.bounds)
		};
		
		faders[(index+1)..(faders.size-1)].do { |item, i|
 			item.bounds = positions[i];
		};		
				
		removeButtons[(index+1)..(removeButtons.size-1)].do { |item, i|
			this.setRemoveButtonAction(item, removeButtons.indexOf(item)-1)	
		};
			
	}
	
	cleanUp {
		this.stop();
		rhythms = List[];
		freqs = List[];
		amps = List[];
		faders = List[];
	}
	
	setRemoveButtonAction {|item, newIndex|
				
		item.action_({
			this.removeGUIChannel(newIndex);
			this.removeRhythm(newIndex);
			currentIndex = currentIndex - 1;
		});
		
	}
	
	correctTempos {	
		var listAsSet = tempoList.asSet;
		if((tempoList.size==1) || (listAsSet.size>1) || ((listAsSet.size==1) && (listAsSet.includes(tempo).not))) {
			tempoList.do { |item, i|
				if(item!=tempo) {
					rhythms[i] = this.createRhythm(i, beatDivisions[i], divisionsPerNote[i]);
					tempoList[i] = tempo;
				};
			};
		};
	}

}


/*
	TODO;
	
	Functionality:
	-padding between faders
	-MIDI output
	-When adding rhythms the default freq/MIDI value should be different to current values...
	-Limiter
	-Divided Gain levels
	-Create with 1-9 pre-made
	-margin to containers
	
	Code:
	-channels as a class?
	-make standalone
	-comment, lol
	-create README
	-organise variables
	-rename divisions/numbers beatDivisions/divisionsPerNote 
	-using class variable for tempo..... bad skills man?...pass a value around.
*/