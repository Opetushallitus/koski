const kill = require("tree-kill")

module.exports = () => {
  const pid = global.__PARCEL_SERVE_PROCESS__?.pid
  if (pid !== undefined) {
    kill(pid)
  }
}
