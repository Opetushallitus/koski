// Customized simple HTTP server for Valpas
// Based on https://github.com/lukejacksonn/servor
//
// * Removed development features (live reload, directory listings)
// * Added `close()` function

const fs = require("fs")
const url = require("url")
const path = require("path")
const http = require("http")
const http2 = require("http2")
const https = require("https")
const zlib = require("zlib")

const mimeTypes = require("./utils/mimeTypes.js")

const { usePort, networkIps } = require("./utils/common.js")

module.exports = async ({
  root = ".",
  fallback = "index.html",
  credentials,
  port,
} = {}) => {
  // Try start on specified port then fail or find a free port

  try {
    port = await usePort(port || process.env.PORT || 8080)
  } catch (e) {
    if (port || process.env.PORT) {
      console.log("[ERR] The port you have specified is already in use!")
      process.exit()
    }
    port = await usePort()
  }

  // Configure globals

  root = root.startsWith("/") ? root : path.join(process.cwd(), root)

  const reloadClients = []
  const protocol = credentials ? "https" : "http"
  const server = credentials
    ? reload
      ? (cb) => https.createServer(credentials, cb)
      : (cb) => http2.createSecureServer(credentials, cb)
    : (cb) => http.createServer(cb)

  // Server utility functions

  const isRouteRequest = (pathname) => !~pathname.split("/").pop().indexOf(".")
  const utf8 = (file) => Buffer.from(file, "binary").toString("utf8")

  const baseDoc = (pathname = "", base = path.join("/", pathname, "/")) =>
    `<!doctype html><meta charset="utf-8"/><base href="${base}"/>`

  const sendError = (res, status) => {
    res.writeHead(status)
    res.write(`${status}`)
    res.end()
  }

  const sendFile = (res, status, file, ext, encoding = "binary") => {
    if (["js", "css", "html", "json", "xml", "svg"].includes(ext)) {
      res.setHeader("content-encoding", "gzip")
      file = zlib.gzipSync(utf8(file))
      encoding = "utf8"
    }
    res.writeHead(status, { "content-type": mimeTypes(ext) })
    res.write(file, encoding)
    res.end()
  }

  const sendMessage = (res, channel, data) => {
    res.write(`event: ${channel}\nid: 0\ndata: ${data}\n`)
    res.write("\n\n")
  }

  // Respond to reload requests with keep alive

  const serveReload = (res) => {
    res.writeHead(200, {
      connection: "keep-alive",
      "content-type": "text/event-stream",
      "cache-control": "no-cache",
    })
    sendMessage(res, "connected", "ready")
    setInterval(sendMessage, 60000, res, "ping", "waiting")
    reloadClients.push(res)
  }

  // Respond to requests with a file extension

  const serveStaticFile = (res, pathname) => {
    const uri = path.join(root, pathname)
    let ext = uri.replace(/^.*[\.\/\\]/, "").toLowerCase()
    if (!fs.existsSync(uri)) return sendError(res, 404)
    fs.readFile(uri, "binary", (err, file) =>
      err ? sendError(res, 500) : sendFile(res, 200, file, ext)
    )
  }

  // Respond to requests without a file extension

  const serveRoute = (res, pathname) => {
    const index = path.join(root, fallback)
    fs.readFile(index, "binary", (err, file) => {
      if (err) return sendError(res, 500)
      const status = pathname === "/" ? 200 : 301
      sendFile(res, status, file, "html")
    })
  }

  // Start the server and route requests

  const instance = server((req, res) => {
    const pathname = decodeURI(url.parse(req.url).pathname)
    res.setHeader("access-control-allow-origin", "*")
    if (!isRouteRequest(pathname)) return serveStaticFile(res, pathname)
    return serveRoute(res, pathname)
  }).listen(parseInt(port, 10))

  // Close socket connections on sigint

  process.on("SIGINT", () => {
    while (reloadClients.length > 0) reloadClients.pop().end()
    process.exit()
  })

  const x = { url: `${protocol}://localhost:${port}` }

  return {
    ...x,
    root,
    protocol,
    port,
    ips: networkIps,
    close: () => instance.close(),
  }
}
