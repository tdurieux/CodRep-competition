package tdurieux.CodRep.context;

import com.sun.org.apache.bcel.internal.generic.NEW;
import com.sun.org.apache.bcel.internal.generic.RETURN;
import com.sun.org.apache.bcel.internal.generic.SWITCH;
import org.omg.CORBA.TRANSIENT;

import java.util.Arrays;
import java.util.List;

public enum Keywords {
    IMPORT,
    PACKAGE,
    INTERFACE,
    CLASS,
    IMPLEMENTS,
    ENUM,
    SUPER,
    THIS,
    ASSERT,
    EXTENDS,
    BREAK,
    CASE,
    CATCH,
    CONTINUE,
    DO,
    FOR,
    IF,

    NEW,
    RETURN,
    SWITCH,
    THROW,
    TRY,
    WHILE,

    PUBLIC, PROTECTED, PRIVATE, ABSTRACT, STATIC, FINAL, TRANSIENT, VOLATILE, SYNCHRONIZED, NATIVE, STRICTFP,

    INT, DOUBLE, FLOAT, LONG, CHAR, BOOLEAN, BYTE, FALSE, TRUE, VOID,

    NULL;

    public static List<Keywords> modifiers() {
        return Arrays.asList(PUBLIC, PROTECTED, PRIVATE, ABSTRACT, STATIC, FINAL, TRANSIENT, VOLATILE, SYNCHRONIZED, NATIVE, STRICTFP);
    }

    public static List<Keywords> primitives() {
        return Arrays.asList(INT, DOUBLE, FLOAT, LONG, CHAR, VOID, BOOLEAN, BYTE);
    }
}
