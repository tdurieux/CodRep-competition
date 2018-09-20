package tdurieux.CodRep.filter;

import tdurieux.CodRep.context.LineContext;

import java.util.Arrays;
import java.util.List;

public class StartFilter implements Filter {
    private static final List<Character> tokens = Arrays.asList('.', '+', '{');

    @Override
    public boolean filter(LineContext existing, LineContext toPredict) {
        for (Character token : tokens) {
            if (toPredict.getLineContent().charAt(0) == token && existing.getLineContent().charAt(0) != token) {
                return false;
            }
        }
        return true;
    }
}
