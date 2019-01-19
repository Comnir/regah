"use strict";
const {app, BrowserWindow} = require('electron');
const WebSocket = require("ws")
const {net} = require('electron');
const MANAGE_PORT = 42421;
const CLIENT_PORT = 42424;
const stringCommon = require('./string.common.js');
const truncate = stringCommon.truncate

var ipcMain = require('electron').ipcMain;
var http = require('http')

var mainWindow = null;
var manageWindow = null;
var downloadWindow = null;

function createMainWindow() {
    mainWindow = new BrowserWindow({width: 800, height: 600})

    mainWindow.loadFile('src/index.html')

    mainWindow.on('closed', () => {
        mainWindow == null
    })
}

app.on('ready', createMainWindow)

ipcMain.on('open-manage-window', function () {
    if (manageWindow) {
        return;
    }

    manageWindow = new BrowserWindow({
        height: 600,
        width: 800
    });

    manageWindow.loadFile('src/manage.html');

    manageWindow.on('closed', function () {
        manageWindow = null;
    });
});

ipcMain.on('open-download-window', function () {
    if (downloadWindow) {
        return;
    }

    downloadWindow = new BrowserWindow({
        height: 600,
        width: 800
    });

    downloadWindow.loadFile('src/download.html');

    downloadWindow.on('closed', function () {
        downloadWindow = null;
    });
});
 
ipcMain.on('add-files', function (event, ip, newPath) {
    console.log("#add-files# Asked  to send 'add files' request with path " + newPath);
    const jsonBody = JSON.stringify({ "paths":[newPath]});

    const options = {
      hostname: ip,
      port: MANAGE_PORT,
      path: '/add',
      method: 'POST',
      headers: {
        "Content-type": "application/json; charset=utf-8",
      }
    };

    sendRequest(options, (rawData) => {
       console.log("#add-files# end of response, raw response: " + rawData);
       event.sender.send('add-succeeded');
    }, jsonBody)
});

ipcMain.on('fetch-list', function (event, ip) {
	console.log('will send request to fetch list');
    const options = {
      hostname: ip,
      port: CLIENT_PORT,
      path: '/listShared',
      method: 'GET',
      headers: {
        "Content-type": "application/json; charset=utf-8"
      }
    };

    sendRequest(options, (rawResponse) => {
       console.log("#fetch-list# end of response, raw data: " + rawResponse);
       const json = JSON.parse(rawResponse);
       console.log('#fetch-list# No more data in response. After parse: ' + json);
       event.sender.send('got-resources-list', json);
    });
});

ipcMain.on('fetch-download-info', function (event, ip, paths) {
    const options = {
      hostname: ip,
      port: CLIENT_PORT,
      path: '/prepareResourceForDownload',
      method: 'POST',
      headers: {
        "Content-type": "application/json; charset=utf-8"
      }
    };
    
    var jsonBody = JSON.stringify({"filePath": paths[0]});

    sendRequest(options, (rawResponse) => {
       console.log("#fetch-download-info# end of response, raw data (truncated): " + truncate(rawResponse, 100));
       const json = JSON.parse(rawResponse);
       console.log('#fetch-download-info# No more data in response.');
       event.sender.send('got-download-info', json);
    }, jsonBody);
});

ipcMain.on('download', function (event, ip, destination, downloadData) {
    const options = {
      hostname: 'localhost',
      port: MANAGE_PORT,
      path: '/download',
      method: 'POST',
      headers: {
        "Content-type": "application/json; charset=utf-8"
      }
    };

    var jsonRequest = JSON.stringify({"path": destination, "downloadData": JSON.stringify(downloadData)});
    
    console.log('#download# send download request with JSON: ' + truncate(jsonRequest, 100));
    
    sendRequest(options, (rawResponse) => {
       console.log("#download# end of response, raw data: " + truncate(rawResponse, 100) );
       event.sender.send('download-started');
    }, jsonRequest);
});

function sendRequest(requestOptions, onRequestEnd, requestBody) {
    const request = net.request(requestOptions);
    request.on('response', (response) => {
        console.log('STATUS: ' + response.statusCode);
        console.log('HEADERS: ' + JSON.stringify(response.headers));

        response.setEncoding('utf8');
        let rawData = '';
        response.on('data', (chunk) => {
          rawData += chunk;
        });

        response.on('end', () => onRequestEnd(rawData));
    });

    if (undefined !== requestBody) {
      request.write(requestBody);
    }
    request.end();
}
