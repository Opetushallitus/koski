const Bundler = require("parcel-bundler")
const express = require("express")
const { createProxyMiddleware } = require("http-proxy-middleware")
const path = require("path")
const { over } = require("ramda")

async function startServer({
  port,
  virkailijaRaamitProxy,
  backend,
  parcelOptions,
}) {
  const app = express()

  // https://stackoverflow.com/questions/53902896/is-there-a-way-to-proxy-requests-in-parcel-as-in-webpack
  if (virkailijaRaamitProxy) {
    app.use(
      createProxyMiddleware("/virkailija-raamit", {
        target: virkailijaRaamitProxy,
        changeOrigin: true,
      })
    )
  } else {
    app.get(/\/virkailija-raamit\/.*/, (_req, res) => res.send(""))
  }

  app.use(
    createProxyMiddleware("/api", {
      target: `${backend}/valpas/`,
      changeOrigin: true,
    })
  )

  app.use(
    createProxyMiddleware("/login", {
      target: `${backend}/user/`,
      changeOrigin: true,
    })
  )

  const valpasEntryPoint = path.join(__dirname, "../src/index.html")
  const bundler = new Bundler(valpasEntryPoint, parcelOptions)
  app.use(bundler.middleware())

  const bundled = new Promise((resolve, reject) => {
    bundler.on("bundled", resolve)
    bundler.on("bundleError", reject)
  })

  const serverStarted = new Promise((resolve) => {
    server = app.listen(Number(port), () => {
      console.log(`\nServer running at http://localhost:${port}`)
      resolve()
    })
  })

  await Promise.all([bundled, serverStarted])

  return {
    close() {
      server.close()
    },
  }
}

function start(overrides) {
  return startServer({
    port: process.env.PORT || 1234,
    virkailijaRaamitProxy: process.env.VIRKAILIJA_RAAMIT_HOST || undefined,
    backend: process.env.BACKEND_HOST || "http://localhost:7021/koski",
    parcelOptions: {
      cache: false,
      watch: false,
      ...(overrides && overrides.parcelOptions),
    },
    ...overrides,
  })
}

module.exports = {
  start,
}
