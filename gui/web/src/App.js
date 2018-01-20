import React, { Component } from 'react';
import './App.css';

class Resources extends React.Component {
  constructor(props) {
    super(props);
    this.state = {list: []};
  }

  listResourcesFrom(ip) {
    fetch('http://' + ip + ':42424/listShared')
      .then(results => { console.log('Got result:'); return results.json() })
      .then(data => {
        console.log('Path ' + data);
        this.setState({
          list: data.results,
        });
      })
    .catch((reason) => console.log(reason.toString()));
  }

  componentWillMount() {
    this.listResourcesFrom('127.0.0.1');
  }

  render() {
     return (
    <select multiple>
      {
        this.state.list.map((resource) => {
          return (<option key={resource.path} value={resource.path}>{resource.path}</option>);
        })
      }
    </select>
  );
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
        </p>
      </div>
    );
  }
}

export default App;
