function addArrayMethod(name, fn) {
  if (!Array.prototype[name]) {
    Object.defineProperty(Array.prototype, name, {
      enumerable: false,
      value: fn
    })
  }
}
addArrayMethod('includes', function (item) {
  return this.indexOf(item) >= 0
})

if (!String.prototype.startsWith) {
  String.prototype.startsWith = function(searchString, position) {
    position = position || 0
    return this.indexOf(searchString, position) === position
  }
}
if (!String.prototype.endsWith) {
  String.prototype.endsWith = function(searchString, position) {
    var subjectString = this.toString()
    if (typeof position !== 'number' || !isFinite(position) || Math.floor(position) !== position || position > subjectString.length) {
      position = subjectString.length
    }
    position -= searchString.length
    var lastIndex = subjectString.lastIndexOf(searchString, position)
    return lastIndex !== -1 && lastIndex === position
  }
}