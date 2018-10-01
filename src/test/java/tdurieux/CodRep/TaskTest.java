package tdurieux.CodRep;

import org.junit.Assert;
import org.junit.Test;
import tdurieux.CodRep.context.LineContext;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class TaskTest {

    @Test
    public void catchTest() throws IOException {
        List<Path> tasks = geTasks("catch (", true);
        int execute = execute(tasks);
        Assert.assertTrue(execute <= 15);
    }

    @Test
    public void superTest() throws IOException {
        List<Path> tasks = geTasks("super(", true);
        int execute = execute(tasks);
        Assert.assertTrue(execute <= 14);
    }

    @Test
    public void thisTest() throws IOException {
        List<Path> tasks = geTasks("this(", true);
        int execute = execute(tasks);
        Assert.assertTrue(execute <= 20);
    }

    @Test
    public void returnTest() throws IOException {
        List<Path> tasks = geTasks("return ", true);
        int execute = execute(tasks);
        Assert.assertTrue(execute <= 20);
    }

    @Test
    public void importTest() throws IOException {
        List<Path> tasks = geTasks("import ", true);
        int execute = execute(tasks);
        Assert.assertTrue(execute <= 12);
    }

    @Test
    public void classTest() throws IOException {
        List<Path> tasks = geTasks("class ", true);
        int execute = execute(tasks);
        Assert.assertTrue(execute <= 12);
    }

    @Test
    public void throwTest() throws IOException {
        List<Path> tasks = geTasks("throw ", true);
        int execute = execute(tasks);
        Assert.assertTrue(execute <= 12);
    }

    @Test
    public void plusTest() throws IOException {
        List<Path> tasks = geTasks("+", false);
        int execute = execute(tasks);
        Assert.assertTrue(execute <= 1);
    }

    @Test
    public void quoteTest() throws IOException {
        List<Path> tasks = geTasks("\"", false);
        int execute = execute(tasks);
        Assert.assertTrue(execute <= 41);
    }

    @Test
    public void commaTest() throws IOException {
        List<Path> tasks = geTasks(",", false);
        int execute = execute(tasks);
        Assert.assertTrue(execute <= 12);
    }

    @Test
    public void annotationTest() throws IOException {
        List<Path> tasks = geTasks("@", false);
        int execute = execute(tasks);
        Assert.assertTrue(execute <= 50);
    }

    @Test
    public void inLineCommentTest() throws IOException {
        List<Path> tasks = geTasks("//", false);
        int execute = execute(tasks);
        Assert.assertTrue(execute <= 80);
    }

    private int execute(List<Path> paths) throws IOException {
        int failled = 0;
        for (int i = 365; i < paths.size(); i++) {
            Path path = paths.get(i);

            String fileContent = new String(Files.readAllBytes(path));
            int indexFirstLine = fileContent.indexOf("\n");
            String newLine = fileContent.substring(0, indexFirstLine);
            fileContent = fileContent.substring(indexFirstLine + 1);


            int solution = getSolution(path);
            String expectedLine = fileContent.split("\n")[solution].trim();
            List<LineContext> predict = new Predictor(newLine, fileContent).predict();

            if (predict.get(0).getLineNumbers().get(0) != solution && !predict.get(0).getLineContent().equals(expectedLine)) {
                System.out.println("Task " + i);
                System.out.println("To Predict " + newLine);
                System.out.println("Expected   " + expectedLine);
                System.out.println("Predicted  " + predict.get(0).getLineContent() + "\n\n");
                failled ++;
            }
        }
        System.out.println("Results " + failled + "/" + paths.size());
        return failled;
    }

    private List<Path> geTasks(String filter, boolean contains) throws IOException {
        List<Path> output = new ArrayList<>();
        int nbDataset = 4;
        for (int i = 0; i < nbDataset; i++) {
            List<Path> tasks = Files.list(new File( "./Datasets/Dataset"+ (i+1) + "/Tasks/").toPath()).collect(Collectors.toList());
            for (int j = 0; j < tasks.size(); j++) {
                Path path = tasks.get(j);

                String fileContent = new String(Files.readAllBytes(path));
                int indexFirstLine = fileContent.indexOf("\n");
                String newLine = fileContent.substring(0, indexFirstLine);
                fileContent = fileContent.substring(indexFirstLine + 1);

                int solution = getSolution(path);
                String expected = fileContent.split("\n")[solution].trim();
                if (contains) {
                    if (newLine.contains(filter) || expected.contains(filter)) {
                        output.add(path);
                    }
                } else {
                    if (newLine.startsWith(filter) || expected.startsWith(filter)) {
                        output.add(path);
                    }
                }
            }
        }
        return output;
    }

    private int getSolution(Path task) throws IOException {
        return Integer.parseInt(new String(Files.readAllBytes(new File( task.toString().replace("Tasks", "Solutions")).toPath())));
    }
}