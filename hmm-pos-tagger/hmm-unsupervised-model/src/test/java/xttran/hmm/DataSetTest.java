package xttran.hmm;

import org.junit.Test;

public class DataSetTest {
	
	private DataSet dataSet = DataSet.createDataset();
	
	@Test
	public void testLoadFromFile() {
		String filePath = "src/main/resources/data/test/acq/0009613";
		dataSet.loadFromFile(filePath);
		
		String[] states = new String[]{"1", "2", "3", "4", "5"};
		
		HMMModel model = HMMModel.createModel();
		HMMLearner learner = new HMMLearner();
		learner.train(model, states, dataSet.getBagOfWords(), dataSet.getSentences());
		
		System.out.println("final model");
		model.print();
		
		HMMDecoder decoder = new HMMDecoder(model);
		decoder.decode("demo/test3/input", "demo/test3/output");
	}
}
