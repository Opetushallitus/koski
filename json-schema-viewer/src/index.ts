import 'jquery/dist/jquery.min.js?asset'
import 'jquery-migrate/dist/jquery-migrate.min.js?asset'
import '../vendor/jquery.mobile.min.js?asset'
import '../vendor/jquery.mobile.min.css?asset'
import './styles/json-schema-viewer.css'
import './styles/json-schema-popup.css'
import 'highlight.js/styles/default.css?highlight-theme'
import './images/loader.gif'
import './images/logo.png'
import './images/logo_sprite.png'
import JSV from './viewer'
import tv4 from './tv4-async-load'

// Servletin bootstrap käyttää näitä globaaleja.
Object.assign(window, {
  JSV,
  tv4
})
