let fs = require('fs')
let filenames = process.argv.slice(2)
if (filenames.length == 0) throw new Error('filename missing')
filenames.forEach(filename => {
  let data = JSON.parse(fs.readFileSync(filename, 'utf-8'))
  data.exams.forEach(e => e.sections = [])
  let json = JSON.stringify(data, null, 2)
  fs.writeFileSync(filename, json, {})
})
