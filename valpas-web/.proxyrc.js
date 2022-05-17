const { createProxyMiddleware } = require("http-proxy-middleware")
const express = require("express")

const STATIC_RESPONSE_APP_PORT = 10101
const REDIRECTS = {
  "/": "/valpas/virkailija",
}

module.exports = function (app) {
  setupBackendProxy(app)
  setupRedirects(app)
}

// Staattinen apupalvelin, joka tarvitaan koska API-proxy ei tue get- eikä redirect-metodeita

const staticApp = express()
Object.entries(REDIRECTS).map(([path, target]) => {
  staticApp.get(path, (_req, res) => res.redirect(target))
  console.log(`Redirecting: ${path} -> ${target}`)
})
staticApp.get("/raamit", (_req, res) => res.send(""))
staticApp.listen(STATIC_RESPONSE_APP_PORT)

// Proxy backend-palvelimeen

const setupBackendProxy = (app) => {
  const backendHost = process.env.BACKEND_HOST || "http://localhost:7021"
  app.use(
    createProxyMiddleware("/koski", {
      target: `${backendHost}/`,
      changeOrigin: true,
    })
  )
}

const setupRedirects = (app) => {
  Object.keys(REDIRECTS).map((path) => {
    app.use(
      createProxyMiddleware((pathname) => pathname === path, {
        target: `http://localhost:${STATIC_RESPONSE_APP_PORT}${path}`,
        changeOrigin: true,
      })
    )
  })
}
