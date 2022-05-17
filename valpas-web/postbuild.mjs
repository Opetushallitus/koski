import fs from "fs/promises"
import { existsSync } from "fs"
import path from "path"
import { load } from "cheerio"
import { fileURLToPath } from "url"
import { dirname } from "path"

const __filename = fileURLToPath(import.meta.url)
const __dirname = dirname(__filename)
const targetDir = path.join(
  __dirname,
  "..",
  "target",
  "webapp",
  "koski",
  "valpas",
  "v2",
  "assets"
)

const webDistDir = path.join(__dirname, "dist-nonce")

const targetWebDistDir = targetDir

const sourceIndexHtml = path.join(webDistDir, "index.html")
const targetIndexHtml = path.resolve(targetDir, "..", "index.html.template")

async function copy(source, dist) {
  const elements = await fs.readdir(source)
  elements.forEach(async (element) => {
    if ((await fs.lstat(path.join(source, element))).isFile()) {
      await fs.copyFile(path.join(source, element), path.join(dist, element))
    } else {
      await copy(path.join(source, element), path.join(dist, element))
    }
  })
}

async function copyDist() {
  if (!existsSync(targetWebDistDir)) {
    console.log(`Target directory ${targetWebDistDir} does not exist, creating`)
    await fs.mkdir(targetWebDistDir, {
      recursive: true,
    })
  }
  console.log(`Copying ${webDistDir} to ${targetWebDistDir}`)
  await copy(webDistDir, targetWebDistDir)
}

async function setNoncePlaceholders() {
  console.log(`Setting CSP nonce placeholders for ${sourceIndexHtml}`)
  const html = (await fs.readFile(sourceIndexHtml)).toString("utf-8")
  const $ = load(html)
  $("link").attr("nonce", "<%==VALPAS_CSP_NONCE==%>")
  $("script").attr("nonce", "<%==VALPAS_CSP_NONCE==%>")
  $("head").append(`<script nonce="<%==VALPAS_CSP_NONCE==%>">window["nonce"] = "<%==VALPAS_CSP_NONCE==%>"</script>`);
  console.log(`Writing ${targetIndexHtml} with CSP nonce placeholders`)
  await fs.writeFile(targetIndexHtml, $.html())
  console.log(`Wrote output to ${targetIndexHtml}`)
}

export async function postbuild() {
  await copyDist()
  await setNoncePlaceholders()
}
