function FakePiwik() {
  return {
    reset: function() {
      testFrame().window._paq.length = 0
    },
    getQueuedMethodCalls: function() {
      return testFrame().window._paq.slice()
    }
  }
}
