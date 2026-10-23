/**
 * Minimaaliset selainglobaalit, jotta app-koodia (esim. i18n) voi importata node-ajossa.
 * Vitestin setupFiles alustaa nämä ennen testitiedostojen app-importteja.
 */
const g = global as any
g.window = g.window || { koskiLocalizationMap: {} }
g.document = g.document || { cookie: '' }

export {}
