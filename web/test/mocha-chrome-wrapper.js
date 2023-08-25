process.env.CHROME_PATH = require('puppeteer').executablePath()
process.on('unhandledRejection', (reason, promise) => {
  console.error(reason)
  process.exit(1)
})

const { exec } = require('child_process')

setInterval(function () {
  exec('../scripts/cpuhogs.sh', function (err, stdout, stderr) {
    console.log(stderr)
    console.log(stdout)
  })
  exec('../scripts/memhogs.sh', function (err, stdout, stderr) {
    console.log(stderr)
    console.log(stdout)
  })
  exec('../scripts/filehogs.sh', function (err, stdout, stderr) {
    console.log(stderr)
    console.log(stdout)
  })
}, 60000)

require('mocha-chrome/cli.js')
