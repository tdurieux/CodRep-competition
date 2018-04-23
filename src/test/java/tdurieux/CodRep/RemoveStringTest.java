package tdurieux.CodRep;

import org.junit.Assert;
import org.junit.Test;

public class RemoveStringTest {

    @Test
    public void test() {
        String str = "test";
        Assert.assertEquals("test", removeString(str));

        str = "'t'";
        Assert.assertEquals("", removeString(str));

        str = "\\\"test\\\"";
        Assert.assertEquals("\\\"test\\\"", removeString(str));

    }

    private int indexOfString(String line, char stringStart, int start) {
        int indexStringStart = line.indexOf(stringStart, start);
        if (indexStringStart == -1) {
            return -1;
        }
        if (indexStringStart > 0 && line.charAt(indexStringStart - 1) == '\\') {
            return indexOfString(line, stringStart, indexStringStart + 1);
        }
        return indexStringStart;
    }
    private String removeString(String line) {
        char stringStart = '"';
        int indexStringStart = indexOfString(line, stringStart, 0);
        if (indexStringStart == -1) {
            stringStart = '\'';
            indexStringStart = indexOfString(line, stringStart, 0);
        }
        if (indexStringStart == -1) {
            return line;
        }
        int indexStringEnd = indexOfString(line, stringStart, indexStringStart + 1);
        if (indexStringEnd == -1) {
            return line.substring(indexStringStart);
        }
        String start = line.substring(0, indexStringStart);
        String end = line.substring(indexStringEnd + 1);

        return removeString(start + end);
    }
}
