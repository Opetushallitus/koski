import type { TestFixture, TestInfo } from '@playwright/test'
import { chromium, expect } from '@playwright/test'
import fsp from 'fs/promises'
import fs from 'fs'
import { resolve } from 'path'
import { Kansalainen, Virkailija } from './users.types'
import { KansalainenLoginPage } from '../pages/login/KansalainenLoginPage'

const PW_STATE_PATH = 'test/.pw-state'

type Cookie = {
  name: string
  value: string
  domain: string
  path: string
  /**
   * Unix time in seconds.
   */
  expires: number
  httpOnly: boolean
  secure: boolean
  sameSite: 'Strict' | 'Lax' | 'None'
}

async function checkFileExists(file: string) {
  try {
    await fsp.access(file, fs.constants.F_OK)
    return true
  } catch (err) {
    return false
  }
}

export async function getVirkailijaSession(
  testInfo: TestInfo,
  username: Virkailija,
  password: string
) {
  // Istunnon regenerointilogiikka
  const sessionPath = virkailijaPath(username)
  const [project] = testInfo.config.projects
  const browser = await chromium.launch({
    headless: true
  })
  const ctx = await browser.newContext({
    baseURL: project.use.baseURL
  })
  const virkailijaLoginPage = await ctx.newPage()
  const request = await virkailijaLoginPage.request.post('/koski/user/login', {
    data: {
      username,
      password
    },
    timeout: 2000
  })
  expect(request.ok()).toBeTruthy()
  await virkailijaLoginPage.context().storageState({
    path: sessionPath
  })
  await browser.close()
  return sessionPath
}
async function getKansalainenSession(testInfo: TestInfo, hetu: string) {
  // Istunnon regenerointilogiikka
  const sessionPath = kansalainenPath(hetu)
  const [project] = testInfo.config.projects
  const browser = await chromium.launch({
    headless: true
  })
  const ctx = await browser.newContext({
    baseURL: project.use.baseURL
  })
  const page = await ctx.newPage()
  const kansalainenLogin = new KansalainenLoginPage(page)
  await kansalainenLogin.loginWithHetu(hetu)
  await page.context().storageState({
    path: sessionPath
  })
  await browser.close()
  return sessionPath
}

export const virkailijaPath = (username: Virkailija) =>
  resolve(PW_STATE_PATH, `${username.toLowerCase()}-virkailija.json`)

export const kansalainenPath = (hetu: string) =>
  resolve(PW_STATE_PATH, `${hetu}-kansalainen.json`)

export const virkailija =
  (virk: Virkailija): TestFixture<string, any> =>
  // eslint-disable-next-line no-empty-pattern
  async ({}, use, testInfo) => {
    const v = virkailijaPath(virk)
    const exists = await checkFileExists(v)

    // Olemassaolevalta istuntotiedostolta tarkistetaan keksin voimassaolo.
    // Alle 20 minuuttia voimassa olevat istunnot uudelleengeneroidaan suoraan
    if (exists) {
      const data = await fsp.readFile(v, {
        encoding: 'utf-8',
        flag: 'r'
      })
      const parsed = JSON.parse(data) as {
        cookies: Cookie[]
      }
      const cookie = parsed.cookies.find((c: any) => c.name === 'koskiUser')
      if (cookie === undefined) {
        throw new Error('No koskiUser cookie found')
      }
      const minuteMs = 20 * 60 * 1000
      if (cookie.expires * 1000 - new Date().getTime() >= minuteMs) {
        await use(v)
      } else {
        const virkailijaSessionPath = await getVirkailijaSession(
          testInfo,
          virk,
          virk
        )
        await use(virkailijaSessionPath)
      }
    } else {
      const virkailijaSessionPath = await getVirkailijaSession(
        testInfo,
        virk,
        virk
      )
      await use(virkailijaSessionPath)
    }
  }

export const kansalainen =
  (hetu: Kansalainen): TestFixture<string, any> =>
  // eslint-disable-next-line no-empty-pattern
  async ({}, use, testInfo) => {
    const v = kansalainenPath(hetu)
    const exists = await checkFileExists(v)
    if (exists) {
      const data = await fsp.readFile(v, {
        encoding: 'utf-8',
        flag: 'r'
      })
      const parsed = JSON.parse(data) as {
        cookies: Cookie[]
      }
      const cookie = parsed.cookies.find((c: any) => c.name === 'koskiOppija')
      if (cookie === undefined) {
        throw new Error('No koskiOppija cookie found')
      }
      const minuteMs = 20 * 60 * 1000
      if (cookie.expires * 1000 - new Date().getTime() >= minuteMs) {
        await use(v)
      } else {
        const kansalainenSessionPath = await getKansalainenSession(
          testInfo,
          hetu
        )
        await use(kansalainenSessionPath)
      }
    } else {
      const kansalainenSessionPath = await getKansalainenSession(testInfo, hetu)
      await use(kansalainenSessionPath)
    }
  }
