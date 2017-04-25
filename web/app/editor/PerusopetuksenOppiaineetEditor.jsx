import React from 'baret'
import Bacon from 'baconjs'
import {Editor} from './Editor.jsx'
import {PropertiesEditor, shouldShowProperty} from './PropertiesEditor.jsx'
import {EnumEditor} from './EnumEditor.jsx'
import {wrapOptional} from './OptionalEditor.jsx'
import R from 'ramda'
import * as L from 'partial.lenses'
import {
  contextualizeSubModel,
  createOptionalEmpty,
  lensedModel,
  modelData,
  modelErrorMessages,
  modelItems,
  modelLens,
  modelLookup,
  modelSetValue,
  pushModel,
  pushRemoval,
  oneOfPrototypes,
  findModelProperty,
  modelProperties,
  addContext
} from './EditorModel'
import {sortGrades} from '../sorting'
import {suoritusValmis, hasArvosana, setTila, lastArviointiLens, suoritusKesken} from './Suoritus'
import {UusiPerusopetuksenOppiaineEditor} from './UusiPerusopetuksenOppiaineEditor.jsx'
import {PerusopetuksenOppiaineEditor} from './PerusopetuksenOppiaineEditor.jsx'
import {isPaikallinen} from './Koulutusmoduuli'

var pakollisetTitle = 'Pakolliset oppiaineet'
var valinnaisetTitle = 'Valinnaiset oppiaineet'
let groupTitleForSuoritus = suoritus => modelData(suoritus).koulutusmoduuli.pakollinen ? pakollisetTitle : valinnaisetTitle

export const PerusopetuksenOppiaineetEditor = ({model}) => {
  model = addContext(model, { suoritus: model })
  let osasuoritukset = modelItems(model, 'osasuoritukset')

  let korotus = osasuoritukset.find(s => modelData(s, 'korotus')) ? ['† = perusopetuksen päättötodistuksen arvosanan korotus'] : []
  let yksilöllistetty = osasuoritukset.find(s => modelData(s, 'yksilöllistettyOppimäärä')) ? ['* = yksilöllistetty oppimäärä'] : []
  let painotettu = osasuoritukset.find(s => modelData(s, 'painotettuOpetus')) ? ['** = painotettu opetus'] : []
  let selitteet = korotus.concat(yksilöllistetty).concat(painotettu).join(', ')
  let uusiOppiaineenSuoritus = createOppiaineenSuoritus(modelLookup(model, 'osasuoritukset'))
  let koulutusmoduuliProtos = oneOfPrototypes(modelLookup(uusiOppiaineenSuoritus, 'koulutusmoduuli'))
  let hasPakollisuus = !isToimintaAlueittain(model) && koulutusmoduuliProtos.some((km) => findModelProperty(km, p=>p.key=='pakollinen'))
  return (<div className="oppiaineet">
    <h5>Oppiaineiden arvosanat</h5>
    <p>Arvostelu 4-10, S (suoritettu) tai H (hylätty)</p>
    {
      hasPakollisuus
        ? <GroupedOppiaineetEditor model={model} uusiOppiaineenSuoritus={uusiOppiaineenSuoritus}/>
        : <SimpleOppiaineetEditor model={model} uusiOppiaineenSuoritus={uusiOppiaineenSuoritus}/>
    }
    {selitteet && <p className="selitteet">{selitteet}</p>}
  </div>)
}

export const isToimintaAlueittain = (model) => !!modelData(model.context.opiskeluoikeus, 'lisätiedot.erityisenTuenPäätös.opiskeleeToimintaAlueittain')

const GroupedOppiaineetEditor = ({model, uusiOppiaineenSuoritus}) => {
  let groups = [pakollisetTitle, valinnaisetTitle]
  let groupedSuoritukset = R.groupBy(groupTitleForSuoritus, modelItems(model, 'osasuoritukset'))
  return (<span>{groups.map(pakollisuus => {
    let onPakolliset = pakollisuus === 'Pakolliset oppiaineet'
    let suoritukset = groupedSuoritukset[pakollisuus] || []
    let addOppiaine = oppiaine => pushModel(oppiaine, model.context.changeBus)
    return (<section className={onPakolliset ? 'pakolliset' : 'valinnaiset'} key={pakollisuus}>
      {(suoritukset.length > 0 || model.context.edit) && (<section>
        {groups.length > 1 && <h5>{pakollisuus}</h5>}
        <Oppiainetaulukko model={model} suoritukset={suoritukset} uusiOppiaineenSuoritus={uusiOppiaineenSuoritus} pakolliset={onPakolliset} />
        <UusiPerusopetuksenOppiaineEditor suoritukset={suoritukset} oppiaineenSuoritus={uusiOppiaineenSuoritus} pakollinen={onPakolliset} resultCallback={addOppiaine} organisaatioOid={modelData(model.context.toimipiste).oid} />
      </section>)
      }
      {
        onPakolliset ? null : <KäyttäytymisenArvioEditor model={model} />
      }
    </section>)
  })}</span>)
}

