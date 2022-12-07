import { modelData } from '../editor/EditorModel'
import { zeroValue } from '../editor/EnumEditor'
import { t } from '../i18n/i18n'
import { Koodistokoodiviite } from '../types/common'
import { EditorModel } from '../types/EditorModels'

declare global {
  interface Window {
    ePerusteetBaseUrl?: string
  }
}

interface EnumOption<T> {
  title: string
  value: string
  data: T
}

export const opintokokonaisuuteenKuuluvatPäätasonSuoritukset = [
  'vapaansivistystyonpaatasonsuoritus',
  'muunkuinsaannellynkoulutuksenpaatasonsuoritus'
]

const isZeroValue = <T>(option: EnumOption<T>): boolean =>
  option.value === zeroValue.value

const hasKoodiarvo = (data?: Koodistokoodiviite): data is Koodistokoodiviite =>
  data !== undefined && 'koodiarvo' in data

export const asHyperLink = (model: EditorModel) => {
  const data = modelData(model)

  if (!hasKoodiarvo(data)) {
    return {
      url: '#',
      target: '_self'
    }
  }
  return {
    url: `${window.ePerusteetBaseUrl}${t(
      'eperusteet_opintopolku_url_fragment'
    )}${data.koodiarvo}`,
    target: '_blank'
  }
}

export const formatOpintokokonaisuusDisplayValue = (
  option?: EnumOption<Koodistokoodiviite>
) => {
  if (option === undefined) {
    return zeroValue.title
  }
  if (isZeroValue(option)) {
    return option.title
  }
  if (!hasKoodiarvo(option.data)) {
    return option.title
  }
  return `${option.data.koodiarvo} ${option.title}`
}

export const formatOpintokokonaisuusTitle = (
  option?: EnumOption<Koodistokoodiviite>
): string => {
  if (option === undefined) {
    return zeroValue.title
  }
  if (isZeroValue(option)) {
    return option.title
  }
  if (!hasKoodiarvo(option.data)) {
    return option.title
  }
  return `${option.data.koodiarvo} ${option.title}`
}
