"use strict";
var ipcRenderer = require('electron').ipcRenderer;

var app = require('electron').remote;
var dialog = app.dialog;

var selectToAddElement = document.getElementById('select-to-add');
var selectedFilesElement = document.getElementById("selected-path");
var addFilesElement = document.getElementById("add-files");

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
