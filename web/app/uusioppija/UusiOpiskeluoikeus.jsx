import React, { fromBacon } from 'baret'
import Bacon from 'baconjs'
import * as R from 'ramda'
import Atom from 'bacon.atom'
import Http from '../util/http'
import { formatISODate } from '../date/date'
import DateInput from '../date/DateInput'
import OrganisaatioPicker from '../virkailija/OrganisaatioPicker'
import UusiNuortenPerusopetuksenSuoritus from './UusiNuortenPerusopetuksenSuoritus'
import UusiAmmatillisenKoulutuksenSuoritus from './UusiAmmatillisenKoulutuksenSuoritus'
import KoodistoDropdown from '../koodisto/KoodistoDropdown'
import UusiPerusopetukseenValmistavanOpetuksenSuoritus from './UusiPerusopetukseenValmistavanOpetuksenSuoritus'
import UusiPerusopetuksenLisaopetuksenSuoritus from './UusiPerusopetuksenLisaopetuksenSuoritus'
import UusiVapaanSivistystyonSuoritus, {
  opintokokonaisuudellisetVstSuoritustyypit
} from './UusiVapaanSivistystyonSuoritus'
import UusiLukioonValmistavanKoulutuksenSuoritus from './UusiLukioonValmistavanKoulutuksenSuoritus'
import { koodiarvoMatch, koodistoValues } from './koodisto'
import { t } from '../i18n/i18n'
import Text from '../i18n/Text'
import { sortLanguages } from '../util/sorting'
import { ift } from '../util/util'
import UusiEsiopetuksenSuoritus from './UusiEsiopetuksenSuoritus.jsx'
import UusiAikuistenPerusopetuksenSuoritus from './UusiAikuistenPerusopetuksenSuoritus'
import UusiLukionSuoritus from './UusiLukionSuoritus'
import { sallitutRahoituskoodiarvot } from '../lukio/lukio'
import { tuvaSallitutRahoituskoodiarvot } from '../tuva/tuva'
import UusiIBSuoritus from './UusiIBSuoritus'
import UusiDIASuoritus from './UusiDIASuoritus'
import { VARHAISKASVATUKSEN_TOIMIPAIKKA } from './esiopetuksenSuoritus'
import UusiInternationalSchoolSuoritus from './UusiInternationalSchoolSuoritus'
import { filterTilatByOpiskeluoikeudenJaSuorituksenTyyppi } from '../opiskeluoikeus/opiskeluoikeus'
import { userP } from '../util/user'
import Checkbox from '../components/Checkbox'
import {
  autoFillRahoitusmuoto,
  opiskeluoikeudenTilaVaatiiRahoitusmuodon,
  getRahoitusmuoto,
  defaultRahoituskoodi
} from '../opiskeluoikeus/opintojenRahoitus'
import RadioButtons from '../components/RadioButtons'
import {
  checkAlkamispäivä,
  checkSuoritus,
  maksuttomuusOptions
} from '../opiskeluoikeus/Maksuttomuus'
import {
  UusiTutkintokoulutukseenValmentavanKoulutuksenSuoritus,
  getTuvaLisätiedot
} from './UusiTutkintokoulutukseenValmentavanKoulutuksenSuoritus'
import UusiEuropeanSchoolOfHelsinkiSuoritus from './UusiEuropeanSchoolOfHelsinkiSuoritus'
import {
  eiOsasuorituksiaEshLuokkaAsteet,
  eshSallitutRahoituskoodiarvot,
  luokkaAsteenOsasuoritukset
} from '../esh/esh'
import { jotpaSallitutRahoituskoodiarvot } from '../jotpa/jotpa'
import UusiMuunKuinSäännellynKoulutuksenSuoritus from './UusiMuunKuinSäännellynKoulutuksenSuoritus'
import { modelData } from '../editor/EditorModel'
import UusiTaiteenPerusopetuksenSuoritus from './UusiTaiteenPerusopetuksenSuoritus'
import UusiEBTutkinnonSuoritus from './UusiEBTutkinnonSuoritus'

