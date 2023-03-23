'use strict'
;(function (plugin) {
  if (
    typeof require === 'function' &&
    typeof exports === 'object' &&
    typeof module === 'object'
  ) {
    // NodeJS
    module.exports = plugin
  } else if (typeof define === 'function' && define.amd) {
    // AMD
    define(function () {
      return plugin
    })
  } else {
    // Other environment (usually <script> tag): plug in to global chai instance directly.
    chai.use(plugin)
  }
})(function (chai, utils) {
  function equalIgnoreNewlines(str1, str2) {
    if (typeof str1 !== 'string' || typeof str2 !== 'string') {
      return false
    }
    return str1.replace(/\r?\n|\r/g, ' ') === str2.replace(/\r?\n|\r/g, ' ')
  }

  chai.Assertion.addMethod('equalIgnoreNewlines', function (expected, msg) {
    var actual = this._obj
    if (msg) {
      utils.flag(this, 'message', msg)
    }
    return this.assert(
      equalIgnoreNewlines(actual, expected),
      'expected #{this} to equal #{exp} ignoring newlines',
      'expected #{this} not to equal #{exp} ignoring newlines',
      expected,
      this._obj,
      true
    )
  })
})