const SimpleOppiaineetEditor = ({model, uusiOppiaineenSuoritus}) => {
  let addOppiaine = oppiaine => pushModel(oppiaine, model.context.changeBus)
  let suoritukset = modelItems(model, 'osasuoritukset')
  return (<span>
    <section>
      <Oppiainetaulukko model={model} suoritukset={suoritukset} uusiOppiaineenSuoritus={uusiOppiaineenSuoritus} pakolliset={false} />
      <UusiPerusopetuksenOppiaineEditor suoritukset={suoritukset} oppiaineenSuoritus={uusiOppiaineenSuoritus} resultCallback={addOppiaine} organisaatioOid={modelData(model.context.toimipiste).oid} />
    </section>
    <KäyttäytymisenArvioEditor model={model}/>
  </span>)
}

const KäyttäytymisenArvioEditor = ({model}) => {
  let edit = model.context.edit
  let käyttäytymisenArvioModel = modelLookup(model, 'käyttäytymisenArvio')
  return (käyttäytymisenArvioModel && (edit || modelData(käyttäytymisenArvioModel)))? (<div className="kayttaytyminen">
    <h5>Käyttäytymisen arviointi</h5>
    {
      <Editor model={model} path="käyttäytymisenArvio"/>
    }
  </div>) : null

}

let createOppiaineenSuoritus = (osasuoritukset) => {
  osasuoritukset = wrapOptional({model: osasuoritukset})
  let newItemIndex = modelItems(osasuoritukset).length
  let oppiaineenSuoritusProto = contextualizeSubModel(osasuoritukset.arrayPrototype, osasuoritukset, newItemIndex)
  let preferredClass = isToimintaAlueittain(oppiaineenSuoritusProto) ? 'perusopetuksentoiminta_alueensuoritus' : 'oppiaineensuoritus'
  let sortValue = (suoritusProto) => suoritusProto.value.classes.includes(preferredClass) ? 0 : 1
  oppiaineenSuoritusProto = oneOfPrototypes(oppiaineenSuoritusProto).sort((a, b) => sortValue(a) - sortValue(b))[0]
  return contextualizeSubModel(oppiaineenSuoritusProto, osasuoritukset, newItemIndex)
}

const Oppiainetaulukko = ({suoritukset, pakolliset, uusiOppiaineenSuoritus}) => {
  if (!suoritukset.length) return null
  let edit = suoritukset[0].context.edit
  let showLaajuus = !!suoritukset.find(s => modelData(s, 'koulutusmoduuli.laajuus')) || edit && !pakolliset
  let showFootnotes = !edit && !!suoritukset.find(s => modelData(s, 'yksilöllistettyOppimäärä') ||modelData(s, 'painotettuOpetus') || modelData(s, 'korotus'))
  return (<table>
      <thead>
      <tr>
        <th className="oppiaine">Oppiaine</th>
        <th className="arvosana" colSpan={(showFootnotes && !showLaajuus) ? '2' : '1'}>Arvosana</th>
        {showLaajuus && <th className="laajuus" colSpan={showFootnotes ? '2' : '1'}>Laajuus</th>}
      </tr>
      </thead>
      {
        suoritukset.map((suoritus) => (<OppiaineenSuoritusEditor key={suoritus.arrayKey} model={suoritus} uusiOppiaineenSuoritus={uusiOppiaineenSuoritus} showLaajuus={showLaajuus} showFootnotes={showFootnotes}/> ))
      }
    </table>
  )
}


let fixTila = (model) => {
  return lensedModel(model, L.rewrite(m => {
    if (hasArvosana(m) && !suoritusValmis(m)) {
      return setTila(m, 'VALMIS')
    }
    return m
  }))
}

let fixArvosana = (model) => {
  let arviointiLens = modelLens('arviointi')
  return lensedModel(model, L.rewrite(m => {
    var arviointiModel = L.get(arviointiLens, m)
    if (!suoritusValmis(m)) {
      return L.set(arviointiLens, createOptionalEmpty(arviointiModel), m)
    }
    return m
  }))
}

