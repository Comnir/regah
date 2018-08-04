"use strict";
const {app, BrowserWindow} = require('electron');
const {net} = require('electron');
var ipcMain = require('electron').ipcMain;
var http = require('http')

var manageWindow = null;

function createMainWindow() {
    mainWindow = new BrowserWindow({width: 800, height: 600})

    mainWindow.loadFile('src/index.html')

    mainWindow.on('closed', () => {
        mainWindow == null
    })
}

app.on('ready', createMainWindow)

var mainWindow = null;
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

ipcMain.on('add-files', function (event, ip, newPath) {
    console.log("Asked  to send 'add files' request with path " + newPath);
    const jsonBody = JSON.stringify({ "paths":[newPath]});

    const options = {
      hostname: ip,
      port: 42421,
      path: '/add',
      method: 'POST',
      headers: {
        "Content-type": "application/json; charset=utf-8",
      }
    };

    sendRequest(options, (rawData) => {
       console.log("end of response, raw response: " + rawData);
       event.sender.send('add-succeeded');
    }, jsonBody)
});

ipcMain.on('fetch-list', function (event, ip) {
    const options = {
      hostname: ip,
      port: 42424,
      path: '/listShared',
      method: 'GET',
      headers: {
        "Content-type": "application/json; charset=utf-8"
      }
    };

    sendRequest(options, (rawResponse) => {
       console.log("end of response, raw data: " + rawResponse);
       const json = JSON.parse(rawResponse);
       console.log('No more data in response. After parse: ' + json);
       event.sender.send('got-resources-list', json);
    });
});

function sendRequest(requestOptions, onRequestEnd, requestBody) {
    const request = net.request(requestOptions);
    request.on('response', (response) => {
        console.log('STATUS: ' + response.statusCode);
        console.log('HEADERS: ' + JSON.stringify(response.headers));

        response.setEncoding('utf8');
        let rawData = '';
        response.on('data', (chunk) => {
          console.log('BODY: ' + chunk);
          rawData += chunk;
        });

        response.on('end', () => onRequestEnd(rawData));
    });

    if (undefined !== requestBody) {
      request.write(requestBody);
    }
    request.end();
}