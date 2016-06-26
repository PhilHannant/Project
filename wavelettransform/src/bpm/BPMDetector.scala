package bpm


trait BPMDetector {

  /**
    * Compute the beats per minute of an audio track
    * @return Track tempo in beats per minute
    **/
  def bpm() : Double;

}