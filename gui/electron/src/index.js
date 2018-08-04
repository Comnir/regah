"use strict";
var ipcRenderer = require('electron').ipcRenderer;

var manageEl = document.getElementById('manage');

manageEl.addEventListener('click', function () {
    ipcRenderer.send('open-manage-window');
});