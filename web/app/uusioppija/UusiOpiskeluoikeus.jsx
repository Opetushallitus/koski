import React, {fromBacon} from 'baret'
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
import UusiVapaanSivistystyonSuoritus from './UusiVapaanSivistystyonSuoritus'
import UusiLukioonValmistavanKoulutuksenSuoritus from './UusiLukioonValmistavanKoulutuksenSuoritus'
import {koodiarvoMatch, koodistoValues} from './koodisto'
import {t} from '../i18n/i18n'
import Text from '../i18n/Text'
import {sortLanguages} from '../util/sorting'
import {ift} from '../util/util'
import UusiEsiopetuksenSuoritus from './UusiEsiopetuksenSuoritus.jsx'
import UusiAikuistenPerusopetuksenSuoritus from './UusiAikuistenPerusopetuksenSuoritus'
import UusiLukionSuoritus from './UusiLukionSuoritus'
import {sallitutRahoituskoodiarvot} from '../lukio/lukio'
import {tuvaSallitutRahoituskoodiarvot} from '../tuva/tuva'
import UusiIBSuoritus from './UusiIBSuoritus'
import UusiDIASuoritus from './UusiDIASuoritus'
import {VARHAISKASVATUKSEN_TOIMIPAIKKA} from './esiopetuksenSuoritus'
import UusiInternationalSchoolSuoritus from './UusiInternationalSchoolSuoritus'
import {filterTilatByOpiskeluoikeudenJaSuorituksenTyyppi} from '../opiskeluoikeus/opiskeluoikeus'
import {userP} from '../util/user'
import Checkbox from '../components/Checkbox'
import {autoFillRahoitusmuoto, opiskeluoikeudenTilaVaatiiRahoitusmuodon, defaultRahoitusmuotoP} from '../opiskeluoikeus/opintojenRahoitus'
import RadioButtons from '../components/RadioButtons'
import {checkAlkamispäivä, checkSuoritus, maksuttomuusOptions} from '../opiskeluoikeus/Maksuttomuus'
import {UusiTutkintokoulutukseenValmentavanKoulutuksenSuoritus, getTuvaLisätiedot} from './UusiTutkintokoulutukseenValmentavanKoulutuksenSuoritus'

