const webpack = require('webpack')

module.exports = {
  entry: './app/Tor.jsx',
  output: {
    path: __dirname + '/dist',
    filename: 'bundle.js'
  },
  module: {
    loaders: [
      {
        test: /\.jsx?$/,
        loader: 'babel',
        include: [ __dirname + '/app' ]
      },
      {test: /\.jsx?$/, loader: 'eslint-loader', exclude: /node_modules/},
      {
        test: /\.less$/,
        loader: 'style!css!autoprefixer-loader?browsers=last 2 version!less'
      }
    ]
  }
}
