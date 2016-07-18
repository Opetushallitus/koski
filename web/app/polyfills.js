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
if (!Array.prototype.find) {
 Array.prototype.find = function(predicate) {
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
 }
}
if (!Array.prototype.findIndex) {
  Array.prototype.findIndex = function(predicate) {
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
  }
}
if (!Array.prototype.last){
  Array.prototype.last = function(){
    return this[this.length - 1]
  }
}
