function FakePiwik() {
  return {
    getQueuedMethodCalls: function() {
      return testFrame().window._paq.slice()
    }
  }
}
