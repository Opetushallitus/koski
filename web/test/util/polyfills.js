function addArrayMethod(name, fn) {
  if (!Array.prototype[name]) {
    console.log(`Defining Array.prototype.${name}`)
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
  console.log('Polyfilling String.prototype.startsWith')
  String.prototype.startsWith = function (searchString, position) {
    position = position || 0
    return this.indexOf(searchString, position) === position
  }
  if (!String.prototype.startsWith) {
    throw new Error('Polyfilling String.prototype.startsWith failed')
  }
}
if (!String.prototype.endsWith) {
  console.log('Polyfilling String.prototype.endsWith')
  String.prototype.endsWith = function (searchString, position) {
    let subjectString = this.toString()
    if (
      typeof position !== 'number' ||
      !isFinite(position) ||
      Math.floor(position) !== position ||
      position > subjectString.length
    ) {
      position = subjectString.length
    }
    position -= searchString.length
    let lastIndex = subjectString.lastIndexOf(searchString, position)
    return lastIndex !== -1 && lastIndex === position
  }

  if (!String.prototype.endsWith) {
    throw new Error('Polyfilling String.prototype.endsWith failed')
  }
}
if (!String.prototype.includes) {
  console.log('Polyfilling String.prototype.includes')
  String.prototype.includes = function (substring) {
    return this.indexOf(substring) >= 0
  }

  if (!String.prototype.startsWith) {
    throw new Error('Polyfilling String.prototype.includes failed')
  }
}

export default {}