export default ({ opiskeluoikeusAtom }) => {
  const dateAtom = Atom(new Date())
  const oppilaitosAtom = Atom()
  const organisaatiotyypitAtom = Atom()
  const suorituskieliAtom = Atom()
  const tyyppiAtom = Atom()
  const tilaAtom = Atom()
  const suoritusAtom = Atom()
  const rahoitusAtom = Atom()
  const varhaiskasvatusOrganisaationUlkopuoleltaAtom = Atom(false)
  const taiteenPerusopetusOrganisaationUlkopuoleltaAtom = Atom(false)
  const varhaiskasvatusJärjestämismuotoAtom = Atom()
  const onTuvaOpiskeluoikeus = Atom(false)
  const tuvaJärjestämislupaAtom = Atom()
  const onTaiteenPerusopetusOpiskeluoikeusAtom = Atom(false)
  const tpoOppimääräAtom = Atom()
  const tpoToteutustapaAtom = Atom()
  const tpoTaiteenalaAtom = Atom()
  const maksuttomuusAtom = Atom()
  tyyppiAtom.changes().onValue((tyyppi) => {
    suoritusAtom.set(undefined)
    rahoitusAtom.set(undefined)
    onTuvaOpiskeluoikeus.set(tyyppi ? tyyppi.koodiarvo === 'tuva' : false)
    onTaiteenPerusopetusOpiskeluoikeusAtom.set(
      tyyppi ? tyyppi.koodiarvo === 'taiteenperusopetus' : false
    )
  })
  const opintokokonaisuusAtom = Atom()
  const osaamismerkkiAtom = Atom()
  const suoritustyyppiAtom = Atom()

  taiteenPerusopetusOrganisaationUlkopuoleltaAtom
    .changes()
    .onValue((v) => oppilaitosAtom.set(null))
  varhaiskasvatusOrganisaationUlkopuoleltaAtom
    .changes()
    .onValue((v) => oppilaitosAtom.set(null))

  const opiskeluoikeustyypitP = oppilaitosAtom
    .flatMapLatest((oppilaitos) =>
      oppilaitos
        ? Http.cachedGet(
            `/koski/api/oppilaitos/opiskeluoikeustyypit/${oppilaitos.oid}`
          )
        : []
    )
    .toProperty()

  opiskeluoikeustyypitP.onValue((tyypit) => tyyppiAtom.set(tyypit[0]))

  const suorituskieletP = Http.cachedGet('/koski/api/editor/koodit/kieli')
    .map(sortLanguages)
    .map((values) => values.map((v) => v.data))
  suorituskieletP.onValue((kielet) => suorituskieliAtom.set(kielet[0]))
  const rahoituksetP = koodistoValues('opintojenrahoitus').map(
    R.sortBy(R.compose(parseInt, R.prop('koodiarvo')))
  )
  const opiskeluoikeudenTilatP = opiskeluoikeudentTilat(
    tyyppiAtom,
    suoritusAtom,
    tuvaJärjestämislupaAtom
  )
  opiskeluoikeudenTilatP.onValue((tilat) => {
    // ei aseteta tilaAtomia takaisin 'lasna'-tilaan jos tila on asetettu ja se löytyy mahdollisista opiskeluoikeuden tiloista
    if (!tilaAtom.get() || !tilat.includes(tilaAtom.get())) {
      tilaAtom.set(tilat.find(koodiarvoMatch('lasna')))
    }
  })

  const maksuttomuusTiedonVoiValitaP = Bacon.combineWith(
    dateAtom.map(checkAlkamispäivä),
    suoritusAtom.flatMap(checkSuoritus),
    R.and
  )

  const suorituskielenVoiValitaP = Bacon.combineWith(
    tyyppiAtom.map(
      (tyyppi) =>
        tyyppi &&
        tyyppi.koodiarvo !== 'internationalschool' &&
        tyyppi.koodiarvo !== 'europeanschoolofhelsinki' &&
        tyyppi.koodiarvo !== 'taiteenperusopetus' &&
        tyyppi.koodiarvo !== 'ebtutkinto'
    ),
    suoritustyyppiAtom.map(
      (tyyppi) => !tyyppi || tyyppi.koodiarvo !== 'vstosaamismerkki'
    ),
    R.and
  )

  const rahoitusmuotoChanges = Bacon.combineWith(
    tyyppiAtom,
    rahoitusAtom,
    tilaAtom,
    suoritustyyppiAtom,
    (ooTyyppi, rahoitus, tila, suoritustyyppi) => ({
      vaatiiRahoituksen: opiskeluoikeudenTilaVaatiiRahoitusmuodon(
        ooTyyppi?.koodiarvo,
        tila?.koodiarvo,
        suoritustyyppi?.koodiarvo
      ),
      rahoitusValittu: rahoitus,
      setDefaultRahoitus: () => {
        getRahoitusmuoto(
          defaultRahoituskoodi(ooTyyppi, suoritustyyppi) // TODO tuonne menee nyt objekti eikä string
        ).onValue((defaultRahoitus) => rahoitusAtom.set(defaultRahoitus.data))
      },
      setRahoitusNone: () => rahoitusAtom.set(undefined)
    })
  )

  rahoitusmuotoChanges.onValue(autoFillRahoitusmuoto)

  const tuvaJärjestämislupaP = koodistoValues('tuvajarjestamislupa')

  const suoritusP = suoritusAtom.flatMap((s) => {
    if (
      s?.koulutusmoduuli?.tunniste?.koodistoUri ===
        'europeanschoolofhelsinkiluokkaaste' &&
      !eiOsasuorituksiaEshLuokkaAsteet.includes(
        s.koulutusmoduuli.tunniste.koodiarvo
      )
    ) {
      return luokkaAsteenOsasuoritukset(
        s.koulutusmoduuli.tunniste.koodiarvo
      ).map((osasuorituksetPrefillattuEditorModel) => {
        return {
          ...s,
          osasuoritukset: modelData(osasuorituksetPrefillattuEditorModel)
        }
      })
    }
    return s
  })

  const opiskeluoikeusP = Bacon.combineWith(
    dateAtom,
    oppilaitosAtom,
    tyyppiAtom,
    suoritusP,
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
    osaamismerkkiAtom,
    onTaiteenPerusopetusOpiskeluoikeusAtom,
    tpoOppimääräAtom,
    tpoToteutustapaAtom,
    tpoTaiteenalaAtom,
    makeOpiskeluoikeus
  )

  opiskeluoikeusP.changes().onValue((oo) => opiskeluoikeusAtom.set(oo))

  return (
    <div>
      <OmanOrganisaationUlkopuolinenOppilaitosPicker
        varhaiskasvatusAtom={varhaiskasvatusOrganisaationUlkopuoleltaAtom}
        taiteenPerusopetusAtom={taiteenPerusopetusOrganisaationUlkopuoleltaAtom}
        järjestämismuotoAtom={varhaiskasvatusJärjestämismuotoAtom}
      />
      <Oppilaitos
        showVarhaiskasvatusToimipisteetP={
          varhaiskasvatusOrganisaationUlkopuoleltaAtom
        }
        showKaikkiOppilaitoksetP={
          taiteenPerusopetusOrganisaationUlkopuoleltaAtom
        }
        oppilaitosAtom={oppilaitosAtom}
        organisaatiotyypitAtom={organisaatiotyypitAtom}
      />
      {ift(
        oppilaitosAtom,
        <OpiskeluoikeudenTyyppi
          opiskeluoikeudenTyyppiAtom={tyyppiAtom}
          opiskeluoikeustyypitP={opiskeluoikeustyypitP}
        />
      )}
      {ift(
        suorituskielenVoiValitaP,
        <Suorituskieli
          suorituskieliAtom={suorituskieliAtom}
          suorituskieletP={suorituskieletP}
        />
      )}
      {ift(
        tyyppiAtom.map((tyyppi) => tyyppi && tyyppi.koodiarvo === 'tuva'),
        <TuvaJärjestämisLupa
          tuvaJärjestämislupaAtom={tuvaJärjestämislupaAtom}
          tuvaJärjestämislupaP={tuvaJärjestämislupaP}
        />
      )}
      {ift(
        tyyppiAtom.map(
          (tyyppi) => tyyppi && tyyppi.koodiarvo === 'taiteenperusopetus'
        ),
        <TaiteenPerusopetuksenOppimäärä tpoOppimääräAtom={tpoOppimääräAtom} />
      )}
      {ift(
        tyyppiAtom.map(
          (tyyppi) => tyyppi && tyyppi.koodiarvo === 'taiteenperusopetus'
        ),
        <TaiteenPerusopetuksenKoulutuksenToteutustapa
          tpoToteutustapaAtom={tpoToteutustapaAtom}
          hankintakoulutusAtom={taiteenPerusopetusOrganisaationUlkopuoleltaAtom}
        />
      )}
      {tyyppiAtom.map('.koodiarvo').map((tyyppi) => {
        if (tyyppi === 'perusopetus')
          return (
            <UusiNuortenPerusopetuksenSuoritus
              suoritusAtom={suoritusAtom}
              oppilaitosAtom={oppilaitosAtom}
              suorituskieliAtom={suorituskieliAtom}
            />
          )
        if (tyyppi === 'aikuistenperusopetus')
          return (
            <UusiAikuistenPerusopetuksenSuoritus
              suoritusAtom={suoritusAtom}
              oppilaitosAtom={oppilaitosAtom}
              suorituskieliAtom={suorituskieliAtom}
            />
          )
        if (tyyppi === 'esiopetus')
          return (
            <UusiEsiopetuksenSuoritus
              suoritusAtom={suoritusAtom}
              oppilaitosAtom={oppilaitosAtom}
              organisaatiotyypitAtom={organisaatiotyypitAtom}
              suorituskieliAtom={suorituskieliAtom}
            />
          )
        if (tyyppi === 'ammatillinenkoulutus')
          return (
            <UusiAmmatillisenKoulutuksenSuoritus
              suoritusAtom={suoritusAtom}
              oppilaitosAtom={oppilaitosAtom}
              suorituskieliAtom={suorituskieliAtom}
            />
          )
        if (tyyppi === 'perusopetukseenvalmistavaopetus')
          return (
            <UusiPerusopetukseenValmistavanOpetuksenSuoritus
              suoritusAtom={suoritusAtom}
              oppilaitosAtom={oppilaitosAtom}
              suorituskieliAtom={suorituskieliAtom}
            />
          )
        if (tyyppi === 'perusopetuksenlisaopetus')
          return (
            <UusiPerusopetuksenLisaopetuksenSuoritus
              suoritusAtom={suoritusAtom}
              oppilaitosAtom={oppilaitosAtom}
              suorituskieliAtom={suorituskieliAtom}
            />
          )
        if (tyyppi === 'lukiokoulutus')
          return (
            <UusiLukionSuoritus
              suoritusAtom={suoritusAtom}
              oppilaitosAtom={oppilaitosAtom}
              suorituskieliAtom={suorituskieliAtom}
            />
          )
        if (tyyppi === 'ibtutkinto')
          return (
            <UusiIBSuoritus
              suoritusAtom={suoritusAtom}
              oppilaitosAtom={oppilaitosAtom}
              suorituskieliAtom={suorituskieliAtom}
            />
          )
        if (tyyppi === 'diatutkinto')
          return (
            <UusiDIASuoritus
              suoritusAtom={suoritusAtom}
              oppilaitosAtom={oppilaitosAtom}
              suorituskieliAtom={suorituskieliAtom}
            />
          )
        if (tyyppi === 'internationalschool')
          return (
            <UusiInternationalSchoolSuoritus
              suoritusAtom={suoritusAtom}
              dateAtom={dateAtom}
              oppilaitosAtom={oppilaitosAtom}
              suorituskieliAtom={suorituskieliAtom}
            />
          )
        if (tyyppi === 'europeanschoolofhelsinki')
          return (
            <UusiEuropeanSchoolOfHelsinkiSuoritus
              suoritusAtom={suoritusAtom}
              oppilaitosAtom={oppilaitosAtom}
              dateAtom={dateAtom}
              näytäKoulutusValitsin={false}
              näytäAlkamispäiväValitsin={false}
            />
          )
        if (tyyppi === 'ebtutkinto')
          return (
            <UusiEBTutkinnonSuoritus
              suoritusAtom={suoritusAtom}
              dateAtom={dateAtom}
              oppilaitosAtom={oppilaitosAtom}
            />
          )
        if (tyyppi === 'vapaansivistystyonkoulutus')
          return (
            <UusiVapaanSivistystyonSuoritus
              suoritusAtom={suoritusAtom}
              opintokokonaisuusAtom={opintokokonaisuusAtom}
              suoritustyyppiAtom={suoritustyyppiAtom}
              oppilaitosAtom={oppilaitosAtom}
              suorituskieliAtom={suorituskieliAtom}
              osaamismerkkiAtom={osaamismerkkiAtom}
            />
          )
        if (tyyppi === 'luva')
          return (
            <UusiLukioonValmistavanKoulutuksenSuoritus
              suoritusAtom={suoritusAtom}
              oppilaitosAtom={oppilaitosAtom}
              suorituskieliAtom={suorituskieliAtom}
            />
          )
        if (tyyppi === 'tuva')
          return (
            <UusiTutkintokoulutukseenValmentavanKoulutuksenSuoritus
              suoritusAtom={suoritusAtom}
              oppilaitosAtom={oppilaitosAtom}
              suorituskieliAtom={suorituskieliAtom}
            />
          )
        if (tyyppi === 'muukuinsaanneltykoulutus')
          return (
            <UusiMuunKuinSäännellynKoulutuksenSuoritus
              suoritusAtom={suoritusAtom}
              oppilaitosAtom={oppilaitosAtom}
              suorituskieliAtom={suorituskieliAtom}
              opintokokonaisuusAtom={opintokokonaisuusAtom}
            />
          )
        if (tyyppi === 'taiteenperusopetus')
          return (
            <UusiTaiteenPerusopetuksenSuoritus
              suoritusAtom={suoritusAtom}
              oppilaitosAtom={oppilaitosAtom}
              oppimääräAtom={tpoOppimääräAtom}
              taiteenalaAtom={tpoTaiteenalaAtom}
            />
          )
      })}
      <Aloituspäivä dateAtom={dateAtom} />
      <OpiskeluoikeudenTila
        tilaAtom={tilaAtom}
        opiskeluoikeudenTilatP={opiskeluoikeudenTilatP}
      />
      {ift(
        rahoitusmuotoChanges.map((x) => x.vaatiiRahoituksen),
        <OpintojenRahoitus
          tyyppiAtom={tyyppiAtom}
          rahoitusAtom={rahoitusAtom}
          opintojenRahoituksetP={rahoituksetP}
          suoritystyyppiP={suoritustyyppiAtom}
        />
      )}
      {ift(
        maksuttomuusTiedonVoiValitaP,
        <MaksuttomuusRadioButtons maksuttomuusAtom={maksuttomuusAtom} />
      )}
    </div>
  )
}

