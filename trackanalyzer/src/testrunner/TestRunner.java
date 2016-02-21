package testrunner;

import at.ofai.music.beatroot.BeatRoot;

import at.ofai.music.util.Event;
import at.ofai.music.util.EventList;


/**
 * Created by philhannant on 21/02/2016.
 */
public class TestRunner {

    private BeatRoot bt;
    private String filename;


    public static void main(String[] args){
        TestRunner tr = new TestRunner();
        tr.run();

    }

    public void run(){
        filename =  "/Users/philhannant/Desktop/Wavs/128.wav";
        bt = new BeatRoot();



        bt.getBPM(filename);

    }
}
