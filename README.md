x
#  Regah - usable, but not there yet - see Basics To-Do list

~~Regah - Not in a usable state yet~~

(Will be, one day,) A file sharing service / application.
oooo
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
1. The Sharing server and client can be started with the run task: <pre>gradlew run</pre>
1. An Electron based GUI can be started with: <pre>gradlew startGui</pre>
1. To allow debugging of Electron main process, start with:<pre>gradlew startGuiDebug</pre> And connect with Chrome remte debugging from 'chrome://inspect' page.

### Git hooks (Linux/Mac)

A git pre-commit hook is installed when executing 'build' with Gradle.

 It can also be 'installed' by executing 'copyPreCommitHook' Gradle task
 or by running
  ln -s ../../hooks/pre-commit pre-commit

from .git/hooks folder.

## To-Do / Wish list
### Basics
1. Write logs to file
   1. ~~Java side~~
   1. ~~Electron side~~
   1. Store all log files in a single folder
1. ~~Use configuration files: ports, data folder, etc.~~
1. Persist data: ~~shared files~~, progress, etc.

### Feature-wise
1. Improve download preparation: don't re-create download data for files which were previously download
1. Support downloading from multiple sources which have the same files
