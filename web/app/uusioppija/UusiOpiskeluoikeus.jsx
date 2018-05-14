import React from 'baret'
import Bacon from 'baconjs'
import * as R from 'ramda'
import Atom from 'bacon.atom'
import Http from '../util/http'
import {formatISODate} from '../date/date.js'
import DateInput from '../date/DateInput'
import OrganisaatioPicker from '../virkailija/OrganisaatioPicker'
import UusiNuortenPerusopetuksenSuoritus from './UusiNuortenPerusopetuksenSuoritus'
import UusiAmmatillisenKoulutuksenSuoritus from './UusiAmmatillisenKoulutuksenSuoritus'
import KoodistoDropdown from '../koodisto/KoodistoDropdown'
import UusiPerusopetukseenValmistavanOpetuksenSuoritus from './UusiPerusopetukseenValmistavanOpetuksenSuoritus'
import UusiPerusopetuksenLisaopetuksenSuoritus from './UusiPerusopetuksenLisaopetuksenSuoritus'
import {koodiarvoMatch, koodistoValues} from './koodisto'
import {t} from '../i18n/i18n'
import Text from '../i18n/Text'
import {sortLanguages} from '../util/sorting'
import {ift} from '../util/util'
import {esiopetuksenSuoritus} from './esiopetuksenSuoritus.js'
import UusiAikuistenPerusopetuksenSuoritus from './UusiAikuistenPerusopetuksenSuoritus'
import UusiLukionSuoritus from './UusiLukionSuoritus'
import {sallitutRahoituskoodiarvot} from '../lukio/lukio'
import UusiIBSuoritus from './UusiIBSuoritus'

export default ({opiskeluoikeusAtom}) => {
  const dateAtom = Atom(new Date())
  const oppilaitosAtom = Atom()
  const suorituskieliAtom = Atom()
  const tyyppiAtom = Atom()
  const tilaAtom = Atom()
  const suoritusAtom = Atom()
  const rahoitusAtom = Atom()
  tyyppiAtom.changes().onValue(() => {
    suoritusAtom.set(undefined)
    rahoitusAtom.set(undefined)
  })

  const opiskeluoikeustyypitP = oppilaitosAtom
    .flatMapLatest((oppilaitos) => (oppilaitos ? Http.cachedGet(`/koski/api/oppilaitos/opiskeluoikeustyypit/${oppilaitos.oid}`) : []))
    .toProperty()

  opiskeluoikeustyypitP.onValue(tyypit => tyyppiAtom.set(tyypit[0]))

  const suorituskieletP = Http.cachedGet('/koski/api/editor/koodit/kieli').map(sortLanguages).map(values => values.map(v => v.data))
  suorituskieletP.onValue(kielet => suorituskieliAtom.set(kielet[0]))

  const tilatP = koodistoValues('koskiopiskeluoikeudentila/lasna,valmistunut,eronnut,katsotaaneronneeksi,valiaikaisestikeskeytynyt,peruutettu,loma')
  const opiskeluoikeudenTilatP = Bacon.combineAsArray(tilatP, tyyppiAtom.map('.koodiarvo')).map(([tilat,tyyppi]) => tyyppi === 'ammatillinenkoulutus' ? tilat : tilat.filter(tila => tila.koodiarvo !== 'loma'))
  const rahoituksetP = koodistoValues('opintojenrahoitus').map(R.sortBy(R.compose(parseInt, R.prop('koodiarvo'))))
  const hasRahoituksetAvailable = tyyppiAtom.map(koodiarvoMatch('ammatillinenkoulutus', 'lukiokoulutus'))

  opiskeluoikeudenTilatP.onValue(tilat => tilaAtom.set(tilat.find(koodiarvoMatch('lasna'))))

  const opiskeluoikeusP = Bacon.combineWith(dateAtom, oppilaitosAtom, tyyppiAtom, suoritusAtom, tilaAtom, rahoitusAtom, makeOpiskeluoikeus)
  opiskeluoikeusP.changes().onValue((oo) => opiskeluoikeusAtom.set(oo))

  return (<div>
    <Oppilaitos oppilaitosAtom={oppilaitosAtom} />
    {
      ift(oppilaitosAtom, <OpiskeluoikeudenTyyppi opiskeluoikeudenTyyppiAtom={tyyppiAtom} opiskeluoikeustyypitP={opiskeluoikeustyypitP} />)
    }
    <Suorituskieli suorituskieliAtom={suorituskieliAtom} suorituskieletP={suorituskieletP} />
    {
      tyyppiAtom.map('.koodiarvo').map(tyyppi => {
        if (tyyppi == 'perusopetus') return <UusiNuortenPerusopetuksenSuoritus suoritusAtom={suoritusAtom} oppilaitosAtom={oppilaitosAtom} suorituskieliAtom={suorituskieliAtom} />
        if (tyyppi == 'aikuistenperusopetus') return <UusiAikuistenPerusopetuksenSuoritus suoritusAtom={suoritusAtom} oppilaitosAtom={oppilaitosAtom} suorituskieliAtom={suorituskieliAtom} />
        if (tyyppi == 'esiopetus') esiopetuksenSuoritus(suoritusAtom, oppilaitosAtom, suorituskieliAtom) // No need to show the diaarinumero selector as there is only one choice
        if (tyyppi == 'ammatillinenkoulutus') return <UusiAmmatillisenKoulutuksenSuoritus suoritusAtom={suoritusAtom} oppilaitosAtom={oppilaitosAtom} suorituskieliAtom={suorituskieliAtom} />
        if (tyyppi == 'perusopetukseenvalmistavaopetus') return <UusiPerusopetukseenValmistavanOpetuksenSuoritus suoritusAtom={suoritusAtom} oppilaitosAtom={oppilaitosAtom} suorituskieliAtom={suorituskieliAtom} />
        if (tyyppi == 'perusopetuksenlisaopetus') return <UusiPerusopetuksenLisaopetuksenSuoritus suoritusAtom={suoritusAtom} oppilaitosAtom={oppilaitosAtom} suorituskieliAtom={suorituskieliAtom} />
        if (tyyppi == 'lukiokoulutus') return <UusiLukionSuoritus suoritusAtom={suoritusAtom} oppilaitosAtom={oppilaitosAtom} suorituskieliAtom={suorituskieliAtom} />
        if (tyyppi == 'ibtutkinto') return <UusiIBSuoritus suoritusAtom={suoritusAtom} oppilaitosAtom={oppilaitosAtom} suorituskieliAtom={suorituskieliAtom} />
      })
    }
    <Aloituspäivä dateAtom={dateAtom} />
    <OpiskeluoikeudenTila tilaAtom={tilaAtom} opiskeluoikeudenTilatP={opiskeluoikeudenTilatP} />
    {
      ift(hasRahoituksetAvailable, <OpintojenRahoitus tyyppiAtom={tyyppiAtom} rahoitusAtom={rahoitusAtom} opintojenRahoituksetP={rahoituksetP} />)
    }
  </div>)
}

