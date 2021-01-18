const Bundler = require("parcel-bundler")
const express = require("express")
const { createProxyMiddleware } = require("http-proxy-middleware")

const PORT = process.env.PORT || 1234
const VIRKAILIJA_RAAMIT_PROXY = process.env.VIRKAILIJA_RAAMIT_PROXY || undefined

const app = express()

// https://stackoverflow.com/questions/53902896/is-there-a-way-to-proxy-requests-in-parcel-as-in-webpack
if (VIRKAILIJA_RAAMIT_PROXY) {
  app.use(
    createProxyMiddleware("/virkailija-raamit", {
      target: VIRKAILIJA_RAAMIT_PROXY,
      changeOrigin: true,
    })
  )
} else {
  app.get(/\/virkailija-raamit\/.*/, (_req, res) => res.send(""))
}

const bundler = new Bundler("src/index.html", { cache: false })
app.use(bundler.middleware())

app.listen(Number(PORT), () => {
  console.log(`\nServer running at http://localhost:${PORT}`)
})
