import React from 'react'
import {modelData, modelItems, modelLookup} from './EditorModel.js'
import {PropertyEditor} from './PropertyEditor.jsx'
import {PropertiesEditor} from './PropertiesEditor.jsx'
import * as Lukio from './Lukio.jsx'
import {Suoritustaulukko} from './Suoritustaulukko.jsx'
import {LuvaEditor} from './LuvaEditor.jsx'
import {PerusopetuksenOppiaineetEditor} from './PerusopetuksenOppiaineetEditor.jsx'
import {sortLanguages} from '../sorting'
import {Editor} from './Editor.jsx'

export const SuoritusEditor = React.createClass({
  render() {
    let {model} = this.props
    let excludedProperties = ['osasuoritukset', 'käyttäytymisenArvio', 'tila', 'vahvistus', 'jääLuokalle', 'pakollinen']

    let resolveEditor = (mdl) => {
      if (['perusopetuksenvuosiluokansuoritus', 'perusopetuksenoppimaaransuoritus', 'perusopetuksenlisaopetuksensuoritus', 'perusopetukseenvalmistavanopetuksensuoritus'].includes(mdl.value.classes[0])) {
        return <PerusopetuksenOppiaineetEditor model={mdl}/>
      }
      if (model.value.classes.includes('ammatillinenpaatasonsuoritus')) {
        return <Suoritustaulukko suoritukset={modelItems(model, 'osasuoritukset') || []}/>
      }
      if (mdl.value.classes.includes('lukionoppimaaransuoritus')) {
        return <Lukio.LukionOppiaineetEditor oppiaineet={modelItems(mdl, 'osasuoritukset') || []} />
      }
      if (mdl.value.classes.includes('lukionoppiaineenoppimaaransuoritus')) {
        return <Lukio.LukionOppiaineetEditor oppiaineet={[mdl]} />
      }
      if (model.value.classes.includes('lukioonvalmistavankoulutuksensuoritus')) {
        return <LuvaEditor suoritukset={modelItems(model, 'osasuoritukset') || []}/>
      }
      return <PropertyEditor model={mdl} propertyName="osasuoritukset"/>
    }

    let className = 'suoritus ' + model.value.classes.join(' ')

    return (<div className={className}>
      <TodistusLink suoritus={model} />
      <PropertiesEditor
        model={model}
        propertyFilter={p => !excludedProperties.includes(p.key)}
        getValueEditor={ (prop, getDefault) => prop.key === 'suorituskieli'
          ? <Editor model={modelLookup(model, 'suorituskieli')} sortBy={sortLanguages}/>
          : getDefault() }
      />
      <TilaJaVahvistus model={model} />
      <div className="osasuoritukset">{resolveEditor(model)}</div>
    </div>)
  }
})
SuoritusEditor.näytettäväPäätasonSuoritus = s => !['perusopetuksenvuosiluokka', 'korkeakoulunopintojakso'].includes(modelData(s).tyyppi.koodiarvo)

const TilaJaVahvistus = React.createClass({
  render() {
    let { model } = this.props
    return (<div className="tila-vahvistus">
        <span className="tila">
          Suoritus: <span className={ 'VALMIS' == modelData(model).tila.koodiarvo ? 'valmis' : ''}>{ modelData(model).tila.koodiarvo }</span> { /* TODO: i18n */ }
        </span>
        {
          modelData(model).vahvistus && <PropertyEditor model={model} propertyName="vahvistus"/>
        }
        {(() => {
          let jääLuokalle = modelData(model, 'jääLuokalle')
          let luokka = modelData(model, 'koulutusmoduuli.tunniste.koodiarvo')
          if (jääLuokalle === true) {
            return <div>Ei siirretä seuraavalle luokalle</div>
          } else if (jääLuokalle === false && luokka !== '9') {
            return <div>Siirretään seuraavalle luokalle</div>
          }
        })()}
      </div>
    )
  }
})

const TodistusLink = React.createClass({
  render() {
    let {suoritus} = this.props
    let oppijaOid = suoritus.context.oppijaOid
    let suoritustyyppi = modelData(suoritus, 'tyyppi').koodiarvo
    let koulutusmoduuliKoodistoUri = modelData(suoritus, 'koulutusmoduuli').tunniste.koodistoUri
    let koulutusmoduuliKoodiarvo = modelData(suoritus, 'koulutusmoduuli').tunniste.koodiarvo
    let suoritusTila = modelData(suoritus, 'tila').koodiarvo
    let href = '/koski/todistus/' + oppijaOid + '?suoritustyyppi=' + suoritustyyppi + '&koulutusmoduuli=' + koulutusmoduuliKoodistoUri + '/' + koulutusmoduuliKoodiarvo
    return suoritusTila == 'VALMIS' && suoritustyyppi != 'korkeakoulututkinto' && suoritustyyppi != 'preiboppimaara' && suoritustyyppi != 'esiopetuksensuoritus' && !(koulutusmoduuliKoodistoUri == 'perusopetuksenluokkaaste' && koulutusmoduuliKoodiarvo == '9')
      ? <a className="todistus" href={href}>näytä todistus</a>
      : null
  }
})