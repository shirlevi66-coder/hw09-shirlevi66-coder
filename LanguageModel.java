import java.util.HashMap;
import java.util.Random;

public class LanguageModel {

    // Maps windows to lists of character data objects
    HashMap<String, List> CharDataMap;

    // Window length
    int windowLength;

    // Random number generator
    private Random randomGenerator;

    /** Constructor with fixed seed */
    public LanguageModel(int windowLength, int seed) {
        this.windowLength = windowLength;
        randomGenerator = new Random(seed);
        CharDataMap = new HashMap<>();
    }

    /** Constructor with random seed */
    public LanguageModel(int windowLength) {
        this.windowLength = windowLength;
        randomGenerator = new Random();
        CharDataMap = new HashMap<>();
    }

    /** Builds a language model from the given file */
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

    /** Calculates p and cp for every CharData in list */
    void calculateProbabilities(List probs) {

        int total = 0;

        ListIterator it = probs.listIterator(0);
        while (it.hasNext()) {
            CharData cd = it.next();
            total += cd.count;
        }

        double cumulative = 0.0;

        it = probs.listIterator(0);
        while (it.hasNext()) {
            CharData cd = it.next();
            cd.p = (double) cd.count / total;
            cumulative += cd.p;
            cd.cp = cumulative;
        }
    }

    /** Returns random character based on probabilities */
    char getRandomChar(List probs) {

        double r = randomGenerator.nextDouble();

        ListIterator it = probs.listIterator(0);
        while (it.hasNext()) {
            CharData cd = it.next();
            if (r < cd.cp) {
                return cd.chr;
            }
        }

        return probs.get(probs.getSize() - 1).chr;
    }

    /** Generates random text */
    public String generate(String initialText, int textLength) {

        if (initialText.length() < windowLength) {
            return initialText;
        }

        StringBuilder result = new StringBuilder(initialText);
        int targetLength = initialText.length() + textLength;

        while (result.length() < targetLength) {

            String window = result.substring(result.length() - windowLength);
            List probs = CharDataMap.get(window);

            if (probs == null)
                break;

            char next = getRandomChar(probs);
            result.append(next);
        }

        return result.toString();
    }

    /** String representation of model */
    public String toString() {

        StringBuilder sb = new StringBuilder();

        for (String key : CharDataMap.keySet()) {
            sb.append(key + " : " + CharDataMap.get(key) + "\n");
        }

        return sb.toString();
    }

    /** MAIN לפי ההוראות */
    public static void main(String[] args) {

        int windowLength = Integer.parseInt(args[0]);
        String initialText = args[1];
        int generatedTextLength = Integer.parseInt(args[2]);
        boolean randomGeneration = args[3].equals("random");
        String fileName = args[4];

        LanguageModel lm;

        if (randomGeneration)
            lm = new LanguageModel(windowLength);
        else
            lm = new LanguageModel(windowLength, 20);

        lm.train(fileName);

        System.out.println(lm.generate(initialText, generatedTextLength));
    }
}

