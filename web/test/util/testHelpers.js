var expect = chai.expect
chai.config.truncateThreshold = 0; // disable truncating

timeout = (function() {
  var defaultTimeout = testTimeoutDefault

  return {
    /**
     * Overrides timeout for wait.until calls.
     * This is a helper that allows a global override in case the actual wait.until calls are buried within abstractions.
     */
    overrideWaitTime: function(timeoutMs) {
      return function() { testTimeoutDefault = timeoutMs }
    },
    /**
     * Resets timeout for wait.until calls.
     * This is a helper that allows resetting the global wait timeout.
     */
    resetDefaultWaitTime: function() {
      return function() { testTimeoutDefault = defaultTimeout }
    }
  }
})()

function S(selector) {
  try {
    if (!testFrame() || !testFrame().jQuery) {
      return $([])
    }
    return testFrame().jQuery(selector)
  } catch (e) {
    console.log("Premature access to testFrame.jQuery, printing stack trace.")
    console.log(new Error().stack);
    throw e;
  }
}

function findSingle(selector, base) {
  return function() {
    var result = findFirst(selector, base)()
    if (result.length > 1) {
      throw new Error(result.length + " instances of " + selector + " found")
    }
    return result
  }
}

function findFirst(selector, base) {
  return function() {
    var baseElem = toElement(base)
    var result = baseElem ? baseElem.find(selector) : S(selector)
    if (result.length == 0) {
      var context = baseElem ? htmlOf(baseElem) : "document"
      throw new Error("Element " + selector + " not found in " + context)
    }
    return result
  }
}

function findFirstNotThrowing(selector, base) {
  var baseElem = toElement(base)
  var result = baseElem ? baseElem.find(selector) : S(selector)
  return result.length > 0 ? result : undefined
}

wait = {
  waitIntervalMs: 10,
  until: function(condition, maxWaitMs) {
    return function() {
      if (maxWaitMs == undefined) maxWaitMs = testTimeoutDefault;
      var deferred = Q.defer()
      var count = Math.floor(maxWaitMs / wait.waitIntervalMs);

      (function waitLoop(remaining) {
        if (condition()) {
          deferred.resolve()
        } else if (remaining === 0) {
          const errorStr = "timeout of " + maxWaitMs + "ms in wait.until for condition:\n" + condition
          console.error(new Error(errorStr), condition)
          deferred.reject(errorStr)
        } else {
          setTimeout(function() {
            waitLoop(remaining-1)
          }, wait.waitIntervalMs)
        }
      })(count)
      return deferred.promise
    }
  },
  untilFalse: function(condition) {
    return wait.until(function() { return !condition()})
  },
  forMilliseconds: function(ms) {
    return function() {
      var deferred = Q.defer()
      setTimeout(function() {
        deferred.resolve()
      }, ms)
      return deferred.promise
    }
  },
  forAjax: function() {
    return wait.forMilliseconds(1)().then(wait.until(isNotLoading)).then(wait.forMilliseconds(1))
  },
  prepareForNavigation: function() {
    var frame = testFrame()
    frame.myWaitForNavigation = frame.location.href
  },
  forNavigation: function() {
    return wait.until(function() {
      var frame = testFrame()
      return frame.myWaitForNavigation !== frame.location.href
    })().then(wait.forAjax)
  },
  untilVisible: function(el) {
    return wait.until(function() { return isElementVisible(el) })
  },
  untilHidden: function(el) {
    return wait.until(function() { return !isElementVisible(el) })
  }
}

function isLoading() {
  return S('.loading').length > 0
}

var isNotLoading = not(isLoading)

function not(f) {
  return function() {
    return !f()
  }
}

function getJson(url) {
  return Q($.ajax({url: url, dataType: "json" }))
}

function postJson(url, data) {
  return sendJson(url, data, 'post')
}

function putJson(url, data) {
  return sendJson(url, data, 'put')
}

