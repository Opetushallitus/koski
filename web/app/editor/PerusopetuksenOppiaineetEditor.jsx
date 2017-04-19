import React from 'baret'
import Bacon from 'baconjs'
import {Editor} from './Editor.jsx'
import {PropertiesEditor} from './PropertiesEditor.jsx'
import {PropertyEditor} from './PropertyEditor.jsx'
import {EnumEditor} from './EnumEditor.jsx'
import {wrapOptional} from './OptionalEditor.jsx'
import DropDown from '../Dropdown.jsx'
import R from 'ramda'
import * as L from 'partial.lenses'
import {
  addContext,
  contextualizeSubModel,
  createOptionalEmpty,
  lensedModel,
  modelData,
  modelErrorMessages,
  modelItems,
  modelLens,
  modelLookup,
  modelSet,
  modelSetData,
  modelSetValue,
  modelTitle,
  pushModel,
  pushRemoval,
  oneOfPrototypes
} from './EditorModel'
import {sortGrades, sortLanguages} from '../sorting'
import {suoritusValmis, hasArvosana, setTila, lastArviointiLens} from './Suoritus'
import {saveOrganizationalPreference, getOrganizationalPreferences} from '../organizationalPreferences'
import {doActionWhileMounted} from '../util'

export const PerusopetuksenOppiaineetEditor = ({model}) => {
  let käyttäytymisenArvioModel = modelLookup(model, 'käyttäytymisenArvio')
  let oppiaineSuoritukset = R.groupBy((o => modelData(o).koulutusmoduuli.pakollinen ? 'Pakolliset oppiaineet' : 'Valinnaiset oppiaineet'), modelItems(model, 'osasuoritukset'))
  let osasuoritukset = modelItems(model, 'osasuoritukset')
  let korotus = osasuoritukset.find(s => modelData(s, 'korotus')) ? ['† = perusopetuksen päättötodistuksen arvosanan korotus'] : []
  let yksilöllistetty = osasuoritukset.find(s => modelData(s, 'yksilöllistettyOppimäärä')) ? ['* = yksilöllistetty oppimäärä'] : []
  let painotettu = osasuoritukset.find(s => modelData(s, 'painotettuOpetus')) ? ['** = painotettu opetus'] : []
  let selitteet = korotus.concat(yksilöllistetty).concat(painotettu).join(', ')

  let groups = ['Pakolliset oppiaineet', 'Valinnaiset oppiaineet']
  groups = oppiaineSuoritukset['Pakolliset oppiaineet'] || model.context.edit ? groups : groups.slice(1) // shove valinnaiset to left if there are no pakolliset

  return (<div className="oppiaineet">
    <h5>Oppiaineiden arvosanat</h5>
    <p>Arvostelu 4-10, S (suoritettu) tai H (hylätty)</p>
    {groups.map(pakollisuus => {
    let onPakolliset = pakollisuus === 'Pakolliset oppiaineet'
    let suoritukset = oppiaineSuoritukset[pakollisuus] || []
    return (<section className={onPakolliset ? 'pakolliset' : 'valinnaiset'} key={pakollisuus}>
      {(suoritukset.length > 0 || model.context.edit) && (<section>
        {groups.length > 1 && <h5>{pakollisuus}</h5>}
        <Oppiainetaulukko model={model} suoritukset={suoritukset} pakolliset={onPakolliset} />
        </section>)
      }
      {
        käyttäytymisenArvioModel && (model.context.edit || modelData(käyttäytymisenArvioModel)) && !onPakolliset && (<div className="kayttaytyminen">
        <h5>Käyttäytymisen arviointi</h5>
        {
          <Editor model={model} path="käyttäytymisenArvio"/>
        }
        </div>)
      }
      </section>)
    })
  }
  {selitteet && <p className="selitteet">{selitteet}</p>}
  </div>)
}

