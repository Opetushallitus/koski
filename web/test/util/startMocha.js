console.log('Starting mocha')
var runner = mocha.run()
runner.errors = []
if (window.callPhantom) {
  console.log('PhantomJS mode')
}

function mapError(e) {
  var logEntry = { title: e.title }
  if (e.err) logEntry.message = e.err.message
  if (e.parent) logEntry.parent = mapError(e.parent)
  return logEntry
}
runner.on('fail', function (e) {
  runner.errors.push(mapError(e))
})

runner.on('fail', function (e) {
  takeScreenshot(e.title)()
})

$(document).keyup(function (e) {
  if (e.keyCode === 27) {
    runner.abort()
  }
})
