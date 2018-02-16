import com.codeparser.*
import com.codeparser.bCodeParser
import java.util.ArrayList
import java.nio.file.Path
import java.nio.file.Paths

class LongMethodSniffer {
    static Path file_path;
    static Bracket parent_bracket;
    static Bracket child_bracket;
    static Bracket[] child_bracket_array;
    static ArrayList long_methods_array = new ArrayList();
    static final int MAX_METHOD_LENGTH = 25;

    public static void main(String[] args) {
        String input_file_name = args[0];
        Bracket main_bracket;
        Bracket[] original_bracket_array = new Bracket[1];
        file_path = Paths.get(input_file_name);


        original_bracket_array[0] = new Bracket(input_file_name);
        findMethods(original_bracket_array);

        // Print results to console
        if (long_methods_array.size() > 0) {
            ListIterator li = long_methods_array.listIterator();
            while (li.hasNext()) {
                System.out.print(li.next().getLineNumber() + ",");
            }
        }
    }

    static boolean bracketHasChidren(Bracket b) {
        parent_bracket = b;
        child_bracket_array = parent_bracket.getChildren();

        if (child_bracket_array.length == 0) {
            return false;
        }
        else {
            return true;
        }

    }

    static void findMethods(Bracket[] bracket_array) {
        for (current_bracket in bracket_array) {
            if (bCodeParser.isMethod(current_bracket.getHead())) {
                // Check whether method is long or not
                    // yes: Make new LongMethod obj and add to array
                    // no: continue
                
                if (isLongMethod(current_bracket)) {
                    int method_location = findMethodLocation(current_bracket);
                    LongMethod lm = new LongMethod(method_location);
                    long_methods_array.add(lm);
                }
            }
            else {
                // Check for child brackets
                // Add any to a bracket array and call recursive function to findMethods

                if (bracketHasChidren(current_bracket)) {
                    Bracket[] discovered_brackets = current_bracket.getChildren();
                    findMethods(discovered_brackets);
                }
            }
        }
    }

    static boolean isLongMethod(Bracket b) {
        String[] method_body = b.getBody().split("\n");
        int method_lines = method_body.length;
        if (method_lines > MAX_METHOD_LENGTH) {
            return true;
        }
        return false;
    }

    static int findMethodLocation(Bracket b) {
        Scanner scnr = new Scanner(file_path);
        int line_number = 0;

        while (scnr.hasNextLine()) {
            String current_line = scnr.nextLine();
            line_number++;
            // DEBUG
            // System.out.println(current_line.trim() + "\n" + b.getHead().trim() + "\n");
            //
            if (current_line.trim().contains(b.getHead().trim())) {
                return line_number;
            }
        }
        return -1;
    }
}

class LongMethod {
    int lineNumber;

    LongMethod(int ln) {
        this.lineNumber = ln;
    }

    int getLineNumber() {
        return this.lineNumber;
    }
}
