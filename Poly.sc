Poly {
	
	var <>tempo;
	var <>rhythms;
	var <>freqs;
	var <>amps;
	
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
	}
	
	addRhythm {|index, aDivison, aValue|
		var ret;
		var beat;

		freqs.add(440);
		amps.add(0.1);
		beat = (tempo/aDivison)*aValue;
		beat.postln;
		ret = Routine {
			inf.do {
				Synth(\polyBeep, [\freq, freqs[index], \amp, amps[index]]);
				beat.wait
			}
		};
		
		rhythms.add(ret);
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
		rhythms = List;
		freqs = List;
		amps = List;
	}
	
}


/*
	TODO 
	-When adding rhythms a default value should be different to current values...
	-Limiter
	-Divided Gain levels
*/