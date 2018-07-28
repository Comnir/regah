"use strict";
var ipcRenderer = require('electron').ipcRenderer;

var fetchListElement = document.getElementById('fetch-list');
var inputTargetAddress = document.getElementById('input-target-address');

fetchListElement.addEventListener('click', function () {
  ipcRenderer.send('fetch-list', inputTargetAddress.value);
});

ipcRenderer.on('got-resources-list', function (sender, json) {
  console.log("manage page got json: " + json);
});
