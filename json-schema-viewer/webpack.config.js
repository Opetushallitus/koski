const path = require('path')
const CssMinimizerPlugin = require('css-minimizer-webpack-plugin')

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
        resourceQuery: /highlight-theme/,
        type: 'asset/resource',
        generator: { filename: 'styles/highlight-default.css' }
      },
      {
        test: /\.(css|png|gif)$/,
        resourceQuery: { not: [/asset/, /highlight-theme/] },
        type: 'asset/resource',
        generator: { filename: '[path][name][ext]' }
      },
      { test: /\.ts$/, use: 'ts-loader' }
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
