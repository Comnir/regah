"use strict";
var ipcRenderer = require('electron').ipcRenderer;

var inputTargetAddress = document.getElementById('input-target-address');
var fetchListElement = document.getElementById('fetch-list');
var filesListSelectable = document.getElementById("shared-list-selectable");
module.exports.inputTargetAddress = inputTargetAddress
module.exports.filesListSelectable = filesListSelectable

function fetchList() {
  ipcRenderer.send('fetch-list', inputTargetAddress.value);
}

fetchListElement.addEventListener('click', function () {
	console.log("Will fetch shared resources list #1")
    fetchList();
});

ipcRenderer.on('got-resources-list', function (sender, json) {
  console.log("got resources list as json: " + json);

  while (filesListSelectable.firstChild) {
      filesListSelectable.removeChild(filesListSelectable.firstChild);
  }

  for(var i=0; i<json.results.length; i++) {
   // console.log(json.results[i]);
    var newItem = document.createElement("option");
    newItem.innerText = json.results[i].path;
    newItem.setAttribute("data-path", json.results[i].path);

    filesListSelectable.appendChild(newItem);

    //           return (<option key={resource.path} value={resource.path}>{resource.path}</option>);
  }
});


