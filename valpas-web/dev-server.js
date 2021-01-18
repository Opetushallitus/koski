const Bundler = require("parcel-bundler")
const express = require("express")
const { createProxyMiddleware } = require("http-proxy-middleware")

const PORT = process.env.PORT || 1234
const VIRKAILIJA_RAAMIT_PROXY = process.env.VIRKAILIJA_RAAMIT_PROXY || undefined
const BACKEND = process.env.BACKEND_PROXY || "http://localhost:7021/koski"

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

app.use(
  createProxyMiddleware("/api", {
    target: `${BACKEND}/valpas/`,
    changeOrigin: true,
    logLevel: "debug",
  })
)

app.use(
  createProxyMiddleware("/login", {
    target: `${BACKEND}/user/`,
    changeOrigin: true,
    logLevel: "debug",
  })
)

const bundler = new Bundler("src/index.html", { cache: false })
app.use(bundler.middleware())

app.listen(Number(PORT), () => {
  console.log(`\nServer running at http://localhost:${PORT}`)
})
