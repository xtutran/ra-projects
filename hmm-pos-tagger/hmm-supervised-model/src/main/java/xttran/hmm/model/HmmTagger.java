package xttran.hmm.model;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.util.*;

public class HmmTagger {

    public static final String WORD_TAG_SEP = "/";
    public static final String SEQUENCE_SEP = " ";
    public static final String ARROW = "->";
    public static final String START_STATE = "start";
    public static final String END_STATE = "end";

    public static void main(String[] args) throws IOException {
        String dataFile = "src/main/resources/Trainset-POS-1";
        HmmModel hmm = new HmmModel();
        hmm.train(dataFile);

        System.out.println(JsonUtil.serialize(hmm));

        HmmTagger tagger = new HmmTagger();
        tagger.tag(hmm, "src/main/resources/test/25272.pos", "src/main/resources/test/25272.output");
        // tagger.viterbi(hmm, "Đó/P là/V con/Nc đường/N biển/N ngắn/A nhất/A để/E đi/V
        // từ/E Ấn_Độ_Dương/Np sang/V Thái_Bình_Dương/Np ,/CH chiếm/V đến/E lượng/N
        // hàng_hóa/N lưu_thông/V đường_biển/N của/E thế_giới/N ,/CH đó/P là/V
        // hải_trình/N lớn/A nhất/R từ/E tây/N sang/E đông/N với/E 50.000/M lượt/Nc
        // tàu_bè/N qua_lại/V mỗi/L năm/N .../CH");
    }

    public void tag(HmmModel model, String inputFile, String outputFile) throws IOException {

        InputStream input = new FileInputStream(inputFile);
        OutputStream output = new FileOutputStream(outputFile);

        List<String> predictedLine = new ArrayList<String>();
        for (Object line : IOUtils.readLines(input, "UTF-8")) {
            String lineData = (String) line;
            if (lineData.isEmpty()) {
                continue;
            }

            predictedLine.add(viterbi(model, lineData));
        }

        IOUtils.write(StringUtils.join(predictedLine.toArray(new String[0]), "\n"), output, "UTF-8");

        input.close();
        output.close();
    }

    public String viterbi(HmmModel model, String lineData) {

        if (model == null || model.isEmpty()) {
            System.err.println("Hmm Model is empty! Need to be trained after tagger");
            return null;
        }

        Map<String, Double> best_score = new HashMap<String, Double>(); // start with <s>
        Map<String, String> best_edge = new HashMap<String, String>();

        List<String> words = new ArrayList<String>();

        int i = 0;
        final String startState = i + ARROW + START_STATE;
        best_score.put(startState, 0d);
        best_edge.put(startState, null);

        Set<String> possibleTags = model.getContext().getVocabulary().keySet();

        // forward step
        String[] sequences = StringUtils.split(lineData, SEQUENCE_SEP);
        for (i = 0; i < sequences.length; i++) {
            String sequence = sequences[i];
            String[] wordtags = StringUtils.split(sequence, WORD_TAG_SEP);
            words.add(wordtags[0]);

            for (String prev : possibleTags) {
                for (String next : possibleTags) {

                    String currentState = i + ARROW + prev;
                    String nextState = (i + 1) + ARROW + next;

                    Double currentScore = best_score.get(currentState);
                    if (currentScore != null && model.getTransition().containWord(prev + ARROW + next)) {
                        double score = currentScore + model.calcTransitionProb(prev, next)
                                + model.calcEmissionProb(next, wordtags[0]);
                        Double nextScore = best_score.get(nextState);
                        if (nextScore == null || nextScore.doubleValue() > score) {
                            best_score.put(nextState, score);
                            best_edge.put(nextState, currentState);
                        }
                    }
                }
            }
        }

        for (String prev : possibleTags) {

            String currentState = i + ARROW + prev;
            String nextState = (i + 1) + ARROW + END_STATE;

            Double currentScore = best_score.get(currentState);
            if (currentScore != null && model.getTransition().containWord(prev + ARROW + END_STATE)) {
                double score = currentScore + model.calcTransitionProb(prev, END_STATE);
                Double nextScore = best_score.get(nextState);
                if (nextScore == null || nextScore.doubleValue() > score) {
                    best_score.put(nextState, score);
                    best_edge.put(nextState, currentState);
                }
            }
        }

        // System.out.println(JsonUtil.serialize(best_score));
        // System.out.println(JsonUtil.serialize(best_edge));

        // backward steps
        List<String> tags = new ArrayList<String>();
        String next_edge = best_edge.get((sequences.length + 1) + ARROW + END_STATE);
        while (!next_edge.equals(startState)) {
            String[] positiontags = StringUtils.split(next_edge, ARROW);
            tags.add(positiontags[1]);
            next_edge = best_edge.get(next_edge);
        }
        Collections.reverse(tags);

        List<String> wordTags = new ArrayList<String>();
        for (i = 0; i < words.size(); i++) {
            String word = words.get(i);
            String tag = tags.get(i);

            wordTags.add(word + WORD_TAG_SEP + tag);
        }
        return StringUtils.join(wordTags, SEQUENCE_SEP);
    }

}
