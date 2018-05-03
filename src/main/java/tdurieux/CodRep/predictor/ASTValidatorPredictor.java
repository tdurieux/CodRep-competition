package tdurieux.CodRep.predictor;


import spoon.reflect.CtModel;
import tdurieux.CodRep.filter.SyntaxLineFilter;
import tdurieux.CodRep.util.SpoonUtil;

import java.util.List;

public class ASTValidatorPredictor extends SyntaxPredictor {

    private final CtModel ctModel;
    private final String[] lines;

    public ASTValidatorPredictor(String fileContent, String line) {
        super(fileContent, line);
        ctModel = SpoonUtil.getModelFromString(fileContent);
        lines = SpoonUtil.splitLine(fileContent);
    }

    @Override
    public List<Integer> predict() {
        List<Integer> prediction = super.predict();

        for (int i = 0; i < Math.min(prediction.size(), 10); i++) {
            Integer integer = prediction.get(i);
            System.out.println(lines[integer]);
        }
        return prediction;
    }
}
