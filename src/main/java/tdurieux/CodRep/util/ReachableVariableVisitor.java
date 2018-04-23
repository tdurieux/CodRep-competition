package tdurieux.CodRep.util;

import spoon.reflect.code.*;
import spoon.reflect.declaration.*;
import spoon.reflect.reference.CtCatchVariableReference;
import spoon.reflect.reference.CtReference;
import spoon.reflect.visitor.CtAbstractVisitor;

import java.util.Collection;
import java.util.HashSet;

import static tdurieux.CodRep.util.SpoonElementLibrary.*;


public class ReachableVariableVisitor extends CtAbstractVisitor {

    public ReachableVariableVisitor(CtElement startingNode) {
        this.startingNode = startingNode;
        this.beforeFilter = new BeforeLocationFilter(CtVariable.class, startingNode().getPosition());
        excludesInstanceFields = false;
    }

    public CtElement startingNode() {
        return startingNode;
    }

    public Collection<CtVariable<?>> reachedVariables() {
        if (reachedVariables == null) {
            reachedVariables = new HashSet<>();
            scan(startingNode());
        }
        return reachedVariables;
    }

    @Override
    public <T> void visitCtThisAccess(CtThisAccess<T> thisAccess) {
        /* this method has to be implemented by the non abstract class */
    }

    @Override
    public void visitCtCodeSnippetStatement(CtCodeSnippetStatement statement) {
		/* ignore any code snippet, only work with what was originally in the code */
    }

    /**
     * Generically scans a collection of meta-model elements.
     */
    public void scan(Collection<? extends CtElement> elements) {
        if (elements != null) {
            for (CtElement e : elements) {
                scan(e);
            }
        }
    }

    /**
     * Generically scans a meta-model element.
     */
    public void superScan(CtElement element) {
        if (element != null) {
            element.accept(this);
        }
    }

    /**
     * Generically scans a meta-model element reference.
     */
    public void scan(CtReference reference) {
        if (reference != null) {
            reference.accept(this);
        }
    }
    public void scan(CtElement element) {
        superScan(element);
        if (hasSafelyReachableParent(element)) {
            scan(element.getParent());
        }
    }

    private boolean hasSafelyReachableParent(CtElement element) {
        if (element == null) {
            return false;
        }
        CtElement parent = element.getParent();
        if (parent != null) {
            return !(parent instanceof CtPackage) || !(isAnonymousClass(element) || isInitializationBlock(element) || isConstructor(element) || isSimpleType(element) && isBlock(parent));
        }
        return false;
    }

    @Override
    public <R> void visitCtBlock(CtBlock<R> block) {
        scanElementsIn(block.getStatements());
    }

    @Override
    public <T> void visitCtConstructor(CtConstructor<T> c) {
        scanElementsIn(c.getParameters());
    }

    @Override
    public <T> void visitCtMethod(CtMethod<T> method) {
        scanElementsIn(method.getParameters());
    }

    @Override
    public <T> void visitCtClass(CtClass<T> ctClass) {
        scanElementsIn(ctClass.getFields());
    }

    @Override
    public <T> void visitCtLocalVariable(CtLocalVariable<T> localVariable) {
        if (isReachable(localVariable)) {
            reachedVariables().add(localVariable);
        }
    }

    @Override
    public <T> void visitCtCatchVariableReference(
            CtCatchVariableReference<T> localVariable) {
		/* Ignore catch variable */
        reachedVariables().add(localVariable.getDeclaration());
    }

    @Override
    public <T> void visitCtParameter(CtParameter<T> parameter) {
        reachedVariables().add(parameter);
    }

    @Override
    public void visitCtFor(CtFor forLoop) {
        forLoop.getForInit().forEach(s -> {
            if (s instanceof CtVariable) {
                if (isReachable((CtVariable<?>) s)) {
                    reachedVariables().add((CtVariable<?>) s);
                }
            }
        });
    }

    @Override
    public void visitCtForEach(CtForEach foreach) {
        if (isReachable(foreach.getVariable())) {
            reachedVariables().add(foreach.getVariable());
        }
    }

    @Override
    public <T> void visitCtField(CtField<T> field) {
        if (!excludesInstanceFields() || hasStaticModifier(field)) {
            reachedVariables().add(field);
        }
    }

    private void scanElementsIn(Collection<? extends CtElement> elements) {
        for (CtElement element : elements) {
            superScan(element);
        }
    }

    private boolean isReachable(CtVariable<?> variable) {
        return beforeFilter().matches(variable);
    }

    private boolean excludesInstanceFields() {
        return excludesInstanceFields;
    }

    private BeforeLocationFilter<CtVariable<?>> beforeFilter() {
        return beforeFilter;
    }

    private CtElement startingNode;
    private boolean excludesInstanceFields;
    private Collection<CtVariable<?>> reachedVariables;
    private BeforeLocationFilter<CtVariable<?>> beforeFilter;
}
