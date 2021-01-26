require("dotenv").config()
const Bundler = require("parcel-bundler")
const express = require("express")
const { createProxyMiddleware } = require("http-proxy-middleware")
const path = require("path")

const proxyPaths = ["/api", "/login", "/localization", "/logout"]

async function startServer({
  port,
  virkailijaRaamitProxy,
  backend,
  publicUrl,
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

  proxyPaths.forEach((proxyPath) => {
    app.use(
      createProxyMiddleware(`${publicUrl}${proxyPath}`, {
        target: backend,
        changeOrigin: true,
      })
    )
  })

  const valpasEntryPoint = path.join(__dirname, "../src/index.html")
  const bundler = new Bundler(valpasEntryPoint, {
    ...parcelOptions,
    publicUrl: publicUrl || "./",
  })
  app.use(bundler.middleware())

  const bundled = new Promise((resolve, reject) => {
    bundler.on("bundled", resolve)
    bundler.on("bundleError", reject)
  })

  const serverStarted = new Promise((resolve) => {
    server = app.listen(Number(port), () => {
      const host = path.join(`localhost:${port}`, publicUrl)
      console.log(`\nServer running at http://${host}`)
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
    publicUrl: process.env.PUBLIC_URL || "",
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