const opiskeluoikeudentTilat = (
  tyyppiAtom,
  suoritusAtom,
  tuvaJärjestämislupaAtom
) => {
  const tilatP = koodistoValues(
    'koskiopiskeluoikeudentila/lasna,valmistunut,eronnut,katsotaaneronneeksi,valiaikaisestikeskeytynyt,peruutettu,loma,hyvaksytystisuoritettu,keskeytynyt,paattynyt'
  )
  return Bacon.combineAsArray(tyyppiAtom, suoritusAtom, tuvaJärjestämislupaAtom)
    .flatMap(([tyyppi, suoritusTyyppi, järjestämislupa]) =>
      tilatP.map(
        filterTilatByOpiskeluoikeudenJaSuorituksenTyyppi(
          tyyppi,
          järjestämislupa,
          suoritusTyyppi ? suoritusTyyppi.tyyppi : undefined
        )
      )
    )
    .toProperty()
}

const OmanOrganisaationUlkopuolinenOppilaitosPicker = ({
  varhaiskasvatusAtom,
  taiteenPerusopetusAtom,
  järjestämismuotoAtom
}) => {
  const isVarhaiskasvatusKoulutustoimijaP = userP
    .map('.varhaiskasvatuksenJärjestäjäKoulutustoimijat')
    .map((koulutustoimijat) => koulutustoimijat.length > 0)
  const isTPOKoulutustoimijaP = userP.map('.hasOneKoulutustoimijaWriteAccess')

  const showVarhaiskasvatusCheckbox = Bacon.combineWith(
    isVarhaiskasvatusKoulutustoimijaP,
    taiteenPerusopetusAtom,
    (kt, tp) => kt && !tp
  )
  const showTaiteenPerusopetusCheckbox = Bacon.combineWith(
    isTPOKoulutustoimijaP,
    varhaiskasvatusAtom,
    (kt, vk) => kt && !vk
  )

  return (
    <React.Fragment>
      {fromBacon(
        ift(
          showVarhaiskasvatusCheckbox,
          <VarhaiskasvatusCheckbox varhaiskasvatusAtom={varhaiskasvatusAtom} />
        )
      )}
      {fromBacon(
        ift(
          showTaiteenPerusopetusCheckbox,
          <TaiteenPerusopetusHankintakoulutusCheckbox
            tpoHankintakoulutusAtom={taiteenPerusopetusAtom}
          />
        )
      )}
      {fromBacon(
        ift(
          varhaiskasvatusAtom,
          <VarhaiskasvatuksenJärjestämismuoto
            järjestämismuotoAtom={järjestämismuotoAtom}
          />
        )
      )}
    </React.Fragment>
  )
}

