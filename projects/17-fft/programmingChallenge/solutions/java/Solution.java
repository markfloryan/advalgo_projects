import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Executable;
import java.util.*;
import java.util.stream.Collectors;
import org.apache.commons.math3.complex.*;
import java.util.Dictionary;
import java.lang.Math;

public class Solution {

    static String[] noteNames = {"C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B"};
    static double[] noteFreqs = {261.626, 277.183, 293.665, 311.127, 329.628, 349.228,
            369.994, 391.995, 415.305, 440.000, 466.164, 493.883};

    static double minCompFreq = 255.0;
    static double maxCompFreq = 505.0;

    static double minThird = Math.pow(2.0, 3.0/12.0);
    static double majThird = Math.pow(2.0, 4.0/12.0);

    static Complex[] fft(Complex[] in) {
        // NOTE: in must be an even length
        int n = in.length;
        // Base case: in contains just one element -> the result of the transform is the element itself
        if (n == 1) {
            return in;
        }

        // Split array into odd and even indices for divide and conquer
        int half_n = n / 2;
        // Init arrays for storing values at the odd and even indices
        Complex[] odds = new Complex[half_n];
        Complex[] evens = new Complex[half_n];
        // Counters to keep track of the cur index in odds and evens
        int odds_i = 0;
        int evens_i = 0;
        // Split in by iterating over all values, and storing into odds or evens depending on cur index
        for (int i = 0; i < n; i++) {
            if (i % 2 == 0) {
                evens[evens_i] = in[i];
                evens_i++;
            }
            else {
                odds[odds_i] = in[i];
                odds_i++;
            }
        }

        // Recursively calculate the DFT on odds and evens separately
        odds = fft(odds);
        evens = fft(evens);

        // Merge the resultant transforms of odds and evens using trigonometric constant coefficients ("twiddle factors")
        // Init array for saving the results
        Complex[] ret = new Complex[n];
        for (int i = 0; i < half_n; i++) {
            // Calc. twiddle factor: exp(-2im*pi*i/n)
            Complex factor = new Complex(0, (-2.0 * Math.PI * i) / n);
            factor = factor.exp().multiply(odds[i]);
            // Front half of the result: evens_i + exp(-2im*pi*i/n) * odds_i
            ret[i] = evens[i].add(factor);
            // Back half of the result: evens_i - exp(-2im*pi*i/n) * odds_i
            ret[i + half_n] = evens[i].subtract(factor);
        }
        return ret;
    }

    static boolean isPowerOfTwo(int n) {
        return (n > 0) && ((n & (n-1)) == 0);
    }

    // Find indices of peaks in FFT output.
    static int[] fftFindPeaks(double[] signal, int threshold) {
        int[] peaks = new int[3];
        int index = 0;
        for (int i = 1; i < signal.length - 1; i++) {
            // Add peak if is local maxima (higher than neighbors) and greater than threshold value.
            if (signal[i] > signal[i - 1] && signal[i] > signal[i + 1] && signal[i] > threshold) {
                peaks[index] = i;
                index++;
            }
        }
        return peaks;
    }

    // Calculate the corresponding frequency bins for indices of FFT output.
    static double[] getFreqBins(int numSamples, double sampleInterval) {
        double[] freqBins = new double[numSamples / 2];
        for (int i = 0; i < numSamples/2; i++) {
            freqBins[i] = i / (numSamples * sampleInterval);
        }
        return freqBins;
    }

    // Given a frequency, find the frequency closest to it in the frequency list. To be used for note name lookup.
    static double findClosest(double freq) {
        // Multiplying or dividing frequency by 2 does not change the note name, so we only need to store one octave.
        while (freq < minCompFreq) {
            freq *= 2;
        }
        while (freq > maxCompFreq) {
            freq /= 2;
        }
        // Find the closest frequency in list and return.
        double closestFreq = noteFreqs[0];
        double minDist = Math.abs(noteFreqs[0] - freq);
        for (int i = 0; i < noteFreqs.length; i++) {
            double dist = Math.abs(noteFreqs[i] - freq);
            if (dist < minDist) {
                minDist = dist;
                closestFreq = noteFreqs[i];
            }
        }
        return closestFreq;
    }

    // For the frequencies returned by the FFT, find the closest values in list for lookup in dict.
    static double[] getExactFreqs(double[] frequencies) {
        double[] exactFrequencies = new double[frequencies.length];
        for(int i = 0; i < frequencies.length; i++) {
            exactFrequencies[i] = findClosest(frequencies[i]);
        }
        return exactFrequencies;
    }

    //Given an exact frequency for an note, return its name.
    static String getNoteName(double freq) {
        for(int i = 0; i < noteFreqs.length; i++) {
            if(noteFreqs[i] == freq) {
                return noteNames[i];
            }
        }
        return "n/a";
    }

    //Get multiple note names.
    static String[] getNoteNames(double[] noteFreqs) {
        int index = 0;
        String[] names = new String[noteFreqs.length];
        for(double f : noteFreqs) {
            names[index] = getNoteName(f);
            index++;
        }
        return names;
    }

