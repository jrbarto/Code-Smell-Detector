# Code-Smell-Detector
A group project for senior design to detect code smells and report on them using scripts/patterns.
Steps to make a commit:
1) Clone this repository using git clone (repoUrlHere)
2) Make some code changes.
3) Do a 'git add .' to add your changes to the current commit.
4) Do a 'git commit -m (some message here)' to create your commit.
5) Finally' 'git push' to push the new commit to the repository.

Quick code description:
CodeSniffRunner is the main class that will execute the FileParser and GHRestClient.
The FileParser will kick off a new groovy process using the ProcessHelper to execute a groovy script.
The GHRestClient will be our access to the Github REST API. All REST commands will go in there. So far I've only added authentication.
