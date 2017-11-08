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
        System.out.println(tagger.viterbi(hmm,
                "Đó/P là/V con/Nc đường/N biển/N ngắn/A nhất/A để/E đi/V từ/E Ấn_Độ_Dương/Np sang/V Thái_Bình_Dương/Np ,/CH chiếm/V đến/E lượng/N hàng_hóa/N lưu_thông/V đường_biển/N của/E thế_giới/N ,/CH đó/P là/V hải_trình/N lớn/A nhất/R từ/E tây/N sang/E đông/N với/E 50.000/M lượt/Nc tàu_bè/N qua_lại/V mỗi/L năm/N .../CH"));

        // hmm.estimation();
    }

}
