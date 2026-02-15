import java.util.LinkedHashMap;
import java.util.Random;

public class LanguageModel {

    // The map of this model.
    // Maps windows to lists of charachter data objects.
    LinkedHashMap<String, List> CharDataMap;

    // The window length used in this model.
    int windowLength;

    // The random number generator used by this model.
    private Random randomGenerator;

    /** Constructs a language model with the given window length and a given
     *  seed value. Generating texts from this model multiple times with the
     *  same seed value will produce the same random texts. Good for debugging. */
    public LanguageModel(int windowLength, int seed) {
        this.windowLength = windowLength;
        randomGenerator = new Random(seed);
        CharDataMap = new LinkedHashMap<String, List>();
    }

    /** Constructs a language model with the given window length.
     * Generating texts from this model multiple times will produce
     * different random texts. Good for production. */
    public LanguageModel(int windowLength) {
        this.windowLength = windowLength;
        randomGenerator = new Random();
        CharDataMap = new LinkedHashMap<String, List>();
    }

    /** Builds a language model from the text in the given file (the corpus). */
    public void train(String fileName) {
        String text = "";
        try {
            java.nio.file.Path path = java.nio.file.Paths.get(fileName);
            text = new String(java.nio.file.Files.readAllBytes(path));
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        for (int i = 0; i + windowLength < text.length(); i++) {
            String window = text.substring(i, i + windowLength);
            char nextChar = text.charAt(i + windowLength);

            List probs = CharDataMap.get(window);
            if (probs == null) {
                probs = new List();
                CharDataMap.put(window, probs);
            }
            probs.update(nextChar);
        }

        for (List probs : CharDataMap.values()) {
            calculateProbabilities(probs);
        }
    }

    // Computes and sets the probabilities (p and cp fields) of all the
    // characters in the given list.
    void calculateProbabilities(List probs) {
        int total = 0;
        for (int i = 0; i < probs.getSize(); i++) {
            total += probs.get(i).count;
        }

        double cumulative = 0.0;
        for (int i = 0; i < probs.getSize(); i++) {
            CharData cd = probs.get(i);
            cd.p = (double) cd.count / total;
            cumulative += cd.p;
            cd.cp = cumulative;
        }

        if (probs.getSize() > 0) {
            probs.get(probs.getSize() - 1).cp = 1.0;
        }
    }

    // Returns a random character from the given probabilities list.
    char getRandomChar(List probs) {
        double r = randomGenerator.nextDouble();

        for (int i = 0; i < probs.getSize(); i++) {
            CharData cd = probs.get(i);
            if (r <= cd.cp) return cd.chr;
        }

        return probs.get(probs.getSize() - 1).chr;
    }

    public String generate(String initialText, int textLength) {
        if (initialText.length() < windowLength) return initialText;

        StringBuilder result = new StringBuilder(initialText);
        int limit = initialText.length() + textLength;

        while (result.length() < limit) {
            String window = result.substring(result.length() - windowLength);
            List probs = CharDataMap.get(window);
            if (probs == null) break;

            char next = getRandomChar(probs);
            result.append(next);
        }

        return result.toString();
    }

    /** Returns a string representing the map of this language model. */
    public String toString() {
        StringBuilder str = new StringBuilder();
        for (String key : CharDataMap.keySet()) {
            List keyProbs = CharDataMap.get(key);
            str.append(key + " : " + keyProbs + "\n");
        }
        return str.toString();
    }

    public static void main(String[] args) {
        // Your code goes here
    }
}
