// Customized HTML asset for parcel that supports ignoring custom external frontend dependencies. Workaround for
// parcel configurability limitation based on https://github.com/parcel-bundler/parcel/issues/1186#issuecomment-453468414
const HTMLAsset = require("parcel-bundler/lib/assets/HTMLAsset")

function shouldIgnore(file) {
  return (
    /\/virkailija-raamit\/apply-raamit.js/.test(file) ||
    /\/valpas\/localization\/set-window-properties.js/.test(file)
  )
}

class SkipExternalAssets extends HTMLAsset {
  addDependency(name, opts) {
    if (!shouldIgnore(opts.resolved)) {
      return super.addDependency(name, opts)
    }
  }

  processSingleDependency(p, opts) {
    if (shouldIgnore(p)) return p
    else return super.processSingleDependency(p, opts)
  }
}

module.exports = SkipExternalAssets
