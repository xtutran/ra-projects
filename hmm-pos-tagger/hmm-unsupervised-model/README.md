### Description: This program implement HMM Model/Forward-Backward/EM/Viterbi algorithms

 1. The structure of program
The program contain 5 core class (DataSet, HMMDecoder, HMMLearner, HMMModel, Tag) and 1 main class (MainApp)
- DataSet: This class used to read input file/folder and transform to input of HMM Model
- HMMDecoder: implements Viterbi algorithm to decode new sequence.
- HMMModel: includes all of parameters for HMM such as (states, symbols, start probabilities, transition and emmission)
- HMMLearner: implements EM and Forward-Backward algorithms.

 2. Input and output

  2.1. Input: Text file
   ```
   #The program will be read file line by line and store in two D array(String[][]) of words 
   Example:
   	Normal Dizzy Cold Normal
   ```
  2.2. Output:
 - Model output: An text file that contains information of HMM model (states, symbols, start probabilities, transition and emmission)
   ```
   Example: 
	STATES=Healthy,Fever
	WORDS=dizzy,cold,normal
	START=0.6,0.4
	TRANS=0.7,0.3;0.6,0.4
	EMMIS=0.1,0.4,0.5;0.6,0.3,0.1
   ```
			
 - Tagged output: An text file that tagged by using Viterbi algorithm
   ```
   Example:
   	normal/Healthy dizzy/Fever cold/Healthy normal/Healthy
   ```
   			
3. How to run:
 - Use command
 ```bash
 java -jar hmm-0.0.1-SNAPSHOT.jar xttran.hmm.MainApp <algorithm> <input> <output>
 ```
 - Where:
 
    + If <algorithm> = -tagger , it mean the program will do full follow (from read train data -> generate hmm model -> infer tag)
      ```bash
    	#Command:
		java -jar hmm-0.0.1-SNAPSHOT.jar xttran.hmm.MainApp -tagger <train data path> <test data path> <tagged output path> <number of states>
	
	#Example:
		java -jar hmm-0.0.1-SNAPSHOT.jar xttran.hmm.MainApp -tagger src/main/resources/data/train/acq/ src/main/resources/data/test/acq/0009613 demo/0009613 5
      ```

    + If <algorithm> = -em , it mean the program will generate hmm model from train data
    		Command:
    			java -jar hmm-0.0.1-SNAPSHOT.jar xttran.hmm.MainApp -em <train data path> <model output path> <number of states>

			Example:
				java -jar hmm-0.0.1-SNAPSHOT.jar xttran.hmm.MainApp -em src/main/resources/data/train/acq/ HMM.tagger 5

    + If <algorithm> = -viterbi , it mean the program will infer tag for new sequence based on existing HMM model
    		Command:
    			java -jar hmm-0.0.1-SNAPSHOT.jar xttran.hmm.MainApp -viterbi <model input path> <input path> <tagged output path>
    			
    		Example:
    			java -jar hmm-0.0.1-SNAPSHOT.jar xttran.hmm.MainApp -viterbi HMM.tagger src/main/resources/data/train/acq/0000005 output-0000005 5
    		
4. Default parameters in EM
```
MAX_INTERATIONS = 100;
MIN_ERROR = 0.001;
```
  
