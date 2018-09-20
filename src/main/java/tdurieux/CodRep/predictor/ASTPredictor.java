package tdurieux.CodRep.predictor;

import spoon.reflect.CtModel;
import spoon.reflect.code.*;
import spoon.reflect.declaration.*;
import spoon.reflect.reference.CtTypeReference;
import spoon.reflect.visitor.filter.AbstractFilter;
import spoon.reflect.visitor.filter.LineFilter;
import spoon.reflect.visitor.filter.TypeFilter;
import tdurieux.CodRep.sort.DistancePredictor;
import tdurieux.CodRep.util.ReachableVariableVisitor;
import tdurieux.CodRep.util.SpoonUtil;

import java.util.*;
import java.util.stream.Collectors;

public class ASTPredictor implements LinePredictor {

    private CtModel ctModel;
    private CtElement ctStatement;
    private final String line;
    private final String fileContent;

    public ASTPredictor(String fileContent, String line) {
        this.line = line;
        this.fileContent = fileContent;
        try {
            ctModel = SpoonUtil.getModelFromString(fileContent).getModel();
            ctStatement = SpoonUtil.getAstFromLine(line);
        } catch (Exception e) {
            System.err.println(e.getMessage());
            System.err.println(line);
        }
    }

    @Override
    public String getLine() {
        return line;
    }

    @Override
    public String getFileContent() {
        return fileContent;
    }

