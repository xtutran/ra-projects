package xttran.hmm;
import org.junit.Test;

import xttran.hmm.HMMModel;
import xttran.hmm.util.HMMUtil;


public class HMMModelTest {
	
	@Test
	public void testInitialize() {
		//test case 1
		String[] states = new String[]{"Healthy", "Fever"};
		String[] words = new String[]{"Cold", "Normal", "Dizzy"};
		
		HMMModel model = HMMModel.createModel();
		model.initialize(states, words);

		model.print();
		
		model.saveToFile("TEST");
		
		//test forward
		HMMLearner learner = new HMMLearner();
		String[] sentence = new String[]{"Normal", "Cold", "Cold", "Normal", "Dizzy"};
		double[][] fwd = learner.computeForward(model, sentence);
		System.out.println("forward");
		HMMUtil.print(fwd);
		System.out.println("backward");
		double[][] bwd = learner.computeBackward(model, sentence);
		HMMUtil.print(bwd);
		
		/*double[] fwdbwd = learner.forwardBackward(fwd, bwd);
		for(double d : fwdbwd) {
			System.out.print(d + " ");
		}*/
		
		//test train
		String[][] trainData = new String[][] {{"Normal", "Cold"},{"Cold", "Normal", "Dizzy"},{"Normal", "Dizzy"}};
		learner.train(model, states, words, trainData);
		//model.print();
		HMMDecoder decoder = new HMMDecoder(model);
		decoder.decode("demo/test1/input", "demo/test1/output2");
	}
}
