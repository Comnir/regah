"use strict";
const {app, BrowserWindow} = require('electron');
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

ipcMain.on('fetch-list', function (event, ip) {
    const {net} = require('electron');

    const options = {
      hostname: ip,
      port: 42424,
      path: '/listShared',
      method: 'GET',
      headers: {
        "Content-type": "application/json; charset=utf-8",
        "Content-Length": 0
      }
    };

    const request = net.request(options);
    request.on('response', (response) => {
        console.log('STATUS: ' + response.statusCode);
        console.log('HEADERS: ' + JSON.stringify(response.headers));

        response.setEncoding('utf8');
        let rawData = '';
        response.on('data', (chunk) => {
          console.log('BODY: ' + chunk);
          rawData += chunk;
        });

        response.on('end', () => {
          console.log("end of response, raw data: " + rawData);
          const json = JSON.parse(rawData);
          console.log('No more data in response. After parse: ' + json);
          event.sender.send('got-resources-list', json);
        });
    });
    request.end();
});