function sendJson(url, data, method) {
  return sendAjax(url, 'application/json', JSON.stringify(data), method)
}

function sendAjax(url, contentType, data, method) {
  return Q($.ajax({
    type: method,
    url: url,
    data: data,
    contentType : contentType,
    dataType: 'json'
  }))
}

function testFrame() {
  return $("#testframe").get(0).contentWindow
}

function reloadTestFrame() {
  testFrame().document.location.reload()
}

function goBack() {
  testFrame().history.back()
}

function goForward() {
  testFrame().history.forward()
}

function insertExample(name) {
  return function() {
    return getJson('/koski/api/documentation/examples/' + name).then(function(data) {
      return putJson('/koski/api/oppija', data).catch(function(){})
    })
  }
}

var isIE = (navigator.appName == 'Microsoft Internet Explorer' ||  !!(navigator.userAgent.match(/Trident/) || navigator.userAgent.match(/rv:11/)) || (typeof $.browser !== "undefined" && $.browser.msie == 1))

function triggerEvent(selector, eventName) {
  return function() {
    var element = toElement(selector)
    if (!element.length) {
      throw new Error("triggerEvent: element not visible")
    }

    var evt
    if(window.callPhantom || isIE) {
      evt = testFrame().document.createEvent('HTMLEvents');
      evt.initEvent(eventName, true, true);
    } else {
      evt = new MouseEvent(eventName, {
        'view': window,
        'bubbles': true,
        'cancelable': true
      })
    }
    element.toArray().forEach(function(elem) {
      elem.dispatchEvent(evt);
    })

    return wait.forAjax()
  }
}

function click(element) {
  return triggerEvent(element, 'click')
}

function seq() {
  var tasks = Array.prototype.slice.call(arguments)
  return function() {
    var promise = Q()
    tasks.forEach(function(task) { promise = promise.then(task) })
    return promise
  }
}

if (!$("#testframe").length) {
  $(document.body).append("<iframe id='testframe'></iframe>")
}

function openPage(path, predicate, width) {
  if (!predicate) {
    predicate = function() { return testFrame().jQuery }
  }
  if (!width) {
    width = 1400
  }
  function addScriptToDocument(w, src) {
    var jquery = document.createElement("script")
    jquery.type = "text/javascript"
    jquery.src = src
    $(w).contents().find("head")[0].appendChild(jquery)
  }
  return function() {
    var newTestFrame = $('<iframe>').attr({src: path, width: width, height: 2000, id: "testframe"}).on("load", function() {
      addScriptToDocument(this, "/koski/test/lib/jquery.js")
    })
    $("#testframe").replaceWith(newTestFrame)
    return wait.until(
        predicate,
        testTimeoutPageLoad
    )().then(function() {
        testFrame().onerror = function(err) { window.uiError = err; } // Hack: force mocha to fail on unhandled exceptions
    })
  }
}

