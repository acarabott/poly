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
	var <>channelWidth;
	var <>channelHeight;
	var <>initialGUIWidth;
	var <>buttonHeight;
	
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
		Post << "INIT currentIndex: " <<  currentIndex << "\n"; 
		rhythms = List[];
		freqs = List[];
		amps = List[];
		tempo = 1;
		
		this.createGUI;
	}
	
	addRhythm {|aIndex, aDivision, aValue|
		var ret;
		var beat;

		freqs.add(440);
		amps.add(0.1);
		beat = (tempo/aDivision)*aValue;
		ret = Routine {
			inf.do {
				Synth(\polyBeep, [\freq, freqs[aIndex], \amp, amps[aIndex]]);
				beat.wait
			}
		};
		rhythms.add(ret);
		Post << "ADD RHYTHM currentIndex: " <<  currentIndex << "\n"; 
		
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
		
	}
	
	createGUI {
		var addButton;
		var labelBoxRect;
		var divisionLabel;
		var numberLabel;
		var divisionBox;
		var numberBox;
		var buttonFlowLayout;
		
		sBounds = Window.screenBounds;
		channelWidth = 50;
		channelHeight = 400;
		buttonHeight = 50;
		initialGUIWidth = channelWidth * 5;
		
		faderContRect = Rect.new(0,0, initialGUIWidth, channelHeight);
		buttonsContRect = Rect.new(0,channelHeight, initialGUIWidth, buttonHeight);
		guiRect = Rect.new(sBounds.width*0.33, sBounds.height*0.33, initialGUIWidth, channelHeight+(buttonHeight*2));
		window = Window.new("Poly", guiRect);
		faderContainer = CompositeView(window, faderContRect);
		buttonContainer = CompositeView(window, buttonsContRect);

		divisionLabel = StaticText(buttonContainer, Rect(0,0,80,20)).string = "Beat Division";
		divisionBox = NumberBox(buttonContainer, Rect(100,0,30,20)).clipLo_(1).value_(4);
		numberLabel = StaticText(buttonContainer, Rect(0,30,80,20)).string = "Sub-Divisions";
		numberBox = NumberBox(buttonContainer, Rect(100,30,30,20)).clipLo_(1).value_(4);
		addButton = Button(buttonContainer, Rect(150,0,buttonHeight,buttonHeight));

		addButton.states_([["Add", Color.white, Color.black]]);
		addButton.action_({ 
			this.addRhythm(currentIndex, divisionBox.value.asInteger,numberBox.value.asInteger);
		});
						
		window.front;
	}
	
	addGUIChannel {|index, division, number|
		var xPos;
		Post << "ADD CHANNEL currentIndex: " <<  currentIndex << "\n"; 
		Post << "index: " <<  index << "\n"; 
		
		xPos = index*channelWidth;
		if(xPos>=initialGUIWidth) {			
			[guiRect, faderContRect, buttonsContRect].do { |item, i|
				item.width_(xPos);
			};
			window.bounds = guiRect;
		};
		EZSlider(faderContainer, Rect(xPos, 0, channelWidth, channelHeight), "D: "++ division.asString ++ " N: "++ number, \db.asSpec.step_(0.01), initVal:1, unitWidth:25, numberWidth:25,layout:\vert);
	}
	
}


/*
	TODO 
	-When adding rhythms the default freq/MIDI value should be different to current values...
	-Limiter
	-Divided Gain levels
	-Create with 1-9 pre-made
	-on close functionality
	-Stop playing twice
	-make faders work
*/