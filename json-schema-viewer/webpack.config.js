const getTargets = require('@babel/helper-compilation-targets').default
const CssMinimizerPlugin = require('css-minimizer-webpack-plugin')
const path = require('path')

// Kohdeselaimet luetaan Kosken .browserslistrc:stä samalla tavalla kuin web/webpack.config.js:ssä.
const targets = getTargets({}, { configPath: __dirname })

const swcRule = (test, syntax) => ({
  test,
  include: path.join(__dirname, 'src'),
  use: {
    loader: 'swc-loader',
    options: { env: { targets }, jsc: { parser: { syntax } } }
  }
})

module.exports = (_, argv = {}) => ({
  context: path.join(__dirname, 'src'),
  entry: './index.ts',
  devtool: argv.mode === 'development' ? 'inline-source-map' : false,
  output: {
    path: path.join(__dirname, '../target/webapp/koski/json-schema-viewer'),
    filename: 'js/json-schema-viewer.js',
    clean: true
  },
  // JsonSchemaViewerHtmlServlet lataa alla tuotetut jQuery-tiedostot
  // script-tageilla ennen viewer-bundlea.
  externals: { jquery: 'jQuery' },
  module: {
    rules: [
      {
        // Tuota jQuery, Migrate ja jQuery Mobile erillisinä tiedostoina bundlaamisen sijaan;
        // servlet lataa ne script- ja link-tageilla.
        resourceQuery: /asset/,
        type: 'asset/resource',
        generator: { filename: 'jquery/[name][ext]' }
      },
      {
        test: /\.(css|png|gif)$/,
        resourceQuery: { not: [/asset/] },
        type: 'asset/resource',
        generator: { filename: '[path][name][ext]' }
      },
      swcRule(/\.ts$/, 'typescript'),
      swcRule(/\.js$/, 'ecmascript')
    ]
  },
  optimization: {
    minimizer: [
      // Webpack korvaa '...'-merkkijonon oletusminimoijilla, jotta myös JavaScript minifioidaan.
      '...',
      new CssMinimizerPlugin({ include: /jquery\.mobile\.css$/ })
    ]
  },
  watchOptions: { ignored: /node_modules/ }
})
