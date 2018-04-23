package tdurieux.CodRep.predictor;


import tdurieux.CodRep.filter.SyntaxLineFilter;

public class SyntaxPredictor extends DistancePredictor {

    public SyntaxPredictor(String fileContent, String line) {
        super(fileContent, line, 2);
        this.lineFilter = new SyntaxLineFilter();
    }
}
