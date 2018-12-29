
#  Electron based gui


Resources / References:
---

[HTTP requests with Electron](https://electronjs.org/docs/api/client-request)
[Electron 'net'](https://electronjs.org/docs/api/net)
Initially, tried [Node.js http requests](https://nodejs.org/api/http.html), but it had a wierd delay before the request actually arrived at the server.
The ['net' specs]([https://github.com/electron/electron/blob/master/spec/api-net-spec.js]) provide more usage examples.

[Electron's IPC - main /renderers](https://medium.com/@hamzasurti/in-progress-6959b733a55a)

Main takes:
- some events can only be executed on the main thread.
- Renderers will dispatch events with ipc.send(<tag>[,parameters]).
- Event handlers are registered with ipc.on(<tag>, function(event[,args...])).
- Main thread can send data back to the renderer for further manipulation. This can be done with event.sender.send(<tag>[,parameters]).

[Sound Machine](https://github.com/bojzi/sound-machine-electron-guide) - relatively simple example of electron application with multiple windows.

Main takes:
- basic project structure:
    - main.js - loaded by package.json
    - index.html - loaded by main.js
    - index.js - JS code for index.html
- setting event listeners in JS file:
    - get the element from the document (e.g., var e = document.getElementById)
    - add an event listener e.addEventListener('click', function...)


On linux, starting GUI might fail with
  regah/gui/electron/node_modules/electron/dist/electron: error while loading shared libraries: libgconf-2.so.4: cannot open shared object file: No such file or directory

Install the missing library with
  sudo apt-get install libgconf-2-4

