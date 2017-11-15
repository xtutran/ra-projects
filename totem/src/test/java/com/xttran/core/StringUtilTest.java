package com.xttran.core;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class StringUtilTest {

    @Test
    public void testTokenize() {
        List<String> tokens = new ArrayList<String>();

        String line = "DOC-1_1.1:CARL_H._LINDNER/PersonOut	cand:carl h. lindner 1	head:lindner 1	type:<person> 1	uni_before:investor 1	bi_before:succeeds investor 1	uni_after:, 1	bi_after:, whose 1	dobj:succeeds 1";

        StringUtil.tokenize(line, tokens, "\t");

        System.out.println(tokens);
    }

    @Test
    public void testSplit() {
        String line = "A: ";

        String[] tokens = StringUtil.split(line, ':');

        for (String token : tokens) {
            System.out.println(token);
        }
    }

}
