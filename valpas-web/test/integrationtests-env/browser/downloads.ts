import * as fs from "fs/promises"
import * as path from "path"
import { eventually } from "./utils"

export const downloadDir = path.resolve(
  path.join(__dirname, "..", "..", "download")
)

export const filePath = (filename: string) => path.join(downloadDir, filename)

const exists = async (p: string): Promise<boolean> => {
  try {
    await fs.stat(p)
    return true
  } catch (e) {
    return false
  }
}

export const cleanupDownloads = async () => {
  if (await exists(downloadDir)) {
    const files = await fs.readdir(downloadDir)
    for (const file of files) {
      await fs.unlink(filePath(file))
    }
  } else {
    await fs.mkdir(downloadDir)
  }
}

export const expectDownloadExists = async (filename: string): Promise<void> =>
  eventually(async () => expect(await exists(filePath(filename))).toBeTruthy())
