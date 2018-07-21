import React, { Component } from 'react';
import './App.css';

class Resources extends React.Component {
  constructor(props) {
    super(props);
    this.state = {
        list: [],
        destination: ""
        };
    this.askForTransportData = this.askForTransportData.bind(this);
    this.askForTransportDataWithSinglePath = this.askForTransportDataWithSinglePath.bind(this);
    this.downloadWithTransportData = this.downloadWithTransportData.bind(this);
    this.updateDestination = this.updateDestination.bind(this);
  }
  // https://facebook.github.io/react-native/docs/network.html
  listResourcesFrom(ip) {
    fetch('http://' + ip + ':42424/listShared', {
         method: 'GET',
         mode: 'cors',
         headers: {
                     "Content-type": "application/json; charset=utf-8"
                 }})
      .then(results => { console.log('Got result :'); return results.json() })
      .then(data => {
        console.log('Path ' + data);
        this.setState({
          list: data.results,
        });
      })
    .catch((reason) => console.log(reason.toString()));
  }

  downloadWithTransportData(ip, data) {
     console.log("Starting downloadWithTransportData with transport data: " + data);
     fetch('http://' + ip + ':42421/download', {
                  method: 'POST',
                  mode: 'cors',
                  headers: {
                              "Content-type": "application/json; charset=utf-8"
                          },
                  body: JSON.stringify(
                      {
                         "path":"/home/jefferson/Documents/regahGames/dest/", // add text field and get it's value
                         "downloadData":JSON.stringify(data)
                      }
                  )
              }
            )
           .then(results => {
             console.log('Got result :');
             return results.json() })
         .catch((reason) => console.log(reason.toString()));
   }

  // https://facebook.github.io/react-native/docs/network.html
  askForTransportData() {
    var selectedOptions = document.getElementById('files').selectedOptions;
    var selectedFiles = [].slice.call(selectedOptions)
        .map(function(el){
            return el.value
         });


    var askSingle = this.askForTransportDataWithSinglePath;

    selectedFiles.forEach(function(path) {
        askSingle('localhost', path);
    })
  }

  askForTransportDataWithSinglePath(ip, path) {
    console.log("Starting askForTransportDataWithPath with file to download: " + path);
    fetch('http://' + ip + ':42424/prepareResourceForDownload', {
             method: 'POST',
             mode: 'cors',
             headers: {
                         "Content-type": "application/json; charset=utf-8"
                     },
             body: JSON.stringify({"filePath":path})
         }
       )
      .then(results => { console.log('Got result :'); return results.json() })
      .then(data => {
              this.downloadWithTransportData(ip, data);
            })

    .catch((reason) => console.log(reason.toString()));
  }

  updateDestination(event) {
    console.log("updated destination: " + event.target.value);
    this.setState({
              destination: event.target.value,
            });
  }


  componentWillMount() {
    this.listResourcesFrom('localhost');
  }

  render() {
     return (
     <p>

    <select multiple id='files' onChange={function (event) {window.alert('changed!' + [event.target.selectedOptions].map(o => o.value).join(', ')) }}>
      {
        this.state.list.map((resource) => {
          return (<option key={resource.path} value={resource.path}>{resource.path}</option>);
        })
      }
    </select>

    <br/>
    <button type="button" onClick={this.askForTransportData}>Download selected files to</button>
    <br/>
    // consider using Electron instead to display the GUI.
    // file manipulation: https://ourcodeworld.com/articles/read/106/how-to-choose-read-save-delete-or-create-a-file-with-electron-framework
    <input id="destination" type="text"  onChange={this.updateDestination}/>
    <br/>
    <button type="button" onClick={function () {
      var selectedFiles = document.getElementById('files').selectedOptions;
      window.alert([].slice
        .call(selectedFiles).map(function(el){
            return el.value}));
        }
    }>alert</button>
    </p>
  );
  }
}

class InputPath extends Component {
    constructor(props) {
        super(props);
        this.state = {
          value: null,
        }
        this.addShare = this.addShare.bind(this);
        this.updatePath = this.updatePath.bind(this);
    }

    updatePath(event) {
        this.setState({
          value: event.target.value,
        });
    }

    addShare(event) {
        alert("Get path: " + this.state.value)

        fetch('http://localhost:42421/add', {
                     method: 'POST',
                     mode: 'cors',
                     headers: {
                                 "Content-type": "application/json; charset=utf-8"
                             },
                     body: JSON.stringify({"paths":[this.state.value]})
                 }
              )
              .then(results => { console.log('Got result :'); return results.json() })
              .then(data => {
                console.log('Path ' + this.state.value);
                this.setState({
                  list: data.results,
                });
              })
            .catch((reason) => console.log("Error encountered while adding shared resource: " + reason.toString()));
    }

    render() {
      return (
        <div className="InputPath">
          Full path to file or folder: <input id="inputText" type="text" onChange={this.updatePath}/>
                  <input id="addFile" type="button" value="Add" onClick={this.addShare} />
        </div>
      )
    }
}

class App extends Component {
  render() {
    return (
      <div className="App">
        <header className="App-header">
          <h1 className="App-title">RegaH share center</h1>
          <p className="App-subtitle">The files are almost on their way...</p>
        </header>
        <p className="Description">
          Select files to download from the list below.


        </p>
        <Resources />
        <p className="Block">
            -= TODO: add a destination field <br/>
            -= TODO: add a download button for the selected files
            -= TODO: how to send file path to local server? for adding shared resources
        </p>
        <br/>
        <InputPath />
      </div>
    );
  }
}

export default App;
