var expect = chai.expect
chai.should()
chai.config.truncateThreshold = 0; // disable truncating

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
  base = toElement(base)
  var result = base ? base.find(selector) : S(selector)
  if (result.length == 0) {
    var context = base ? htmlOf(base) : "document"
    throw new Error("Element " + selector + " not found in " + context)
  }
  if (result.length > 1) {
    throw new Error(result.length + " instances of " + selector + " found")
  }
  return result
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
  untilVisible: function(el) {
    return wait.until(function() { return isElementVisible(el) })
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

function triggerEvent(element, eventName) {
  element = toElement(element)
  if (!element.length) {
    throw new Error("triggerEvent: element not visible")
  }

  var evt
  if(window.callPhantom) {
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
}

function openPage(path, predicate) {
  // To clear the onerror handler set by mocha. This causes the tests to actually crash if an unhandled exception is thrown. Without this, we get silent failures.
  // And yes, it needs to be here, to be called late enough. Otherwise, mocha installs its uncaught exception handler and the exceptions are not shown anywhere.
  // Better yet would be to make mocha fail and show those exceptions!
  window.onerror = undefined
  if (!predicate) {
    predicate = function() { return testFrame().jQuery }
  }
  function addScriptToDocument(w, src) {
    var jquery = document.createElement("script")
    jquery.type = "text/javascript"
    jquery.src = src
    $(w).contents().find("head")[0].appendChild(jquery)
  }
  return function() {
    var newTestFrame = $('<iframe>').attr({src: path, width: 1400, height: 2000, id: "testframe"}).load(function() {
      addScriptToDocument(this, "/koski/test/lib/jquery.js")
    })
    $("#testframe").replaceWith(newTestFrame)
    return wait.until(
        function() {
          return predicate()
        },
        testTimeoutPageLoad
    )().then(function() {
        window.uiError = null
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
  return toArray(elements).map(function(el) { return $(el).text().trim() })
}

function sanitizeText(text) {
  return text.replace(/ *\n+ */g, "\n").replace(/ +/g, " ").replace(/|||/g, "")
}

function extractAsText(el, subElement) {
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
  return S(el).wrap('<div>').parent().html()
}

function isJQuery(el) {
  return typeof(el.children) == "function"
}

function toArray(elements) {
  if (isJQuery(elements)) elements = elements.get() // <- a jQuery object
  return Array.prototype.slice.apply(elements)
}

function isElementVisible(el) {
  try{
    el = toElement(el)
    if (el.get) el = el.get(0)  // <- extract HTML element from jQuery object
    if (!el) return false; // <- `undefined` -> invisible
    return $(el).is(":visible")
  } catch (e) {
    if (e.message.indexOf('not found') > 0) return false
    throw e
  }
}

function toElement(el) {
  if (!el) return el
  if (typeof el == 'function') el = el()
  return S(el)
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

function debug(x) {
  console.log(x)
  return x
}

function resetFixtures() {
  return Q($.ajax({ url: '/koski/fixtures/reset', method: 'post'}))
}

function refreshIndices() {
    return Q($.ajax({ url: '/koski/fixtures/refresh', method: 'post'}))
}

function mockHttp(url, result) {
  return function() { testFrame().http.mock(url, result) }
}
