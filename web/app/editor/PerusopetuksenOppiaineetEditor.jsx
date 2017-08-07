import React from 'baret'
import Bacon from 'baconjs'
import {Editor} from './Editor.jsx'
import {PropertiesEditor, shouldShowProperty} from './PropertiesEditor.jsx'
import {PropertyEditor} from './PropertyEditor.jsx'
import {KurssiEditor} from './KurssiEditor.jsx'
import {EnumEditor} from './EnumEditor.jsx'
import {wrapOptional} from './OptionalEditor.jsx'
import R from 'ramda'
import * as L from 'partial.lenses'
import {
  addContext,
  contextualizeSubModel,
  createOptionalEmpty,
  ensureArrayKey,
  findModelProperty,
  lensedModel,
  modelData,
  modelErrorMessages,
  modelItems,
  modelLens,
  modelLookup,
  modelProperties,
  modelSet,
  modelSetValue,
  oneOfPrototypes,
  pushModel,
  pushRemoval
} from './EditorModel'
import {sortGrades} from '../sorting'
import {hasArvosana, lastArviointiLens, setTila, suoritusKesken, suoritusValmis} from './Suoritus'
import {UusiPerusopetuksenOppiaineDropdown} from './UusiPerusopetuksenOppiaineDropdown.jsx'
import {PerusopetuksenOppiaineEditor} from './PerusopetuksenOppiaineEditor.jsx'
import {isPaikallinen} from './Koulutusmoduuli'
import {accumulateExpandedState} from './ExpandableItems'
import {t} from '../i18n'
import Text from '../Text.jsx'
import {isToimintaAlueittain, isYsiluokka, jääLuokalle, luokkaAste, luokkaAsteenOsasuoritukset} from './Perusopetus'

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
  let uusiOppiaineenSuoritus = model.context.edit ? createOppiaineenSuoritus(modelLookup(model, 'osasuoritukset')) : null
  let showOppiaineet = !(isYsiluokka(model) && !jääLuokalle(model)) && (model.context.edit || osasuoritukset.filter(R.complement(suoritusKesken)).length > 0)

  if (isYsiluokka(model) && jääLuokalle(model) && osasuoritukset.length == 0) {
    luokkaAsteenOsasuoritukset(luokkaAste(model), isToimintaAlueittain(model)).onValue(oppiaineet => {
      pushModel(modelSetValue(model, oppiaineet.value, 'osasuoritukset'))
    })
  } else if (isYsiluokka(model) && !jääLuokalle(model) && osasuoritukset.length > 0) {
    pushModel(modelSetValue(model, [], 'osasuoritukset'))
  }

  return (<div className="oppiaineet">
    { isYsiluokka(model) && (<div className="ysiluokka-jaa-luokalle">
        <PropertyEditor model={model} propertyName="jääLuokalle" />
        { model.context.edit && <em><Text name="Oppiaineiden arvioinnit syötetään 9. vuosiluokalla vain, jos oppilas jää luokalle"/></em> }
      </div>)
    }
    { showOppiaineet && (<div>
        <h5><Text name="Oppiaineiden arvosanat"/></h5>
        <p><Text name="Arvostelu 4-10, S (suoritettu) tai H (hylätty)"/></p>
        {
          hasPakollisuus(model, uusiOppiaineenSuoritus)
            ? <GroupedOppiaineetEditor model={model} uusiOppiaineenSuoritus={uusiOppiaineenSuoritus}/>
            : <SimpleOppiaineetEditor model={model} uusiOppiaineenSuoritus={uusiOppiaineenSuoritus}/>
        }
        {selitteet && <p className="selitteet">{selitteet}</p>}
      </div>)
    }
  </div>)
}

const hasPakollisuus = (model, uusiOppiaineenSuoritus) => {
  let oppiaineHasPakollisuus = (oppiaine) => findModelProperty(oppiaine, p=>p.key=='pakollinen')
  let koulutusmoduuliProtos = oneOfPrototypes(modelLookup(uusiOppiaineenSuoritus, 'koulutusmoduuli'))
  return !isToimintaAlueittain(model) && (koulutusmoduuliProtos.some(oppiaineHasPakollisuus) || modelItems(model, 'osasuoritukset').map(m => modelLookup(m, 'koulutusmoduuli')).some(oppiaineHasPakollisuus))
}

