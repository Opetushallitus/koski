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
  // script-tageilla ennen viewer-bundlea, koska vanha jQuery Mobile 1.4.5 tarvitsee
  // globaalin jQueryn ja ennen itseään ladatun Migrate-yhteensopivuuskirjaston.
  externals: { jquery: 'jQuery' },
  resolve: {
    // Servletin script-tageja varten index.ts importoi jQueryn ja Migraten valmiit
    // dist-tiedostot. jQuery 4 ja Migrate 4 sallivat package.json:n exports-kentässä
    // vain tietyt import-polut, eikä dist ole niiden joukossa, joten webpack hylkää
    // importin. Alias osoittaa suoraan node_modules-hakemistoon, jolloin
    // exports-rajausta ei sovelleta.
    alias: {
      'jquery/dist': path.join(import.meta.dirname, 'node_modules/jquery/dist'),
      'jquery-migrate/dist': path.join(
        import.meta.dirname,
        'node_modules/jquery-migrate/dist'
      )
    }
  },
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
