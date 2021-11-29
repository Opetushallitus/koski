export type TranslationId = string
export type Translation = string

export const supportedLanguages = ["fi", "sv", "en"] as const

export type Language = typeof supportedLanguages[number]

type LanguageRecord = Record<Language, Translation>

export type LocalizationMap = Record<TranslationId, LanguageRecord>

export type AppConfiguration = {
  valpasLocalizationMap: LocalizationMap
  environment: string
  opintopolkuVirkailijaUrl: string
}
