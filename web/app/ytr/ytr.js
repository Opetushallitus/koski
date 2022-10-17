import { modelData } from '../editor/EditorModel'

export const pakollisetKokeetSuoritettuEnnen1990 = (model) =>
  modelData(model, 'tyyppi.koodiarvo') === 'ylioppilastutkinto' &&
  modelData(model, 'pakollisetKokeetSuoritettu') &&
  suorituksetEnnen1990(modelData(model, 'osasuoritukset'))

const suorituksetEnnen1990 = (osasuoritukset) =>
  osasuoritukset &&
  osasuoritukset
    .map((osasuoritus) => osasuoritus.tutkintokerta.vuosi)
    .every((vuosi) => vuosi < 1990)