function takeScreenshot(name) {
  return function() {
    var date = new Date()
    var path = "target/screenshots/"
    var filename = path + (name.toLowerCase().replace(/ /g, '_').replace(/ä/g, 'a').replace(/ö/g, 'o').replace(/"/g, '') || date.getTime())
    console.log("Taking screenshot web/" + filename + ".png")
    if (window.callPhantom) {
      callPhantom({'screenshot': filename})
    } else {
      return Q(html2canvas(testFrame().document.body).then(function(canvas) {
        $(document.body).append($("<div>").append($("<h4>").text("Screenshot: " + filename)).append($(canvas)))
      }))
    }
  }
}

function textsOf(elements) {
  elements = evalFunc(elements)
  return toArray(elements).map(function(el) { return $(el).text().trim() })
}

function sanitizeText(text) {
  return text.replace(/ *\n+ */g, "\n").replace(/ +/g, " ").replace(/||||||\u00ad/g, "")
}

function extractAsText(el, subElement) {
  el = evalFunc(el)
  if (isJQuery(el) && el.length > 1) {
    return extractMultiple(el)
  }
  if (el.nodeType === 3) return el.textContent.trim()
  if (el.nodeType && el.nodeType != 1) return ""

  el = $(el)
  var element = el[0]

  if (el.hasClass("toggle-edit") || el.hasClass("opintosuoritusote")) return ""

  var isBlockElement =
    element.tagName == "SECTION" ||
    ["block", "table", "table-row", "table-row-group", "list-item"].indexOf((element.currentStyle || window.getComputedStyle(element, "")).display) >= 0

  var separator = (isBlockElement ? "\n" : "")
  var text = sanitizeText(separator + extractMultiple(element.childNodes) + separator)
  return subElement && isBlockElement ? text : text.trim()

  function nonEmpty(x) {
    return x.trim().length > 0
  }
  function extractMultiple(elements) {
    return sanitizeText(toArray(elements).map(extractSubElement).filter(nonEmpty).join(" ")).trim()
  }
  function extractSubElement(el) {
    return extractAsText(el, true)
  }
}

function htmlOf(el) {
  return S(el).clone().wrap('<div>').parent().html()
}

function hrefOf(el) {
  return S(el)[0].href
}

function isJQuery(el) {
  return typeof(el.children) == "function"
}

function toArray(elements) {
  elements = evalFunc(elements)
  if (isJQuery(elements)) elements = elements.get() // <- a jQuery object
  return Array.prototype.slice.apply(elements)
}

function evalFunc(f) {
  while (typeof f == 'function') f = f()
  return f
}

function isElementVisible(el) {
  try{
    el = toElement(el)
    if (el.get) el = el.get(0)  // <- extract HTML element from jQuery object
    if (!el) {
      return false;
    } // <- `undefined` -> invisible
    return $(el).is(":visible")
  } catch (e) {
    if (e.message.indexOf('not found') > 0) return false
    throw e
  }
}

function subElement(el, selector) {
  return function() {
    if (!el) return S(selector)
    return toElement(el).find(selector)
  }
}

function toElement(el) {
  if (!el) return el
  return S(evalFunc(el))
}

(function improveMocha() {
  var origBefore = before
  before = function() {
    Array.prototype.slice.call(arguments).forEach(function(arg) {
      if (typeof arg !== "function") {
        throw ("not a function: " + arg)
      }
      origBefore(arg)
    })
  }
  var origAfter = after
  after = function() {
    Array.prototype.slice.call(arguments).forEach(function(arg) {
      if (typeof arg !== "function") {
        throw ("not a function: " + arg)
      }
      origAfter(arg)
    })
  }
})()

function resetFixtures() {
  return Q($.ajax({ url: '/koski/fixtures/reset', method: 'post'}))
}

function syncTiedonsiirrot() {
  return Q($.ajax({ url: '/koski/fixtures/sync-tiedonsiirrot', method: 'post'}))
}

function syncPerustiedot() {
  return Q($.ajax({ url: '/koski/fixtures/sync-perustiedot', method: 'post'}))
}

function mockHttp(url, result) {
  return function() { testFrame().http.mock(url, result) }
}

function getAsTextArray(selector) {
  return S(selector).map((_, elem) => S(elem).text()).toArray()
}

function getValuesAsArray(selector) {
  return S(selector).map((_, elem) => S(elem).val()).toArray()
}

function setInputValue(elementOrSelector, value) {
  // Setter .value= is not working as we wanted because React library overrides input value setter but we can call the function directly on the input as context.
  // See: https://stackoverflow.com/questions/23892547/what-is-the-best-way-to-trigger-onchange-event-in-react-js
  const input = S(elementOrSelector)[0]
  const nativeInputValueSetter = Object.getOwnPropertyDescriptor(window.HTMLInputElement.prototype, "value").set
  nativeInputValueSetter.call(input, value)
  input.dispatchEvent(new Event('input', { bubbles: true }))
}
