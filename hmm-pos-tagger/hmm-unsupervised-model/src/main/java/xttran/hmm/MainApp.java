package xttran.hmm;

public class MainApp {
	
	private static String[] generateStates(Integer numberOfStates) {
		if(numberOfStates == null || numberOfStates == 0) {
			throw new IllegalArgumentException("MainApp: number of states must be a positive number");
		}
		
		String[] states = new String[numberOfStates.intValue()];
		for(int i = 0; i < numberOfStates.intValue(); i++) {
			states[i] = "" + (i + 1);
		}
		return states;
	}

	public static void main(String[] args) {
		
		if (args.length < 3) {
			System.err.println("Missing argurments. Please set args: <model file> <input file> <output file>");
			System.exit(1);
		}

		HMMModel model = HMMModel.createModel();
		HMMLearner learner = new HMMLearner();
		DataSet dataSet = DataSet.createDataset();
		if("-tagger".equals(args[0])) {
			String trainData = args[1];
			String testData = args[2];
			String taggedData = args[3];
			Integer states = Integer.valueOf(args[4]);
			
			dataSet.loadFromFolder(trainData);
			
			learner.train(model, generateStates(states), dataSet.getBagOfWords(), dataSet.getSentences());
			model.saveToFile("HMM.tagger");
			HMMDecoder decoder = new HMMDecoder(model);
			decoder.decode(testData, taggedData);
			
		}	else if("-em".equals(args[0])) {
			String trainData = args[1]; 
			String modelOutput = args[2];
			Integer states = Integer.valueOf(args[3]);
			
			dataSet.loadFromFolder(trainData);
			learner.train(model, generateStates(states), dataSet.getBagOfWords(), dataSet.getSentences());
			model.saveToFile(modelOutput);
		} else if("-viterbi".equals(args[0])) {
			
			String modelInput = args[1];
			String inputFile = args[2];
			String outFile = args[3];
			
			model.loadFromFile(modelInput);
			dataSet.loadFromFolder(inputFile);
			
			HMMDecoder decoder = new HMMDecoder(model);
			decoder.decode(dataSet.getSentences(), outFile);
			System.out.println("Finish decoding");
			
		} else {
			System.err.println("Need specify algorithm!");
		}
		
		
		
	}
}
