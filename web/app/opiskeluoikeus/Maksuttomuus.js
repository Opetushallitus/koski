import { formatISODate, parseISODate } from '../date/date'
import Bacon from 'baconjs'
import Http from '../util/http'
import { t } from '../i18n/i18n'

export const checkAlkamispäivä = (d) => {
  const validDate = formatISODate(d)
  return validDate && parseISODate(validDate) >= new Date(2021, 0, 1) // 0=Tammikuu, month indexing starts from 0
}

export const checkSuoritus = (s) =>
  s
    ? Http.post(
        '/koski/api/editor/check-vaatiiko-suoritus-maksuttomuus-tiedon',
        s
      )
    : Bacon.constant(false)

export const maksuttomuusOptions = [
  {
    key: 'none',
    value: t(
      'Henkilö on syntynyt ennen vuotta 2004 ja ei ole laajennetun oppivelvollisuuden piirissä'
    )
  },
  {
    key: true,
    value: t('Koulutus on maksutonta')
  },
  {
    key: false,
    value: t('Maksullinen koulutus')
  }
]
