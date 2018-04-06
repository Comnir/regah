import React, { Component } from 'react';
import './App.css';

class Resources extends React.Component {
  constructor(props) {
    super(props);
    this.state = {list: []};
  }
  // https://facebook.github.io/react-native/docs/network.html
  listResourcesFrom(ip) {
    fetch('http://' + ip + ':42424/listShared', { method: 'GET' })
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
      var files = document.getElementById('files');
      window.alert(files.selectedOptions);

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
    }
    render() {
      return (
        <div className="InputPath">
          Full path to file or folder: <input id="inputText" type="text"/>
                  <input id="addFile" type="button" onClick="" value="Add"/>
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
