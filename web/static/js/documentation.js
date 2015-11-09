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


