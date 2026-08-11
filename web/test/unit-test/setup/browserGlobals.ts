/**
 * Minimaaliset selainglobaalit, jotta app-koodia (esim. i18n) voi importata node-ajossa.
 * Importoi tämä ennen muita app-importteja — ES-moduulit ajetaan importtien järjestyksessä.
 */
const g = global as any
g.window = g.window || { koskiLocalizationMap: {} }
g.document = g.document || { cookie: '' }

export {}
