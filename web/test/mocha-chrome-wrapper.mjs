import puppeteer from 'puppeteer'

process.env.CHROME_PATH = puppeteer.executablePath()
process.on('unhandledRejection', (reason, _promise) => {
  console.error(reason)
  process.exit(1)
})

import 'mocha-chrome/cli'
