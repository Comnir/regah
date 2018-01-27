#!/bin/sh

# stash any unstaged changes - we want to verify commited state
git stash -q --keep-index

echo "Running tests with Gradle from folder " + `pwd`
./gradlew test --daemon

# store the exit code of the test run
RESULT=$?

# unstash changes
git stash pop -q

exit $RESULT
