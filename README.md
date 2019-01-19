
#  Regah - Not in a usable state yet

(Will be, one day,) A file sharing service / application.

## The plan
<pre>
 ____________
|            |======= Managment API (add/remove/list shared files)
|   Sharing  |
|    server  |                                                
|____________|======= Serving API (list/fetch shared files)
                       |      /|\                
                       |       |
                      \|/      |
                     _____________
                     |           |
                     |  Sharing  |
                     |   Client  |
                     |___________|
 
</pre>

## Running
The Sharing server and client can be started with the run task:
<pre>gradlew run</pre>

An Electron based GUI can be started with:
<pre>gradlew startGui</pre>

### Git hooks (Linux/Mac)

A git pre-commit hook is installed when executing 'build' with Gradle.

 It can also be 'installed' by executing 'copyPreCommitHook' Gradle task
 or by running
  ln -s ../../hooks/pre-commit pre-commit

from .git/hooks folder.
