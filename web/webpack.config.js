const webpack = require('webpack')
const CopyWebpackPlugin = require('copy-webpack-plugin')
const path = require('path')
const autoprefixer = require('autoprefixer')

module.exports = {
  entry: {
    main: './app/Virkailija.jsx',
    omattiedot: './app/OmatTiedot.jsx',
    login: './app/VirkailijaLogin.jsx',
    pulssi: './app/Pulssi.jsx',
    lander: './app/Lander.jsx',
    eisuorituksia: './app/EiSuorituksia.jsx',
    korhopankki: './app/Korhopankki.jsx'
  },
  output: {
    path: __dirname + '/../target/webapp',
    filename: 'js/koski-[name].js'
  },
  stats: {
    minimal: true
  },
  resolve: {
    extensions: ['.js', '.jsx']
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
        {from: 'node_modules/chai/chai.js', to: 'test/lib'},
        {from: 'node_modules/jquery/dist/jquery.js', to: 'test/lib'},
        {from: 'node_modules/mocha/mocha.js', to: 'test/lib'},
        {from: 'node_modules/mocha/mocha.css', to: 'test/css'},
        {from: 'node_modules/lodash/lodash.js', to: 'test/lib'},
        {from: 'node_modules/q/q.js', to: 'test/lib'},
        {from: 'node_modules/html2canvas/dist/html2canvas.js', to: 'test/lib'},
        {from: 'WEB-INF', to: 'WEB-INF'},
        {from: 'node_modules/codemirror/lib/codemirror.js', to: 'js/codemirror'},
        {from: 'node_modules/codemirror/mode/javascript/javascript.js', to: 'js/codemirror'},
        {from: 'node_modules/codemirror/lib/codemirror.css', to: 'css/codemirror'}
      ]
    )
  ]
}
