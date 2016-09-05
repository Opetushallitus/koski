import './polyfill-bind.js'
import 'whatwg-fetch'
import 'es6-promise'
import './window-error-handler.js'
if (!String.prototype.startsWith) {
  String.prototype.startsWith = function(searchString, position) {
    position = position || 0
    return this.indexOf(searchString, position) === position
  }
}
function addArrayMethod(name, fn) {
  if (!Array.prototype[name]) {
    Object.defineProperty(Array.prototype, name, {
      enumerable: false,
      value: fn
    })
  }
}
addArrayMethod('find', function(predicate) {
   var list = Object(this)
   var length = list.length < 0 ? 0 : list.length >>> 0 // ES.ToUint32
   if (length === 0) return undefined
   if (typeof predicate !== 'function' || Object.prototype.toString.call(predicate) !== '[object Function]') {
     throw new TypeError('Array#find: predicate must be a function')
   }
   var thisArg = arguments[1]
   for (var i = 0, value; i < length; i++) {
     value = list[i]
     if (predicate.call(thisArg, value, i, list)) return value
   }
})
addArrayMethod('findIndex', function(predicate) {
  if (this === null) {
    throw new TypeError('Array.prototype.findIndex called on null or undefined')
  }
  if (typeof predicate !== 'function') {
    throw new TypeError('predicate must be a function')
  }
  var list = Object(this)
  var length = list.length >>> 0
  var thisArg = arguments[1]
  var value

  for (var i = 0; i < length; i++) {
    value = list[i]
    if (predicate.call(thisArg, value, i, list)) {
      return i
    }
  }
  return -1
})
addArrayMethod('flatMap', function (lambda) {
    return Array.prototype.concat.apply([], this.map(lambda))
})
addArrayMethod('last', function () {
  return this[this.length - 1]
})

Number.isNaN = Number.isNaN || function(value) {
  return typeof value === 'number' && isNaN(value)
}

if (!history.pushState) {
  console.log('history.pushState not available')
  history.pushState = function() {}
}