const VarhaiskasvatusCheckbox = ({ varhaiskasvatusAtom }) => {
  const varhaiskasvatusOnChange = () => varhaiskasvatusAtom.modify((v) => !v)
  return (
    <label className="varhaiskasvatus-checkbox">
      <Text name="Päiväkodin esiopetus ostopalveluna tai palvelusetelinä" />
      <Checkbox
        id="varhaiskasvatus-checkbox"
        onChange={varhaiskasvatusOnChange}
        label="Esiopetus ostetaan oman organisaation ulkopuolelta"
        listStylePosition="inside"
      />
    </label>
  )
}

const TaiteenPerusopetusHankintakoulutusCheckbox = ({
  tpoHankintakoulutusAtom
}) => {
  const hankintakoulutusOnChange = () => {
    tpoHankintakoulutusAtom.modify((v) => !v)
  }
  return (
    <label className="tpo-hankintakoulutus-checkbox">
      <Text name="Taiteen perusopetus hankintakoulutuksena järjestettynä" />
      <Checkbox
        id="hankintakoulutus-checkbox"
        onChange={hankintakoulutusOnChange}
        label="Koulutus järjestetään oman organisaation ulkopuolelta"
        listStylePosition="inside"
      />
    </label>
  )
}

const VarhaiskasvatuksenJärjestämismuoto = ({ järjestämismuotoAtom }) => {
  const järjestysMuotoP = koodistoValues('vardajarjestamismuoto/JM02,JM03')
  return (
    <label id="varhaiskasvatus-jarjestamismuoto">
      <KoodistoDropdown
        className="varhaiskasvatus-jarjestamismuoto"
        title="Varhaiskasvatuksen järjestämismuoto"
        options={järjestysMuotoP}
        selected={järjestämismuotoAtom}
      />
    </label>
  )
}

