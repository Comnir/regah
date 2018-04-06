
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

A web GUI can be started with the startWebGui task:
<pre>gradlew startWebGui</pre>

### Git hooks (Linux/Mac)

A git pre-commit hook can be 'installed' by executing
  ln -s ../../hooks/pre-commit.sh pre-commit

From .git/hooks folder.
