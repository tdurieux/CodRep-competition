package tdurieux.CodRep;

import org.junit.Test;
import tdurieux.CodRep.predictor.ASTValidatorPredictor;
import tdurieux.CodRep.predictor.LinePredictor;
import tdurieux.CodRep.predictor.SyntaxPredictor;
import tdurieux.CodRep.util.SpoonUtil;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.nio.file.Files;
import java.nio.file.Path;
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
     * SyntaxPredictor sorensen:     3887/4394 88% , in Top 10 4331 98% , loss 0.1143165305556653
     * SyntaxPredictor jaccard:      3887/4394 88% , in Top 10 4331 98% , loss 0.1143165305556653
     * SyntaxPredictor jaroWinkler   3468/4394 78% , in Top 10 4167 94% , loss 0.20649968333865018
     * SyntaxPredictor Levenshtein   3611/4394 82% , in Top 10 4257 96% , loss 0.1757567738051666
     */
    @Test
    public void dataset1() throws IOException {
        //runOnDataset(DistancePredictor.class, "./Datasets/Dataset1");
        //runOnDataset(SyntaxPredictor.class, "./Datasets/Dataset1");
        runOnDataset(ASTValidatorPredictor.class, "./Datasets/Dataset1");
    }

    /**
     * Score: Valid 1527, In Prediction 3606, Error 2732, Total 11069
     * DistancePredictor cosin       9980/11069 90% , in Top 10 10798 97% , loss 0.09612452079485122
     * DistancePredictor sorensen    9935/11069 89% , in Top 10 10816 97% , loss 0.10024445092498996
     * SyntaxPredictor cosin         9962/11069 89% , in Top 10 10809 97% , loss 0.09809164895063167
     * SyntaxPredictor sorensen     10104/11069 91% , in Top 10 10833 97% , loss 0.08536293667171425
     */
    @Test
    public void dataset2() throws IOException {
        //runOnDataset(DistancePredictor.class,"./Datasets/Dataset2");
        runOnDataset(SyntaxPredictor.class,"./Datasets/Dataset2");
    }

    public static <T extends LinePredictor> T linePredictorFactory(Class<T> predictorClass, File filename) throws IOException {
        String fileContent = new String(Files.readAllBytes(filename.toPath()));
        int indexFirstLine = fileContent.indexOf("\n");
        String newLine = fileContent.substring(0, indexFirstLine);
        fileContent = fileContent.substring(indexFirstLine + 1);

        try {
            Constructor<T> constructor = predictorClass.getConstructor(String.class, String.class);
            return constructor.newInstance(fileContent, newLine);
        } catch (Exception e) {
            throw new IllegalArgumentException("Constructor not found");
        }
    }

    private <T extends LinePredictor> void runOnDataset(Class<T> predictorClass, String path) throws IOException {
        int valid = 0;
        int presentInThePrediction = 0;
        double totalLoss = 0;
        List<Path> tasks = Files.list(new File(path + "/Tasks/").toPath()).collect(Collectors.toList());
        for (int i = 0; i < tasks.size(); i++) {
            Path p = tasks.get(i);
            T predictor;
            try {
                predictor = linePredictorFactory(predictorClass, p.toFile());
            } catch (Throwable e) {
                continue;
            }
            try {

                String task = p.toFile().getName();

                int solution = Integer.parseInt(new String(Files.readAllBytes(new File(path + "/Solutions/" + task).toPath())));
                List<Integer> predictions = predictor.predict();

                int prediction = 0;
                if (!predictions.isEmpty()) {
                    prediction = predictions.get(0);
                } else {
                    //predictor.predict();
                    //throw new RuntimeException("should not " + predictor.getLine());
                }
                if (prediction == solution) {
                    valid++;
                } else {
                    System.out.println("Old Line  " + predictor.getLine().trim());
                    String[] fileByLine = SpoonUtil.splitLine(predictor.getFileContent());
                    System.out.println("Expected  " + fileByLine[solution].trim() + (predictions.contains(solution)? " in top 10 ":""));
                    System.out.println("Predicted " + fileByLine[prediction].trim() + "\n\n");
                    predictor.predict();
                }
                if (predictions.contains(solution)) {
                    presentInThePrediction++;
                }
                double loss = Math.tanh(Math.abs(solution-prediction));
                if (loss > 0.5) {
                    predictor.predict();
                    //System.err.println(task);
                }
                totalLoss += loss;


            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        int recall = valid*100/(tasks.size()+1);
        System.out.println(predictorClass.getSimpleName() + " " + valid + "/"+ (tasks.size()) + " " + recall + "% , in Top 10 " + presentInThePrediction + " " + (presentInThePrediction*100/(tasks.size()+1)) + "% , loss " + totalLoss/(tasks.size() + 1));
    }
}