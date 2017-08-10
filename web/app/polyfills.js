import './polyfill-bind.js'
import 'whatwg-fetch'
import 'es6-promise/auto'
import './window-error-handler.js'
import './polyfills/closest.js'
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
if (!String.prototype.includes) {
  String.prototype.includes = function (substring) {
    return this.indexOf(substring) >= 0
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
addArrayMethod('flatten', function () {
  return this.flatMap((x) => x)
})
addArrayMethod('last', function () {
  return this[this.length - 1]
})
addArrayMethod('includes', function (item) {
  return this.indexOf(item) >= 0
})

addArrayMethod('reduce', function(callback /*, initialValue*/) {
  if (this === null) {
    throw new TypeError('Array.prototype.reduce called on null or undefined')
  }
  if (typeof callback !== 'function') {
    throw new TypeError(callback + ' is not a function')
  }
  var t = Object(this), len = t.length >>> 0, k = 0, value
  if (arguments.length == 2) {
    value = arguments[1]
  } else {
    while (k < len && !(k in t)) {
      k++
    }
    if (k >= len) {
      throw new TypeError('Reduce of empty array with no initial value')
    }
    value = t[k++]
  }
  for (; k < len; k++) {
    if (k in t) {
      value = callback(value, t[k], k, t)
    }
  }
  return value
})

if (!Array.prototype.fill) {
  Object.defineProperty(Array.prototype, 'fill', {
    value: function(value) {
      if (this == null) {
        throw new TypeError('this is null or not defined')
      }
      var O = Object(this)
      var len = O.length >>> 0
      var start = arguments[1]
      var relativeStart = start >> 0
      var k = relativeStart < 0 ?
        Math.max(len + relativeStart, 0) :
        Math.min(relativeStart, len)
      var end = arguments[2]
      var relativeEnd = end === undefined ?
        len : end >> 0
      var final = relativeEnd < 0 ?
        Math.max(len + relativeEnd, 0) :
        Math.min(relativeEnd, len)
      while (k < final) {
        O[k] = value
        k++
      }
      return O
    }
  })
}

if (typeof Object.assign != 'function') {
  Object.assign = function (target) {
    if (target == null) {
      throw new TypeError('Cannot convert undefined or null to object')
    }

    var to = Object(target)

    for (var index = 1; index < arguments.length; index++) {
      var nextSource = arguments[index]

      if (nextSource != null) { // Skip over if undefined or null
        for (var nextKey in nextSource) {
          // Avoid bugs when hasOwnProperty is shadowed
          if (Object.prototype.hasOwnProperty.call(nextSource, nextKey)) {
            to[nextKey] = nextSource[nextKey]
          }
        }
      }
    }
    return to
  }
}

Number.isNaN = Number.isNaN || function(value) {
  return typeof value === 'number' && isNaN(value)
}

if (!history.pushState) {
  console.log('history.pushState not available')
  history.pushState = function() {}
}