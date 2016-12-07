import '../polyfills.js'
import '../polyfills/closest.js'
import '../polyfills/dataset.js'
import '../style/documentation.less'

function forEach(nodelist, f) {
  Array.prototype.forEach.call(nodelist, f)
}

function toggleExpanded(elem) {
  return function() {
    var index = elem.className.indexOf(' expanded')
    if (index >= 0) {
      elem.className = elem.className.substring(0, index)
    } else {
      elem.className = elem.className + ' expanded'
      elem.dispatchEvent(new Event('expand'))
    }
  }
}

window.onload = function() {
  // API Tester logic
  forEach(document.querySelectorAll('.api-operation'), function(operationElem) {
    operationElem.querySelector('h3').addEventListener('click', toggleExpanded(operationElem))
    forEach(operationElem.querySelectorAll('.status-codes'), function(elem) {
      elem.querySelector('h4').addEventListener('click', toggleExpanded(elem))
    })
    forEach(operationElem.querySelectorAll('.api-tester'), function(apiTesterElem) {
      var exampleSelector = apiTesterElem.querySelector('.examples select')
      var codeMirror
      var queryParamInputs = a(apiTesterElem.querySelectorAll('.parameters input.query-param'))
      var pathParamInputs = a(apiTesterElem.querySelectorAll('.parameters input.path-param, .parameters select.path-param'))
      var paramInputs = queryParamInputs.concat(pathParamInputs)

      function apiUrl() {
        var path = apiTesterElem.dataset.path
        pathParamInputs.forEach(function(input) {
          path = path.replace('{' + input.name + '}', encodeURIComponent(input.value))
        })
        var queryParameters = queryParamInputs.reduce(function(query, input) {
          return input.value ? query + (query ? '&' : '?') + encodeURIComponent(input.name) + '=' + encodeURIComponent(input.value) : ''
        },'')
        return document.location.protocol + '//' + document.location.host + path + queryParameters
      }

      if (exampleSelector) {
        var editorElem = apiTesterElem.querySelector('textarea')
        codeMirror = window.CodeMirror.fromTextArea(editorElem, { mode: { name: 'javascript', json: true}, theme: 'custom' })
        function showExampleData() {
          var index = exampleSelector.selectedIndex
          var data = exampleSelector.options[index].dataset.exampledata
          editorElem.value=data
          codeMirror.setValue(data)
        }
        exampleSelector.addEventListener('change', showExampleData)
        showExampleData()

        apiTesterElem.closest('.api-operation').addEventListener('expand', showExampleData)
      }

      apiTesterElem.querySelector('.try').addEventListener('click', function() {
        apiTesterElem.className = 'api-tester loading'

        forEach(apiTesterElem.querySelectorAll('button'), function(buttonElem) { buttonElem.disabled = true })

        var options = {credentials: 'include', method: apiTesterElem.dataset.method, headers: {'Content-Type': 'application/json'}}

        if (codeMirror) {
          options.body = codeMirror.getValue()
        }

        function showResponse(response) {
          var resultElem = apiTesterElem.querySelector('.result')
          apiTesterElem.className = 'api-tester'
          forEach(apiTesterElem.querySelectorAll('button'), function(buttonElem) {buttonElem.disabled = false})
          resultElem.innerHTML = response
          var codeBlock = resultElem.querySelector('code')
          if (codeBlock) {
            window.hljs.highlightBlock(codeBlock)
          }
        }

        fetch(apiUrl(), options)
          .then(function(response) {
            return response.text().then(function(text) {
              if (response.status == 401) {
                showResponse(response.status + ' ' + response.statusText + ' <a href="/koski" target="_new">Login</a>')
              } else if (text) {
                showResponse(response.status + ' ' + response.statusText + '<pre><code>' + JSON.stringify(JSON.parse(text), null, 2) + '</code></pre>')
              } else {
                showResponse(response.status + ' ' + response.statusText)
              }
            })
          })
          .catch(function(error) {
            showResponse(error)
          })
      })

      var newWindowButton = apiTesterElem.querySelector('.try-newwindow')
      if (apiTesterElem.dataset.method == 'GET') {
        newWindowButton.addEventListener('click', function() {
          window.open(apiUrl())
        })
      } else {
        newWindowButton.className = 'hidden'
      }

      apiTesterElem.querySelector('.curl').onclick = function() {
        apiTesterElem.querySelector('.curl').className='curl open'
        selectElementContents(apiTesterElem.querySelector('.curl .line'))
      }
      updateCurl()

      paramInputs.forEach(function(input) {
        input.addEventListener('keyup', updateCurl)
        input.addEventListener('input', updateCurl)
        input.addEventListener('change', updateCurl)
      })

      function updateCurl() {
        var curl = 'curl "' + apiUrl() + '" --user kalle:kalle'
        if (apiTesterElem.dataset.method != 'GET') {
          curl += ' -X ' + apiTesterElem.dataset.method
        }
        if (apiTesterElem.dataset.method == 'POST' || apiTesterElem.dataset.method == 'PUT') {
          curl += ' -H "content-type: application/json" -d @curltestdata.json'
        }
        apiTesterElem.querySelector('.curl .line').innerHTML = curl
      }

      function a(elems) {
        return Array.prototype.slice.call(elems, 0)
      }

      function selectElementContents(el) {
        var range = document.createRange()
        range.selectNodeContents(el)
        var sel = window.getSelection()
        sel.removeAllRanges()
        sel.addRange(range)
      }
    })

    forEach(operationElem.querySelectorAll('.example-response'), function(responseElem) {
      forEach(responseElem.querySelectorAll('a'), function(link) { link.onclick = toggleExpanded(responseElem) })
      var codeElem = responseElem.querySelector('code')
      codeElem.innerHTML = JSON.stringify(JSON.parse(codeElem.innerHTML), null, 2) // pretty-print JSON
      window.hljs.highlightBlock(codeElem)
    })

  })

// JSON Examples expand/collapse logic
  forEach(document.querySelectorAll('.example-item'), function(exampleElem) {
    exampleElem.querySelector('.example-link').addEventListener('click', toggleExpanded(exampleElem))
  })

  forEach(document.querySelectorAll('.json-row .collapsible'), function(node) {
    function isNodeClass (clazz) {
      return clazz.startsWith('node')
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

    function clickHandler(e) {
      function findParentRow(el) {
        while ((el = el.parentElement) && !el.classList.contains('json-row')) {}
        return el
      }
      toggleCollapse(findParentRow(e.currentTarget))
    }

    node.addEventListener('click', clickHandler, false)
  })
}