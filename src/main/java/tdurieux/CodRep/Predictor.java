package tdurieux.CodRep;

import tdurieux.CodRep.context.Parser;
import tdurieux.CodRep.context.LineContext;
import tdurieux.CodRep.filter.BraceFilter;
import tdurieux.CodRep.filter.BreakFilter;
import tdurieux.CodRep.filter.EquaityFilter;
import tdurieux.CodRep.filter.Filter;
import tdurieux.CodRep.filter.ImportFilter;
import tdurieux.CodRep.filter.KeywordsFilter;
import tdurieux.CodRep.filter.ReturnFilter;
import tdurieux.CodRep.filter.SizeFilter;
import tdurieux.CodRep.filter.StartFilter;
import tdurieux.CodRep.filter.ThrowFilter;
import tdurieux.CodRep.filter.VariableFilter;
import tdurieux.CodRep.sort.DistancePredictor;
import tdurieux.CodRep.util.SpoonUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class Predictor {

    private final List<LineContext> lines;
    private final String fileContent;
    private final String line;
    private final LineContext context;

    private final List<Filter> filters = Arrays.asList(
            new EquaityFilter(),
            new SizeFilter(),
            new BraceFilter(),
            new StartFilter(),
            new KeywordsFilter(),
            new ReturnFilter(),
            new ThrowFilter(),
            new BreakFilter(),
            new ImportFilter());

    public Predictor(String newLine, String fileContent) {
        this.line = newLine.trim();
        this.context = Parser.parse(this.line);
        this.fileContent = fileContent;
        lines = SpoonUtil.splitLineToMap(fileContent);
    }

    private boolean filter(LineContext existingContext) {
        for (int i = 0; i < filters.size(); i++) {
            Filter filter = filters.get(i);
            if (!filter.filter(existingContext, context)) {
                return false;
            }
        }
        return true;
    }

    public List<LineContext> predict() {
        List<LineContext> result = new ArrayList<>();
        for (LineContext s : lines) {
            if (filter(s)) {
                result.add(s);
            }
        }
        if (result.isEmpty()) {
            result = lines;
        }

        DistancePredictor distancePredictor = new DistancePredictor();
        return distancePredictor.sort(context, result);
    }

    public String getLine() {
        return line;
    }

    public String getFileContent() {
        return fileContent;
    }
}
