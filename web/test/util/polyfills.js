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