const Oppiainetaulukko = ({suoritukset, model, pakolliset}) => {
  let showLaajuus = !!suoritukset.find(s => modelData(s, 'koulutusmoduuli.laajuus')) || model.context.edit && !pakolliset
  let showFootnotes = !model.context.edit && !!suoritukset.find(s => modelData(s, 'yksilöllistettyOppimäärä') ||modelData(s, 'painotettuOpetus') || modelData(s, 'korotus'))
  let addOppiaine = oppiaine => pushModel(oppiaine, model.context.changeBus)
  return (<table>
      <thead>
      <tr>
        <th className="oppiaine">Oppiaine</th>
        <th className="arvosana" colSpan={(showFootnotes && !showLaajuus) ? '2' : '1'}>Arvosana</th>
        {showLaajuus && <th className="laajuus" colSpan={showFootnotes ? '2' : '1'}>Laajuus</th>}
      </tr>
      </thead>
      {
        suoritukset.map((suoritus) => (<OppiaineenSuoritusEditor key={suoritus.arrayKey} model={suoritus} showLaajuus={showLaajuus} showFootnotes={showFootnotes}/> ))
      }
      {
        model.context.edit && <NewOppiaine osasuoritukset={modelLookup(model, 'osasuoritukset')} pakollinen={pakolliset} resultCallback={addOppiaine} organisaatioOid={modelData(model.context.toimipiste).oid} />
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
    let {model, showLaajuus, showFootnotes} = this.props
    let {expanded} = this.state

    let oppiaine = modelLookup(model, 'koulutusmoduuli')
    let tunniste = modelData(oppiaine, 'tunniste')
    let sanallinenArviointi = modelTitle(model, 'arviointi.-1.kuvaus')
    let editing = model.context.edit
    let tila = modelData(model, 'tila.koodiarvo')
    let extraPropertiesFilter = p => !['koulutusmoduuli', 'arviointi'].includes(p.key)
    let showExpand = sanallinenArviointi || editing && model.value.properties.some(extraPropertiesFilter)
    let toggleExpand = () => { this.setState({expanded : !expanded}) }
    let errors = modelErrorMessages(model)
    let pakollinen = modelData(model, 'koulutusmoduuli.pakollinen')
    let pakollisuus = pakollinen ? 'pakollinen' : 'valinnainen'
    let className = 'oppiaine ' + pakollisuus + ' ' + tunniste.koodiarvo + (' ' + tila.toLowerCase()) + (expanded ? ' expanded' : '') + (isPaikallinen(oppiaine) ? ' paikallinen' : '')

    return (<tbody className={className}>
    <tr>
      <td className="oppiaine">
        { // expansion link
          showExpand && <a className={ sanallinenArviointi || editing ? 'toggle-expand' : 'toggle-expand disabled'} onClick={toggleExpand}>{ expanded ? '' : ''}</a>
        }
        <OppiaineEditor {...{oppiaine, showExpand, toggleExpand}}/>

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
      expanded && <tr key='sanallinen-arviointi'><td className="details"><PropertyEditor model={modelLookup(model, 'arviointi.-1')} propertyName="kuvaus" /></td></tr>
    }
    {
      editing && expanded && <tr key='details'><td className="details"><PropertiesEditor model={fixArvosana(model)} propertyFilter={extraPropertiesFilter} /></td></tr>
    }
    {
      errors.map((error, i) => <tr key={'error-' + i} className="error"><td className="error">{error}</td></tr>)
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

let fixKuvaus = (oppiaine) => {
  return lensedModel(oppiaine, L.rewrite(m => {
    let nimi = modelLookup(m, 'tunniste.nimi').value
    return modelSetValue(m, nimi, 'kuvaus')
  }))
}

let OppiaineEditor = React.createClass({
  render() {
    let { oppiaine, showExpand, toggleExpand } = this.props
    let oppiaineTitle = (aine) => {
      let title = modelData(aine, 'tunniste.nimi').fi + (kielenOppiaine || äidinkieli ? ', ' : '')
      return pakollinen === false ? 'Valinnainen ' + title.toLowerCase() : title
    }
    let pakollinen = modelData(oppiaine, 'pakollinen')
    let kielenOppiaine = oppiaine.value.classes.includes('peruskoulunvierastaitoinenkotimainenkieli')
    let äidinkieli = oppiaine.value.classes.includes('peruskoulunaidinkielijakirjallisuus')

    return (<span>
    {
      oppiaine.context.edit && isPaikallinen(oppiaine)
        ? <span className="koodi-ja-nimi">
              <span className="koodi"><Editor model={oppiaine} path="tunniste.koodiarvo" placeholder="Koodi"/></span>
              <span className="nimi"><Editor model={fixKuvaus(oppiaine)} path="tunniste.nimi" placeholder="Oppiaineen nimi"/></span>
          </span>
        : showExpand ? <a className="nimi" onClick={toggleExpand}>{oppiaineTitle(oppiaine)}</a> : <span className="nimi">{oppiaineTitle(oppiaine)}</span>
    }
      {
        // kielivalinta
        (kielenOppiaine || äidinkieli) && <span className="value"><Editor model={oppiaine} path="kieli" sortBy={kielenOppiaine && sortLanguages}/></span>
      }
      {
        this.state && this.state.changed && isPaikallinen(oppiaine) && doActionWhileMounted(oppiaine.context.doneEditingBus, () => {
          let data = modelData(oppiaine)
          let organisaatioOid = modelData(oppiaine.context.toimipiste).oid
          let key = data.tunniste.koodiarvo
          saveOrganizationalPreference(organisaatioOid, 'perusopetuksenoppiaineet', key, data)
        })
      }
  </span>)
  },

  componentWillReceiveProps(newProps) {
    let currentData = modelData(this.props.oppiaine)
    let newData = modelData(newProps.oppiaine)
    if (!R.equals(currentData, newData)) {
      this.setState({ changed: true})
    }
  }
})

const NewOppiaine = ({organisaatioOid, osasuoritukset, pakollinen, resultCallback}) => {
  let pakollisuus = pakollinen ? 'pakollinen' : 'valinnainen'
  let wrappedOsasuoritukset = wrapOptional({model: osasuoritukset})
  let newItemIndex = modelItems(wrappedOsasuoritukset).length
  let oppiaineenSuoritusProto = contextualizeSubModel(wrappedOsasuoritukset.arrayPrototype, wrappedOsasuoritukset, newItemIndex)
  let sortValue = (suoritusProto) => suoritusProto.value.classes.includes('oppiaineensuoritus') ? 0 : 1
  oppiaineenSuoritusProto = oneOfPrototypes(oppiaineenSuoritusProto).sort((a, b) => sortValue(a) - sortValue(b))[0]

  let oppiaineenSuoritusModel = contextualizeSubModel(oppiaineenSuoritusProto, wrappedOsasuoritukset, newItemIndex)
  oppiaineenSuoritusModel = addContext(oppiaineenSuoritusModel, { editAll: true })

  let paikallisetOppiaineet = getOrganizationalPreferences(organisaatioOid, 'perusopetuksenoppiaineet').startWith([])

  var koulutusmoduuli = modelLookup(oppiaineenSuoritusModel, 'koulutusmoduuli')

  var oneOfProtos = oneOfPrototypes(koulutusmoduuli)

  let paikallinenOppiainePrototype = oneOfProtos.find(isPaikallinen)

  let oppiaineModels = oneOfProtos
    .filter(R.complement(isPaikallinen))
    .map(oppiaineModel => modelSetData(oppiaineModel, pakollinen, 'pakollinen'))

  let valtakunnallisetOppiaineet = completeWithFieldAlternatives(oppiaineModels, 'tunniste')

  let oppiaineet = Bacon.combineWith(paikallisetOppiaineet, valtakunnallisetOppiaineet, (x,y) => x.concat(y))

  return (<tbody className={'uusi-oppiaine ' + pakollisuus}>
  <tr>
    <td>
      {
        <DropDown
          options={oppiaineet}
          keyValue={oppiaine => isUusi(oppiaine) ? 'uusi' : modelData(oppiaine, 'tunniste').koodiarvo}
          displayValue={oppiaine => isUusi(oppiaine) ? 'Lisää...' : modelLookup(oppiaine, 'tunniste').value.title}
          onSelectionChanged={oppiaine => {
            resultCallback(modelSet(oppiaineenSuoritusModel, oppiaine, 'koulutusmoduuli'))
          }}
          selectionText={`Lisää ${pakollisuus} oppiaine`}
          newItem={!pakollinen && paikallinenOppiainePrototype}
          enableFilter={true}
        />
      }
    </td>
  </tr>
  </tbody>)
}

let isUusi = (oppiaine) => {
  return !modelData(oppiaine, 'tunniste').koodiarvo
}

const completeWithFieldAlternatives = (models, path) => {
  const alternativesForField = (model) => EnumEditor.fetchAlternatives(modelLookup(model, path))
    .map(alternatives => alternatives.map(enumValue => modelSetValue(model, enumValue, path)))
  return Bacon.combineAsArray(models.map(alternativesForField)).last().map(x => x.flatten()).startWith([])
}


let isPaikallinen = (m) => m.value.classes.includes('paikallinenkoulutusmoduuli')