const Oppilaitos = ({
  showVarhaiskasvatusToimipisteetP,
  showKaikkiOppilaitoksetP,
  oppilaitosAtom,
  organisaatiotyypitAtom
}) => {
  const selectableOrgTypes = [
    'OPPILAITOS',
    'OPPISOPIMUSTOIMIPISTE',
    VARHAISKASVATUKSEN_TOIMIPAIKKA
  ]
  return (
    <label className="oppilaitos">
      <Text name="Oppilaitos" />
      {Bacon.combineWith(
        oppilaitosAtom,
        showVarhaiskasvatusToimipisteetP,
        showKaikkiOppilaitoksetP,
        (
          oppilaitos,
          showVarhaiskasvatusToimipisteet,
          showKaikkiOppilaitokset
        ) => (
          <OrganisaatioPicker
            key={
              'uuden-oppijan-oppilaitos-' +
              (showVarhaiskasvatusToimipisteet
                ? 'vain-varhaiskasvatus'
                : 'oma-organisaatio') +
              (showKaikkiOppilaitokset ? '-kaikki' : '')
            }
            preselectSingleOption={true}
            selectedOrg={{
              oid: oppilaitos && oppilaitos.oid,
              nimi: oppilaitos && oppilaitos.nimi && t(oppilaitos.nimi)
            }}
            onSelectionChanged={(org) => {
              oppilaitosAtom.set({ oid: org && org.oid, nimi: org && org.nimi })
              organisaatiotyypitAtom.set(org && org.organisaatiotyypit)
            }}
            shouldShowOrg={(org) =>
              !org.organisaatiotyypit.some((tyyppi) => tyyppi === 'TOIMIPISTE')
            }
            canSelectOrg={(org) =>
              org.organisaatiotyypit.some((ot) =>
                selectableOrgTypes.includes(ot)
              )
            }
            clearText={t('tyhjennä')}
            noSelectionText="Valitse..."
            orgTypesToShow={
              showVarhaiskasvatusToimipisteet
                ? 'vainVarhaiskasvatusToimipisteet'
                : 'vainOmatOrganisaatiot'
            }
            showAll={!!showKaikkiOppilaitokset}
          />
        )
      )}
    </label>
  )
}

