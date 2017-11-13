import com.codeparser.Driver
import com.codeparser.Bracket
import com.codeparser.bCodeParser

def filename = args[0]
Bracket globalBracket = new Bracket(filename)
Bracket[] children = globalBracket.getChildren()

// test too many args
int maxArgs = 3
for (child in children) {
    child.print()
    Driver.m2ManyArgs(child, bCodeParser.fileToLines(filename), maxArgs)
}
