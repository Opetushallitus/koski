function isNodeClass (clazz) {
  return clazz.startsWith('node')
}

function forEach(nodelist, f) {
  Array.prototype.forEach.call(nodelist, f)
}

function toggleCollapse(row) {
  row.classList.toggle('collapsed-parent')
  forEach(row.parentNode.querySelectorAll('.json-row'), function (elem) {
    elem.classList.remove('collapsed')
  })
  forEach(row.parentNode.querySelectorAll('.collapsed-parent'), function (parent) {
    forEach(row.parentNode.querySelectorAll('.' + parent.className.split(' ').filter(isNodeClass).join('.')), function (elem) {
      if (!elem.classList.contains('collapsed') && elem !== parent) {
        elem.classList.add('collapsed')
      }
    })
  })
}
var clickHandler = function(e) {
  function findParentRow(el) {
    while ((el = el.parentElement) && !el.classList.contains('json-row'));
    return el
  }
  toggleCollapse(findParentRow(e.currentTarget));
}

forEach(document.querySelectorAll('.json-row .collapsible'), function(node) {
    node.addEventListener('click', clickHandler, false)
})

forEach(document.querySelectorAll('.api-operation'), function(operationElem) {
  var toggleLink = operationElem.querySelector(".toggle-details")
  toggleLink.addEventListener("click", function() {
    if (operationElem.className.indexOf("expanded") >= 0) {
      operationElem.className = "api-operation"
    } else {
      operationElem.className = "api-operation expanded"
    }
  })
})

forEach(document.querySelectorAll('.api-tester'), function(elem) {
  var exampleSelector = elem.querySelector(".examples select")
  var codeMirror
  var queryParamInputs = a(elem.querySelectorAll(".parameters input.query-param"))
  var pathParamInputs = a(elem.querySelectorAll(".parameters input.path-param"))
  var paramInputs = queryParamInputs.concat(pathParamInputs)

  function apiUrl() {
    var path = elem.dataset.path
    pathParamInputs.forEach(function(input) {
      path = path.replace('{' + input.name + '}', encodeURIComponent(input.value))
    })
    var queryParameters = queryParamInputs.reduce(function(query, input) {
      return input.value ? query + (query ? '&' : '?') + encodeURIComponent(input.name) + '=' + encodeURIComponent(input.value) : ''
    },'')
    return document.location.protocol + "//" + document.location.host + path + queryParameters
  }

  if (exampleSelector) {
    var editorElem = elem.querySelector("textarea");
    codeMirror = CodeMirror.fromTextArea(editorElem, { mode: { name: "javascript", json: true}, theme: "custom" })
    exampleSelector.addEventListener("change", function(a,b,c) {
      var data = event.target.options[event.target.selectedIndex].dataset.exampledata
      editorElem.value=data
      codeMirror.setValue(data)
    })
  }
  var button = elem.querySelector(".try")
  button.addEventListener('click', function() {
    elem.className = "api-tester loading"
    button.disabled = true
    var options = {credentials: 'include', method: elem.dataset.method, headers: {'Content-Type': 'application/json'}};

    if (codeMirror) {
      options.body = codeMirror.getValue()
    }

    function showResponse(response) {
      var resultElem = elem.querySelector(".result");
      elem.className = "api-tester"
      button.disabled = false
      resultElem.innerHTML = response
      var codeBlock = resultElem.querySelector("code");
      if (codeBlock)
        hljs.highlightBlock(codeBlock)
    }

    fetch(apiUrl(), options)
      .then(function(response) {
        return response.text().then(function(text, err) {
          if (response.status == 401) {
            showResponse(response.status + " " + response.statusText + ' <a href="/tor" target="_new">Login</a>')
          } else if (text) {
            showResponse(response.status + " " + response.statusText + "<pre><code>" + JSON.stringify(JSON.parse(text), null, 2) + "</code></pre>")
          } else {
            showResponse(response.status + " " + response.statusText)
          }
        })
      })
      .catch(function(error) {
        showResponse(error)
      })
  })
  var newWindowButton = elem.querySelector(".try-newwindow")
  if (elem.dataset.method == 'GET') {
    newWindowButton.addEventListener('click', function() {
      window.open(apiUrl())
    })
  } else {
    newWindowButton.className = 'hidden'
  }

  elem.querySelector(".curl").onclick = function() {
    elem.querySelector(".curl").className="curl open"
    selectElementContents(elem.querySelector(".curl .line"))
  }
  updateCurl()

  paramInputs.forEach(function(input) {
    input.addEventListener("keyup", updateCurl)
    input.addEventListener("input", updateCurl)
    input.addEventListener("change", updateCurl)
  })

  function updateCurl() {
    var curl = "curl '" + apiUrl() + "' --user kalle:kalle"
    if (elem.dataset.method != "GET") {
      curl += " -X " + elem.dataset.method
    }
    if (elem.dataset.method == "POST" || elem.dataset.method == "PUT") {
      curl += " -H 'content-type: application/json' -d @curltestdata.json"
    }
    elem.querySelector(".curl .line").innerHTML = curl
  }

  function a(elems) {
    return Array.prototype.slice.call(elems, 0)
  }

  function selectElementContents(el) {
    var range = document.createRange();
    range.selectNodeContents(el);
    var sel = window.getSelection();
    sel.removeAllRanges();
    sel.addRange(range);
  }
})