    public List<List<Integer>> predict() {
        /*DistancePredictor distancePredictor = new DistancePredictor(fileContent, line);

        if (ctStatement == null) {
            return distancePredictor.predict();
        }

        Set<String> neededVariables = ctStatement.getElements(new AbstractFilter<CtElement>() {
            @Override
            public boolean matches(CtElement element) {
                if (!super.matches(element)) {
                    return false;
                }
                if (element.isImplicit()) {
                    return false;
                }
                if (!(element instanceof CtVariableAccess) && !(element instanceof CtTypeAccess)) {
                    return false;
                }

                if (element instanceof CtTypeAccess) {
                    CtTypeReference accessedType = ((CtTypeAccess) element).getAccessedType();
                    if (accessedType == null
                            || accessedType.getSimpleName().isEmpty()
                            || !Character.isLowerCase(accessedType.getSimpleName().charAt(0))
                            || accessedType.getSimpleName().charAt(0) == '_') {
                        return false;
                    }
                }
                if (element instanceof CtFieldAccess) {
                    CtExpression target = ((CtFieldAccess) element).getTarget();
                    if (target != null && !target.toString().equals("this")) {
                        return false;
                    }
                }
                if (element instanceof CtVariableAccess) {
                    if (((CtVariableAccess) element).getVariable().getDeclaration() != null) {
                        return false;
                    }
                }
                return true;
            }
        }).stream().map(v -> {
            if (v instanceof CtTypeAccess) {
                return ((CtTypeAccess) v).getAccessedType().getSimpleName();
            }
            if (v instanceof CtFieldAccess) {
                return ((CtFieldAccess) v).getVariable().getSimpleName();
            }
            return v.toString();
        }).collect(Collectors.toSet());

        if (this.ctStatement instanceof CtTypeMember) {
            List<CtTypeMember> options = ctModel.getElements(new TypeFilter<>(CtTypeMember.class)).stream().filter(s -> {
                if (s instanceof CtConstructor && ctStatement instanceof CtExecutable) {
                    if (s.getDeclaringType().getSimpleName().equals(((CtTypeMember) ctStatement).getSimpleName())) {
                        if (((CtConstructor) s).getParameters().size() == ((CtExecutable) ctStatement).getParameters().size()) {
                            return true;
                        }
                    }
                    return false;
                }
                return ((CtTypeMember) ctStatement).getSimpleName().equals(s.getSimpleName());
            }).collect(Collectors.toList());

            List<List<Integer>> output = new ArrayList<>();
            if (options.isEmpty()) {
                return distancePredictor.predict();
            }
            for (int i = 0; i < options.size(); i++) {
                CtTypeMember option =  options.get(i);
                //output.add(option.getPosition().getLineContent() - 1);
            }
            Map<List<Integer>, Double> result = distancePredictor.getSimilarityImpl();
            output.sort((s1, s2) -> {
                if (!result.containsKey(s1) && result.containsKey(s2)) {
                    return 1;
                }
                if (result.containsKey(s1) && !result.containsKey(s2)) {
                    return -1;
                }
                if (!result.containsKey(s1) && !result.containsKey(s2)) {
                    return 0;
                }
                if (result.get(s1) > result.get(s2)) {
                    return -1;
                } else if (result.get(s1) < result.get(s2)) {
                    return 1;
                }
                return 0;
            });
            return output;
        }
        final Class<? extends CtStatement> statementType;
        if (this.ctStatement instanceof CtLocalVariable) {
            statementType = CtLocalVariable.class;
        } else if (this.ctStatement instanceof CtLoop) {
            statementType = (Class<? extends CtStatement>) ctStatement.getClass();
        } else if (this.ctStatement instanceof CtType) {
            statementType = (Class<? extends CtStatement>) ctStatement.getClass();
        } else if (this.ctStatement instanceof CtTypeMember) {
            statementType = (Class<? extends CtStatement>) ctStatement.getClass();
        } else if (this.ctStatement.getParent().getParent() instanceof CtConstructor) {
            statementType = CtInvocation.class;
        } else {
            statementType = CtStatement.class;
        }
        String ctStatementToString = ctStatement.toString();

        List<CtStatement> options = ctModel.getElements(new TypeFilter<CtStatement>((Class<? super CtStatement>) statementType)).stream().filter(s -> {
            if (s.equals(ctStatement) || s.isImplicit()) {
                return false;
            }
            try {
                if (!(ctStatement instanceof CtType) && !new LineFilter().matches(s)) {
                    return false;
                }
            } catch (Exception e) {
                return false;
            }
            if (s instanceof CtBodyHolder && !(ctStatement instanceof CtBodyHolder)) {
                return false;
            }
            if (s instanceof CtLoop && !(ctStatement instanceof CtLoop) && !(ctStatement instanceof CtIf)) {
                return false;
            }
            if (s instanceof CtIf && !(ctStatement instanceof CtLoop) && !(ctStatement instanceof CtIf)) {
                return false;
            }
            if (ctStatement instanceof CtLocalVariable) {
                if (!((CtVariable) s).getSimpleName().equals(((CtVariable) ctStatement).getSimpleName())) {
                    return false;
                }
            }
            if ((s instanceof CtReturn || s instanceof CtThrow) && !(ctStatement instanceof CtBodyHolder) && !(ctStatement instanceof CtThrow)) {
                CtStatementList parent = s.getParent(CtStatementList.class);
                // cannot remove the last return
                CtStatement lastStatement = s.getParent(CtExecutable.class).getBody().getLastStatement();
                if (parent.getLastStatement() == s && (s.hasParent(lastStatement) || lastStatement.equals(s))) {
                    return false;
                }
            }
            ReachableVariableVisitor variableVisitor = new ReachableVariableVisitor(s);
            Set<String> list = variableVisitor.reachedVariables().stream().map(v -> v.getSimpleName()).collect(Collectors.toSet());
            varLoop:
            for (String variableName: neededVariables) {
                if (!list.contains(variableName)) {
                    return false;
                }
            }
            try {
                return true || !ctStatementToString.equals(s.toString());
            } catch (Exception e) {
                return false;
            }
        }).collect(Collectors.toList());

        Map<List<Integer>, Double> result = distancePredictor.getSimilarityImpl();
        /*TreeSet<List<Integer> output = new TreeSet<>((s1, s2) -> {
            if (!result.containsKey(s1) && result.containsKey(s2)) {
                return 1;
            }
            if (result.containsKey(s1) && !result.containsKey(s2)) {
                return -1;
            }
            if (!result.containsKey(s1) && !result.containsKey(s2)) {
                return 0;
            }
            if (result.get(s1) > result.get(s2)) {
                return -1;
            } else if (result.get(s1) < result.get(s2)) {
                return 1;
            }
            return 0;
        });
        if (options.isEmpty()) {
            return distancePredictor.predict();
        }
        for (int i = 0; i < options.size(); i++) {
            CtStatement option =  options.get(i);
            if (option instanceof CtType) {
                output.add(option.getPosition().getLineContent() - 1);
            } else {
                for (int j = option.getPosition().getLineContent() - 1; j < option.getPosition().getEndLine(); j++) {
                    output.add(j);
                }
            }
        }*/
        return null;
    }
}
