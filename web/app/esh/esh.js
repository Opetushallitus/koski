import { modelData, modelItems } from '../editor/EditorModel'
import { arvioituTaiVahvistettu } from '../suoritus/Suoritus'
import Http from '../util/http'

export const eshSallitutRahoituskoodiarvot = ['6']

export const jääLuokalle = (suoritus) =>
  modelData(suoritus, 'jääLuokalle') === true

export const eiOsasuorituksiaEshLuokkaAsteet = ['N1', 'N2']

export const luokkaAsteenOsasuoritukset = (luokkaAste) =>
  Http.cachedGet(
    `/koski/api/editor/koodit/europeanschoolofhelsinkiluokkaaste/${luokkaAste}/suoritukset/prefill`
  )

export const luokkaAsteenOsasuorituksenAlaosasuoritukset = (
  luokkaAste,
  oppiainekoodi
) =>
  Http.cachedGet(
    `/koski/api/editor/koodit/europeanschoolofhelsinkiluokkaaste/${luokkaAste}/alaosasuoritukset/${oppiainekoodi}/prefill`
  )

export const valmiitaSuorituksia = (oppiaineSuoritukset) =>
  oppiaineSuoritukset.some(
    (oppiaine) =>
      arvioituTaiVahvistettu(oppiaine) ||
      modelItems(oppiaine, 'osasuoritukset').some(arvioituTaiVahvistettu)
  )
