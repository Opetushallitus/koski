require("dotenv").config()
const Bundler = require("parcel-bundler")
const express = require("express")
const { createProxyMiddleware } = require("http-proxy-middleware")
const path = require("path")

async function startServer({
  port,
  virkailijaRaamitProxy,
  backend,
  publicUrl,
  parcelOptions,
  redirects,
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
    createProxyMiddleware("/koski", {
      target: backend,
      changeOrigin: true,
    })
  )

  Object.entries(redirects).forEach(([path, target]) => {
    app.get(path, (_req, res) => res.redirect(target))
    console.log(`Redirecting: ${path} -> ${target}`)
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
      resolve(path.join(`localhost:${port}`, publicUrl))
    })
  })

  await bundled
  const host = await serverStarted

  console.log(`\nVirkailija app running at http://${host}/virkailija`)

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
    backend: process.env.BACKEND_HOST || "http://localhost:7021",
    publicUrl: process.env.PUBLIC_URL || "",
    parcelOptions: {
      cache: !!process.env.PARCEL_CACHE,
      watch: false,
      ...(overrides && overrides.parcelOptions),
    },
    redirects: {},
    ...overrides,
  })
}

module.exports = {
  start,
}
