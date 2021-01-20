const { start } = require("./server")

start({
  parcelOptions: {
    cache: true,
    watch: true,
  },
})
  .then(() => console.log("\n\nDevserver is up and running\n\n"))
  .catch((err) => console.error("Oh noes!", err))
