import React from 'baret'
import Bacon from 'baconjs'
import {Editor} from './Editor.jsx'
import {PropertiesEditor} from './PropertiesEditor.jsx'
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
  modelErrors,
  modelItems,
  modelLens,
  modelLookup,
  modelLookupRequired,
  modelSet,
  modelSetData,
  modelSetValue,
  modelTitle,
  pushModel,
  pushRemoval
} from './EditorModel'
import {sortGrades, sortLanguages} from '../sorting'
import {suoritusValmis, hasArvosana, arvosanaLens, setTila} from './Suoritus'

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
    <p>Arvostelu 4-10, S (suoritettu), H (hylätty) tai V (vapautettu)</p>
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
        suoritukset.map((suoritus, i) => (<OppiaineEditor key={i} model={suoritus} showLaajuus={showLaajuus} showFootnotes={showFootnotes}/> ))
      }
      {
        model.context.edit && <NewOppiaine osasuoritukset={modelLookup(model, 'osasuoritukset')} pakollinen={pakolliset} resultCallback={addOppiaine} />
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

export const OppiaineEditor = React.createClass({
  render() {
    let {model, showLaajuus, showFootnotes} = this.props
    let {expanded} = this.state

    let oppiaine = modelLookup(model, 'koulutusmoduuli')
    let tunniste = modelData(oppiaine, 'tunniste')
    let sanallinenArviointi = modelTitle(model, 'arviointi.-1.kuvaus')
    let kielenOppiaine = modelLookupRequired(model, 'koulutusmoduuli').value.classes.includes('peruskoulunvierastaitoinenkotimainenkieli')
    let äidinkieli = modelLookupRequired(model, 'koulutusmoduuli').value.classes.includes('peruskoulunaidinkielijakirjallisuus')
    let editing = model.context.edit
    let tila = modelData(model, 'tila.koodiarvo')
    let extraPropertiesFilter = p => !['koulutusmoduuli', 'arviointi'].includes(p.key)
    let showExpand = sanallinenArviointi || editing && model.value.properties.some(extraPropertiesFilter)
    let toggleExpand = () => { this.setState({expanded : !expanded}) }
    let errors = modelErrors(model)
    let pakollinen = modelData(model, 'koulutusmoduuli.pakollinen')

    let oppiaineTitle = (aine) => {
      let title = modelTitle(aine, 'tunniste') + (kielenOppiaine || äidinkieli ? ', ' : '')
      return pakollinen === false ? 'Valinnainen ' + title.toLowerCase() : title
    }

    let pakollisuus = pakollinen ? 'pakollinen' : 'valinnainen'
    let className = 'oppiaine ' + pakollisuus + ' ' + tunniste.koodiarvo + (' ' + tila.toLowerCase()) + (expanded ? ' expanded' : '')

    return (<tbody className={className}>
    <tr>
      <td className="oppiaine">
        { showExpand && <a className={ sanallinenArviointi || editing ? 'toggle-expand' : 'toggle-expand disabled'} onClick={toggleExpand}>{ expanded ? '' : ''}</a> }
        {
          showExpand ? <a className="nimi" onClick={toggleExpand}>{oppiaineTitle(oppiaine)}</a> : <span className="nimi">{oppiaineTitle(oppiaine)}</span>
        }
        {
          (kielenOppiaine || äidinkieli) && <span className="value"><Editor model={model} path="koulutusmoduuli.kieli" sortBy={kielenOppiaine && sortLanguages}/></span>
        }
      </td>
      <td className="arvosana">
        <span className="value"><Editor model={ lensedModel(fixTila(model), arvosanaLens) } sortBy={sortGrades}/></span>
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
      !!sanallinenArviointi && expanded && <tr key='sanallinen-arviointi'><td className="details"><span className="sanallinen-arviointi">{sanallinenArviointi}</span></td></tr>
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

OppiaineEditor.validateModel = (m) => {
  if (suoritusValmis(m) && !hasArvosana(m)) {
    return ['Suoritus valmis, mutta arvosana puuttuu']
  }
}

const NewOppiaine = ({osasuoritukset, pakollinen, resultCallback}) => {
  let selectionBus = Bacon.Bus()
  let pakollisuus = pakollinen ? 'pakollinen' : 'valinnainen'
  let wrappedOsasuoritukset = wrapOptional({model: osasuoritukset})
  let newItemIndex = modelItems(wrappedOsasuoritukset).length
  let oppiaineenSuoritusProto = contextualizeSubModel(wrappedOsasuoritukset.arrayPrototype, wrappedOsasuoritukset, newItemIndex).oneOfPrototypes.find(p => p.key === 'perusopetuksenoppiaineensuoritus')
  let oppiaineenSuoritusModel = contextualizeSubModel(oppiaineenSuoritusProto, wrappedOsasuoritukset, newItemIndex)
  oppiaineenSuoritusModel = addContext(oppiaineenSuoritusModel, { editAll: true })

  let oppiaineModels = modelLookup(oppiaineenSuoritusModel, 'koulutusmoduuli')
    .oneOfPrototypes.filter(p => p.key !== 'perusopetuksenpaikallinenvalinnainenoppiaine')
    .map(proto => contextualizeSubModel(proto, oppiaineenSuoritusModel, 'koulutusmoduuli'))
    .map(oppiaineModel => modelSetData(oppiaineModel, pakollinen, 'pakollinen'))

  let emptyAlternatives = alts => !alts.some(a => a === undefined || a.length === 0)

  selectionBus.onValue(resultCallback)

  return (<tbody className={'uusi-oppiaine ' + pakollisuus}>
  <tr>
    <td>
      {
        <DropDown baret-lift
          options={Bacon.combineAsArray(oppiaineModels.map(oppiaineAlternativesP)).filter(emptyAlternatives).map(x => x.flatten())}
          keyValue={([,tunniste]) => tunniste.value}
          displayValue={([,tunniste]) => tunniste.title}
          onSelectionChanged={([oppiaineModel, tunniste]) => {
            oppiaineModel = modelSetValue(oppiaineModel, tunniste, 'tunniste')
            selectionBus.push(modelSet(oppiaineenSuoritusModel, oppiaineModel, 'koulutusmoduuli'))
          }}
          selectionText={`Lisää ${pakollisuus} oppiaine`}
        />
      }
    </td>
  </tr>
  </tbody>)
}

// oppiaineModel -> Prop [(oppiainemodel, tunniste)]
const oppiaineAlternativesP = oppiaineModel => {
  let tunniste = modelLookup(oppiaineModel, 'tunniste')
  return EnumEditor.fetchAlternatives(tunniste).map(alt => alt.map(a => [oppiaineModel, a]))
}
