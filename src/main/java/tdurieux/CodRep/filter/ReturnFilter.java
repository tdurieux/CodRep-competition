package tdurieux.CodRep.filter;

import tdurieux.CodRep.context.Keywords;
import tdurieux.CodRep.context.LineContext;

import java.util.ArrayList;

public class ReturnFilter implements Filter {

    @Override
    public boolean filter(LineContext existing, LineContext toPredict) {
        if (toPredict.getLineContent().equals("return true;")) {
            return existing.getLineContent().equals("return false;");
        }
        if (toPredict.getLineContent().equals("return false;")) {
            return existing.getLineContent().equals("return true;");
        }
        if (!existing.hasKeyword(Keywords.RETURN)) {
            return true;
        }
        if (toPredict.hasKeyword(Keywords.BREAK) || toPredict.hasKeyword(Keywords.CONTINUE) || toPredict.hasKeyword(Keywords.RETURN) || toPredict.hasKeyword(Keywords.THROW)) {
            if (toPredict.getLineContent().equals("return null!")) {
                return !(existing.getNumbers().size() > 0);
            }
            return true;
        }
        return false;
    }
}
