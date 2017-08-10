function Page(mainElement) {
  if (!mainElement) {
    mainElement = function() {return S('body')}
  }
  if (typeof mainElement != 'function') {
    var el = mainElement
    mainElement = function () { return el }
  }
  var api = {
    getInput: function(selector) {
      return Input(function () {
        return findSingle(selector, mainElement())
      })
    },
    getRadioLabel: function(selector) {
      return Input(function () {
        return mainElement().find(selector)
      })
    },
    setInputValue: function(selector, value, exact) {
      return function() {
        var input = api.getInput(selector)
        var isRadio = input.attr('type') === 'radio'
        var visibleElement = isRadio ? api.getRadioLabel(selector) : input
        return wait.until(visibleElement.isVisible)()
          .then(function() {return input.setValue(value, exact)})
          .then(wait.forAjax)
      }
    },
    getInputValue: function(selector) {
      return api.getInput(selector).value()
    },
    getInputOptions: function(selector) {
      return api.getInput(selector).getOptions()
    },
    button: function(el) {
      return Clickable(el)
    },
    elementText: function(id) {
      return api.elementTextBySelector('#' + escapeSelector(id))
    },
    elementTextBySelector: function(selector) {
      var found = mainElement().find(selector).first()
      if (found.prop('tagName') === 'TEXTAREA' ||
        found.prop('tagName') === 'INPUT' ||
        found.prop('tagName') === 'SELECT') {
        throw new Error('Use Input.value() to read inputs from form elements')
      }
      return found.text().trim()
    },
    classAttributeOf: function(htmlId) {
      return mainElement().find('#' + escapeSelector(htmlId)).first().attr('class')
    }
  }
  return api

  function Input(el) {
    return {
      element: function() {
        return el()
      },
      value: function() {
        return el().val()
      },
      attr: function(name) {
        return el().attr(name)
      },
      isVisible: function() {
        return isElementVisible(el)
      },
      isEnabled: function () {
        return el().is(':enabled')
      },
      setValue: function(value, exact) {
        var input = el()
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
              Object.getOwnPropertyDescriptor(Object.getPrototypeOf(domElem), 'value').set.call(domElem, value)
            }
            triggerEvent(input, 'input')
            break
          case 'CHECKBOX':
            if (value != input.is(':checked')) {
              if(window.callPhantom) {
                input.prop('checked', value)
              }
              triggerEvent(input, 'click')
            }
            break
          case 'RADIO':
            var radioOption = _(input).find(function(item) { return $(item).prop('value') == value })
            if (!option) throw new Error('Option ' + value + ' not found')
            S(radioOption).click()
            triggerEvent(S(radioOption), 'click')
            break
          case 'SELECT':
            var option = _(input.children()).find(function(item) { return $(item).prop('value') == value })
            if (!option) throw new Error('Option ' + value + ' not found in ' + htmlOf(input))
            input.val($(option).attr('value'))
            triggerEvent(input, 'change')
            break
          case 'DROPDOWN': // Dropdown.jsx
            if (!findSingle('.options', S(input)).hasClass('open')) {
              triggerEvent(findSingle('.select', S(input)), 'click')
            }

            if (exact) {
              var result = S(input).find('.options li').filter(function(i, v) {return $(v).text().trim() === value})
              if (result.length !== 1) {
                throw new Error("Element '.options li' filtered by text '"+value+"' result length "+result.length+' !== 1 in '+S(input))
              }
              triggerEvent(result, 'mousedown')
            }
            else {
              triggerEvent(findSingle('.options li:contains(' + value + ')', S(input)), 'mousedown')
            }
            break
          case 'AUTOCOMPLETE': // Autocomplete.jsx
            function selectedItem() { return findSingle('.results .selected', input) }

            return Page(input).setInputValue('input', value)()
              .then(wait.untilVisible(selectedItem))
              .then(function() { triggerEvent(selectedItem, 'click')})

				  default:
						throw new Error('Unknown input type: ' + inputType(input))
        }
      },
      getOptions: function() {
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
      else if ($(el).hasClass('dropdown')) // Dropdown.jsx
        return 'DROPDOWN'
      else if ($(el).hasClass('autocomplete')) // Autocomplete.jsx
        return 'AUTOCOMPLETE'
      else
        return el.prop('type').toUpperCase()
    }
  }

  function Clickable(el) {
    return {
      element: function() {
        return el()
      },
      isEnabled: function () {
        return el().is(':enabled')
      },
      isVisible: function() {
        return isElementVisible(el())
      },
      text: function() {
        return el().text()
      },
      click: function () {
        triggerEvent(el().first(), 'click')
      }
    }
  }

  function escapeSelector(s){
    return s.replace( /(:|\.|\[|\])/g, '\\$1' )
  }
}
