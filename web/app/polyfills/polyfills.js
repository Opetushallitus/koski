import './window-error-handler.js'

// These are needed for IE11 (not much else)
import 'whatwg-fetch'
import 'es6-promise/auto'
import 'element-closest'
import 'core-js/es/string/starts-with'
import 'core-js/es/string/ends-with'
import 'core-js/es/string/includes'
import 'core-js/es/array/find'
import 'core-js/es/array/find-index'
import 'core-js/es/array/includes'
import 'core-js/es/array/fill'
import 'core-js/es/object/assign'
import 'core-js/es/object/values'
import 'core-js/es/number/is-nan'
import 'core-js/es/number/is-integer'
import 'core-js/es/map'
import 'core-js/es/set'

if (!history.pushState) {
  console.log('history.pushState not available')
  history.pushState = function () {}
}
