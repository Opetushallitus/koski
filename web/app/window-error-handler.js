window.onerror = function(errorMsg, url, lineNumber, columnNumber, exception) {
  var data = url + ":" + lineNumber
  if (typeof columnNumber !== "undefined") data += ":" + columnNumber
  if (typeof exception !==  "undefined") data += "\n" + exception.stack
  console.log("ERROR:", errorMsg, "at", data)
}