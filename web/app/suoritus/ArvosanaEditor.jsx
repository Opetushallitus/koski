import React from 'baret'
import {Editor} from '../editor/Editor'
import {wrapOptional, modelEmpty} from '../editor/EditorModel'
import * as L from 'partial.lenses'
import {lensedModel, modelData, modelLookup, modelSetValue, oneOfPrototypes} from '../editor/EditorModel'
import {sortGrades} from '../util/sorting'
import {fetchAlternativesBasedOnPrototypes} from '../editor/EnumEditor'
import {fixArviointi} from './Suoritus'

export const ArvosanaEditor = ({model}) => {
  if (!model.context.edit) {
    const arvosanaModel = resolveArvosanaModel(model)
    return arvosanaModel ? <Editor model={ arvosanaModel }/> : null
  }
  model = fixArviointi(model)
  const alternativesP = fetchAlternativesBasedOnPrototypes(oneOfPrototypes(wrapOptional(modelLookup(model, 'arviointi.-1'))), 'arvosana').startWith([])
  const arvosanatP = alternativesP.map(alternatives => alternatives.map(m => modelLookup(m, 'arvosana').value))
  return (<span>{
    alternativesP.map(alternatives => {
      const arvosanaLens = L.lens(
        (m) => {
          return modelLookup(m, '-1.arvosana')
        },
        (v, m) => {
          if (modelData(v)) {
            // Arvosana valittu -> valitaan vastaava prototyyppi (eri prototyypit eri arvosanoille)
            const valittuKoodiarvo = modelData(v).koodiarvo
            const found = alternatives.find(alt => {
              return modelData(alt, 'arvosana').koodiarvo == valittuKoodiarvo
            })
            return modelSetValue(m, found.value, '-1')
          } else {
            // Ei arvosanaa -> poistetaan arviointi kokonaan
            return modelSetValue(m, undefined)
          }
        }
      )
      const arviointiModel = modelLookup(model, 'arviointi')
      const arvosanaModel = lensedModel(arviointiModel, arvosanaLens)
      // Use key to ensure re-render when alternatives are supplied
      return <Editor key={alternatives.length} model={ arvosanaModel } sortBy={sortGrades} fetchAlternatives={() => arvosanatP} showEmptyOption="true"/>
    })
  }</span>)
}

const resolveArvosanaModel = model => {
  const arviointi = modelLookup(model, 'arviointi.-1')
  const arvosana = arviointi ? modelLookup(model, 'arviointi.-1.arvosana') : null

  const isPaikallinenArviointi = arviointi && !modelEmpty(arviointi) && arviointi.value.classes.includes('paikallinenarviointi')

  return isPaikallinenArviointi ? modelLookup(arvosana, 'nimi') : arvosana
}
