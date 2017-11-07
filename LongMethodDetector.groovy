class LongMethodDetector {
    static ArrayList longMethodTracker = new ArrayList();
    static int openSlot = 0;
    static int methodSignatureLocation = 1;      


    static void main(String[] args) {
        String filePath;
        ListIterator myIterator;

        if (args.length == 0) {
            System.out.println("method Main:  missing filePath argument");
        }
        else {
            filePath = args[0];
            findMethods(filePath);

            if (longMethodTracker.size() != 0) {
                myIterator = longMethodTracker.listIterator();
                System.out.println("\nLong methods found at lines:")
                while(myIterator.hasNext()) {
                    System.out.println(myIterator.next());
                }
            }
        }
    }

    static boolean findMethods(String filePath) {
        File sourceFile = new File(filePath);
        Scanner s = new Scanner(sourceFile).useDelimiter("\\n");
        String scanReturnString;
        int linesInMethod;

        while (s.hasNextLine()) {
            scanReturnString = s.findInLine("[a-zA-Z]+[(][\\s\\S]*[)][\\s{]+");     // searches for text preceeding parenthesis and an open curly brace
            if (scanReturnString != null) {     // if match was found
                System.out.println(scanReturnString + " at line " + methodSignatureLocation);
                linesInMethod = findMethodLength(s);
                checkMethodLength(linesInMethod, methodSignatureLocation);
                methodSignatureLocation += linesInMethod;
            }
            s.nextLine();
            methodSignatureLocation++;
        }

        s.close();
    }

    static void checkMethodLength(int methodLines, int methodSigLoc) {
        if (isTooLong(methodLines)) {
            longMethodTracker[openSlot] = methodSigLoc;
            openSlot++;
        }


    }

    static int findMethodLength(Scanner s) {
        String scanReturnString;
        int linesTraversed;

        while (s.hasNextLine()) {

            linesTraversed += findStartBrace(s);
            scanReturnString = s.findInLine("[}]");
            if (scanReturnString != null) {
                return linesTraversed;
            }
            s.nextLine();
            linesTraversed++;
        }   
    }

    static int findStartBrace(Scanner s) {
        String scanReturnString = s.findInLine("[{]");
        int linesTraversed;

        if (scanReturnString != null) {
            linesTraversed = findMethodLength(s);
        }
        else {
            return linesTraversed;
        }
    }

    static boolean isTooLong(int length) {
        System.out.println("Lines in this method: " + length);
        if (length > 25){
            return true;
        }
        return false;
    }
}