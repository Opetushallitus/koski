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