export default ({opiskeluoikeusAtom}) => {
  const dateAtom = Atom(new Date())
  const oppilaitosAtom = Atom()
  const organisaatiotyypitAtom = Atom()
  const suorituskieliAtom = Atom()
  const tyyppiAtom = Atom()
  const tilaAtom = Atom()
  const suoritusAtom = Atom()
  const rahoitusAtom = Atom()
  const varhaiskasvatusOrganisaationUlkopuoleltaAtom = Atom(false)
  const varhaiskasvatusJärjestämismuotoAtom = Atom()
  const onTuvaOpiskeluoikeus = Atom(false)
  const tuvaJärjestämislupaAtom = Atom()
  const maksuttomuusAtom = Atom()
  tyyppiAtom.changes().onValue((tyyppi) => {
    suoritusAtom.set(undefined)
    rahoitusAtom.set(undefined)
    onTuvaOpiskeluoikeus.set(tyyppi ? tyyppi.koodiarvo === 'tuva' : false)
  })

  const opintokokonaisuusAtom = Atom()
  const suoritustyyppiAtom = Atom()

  const opiskeluoikeustyypitP = oppilaitosAtom
    .flatMapLatest((oppilaitos) => (oppilaitos ? Http.cachedGet(`/koski/api/oppilaitos/opiskeluoikeustyypit/${oppilaitos.oid}`) : []))
    .toProperty()

  opiskeluoikeustyypitP.onValue(tyypit => tyyppiAtom.set(tyypit[0]))

  const suorituskieletP = Http.cachedGet('/koski/api/editor/koodit/kieli').map(sortLanguages).map(values => values.map(v => v.data))
  suorituskieletP.onValue(kielet => suorituskieliAtom.set(kielet[0]))
  const rahoituksetP = koodistoValues('opintojenrahoitus').map(R.sortBy(R.compose(parseInt, R.prop('koodiarvo'))))
  const opiskeluoikeudenTilatP = opiskeluoikeudentTilat(tyyppiAtom, suoritusAtom, tuvaJärjestämislupaAtom)
  opiskeluoikeudenTilatP.onValue(tilat => {
    // ei aseteta tilaAtomia takaisin 'lasna'-tilaan jos tila on asetettu ja se löytyy mahdollisista opiskeluoikeuden tiloista
    if (!tilaAtom.get() || !tilat.includes(tilaAtom.get())) {
      tilaAtom.set(tilat.find(koodiarvoMatch('lasna')))
    }
  })

  const maksuttomuusTiedonVoiValitaP = Bacon.combineWith(dateAtom.map(checkAlkamispäivä), suoritusAtom.flatMap(checkSuoritus), R.and)

  const rahoitusmuotoChanges = Bacon.combineWith(tyyppiAtom, rahoitusAtom, tilaAtom, defaultRahoitusmuotoP, (ooTyyppi, rahoitus, tila, defaultRahoitus) => ({
    vaatiiRahoituksen: opiskeluoikeudenTilaVaatiiRahoitusmuodon(ooTyyppi?.koodiarvo, tila?.koodiarvo),
    rahoitusValittu: rahoitus,
    setDefaultRahoitus: () => rahoitusAtom.set(defaultRahoitus.data),
    setRahoitusNone: () => rahoitusAtom.set(undefined)
  }))

  rahoitusmuotoChanges.onValue(autoFillRahoitusmuoto)

  const tuvaJärjestämislupaP = koodistoValues('tuvajarjestamislupa')

  const opiskeluoikeusP = Bacon.combineWith(
    dateAtom,
    oppilaitosAtom,
    tyyppiAtom,
    suoritusAtom,
    tilaAtom,
    rahoitusAtom,
    varhaiskasvatusOrganisaationUlkopuoleltaAtom,
    varhaiskasvatusJärjestämismuotoAtom,
    maksuttomuusAtom,
    maksuttomuusTiedonVoiValitaP,
    onTuvaOpiskeluoikeus,
    tuvaJärjestämislupaAtom,
    suoritustyyppiAtom,
    opintokokonaisuusAtom,
    makeOpiskeluoikeus
  )

  opiskeluoikeusP.changes().onValue((oo) => opiskeluoikeusAtom.set(oo))

  return (<div>
    <VarhaiskasvatuksenJärjestämismuotoPicker varhaiskasvatusAtom={varhaiskasvatusOrganisaationUlkopuoleltaAtom} järjestämismuotoAtom={varhaiskasvatusJärjestämismuotoAtom} />
    <Oppilaitos showVarhaiskasvatusToimipisteetP={varhaiskasvatusOrganisaationUlkopuoleltaAtom} oppilaitosAtom={oppilaitosAtom} organisaatiotyypitAtom={organisaatiotyypitAtom} />
    {
      ift(oppilaitosAtom, <OpiskeluoikeudenTyyppi opiskeluoikeudenTyyppiAtom={tyyppiAtom} opiskeluoikeustyypitP={opiskeluoikeustyypitP} />)
    }
    {
      ift(tyyppiAtom.map(tyyppi => tyyppi && tyyppi.koodiarvo !== 'internationalschool'), <Suorituskieli suorituskieliAtom={suorituskieliAtom} suorituskieletP={suorituskieletP} />)
    }
    {
      ift(tyyppiAtom.map(tyyppi => tyyppi && tyyppi.koodiarvo === 'tuva'), <TuvaJärjestämisLupa tuvaJärjestämislupaAtom={tuvaJärjestämislupaAtom} tuvaJärjestämislupaP={tuvaJärjestämislupaP} />)
    }
    {
      tyyppiAtom.map('.koodiarvo').map(tyyppi => {
        if (tyyppi === 'perusopetus') return <UusiNuortenPerusopetuksenSuoritus suoritusAtom={suoritusAtom} oppilaitosAtom={oppilaitosAtom} suorituskieliAtom={suorituskieliAtom} />
        if (tyyppi === 'aikuistenperusopetus') return <UusiAikuistenPerusopetuksenSuoritus suoritusAtom={suoritusAtom} oppilaitosAtom={oppilaitosAtom} suorituskieliAtom={suorituskieliAtom} />
        if (tyyppi === 'esiopetus') return <UusiEsiopetuksenSuoritus suoritusAtom={suoritusAtom} oppilaitosAtom={oppilaitosAtom} organisaatiotyypitAtom={organisaatiotyypitAtom} suorituskieliAtom={suorituskieliAtom} />
        if (tyyppi === 'ammatillinenkoulutus') return <UusiAmmatillisenKoulutuksenSuoritus suoritusAtom={suoritusAtom} oppilaitosAtom={oppilaitosAtom} suorituskieliAtom={suorituskieliAtom} />
        if (tyyppi === 'perusopetukseenvalmistavaopetus') return <UusiPerusopetukseenValmistavanOpetuksenSuoritus suoritusAtom={suoritusAtom} oppilaitosAtom={oppilaitosAtom} suorituskieliAtom={suorituskieliAtom} />
        if (tyyppi === 'perusopetuksenlisaopetus') return <UusiPerusopetuksenLisaopetuksenSuoritus suoritusAtom={suoritusAtom} oppilaitosAtom={oppilaitosAtom} suorituskieliAtom={suorituskieliAtom} />
        if (tyyppi === 'lukiokoulutus') return <UusiLukionSuoritus suoritusAtom={suoritusAtom} oppilaitosAtom={oppilaitosAtom} suorituskieliAtom={suorituskieliAtom} />
        if (tyyppi === 'ibtutkinto') return <UusiIBSuoritus suoritusAtom={suoritusAtom} oppilaitosAtom={oppilaitosAtom} suorituskieliAtom={suorituskieliAtom} />
        if (tyyppi === 'diatutkinto') return <UusiDIASuoritus suoritusAtom={suoritusAtom} oppilaitosAtom={oppilaitosAtom} suorituskieliAtom={suorituskieliAtom} />
        if (tyyppi === 'internationalschool') return <UusiInternationalSchoolSuoritus suoritusAtom={suoritusAtom} dateAtom={dateAtom} oppilaitosAtom={oppilaitosAtom} suorituskieliAtom={suorituskieliAtom} />
        if (tyyppi === 'vapaansivistystyonkoulutus') return <UusiVapaanSivistystyonSuoritus suoritusAtom={suoritusAtom} opintokokonaisuusAtom={opintokokonaisuusAtom} suoritustyyppiAtom={suoritustyyppiAtom} oppilaitosAtom={oppilaitosAtom} suorituskieliAtom={suorituskieliAtom}/>
        if (tyyppi === 'luva') return <UusiLukioonValmistavanKoulutuksenSuoritus suoritusAtom={suoritusAtom} oppilaitosAtom={oppilaitosAtom} suorituskieliAtom={suorituskieliAtom}/>
        if (tyyppi === 'tuva') return <UusiTutkintokoulutukseenValmentavanKoulutuksenSuoritus suoritusAtom={suoritusAtom} oppilaitosAtom={oppilaitosAtom} suorituskieliAtom={suorituskieliAtom} />
      })
    }
    <Aloituspäivä dateAtom={dateAtom} />
    <OpiskeluoikeudenTila tilaAtom={tilaAtom} opiskeluoikeudenTilatP={opiskeluoikeudenTilatP} />
    {
      ift(rahoitusmuotoChanges.map(x => x.vaatiiRahoituksen), <OpintojenRahoitus tyyppiAtom={tyyppiAtom} rahoitusAtom={rahoitusAtom} opintojenRahoituksetP={rahoituksetP} />)
    }
    {
      ift(maksuttomuusTiedonVoiValitaP, <MaksuttomuusRadioButtons maksuttomuusAtom={maksuttomuusAtom}/>)
    }
  </div>)
}

