import sun.plugin2.jvm.CircularByteBuffer;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.TargetDataLine;

public class AudioTest {

    private static AudioFormat format = new AudioFormat(8000.0f, 16, 1, true, true);
    private static TargetDataLine microphone;
    private static AudioInputStream audioInputStream;
    private static SourceDataLine sourceDataLine;


    public static void main(String[] args) {


        try {
            microphone = AudioSystem.getTargetDataLine(format);

            DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);
            microphone = (TargetDataLine) AudioSystem.getLine(info);
            microphone.open(format);

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            int numBytesRead;
            int CHUNK_SIZE = 1024;
            byte[] data = new byte[microphone.getBufferSize() / 5];
            microphone.start();

            int bytesRead = 0;
            int x = 1;
            try {
                while (bytesRead < 10000) { // Just so I can test if recording
                    // my mic works...
                    numBytesRead = microphone.read(data, 0, CHUNK_SIZE);
                    bytesRead = bytesRead + numBytesRead;
                    System.out.println(bytesRead);
                    out.write(data, 0, numBytesRead);
                    byte audioData[] = out.toByteArray();
                    System.out.println("STARTING THREAD");
                    Thread thread = new Thread(new AudioPlayer(microphone, audioInputStream, sourceDataLine, audioData));
                    threader(thread, x);
                    x++;

                }
            } catch (Exception e) {
                e.printStackTrace();
            }

//
//            byte audioData[] = out.toByteArray();



            // Get an input stream on the byte array
            // containing the data

            // Block and wait for internal buffer of the
            // data line to empty.
//            sourceDataLine.drain();
//            sourceDataLine.close();
//            microphone.close();
        } catch (LineUnavailableException e) {
            e.printStackTrace();
        }

    }

    public static synchronized void threader(Thread thread, int x){
        System.out.println(x);
        thread.start();
    }

}