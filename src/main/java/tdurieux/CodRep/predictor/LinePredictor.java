package tdurieux.CodRep.predictor;

import java.util.List;

public interface LinePredictor {
    List<List<Integer>> predict();

    String getLine();

    String getFileContent();
}