const GroupedOppiaineetEditor = ({model, uusiOppiaineenSuoritus}) => {
  let groups = [pakollisetTitle, valinnaisetTitle]
  let groupedSuoritukset = R.groupBy(groupTitleForSuoritus, modelItems(model, 'osasuoritukset'))
  return (<span>{groups.map(pakollisuus => {
    let pakollinen = pakollisuus === 'Pakolliset oppiaineet'
    let suoritukset = groupedSuoritukset[pakollisuus] || []

    return (<section className={pakollinen ? 'pakolliset' : 'valinnaiset'} key={pakollisuus}>
      <Oppiainetaulukko model={model} title={groups.length > 1 && pakollisuus} suoritukset={suoritukset} uusiOppiaineenSuoritus={uusiOppiaineenSuoritus} pakolliset={pakollinen} />
      {
        pakollinen ? null : <KäyttäytymisenArvioEditor model={model} />
      }
    </section>)
  })}</span>)
}

const SimpleOppiaineetEditor = ({model, uusiOppiaineenSuoritus}) => {
  let suoritukset = modelItems(model, 'osasuoritukset')
  return (<span>
    <Oppiainetaulukko model={model} suoritukset={suoritukset} uusiOppiaineenSuoritus={uusiOppiaineenSuoritus}/>
    <KäyttäytymisenArvioEditor model={model}/>
  </span>)
}

