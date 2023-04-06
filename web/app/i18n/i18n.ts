import Cookie from 'js-cookie'
import { English } from '../types/fi/oph/koski/schema/English'
import { Finnish } from '../types/fi/oph/koski/schema/Finnish'
import { LocalizedString } from '../types/fi/oph/koski/schema/LocalizedString'
import { Swedish } from '../types/fi/oph/koski/schema/Swedish'

export const supportedLanguages = ['fi', 'sv', 'en'] as const

export type TranslationId = string
export type Translation = string
export type Language = (typeof supportedLanguages)[number]
export type LanguageRecord = Record<Language, Translation>
export type LocalizationMap = Record<TranslationId, LanguageRecord>

declare global {
  interface Window {
    koskiLocalizationMap: LanguageRecord
  }
}

const texts = window.koskiLocalizationMap
const missing: Record<string, boolean> = {}

export const lang = (Cookie.get('lang') || 'fi') as Language

export const setLang = (newLang: Language) => {
  Cookie.set('lang', newLang)
  window.location.reload()
}

export function t(
  s: string | LocalizedString | undefined,
  ignoreMissing?: false | undefined,
  languageOverride?: Language
): string

export function t(
  s: string | LocalizedString | undefined,
  ignoreMissing?: boolean,
  languageOverride?: Language
): string | null {
  const usedLanguage = languageOverride || lang
  if (!s) return ''
  if (typeof s === 'object') {
    // @ts-ignore - assume it's a localized string
    return s[usedLanguage] || s.fi || s.sv || s.en
  }
  if (typeof s === 'string') {
    // @ts-ignore try to find a localization from the bundle
    const localizedString = texts[s] || {}
    if (!localizedString[usedLanguage]) {
      if (ignoreMissing === true) return null
      if (!missing[usedLanguage + '.' + s]) {
        if (usedLanguage === 'fi')
          console.error(`Localization missing for language ${usedLanguage}:`, s)
        missing[usedLanguage + '.' + s] = true
      }
      return s
    }
    return localizedString[usedLanguage]
  }
  console.error('Trying to localize', s)
  return null
}

// @ts-ignore
export const tExists = (s: string): boolean => texts[s] !== undefined

export const tTemplate = (s: string, args: object): string =>
  Object.entries(args).reduce(
    (str, [key, value]) =>
      str.replace(
        `{{${key}}}`,
        typeof value === 'object' ? JSON.stringify(value) : value
      ),
    t(s)
  )

export const localize = (str: string): LocalizedString =>
  lang === 'fi'
    ? Finnish({ fi: str })
    : lang === 'sv'
    ? Swedish({ sv: str })
    : English({ en: str })