export const OppiaineenSuoritusEditor = React.createClass({
  render() {
    let {model, showLaajuus, showFootnotes, uusiOppiaineenSuoritus} = this.props
    let {expanded} = this.state

    let oppiaine = modelLookup(model, 'koulutusmoduuli')
    let edit = model.context.edit
    let className = 'oppiaine'
      + ' ' + (modelData(model, 'koulutusmoduuli.pakollinen') ? 'pakollinen' : 'valinnainen')
      + ' ' + modelData(oppiaine, 'tunniste').koodiarvo
      + ' ' + modelData(model, 'tila.koodiarvo').toLowerCase()
      + (expanded ? ' expanded' : '')
      + (isPaikallinen(oppiaine) ? ' paikallinen' : '')

    let extraPropertiesFilter = p => {
      if (!edit && ['yksilöllistettyOppimäärä', 'painotettuOpetus', 'tila', 'suorituskieli', 'korotus'].includes(p.key)) return false // these are only shown when editing
      if (['koulutusmoduuli', 'arviointi', 'tunniste', 'kieli', 'laajuus', 'pakollinen', 'arvosana', 'päivä'].includes(p.key)) return false // these are never shown
      return shouldShowProperty(model.context)(p)
    }

    let extraProperties = modelProperties(modelLookup(model, 'arviointi.-1'))
      .concat(modelProperties(oppiaine))
      .concat(modelProperties(fixArvosana(model)))
      .filter(extraPropertiesFilter)

    let showExpand = extraProperties.length > 0
    let toggleExpand = () => { this.setState({expanded : !expanded}) }

    return (<tbody className={className}>
    <tr>
      <td className="oppiaine">
        { // expansion link
          showExpand && <a className="toggle-expand" onClick={toggleExpand}>{ expanded ? '' : ''}</a>
        }
        <PerusopetuksenOppiaineEditor {...{oppiaine, showExpand, toggleExpand, uusiOppiaineenSuoritus}}/>

      </td>
      <td className="arvosana">
        <span className="value"><ArvosanaEditor model={ lensedModel(fixTila(model), lastArviointiLens) } /></span>
      </td>
      {
        showLaajuus && (<td className="laajuus">
          <Editor model={model} path="koulutusmoduuli.laajuus"/>
        </td>)
      }
      {
        showFootnotes && (
          <td className="footnotes">
            <div className="footnotes-container">
              {modelData(model, 'yksilöllistettyOppimäärä') ? <sup className="yksilollistetty" title="Yksilöllistetty oppimäärä"> *</sup> : null}
              {modelData(model, 'painotettuOpetus') ? <sup className="painotettu" title="Painotettu opetus"> **</sup> : null}
              {modelData(model, 'korotus') ? <sup className="korotus" title="Perusopetuksen päättötodistuksen arvosanan korotus"> †</sup> : null}
            </div>
          </td>
        )
      }
      {
        model.context.edit && (
          <td>
            <a className="remove-value" onClick={() => pushRemoval(model)}></a>
          </td>
        )
      }
    </tr>
    {
      expanded && <tr key='details'><td colSpan="4" className="details"><PropertiesEditor context={model.context} properties={extraProperties} /></td></tr>
    }
    {
      modelErrorMessages(model).map((error, i) => <tr key={'error-' + i} className="error"><td colSpan="4" className="error">{error}</td></tr>)
    }
    </tbody>)
  },
  getInitialState() {
    return { expanded: false }
  }
})

OppiaineenSuoritusEditor.validateModel = (m) => {
  if (suoritusValmis(m) && !hasArvosana(m)) {
    return [{key: 'missing', message: 'Suoritus valmis, mutta arvosana puuttuu'}]
  }
  if (suoritusKesken(m) && m.context && m.context.suoritus && suoritusValmis(m.context.suoritus)) {
    return [{key: 'osasuorituksenTilla', message: 'Oppiaineen suoritus ei voi olla KESKEN, kun päätason suoritus on VALMIS'}]
  }
}

const ArvosanaEditor = ({model}) => {
  let alternativesP = completeWithFieldAlternatives(oneOfPrototypes(wrapOptional({model})), 'arvosana').startWith([])
  let arvosanatP = alternativesP.map(alternatives => alternatives.map(m => modelLookup(m, 'arvosana').value))
  return (<span>{
    alternativesP.map(alternatives => {
      let arvosanaLens = modelLens('arvosana')
      let coolLens = L.lens(
        (m) => L.get(arvosanaLens, m),
        (v, m) => {
          let found = alternatives.find(alt => {
            return modelData(alt, 'arvosana').koodiarvo == modelData(v).koodiarvo
          })
          return modelSetValue(m, found.value)
        }
      )
      let arvosanaModel = lensedModel(model, coolLens)
      // Use key to ensure re-render when alternatives are supplied
      return <Editor key={alternatives.length} model={ arvosanaModel } sortBy={sortGrades} fetchAlternatives={() => arvosanatP}/>
    })
  }</span>)
}

export const completeWithFieldAlternatives = (models, path) => {
  const alternativesForField = (model) => EnumEditor.fetchAlternatives(modelLookup(model, path))
    .map(alternatives => alternatives.map(enumValue => modelSetValue(model, enumValue, path)))
  return Bacon.combineAsArray(models.map(alternativesForField)).last().map(x => x.flatten()).startWith([])
}