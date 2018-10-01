package tdurieux.CodRep.util;

import org.eclipse.jdt.core.compiler.CategorizedProblem;
import spoon.Launcher;
import spoon.reflect.CtModel;
import spoon.reflect.code.CtBlock;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtConstructor;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtParameter;
import spoon.reflect.declaration.CtTypeMember;
import spoon.reflect.declaration.ModifierKind;
import spoon.reflect.factory.Factory;
import spoon.reflect.reference.CtTypeReference;
import spoon.support.compiler.VirtualFile;
import spoon.support.compiler.jdt.JDTBasedSpoonCompiler;
import tdurieux.CodRep.context.LineContext;
import tdurieux.CodRep.context.Parser;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class SpoonUtil {

    public static String[] splitLine(String fileContent) {
        return fileContent.split("\\r?\\n");
    }

    public static List<LineContext> splitLineToMap(String fileContent) {
        Parser parser = new Parser();
        Map<String, LineContext> output = new HashMap<>();
        String[] split = fileContent.split("\\r?\\n");
        int lineNumber = 0;
        for (String line : split) {
            line = line.trim();
            LineContext context = parser.parse(line);
            if (!output.containsKey(line)) {
                output.put(line, context);
            }
            output.get(line).getLineNumbers().add(lineNumber);
            lineNumber++;
        }
        return new ArrayList<>(output.values());
    }

    public static Launcher getModelFromString(String contents) {
        Launcher launcher = new Launcher();
        launcher.addInputResource(new VirtualFile(contents));
        launcher.getEnvironment().setNoClasspath(true);
        launcher.getEnvironment().setAutoImports(true);
        launcher.getEnvironment().setCommentEnabled(true);
        launcher.buildModel();
        return launcher;
    }

    public static List<CategorizedProblem> filterProblem(List<CategorizedProblem> input, int line) {
        List<CategorizedProblem> problems = new ArrayList<>();
        for (int i = 0; i < input.size(); i++) {
            CategorizedProblem problem = input.get(i);
            if (problem.getSourceLineNumber() - 1 == line) {
                problems.add(problem);
            }
        }
        return problems;
    }

    public static CtElement getAstFromLine(String line) {
        Launcher launcher = new Launcher();
        Factory f = launcher.getFactory();
        CtClass<?> w = f.Class().create("Wrapper");
        Set<ModifierKind> modifiers = EnumSet.of(ModifierKind.STATIC);
        Set<CtTypeReference<? extends Throwable>> thrownTypes = new HashSet<>();
        CtClass<Throwable> exception = f.Class().get(Throwable.class);
        thrownTypes.add(exception.getReference());
        CtTypeReference<?> returnType = f.Type().VOID_PRIMITIVE;

        line = line.trim();
        if (line.contains("{") && !line.contains("}")) {
            line += "}";
        }
        if (line.contains(")") && !line.contains("(")) {
            return null;
        }
        if (line.contains("(") && !line.contains(")")) {
            return null;
        }
        if (line.startsWith("return")) {
            returnType = f.Type().OBJECT;
        }
        if (line.startsWith("} else ")) {
            line = line.substring(6);
        }
        if (line.startsWith("+")) {
            // HAAAA
            return null;
        }
        if (line.startsWith("import")) {
            return null;
        }
        if (line.startsWith("@")) {
            return null;
        }
        if (line.startsWith("#")) {
            return null;
        }

        if (line.contains("super(") || line.contains("this(")) {
            String wrapInClass = "abstract class Wrapper { Wrapper() {" + line + "}}";
            try {
                CtModel modelFromString = getModelFromString(wrapInClass).getModel();
                return modelFromString.getRootPackage().getFactory().Class().get("Wrapper").getConstructor().getBody().getStatement(0);
            } catch (Exception ignore) {
            }
        }

        if (line.contains("public")
                || line.contains("void")
                || line.contains("static")
                || line.contains("abstract")
                || line.contains("final")
                || line.contains("private")) {
            String wrapInClass = "abstract class Wrapper {" + line + "}";
            try {
                CtModel modelFromString = getModelFromString(wrapInClass).getModel();
                List<CtTypeMember> elements = modelFromString.getRootPackage().getFactory().Class().get("Wrapper").getTypeMembers().stream().filter(t -> !(t instanceof CtConstructor)).collect(Collectors.toList());
                if (!elements.isEmpty()) {
                    return elements.get(0);
                }
            } catch (Exception ignore) {
            }
        }

        CtBlock body = f.createCtBlock(f.createCodeSnippetStatement(line));


        f.createMethod(w, modifiers, returnType, "wrap", new ArrayList<CtParameter<?>>(), thrownTypes, body);

        String contents = w.toString();
        w.getPackage().removeType(w);
        launcher.addInputResource(new VirtualFile(contents));
        launcher.getEnvironment().setNoClasspath(true);
        launcher.getEnvironment().setAutoImports(true);
        launcher.buildModel();
        return launcher.getFactory().Class().get("Wrapper").getMethod("wrap").getBody().getStatement(0);
    }
}
