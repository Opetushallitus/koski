const webpack = require('webpack')
const CopyWebpackPlugin = require('copy-webpack-plugin');
const path = require('path')

module.exports = {
  entry: {
    main: './app/Koski.jsx',
    login: './app/Login.jsx',
    documentation: './app/documentation/documentation.js'
  },
  output: {
    path: __dirname + '/../target/webapp',
    filename: 'js/koski-[name].js'
  },
  eslint: {
    failOnWarning: !!process.env.failOnWarning,
    failOnError: true
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
  },
  plugins: [
    new CopyWebpackPlugin(
      [
        { from: 'static'},
        { from: 'test', to: 'test'},
        { from: 'WEB-INF', to: 'WEB-INF'},
        { from: 'node_modules/codemirror/lib/codemirror.js', to: 'js/codemirror' },
        { from: 'node_modules/codemirror/mode/javascript/javascript.js', to: 'js/codemirror' },
        { from: 'node_modules/codemirror/lib/codemirror.css', to: 'css/codemirror' }
      ]
    )
  ]
}