const opiskeluoikeudentTilat = (tyyppiAtom, suoritusAtom, tuvaJärjestämislupaAtom) => {
  const tilatP = koodistoValues('koskiopiskeluoikeudentila/lasna,valmistunut,eronnut,katsotaaneronneeksi,valiaikaisestikeskeytynyt,peruutettu,loma,hyvaksytystisuoritettu,keskeytynyt')
  return Bacon.combineAsArray(tyyppiAtom, suoritusAtom, tuvaJärjestämislupaAtom).flatMap(([tyyppi, suoritusTyyppi, järjestämislupa])=> tilatP.map(filterTilatByOpiskeluoikeudenJaSuorituksenTyyppi(tyyppi, järjestämislupa, suoritusTyyppi ? suoritusTyyppi.tyyppi : undefined))).toProperty()
}

const VarhaiskasvatuksenJärjestämismuotoPicker = ({varhaiskasvatusAtom, järjestämismuotoAtom}) => {
  const isKoulutustoimijaP = userP.map('.varhaiskasvatuksenJärjestäjäKoulutustoimijat').map(koulutustoimijat => koulutustoimijat.length > 0)
  return (<React.Fragment>
    {fromBacon(ift(isKoulutustoimijaP, <VarhaiskasvatusCheckbox varhaiskasvatusAtom={varhaiskasvatusAtom}/>))}
    {fromBacon(ift(varhaiskasvatusAtom, <VarhaiskasvatuksenJärjestämismuoto järjestämismuotoAtom={järjestämismuotoAtom} />))}
  </React.Fragment>)
}

