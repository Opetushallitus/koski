const $ = require('jquery')
const tv4 = require('tv4')

if (typeof tv4.asyncLoad === 'undefined') {
  tv4.asyncLoad = function (uri, callback, uriPrefix) {
    var missing = uri instanceof Array ? uri : tv4.getMissingUris(),
      pref = uriPrefix || ''
    if (!missing.length && !uri) {
      if (callback) {
        callback(tv4.getSchemaMap())
      } else {
        return true
      }
    } else {
      var missingSchemas = $.map(missing, function (schemaUri) {
        return $.getJSON(pref + schemaUri)
          .done(function (fetchedSchema) {
            tv4.addSchema(schemaUri, fetchedSchema)
          })
          .fail(function () {
            tv4.addSchema(schemaUri, {})
          })
      })
      $.when.apply($, missingSchemas).done(function () {
        tv4.asyncLoad(false, callback, uriPrefix)
      })
    }
  }
}
module.exports = tv4
