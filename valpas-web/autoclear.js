const { exec } = require("child_process")
const { join } = require("path")
const { readFile, writeFile } = require("fs/promises")

const run = (command) =>
  new Promise((resolve, reject) => {
    exec(command, (error, stdout, stderr) => {
      if (error) {
        reject({ error, stderr: stderr.toString() })
      } else {
        resolve(stdout.toString().trim())
      }
    })
  })

const readFileNeverFail = (filepath) => readFile(filepath).catch(() => "none")

const getCurrentBranch = () => run("git rev-parse --abbrev-ref HEAD")
const getGitRootDir = () => run("git rev-parse --show-toplevel")
const clearCache = () => run("pnpm run clean")

const getVersionPath = async () =>
  join(await getGitRootDir(), "valpas-web", ".last-build")

const getLastBuildVersion = async () =>
  (await readFileNeverFail(await getVersionPath())).toString()

const setLastBuildVersion = async (version) =>
  writeFile(await getVersionPath(), version)

const autoclearCache = async (variant) => {
  const previous = await getLastBuildVersion()
  const next = `${await getCurrentBranch()}:${variant}`
  if (previous != next) {
    console.log(
      `Branch or build target change detected (${previous} -> ${next}): clearing caches...`
    )
    await clearCache()
    setLastBuildVersion(next)
  }
}

const getVariant = () => {
  const flag = process.argv.filter((a) => a.match(/^--.+$/))[0]
  return flag ? flag.slice(2) : "dev"
}

autoclearCache(getVariant()).catch(console.error)
