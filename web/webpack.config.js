const webpack = require('webpack')
const CopyWebpackPlugin = require('copy-webpack-plugin')
const path = require('path')
const autoprefixer = require('autoprefixer')

module.exports = {
  entry: {
    main: './app/Koski.jsx',
    login: './app/Login.jsx',
    pulssi: './app/Pulssi.jsx'
  },
  output: {
    path: __dirname + '/../target/webapp',
    filename: 'js/koski-[name].js'
  },
  stats: {
    minimal: true
  },
  module: {
    rules: [
      {
        test: /\.jsx?$/,
        include: [__dirname + '/app'],
        use: {
          loader: 'babel-loader',
          options: {
            cacheDirectory: true,
            presets: ['es2015', 'react', 'stage-3']
          }
        }
      },
      {
        test: /\.jsx?$/,
        exclude: /node_modules/,
        loader: "eslint-loader",
        options: {
          failOnWarning: !!process.env.failOnWarning,
          failOnError: true
        }
      },
      {
        test: /\.less$/,
        use: [
          {
            loader: "style-loader"
          },
          {
            loader: "css-loader"
          },
          {
            loader: 'postcss-loader',
            options: {
              plugins: function () {
                return [autoprefixer('last 2 versions')]
              }
            }
          },
          {
            loader: "less-loader"
          }
        ]
      }
    ]
  },
  plugins: [
    new CopyWebpackPlugin(
      [
        {from: 'static'},
        {from: 'test', to: 'test'},
        {from: 'WEB-INF', to: 'WEB-INF'},
        {from: 'node_modules/codemirror/lib/codemirror.js', to: 'js/codemirror'},
        {from: 'node_modules/codemirror/mode/javascript/javascript.js', to: 'js/codemirror'},
        {from: 'node_modules/codemirror/lib/codemirror.css', to: 'css/codemirror'}
      ]
    )
  ]
}
