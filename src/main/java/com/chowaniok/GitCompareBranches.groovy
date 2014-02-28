package com.chowaniok

/**
 * User: Marek Chowaniok
 * Date: 28/02/14
 */
class GitCompareBranches {
    def gitRepoPath = ""
    def branchNames = ""
    def hashes = []
    def changedFiles = []

    def run(args) {
        init(args)
        retrieveCommitHashes()
        retrieveNameOfFiles()
        compareFileOnBothBranches()
    }


    private void init(args) {
        try {
            if (args[0]?.size() > 0) {
                branchNames = args[0]
            } else {
                throw new InvalidPropertiesFormatException("branch names not defined i.e. groovy GitCompareBranches.groovy  origin/master..origin/development  /path/to/git/repo")
            }

            if (args[1]?.size() > 0) {
                gitRepoPath = args[1]
            } else {
                println("Path to git repo not defined -i.e. groovy GitCompareBranches.groovy  origin/master..origin/development  /path/to/git/repo")
                println("Checking properties file")
                def config = new ConfigSlurper().parse(getClass().getClassLoader().getResource("private.properties"))
                gitRepoPath = config.git.gitRepoPath
                if (gitRepoPath.size() == 0) {
                    throw new InvalidPropertiesFormatException("Path to git repo is not set in private.properties - i.e. git.gitRepoPath = /path/to/my/git/repo")
                }
            }
        } catch (Exception e) {
            throw new InvalidPropertiesFormatException("Make sure your gitRepoPath and branch name are correct! i.e. groovy GitCompareBranches.groovy  origin/master..origin/development  /path/to/git/repo")
        }
    }

    def retrieveCommitHashes() {
        def checkBranchesForDifferentCommits = executeOnShell("git log --oneline  --no-merges ".concat(branchNames))
        checkBranchesForDifferentCommits.eachLine {
            def commitHashID = new HashIDWithJiraID()
            commitHashID.setHashID(it.substring(0, 7))
            commitHashID.setJiraID(it.substring(8, it.length() < 17 ? it.length() : 17))
            hashes.add(commitHashID)
        }
    }

    def retrieveNameOfFiles() {
        hashes.each {
            def gitCommand = "git diff-tree --no-commit-id --name-only -r ".concat(it.getHashID())
            def changedFilesFromGit = executeOnShell(gitCommand)
            changedFilesFromGit.eachLine { it2 ->
                def changedFilesWithingCommit = new ChangedFilesWithHashID()
                changedFilesWithingCommit.setChangedFile(it2)
                changedFilesWithingCommit.setHashID(it.getHashID())
                changedFilesWithingCommit.setJiraID(it.getJiraID())
                changedFiles.add(changedFilesWithingCommit)
            }
        }
    }

    def compareFileOnBothBranches() {
        def listofCorrectCommits = [] as Set
        changedFiles.each {
            def gitCommand = "git diff -w ".concat(branchNames).concat(" ").concat(it.getChangedFile())
            def isFileDifferent = executeOnShell(gitCommand)
            if (isFileDifferent.size() > 0) {
                println("${it.getHashID()} - ${it.getJiraID()} - ${it.getChangedFile()} \t - ${gitCommand}")
            } else {
                listofCorrectCommits.add(it.getHashID())
            }
        }
        println("********* CORRECT ***********")
        listofCorrectCommits.each { println(it) }
    }

    def executeOnShell(String command) {
        return executeOnShell(command, new File(gitRepoPath))
    }

    def executeOnShell(String command, File workingDir) {
        String result = ""
        def process = new ProcessBuilder(addShellPrefix(command))
                .directory(workingDir)
                .redirectErrorStream(true)
                .start()

        process.inputStream.eachLine {
            result = result.concat(it).concat("\n")
        }
        process.waitFor();
        return result
    }

    private def addShellPrefix(String command) {
        def commandArray = new String[3]
        commandArray[0] = "sh"
        commandArray[1] = "-c"
        commandArray[2] = command
        return commandArray
    }



    public static void main(String[] args) {
        new GitCompareBranches().run(args)
    }
}

class ChangedFilesWithHashID {
    def changedFile
    def hashID
    def jiraID
}

class HashIDWithJiraID {
    def jiraID
    def hashID
}
