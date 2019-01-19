"use strict";
var ipcRenderer = require('electron').ipcRenderer;
const truncate = require('./string.common.js').truncate
var listJs = require('./list.js');
var filesListSelectable = listJs.filesListSelectable;
var inputTargetAddress = listJs.inputTargetAddress
var app = require('electron').remote;
var dialog = app.dialog;

var selectDownloadDestinationElement = document.getElementById('select-destination');
var selectionErrorElement = document.getElementById("selection-error");
var destinationElement = document.getElementById("destination");
var downloadElement = document.getElementById("download");


function registerForServerNotifications(sender) {
    if (registered[sender]) {
        console.log("Already registered");
        return;
    }

    console.log("Sender " + sender + " will be registered");

    var progressNotifications = new WebSocket("ws://127.0.0.1:42100/subscribe/" + sender);
    var listener = function (message) {
        console.log("Got some message:" + message);
        progressUpdate(message);
    };
    progressNotifications.onopen = function () {
        console.log("Opened connection for " + sender);
        document.getElementById("progress-section").style.visibility = "visible";
    }

    progressNotifications.onmessage = listener
//    progressNotifications.on('message', listener);
}

var registered = {};

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
		console.log("Download destination is: " + destination);


        const transportId = info["TRANSPORT_ID"]
        console.log("Register for notification for ID: " + transportId)
        registerForServerNotifications(transportId);

		ipcRenderer.send('download', address, destination, info)
	}
);

ipcRenderer.on('download-started', 
	function (sender) {
		console.log("Download started.");
	}
);

function progressUpdate(message){
	console.log("Got a progress update: " + message.data);
	document.getElementById("progress-updates").innerHTML = message.data;
}

function showDownloadButton() {
  downloadElement.style.visibility = "visible";
}

function hideDownloadButton() {
  downloadElement.style.visibility = "hidden";
}