const VarhaiskasvatusCheckbox = ({varhaiskasvatusAtom}) => {
  const varhaiskasvatusOnChange = () => varhaiskasvatusAtom.modify(v => !v)
  return (<label className='varhaiskasvatus-checkbox'><Text name='Päiväkodin esiopetus ostopalveluna tai palvelusetelinä'/>
    <Checkbox id='varhaiskasvatus-checkbox' onChange={varhaiskasvatusOnChange} label='Esiopetus ostetaan oman organisaation ulkopuolelta' listStylePosition='inside'/>
  </label>)
}

const VarhaiskasvatuksenJärjestämismuoto = ({järjestämismuotoAtom}) => {
  const järjestysMuotoP = koodistoValues('vardajarjestamismuoto/JM02,JM03')
  return (<label id='varhaiskasvatus-jarjestamismuoto'>
    <KoodistoDropdown
      className='varhaiskasvatus-jarjestamismuoto'
      title='Varhaiskasvatuksen järjestämismuoto'
      options={järjestysMuotoP}
      selected={järjestämismuotoAtom}/>
  </label>)
}

const Oppilaitos = ({showVarhaiskasvatusToimipisteetP, oppilaitosAtom, organisaatiotyypitAtom}) => {
  const selectableOrgTypes = ['OPPILAITOS', 'OPPISOPIMUSTOIMIPISTE', VARHAISKASVATUKSEN_TOIMIPAIKKA]
  return (<label className='oppilaitos'><Text name="Oppilaitos"/>
    {
      Bacon.combineWith(oppilaitosAtom, showVarhaiskasvatusToimipisteetP, (oppilaitos, show) =>
        (<OrganisaatioPicker
            key={'uuden-oppijan-oppilaitos-' + (show ? 'vain-varhaiskasvatus' : 'oma-organisaatio')}
            preselectSingleOption={true}
            selectedOrg={{oid: oppilaitos && oppilaitos.oid, nimi: oppilaitos && oppilaitos.nimi && t(oppilaitos.nimi)}}
            onSelectionChanged={org => {
              oppilaitosAtom.set({oid: org && org.oid, nimi: org && org.nimi})
              organisaatiotyypitAtom.set(org && org.organisaatiotyypit)
            }}
            shouldShowOrg={org => !org.organisaatiotyypit.some(tyyppi => tyyppi === 'TOIMIPISTE')}
            canSelectOrg={(org) => org.organisaatiotyypit.some(ot => selectableOrgTypes.includes(ot))}
            clearText={t('tyhjennä')}
            noSelectionText="Valitse..."
            orgTypesToShow={show ? 'vainVarhaiskasvatusToimipisteet' : 'vainOmatOrganisaatiot'}
          />
        )
      )
    }
  </label>)
}

