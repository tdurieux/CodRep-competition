package tdurieux.CodRep;

import tdurieux.CodRep.predictor.LinePredictor;
import tdurieux.CodRep.predictor.SyntaxPredictor;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

public class Launcher {
	public static void main(String[] args) throws IOException {
		if (args.length != 1) {
			System.err.println("Invalid Usage");
			System.err.println("java -jar coderep.jar <Files>");
			return;
		}

		runOnDataset(SyntaxPredictor.class, args[0]);
	}

	private static <T extends LinePredictor> T linePredictorFactory(Class<T> predictorClass, File filename) throws IOException {
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

	private static <T extends LinePredictor> void runOnDataset(Class<T> predictorClass, String path) throws IOException {
		List<Path> tasks = Files.list(new File(path).toPath()).collect(Collectors.toList());
		for (Path p : tasks) {
			T predictor;
			try {
				predictor = linePredictorFactory(predictorClass, p.toFile());
			} catch (Throwable e) {
				e.printStackTrace();
				continue;
			}

			List<Integer> predictions = predictor.predict();
			int prediction = 1;
			if (!predictions.isEmpty()) {
				prediction = predictions.get(0);
			}
			System.out.println(p.toAbsolutePath() + " " + (prediction));
		}
	}
}
