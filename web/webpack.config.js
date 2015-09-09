const webpack = require("webpack")

module.exports = {
  entry: "./Tor.jsx",
  output: {
    path: __dirname + "/dist",
    filename: "bundle.js"
  }
}