const KäyttäytymisenArvioEditor = ({model}) => {
  let edit = model.context.edit
  let käyttäytymisenArvioModel = modelLookup(model, 'käyttäytymisenArvio')
  return (käyttäytymisenArvioModel && (edit || modelData(käyttäytymisenArvioModel)))? (<div className="kayttaytyminen">
    <h5><Text name="Käyttäytymisen arviointi"/></h5>
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

class Oppiainetaulukko extends React.Component {
  render() {
    let {model, suoritukset, title, pakolliset, uusiOppiaineenSuoritus} = this.props
    let { isExpandedP, setExpanded } = accumulateExpandedState({suoritukset, filter: s => expandableProperties(s).length > 0, component: this})

    let edit = model.context.edit
    let showLaajuus = !!suoritukset.find(s => modelData(s, 'koulutusmoduuli.laajuus')) || edit && !pakolliset
    let showFootnotes = !edit && !!suoritukset.find(s => modelData(s, 'yksilöllistettyOppimäärä') ||modelData(s, 'painotettuOpetus') || modelData(s, 'korotus'))
    let addOppiaine = oppiaine => {
      var suoritusUudellaOppiaineella = modelSet(uusiOppiaineenSuoritus, oppiaine, 'koulutusmoduuli')
      pushModel(suoritusUudellaOppiaineella, model.context.changeBus)
      ensureArrayKey(suoritusUudellaOppiaineella)
      setExpanded(suoritusUudellaOppiaineella)(true)
    }

    if (suoritukset.length == 0 && !model.context.edit) return null
    let placeholder = t(pakolliset == undefined
      ? 'Lisää oppiaine'
      : (pakolliset ? 'Lisää pakollinen oppiaine' : 'Lisää valinnainen oppiaine'))

    return (<section>
        {title && <h5>{title}</h5>}
        { suoritukset.length > 0 && (
          <table>
            <thead>
            <tr>
              <th className="oppiaine"><Text name="Oppiaine"/></th>
              <th className="arvosana" colSpan={(showFootnotes && !showLaajuus) ? '2' : '1'}><Text name="Arvosana"/></th>
              {showLaajuus && <th className="laajuus" colSpan={showFootnotes ? '2' : '1'}><Text name="Laajuus"/></th>}
            </tr>
            </thead>
            {
              suoritukset.map((suoritus) => (<OppiaineenSuoritusEditor baret-lift
                                                                       key={suoritus.arrayKey} model={suoritus} uusiOppiaineenSuoritus={uusiOppiaineenSuoritus}
                                                                       expanded={isExpandedP(suoritus)} onExpand={setExpanded(suoritus)}
                                                                       showLaajuus={showLaajuus} showFootnotes={showFootnotes}/> ))
            }
          </table>
        )}
        <UusiPerusopetuksenOppiaineDropdown suoritukset={suoritukset} oppiaineenSuoritus={uusiOppiaineenSuoritus}
                                            pakollinen={pakolliset} resultCallback={addOppiaine}
                                            organisaatioOid={modelData(model.context.toimipiste).oid} // FIXME: no toimipiste in model.context
                                            placeholder={placeholder}/>
      </section>
    )
  }
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

let expandableProperties = (model) => {
  let edit = model.context.edit
  let oppiaine = modelLookup(model, 'koulutusmoduuli')

  let extraPropertiesFilter = p => {
    if (!edit && ['yksilöllistettyOppimäärä', 'painotettuOpetus', 'tila', 'suorituskieli', 'korotus'].includes(p.key)) return false // these are only shown when editing
    if (['koulutusmoduuli', 'arviointi', 'tunniste', 'kieli', 'laajuus', 'pakollinen', 'arvosana', 'päivä', 'perusteenDiaarinumero', 'osasuoritukset'].includes(p.key)) return false // these are never shown
    return shouldShowProperty(model.context)(p)
  }

  return modelProperties(oppiaine)
    .concat(modelProperties(fixArvosana(model)))
    .filter(extraPropertiesFilter)

}
export class OppiaineenSuoritusEditor extends React.Component {
  render() {
    let {model, showLaajuus, showFootnotes, uusiOppiaineenSuoritus, expanded, onExpand} = this.props

    let oppiaine = modelLookup(model, 'koulutusmoduuli')
    let className = 'oppiaine'
      + ' ' + (modelData(model, 'koulutusmoduuli.pakollinen') ? 'pakollinen' : 'valinnainen')
      + ' ' + modelData(oppiaine, 'tunniste').koodiarvo
      + ' ' + modelData(model, 'tila.koodiarvo').toLowerCase()
      + (expanded ? ' expanded' : '')
      + (isPaikallinen(oppiaine) ? ' paikallinen' : '')


    let extraProperties = expandableProperties(model)

    let showExpand = extraProperties.length > 0

    let sanallinenArvioProperties = modelProperties(modelLookup(model, 'arviointi.-1'), p => p.key == 'kuvaus')

    return (<tbody className={className}>
    <tr>
      <td className="oppiaine">
        { // expansion link
          showExpand && <a className="toggle-expand" onClick={() => onExpand(!expanded)}>{ expanded ? '' : ''}</a>
        }
        <PerusopetuksenOppiaineEditor {...{oppiaine, showExpand, expanded, onExpand, uusiOppiaineenSuoritus}}/>

      </td>
      <td className="arvosana">
        <span className="value"><ArvosanaEditor model={ lensedModel(fixTila(model), lastArviointiLens) } /></span>
      </td>
      {
        showLaajuus && (<td className="laajuus">
          <Editor model={model} path="koulutusmoduuli.laajuus" compact="true"/>
        </td>)
      }
      {
        showFootnotes && (
          <td className="footnotes">
            <div className="footnotes-container">
              {modelData(model, 'yksilöllistettyOppimäärä') ? <sup className="yksilollistetty" title={t('Yksilöllistetty oppimäärä')}>{' *'}</sup> : null}
              {modelData(model, 'painotettuOpetus') ? <sup className="painotettu" title={t('Painotettu opetus')}>{' **'}</sup> : null}
              {modelData(model, 'korotus') ? <sup className="korotus" title={t('Perusopetuksen päättötodistuksen arvosanan korotus')}>{' †'}</sup> : null}
            </div>
          </td>
        )
      }
      {
        model.context.edit && (
          <td>
            <a className="remove-value" onClick={() => pushRemoval(model)}>{''}</a>
          </td>
        )
      }
    </tr>
    {
      sanallinenArvioProperties.length > 0 && <tr key='sanallinenArviointi' className="sanallinen-arviointi"><td colSpan="4" className="details"><PropertiesEditor properties={sanallinenArvioProperties} context={model.context} /></td></tr>
    }
    {
      expanded && <tr key='details'><td colSpan="4" className="details"><PropertiesEditor context={model.context} properties={extraProperties} /></td></tr>
    }
    <PerusopetuksenKurssitEditor model={model}/>
    {
      modelErrorMessages(model).map((error, i) => <tr key={'error-' + i} className="error"><td colSpan="4" className="error">{error}</td></tr>)
    }
    </tbody>)
  }
}

const PerusopetuksenKurssitEditor = ({model}) => {
  let kurssit = modelItems(model, 'osasuoritukset')
  if (!kurssit.length) return null
  return (<tr className="kurssit"><td colSpan="4"><ul className="kurssit">{
    kurssit.map((kurssi, kurssiIndex) =>
      <KurssiEditor key={kurssiIndex} kurssi={kurssi}/>
    )
  }</ul></td></tr>)
}

OppiaineenSuoritusEditor.validateModel = (m) => {
  if (suoritusValmis(m) && !hasArvosana(m)) {
    return [{key: 'missing', message: <Text name='Suoritus valmis, mutta arvosana puuttuu'/>}]
  }
  if (suoritusKesken(m) && m.context && m.context.suoritus && suoritusValmis(m.context.suoritus)) {
    return [{key: 'osasuorituksenTilla', message: <Text name='Oppiaineen suoritus ei voi olla KESKEN, kun päätason suoritus on VALMIS'/>}]
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
  return Bacon.combineAsArray(models.map(alternativesForField)).last().map(x => x.flatten())
}