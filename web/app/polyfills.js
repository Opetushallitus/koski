import PolyfillBind from './polyfill-bind.js'
import fetch from 'whatwg-fetch'
import Promise from 'es6-promise'
import nothing from './window-error-handler.js'
if (!String.prototype.startsWith) {
  String.prototype.startsWith = function(searchString, position) {
    position = position || 0
    return this.indexOf(searchString, position) === position
  }
}