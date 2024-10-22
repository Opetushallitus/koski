const os = require("os")

const addresses = Object.values(os.networkInterfaces()).reduce((acc, v) =>
  acc.concat(v),
)
console.log(addresses.find((a) => a.family === "IPv4" && !a.internal).address)
