const kill = require("tree-kill")

module.exports = () => {
  if (global.__PARCEL_SERVE_PROCESS__) {
    kill(global.__PARCEL_SERVE_PROCESS__.pid)
  }
}
