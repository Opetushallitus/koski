import { setTimeout } from 'node:timers/promises'
import { format } from 'node:util'
import puppeteer, { type JSHandle } from 'puppeteer'

const MOCHA_START_TIMEOUT_MILLIS = 60_000
// CI runs one spec file at a time, locally the whole suite may run at once
const RESULT_POLLING_TIMEOUT_MILLIS = process.env.CI ? 15 * 60 * 1000 : Infinity
const RESULT_POLLING_INTERVAL_MILLIS = 1000

type TestError = { fullTitle: string; message?: string }

declare global {
  interface Window {
    runner?: Mocha.Runner & { errors: TestError[] }
  }
}

const toText = (arg: JSHandle) => {
  const remoteObject = arg.remoteObject()
  return 'value' in remoteObject ? remoteObject.value : remoteObject.description
}

const escapeForGithub = (text: string) =>
  text.replaceAll('%', '%25').replaceAll('\r', '%0D').replaceAll('\n', '%0A')

async function main(url: string): Promise<number> {
  const browser = await puppeteer.launch({
    args: ['--no-sandbox'],
    defaultViewport: { width: 1280, height: 720 }
  })

  try {
    const page = await browser.newPage()

    page.on('console', (msg) => {
      const args = msg.args()
      // Browser's own log entries, e.g. failed requests, have no args
      if (args.length === 0) {
        console.log(msg.text())
        return
      }
      console.log(format(...args.map(toText)))
    })
    page.on('pageerror', (error) => console.error('[pageerror]', error))
    page.on('error', (error) => {
      console.error('[page crashed]', error)
      process.exit(1)
    })

    const testUrl = new URL(url)
    testUrl.searchParams.set('reporter', 'spec')
    await page.goto(testUrl.href, { waitUntil: 'domcontentloaded' })

    await page.waitForFunction(() => window.runner, {
      timeout: MOCHA_START_TIMEOUT_MILLIS
    })
    // Polled from here instead of a long page.waitForFunction, which
    // Puppeteer's protocolTimeout (3 min) would cut short. Raising
    // protocolTimeout would raise it for every browser call, which would then
    // all need their own shorter timeouts.
    const deadline = Date.now() + RESULT_POLLING_TIMEOUT_MILLIS
    while (!(await page.evaluate(() => Boolean(window.runner?.stats?.end)))) {
      if (Date.now() > deadline) {
        throw new Error('Mocha did not finish in time')
      }
      await setTimeout(RESULT_POLLING_INTERVAL_MILLIS)
    }

    const { tests, failures, errors } = await page.evaluate(() => ({
      tests: window.runner?.stats?.tests ?? 0,
      failures: window.runner?.stats?.failures ?? 0,
      errors: window.runner?.errors ?? []
    }))

    if (process.env.GITHUB_ACTIONS) {
      for (const { fullTitle, message } of errors) {
        console.log(
          `::error title=Mocha::${escapeForGithub(`${fullTitle}: ${message}`)}`
        )
      }
    }

    if (tests === 0) {
      console.error('No tests were run')
      return 1
    }

    if (failures > 0) {
      return 1
    }

    return 0
  } finally {
    await browser.close()
  }
}

const testRunnerUrl = process.argv[2]
process.exitCode = await main(testRunnerUrl)
