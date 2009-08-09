Poly {
	
	var <>tempo;

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
	var <>nextChannelX;
	
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
		rhythms = List[];
		freqs = List[];
		amps = List[];
		tempo = 1;
		
		this.createGUI;
	}
	
	addRhythm {|index, aDivision, aValue|
		var ret;
		var beat;

		freqs.add(440);
		amps.add(0.1);
		beat = (tempo/aDivision)*aValue;
		ret = Routine {
			inf.do {
				Synth(\polyBeep, [\freq, freqs[index], \amp, amps[index]]);
				beat.wait
			}
		};
		rhythms.add(ret);
		
		this.addGUIChannel(aDivision, aValue);
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
	
	clearAll {
		rhythms = List[];
		freqs = List[];
		amps = List[];
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
		nextChannelX = 0;
		
		faderContRect = Rect.new(0,0, initialGUIWidth, channelHeight);
		buttonsContRect = Rect.new(0,channelHeight, initialGUIWidth, buttonHeight);
		guiRect = Rect.new(sBounds.width*0.33, sBounds.height*0.33, initialGUIWidth, channelHeight+(buttonHeight*2));
		window = Window.new("Poly", guiRect);
		faderContainer = CompositeView(window, faderContRect);
		buttonContainer = CompositeView(window, buttonsContRect);
		buttonFlowLayout = buttonContainer.addFlowLayout;
		
		labelBoxRect = Rect(0,0,80,20);
		divisionLabel = StaticText(buttonContainer, labelBoxRect).string = "Beat Division";
		divisionBox = NumberBox(buttonContainer, labelBoxRect);
		numberLabel = StaticText(buttonContainer, labelBoxRect).string = "Sub-Divisions";
		numberBox = NumberBox(buttonContainer, labelBoxRect);
		buttonFlowLayout.nextLine;
		buttonFlowLayout.nextLine;
		addButton = Button(buttonContainer, Rect(0,0,buttonHeight,buttonHeight));
		addButton.states_([["Add", Color.white, Color.black]]);
		addButton.action_({ 
			this.addGUIChannel(4,3);
		});
						
		window.front;

	}
	
	addGUIChannel {|division, number|
		
		if(nextChannelX>=initialGUIWidth) {			
			[guiRect, faderContRect, buttonsContRect].do { |item, i|
				item.width_(item.width+channelWidth);
			};
			window.bounds = guiRect;
		};
		EZSlider(faderContainer, Rect(nextChannelX, 0, channelWidth, channelHeight), "D: "++ division.asString ++ " N: "++ number, \db.asSpec.step_(0.01), initVal:1, unitWidth:25, numberWidth:25,layout:\vert);
		this.nextChannelX = nextChannelX + channelWidth;		
	}
	
}


/*
	TODO 
	-When adding rhythms the default freq/MIDI value should be different to current values...
	-Limiter
	-Divided Gain levels
	-Create with 1-9 pre-made
*/