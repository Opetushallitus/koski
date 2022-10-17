import './window-error-handler.js'

// These are needed for IE11 (not much else)
import 'whatwg-fetch'
import 'es6-promise/auto'
import 'element-closest'
import 'core-js/fn/string/starts-with'
import 'core-js/fn/string/ends-with'
import 'core-js/fn/string/includes'
import 'core-js/fn/array/find'
import 'core-js/fn/array/find-index'
import 'core-js/fn/array/includes'
import 'core-js/fn/array/fill'
import 'core-js/fn/object/assign'
import 'core-js/fn/object/values'
import 'core-js/fn/number/is-nan'
import 'core-js/fn/number/is-integer'
import 'core-js/es6/map'
import 'core-js/es6/set'

if (!history.pushState) {
  console.log('history.pushState not available')
  history.pushState = function () {}
}
