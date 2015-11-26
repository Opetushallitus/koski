var isNodeClass = function(clazz) {
  return clazz.startsWith('node')
}

var forEach = function(nodelist, f) {
  Array.prototype.forEach.call(nodelist, f)
}

var findParentRow = function(el) {
  while ((el = el.parentElement) && !el.classList.contains('json-row'));
  return el
}

var clickHandler = function(e) {
  var row = findParentRow(e.currentTarget)

  row.classList.toggle('collapsed-parent')

  forEach(row.parentNode.querySelectorAll('.json-row'), function(elem) {
    elem.classList.remove('collapsed')
  })

  forEach(row.parentNode.querySelectorAll('.collapsed-parent'), function(parent) {
    forEach(row.parentNode.querySelectorAll('.' + parent.className.split(' ').filter(isNodeClass).join('.')), function(elem){
      if(!elem.classList.contains('collapsed') && elem !== parent) {
        elem.classList.add('collapsed')
      }
    })
  })
}

forEach(document.querySelectorAll('.json-row .collapsible'), function(node) {
    node.addEventListener('click', clickHandler, false)
  }
)

forEach(document.querySelectorAll('.api-tester'), function(elem) {
  elem.querySelector(".examples select").addEventListener("change", function(a,b,c) {
    var data = event.target.options[event.target.selectedIndex].dataset.exampledata
    elem.querySelector("textarea").value=data
  })
  elem.querySelector(".try").addEventListener('click', function() {
    var data = elem.querySelector("textarea").value;
    fetch(document.location.protocol + "//" + document.location.host + elem.dataset.path, { credentials: 'include', method: elem.dataset.method, body: data, headers: { 'Content-Type': 'application/json'} })
      .then(function(response) {
        var resultElem = elem.querySelector(".result");
        response.text().then(function(text, err) {
          if (response.status == 401) {
            resultElem.innerHTML = response.status + " " + response.statusText + ' <a href="/tor" target="_new">Login</a>'
          } else if (text) {
            resultElem.innerHTML = response.status + " " + response.statusText + "<pre>" + text + "</pre>"
          } else {
            resultElem.innerHTML = response.status + " " + response.statusText
          }
        })
      })
  })
})