const Suorituskieli = ({suorituskieliAtom, suorituskieletP}) => <KoodistoDropdown className="suorituskieli" title={t('Suorituskieli')} selected={suorituskieliAtom} options={suorituskieletP}/>
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

  const options = Bacon.combineWith(tyyppiAtom, opintojenRahoituksetP, (tyyppi, rahoitukset) => {
      if (koodiarvoMatch('aikuistenperusopetus', 'lukiokoulutus', 'internationalschool', 'ibtutkinto')(tyyppi)) {
        return rahoitukset.filter(v => sallitutRahoituskoodiarvot.includes(v.koodiarvo))
      } else if (koodiarvoMatch('tuva')(tyyppi)) {
        return rahoitukset.filter(v => tuvaSallitutRahoituskoodiarvot.includes(v.koodiarvo))
      } else {
        return rahoitukset
      }
    }
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

const MaksuttomuusRadioButtons = ({maksuttomuusAtom}) => {
  return (
    <RadioButtons
      options={maksuttomuusOptions}
      selected={maksuttomuusAtom}
      onSelectionChanged={selected => maksuttomuusAtom.set(selected.key)}
      data-test-id="maksuttomuus-radio-buttons"
    />
  )
}

const TuvaJärjestämisLupa = ({tuvaJärjestämislupaAtom, tuvaJärjestämislupaP}) => {
  return (
    <KoodistoDropdown
      className="järjestämislupa"
      title="Järjestämislupa"
      options={tuvaJärjestämislupaP}
      selected={tuvaJärjestämislupaAtom}
    />
  )
}

const makeOpiskeluoikeus = (
  alkamispäivä,
  oppilaitos,
  tyyppi,
  suoritus,
  tila,
  opintojenRahoitus,
  varhaiskasvatusOrganisaationUlkopuolelta,
  varhaiskasvatusJärjestämismuoto,
  maksuttomuus,
  maksuttomuusTiedonVoiValita,
  onTuvaOpiskeluoikeus,
  tuvaJärjestämislupa,
  suoritustyyppi,
  opintokokonaisuus
) => {
  const makeOpiskeluoikeusjakso = () => {
    const opiskeluoikeusjakso = alkamispäivä && tila && {alku: formatISODate(alkamispäivä), tila}
    opiskeluoikeusjakso && opintojenRahoitus
      ? opiskeluoikeusjakso.opintojenRahoitus = opintojenRahoitus
      : opiskeluoikeusjakso

    return opiskeluoikeusjakso
  }

  if (
    alkamispäivä
    && oppilaitos
    && tyyppi
    && suoritus
    && tila
    && (!varhaiskasvatusOrganisaationUlkopuolelta || varhaiskasvatusJärjestämismuoto)
    && (!maksuttomuusTiedonVoiValita || maksuttomuus !== undefined)
    && (!onTuvaOpiskeluoikeus || tuvaJärjestämislupa)
  ) {

    const järjestämismuoto = tyyppi.koodiarvo === 'esiopetus' ? { järjestämismuoto: varhaiskasvatusJärjestämismuoto} : {}
    const maksuttomuusLisätieto = maksuttomuusTiedonVoiValita && maksuttomuus !== 'none'
      ? {lisätiedot: {maksuttomuus: [{alku: formatISODate(alkamispäivä), maksuton: maksuttomuus}]}}
      : {}
    const järjestämislupa = onTuvaOpiskeluoikeus ? { järjestämislupa: tuvaJärjestämislupa ? tuvaJärjestämislupa : {} } : {}
    const tuvaOletusLisätiedot = onTuvaOpiskeluoikeus ? getTuvaLisätiedot(tuvaJärjestämislupa) : {}

    if(opintokokonaisuus && tyyppi.koodiarvo === 'vapaansivistystyonkoulutus' && suoritustyyppi.koodiarvo === 'vstvapaatavoitteinenkoulutus') {
      suoritus.koulutusmoduuli.opintokokonaisuus = opintokokonaisuus
    }

    const opiskeluoikeus =  {
      tyyppi: tyyppi,
      oppilaitos: oppilaitos,
      alkamispäivä: formatISODate(alkamispäivä),
      tila: {
        opiskeluoikeusjaksot: [makeOpiskeluoikeusjakso()]
      },
      suoritukset: [suoritus]
    }
    return R.mergeAll([opiskeluoikeus, järjestämismuoto, R.mergeDeepLeft(maksuttomuusLisätieto, tuvaOletusLisätiedot), järjestämislupa])
  }
}
