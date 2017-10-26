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
- The GHRestClient will be our access to the Github REST API. All REST commands will go in there. So far I've only added authentication.

Building the code:
- You must install the 'ant' build tool. Here is in depth documentation on how to do so on Windows: https://www.mkyong.com/ant/how-to-install-apache-ant-on-windows/ On Linux: http://how-to.cc/install-ant-on-linux
- Once you have ant installed, go to the main directory of the CodeSniffer project. ('./Code-Smell-Detector') and enter the command 'ant'.
- The ant command will run the 'main' target in the build.xml file, which will compile the code and package it into a jar file in the './Code-Smell-Detector/dist' directory.

Running the code (this is for testing, and used for testing since we haven't implemented it as a github application yet):
- Prerequisites:
  - You must have groovy and java installed. (Also you should try to install a jdk version of java if you can, because you will have trouble tracking errors if you don't)
- Navigate to the './CodeSmellDetector/dist' directory and you will find a 'CodeSniffer-<timestamp>.jar' file.
- Execute the command 'java -jar CodeSniffer-<timestamp>.jar <arg1> <arg2>' to execute the jar file.
- The <arg1> and <arg2> arguments are passed to the main class of the jar file (which is CodeSniffRunner). <arg1> is the full path of the groovy file that you will be using to analyze <arg2>. Hence, <arg2> is the source file that you will be analyzing.
- A process will be kicked off on the command line (It will be executing 'groovy <arg1> <arg2>' (remember that arg1 is your groovy script and arg2 is the source file to analyze)
- This means that the groovy script will take in an argument (<arg2>). So, write your scripts to accept a file argument for now.
- This code will allow you to test out parsing a file using a groovy script to find code smells.
