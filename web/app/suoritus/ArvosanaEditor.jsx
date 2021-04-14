import React from 'baret'
import {Editor} from '../editor/Editor'
import {wrapOptional, modelEmpty, modelProperty} from '../editor/EditorModel'
import * as L from 'partial.lenses'
import {lensedModel, modelData, modelLookup, modelSetValue, oneOfPrototypes, pushModel, contextualizeSubModel, modelItems, resetOptionalModel} from '../editor/EditorModel'
import {sortGrades} from '../util/sorting'
import {fetchAlternativesBasedOnPrototypes} from '../editor/EnumEditor'
import {fixArviointi} from './Suoritus'
import {DateEditor} from '../editor/DateEditor'

let global_run = 0

export const ArvosanaEditor = ({model, notFoundText}) => {
  if (!model.context.edit) {
    const arvosanaModel = resolveArvosanaModel(model)
    return arvosanaModel
      ? <Editor model={ arvosanaModel }/>
      : (notFoundText ? <span>{notFoundText}</span> : null)
  }

  if (!modelProperty(model, 'arviointi')) {
    return null
  }

  model = fixArviointi(model)

  const arvioinnitModel = wrapOptional(modelLookup(model, 'arviointi'))

  let arviointiItems = modelItems(arvioinnitModel)

  let addItem = () => {
    pushModel(contextualizeSubModel(arvioinnitModel.arrayPrototype, arvioinnitModel, arviointiItems.length))
  }

  return (<div>
      {
        arviointiItems.map(m => {
          return (<div>
            {YksittainenArvosana(m)}
            {YksittainenArviointipaiva(m)}
            <br/>
            <span>
              <a className="remove-value" style={{position:"static"}} onClick={() => resetOptionalModel(m, undefined)}>Poista arviointi</a>
            </span>
          </div>)
        })
     }
    <a className="add-value" onClick={() => addItem()}>Lisää arviointi</a>
  </div>)
}

export const YksittainenArvosana = model => {
  const alternativesP = fetchAlternativesBasedOnPrototypes(oneOfPrototypes(wrapOptional(model)), 'arvosana').startWith([])
  const arvosanatP = alternativesP.map(alternatives => alternatives.map(m => modelLookup(m, 'arvosana').value))

  return (<span>{
    alternativesP.map(alternatives => {
      const arvosanaLens = L.lens(
        (m) => {
          return modelLookup(m, 'arvosana')
        },
        (v, m) => {
          const valittu = modelData(v)
          if (valittu) {
            // Arvosana valittu -> valitaan vastaava prototyyppi (eri prototyypit eri arvosanoille)
            const found = alternatives.find(alt => {
              const altData = modelData(alt, 'arvosana')
              return altData.koodiarvo === valittu.koodiarvo && altData.koodistoUri === valittu.koodistoUri
            })
            return modelSetValue(m, found.value)
          } else {
            // Ei arvosanaa -> poistetaan arviointi kokonaan
            return modelSetValue(m, undefined)
          }
        }
      )
      const arviointiModel = model
      const arvosanaModel = lensedModel(arviointiModel, arvosanaLens)
      // Use key to ensure re-render when alternatives are supplied
      return <Editor key={alternatives.length} model={ arvosanaModel } sortBy={sortGrades} fetchAlternatives={() => arvosanatP} showEmptyOption="true"/>
    })
  }</span>)
}

export const YksittainenArviointipaiva = model => {
  const päiväModel = modelLookup(model, 'päivä')

  return (<span>
    <DateEditor model={päiväModel} />
  </span>)
}

export const resolveArvosanaModel = model => {
  const arviointi = modelLookup(model, 'arviointi.-1')
  const arvosana = arviointi ? modelLookup(model, 'arviointi.-1.arvosana') : null

  const isPaikallinenArviointi = arviointi && !modelEmpty(arviointi) && arviointi.value.classes.includes('paikallinenarviointi')

  return isPaikallinenArviointi ? modelLookup(arvosana, 'nimi') : arvosana
}