const Suorituskieli = ({ suorituskieliAtom, suorituskieletP }) => (
  <KoodistoDropdown
    className="suorituskieli"
    title={t('Suorituskieli')}
    selected={suorituskieliAtom}
    options={suorituskieletP}
  />
)
const OpiskeluoikeudenTyyppi = ({
  opiskeluoikeudenTyyppiAtom,
  opiskeluoikeustyypitP
}) => (
  <KoodistoDropdown
    className="opiskeluoikeudentyyppi"
    title="Opiskeluoikeus"
    options={opiskeluoikeustyypitP}
    selected={opiskeluoikeudenTyyppiAtom}
  />
)

const Aloituspäivä = ({ dateAtom }) => {
  return (
    <label className="aloituspaiva">
      <Text name="Aloituspäivä" />
      <DateInput
        value={dateAtom.get()}
        valueCallback={(value) => dateAtom.set(value)}
        validityCallback={(valid) => !valid && dateAtom.set(undefined)}
        data-testid={'aloituspaiva-input'}
      />
    </label>
  )
}

const OpiskeluoikeudenTila = ({ tilaAtom, opiskeluoikeudenTilatP }) => {
  return (
    <KoodistoDropdown
      className="opiskeluoikeudentila"
      title="Opiskeluoikeuden tila"
      options={opiskeluoikeudenTilatP}
      selected={tilaAtom}
    />
  )
}

