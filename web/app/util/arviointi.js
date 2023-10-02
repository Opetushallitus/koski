import { sort } from 'fp-ts/lib/Array'
import { contramap } from 'fp-ts/lib/Ord'
import { Ord as StringOrd } from 'fp-ts/lib/string'
import { modelData, modelItems, modelLookup } from '../editor/EditorModel'

export const viimeisinArviointi = (suoritus, arviointiField = 'arviointi') => {
  const arvioinnit = modelData(suoritus, arviointiField)
  return arvioinnit && sort(ArviointiDateOrd)(arvioinnit)[arvioinnit.length - 1]
}

export const parasArviointi = (suoritus, arviointiField = 'arviointi') => {
  const arvioinnit = sort(ArviointiDateOrd)(
    modelItems(suoritus, arviointiField)
  )
  let arviointi = arvioinnit[arvioinnit.length - 1]
  let arvosana = arviointi
    ? modelLookup(suoritus, `${arviointiField}.-1.arvosana`)
    : null

  arvioinnit.map((item) => {
    const nthArvosana = modelLookup(item, 'arvosana')
    if (
      nthArvosana.value.data !== undefined &&
      (nthArvosana.value.data.koodiarvo === 'S' ||
        parseInt(nthArvosana.value.data.koodiarvo) >
          parseInt(arvosana.value.data.koodiarvo))
    ) {
      arviointi = item
      arvosana = nthArvosana
    }
  })

  return arviointi
}

export const ArviointiDateOrd = contramap(
  (a) => modelData(a, 'päivä') || '0000-00-00'
)(StringOrd)
