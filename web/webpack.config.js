const CopyWebpackPlugin = require('copy-webpack-plugin')
const ESLintPlugin = require('eslint-webpack-plugin')
const path = require('path')

module.exports = {
  entry: {
    main: './app/Virkailija.jsx',
    omattiedot: './app/OmatTiedot.jsx',
    suoritusjako: './app/Suoritusjako.jsx',
    login: './app/VirkailijaLogin.jsx',
    pulssi: './app/Pulssi.jsx',
    lander: './app/Lander.jsx',
    omadata: './app/omadata/HyvaksyntaLanding.jsx',
    eisuorituksia: './app/EiSuorituksia.jsx',
    korhopankki: './app/Korhopankki.jsx',
    kayttooikeudet: './app/Kayttooikeudet.jsx'
  },
  output: {
    path: path.join(__dirname, '..', 'target/webapp/koski'),
    filename: 'js/koski-[name].js',
    publicPath: '/koski/'
  },
  stats: 'normal',
  resolve: {
    extensions: ['.js', '.jsx', '.ts', '.tsx']
  },
  module: {
    rules: [
      {
        test: /\.(js|jsx)$/,
        include: [path.join(__dirname, 'app')],
        use: {
          loader: 'babel-loader',
          options: {
            cacheDirectory: true,
            presets: ['@babel/preset-env', '@babel/preset-react']
          }
        }
      },
      {
        test: /\.(ts|tsx)$/,
        include: [path.join(__dirname, 'app')],
        use: ['ts-loader']
      },
      {
        test: /\.less$/,
        use: [
          {
            loader: 'style-loader'
          },
          {
            loader: 'css-loader'
          },
          {
            loader: 'postcss-loader',
            options: {
              postcssOptions: {
                plugins: [['postcss-preset-env', {}]]
              }
            }
          },
          {
            loader: 'less-loader'
          }
        ]
      }
    ]
  },
  plugins: [
    new ESLintPlugin({
      extensions: ['js', 'jsx', 'ts', 'tsx'],
      failOnWarning: false // Ei failata compilationia ESLint-varoituksille
    }),
    new CopyWebpackPlugin({
      patterns: [
        { from: 'static' },
        {
          from: 'test',
          to: 'test',
          globOptions: {
            ignore: ['.eslintrc']
          }
        },
        { from: 'node_modules/chai/chai.js', to: 'test/lib' },
        { from: 'node_modules/jquery/dist/jquery.js', to: 'test/lib' },
        { from: 'node_modules/mocha/mocha.js', to: 'test/lib' },
        { from: 'node_modules/mocha/mocha.css', to: 'test/css' },
        { from: 'node_modules/lodash/lodash.js', to: 'test/lib' },
        { from: 'node_modules/q/q.js', to: 'test/lib' },
        {
          from: 'node_modules/html2canvas/dist/html2canvas.js',
          to: 'test/lib'
        },
        { from: 'WEB-INF', to: '../WEB-INF' },
        {
          from: 'node_modules/codemirror/lib/codemirror.js',
          to: 'js/codemirror'
        },
        {
          from: 'node_modules/codemirror/mode/javascript/javascript.js',
          to: 'js/codemirror'
        },
        {
          from: 'node_modules/codemirror/lib/codemirror.css',
          to: 'css/codemirror'
        }
      ]
    })
  ]
}
