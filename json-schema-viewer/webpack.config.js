const path = require('path')

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
      { test: /\.ts$/, use: 'ts-loader' }
    ]
  },
  watchOptions: { ignored: /node_modules/ }
})