    // Bulk of code for chord determination. Determines if a given chord is in root position. If it is, return the chord
    // root and quality, e.g. C maj.
    static String[] isRootPos(double[] frequencies) {
        double tolerance = 0.03;
        // Calculate the intervals between notes using frequency ratios.
        double firstInterval = frequencies[1] / frequencies[0];
        double secondInterval = frequencies[2] / frequencies[1];
        // Boolean values for interval type. Checks if difference between observed and true interval is within tolerance.
        boolean int1IsMinThird = Math.abs(firstInterval - minThird) < tolerance;
        boolean int2IsMinThird = Math.abs(secondInterval - minThird) < tolerance;
        boolean int1IsMajThird = Math.abs(firstInterval - majThird) < tolerance;
        boolean int2IsMajThird = Math.abs(secondInterval - majThird) < tolerance;
        // Chord is in root position if both intervals are a type of third.
        boolean isRootPos = (int1IsMajThird || int1IsMinThird) && (int2IsMajThird || int2IsMinThird);
        if (isRootPos) {
            if (int1IsMajThird && int2IsMinThird) {
                return new String[] {"T", "maj"};
            }
            if (int1IsMinThird && int2IsMajThird) {
                return new String[] {"T", "min"};
            }
            if (int1IsMinThird && int2IsMinThird) {
                return new String[] {"T", "dim"};
            }
        }
        return new String[] {"F", "n/a"};
    }

    // Repeatedly move the lowest frequency note up by an octave until chord is in root position.
    static double[] getRootPosChord(double[] frequencies) {
        // Want to get chord into root position so that we can determine its quality (maj/min/dim).
        while (!isRootPos(frequencies)[0].equals("T")) {
            // Double lowest frequency, preserves note name and chord quality but changes intervals.
            frequencies[0] *= 2;
            // Rearrange notes in frequency order after doubling lowest.
            Arrays.sort(frequencies);
        }
        return frequencies;
    }

    // Given bass (lowest) note of a chord and the notes in root position, determine the chord inversion.
    // Root in bass = root pos (0); third in bass = 1st inv. (1); fifth in bass = 2nd inv. (2)
    static int getChordInversion(String bassNote, String[] rootPosNotes) {
        for (int i = 0; i < rootPosNotes.length; i++) {
            if (rootPosNotes[i].equals(bassNote)) {
                return i;
            }
        }
        return -1;
    }

    public static void main(String[] args) {
        try {
            // Read in parameters.
            BufferedReader r = new BufferedReader(new InputStreamReader(System.in));
            String[] params = r.readLine().split(" ");
            if (params.length != 3) {
                throw new Exception("First line should have 3 integer arguments");
            }
            int n = Integer.parseInt(params[0]); // Number of waveforms
            int m = Integer.parseInt(params[1]); // Number of samples per waveform
            double t = Double.parseDouble(params[2]); // Duration of each waveform in seconds
            if (!isPowerOfTwo(m)) {
                throw new Exception("Second argument needs to be power of 2");
            }
            for (int i = 0; i < n; i++) {
                String[] sData = r.readLine().split(" ");
                if (sData.length != m) {
                    throw new Exception("Line " + i + " should have " + m + " arguments");
                }
                Complex[] nData = new Complex[m];
                for (int j = 0; j < m; j++) {
                    nData[j] = new Complex(Double.parseDouble(sData[j]));
                }

                // Compute FFT of waveform.
                Complex[] fftResult = fft(nData);

                // Get magnitude of complex results. Only first half of output represents real frequencies.
                double[] posResult = new double[fftResult.length / 2];
                for (int j = 0; j < fftResult.length / 2; j++) {
                    posResult[j] = fftResult[j].abs();
                }

                // Get corresponding frequency bins.
                double[] freqBins = getFreqBins(m, t/m);

                // Find peaks in FFT output.
                int[] peakIndices = fftFindPeaks(posResult, 100);
                double[] peakFreqs = new double[peakIndices.length];
                for(int j = 0; j < peakIndices.length; j++) {
                    peakFreqs[j] = freqBins[peakIndices[j]];
                }

                // ----------------------------------------------------
                // Above is fft code, below is chord determination code
                // ----------------------------------------------------

                // Correct to exact frequencies to account for frequency bin variation.
                double[] exactFrequencies = getExactFreqs(peakFreqs);
                // Get note names. Will be in ascending order of frequency.
                String[] notes = getNoteNames(exactFrequencies);

                // Get chord root and quality.
                double[] rootPosChord = getRootPosChord(exactFrequencies);
                double rootFreq = findClosest(rootPosChord[0]);
                String rootNote = getNoteName(rootFreq);
                String chordType = isRootPos(rootPosChord)[1];

                // Get notes rearranged into root position, then determine chord inversion.
                double[] rootPosExactFreqs = getExactFreqs(rootPosChord);
                String[] rootPosNotes = getNoteNames(rootPosExactFreqs);
                // Use base note and root position notes.
                int inversion = getChordInversion(notes[0], rootPosNotes);

                // Print results.
                for (String note : notes) {
                    System.out.printf("%s ", note);
                }
                System.out.printf("\n%s %s %d\n", rootNote, chordType, inversion);



            }
        } catch (Exception e) {
            System.err.println(e.getMessage());
            System.exit(-1);
        }
    }
}
