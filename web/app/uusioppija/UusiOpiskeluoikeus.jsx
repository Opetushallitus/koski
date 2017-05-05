import React from 'baret'
import Bacon from 'baconjs'
import Atom from 'bacon.atom'
import Http from '../http'
import {formatISODate} from '../date.js'
import DateInput from '../DateInput.jsx'
import OrganisaatioPicker from '../OrganisaatioPicker.jsx'
import UusiPerusopetuksenSuoritus from './UusiPerusopetuksenSuoritus.jsx'
import UusiAmmatillisenKoulutuksenSuoritus from './UusiAmmatillisenKoulutuksenSuoritus.jsx'
import KoodistoDropdown from './KoodistoDropdown.jsx'
import UusiPerusopetukseenValmistavanOpetuksenSuoritus from './UusiPerusopetukseenValmistavanOpetuksenSuoritus.jsx'
import UusiPerusopetuksenLisaopetuksenSuoritus from './UusiPerusopetuksenLisaopetuksenSuoritus.jsx'
import {koodiarvoMatch, koodistoValues} from './koodisto'

export default ({opiskeluoikeusAtom}) => {
  const dateAtom = Atom(new Date())
  const oppilaitosAtom = Atom()
  const tyyppiAtom = Atom()
  const tilaAtom = Atom()
  const suoritusAtom = Atom()

  const opiskeluoikeustyypitP = oppilaitosAtom
    .flatMapLatest((oppilaitos) => (oppilaitos ? Http.cachedGet(`/koski/api/oppilaitos/opiskeluoikeustyypit/${oppilaitos.oid}`) : []))
    .toProperty()

  opiskeluoikeustyypitP.onValue(tyypit => tyyppiAtom.set(tyypit[0]))

  const opiskeluoikeudenTilatP = koodistoValues('koskiopiskeluoikeudentila')
  opiskeluoikeudenTilatP.onValue(tilat => tilaAtom.set(tilat.find(koodiarvoMatch('lasna'))))

  const opiskeluoikeusP = Bacon.combineWith(dateAtom, oppilaitosAtom, tyyppiAtom, suoritusAtom, tilaAtom, makeOpiskeluoikeus)
  opiskeluoikeusP.changes().onValue((oo) => opiskeluoikeusAtom.set(oo))

  return (<div>
      <Oppilaitos oppilaitosAtom={oppilaitosAtom} />
      {
        oppilaitosAtom.map(o => !!o).and(<OpiskeluoikeudenTyyppi opiskeluoikeudenTyyppiAtom={tyyppiAtom} opiskeluoikeustyypitP={opiskeluoikeustyypitP} />)
      }
      {
        tyyppiAtom.map('.koodiarvo').map(tyyppi => {
          if (tyyppi == 'perusopetus') return <UusiPerusopetuksenSuoritus suoritusAtom={suoritusAtom} oppilaitosAtom={oppilaitosAtom}/>
          if (tyyppi == 'ammatillinenkoulutus') return <UusiAmmatillisenKoulutuksenSuoritus suoritusAtom={suoritusAtom} oppilaitosAtom={oppilaitosAtom}/>
          if (tyyppi == 'perusopetukseenvalmistavaopetus') return <UusiPerusopetukseenValmistavanOpetuksenSuoritus suoritusAtom={suoritusAtom} oppilaitosAtom={oppilaitosAtom}/>
          if (tyyppi == 'perusopetuksenlisaopetus') return <UusiPerusopetuksenLisaopetuksenSuoritus suoritusAtom={suoritusAtom} oppilaitosAtom={oppilaitosAtom}/>
        })
      }
      <Aloituspäivä dateAtom={dateAtom} />
      <OpiskeluoikeudenTila tilaAtom={tilaAtom} opiskeluoikeudenTilatP={opiskeluoikeudenTilatP} />
  </div>)
}

const Oppilaitos = ({oppilaitosAtom}) => (<label className='oppilaitos'>Oppilaitos
  {
    oppilaitosAtom.map(oppilaitos => (
      <OrganisaatioPicker
        preselectSingleOption={true}
        selectedOrg={{ oid: oppilaitos && oppilaitos.oid, nimi: oppilaitos && oppilaitos.nimi && oppilaitos.nimi.fi }}
        onSelectionChanged={org => oppilaitosAtom.set({oid: org && org.oid, nimi: org && org.nimi})}
        shouldShowOrg={org => !org.organisaatiotyypit.some(t => t === 'TOIMIPISTE')}
        canSelectOrg={(org) => org.organisaatiotyypit.some(t => t === 'OPPILAITOS') }
        clearText="tyhjennä"
      />
    ))
  }
</label>)

const OpiskeluoikeudenTyyppi = ({opiskeluoikeudenTyyppiAtom, opiskeluoikeustyypitP}) => <KoodistoDropdown className="opiskeluoikeudentyyppi" title="Opiskeluoikeus" optionsP={opiskeluoikeustyypitP} atom={opiskeluoikeudenTyyppiAtom}/>

const Aloituspäivä = ({dateAtom}) => {
  return (<label className='aloituspaiva'>Aloituspäivä
    <DateInput value={dateAtom.get()} valueCallback={(value) => dateAtom.set(value)} validityCallback={(valid) => !valid && dateAtom.set(undefined)} />
  </label>)
}

const OpiskeluoikeudenTila = ({tilaAtom, opiskeluoikeudenTilatP}) => {
  return (<KoodistoDropdown
    className="opiskeluoikeudentila"
    title="Opiskeluoikeuden tila"
    optionsP={opiskeluoikeudenTilatP}
    atom={tilaAtom}/>)
}

var makeOpiskeluoikeus = (date, oppilaitos, tyyppi, suoritus, tila) => {
  return date && oppilaitos && tyyppi && suoritus && tila && {
      tyyppi: tyyppi,
      oppilaitos: oppilaitos,
      alkamispäivä: formatISODate(date),
      tila: {
        opiskeluoikeusjaksot: [ { alku: formatISODate(date), tila }]
      },
      suoritukset: [suoritus]
    }
}