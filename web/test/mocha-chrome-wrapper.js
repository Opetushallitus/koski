/* eslint-disable @typescript-eslint/no-require-imports */
process.env.CHROME_PATH = require('puppeteer').executablePath()
process.on('unhandledRejection', (reason) => {
  console.error(reason)
  process.exit(1)
})

require('mocha-chrome/cli.js')
