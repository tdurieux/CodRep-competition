package tdurieux.CodRep.predictor;


import org.eclipse.jdt.core.compiler.CategorizedProblem;
import spoon.Launcher;
import spoon.reflect.CtModel;
import spoon.support.compiler.jdt.JDTBasedSpoonCompiler;
import tdurieux.CodRep.util.SpoonUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

public class ASTValidatorPredictor extends SyntaxPredictor {

    private final CtModel ctModel;
    private final Map<String, List<Integer>> lines;
    private final List<CategorizedProblem> problems;
    private final String[] strLines;

    public ASTValidatorPredictor(String fileContent, String line) {
        super(fileContent, line);
        Launcher modelFromString = SpoonUtil.getModelFromString(fileContent);
        this.problems = ((JDTBasedSpoonCompiler) modelFromString.getModelBuilder()).getProblems();
        ctModel = modelFromString.getModel();
        lines = SpoonUtil.splitLineToMap(fileContent);
        strLines = SpoonUtil.splitLine(fileContent);
    }

    @Override
    public List<List<Integer>> predict() {
        List<List<Integer>> output = new ArrayList<>();
        List<List<Integer>> prediction = super.predict();

        for (int i = 0; i < Math.min(prediction.size(), 10); i++) {
            Integer integer = prediction.get(i).get(0);
            String oldLine = strLines[integer];
            strLines[integer] = this.getLine();
            try {
                Launcher launcher = SpoonUtil.getModelFromString(String.join("\n", strLines));
                List<CategorizedProblem> localroblems = SpoonUtil.filterProblem(((JDTBasedSpoonCompiler) launcher.getModelBuilder()).getProblems(), integer);
                if (isFatalError(localroblems)) {
                    continue;
                }
                output.add(prediction.get(i));
                //System.out.println(oldLine.trim() + " " + localroblems.size() + "/" + SpoonUtil.filterProblem(problems, integer).size());
            } catch (Exception e) {
                continue;
            }
        }
        return output;
    }

    private boolean isFatalError(List<CategorizedProblem> problems) {
        int[] errors = new int[]{55, 83, 231, 232, 235};
        for (int i = 0; i < problems.size(); i++) {
            CategorizedProblem categorizedProblem = problems.get(i);
            if (IntStream.of(errors).anyMatch(x->x==(categorizedProblem.getID() & 8388607))) {
                return true;
            }
        }
        return false;
    }
}
