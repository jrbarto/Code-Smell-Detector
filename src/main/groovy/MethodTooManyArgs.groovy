import com.codeparser.Driver

def filename = args[0]
int maxArgs = 3

// The Bracket class should be created only once per filename or it be
// exponentially slower because we are reading from the file unnecessary times 
// Therefore, the caller of this method should have the object and lines[].
// Note: This method is a okay for sandbox but not for production.

Driver.mTooManyArgs(filename, maxArgs)