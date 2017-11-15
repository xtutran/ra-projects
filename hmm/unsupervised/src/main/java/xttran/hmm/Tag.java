package xttran.hmm;

public class Tag {
    public static final String SEPARATOR = "/";
    private double probability;
    private double temp;
    private String path;

    public Tag(double probability, double temp, String path) {
        this.probability = probability;
        this.setTemp(temp);
        this.path = path;
    }

    public static String updatePath(String... paths) {
        StringBuilder builder = new StringBuilder(16 * (paths.length + 1));
        builder.append(paths[0]);
        builder.append(SEPARATOR);
        builder.append(paths[1]);
        return builder.toString();
    }

    public double getProbability() {
        return probability;
    }

    public void setProbability(double totalProb) {
        this.probability = totalProb;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String toString(String[] observations) {
        if (observations == null || observations.length == 0 || path == null) {
            return null;
        }

        String[] states = path.split(SEPARATOR);
        if (states.length == 0) {
            System.err.println("[ERROR] in decode algorithm");
            return null;
        }

        // List<String> result = new ArrayList<String>();
        StringBuilder builder = new StringBuilder(16 * observations.length * 2);
        for (int i = 0; i < observations.length; i++) {
            builder.append(updatePath(observations[i], states[i]));
            builder.append(" ");
        }
        return builder.toString();
    }

    public double getTemp() {
        return temp;
    }

    public void setTemp(double temp) {
        this.temp = temp;
    }
}
