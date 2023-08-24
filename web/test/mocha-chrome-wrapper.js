process.env.CHROME_PATH = require('puppeteer').executablePath()
process.on('unhandledRejection', (reason, promise) => {
  console.error(reason)
  process.exit(1)
})

const { exec } = require('child_process')

console.log('Scheduling checkup scripts')

setInterval(function () {
  exec('../scripts/cpuhogs.sh', function (err, stdout, stderr) {
    console.log('cpuhogs stdout', stdout)
    console.log('cpuhogs stderr', stderr)
  })
  exec('../scripts/memhogs.sh', function (err, stdout, stderr) {
    console.log('memhogs stdout', stdout)
    console.log('memhogs stderr', stderr)
  })
  exec('../scripts/filehogs.sh', function (err, stdout, stderr) {
    console.log('filehogs stdout', stdout)
    console.log('filehogs stderr', stderr)
  })
}, 30000)
console.log('Scheduling checkup scripts done')

require('mocha-chrome/cli.js')
