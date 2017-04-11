import React from 'baret'
import Bacon from 'baconjs'
import {Editor} from './Editor.jsx'
import {PropertiesEditor} from './PropertiesEditor.jsx'
import {EnumEditor} from './EnumEditor.jsx'
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
  pushModel
} from './EditorModel'
import {sortGrades, sortLanguages} from '../sorting'

export const PerusopetuksenOppiaineetEditor = ({model}) => {
  let käyttäytymisenArvioModel = modelLookup(model, 'käyttäytymisenArvio')
  let grouped = R.toPairs(R.groupBy((o => modelData(o).koulutusmoduuli.pakollinen ? 'Pakolliset oppiaineet' : 'Valinnaiset oppiaineet'), modelItems(model, 'osasuoritukset')))
  let osasuoritukset = modelItems(model, 'osasuoritukset')
  let korotus = osasuoritukset.find(s => modelData(s, 'korotus')) ? ['† = perusopetuksen päättötodistuksen arvosanan korotus'] : []
  let yksilöllistetty = osasuoritukset.find(s => modelData(s, 'yksilöllistettyOppimäärä')) ? ['* = yksilöllistetty oppimäärä'] : []
  let painotettu = osasuoritukset.find(s => modelData(s, 'painotettuOpetus')) ? ['** = painotettu opetus'] : []
  let selitteet = korotus.concat(yksilöllistetty).concat(painotettu).join(', ')

  return grouped.length > 0 && (<div className="oppiaineet">
      <h5>Oppiaineiden arvosanat</h5>
      <p>Arvostelu 4-10, S (suoritettu), H (hylätty) tai V (vapautettu)</p>
      {
        grouped.map(([name, suoritukset], i) => (<section key={i}>
            { grouped.length > 1 && <h5>{name}</h5> }
            <Oppiainetaulukko model={model} suoritukset={suoritukset} />
            {
              käyttäytymisenArvioModel && (model.context.edit || modelData(käyttäytymisenArvioModel)) && (i == grouped.length - 1) && (<div className="kayttaytyminen">
                <h5>Käyttäytymisen arviointi</h5>
                {
                  <Editor model={model} path="käyttäytymisenArvio"/>
                }
              </div>)
            }
          </section>
        ))
      }
      {selitteet && <p className="selitteet">{selitteet}</p>}
    </div>)
}

const Oppiainetaulukko = ({suoritukset, model}) => {
  let valinnaiset = !!suoritukset.find(s => modelData(s, 'koulutusmoduuli.pakollinen') === false)
  let showLaajuus = !!suoritukset.find(s => modelData(s, 'koulutusmoduuli.laajuus')) || model.context.edit && valinnaiset
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
        model.context.edit && <NewOppiaine oppiaineet={modelLookup(model, 'osasuoritukset')} pakollinen={!valinnaiset} resultCallback={addOppiaine} />
      }
    </table>
  )
}

let arvosanaLens = modelLens('arviointi.-1.arvosana')
let tilaLens = modelLens('tila')

let fixTila = (model) => {
  return lensedModel(model, L.rewrite(m => {
    let t = L.get(tilaLens, m)
    if (hasArvosana(m) && !suoritusValmis(m)) {
      t = modelSetValue(t, { data: { koodiarvo: 'VALMIS', koodistoUri: 'suorituksentila' }, title: 'Suoritus valmis' })
      return L.set(tilaLens, t, m)
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

const suoritusValmis = (m) => modelData(m, 'tila').koodiarvo === 'VALMIS'
const hasArvosana = (m) => !!modelData(m, 'arviointi.-1.arvosana')

OppiaineEditor.validateModel = (m) => {
  if (suoritusValmis(m) && !hasArvosana(m)) {
    return ['Suoritus valmis, mutta arvosana puuttuu']
  }
}

const NewOppiaine = ({oppiaineet, pakollinen, resultCallback}) => {
  let selectionBus = Bacon.Bus()

  let pakollisuus = pakollinen ? 'pakollinen' : 'valinnainen'
  let newItemIndex = modelItems(oppiaineet).length
  let oppiaineenSuoritusProto = contextualizeSubModel(oppiaineet.arrayPrototype, oppiaineet, newItemIndex).oneOfPrototypes.find(p => p.key === 'perusopetuksenoppiaineensuoritus')
  let oppiaineenSuoritusModel = contextualizeSubModel(oppiaineenSuoritusProto, oppiaineet, newItemIndex)
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
