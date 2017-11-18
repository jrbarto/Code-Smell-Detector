import com.codeparser.Driver

def filename = args[0]
int maxArgs = 3

// A Bracket object should be created only once per filename or it will be
// exponentially slower because we are reading from the disk unnecessary times. 
// Therefore, the caller of this method should pass the object and lines[].
// Note: This method is a okay for sandbox but not for production.

Driver.mTooManyArgs(filename, maxArgs)