const Oppilaitos = ({oppilaitosAtom}) => {
  const selectableOrgTypes = ['OPPILAITOS', 'OPPISOPIMUSTOIMIPISTE']
  return (<label className='oppilaitos'><Text name="Oppilaitos"/>
    {
      oppilaitosAtom.map(oppilaitos => (
        <OrganisaatioPicker
          preselectSingleOption={true}
          selectedOrg={{ oid: oppilaitos && oppilaitos.oid, nimi: oppilaitos && oppilaitos.nimi && t(oppilaitos.nimi) }}
          onSelectionChanged={org => oppilaitosAtom.set({oid: org && org.oid, nimi: org && org.nimi})}
          shouldShowOrg={org => !org.organisaatiotyypit.some(tyyppi => tyyppi === 'TOIMIPISTE')}
          canSelectOrg={(org) => org.organisaatiotyypit.some(ot => selectableOrgTypes.includes(ot))}
          clearText="tyhjennä"
          noSelectionText="Valitse..."
        />
      ))
    }
  </label>)
}

const Suorituskieli = ({suorituskieliAtom, suorituskieletP}) => <KoodistoDropdown className="suorituskieli" title="Suorituskieli" selected={suorituskieliAtom} options={suorituskieletP}/>
const OpiskeluoikeudenTyyppi = ({opiskeluoikeudenTyyppiAtom, opiskeluoikeustyypitP}) => (<KoodistoDropdown
  className="opiskeluoikeudentyyppi"
  title="Opiskeluoikeus"
  options={opiskeluoikeustyypitP}
  selected={opiskeluoikeudenTyyppiAtom}
/>)

const Aloituspäivä = ({dateAtom}) => {
  return (<label className='aloituspaiva'><Text name="Aloituspäivä"/>
    <DateInput value={dateAtom.get()} valueCallback={(value) => dateAtom.set(value)} validityCallback={(valid) => !valid && dateAtom.set(undefined)} />
  </label>)
}

const OpiskeluoikeudenTila = ({tilaAtom, opiskeluoikeudenTilatP}) => {
  return (<KoodistoDropdown
    className="opiskeluoikeudentila"
    title="Opiskeluoikeuden tila"
    options={opiskeluoikeudenTilatP}
    selected={tilaAtom}/>)
}

const OpintojenRahoitus = ({tyyppiAtom, rahoitusAtom, opintojenRahoituksetP}) => {
  const options = Bacon.combineWith(tyyppiAtom, opintojenRahoituksetP, (tyyppi, rahoitukset) =>
    koodiarvoMatch('lukiokoulutus')(tyyppi)
      ? rahoitukset.filter(v => sallitutRahoituskoodiarvot.includes(v.koodiarvo))
      : rahoitukset
  )

  return (
    <KoodistoDropdown
      className="opintojenrahoitus"
      title="Opintojen rahoitus"
      options={options}
      selected={rahoitusAtom}
    />
  )
}

var makeOpiskeluoikeus = (date, oppilaitos, tyyppi, suoritus, tila, opintojenRahoitus) => {
  const makeOpiskeluoikeusjakso = () => {
    const opiskeluoikeusjakso = date && tila && {alku: formatISODate(date), tila}
    opiskeluoikeusjakso && opintojenRahoitus
      ? opiskeluoikeusjakso.opintojenRahoitus = opintojenRahoitus
      : opiskeluoikeusjakso

    return opiskeluoikeusjakso
  }

  return date && oppilaitos && tyyppi && suoritus && tila && {
    tyyppi: tyyppi,
    oppilaitos: oppilaitos,
    alkamispäivä: formatISODate(date),
    tila: {
      opiskeluoikeusjaksot: [makeOpiskeluoikeusjakso()]
    },
    suoritukset: [suoritus]
  }
}
