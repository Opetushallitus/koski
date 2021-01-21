require("dotenv").config()
const server = require("../../devserver/server")

module.exports = async () => {
  global.__FRONTEND_SERVER__ = await server.start()
}
