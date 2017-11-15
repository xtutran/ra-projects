package xttran.hmm.model;

import org.junit.Test;

import java.io.IOException;

public class TestHmmModel {
    @Test
    public void testReadData() throws IOException {
        // String dataFile = "src/main/resources/Trainset-POS-1/100276.seg.pos";
        String dataFile = "src/main/resources/Trainset-POS-1";
        HmmModel hmm = new HmmModel();
        hmm.train(dataFile);

        System.out.println(JsonUtil.serialize(hmm));

        HmmTagger tagger = new HmmTagger();
        String inputSentence = "Đó là con đường biển ngắn nhất để đi từ Ấn_Độ_Dương sang Thái_Bình_Dương , " +
                        "chiếm đến lượng hàng_hóa lưu_thông đường_biển của thế_giới , " +
                        "đó là hải_trình lớn nhất từ tây sang đông với 50.000 lượt tàu_bè qua_lại mỗi năm ...";
        System.out.println(tagger.viterbi(hmm, inputSentence));

    }

}
