
var fetchListElement = document.getElementById('fetch-list');
var filesList = document.getElementById("shared-list");
var filesListSelectable = document.getElementById("shared-list-selectable");


fetchListElement.addEventListener('click', function () {
    fetchList();
});

ipcRenderer.on('got-resources-list', function (sender, json) {
  console.log("manage page got json: " + json);

  filesList.innerHTML = "" // clear old list - haven't verified whether this is the best way...

  for(var i=0; i<json.results.length; i++) {
    console.log(json.results[i]);
    var newItem = document.createElement("li");
    newItem.innerText = json.results[i].path;
    newItem.setAttribute("data-path", json.results[i].path);
    filesList.appendChild(newItem);
  }

  filesListSelectable.innerHTML = ""

  for(var i=0; i<json.results.length; i++) {
    console.log(json.results[i]);
    var newItem = document.createElement("option");
    newItem.innerText = json.results[i].path;
    newItem.setAttribute("data-path", json.results[i].path);

    filesListSelectable.appendChild(newItem);

    //           return (<option key={resource.path} value={resource.path}>{resource.path}</option>);
  }
});


