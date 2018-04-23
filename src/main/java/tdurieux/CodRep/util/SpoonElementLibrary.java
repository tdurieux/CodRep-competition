package tdurieux.CodRep.util;

import spoon.reflect.code.*;
import spoon.reflect.declaration.*;
import spoon.reflect.factory.Factory;
import spoon.reflect.reference.CtReference;
import spoon.reflect.reference.CtTypeReference;
import spoon.reflect.visitor.Filter;
import spoon.reflect.visitor.Query;
import spoon.reflect.visitor.filter.TypeFilter;

import java.util.List;


public class SpoonElementLibrary {

    public static <T extends CtElement> List<T> allChildrenOf(CtElement rootElement, Class<T> childrenClasses) {
        return Query.getElements(rootElement, new TypeFilter<T>(childrenClasses));
    }

    public static <T extends CtElement> List<T> filteredElements(Factory factory, Filter<T> filter) {
        return Query.getElements(factory, filter);
    }

    public static <T extends CtElement> boolean hasChildrenOfType(CtElement rootElement, Class<T> childrenClass) {
        return !allChildrenOf(rootElement, childrenClass).isEmpty();
    }

    public static <T extends CtElement> T parentOfType(Class<T> parentType, CtElement element) {
        return element.getParent(parentType);
    }

    public static <T extends CtElement> boolean hasParentOfType(Class<T> parentType, CtElement element) {
        return parentOfType(parentType, element) != null;
    }

    public static boolean haveSamePackage(CtElement one, CtElement other) {
        String onePackage = packageOf(one).getQualifiedName();
        String otherPackage = packageOf(other).getQualifiedName();
        return onePackage.equals(otherPackage);
    }

    public static boolean isBlock(CtElement element) {
        return element instanceof CtBlock;
    }

    public static boolean isMethod(CtElement element) {
        return element instanceof CtMethod;
    }

    public static boolean isLocalVariable(CtElement element) {
        return element instanceof CtLocalVariable;
    }

    public static boolean isParameter(CtElement element) {
        return element instanceof CtParameter;
    }

    public static boolean isAnonymousClass(CtElement element) {
        return element instanceof CtNewClass;
    }

    public static boolean isConstructor(CtElement element) {
        return element instanceof CtConstructor;
    }

    public static boolean isInitializationBlock(CtElement element) {
        return element instanceof CtAnonymousExecutable ;
    }

    public static boolean isSimpleType(CtElement element) {
        return element instanceof CtType;
    }

    public static boolean isType(CtElement element) {
        return element instanceof CtType;
    }

    public static boolean isInterface(CtElement element) {
        return element instanceof CtInterface;
    }

    public static boolean isANestedType(CtElement element) {
        return isSimpleType(element) && hasParentOfType(CtType.class, element);
    }

    public static boolean isTypedElement(CtElement element) {
        return element instanceof CtTypedElement;
    }

    public static boolean isField(CtElement element) {
        return element instanceof CtField;
    }

    public static boolean isFieldAccess(CtElement element) {
        return element instanceof CtFieldAccess;
    }

    public static boolean isReference(CtElement element) {
        return element instanceof CtReference;
    }

    public static boolean isStatement(CtElement element) {
        return element instanceof CtStatement;
    }

    public static boolean isReturnStatement(CtElement element) {
        return element instanceof CtReturn;
    }

    public static boolean allowsModifiers(CtElement element) {
        return element instanceof CtModifiable;
    }

    public static CtPackage packageOf(CtElement element) {
        return element.getParent(CtPackage.class);
    }

    public static CtTypeReference<?> typeOf(CtElement element) {
        if (isSimpleType(element)) {
            return ((CtType<?>) element).getReference();
        }
        if (isTypedElement(element)) {
            return ((CtTypedElement<?>) element).getType();
        }
        return typeOf(parentOfType(CtType.class, element));
    }

    public static boolean hasStaticModifier(CtElement element) {
        return hasModifier(element, ModifierKind.STATIC);
    }

    public static boolean hasPublicModifier(CtElement element) {
        return hasModifier(element, ModifierKind.PUBLIC);
    }

    public static boolean hasPrivateModifier(CtElement element) {
        return hasModifier(element, ModifierKind.PRIVATE);
    }

    public static boolean hasProtectedModifier(CtElement element) {
        return hasModifier(element, ModifierKind.PROTECTED);
    }

    public static boolean hasNoVisibilityModifier(CtElement element) {
        return !(hasPublicModifier(element) || hasPrivateModifier(element) || hasProtectedModifier(element));
    }

    public static boolean hasModifier(CtElement element, ModifierKind kind) {
        if (allowsModifiers(element)) {
            return ((CtModifiable) element).hasModifier(kind);
        }
        return false;
    }

    public static boolean inStaticCode(CtElement element) {
        if (allowsModifiers(element)) {
            boolean inStatic = hasStaticModifier(element);
            if (isANestedType(element)) {
                inStatic |= inStaticCode(parentOfType(CtType.class, element));
            }
            return inStatic;
        }
        return hasStaticModifier(element.getParent(CtModifiable.class)) || inStaticCode(element.getParent(CtType.class));
    }
}
