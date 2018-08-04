"use strict";
var ipcRenderer = require('electron').ipcRenderer;

var app = require('electron').remote;
var dialog = app.dialog;

var inputTargetAddress = document.getElementById('input-target-address');
var fetchListElement = document.getElementById('fetch-list');
var filesList = document.getElementById("shared-list");

var selectToAddElement = document.getElementById('select-to-add');
var selectedFilesElement = document.getElementById("selected-path");
var addFilesElement = document.getElementById("add-files");

fetchListElement.addEventListener('click', function () {
    fetchList();
});

function fetchList() {
  ipcRenderer.send('fetch-list', inputTargetAddress.value);
}

ipcRenderer.on('got-resources-list', function (sender, json) {
  console.log("manage page got json: " + json);

  filesList.innerHTML = "" // clear old list - haven't verified whether this is the best way...

  for(var i=0; i<json.results.length; i++) {
    console.log(json.results[i]);
    var newItem = document.createElement("li");
    newItem.innerText = json.results[i].path;
    filesList.appendChild(newItem);
  }
});

selectToAddElement.addEventListener('click', function () {
    var dialogOptions = {
        properties: ['openFile', 'openDirectory']
    };
    dialog.showOpenDialog(dialogOptions,
     (path) => {
       if (undefined === path) {
         // nothing selected - leave old state as-is
       } else {
        console.log("selected a file: " + path);
        selectedFilesElement.innerHTML = path;
        showAddButton();
       }

    });
});

addFilesElement.addEventListener('click', function () {
  console.log("Add file:" + selectedFilesElement.innerHTML);
  ipcRenderer.send('add-files', inputTargetAddress.value, selectedFilesElement.innerHTML);
});

ipcRenderer.on('add-succeeded', function (sender, json) {
  console.log("Successfully added files");
  selectedFilesElement.innerHTML = ""
  hideAddButton();
  fetchList(); // refresh list of shared resources
});

function showAddButton() {
  addFilesElement.style.visibility = "visible";
}

function hideAddButton() {
  addFilesElement.style.visibility = "hidden";
}