import javax.sound.sampled.*;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by philhannant on 23/06/2016.
 */
public class AudioPlayer implements Runnable {

    private static AudioFormat format = new AudioFormat(8000.0f, 16, 1, true, true);
    private TargetDataLine microphone;
    private AudioInputStream audioInputStream;
    private SourceDataLine sourceDataLine;
    private byte[] audioData;

    public AudioPlayer(TargetDataLine mic, AudioInputStream inStream, SourceDataLine sourceLine, byte[] audio){
        this.microphone = mic;
        this.audioInputStream = inStream;
        this.sourceDataLine = sourceLine;
        this.audioData = audio;
    }

    @Override
    public void run() {
        try {
            InputStream byteArrayInputStream = new ByteArrayInputStream(
                    audioData);
            audioInputStream = new AudioInputStream(byteArrayInputStream, format, audioData.length / format.getFrameSize());
            DataLine.Info dataLineInfo = new DataLine.Info(SourceDataLine.class, format);
            sourceDataLine = (SourceDataLine) AudioSystem.getLine(dataLineInfo);
            sourceDataLine.open(format);
            sourceDataLine.start();
            int cnt = 0;
            byte tempBuffer[] = new byte[10000];
            try {
                while ((cnt = audioInputStream.read(tempBuffer, 0, tempBuffer.length)) != -1) {
                    if (cnt > 0) {
                        // Write data to the internal buffer of
                        // the data line where it will be
                        // delivered to the speaker.
                        sourceDataLine.write(tempBuffer, 0, cnt);
                    }// end if
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (LineUnavailableException ex) {
            ex.printStackTrace();
        }

    }

}
