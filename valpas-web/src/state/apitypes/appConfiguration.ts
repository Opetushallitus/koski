import { Oid } from "../common"

export type TranslationId = string
export type Translation = string

export const supportedLanguages = ["fi", "sv", "en"] as const

export type Language = (typeof supportedLanguages)[number]

type LanguageRecord = Record<Language, Translation>

export type LocalizationMap = Record<TranslationId, LanguageRecord>

export type OppijaRaamitUser = {
  name: string
  oid: Oid
}

export type AppConfiguration = {
  valpasLocalizationMap: LocalizationMap
  environment: string
  opintopolkuVirkailijaUrl: string
  opintopolkuOppijaUrl: string
  oppijaRaamitUser?: OppijaRaamitUser
}
