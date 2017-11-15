package edu.smu.experiment.dto;

public class Tag implements Comparable<Tag> {
    public int clusterLabel;
    public int candidateLabel;
    public String tag;

    public double precision;
    public double recall;
    public double f1;
    public int amount;
    public int trueLabelSize;

    public double preDev_s = 0;
    public double recallDev_s = 0;
    public double f1Dev_s = 0;

    public double preOverall = 0;
    public double preOverall_s = 0;
    public double recallOverall = 0;
    public double recallOverall_s = 0;
    public double f1Overall = 0;
    public double f1Overall_s = 0;

    public Tag() {
        precision = 0;
        recall = 0;
        f1 = 0;
    }

    public Tag(String trueLabel, String text) {
        parseFromText(trueLabel, text);
    }

    public Tag(String tag, double precision, double recall, double f1) {
        this.tag = tag;
        this.precision = precision;
        this.recall = recall;
        this.f1 = f1;
    }

    public Tag(Tag newlabel) {
        this.candidateLabel = newlabel.candidateLabel;
        this.clusterLabel = newlabel.clusterLabel;
        this.precision = newlabel.precision;
        this.recall = newlabel.recall;
        this.f1 = newlabel.f1;
        this.amount = newlabel.amount;
    }

    public Tag(int clusterLabel, int candidateLabel, double precision, double recall, double f1) {
        this.clusterLabel = clusterLabel;
        this.candidateLabel = candidateLabel;
        this.precision = precision;
        this.recall = recall;
        this.f1 = f1;
    }

    public Tag(int clusterLabel, int candidateLabel, double precision, double recall, double f1, int amount) {
        this.clusterLabel = clusterLabel;
        this.candidateLabel = candidateLabel;
        this.precision = precision;
        this.recall = recall;
        this.f1 = f1;
        this.amount = amount;
    }

    public void sum(Tag l) {
        this.clusterLabel = l.clusterLabel;
        this.candidateLabel = l.candidateLabel;
        this.precision += l.precision;
        this.recall += l.recall;
        this.f1 += l.f1;
    }

    public void average(int total) {
        this.precision /= total;
        this.recall /= total;
        this.f1 /= total;
    }

    public int compareTo(Tag o) {
        // TODO Auto-generated method stub
        int result = 0;

        if (o.f1 - this.f1 > 0)
            result = 1;
        else if (o.f1 - this.f1 < 0)
            result = -1;
        else
            result = 0;

        return result;
    }

    public void print(String finalTag) {
        System.out.println("F1 = " + f1 + "+/-" + f1Dev_s);
        System.out.println("Precision = " + precision + "+/-" + preDev_s);
        System.out.println("Recall = " + recall + " +- " + recallDev_s);
        System.out.println("Final Label: " + finalTag);
    }

    public String toString(String candTag) {
        return String.format("%-10.10s %-10.10s %-10.10s %-10.10s %-10.10s", candTag, amount, precision, recall, f1);
    }

    public void parseFromText(String trueLabel, String text) {
        this.tag = trueLabel;

        String[] tokens = text.split("\t");
        if (tokens.length == 3) {
            this.precision = Double.parseDouble(tokens[0]);
            this.recall = Double.parseDouble(tokens[1]);
            this.f1 = Double.parseDouble(tokens[2]);
        }
    }
}
