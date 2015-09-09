const webpack = require("webpack")

module.exports = {
  entry: "./app/Tor.jsx",
  output: {
    path: __dirname + "/dist",
    filename: "bundle.js"
  }
}
