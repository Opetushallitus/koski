import React from 'react'
import {modelData, modelLookup, modelTitle, modelItems} from './EditorModel.js'
import {Editor} from './Editor.jsx'
import {PropertiesEditor} from './PropertiesEditor.jsx'
import R from 'ramda'
import * as L from 'partial.lenses'
import {
  modelLookupRequired,
  lensedModel,
  modelLens,
  modelSetValue,
  createOptionalEmpty,
  modelErrors
} from './EditorModel'

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
  let showLaajuus = !!suoritukset.find(s => modelData(s, 'koulutusmoduuli.laajuus')) || model.context.edit && !!suoritukset.find(s => modelData(s, 'koulutusmoduuli.pakollinen') === false)
  let showFootnotes = !model.context.edit && !!suoritukset.find(s => modelData(s, 'yksilöllistettyOppimäärä') ||modelData(s, 'painotettuOpetus') || modelData(s, 'korotus'))
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
    let sanallinenArviointi = modelTitle(model, 'arviointi.-1.kuvaus')
    let kielenOppiaine = modelLookupRequired(model, 'koulutusmoduuli').value.classes.includes('peruskoulunvierastaitoinenkotimainenkieli')
    let äidinkieli = modelLookupRequired(model, 'koulutusmoduuli').value.classes.includes('peruskoulunaidinkielijakirjallisuus')
    let editing = model.context.edit
    let tila = modelData(model, 'tila.koodiarvo')
    let extraPropertiesFilter = p => !['koulutusmoduuli', 'arviointi'].includes(p.key)
    let showExpand = sanallinenArviointi || editing && model.value.properties.some(extraPropertiesFilter)
    let toggleExpand = () => { this.setState({expanded : !expanded}) }
    let errors = modelErrors(model)

    let oppiaineTitle = (aine) => {
      let title = kielenOppiaine || äidinkieli ? modelTitle(aine, 'tunniste') + ', ' : modelTitle(aine)
      return modelData(model, 'koulutusmoduuli.pakollinen') === false ? 'Valinnainen ' + title.toLowerCase() : title
    }

    let className = 'oppiaine' + (' ' + tila.toLowerCase()) + (expanded ? ' expanded' : '')

    return (<tbody className={className}>
    <tr>
      <td className="oppiaine">
        { showExpand && <a className={ sanallinenArviointi || editing ? 'toggle-expand' : 'toggle-expand disabled'} onClick={toggleExpand}>{ expanded ? '' : ''}</a> }
        {
          showExpand ? <a className="nimi" onClick={toggleExpand}>{oppiaineTitle(oppiaine)}</a> : <span className="nimi">{oppiaineTitle(oppiaine)}</span>
        }
        {
          (kielenOppiaine || äidinkieli) && <span className="value"><Editor model={model} path="koulutusmoduuli.kieli"/></span>
        }
      </td>
      <td className="arvosana">
        <span className="value"><Editor model={ lensedModel(fixTila(model), arvosanaLens) } sortBy={this.sortGrades}/></span>

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
  },
  sortGrades(gradeX, gradeY) {
    let x = gradeX.value
    let y = gradeY.value
    let xAsFloat = parseFloat(x)
    let yAsFloat = parseFloat(y)
    if (isNaN(xAsFloat) && isNaN(yAsFloat)) {
      return (x < y) ? -1 : (x > y) ? 1 : 0
  }
    if (isNaN(xAsFloat)) {
      return 1
    }
    if (isNaN(yAsFloat)) {
      return -1
    }
    return parseFloat(x) - parseFloat(y)
  }
})

const suoritusValmis = (m) => modelData(m, 'tila').koodiarvo === 'VALMIS'
const hasArvosana = (m) => !!modelData(m, 'arviointi.-1.arvosana')

OppiaineEditor.validateModel = (m) => {
  if (suoritusValmis(m) && !hasArvosana(m)) {
    return ['Suoritus valmis, mutta arvosana puuttuu']
  }
}