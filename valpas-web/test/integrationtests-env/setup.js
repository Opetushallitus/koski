const server = require("./test-http-server")
const { join } = require("path")

module.exports = async () => {
  global.__FRONTEND_SERVER__ = await server({
    root: join(__dirname, "..", "..", "dist"),
    fallback: "index.html",
    credentials: null,
    port: 7357,
  })
}
