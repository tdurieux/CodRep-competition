package tdurieux.CodRep.predictor;

import java.util.List;

public interface LinePredictor {
    List<Integer> predict();

    String getLine();

    String getFileContent();
}
