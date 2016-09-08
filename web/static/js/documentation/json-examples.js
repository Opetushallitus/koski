function forEach(nodelist, f) {
  Array.prototype.forEach.call(nodelist, f)
}

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
      while ((el = el.parentElement) && !el.classList.contains('json-row'));
      return el
    }
    toggleCollapse(findParentRow(e.currentTarget));
  }

  node.addEventListener('click', clickHandler, false)
})
