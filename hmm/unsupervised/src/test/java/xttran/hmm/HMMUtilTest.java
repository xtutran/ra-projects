package xttran.hmm;

import org.junit.Test;
import xttran.hmm.util.HMMUtil;

public class HMMUtilTest {

    // @Test
    public void testRandom() {
        int n = 10;
        double[] actual = HMMUtil.random(n);
        for (double a : actual) {
            System.out.println(a);
        }
    }

    @Test
    public void testLog() {
        System.out.println(Math.log10(2) / Math.log(2));
    }
}
