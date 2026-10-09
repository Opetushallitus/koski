import { spawn } from 'node:child_process'
import readline from 'node:readline'

export const followProcessOutput = (
  command: string,
  args: string[],
  onLine: (line: string) => void
) => {
  const child = spawn(command, args)
  readline.createInterface({ input: child.stdout }).on('line', onLine)
  child.stderr.on('data', (error: Buffer) => console.error(error.toString()))
  child.on('error', (error) => console.error(error))
}
