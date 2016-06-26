package bpm

import jwave._
import jwave.transforms._
import jwave.transforms.wavelets.daubechies._
import jwave.transforms.wavelets.haar._

import scala.collection.mutable.ArrayBuffer
import scala.math.{abs, min, pow}

import arrays.ArrayOperations.SciArray


class WaveletBPMDetector private (
                                   private val audioFile : AudioFile,
                                   private val windowFrames : Int,
                                   private val waveletType : WaveletBPMDetector.Wavelet,
                                   private val windowsToProcess : Int) extends BPMDetector {

  val wavelet = waveletType match {
    case WaveletBPMDetector.Haar => new Haar1()
    case WaveletBPMDetector.Daubechies4 => new Daubechies4()
  }

  /*
   * Array of BPM values computed for each window in the track.
   * Overall BPM is computed as the median of this collection
   **/
  private var instantBpm = ArrayBuffer[Double]()

  /**
    * The tempo in beats-per-minute computed for the track
    **/
  var _bpm : Double = -1.0;


  private def detectPeak(data : Array[Double]) : Int = {
    var max: Double = Double.MinValue

    for (x <- data) {
      if (abs(x) > max) max = abs(x)
    }
    var location = -1;
    var i = 0
    while (i < data.length && location == -1) {
      if (data(i) == max) {
        location = i;
      }
      i = i + 1;
    }
    i = 0;
    while (i < data.length && location == -1) {
      if (data(i) == -max) {
        location = i;
      }
      i = i + 1;
    }
    return location
  }


  private def computeWindowBpm(data : Array[Double]) {

    var aC : Array[Double] = null
    var dC : Array[Double] = null
    var dCSum : Array[Double] = null
    var dCMinLength : Int = 0
    val levels = 4
    val maxDecimation = pow(2, levels-1)
    val minIndex : Int = (60.toDouble / 220 * audioFile.sampleRate.toDouble/maxDecimation).toInt
    val maxIndex : Int = (60.toDouble / 40 * audioFile.sampleRate.toDouble/maxDecimation).toInt

    // 4 Level DWT
    for (loop <- 0 until levels) {

      // Apply DWT
      val transform = new Transform(new FastWaveletTransform(wavelet));
      if (loop == 0) {
        val coefficients : Array[Array[Double]] = transform.decompose(data)
        val l = coefficients.length - 1
        aC = coefficients(1).slice(0, coefficients(1).length/2)
        dC = coefficients(l).slice(coefficients(l).length/2, coefficients(l).length)
        dCMinLength = (dC.length/maxDecimation).toInt + 1
      } else {
        val coefficients : Array[Array[Double]] = transform.decompose(aC)
        val l = coefficients.length - 1
        aC = coefficients(1).slice(0, coefficients(1).length/2)
        dC = coefficients(l).slice(coefficients(l).length/2, coefficients(l).length)
      }

      // Extract envelope from detail coefficients
      //  1) Undersample
      //  2) Absolute value
      //  3) Subtract mean
      val pace = pow(2, (levels-loop-1)).toInt
      dC = dC.undersample(pace).abs
      dC = dC - dC.mean

      // Recombine detail coeffients
      if (dCSum == null) {
        dCSum = dC.slice(0, dCMinLength)
      } else {
        dCSum = dC.slice(0, min(dCMinLength, dC.length)) |+| dCSum
      }
    }

    // Add the last approximated data
    aC = aC.abs
    aC = aC - aC.mean
    dCSum = aC.slice(0, min(dCMinLength, dC.length)) |+| dCSum

    // Autocorrelation
    var correlated : Array[Double] = dCSum.correlate
    val correlatedTmp = correlated.slice(minIndex, maxIndex)


    // Detect peak in correlated data
    val location = detectPeak(correlatedTmp)

    // Compute window BPM given the peak
    val realLocation = minIndex + location
    val windowBpm : Double = 60.toDouble / realLocation * (audioFile.sampleRate.toDouble/maxDecimation)
    println(windowBpm)
    instantBpm += windowBpm;
  }

  override def bpm() : Double = {
    if (_bpm == -1) {
      for (currentWindow <- 0 until windowsToProcess) {
        val buffer : Array[Int]  = new Array[Int](windowFrames * audioFile.numChannels);
        val framesRead = audioFile.readFrames(buffer, windowFrames);
        val leftChannelSamples : Array[Double] =
          buffer.zipWithIndex.filter(_._2 % 2 == 0).map(_._1.toDouble)
        computeWindowBpm(leftChannelSamples)
      }
      _bpm = instantBpm.toArray.median()

    }
    return _bpm
  }

}

object WaveletBPMDetector {

  /**
    * Trait for wavelet types
    **/
  sealed trait Wavelet

  /**
    * Haar wavelet type
    **/
  case object Haar extends Wavelet

  /**
    * Daubechies4 wavelet type
    **/
  case object Daubechies4 extends Wavelet

  class WindowSizeException(message: String = null, cause: Throwable = null)
    extends RuntimeException(message, cause);

  /**
    * Create a <code>WaveletBPMDetector</code> object given an audio file,
    * a size of the window in frames and a wavelet type
    * @param audioFile Audio file
    * @param windowFrames Number of frames that form a window on which the beat detection
    * algorithm is applied. Due to the way DWT is applied <code>windowFrames</code>
    * must be a power of two. <code>windowFrames</code> should be such that a window
    * corresponds to 1 to 10 seconds of the audio file.
    * @param waveletType The type of wavelet. Default value is <code>Daubechies4</code>
    * @param windowsToConsider The number of windows to process. If not specified
    * the whole track is processed
    * @return A object of the class <code>WaveletBPMDetector</code>
    * @throws WindowSizeException
    **/
  def apply(
             audioFile : AudioFile,
             windowFrames : Int,
             waveletType : Wavelet = Daubechies4,
             windowsToConsider : Int = 0) : WaveletBPMDetector = {

    if ((windowFrames < 0) || !((windowFrames & (windowFrames - 1)) == 0))
      throw new WindowSizeException("windowFrames parameter must be a power of 2")

    var windowsNumber = windowsToConsider
    if (windowsNumber <= 0) {
      windowsNumber = (audioFile.numFrames / windowFrames).toInt
    }

    return new WaveletBPMDetector(audioFile, windowFrames, waveletType, windowsNumber)
  }

}



object test extends App{
  val audioFile = WavFile("/Users/philhannant/Desktop/Wavs/EastBay139.wav")
  val tempo = WaveletBPMDetector(
    audioFile,
    131072,
    WaveletBPMDetector.Haar).bpm
  println(tempo)
}