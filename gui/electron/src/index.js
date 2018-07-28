"use strict";
var ipcRenderer = require('electron').ipcRenderer;

var manageEl = document.querySelector('.manage');

manageEl.addEventListener('click', function () {
    ipcRenderer.send('open-manage-window');
});