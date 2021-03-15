const { start } = require("./server")

start({
  parcelOptions: {
    cache: true,
    watch: true,
  },
  redirects: {
    "/": "/valpas",
    "/valpas": "/valpas/virkailija",
  },
}).catch((err) => console.error("Oh noes!", err))
