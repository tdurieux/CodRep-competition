package tdurieux.CodRep.filter;

import tdurieux.CodRep.context.Keywords;
import tdurieux.CodRep.context.LineContext;

import java.util.ArrayList;

public class VariableFilter implements Filter {

    @Override
    public boolean filter(LineContext existing, LineContext toPredict) {
        if (true || toPredict.getVariables().size() < 3 || existing.hasKeyword(Keywords.IMPLEMENTS)) {
            return true;
        }
        ArrayList<String> variables = new ArrayList<>(toPredict.getVariables());
        variables.removeAll(existing.getVariables());
        return variables.size() < toPredict.getVariables().size();
    }
}
