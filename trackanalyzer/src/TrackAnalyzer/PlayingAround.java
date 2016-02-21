package TrackAnalyzer;

import it.sauronsoftware.jave.Encoder;
import it.sauronsoftware.jave.EncoderException;

import java.io.File;

/**
 * Created by philhannant on 15/02/2016.
 */
public class PlayingAround {

    Encoder en = new Encoder();

    public static void main(String[] args) throws EncoderException {
        PlayingAround pa = new PlayingAround();
        pa.run();
    }

    public void run() throws EncoderException {
    String fileName = "/Users/philhannant/Desktop/Wavs/bottle_120bpm.wav";
    File file = new File(fileName);
        try {
            System.out.println(file.getAbsolutePath());
            System.out.println(en.getInfo(file));
        } catch (EncoderException e) {
            e.printStackTrace();
        }
    }
}

