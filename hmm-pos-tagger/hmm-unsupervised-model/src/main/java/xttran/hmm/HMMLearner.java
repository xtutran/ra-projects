package xttran.hmm;

import xttran.hmm.util.HMMUtil;

public class HMMLearner {

    public static final int MAX_INTERATIONS = 100;
    public static final double MIN_ERROR = 0.001;

    public void train(HMMModel model, String[] states, String[] bagOfWords, String[][] trainSentences) {
        if (model == null) {
            model = HMMModel.createModel();
        }

        int trainingSize = trainSentences.length;
        double[][] alpha;
        double[][] beta;
        double[] gamma;
        String[] sentence;
        int sentenceSize;

        model.initialize(states, bagOfWords);

        for (int s = 0; s < MAX_INTERATIONS; s++) {
            System.out.println("Step : " + (s + 1));
            model.print();

            double pi[] = new double[states.length];
            double a[][] = new double[states.length][states.length];
            double b[][] = new double[states.length][bagOfWords.length];

            double likelihood = forwardBackward(model, trainSentences);
            double nextLikelihood = 1.0;
            double diff = 1.0;

            // E-step
            for (int i = 0; i < trainingSize; i++) {
                sentence = trainSentences[i];

                sentenceSize = sentence.length;

                // calculation of Forward-Backward probabilities from the current model
                alpha = computeForward(model, sentence);
                beta = computeBackward(model, sentence);
                gamma = computeGama(alpha, beta);

                // re-estimation of initial state probabilities
                for (int p = 0; p < model.getNumOfStates(); p++) {
                    pi[p] += gamma[p];
                }

                // re-estimation of transition probabilities
                for (int p = 0; p < model.getNumOfStates(); p++) {
                    for (int q = 0; q < model.getNumOfStates(); q++) {
                        double sum = 0;
                        for (int j = 0; j < sentenceSize; j++) {

                            if (j == sentenceSize - 1) {
                                sum += alpha[j][p] * model.getTransitionProbs()[p][q];
                            } else {
                                int oj = model.getIndexOfWord(sentence[j]);
                                sum += alpha[j][p] * model.getTransitionProbs()[p][q] * model.getEmissionProbs()[p][oj]
                                        * beta[j + 1][q];
                            }
                        }
                        a[p][q] += HMMUtil.divide(sum, gamma[p]);
                    }
                }

                // re-estimation of emission probabilities
                for (int p = 0; p < model.getNumOfStates(); p++) {
                    for (int j = 0; j < sentenceSize; j++) {
                        int oj = model.getIndexOfWord(sentence[j]);
                        b[p][oj] += HMMUtil.divide(alpha[j][p] * beta[j][p], gamma[p]);
                    }
                }
            }

            // M-step
            double piSum = 0;
            for (int k = 0; k < model.getNumOfStates(); k++) {
                piSum += pi[k];
            }

            for (int u = 0; u < model.getNumOfStates(); u++) {

                double aSum = 0;
                for (int k = 0; k < model.getNumOfStates(); k++) {
                    aSum += a[u][k];
                }

                model.getStartProbs()[u] = pi[u] / piSum;

                for (int k = 0; k < model.getNumOfStates(); k++) {
                    model.getTransitionProbs()[u][k] = a[u][k] / aSum;
                }

                double bSum = 0;
                for (int i = 0; i < model.getNumOfWords(); i++) {
                    bSum += b[u][i];
                }

                for (int i = 0; i < model.getNumOfWords(); i++) {
                    model.getEmissionProbs()[u][i] = b[u][i] / bSum;
                }
            }

            nextLikelihood = forwardBackward(model, trainSentences);
            diff = Math.abs(nextLikelihood - likelihood);
            if (diff <= MIN_ERROR) {
                break;
            }
        }
    }

    public double[] computeGama(double[][] anpha, double[][] beta) {
        int N = anpha.length;
        int W = anpha[0].length;

        double[] prob = new double[W];
        for (int j = 0; j < W; j++) {
            double sum = 0.0;
            for (int p = 0; p < N; p++) {
                sum += anpha[p][j] * beta[p][j];
            }
            prob[j] = sum;
        }
        return prob;
    }

    private double sumAlpha(double[][] alpha, int k) {
        double sum = 0.0;
        if (k > alpha.length - 1) {
            return sum;
        }

        for (int i = 0; i < alpha[0].length; i++) {
            sum += alpha[k][i];
        }
        return sum;
    }

    public double forwardBackward(HMMModel model, String[][] trainData) {
        int length = trainData.length;

        double likeLiHood = 0.0;
        for (int i = 0; i < length; i++) {
            String[] sentence = trainData[i];
            double[][] alpha = computeForward(model, sentence);
            likeLiHood += sumAlpha(alpha, sentence.length - 1);
        }
        return likeLiHood;
    }

    public double[][] computeForward(HMMModel model, String[] sentence) {

        int N = model.getNumOfStates();
        int W = sentence.length;

        double[][] alpha = new double[W][N];

        // Base case
        for (int p = 0; p < N; p++) {
            alpha[0][p] = model.getStartProbs()[p];
        }

        // Recursive case
        for (int j = 0; j < W - 1; j++) {

            int xj = model.getIndexOfWord(sentence[j + 1]);

            for (int p = 0; p < model.getNumOfStates(); p++) {
                double sum = 0.0;
                for (int q = 0; q < model.getNumOfStates(); q++) {
                    sum += alpha[j][q] * model.getTransitionProbs()[p][q] * model.getEmissionProbs()[q][xj];
                }
                alpha[j + 1][p] = sum;
            }
        }

        return alpha;

    }

    public double[][] computeBackward(HMMModel model, String[] sentence) {

        int N = model.getNumOfStates();
        int W = sentence.length;

        double[][] beta = new double[W][N];

        int xn = model.getIndexOfWord(sentence[W - 1]);

        // Base case
        for (int p = 0; p < model.getNumOfStates(); p++) {
            beta[W - 1][p] = model.getTransitionProbs()[p][N - 1] * model.getEmissionProbs()[p][xn];
        }

        // Recursive case
        for (int j = W - 2; j >= 0; j--) {

            int xj = model.getIndexOfWord(sentence[j]);
            // int xk = model.getIndexOfWord(trainingSet[j - 1]);

            for (int p = 0; p < model.getNumOfStates(); p++) {
                double sum = 0.0;
                for (int q = 0; q < model.getNumOfStates(); q++) {
                    sum += model.getTransitionProbs()[p][q] * model.getEmissionProbs()[p][xj] * beta[j + 1][q];
                }
                beta[j][p] = sum;
            }
        }
        return beta;
    }
}
