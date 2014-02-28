package com.chowaniok

import spock.lang.Specification
/**
 * User: Marek Chowaniok
 * Date: 28/02/14
 */
class GitCompareBranchesTest extends Specification {

    GitCompareBranches gitCompareBranches

    void setup() {
        gitCompareBranches = Spy(GitCompareBranches)
        gitCompareBranches.executeOnShell(_) >> {String message ->
            if (message.contains("git log --oneline  --no-merges")) {
                return "fc23f7e VEN-28533 remove synchronization on send, put back sync on session\n" +
                        "1877214 VEN-28533 JMS engine on netweaver 7.3+ may fail when invoked during http session invalidation\n" +
                        "cfd8f72 VEN-28666 - added check whether the report is for Excel or whether it is Analysis report"
            }else if (message.contains("git diff-tree --no-commit-id --name-only -r")) {
                return "src/com/vendavo/platform/cache/CacheMessage.java\n" +
                        "src/com/vendavo/platform/cluster/JMSCacheListener.java\n" +
                        "src/com/vendavo/platform/cluster/JMSClusterManager.java"
            } else if (message.contains("git diff -w")) {
                message.size() == 61 ? "diff --git a/src/com/vendavo/..." : ""
            }
        }
    }

    void cleanup() {
        gitCompareBranches = null
    }

    def "testing retrieving CommitIDs"() {
        when:
        gitCompareBranches.retrieveCommitHashes()

        then:
        gitCompareBranches.hashes.size() == 3
        gitCompareBranches.hashes.get(0).getHashID() == "fc23f7e"
        gitCompareBranches.hashes.get(0).getJiraID() == "VEN-28533"
    }

    def 'testing list of changed files'() {
        setup:
        gitCompareBranches.retrieveCommitHashes()

        when:
        gitCompareBranches.retrieveNameOfFiles()

        then:
        gitCompareBranches.changedFiles.size() == 9
        gitCompareBranches.changedFiles.get(0).getChangedFile() == "src/com/vendavo/platform/cache/CacheMessage.java"
        gitCompareBranches.changedFiles.get(0).getHashID() == "fc23f7e"
        gitCompareBranches.changedFiles.get(0).getJiraID() == "VEN-28533"


    }

    def 'is file same on both branches'() {
        setup:
        gitCompareBranches.retrieveCommitHashes()
        gitCompareBranches.retrieveNameOfFiles()

        when:
        def correctCommits = gitCompareBranches.compareFileOnBothBranches()

        then:
        correctCommits.size() == 3
        correctCommits.containsAll("fc23f7e","1877214","cfd8f72")
    }
}
