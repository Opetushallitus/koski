import React from 'react'
import {modelData, modelItems} from './EditorModel.js'
import {PropertyEditor} from './PropertyEditor.jsx'
import {TogglableEditor} from './TogglableEditor.jsx'
import {PropertiesEditor} from './PropertiesEditor.jsx'
import * as Lukio from './Lukio.jsx'
import {Suoritustaulukko} from './Suoritustaulukko.jsx'
import {LuvaEditor} from './LuvaEditor.jsx'
import * as Perusopetus from './Perusopetus.jsx'

export const SuoritusEditor = React.createClass({
  render() {
    let {model} = this.props
    let excludedProperties = ['osasuoritukset', 'käyttäytymisenArvio', 'tila', 'vahvistus', 'jääLuokalle', 'pakollinen']

    let resolveEditor = (mdl) => {
      if (['perusopetuksenvuosiluokansuoritus', 'perusopetuksenoppimaaransuoritus', 'perusopetuksenlisaopetuksensuoritus', 'perusopetukseenvalmistavanopetuksensuoritus'].includes(mdl.value.classes[0])) {
        return <Perusopetus.PerusopetuksenOppiaineetEditor model={mdl}/>
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

    return (<TogglableEditor
      model={model}
      renderChild={ (mdl, editLink) => {
        let className = 'suoritus ' + (mdl.context.edit ? 'editing ' : '') + mdl.value.classes.join(' ')
        return (<div className={className}>
          {editLink}
          <TodistusLink suoritus={mdl} />
          <PropertiesEditor
            model={mdl}
            propertyFilter={p => !excludedProperties.includes(p.key)}
          />
          <TilaJaVahvistus model={mdl} />
          <div className="osasuoritukset">{resolveEditor(mdl)}</div>
        </div>)
        }
      }
    />)
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