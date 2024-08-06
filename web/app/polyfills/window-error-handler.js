window.onerror = function (errorMsg, url, lineNumber, columnNumber, exception) {
  let data = url + ':' + lineNumber
  if (typeof columnNumber !== 'undefined') data += ':' + columnNumber
  if (typeof exception !== 'undefined' && exception !== null)
    data += '\n' + exception.stack
  console.log('ERROR:', errorMsg, 'at', data)
}
