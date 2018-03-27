import com.codeparser.*
import com.codeparser.bCodeParser
import java.util.ArrayList
import java.nio.file.Path
import java.nio.file.Paths

class LargeClassSniffer {
    static ArrayList<Bracket> class_list = new ArrayList();
    static ArrayList<Bracket> all_brackets = new ArrayList();

    public static void main(String[] args) {

        String input_file_name = args[0];
        Path file_path = Paths.get(input_file_name);
        Bracket top_bracket = new Bracket(input_file_name);
        final int CLASS_LENGTH_THRESHOLD = 200;

        findBrackets(top_bracket);
        for (Bracket bracket:all_brackets) {
            if (isClass(bracket.getHead())) {
                class_list.add(bracket);
            }
        }
        for (Bracket class_bracket:class_list) {
            if (isLargeClass(class_bracket, CLASS_LENGTH_THRESHOLD)) {
                System.out.print(getLineNumber(class_bracket, file_path) + ",");
            }
        }
    }

    static Bracket[] findBrackets(Bracket b) {
        if (b.getMax_depth() > 0) {
            Bracket[] array = b.getChildren();
            for (Bracket bracket:array) {
                all_brackets.add(bracket);
                findBrackets(bracket);
            }
        }
    }

    static boolean isClass(String s) {
        return s.trim().matches(".*class\\s+\\w+.*");
    }

    static boolean isLargeClass(Bracket b, int threshold) {
        String[] body = b.getBody().split("\n");
        int total_lines = body.length;
        if (total_lines > threshold) {
            return true;
        }
        return false;
    }

    static int getLineNumber(Bracket b, Path p) {
        Scanner s = new Scanner(p);
        int line_num = 0;

        while (s.hasNextLine()) {
            String current_line = s.nextLine();
            line_num++;
            if (current_line.trim().contains(b.getHead().trim())) {
                return line_num;
            }
        }
        return -1;
    }
}