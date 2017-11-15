package edu.smu.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Alphabet {

    private Map<String, Integer> indices;
    private List<String> symbols;

    public Alphabet() {
        indices = new HashMap<String, Integer>();
        symbols = new ArrayList<String>();
    }

    public int size() {
        return symbols.size();
    }

    /**
     * Adds a new symbol into the <code>Alphabet</code>. If the symbol is
     * already contained in the <code>Alphabet</code>, this method simply returns
     * the index of the symbol. Otherwise, a new index is assigned to the symbol
     * and the new index is returned. Indices are non-negative integer numbers
     * and sequentially assigned. The first symbol is assigned the index 0.
     *
     * @param symbol symbol to be added
     * @return index of the newly added symbol
     */
    public int addSymbol(String symbol) {
        if (!indices.containsKey(symbol)) {
            indices.put(symbol, new Integer(indices.size()));
            symbols.add(symbol);
        }
        return indices.get(symbol).intValue();
    }

    /**
     * Returns the index of the specified symbol. If the symbol is not contained
     * in the <code>Alphabet</code>, -1 is returned.
     *
     * @param symbol symbol of which the index is to be returned
     * @return index of the specified symbol or -1 if the symbol is not contained
     * in the <code>Alphabet</code>
     */
    public int getIndex(String symbol) {
        if (indices.containsKey(symbol)) {
            return indices.get(symbol).intValue();
        }
        return -1;
    }

    /**
     * Returns the symbol associated with the specified index. If the index is
     * invalid, <code>null</code> is returned.
     *
     * @param index index with which the symbol associated is returned
     * @return symbol associated with the specified index or <code>null</code>
     * if the index is invalid
     */
    public String getSymbol(int index) {
        if (index >= 0 && index < symbols.size()) {
            return symbols.get(index);
        }
        return null;
    }

    public String toString() {
        String text = "Alphabet[size: " + symbols.size();
        if (symbols.size() > 0) {
            text += "; members: 0 ==> " + symbols.get(0);
            for (int i = 1; i < symbols.size(); i++) {
                text += ", " + i + " ==> " + symbols.get(i);
            }
        }
        text += "]";
        return text;
    }
}