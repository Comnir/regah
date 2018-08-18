"use strict";
var ipcRenderer = require('electron').ipcRenderer;

document.getElementById('manage')
	.addEventListener('click', function () {
		ipcRenderer.send('open-manage-window');
	});
	
document.getElementById('download')
	.addEventListener('click', function () {
		ipcRenderer.send('open-download-window');
	});