const OpintojenRahoitus = ({
  tyyppiAtom,
  rahoitusAtom,
  opintojenRahoituksetP,
  suoritystyyppiP
}) => {
  const options = Bacon.combineWith(
    tyyppiAtom,
    opintojenRahoituksetP,
    suoritystyyppiP,
    (opiskeluoikeudenTyyppi, rahoitukset, päätasonSuorituksenTyyppi) => {
      if (
        koodiarvoMatch(
          'aikuistenperusopetus',
          'lukiokoulutus',
          'internationalschool',
          'ibtutkinto'
        )(opiskeluoikeudenTyyppi)
      ) {
        return rahoitukset.filter((v) =>
          sallitutRahoituskoodiarvot.includes(v.koodiarvo)
        )
      } else if (koodiarvoMatch('tuva')(opiskeluoikeudenTyyppi)) {
        return rahoitukset.filter((v) =>
          tuvaSallitutRahoituskoodiarvot.includes(v.koodiarvo)
        )
      } else if (
        koodiarvoMatch('europeanschoolofhelsinki')(opiskeluoikeudenTyyppi)
      ) {
        return rahoitukset.filter((v) =>
          eshSallitutRahoituskoodiarvot.includes(v.koodiarvo)
        )
      } else if (
        koodiarvoMatch('vstjotpakoulutus')(päätasonSuorituksenTyyppi) ||
        koodiarvoMatch('muukuinsaanneltykoulutus')(opiskeluoikeudenTyyppi)
      ) {
        return rahoitukset.filter((v) =>
          jotpaSallitutRahoituskoodiarvot.includes(v.koodiarvo)
        )
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

const MaksuttomuusRadioButtons = ({ maksuttomuusAtom }) => {
  return (
    <RadioButtons
      options={maksuttomuusOptions}
      selected={maksuttomuusAtom}
      onSelectionChanged={(selected) => maksuttomuusAtom.set(selected.key)}
      data-testid="maksuttomuus-radio-buttons"
    />
  )
}

const TuvaJärjestämisLupa = ({
  tuvaJärjestämislupaAtom,
  tuvaJärjestämislupaP
}) => {
  return (
    <KoodistoDropdown
      className="järjestämislupa"
      title="Järjestämislupa"
      options={tuvaJärjestämislupaP}
      selected={tuvaJärjestämislupaAtom}
    />
  )
}

const TaiteenPerusopetuksenOppimäärä = ({ tpoOppimääräAtom }) => {
  const tpoOppimäärätP = koodistoValues('taiteenperusopetusoppimaara').map(
    (values) => values.reverse()
  )
  tpoOppimäärätP.onValue((koodit) =>
    tpoOppimääräAtom.set(koodit.find(koodiarvoMatch('yleinenoppimaara')))
  )
  return (
    <label id="tpo-oppimäärä">
      <KoodistoDropdown
        className="tpo-oppimäärä"
        title="Oppimäärä"
        options={tpoOppimäärätP}
        selected={tpoOppimääräAtom}
      />
    </label>
  )
}

const TaiteenPerusopetuksenKoulutuksenToteutustapa = ({
  tpoToteutustapaAtom,
  hankintakoulutusAtom
}) => {
  const onHankintakoulutus = !!hankintakoulutusAtom.get()
  const tpoToteutustavatP = koodistoValues(
    onHankintakoulutus
      ? 'taiteenperusopetuskoulutuksentoteutustapa/hankintakoulutus'
      : 'taiteenperusopetuskoulutuksentoteutustapa/itsejarjestettykoulutus'
  )
  tpoToteutustavatP.onValue((koodit) =>
    tpoToteutustapaAtom.set(
      koodit.find(
        koodiarvoMatch(
          onHankintakoulutus ? 'hankintakoulutus' : 'itsejarjestettykoulutus'
        )
      )
    )
  )
  return (
    <label id="tpo-toteutustapa">
      <KoodistoDropdown
        className="tpo-toteutustapa"
        title="Koulutuksen toteutustapa"
        options={tpoToteutustavatP}
        selected={tpoToteutustapaAtom}
      />
    </label>
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
  opintokokonaisuus,
  osaamismerkki,
  onTaiteenPerusopetusOpiskeluoikeus,
  tpoOppimäärä,
  tpoToteutustapa,
  tpoTaiteenala
) => {
  const makeOpiskeluoikeusjakso = () => {
    const opiskeluoikeusjakso = alkamispäivä &&
      tila && { alku: formatISODate(alkamispäivä), tila }
    // TODO: Korjaa. Miksei käytetä if-rakennetta?
    // eslint-disable-next-line no-unused-expressions
    opiskeluoikeusjakso && opintojenRahoitus
      ? (opiskeluoikeusjakso.opintojenRahoitus = opintojenRahoitus)
      : opiskeluoikeusjakso

    return opiskeluoikeusjakso
  }

  const vstOpintokokonaisuusOk =
    !suoritustyyppi ||
    !opintokokonaisuudellisetVstSuoritustyypit.includes(
      suoritustyyppi.koodiarvo
    ) ||
    opintokokonaisuus

  const vstOsaamismerkkiOk =
    !suoritustyyppi ||
    !['vstosaamismerkki'].includes(suoritustyyppi.koodiarvo) ||
    osaamismerkki

  const muksOpintokokonaisuusOk =
    tyyppi &&
    (tyyppi.koodiarvo === 'muukuinsaanneltykoulutus'
      ? !!opintokokonaisuus
      : true)

  if (
    alkamispäivä &&
    oppilaitos &&
    tyyppi &&
    suoritus &&
    tila &&
    (!varhaiskasvatusOrganisaationUlkopuolelta ||
      varhaiskasvatusJärjestämismuoto) &&
    (!maksuttomuusTiedonVoiValita || maksuttomuus !== undefined) &&
    (!onTuvaOpiskeluoikeus || tuvaJärjestämislupa) &&
    vstOpintokokonaisuusOk &&
    vstOsaamismerkkiOk &&
    muksOpintokokonaisuusOk &&
    (!onTaiteenPerusopetusOpiskeluoikeus ||
      (tpoOppimäärä && tpoToteutustapa && tpoTaiteenala))
  ) {
    const järjestämismuoto =
      tyyppi.koodiarvo === 'esiopetus'
        ? { järjestämismuoto: varhaiskasvatusJärjestämismuoto }
        : {}
    const maksuttomuusLisätieto =
      maksuttomuusTiedonVoiValita && maksuttomuus !== 'none'
        ? {
            lisätiedot: {
              maksuttomuus: [
                { alku: formatISODate(alkamispäivä), maksuton: maksuttomuus }
              ]
            }
          }
        : {}
    const järjestämislupa = onTuvaOpiskeluoikeus
      ? { järjestämislupa: tuvaJärjestämislupa || {} }
      : {}
    const tuvaOletusLisätiedot = onTuvaOpiskeluoikeus
      ? getTuvaLisätiedot(tuvaJärjestämislupa)
      : {}
    const oppimäärä =
      tyyppi.koodiarvo === 'taiteenperusopetus'
        ? { oppimäärä: tpoOppimäärä }
        : {}
    const toteutustapa =
      tyyppi.koodiarvo === 'taiteenperusopetus'
        ? { koulutuksenToteutustapa: tpoToteutustapa }
        : {}

    if (
      opintokokonaisuus &&
      tyyppi.koodiarvo === 'vapaansivistystyonkoulutus' &&
      suoritustyyppi.koodiarvo === 'vstvapaatavoitteinenkoulutus'
    ) {
      suoritus.koulutusmoduuli.opintokokonaisuus = opintokokonaisuus
    }

    if (
      osaamismerkki &&
      tyyppi.koodiarvo === 'vapaansivistystyonkoulutus' &&
      suoritustyyppi.koodiarvo === 'vstosaamismerkki'
    ) {
      suoritus.koulutusmoduuli.tunniste = osaamismerkki
    }

    const opiskeluoikeus = {
      tyyppi,
      oppilaitos,
      alkamispäivä: formatISODate(alkamispäivä),
      tila: {
        opiskeluoikeusjaksot: [makeOpiskeluoikeusjakso()]
      },
      suoritukset: [suoritus]
    }
    return R.mergeAll([
      opiskeluoikeus,
      järjestämismuoto,
      R.mergeDeepLeft(maksuttomuusLisätieto, tuvaOletusLisätiedot),
      järjestämislupa,
      oppimäärä,
      toteutustapa
    ])
  }
}
