package xttran.hmm;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class HMMDecoder {

    private HMMModel model;
    private Tag predict;

    public HMMDecoder(HMMModel model) {
        this.model = model;
        this.predict = new Tag(0, 0, "");
    }

    public void decode(String[][] sentences, String outputFile) {
        BufferedWriter writer = null;
        try {
            writer = new BufferedWriter(new FileWriter(outputFile));
            for (String[] observations : sentences) {
                // infer tag
                Tag tag = viterbi(observations);
                // write to output file
                if (tag == null) {
                    return;
                }
                writer.write(tag.toString(observations) + "\n");
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

    }

    public void decode(String inputFile, String outputFile) {
        BufferedReader reader = null;
        BufferedWriter writer = null;
        try {
            reader = new BufferedReader(new FileReader(inputFile));
            writer = new BufferedWriter(new FileWriter(outputFile));

            String line = null;
            List<String> sentences = new ArrayList<String>();
            while ((line = reader.readLine()) != null) {
                sentences.add(line.trim());
            }

            for (String sentence : sentences) {
                // word split
                String[] observations = sentence.split(" ");

                List<String> normalized = new ArrayList<String>();
                for (String obs : observations) {
                    if (obs.isEmpty()) {
                        continue;
                    }
                    normalized.add(obs.trim());
                }

                if (normalized.size() == 0) {
                    continue;
                }
                System.out.println(sentence);
                observations = normalized.toArray(new String[0]);

                // infer tag
                Tag tag = viterbi(observations);
                // write to output file
                if (tag == null) {
                    return;
                }
                writer.write(tag.toString(observations) + "\n");
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (writer != null)
                    writer.close();
                if (reader != null)
                    reader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    private Tag viterbi(String[] observations) {

        if (observations == null || observations.length == 0) {
            System.out.println("[WARN] empty input");
            return null;
        }

        // initialize start probability
        List<Tag> startTags = new ArrayList<Tag>();

        for (int i = 0; i < model.getNumOfStates(); i++) {
            String state = model.getStates()[i];
            double startProb = model.getStartProbs()[i];
            startTags.add(new Tag(startProb, startProb, state));
        }

        for (String observation : observations) {
            int index = model.getIndexOfWord(observation);
            if (index < 0) {
                System.err.println(observation + " doesn't exist in bag");
                index = 0;
                // continue;
            }

            List<Tag> currentTags = new ArrayList<Tag>();

            for (int v = 0; v < model.getNumOfStates(); v++) {
                String nextState = model.getStates()[v];

                double totalProb = 0;
                String path = null;
                double maxValue = 0;

                for (int u = 0; u < model.getNumOfStates(); u++) {

                    Tag tag = startTags.get(u);
                    double prob = tag.getProbability();
                    String currentPath = tag.getPath();
                    double tmpProb = tag.getTemp();

                    // follow formulation (18)
                    double p = model.getEmissionProbs()[u][index] * model.getTransitionProbs()[u][v];

                    prob *= p;
                    tmpProb *= p;
                    totalProb += prob;
                    if (tmpProb > maxValue) {
                        path = Tag.updatePath(currentPath, nextState);
                        maxValue = tmpProb;
                    }
                }
                currentTags.add(new Tag(totalProb, maxValue, path));
            }
            startTags = currentTags;
        }

        double totalProb = 0;
        String finalPath = null;
        double maxValue = 0;

        // compute final argmax
        for (int i = 0; i < model.getNumOfStates(); i++) {
            Tag tag = startTags.get(i);

            double prob = tag.getProbability();
            String path = tag.getPath();
            double tmpProb = tag.getTemp();
            totalProb += prob;
            if (tmpProb > maxValue) {
                finalPath = path;
                maxValue = tmpProb;
            }
        }
        return new Tag(totalProb, maxValue, finalPath);
    }

    public Tag getPredict() {
        return predict;
    }
}
