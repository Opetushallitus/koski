process.env.CHROME_PATH = require('puppeteer').executablePath()
process.on('unhandledRejection', (reason, promise) => {
  console.error(reason)
  process.exit(1)
})

require('mocha-chrome/cli.js')
