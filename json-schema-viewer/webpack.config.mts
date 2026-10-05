import getTargets from '@babel/helper-compilation-targets'
import CssMinimizerPlugin from 'css-minimizer-webpack-plugin'
import path from 'node:path'
import type { Configuration } from 'webpack'

// Kohdeselaimet luetaan Kosken .browserslistrc:stä samalla tavalla kuin web/webpack.config.mts:ssä.
const targets = getTargets({}, { configPath: import.meta.dirname })

const swcRule = (test: RegExp, syntax: 'typescript' | 'ecmascript') => ({
  test,
  include: path.join(import.meta.dirname, 'src'),
  use: {
    loader: 'swc-loader',
    options: { env: { targets }, jsc: { parser: { syntax } } }
  }
})

export default (
  _: unknown,
  argv: { mode?: Configuration['mode'] } = {}
): Configuration => ({
  context: path.join(import.meta.dirname, 'src'),
  entry: './index.ts',
  devtool: argv.mode === 'development' ? 'inline-source-map' : false,
  cache: process.env.CI
    ? false
    : {
        type: 'filesystem',
        buildDependencies: {
          browserslist: [path.join(import.meta.dirname, '.browserslistrc')]
        }
      },
  output: {
    path: path.join(
      import.meta.dirname,
      '../target/webapp/koski/json-schema-viewer'
    ),
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
