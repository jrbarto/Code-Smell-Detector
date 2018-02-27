# Code-Smell-Detector
A group project for senior design to detect code smells and report on them using scripts/patterns.
Steps to make a commit:
1) Clone this repository using git clone (repoUrlHere)
2) Make some code changes.
3) Do a 'git add .' to add your changes to the current commit.
4) Do a 'git commit -m (some message here)' to create your commit.
5) Finally' 'git push' to push the new commit to the repository.

Quick code description:
- CodeSniffRunner is the main class that will execute the FileParser and GHRestClient.
- The FileParser will kick off a new groovy process using the ProcessHelper to execute a groovy script.
- The GHRestClient will be our access to the Github REST API. All REST commands will go in there.

Building the code:
- You must install the 'ant' build tool. Here is in depth documentation on how to do so on Windows: https://www.mkyong.com/ant/how-to-install-apache-ant-on-windows/ On Linux: http://how-to.cc/install-ant-on-linux
- Once you have ant installed, go to the main directory of the CodeSniffer project. ('./Code-Smell-Detector') and enter the command 'ant'.
- The ant command will run the 'main' target in the build.xml file, which will compile the code and package it into a jar file in the './Code-Smell-Detector/dist' directory.

Running the code (this is solely for testing purposes currently since we haven't implemented it as a github application yet):
- Prerequisites:
  - You must have groovy and java installed. (Also you should try to install a jdk version of java if you can, because you will have trouble tracking errors if you don't)
- Navigate to the './CodeSmellDetector/dist' directory and you will find a 'CodeSniffer-`<timestamp>`.jar' file.
- Execute the command 'java -jar CodeSniffer-`<timestamp>`.jar `<arg1>` `<arg2> <arg3>` `<arg4>` `<arg5>` `<arg6>` `<arg7>` to execute the jar file.
- The `<arg1>`, `<arg2>`, `<arg3`, `<arg4>`, `<arg5>`, `<arg6>`, and `<arg7>` arguments are passed to the main class of the jar file (which is CodeSniffRunner). `<arg1>` is the full path of the groovy file that you will be using to analyze all files in the most recent pull request on a github repository. Hence, `<arg2>` is the owner of the repository to examine, `<arg3>` is the name of the repository, `<arg4>` is the username to login to github with, `<arg5>` is that user's password, `<arg6>` is the comment to post (a string such as "Too many arguments here."), and `<arg7>` is either true or false to determine if a full repository review will be made.
- If you forget an argument, you will see an error: [Solution] Please run in the format 'CodeSniffer.jar /path/to/groovyfile owner repo username password comment true/false'
- For example, I have created a sample repo to test with, and to analyze the most recent pull request to that repo I would do: 'java -jar dist/CodeSniffRunner.jar ../src/main/groovy/MethodTooManyArgs.groovy jrbarto Sample-Repo snifftestuser password123 "This method has too many arguments." false' and this will run the MethodTooManyArgs.groovy script on all files in the most recent pull request of the Sample-Repo repository on Github whose owner is jrbarto. The username is snifftestuser and password is password123. The false value means that this is not a full review, and instead a pull request review will be executed on the most recent pull request.
- The groovy script will take in a single source file as an argument. The main program will execute the script for each file in the pull request. So, groovy scripts must accept a single file argument.
- A process will be kicked off on the command line for each source file discovered in the most recent pull request (It will be executing 'groovy `<arg1>` `<src_file_n>`' for each of the 1 to n source files). We will probably run some of these concurrently in the future, but for now they are executed in linear fashion.
- This code will parse all files in a GitHub pull request using a groovy script to find code smells (The abridged version of what I've said above).
