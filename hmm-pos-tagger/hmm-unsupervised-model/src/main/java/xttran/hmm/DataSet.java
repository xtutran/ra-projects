package xttran.hmm;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DataSet {
	public List<String[]> sentences ;
	private Map<String, Integer> dict ;
	private static final DataSet INSTANCE = new DataSet();
	
	private DataSet() {}
	
	public static DataSet createDataset() {
		return INSTANCE;
	}
	
	public String[][] getSentences() {
		if(sentences == null) {
			throw new NullPointerException("DataSet: must be load from file or folder");
		}
		
		return sentences.toArray(new String[0][0]);
	}
	
	public String[] getBagOfWords() {
		if(dict == null) {
			throw new NullPointerException("DataSet: must be load from file or folder");
		}
		
		return dict.keySet().toArray(new String[0]);
	}

	public void loadFromFolder(String folderPath) {
		File folder = new File(folderPath);
		if(!folder.exists() || !folder.canRead()) {
			throw new IllegalArgumentException("FolderPath: must exist or able to read");
		}
		
		File[] files ;
		if(folder.isDirectory()) {
			files = folder.listFiles();
		} else {
			files = new File[]{folder};
		}
		
		if(files.length == 0) {
			throw new IllegalArgumentException("DataSet: must be non-empty");
		}
		
		for(File file : files) {
			System.out.println(file.getAbsolutePath());
			loadFromFile(file.getAbsolutePath());
		}
	}
	
	public void loadFromFile(String filePath) {
		try {
	    BufferedReader reader = new BufferedReader(new FileReader(filePath));
	    
	    String line = null;
	    while((line = reader.readLine()) != null) {
	    	if(line.isEmpty()) {
	    		continue;
	    	}
	    	
	    	String[] words = line.trim().split(" ");
	    	addWords(words);
	    }
	    reader.close();
    } catch (FileNotFoundException e) {
	    e.printStackTrace();
    } catch (IOException e) {
	    e.printStackTrace();
    }
    
	}
	
	private void addWords(String[] words) {
		if(this.sentences == null) {
			this.sentences = new ArrayList<String[]>();
		}
		
		if(this.dict == null) {
			dict = new HashMap<String, Integer>();
		}
		
		if(words == null || words.length == 0) {
			return;
		}
		
		//this.sentences.add(words);
		List<String> meaningWords = new ArrayList<String>();
		for(String word : words) {
			if(word.isEmpty()) {
				continue;
			}
			
			String normalized = word.replaceAll("[,]", "");
			
			meaningWords.add(normalized);
			Integer i = this.dict.get(normalized);
			if(i == null) {
				this.dict.put(normalized, 1);
			} else {
				i = i + 1;
			}
		}
		
		if(meaningWords.size() == 0) {
			return;
		}
		this.sentences.add(meaningWords.toArray(new String[0]));
	}
}
