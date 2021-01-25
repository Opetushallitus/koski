module.exports = function (bundler) {
  bundler.addAssetType("html", require.resolve("./SkipExternalAssets"))
}
