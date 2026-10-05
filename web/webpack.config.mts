import getTargets from '@babel/helper-compilation-targets'
import CopyWebpackPlugin from 'copy-webpack-plugin'
import { createRequire } from 'node:module'
import path from 'node:path'
import type { Configuration } from 'webpack'

const require = createRequire(import.meta.url)

// Kohdeselaimet määritetään .browserslistrc:stä Babelin apukirjastolla ja
// annetaan swc:lle. swc:n oma browserslist-toteutus käyttää swc-version
// mukana tulevaa selaindataa, joka voi olla vanhempaa kuin projektin
// caniuse-lite.
const targets = getTargets({}, { configPath: import.meta.dirname })

const swcRule = (test: RegExp, parser: object) => ({
  test,
  include: path.join(import.meta.dirname, 'app'),
  use: {
    loader: 'swc-loader',
    options: { env: { targets }, jsc: { parser } }
  }
})

export default (
  _: unknown,
  argv: { mode?: Configuration['mode'] } = {}
): Configuration => ({
  context: import.meta.dirname,
  devtool: argv.mode === 'development' ? 'inline-source-map' : false,
  cache: process.env.CI
    ? false
    : {
        type: 'filesystem',
        buildDependencies: {
          browserslist: [path.join(import.meta.dirname, '.browserslistrc')]
        }
      },
  entry: {
    main: './app/Virkailija.jsx',
    omattiedot: './app/OmatTiedot.jsx',
    suoritusjako: './app/Suoritusjako.jsx',
    suoritetuttutkinnot: './app/SuoritetutTutkinnot.tsx',
    aktiivisetjapaattyneetopinnot: './app/AktiivisetJaPaattyneetOpinnot.tsx',
    login: './app/VirkailijaLogin.jsx',
    pulssi: './app/Pulssi.jsx',
    lander: './app/Lander.jsx',
    omadata: './app/omadata/HyvaksyntaLanding.jsx',
    omadataoauth2: './app/omadata/OmaDataOAuth2HyvaksyntaLanding.tsx',
    eisuorituksia: './app/EiSuorituksia.jsx',
    korhopankki: './app/Korhopankki.jsx',
    kayttooikeudet: './app/Kayttooikeudet.jsx'
  },
  output: {
    path: path.join(import.meta.dirname, '..', 'target/webapp/koski'),
    filename: 'js/koski-[name].js',
    // Entry-bundlet saavat välimuistin ohituksen HtmlNodes.scala:n
    // ?buildVersion-parametrista, mutta async-chunkkien osoitteen muodostaa
    // webpackin oma runtime, joten niiden nimeen tarvitaan sisältöhash. Ilman
    // sitä selain voi julkaisun jälkeen käyttää välimuistista vanhaa chunkkia,
    // jonka tunniste ei enää vastaa uutta runtimea, jolloin sivu jää kokonaan
    // ilman tyylejä. Kehitystilassa hash jätetään pois: target-hakemistoa ei
    // siivota, joten jokainen käännös jättäisi vanhat tiedostot jäljelle.
    chunkFilename:
      argv.mode === 'production'
        ? 'js/koski-[name].[contenthash:8].js'
        : 'js/koski-[name].js',
    publicPath: '/koski/'
  },
  resolve: {
    extensions: ['.js', '.jsx', '.ts', '.tsx'],
    alias: {
      // Teema samasta highlight.js-versiosta, jota react-highlight käyttää
      'highlight.js/styles': path.join(
        path.dirname(
          require.resolve('highlight.js/package.json', {
            paths: [path.dirname(require.resolve('react-highlight'))]
          })
        ),
        'styles'
      )
    }
  },
  module: {
    rules: [
      swcRule(/\.(js|jsx)$/, { syntax: 'ecmascript', jsx: true }),
      swcRule(/\.(ts|tsx)$/, { syntax: 'typescript' }),
      {
        test: /\.woff2$/,
        type: 'asset/resource',
        generator: { filename: 'fonts/[name].[contenthash:8][ext]' }
      },
      {
        test: /\.less$/,
        use: [
          {
            loader: 'style-loader'
          },
          {
            loader: 'css-loader',
            options: {
              url: {
                filter: (url: string) =>
                  !url.startsWith('/') && !url.startsWith('data:')
              }
            }
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
        { from: 'node_modules/moment/min/moment.min.js', to: 'test/lib' },
        { from: 'node_modules/mocha/mocha.js', to: 'test/lib' },
        { from: 'node_modules/mocha/mocha.css', to: 'test/css' },
        { from: 'node_modules/lodash/lodash.js', to: 'test/lib' },
        {
          from: 'node_modules/html2canvas/dist/html2canvas.js',
          to: 'test/lib'
        },
        { from: 'WEB-INF', to: '../WEB-INF' }
      ]
    })
  ],
  watchOptions: {
    ignored: /node_modules/
  }
})
