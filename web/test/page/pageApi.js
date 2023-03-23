function Page(mainElement) {
  if (!mainElement) {
    mainElement = function () {
      return S('body')
    }
  }
  if (typeof mainElement !== 'function') {
    var el = mainElement
    mainElement = function () {
      return el
    }
  }
  var api = {
    getInput: function (selector) {
      return Input(findSingle(selector, mainElement))
    },
    getRadioLabel: function (selector) {
      return Input(function () {
        return mainElement().find(selector)
      })
    },
    setInputValue: function (selector, value, index) {
      return function () {
        var input = api.getInput(selector)
        var isRadio = input.attr('type') === 'radio'
        var visibleElement = isRadio ? api.getRadioLabel(selector) : input
        return wait
          .until(visibleElement.isVisible)()
          .then(function () {
            return input.setValue(value, index)
          })
          .then(wait.forAjax)
      }
    },
    getInputValue: function (selector) {
      return api.getInput(selector).value()
    },
    getInputOptions: function (selector) {
      return api.getInput(selector).getOptions()
    },
    button: function (el) {
      return Clickable(el)
    },
    elementText: function (id) {
      return api.elementTextBySelector('#' + escapeSelector(id))
    },
    elementTextBySelector: function (selector) {
      var found = mainElement().find(selector).first()
      if (
        found.prop('tagName') === 'TEXTAREA' ||
        found.prop('tagName') === 'INPUT' ||
        found.prop('tagName') === 'SELECT'
      ) {
        throw new Error('Use Input.value() to read inputs from form elements')
      }
      return found.text().trim()
    },
    classAttributeOf: function (htmlId) {
      return mainElement()
        .find('#' + escapeSelector(htmlId))
        .first()
        .attr('class')
    }
  }
  return api

  function Input(el) {
    return {
      element: function () {
        return el()
      },
      value: function () {
        var e = el()
        var ic = e.find('.input-container input')
        if (ic.length !== 0) {
          e = ic
        }
        if (inputType(e) === 'CHECKBOX') {
          return e.is(':checked')
        }
        return e.val()
      },
      attr: function (name) {
        return el().attr(name)
      },
      isVisible: function () {
        return isElementVisible(el)
      },
      isEnabled: function () {
        return el().is(':enabled')
      },
      setValue: function (value, i) {
        var input = el()
        var index = i || 0
        switch (inputType(input)) {
          case 'EMAIL':
          case 'TEXT':
          case 'NUMBER':
          case 'PASSWORD':
          case 'TEXTAREA':
            if (window.callPhantom) {
              input.val(value)
            } else {
              var domElem = el()[0]
              // Workaround for react-dom > 15.6
              // React tracks input.value = 'foo' changes too, so when the event is dispatched, it doesn't see any changes in the value and thus the event is ignored
              Object.getOwnPropertyDescriptor(
                Object.getPrototypeOf(domElem),
                'value'
              ).set.call(domElem, value)
            }
            return triggerEvent(input, 'input')()
          case 'CHECKBOX':
            if (value != input.is(':checked')) {
              if (window.callPhantom) {
                input.prop('checked', value)
              }
              return click(input)()
            }
            break
          case 'RADIO':
            var radioOption = _(input).find(function (item) {
              return $(item).prop('value') == value
            })
            // eslint-disable-next-line no-use-before-define
            if (!option) throw new Error('Option ' + value + ' not found')
            S(radioOption).click()
            return click(S(radioOption))()
          case 'SELECT':
            var option = _(input.children()).find(function (item) {
              return $(item).prop('value') == value
            })
            if (!option)
              throw new Error(
                'Option ' + value + ' not found in ' + htmlOf(input)
              )
            input.val($(option).attr('value'))
            return triggerEvent(input, 'change')()
          case 'DROPDOWN': // Dropdown.jsx
            if (!findSingle('.options', S(input))().hasClass('open')) {
              click(findSingle('.select', S(input)), 'click')()
            }

            var result = S(input)
              .find('.options li.option')
              .filter(function (i, v) {
                return $(v).text().includes(value)
              })[index]

            if (result) {
              return triggerEvent(result, 'mousedown')()
            } else {
              throw new Error(
                'Option with value ' +
                  value +
                  ' and index ' +
                  index +
                  ' was not found.'
              )
            }
          case 'AUTOCOMPLETE': // Autocomplete.jsx
            var selectedItem = findSingle('.results .selected', input)

            return Page(input)
              .setInputValue('input', value)()
              .then(wait.untilVisible(selectedItem))
              .then(click(selectedItem))

          default:
            throw new Error('Unknown input type: ' + inputType(input))
        }
      },
      getOptions: function () {
        var input = el()
        switch (inputType(input)) {
          case 'DROPDOWN': // Dropdown.jsx
            return textsOf(input.find('.option')).map(sanitizeText)
          default:
            throw new Error('getOptions not supported for ' + inputType(input))
        }
      }
    }

    function inputType(el) {
      if (el.prop('tagName') == 'SELECT' || el.prop('tagName') == 'TEXTAREA')
        return el.prop('tagName')
      else if ($(el).hasClass('dropdown'))
        // Dropdown.jsx
        return 'DROPDOWN'
      else if ($(el).hasClass('autocomplete'))
        // Autocomplete.jsx
        return 'AUTOCOMPLETE'
      else return el.prop('type').toUpperCase()
    }
  }

  function Clickable(el) {
    return {
      element: function () {
        return el()
      },
      isDisabled: function () {
        return !el().is(':enabled')
      },
      isEnabled: function () {
        return el().is(':enabled')
      },
      isVisible: function () {
        return isElementVisible(el())
      },
      text: function () {
        return el().text()
      },
      click: function () {
        click(el().first())()
      }
    }
  }

  function escapeSelector(s) {
    return s.replace(/(:|\.|\[|\])/g, '\\$1')
  }
}
