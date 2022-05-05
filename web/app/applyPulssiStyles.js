/**
 * Tätä tiedostoa käytetään Koski-palvelun tyylitysten lataamiseen.
 * Lataus on eritelty omaan tiedostoonsa, koska Webpackin nonce-ominaisuus tulee käyttöön ainoastaan entrypointeista tehdyistä kutsuista.
 */
// eslint-disable-next-line no-undef
__webpack_nonce__ = window.nonce
import(/* webpackChunkName: "pulssi-styles" */ './style/pulssi.less')
