import React, { Component } from 'react';
import './App.css';

class Resources extends React.Component {
  constructor(props) {
    super(props);
    this.state = {list: []};
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

    <button type="button" onClick={function () {
      var selectedFiles = document.getElementById('files').selectedOptions;
      window.alert([].slice
        .call(selectedFiles).map(function(el){return el.value}));

    }}>Click Me!</button>
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
                 body: JSON.stringify({"paths":[this.state.value]})})
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
        <input id="inputFile" type="file" />
        <br/>
        <InputPath />
      </div>
    );
  }
}

export default App;
