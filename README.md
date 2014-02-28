VENUtils
========
utils to simplify some work

* GitCompareBranches
compares not only git commits, but also diff of files between 2 branches. 

usage: groovy GitCompareBranches.groovy [2 branches] [git repo]

i.e.:  groovy GitCompareBranches.groovy origin/master..origin/development /path/to/git/repo
