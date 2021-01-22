import React from "react"
import Cookie from "js-cookie"
import { logWarning } from "../utils/log"

export type TranslationId = string
export type Translation = string

const supportedLanguages = ["fi", "sv", "en"] as const

type Language = typeof supportedLanguages[number]

type LanguageRecord = Record<Language, Translation>

type LocalizationMap = Record<TranslationId, LanguageRecord>

declare global {
  interface Window {
    valpasLocalizationMap: LocalizationMap
  }
}

const texts = window.valpasLocalizationMap
const missing: Record<string, boolean> = {}

export const getLanguage = (): Language => {
  const maybeLanguage: unknown = Cookie.get("lang")
  const language = supportedLanguages.find(
    (validLanguage) => validLanguage === maybeLanguage
  )

  return language || "fi"
}

export const setLanguage = (newLang: Language) => {
  Cookie.set("lang", newLang)
  window.location.reload()
}

export const t = (id: TranslationId): Translation => {
  if (!id) {
    return ""
  }

  const localizedString = texts[id]
  const usedLanguage = getLanguage()

  if (!localizedString || !localizedString[usedLanguage]) {
    if (!missing[usedLanguage + "." + id]) {
      logWarning(`Käännös puuttuu ${usedLanguage}:`, id)
      missing[usedLanguage + "." + id] = true
    }

    return id
  }

  return localizedString[usedLanguage]
}

export type LocalizedTextProps = {
  id: TranslationId
}

export const T = (props: LocalizedTextProps) => <>{t(props.id)}</>
