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
    return wait.forMilliseconds(1)().then(wait.until(KoskiPage().isReady))
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

function triggerEvent(element, eventName) {
  if (!element.length) throw new Error("triggerEvent: element not visible")
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
  return function() {
    var newTestFrame = $('<iframe>').attr({src: path, width: 1280, height: 800, id: "testframe"}).load(function() {
      var jquery = document.createElement("script")
      jquery.type = "text/javascript"
      jquery.src = "//code.jquery.com/jquery-1.11.1.min.js"
      $(this).contents().find("head")[0].appendChild(jquery)
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

function takeScreenshot() {
  if (window.callPhantom) {
    var date = new Date()
    var filename = "target/screenshots/" + date.getTime()
    console.log("Taking screenshot web/" + filename + ".png")
    callPhantom({'screenshot': filename})
  }
}

function textsOf(elements) {
  return Array.prototype.slice.apply(elements.get()).map(function(el) { return $(el).text() })
}

function isElementVisible(el) {
  if (el.get) el = el.get(0)  // <- extract HTML element from jQuery object
  if (!el) return false; // <- `undefined` -> invisible

  if(window.callPhantom) {
    // For some reason, the "actually visible" logic below fails in phantom, so we fallback to less strict check.
    return $(el).is(":visible")
  }

  // Check for actual visibility. Returns false if the element is not scrolled into view, or is obscured by
  // another element with higher z-index.
  
  var doc      = testFrame().document
  var rect     = el.getBoundingClientRect()
  var vWidth   = window.innerWidth || doc.documentElement.clientWidth
  var vHeight  = window.innerHeight || doc.documentElement.clientHeight
  var efp      = function (x, y) { return doc.elementFromPoint(x, y) };

  // Return false if it's not in the viewport
  if (rect.right < 0 || rect.bottom < 0 || rect.left > vWidth || rect.top > vHeight) {
    return false;
  }

  // Return true if any of its four corners are visible
  return (
    el.contains(efp(rect.left,  rect.top))
    ||  el.contains(efp(rect.right, rect.top))
    ||  el.contains(efp(rect.right, rect.bottom))
    ||  el.contains(efp(rect.left,  rect.bottom))
  );
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
})()

function debug(x) {
  console.log(x)
  return x
}


function resetFixtures() {
  return Q($.ajax({ url: '/koski/fixtures/reset', method: 'post'}))
}

function mockHttp(url, result) {
  return function() { testFrame().http.mock(url, result) }
}