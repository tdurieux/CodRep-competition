package tdurieux.CodRep;

import tdurieux.CodRep.context.LineContext;
import tdurieux.CodRep.predictor.LinePredictor;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.nio.file.Files;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

public class Launcher {
	public static void main(String[] args) throws Exception {
		if (args.length != 1) {
			System.err.println("Invalid Usage");
			System.err.println("java -jar coderep.jar <Files>");
			return;
		}

		runOnDataset(args[0]);
	}

	private static Predictor linePredictorFactory(File filename) throws IOException {
		String fileContent = new String(Files.readAllBytes(filename.toPath()));
		int indexFirstLine = fileContent.indexOf("\n");
		String newLine = fileContent.substring(0, indexFirstLine);
		fileContent = fileContent.substring(indexFirstLine + 1);
		return new Predictor(newLine, fileContent);
	}

	private static <T extends LinePredictor> void runOnDataset(String path) throws IOException, InterruptedException {
		ExecutorService executor = Executors.newFixedThreadPool(4);

		List<Callable<Integer>> tasks = Files.list(new File(path).toPath()).collect(Collectors.toList()).stream().map(p -> (Callable<Integer>) () -> {
			Predictor predictor;
			try {
				predictor = linePredictorFactory(p.toFile());
			} catch (Throwable e) {
				e.printStackTrace();
				return 1;
			}

			List<LineContext> predictions = predictor.predict();
			Integer prediction = 1;
			if (!predictions.isEmpty()) {
				int randomLine =  predictions.get(0).getLineNumbers().size() - 1;
				prediction = predictions.get(0).getLineNumbers().get(randomLine);
			}
			System.out.println(p.toAbsolutePath() + " " + (prediction));
			return prediction;
		}).collect(Collectors.toList());

		executor.invokeAll(tasks).stream()
			.map(future -> {
				try {
					return future.get();
				}
				catch (Exception e) {
					throw new IllegalStateException(e);
				}
			});
		executor.shutdown();
	}
}
