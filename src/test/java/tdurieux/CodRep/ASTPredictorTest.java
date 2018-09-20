package tdurieux.CodRep;

import org.junit.Test;
import tdurieux.CodRep.context.LineContext;
import tdurieux.CodRep.predictor.LinePredictor;
import tdurieux.CodRep.util.SpoonUtil;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ASTPredictorTest {

    /**
     * Score: Valid 3871/4394, In Prediction 4325, loss 0.0, Average: 0.11779832388093357
     * 3767/4394 85% , in Top 104083, loss 0.0 avg. 0.1411294693832948
     * DistancePredictor Cousin:     3798/4394 86% , in Top 10 4303 97% , loss 0.13431694712302783
     * DistancePredictor sorensen:   3844/4394 87% , in Top 10 4314 98% , loss 0.12433358155850202
     * DistancePredictor jaccard:    3844/4394 87% , in Top 10 4314 98% , loss 0.12433358155850202
     * DistancePredictor jaroW       3388/4394 77% , in Top 10 4069 92% , loss 0.22485497589609324
     * DistancePredictor Levenshtein 3557/4394 80% , in Top 10 4221 96% , loss 0.18782169269848853
     * SyntaxPredictor Cousin:       3834/4394 87% , in Top 10 4332 98% , loss 0.12629793562473987
     * SyntaxPredictor sorensen:     3907/4394 88% , in Top 10 4327 98% , loss 0.10981198440495205
     * SyntaxPredictor jaccard:      3908/4394 88% , in Top 10 4325 98% , loss 0.10958444752743954
     * SyntaxPredictor jaroWinkler   3468/4394 78% , in Top 10 4167 94% , loss 0.20649968333865018
     * SyntaxPredictor Levenshtein   3611/4394 82% , in Top 10 4257 96% , loss 0.1757567738051666
     * SyntaxPredictor 3934/4394 89% , in Top 10 4334 98% , loss 0.10400918447628195
     * 3964/4394 90% , in Top 10 4352 99% , loss 0.09722562405352109
     */
    @Test
    public void dataset1() throws IOException {
        runOnDataset("./Datasets/Dataset1");
    }

    /**
     * Score: Valid 1527, In Prediction 3606, Error 2732, Total 11069
     * DistancePredictor cosin       9980/11069 90% , in Top 10 10798 97% , loss 0.09612452079485122
     * DistancePredictor sorensen    9935/11069 89% , in Top 10 10816 97% , loss 0.10024445092498996
     * SyntaxPredictor cosin         9962/11069 89% , in Top 10 10809 97% , loss 0.09809164895063167
     * SyntaxPredictor sorensen     10104/11069 91% , in Top 10 10831 97% , loss 0.0853413900365686
     * SyntaxPredictor 10123/11069 91% , in Top 10 10850 98% , loss 0.08405196035050681
     * 10174/11069 91% , in Top 10 10914 98% , loss 0.07949290882664403
     */
    @Test
    public void dataset2() throws IOException {
        runOnDataset("./Datasets/Dataset2");
    }

    /**
     * SyntaxPredictor 17466/18633 93% , in Top 10 18444 98% , loss 0.06108865517130069
     * SyntaxPredictor 17520/18633 94% , in Top 10 18454 99% , loss 0.05824092991175515
     * SyntaxPredictor 17656/18633 94% , in Top 10 18421 98% , loss 0.051236059205672306
     * 17733/18633 95% , in Top 10 18481 99% , loss 0.04740034860505994
     *
     */
    @Test
    public void dataset3() throws IOException {
        runOnDataset("./Datasets/Dataset3");
    }

    /**
     * SyntaxPredictor 15786/17132 92% , in Top 10 16936 98% , loss 0.07700153400317002
     * SyntaxPredictor 15930/17132 92% , in Top 10 17001 99% , loss 0.06890840252070096
     * SyntaxPredictor 16014/17132 93% , in Top 10 16988 99% , loss 0.06400286891954794
     * 16049/17132 93% , in Top 10 17011 99% , loss 0.06201224512601686
     */
    @Test
    public void dataset4() throws IOException {
        runOnDataset("./Datasets/Dataset4");
    }

    public static Predictor linePredictorFactory(File filename) throws IOException {
        String fileContent = new String(Files.readAllBytes(filename.toPath()));
        int indexFirstLine = fileContent.indexOf("\n");
        String newLine = fileContent.substring(0, indexFirstLine);
        fileContent = fileContent.substring(indexFirstLine + 1);
        return new Predictor(newLine, fileContent);
    }

    private <T extends LinePredictor> void runOnDataset(String path) throws IOException {
        int valid = 0;
        int presentInThePrediction = 0;
        double totalLoss = 0;
        List<Path> tasks = Files.list(new File(path + "/Tasks/").toPath()).collect(Collectors.toList());
        for (int i = 0; i < tasks.size(); i++) {
            Path p = tasks.get(i);
            Predictor predictor;
            try {
                predictor = linePredictorFactory(p.toFile());
            } catch (Throwable e) {
                throw new RuntimeException(e);
                //continue;
            }
            try {

                String task = p.toFile().getName();

                int solution = Integer.parseInt(new String(Files.readAllBytes(new File(path + "/Solutions/" + task).toPath())));
                List<LineContext> predictions = predictor.predict();
                List<Integer> allLines = new ArrayList<>(10);
                for (LineContext prediction : predictions) {
                    if (prediction == null) {
                        predictor.predict();
                    }
                    allLines.addAll(prediction.getLineNumbers());
                }

                int prediction = 0;
                if (!predictions.isEmpty()) {
                    //int randomLine = ThreadLocalRandom.current().nextInt(0, predictions.get(0).size());
                    int randomLine =  predictions.get(0).getLineNumbers().size() - 1;
                    prediction = predictions.get(0).getLineNumbers().get(randomLine);
                } else {
                    //predictor.predict();
                    //throw new RuntimeException("should not " + predictor.getLineContent());
                }
                if (prediction == solution) {
                    valid++;
                } else {
                    System.out.println("Old Line  " + predictor.getLine().trim());
                    String[] fileByLine = SpoonUtil.splitLine(predictor.getFileContent());
                    System.out.println("Expected  " + fileByLine[solution].trim() + (allLines.contains(solution)? " in top 10 " + (allLines.indexOf(solution)) :""));
                    System.out.println("Predicted " + fileByLine[prediction].trim() + "\n\n");
                    predictor.predict();
                }
                if (allLines.contains(solution)) {
                    presentInThePrediction++;
                }
                double loss = Math.tanh(Math.abs(solution-prediction));
                if (loss > 0.5) {
                    //predictor.predict();
                    //System.err.println(task);
                }
                totalLoss += loss;


            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        int recall = valid*100/(tasks.size()+1);
        System.out.println(valid + "/"+ (tasks.size()) + " " + recall + "% , in Top 10 " + presentInThePrediction + " " + (presentInThePrediction*100/(tasks.size()+1)) + "% , loss " + totalLoss/(tasks.size() + 1));
    }
}