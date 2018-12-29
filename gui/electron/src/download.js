"use strict";
var ipcRenderer = require('electron').ipcRenderer;
var listJs = require('./list.js');
var filesListSelectable = listJs.filesListSelectable;
var inputTargetAddress = listJs.inputTargetAddress
var app = require('electron').remote;
var dialog = app.dialog;

var selectDownloadDestinationElement = document.getElementById('select-destination');
var selectionErrorElement = document.getElementById("selection-error");
var destinationElement = document.getElementById("destination");
var downloadElement = document.getElementById("download");

selectDownloadDestinationElement.addEventListener('click', function () {
    var dialogOptions = {
        properties: ['openDirectory']
    };
    dialog.showOpenDialog(dialogOptions,
     (path) => {
       if (undefined === path) {
         // nothing selected - leave old state as-is
       } else {
        console.log("selected a folder: " + path);
        destinationElement.innerHTML = path;
        showDownloadButton();
       }

    });
});

downloadElement.addEventListener('click', function () {
  console.log("Download file");
  
  var address = inputTargetAddress.value;
  
  var selectedFiles = [].slice
    .call(filesListSelectable.selectedOptions)
    .map(function(el){
        return el.value
        }
    );

  if (0 == selectedFiles.length) {
    selectionErrorElement.innerHTML = "Please select files to download.";
    selectionErrorElement.hidden = false;
//    window.alert("Selected files: " + selectedFiles);
    return;
  }

  console.log('Download path "' + selectedFiles + '" from ' + address);
  ipcRenderer.send('fetch-download-info', address, selectedFiles);
});

ipcRenderer.on('got-download-info', 
	function (sender, info) {
		var destination = destinationElement.innerHTML
		var address = inputTargetAddress.value;
		// TODO: handle failed request
		console.log("Got download info: " + info);
		console.log("Download destination is: " + destination);
		ipcRenderer.send('download', address, destination, info)
	}
);

ipcRenderer.on('got-download-info', 
	function (sender) {
		console.log("Download started.");
	}
);

function showDownloadButton() {
  downloadElement.style.visibility = "visible";
}

function hideDownloadButton() {
  downloadElement.style.visibility = "hidden";
}
