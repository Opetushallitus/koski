import Cookie from "js-cookie"
import React, { useMemo } from "react"
import { LocalizedString } from "../state/types"
import { logWarning } from "../utils/log"

export type TranslationId = string
export type Translation = string

export const supportedLanguages = ["fi", "sv", "en"] as const

export type Language = typeof supportedLanguages[number]

type LanguageRecord = Record<Language, Translation>

type LocalizationMap = Record<TranslationId, LanguageRecord>

type ParamsMap = Record<string, string | number>
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

export const t = (id: TranslationId, params?: ParamsMap): Translation => {
  if (!id) {
    return ""
  }

  const tRecursive = (
    usedLanguage: Language,
    id: TranslationId,
    params?: ParamsMap
  ): Translation => {
    const localizedString = texts[id]

    if (!localizedString || !localizedString[usedLanguage]) {
      if (!missing[usedLanguage + "." + id]) {
        logWarning(`Käännös puuttuu ${usedLanguage}:`, id)
        missing[usedLanguage + "." + id] = true
      }

      return usedLanguage === "fi" ? id : tRecursive("fi", id, params)
    }

    const source = localizedString[usedLanguage]
    return params ? replaceParams(source, params) : source
  }

  return tRecursive(getLanguage(), id, params)
}

const replaceParams = (source: string, params: ParamsMap): string =>
  Object.entries(params).reduce(
    (str, [key, value]) => str.replace(`{{${key}}}`, value.toString()),
    source
  )

export type LocalizedTextProps = {
  id: TranslationId
  params?: ParamsMap
}

export const T = (props: LocalizedTextProps) => <>{t(props.id, props.params)}</>

export const getLocalized = (
  localizedString?: LocalizedString
): string | undefined =>
  localizedString === undefined
    ? undefined
    : localizedString[getLanguage()] ||
      localizedString["fi"] ||
      localizedString["sv"] ||
      localizedString["en"] ||
      "KÄÄNNÖS PUUTTUU"

export const formatFixedNumber = (
  n: number | undefined,
  fractionDigits: number
): string | undefined => n?.toFixed(fractionDigits).replace(".", ",")

export const useLanguage = () => useMemo(() => getLanguage(), [])
