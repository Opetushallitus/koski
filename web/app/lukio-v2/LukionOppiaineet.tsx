import React, { useContext, useState } from 'react'
import { OpiskeluoikeusContext } from '../appstate/opiskeluoikeus'
import { usePreferences } from '../appstate/preferences'
import { FormModel, FormOptic } from '../components-v2/forms/FormModel'
import { ActivePäätasonSuoritus } from '../components-v2/containers/EditorContainer'
import { LukionOpiskeluoikeus } from '../types/fi/oph/koski/schema/LukionOpiskeluoikeus'
import { LocalizedString } from '../types/fi/oph/koski/schema/LocalizedString'
import { TestIdLayer, TestIdText } from '../appstate/useTestId'
import { finnish, t } from '../i18n/i18n'
import { FootnoteDescriptions } from '../components/footnote'
import { formatNumber, removeFloatingPointDrift, sum } from '../util/numbers'
import {
  isLukionOppimääränSuoritus2019,
  LukionOppimääränSuoritus2019
} from '../types/fi/oph/koski/schema/LukionOppimaaranSuoritus2019'
import { isTuettuSuoritus } from './LukionSuorituksenTiedot'
import {
  isLukionOppiaineenSuoritus2019,
  LukionOppiaineenSuoritus2019
} from '../types/fi/oph/koski/schema/LukionOppiaineenSuoritus2019'
import { isLukionPaikallisenOpintojaksonSuoritus2019 } from '../types/fi/oph/koski/schema/LukionPaikallisenOpintojaksonSuoritus2019'
import {
  isPaikallinenLukionOppiaine2019,
  PaikallinenLukionOppiaine2019
} from '../types/fi/oph/koski/schema/PaikallinenLukionOppiaine2019'
import { PaikallinenKoodi } from '../types/fi/oph/koski/schema/PaikallinenKoodi'
import { TextEdit } from '../components-v2/controls/TextField'
import {
  LocalizedTextEdit,
  LocalizedTextView
} from '../components-v2/controls/LocalizedTestField'
import { Arviointi } from '../types/fi/oph/koski/schema/Arviointi'
import { LaajuusOpintopisteissä } from '../types/fi/oph/koski/schema/LaajuusOpintopisteissa'
import {
  parasArviointi,
  parasArviointiIndex,
  viimeisinArviointi
} from '../util/arvioinnit'
import { suoritusValmis } from '../util/suoritus'
import {
  CHARCODE_KESKEN,
  CHARCODE_REMOVE,
  CHARCODE_VALMIS,
  Icon
} from '../components-v2/texts/Icon'
import { FormField } from '../components-v2/forms/FormField'
import {
  koodiarvoOnly,
  ParasArvosanaEdit,
  ParasArvosanaKoodiarvoView
} from '../components-v2/opiskeluoikeus/ArvosanaField'
import {
  KoodistoEdit,
  KoodistoViewProps,
  KoodistoViewSpan
} from '../components-v2/opiskeluoikeus/KoodistoField'
import {
  isLukionÄidinkieliJaKirjallisuus2019,
  LukionÄidinkieliJaKirjallisuus2019
} from '../types/fi/oph/koski/schema/LukionAidinkieliJaKirjallisuus2019'
import { isVierasTaiToinenKotimainenKieli2019 } from '../types/fi/oph/koski/schema/VierasTaiToinenKotimainenKieli2019'
import {
  isLukionMatematiikka2019,
  LukionMatematiikka2019
} from '../types/fi/oph/koski/schema/LukionMatematiikka2019'
import { appendOptional, deleteAt } from '../util/array'
import { IconButton } from '../components-v2/controls/IconButton'
import { Select, SelectOption } from '../components-v2/controls/Select'
import { useKoodisto } from '../appstate/koodisto'
import { Koodistokoodiviite } from '../types/fi/oph/koski/schema/Koodistokoodiviite'
import { LukionUskonto2019 } from '../types/fi/oph/koski/schema/LukionUskonto2019'
import { LukionMuuValtakunnallinenOppiaine2019 } from '../types/fi/oph/koski/schema/LukionMuuValtakunnallinenOppiaine2019'
import { FlatButton } from '../components-v2/controls/FlatButton'
import { UusiLukionModuuli } from './UusiLukionModuuli'
import {
  BooleanEdit,
  BooleanView
} from '../components-v2/opiskeluoikeus/BooleanField'
import {
  LaajuusOpintopisteissäEdit,
  LaajuusView
} from '../components-v2/opiskeluoikeus/LaajuusField'
import {
  OsaamisenTunnustusEdit,
  OsaamisenTunnustusView
} from '../components-v2/opiskeluoikeus/TunnustusField'
import { DateView, DateEdit } from '../components-v2/controls/DateField'
import {
  KeyValueTable,
  KeyValueRow
} from '../components-v2/containers/KeyValueTable'
import {
  PositionalPopup,
  PositionalPopupHolder
} from '../components-v2/containers/PositionalPopup'
import {
  Modal,
  ModalTitle,
  ModalBody,
  ModalFooter
} from '../components-v2/containers/Modal'
import { LukionPaikallinenOpintojakso2019 } from '../types/fi/oph/koski/schema/LukionPaikallinenOpintojakso2019'
import { OsaamisenTunnustaminen } from '../types/fi/oph/koski/schema/OsaamisenTunnustaminen'
import {
  isMuidenLukioOpintojenSuoritus2019,
  MuidenLukioOpintojenSuoritus2019
} from '../types/fi/oph/koski/schema/MuidenLukioOpintojenSuoritus2019'
import { MuutLukionSuoritukset2019 } from '../types/fi/oph/koski/schema/MuutLukionSuoritukset2019'
import { Lukiodiplomit2019 } from '../types/fi/oph/koski/schema/Lukiodiplomit2019'
import { TemaattisetOpinnot2019 } from '../types/fi/oph/koski/schema/TemaattisetOpinnot2019'
import { UusiLukionMuuSuoritusModuuli } from './UusiLukionMuuSuoritusModuuli'

type LukionOppiaineetProps = {
  form: FormModel<LukionOpiskeluoikeus>
  päätasonSuoritus: ActivePäätasonSuoritus<LukionOpiskeluoikeus>
}

// "Tila"/"Poista"-sarakkeiden otsikot ovat vain kuvakkeita (ei näkyvää
// tekstiä) - axe-core (empty-table-header) vaatii oikean, näkyvän-
// ruudunlukijalle tekstisisällön, pelkkä aria-label th:lla ei riitä.
const VISUALLY_HIDDEN: React.CSSProperties = {
  position: 'absolute',
  width: 1,
  height: 1,
  overflow: 'hidden',
  clip: 'rect(0, 0, 0, 0)',
  whiteSpace: 'nowrap'
}

type ArviointiLike = {
  arvosana: { koodiarvo: string }
  hyväksytty?: boolean
}

// Sama poissulkulista kuin YleissivistävänKoulutuksenArviointi.hyväksytty
// (schema) ja vanhan käyttöliittymän hyväksytystiArvioidutOsasuoritukset
// (lukio/lukio.js): "O" (osallistunut, ei arvioitu) ei ole hyväksytty,
// vaikka se ei olekaan varsinainen hylätty arvosana.
const isHyväksytty = (arviointi?: ArviointiLike): boolean => {
  if (!arviointi) return false
  if (arviointi.hyväksytty !== undefined) return arviointi.hyväksytty
  return !['4', 'H', 'O'].includes(arviointi.arvosana.koodiarvo)
}

// Kieliaineen kieli (esim. "Suomen kieli ja kirjallisuus") ja matematiikan
// oppimäärä (pitkä/lyhyt/yhteinen opintokokonaisuus) renderöidään molemmat
// inline-tekstinä oppiaineen nimen jatkeena, kuten vanhassa
// käyttöliittymässäkin - ei KoodistoView'n <div>, joka katkaisisi rivin. Oma
// testId kummallekin, jottei sekoitu toisiinsa.
const inlineNimiView =
  (testId: string) =>
  <T extends string>(props: KoodistoViewProps<T>) => (
    <TestIdText id={testId}>
      {props.value ? t(props.value.nimi) : ''}
    </TestIdText>
  )

const KieliNimiView = inlineNimiView('kieli.value')
const OppimääräNimiView = inlineNimiView('oppimäärä.value')

// Vain kahdella oppiainetyypillä on kieli-kenttä, ja kummallakin on oma
// koodistonsa - ks. Lukio2019.scala:n LukionÄidinkieliJaKirjallisuus2019 ja
// VierasTaiToinenKotimainenKieli2019.
const kieliaineKoodistoUri = (
  koulutusmoduuli: LukionOppiaineenSuoritus2019['koulutusmoduuli']
): string | undefined => {
  if (isLukionÄidinkieliJaKirjallisuus2019(koulutusmoduuli)) {
    return 'oppiaineaidinkielijakirjallisuus'
  }
  if (isVierasTaiToinenKotimainenKieli2019(koulutusmoduuli)) {
    return 'kielivalikoima'
  }
  return undefined
}

// Yleinen "laajuuden laskenta arvioiduista osasuorituksista" - toimii sekä
// oikeille kursseille/opintojaksoille että (erityisenä tutkintona
// suoritetulle oppiaineelle) oppiaineelle ITSELLEEN yhden alkion listana, ks.
// isErityinenTutkinto/oppiaineenArvioituLaajuus. Sama kaksoiskäyttö kuin
// vanhan käyttöliittymän lukio.js:n laajuudet()/arvioidutOsasuoritukset()
// -funktioilla, jotka ottavat kurssilistan TAI [oppiaine]:n vastaan.
type LaajuusArvioitavaItem = {
  arviointi?: unknown[]
  koulutusmoduuli: { laajuus?: LaajuusOpintopisteissä }
}

const itemArviointi = (item: LaajuusArvioitavaItem) =>
  parasArviointi(item.arviointi as Arviointi[] | undefined)

// Puuttuva laajuus lasketaan yhdeksi (vrt. lukio.js: "laajuus || 1") - ei
// pitäisi tulla vastaan tämän hetken skeemalla, mutta pidetään samana
// turvaverkkona kuin vanhassa käyttöliittymässä.
const itemLaajuusArvo = (item: LaajuusArvioitavaItem): number =>
  item.koulutusmoduuli.laajuus?.arvo ?? 1

// "Arvioitu" (kokonaismäärä) ja "Hyväksytysti arvioitu" katsovat eri
// arviointia vanhassa käyttöliittymässä (lukio.js:
// arvioidutOsasuoritukset/hyväksytystiArvioidutOsasuoritukset) - tämä EI ole
// vahinko, vaan tarkoituksellista: "arvioitu" kertoo onko osasuoritus ylipäätään
// käsitelty VIIMEISIMMÄN arvosanan perusteella (jottei esim. korjattu/uusittu
// arvosana näy vielä arvioimattomana), kun taas "hyväksytysti arvioitu" katsoo
// PARASTA arvosanaa (jottei esim. kesken-jäänyt uusintayritys pilaa aiemmin
// saatua hyväksyttyä arvosanaa). Molemmissa "O" (osallistunut, ei arvioitu)
// suljetaan pois - se ei ole arvioimaton (undefined), mutta ei myöskään
// lasketa arvioiduksi.
const ARVOSANA_OSALLISTUNUT = 'O'
const itemViimeisinArvosana = (
  item: LaajuusArvioitavaItem
): string | undefined =>
  viimeisinArviointi(item.arviointi as Arviointi[] | undefined)?.arvosana
    .koodiarvo

const arvioituLaajuus = (
  items: LaajuusArvioitavaItem[]
): { arvioitu: number; hyväksytysti: number } => {
  const arvioidut = items.filter((i) => {
    const arvosana = itemViimeisinArvosana(i)
    return arvosana !== undefined && arvosana !== ARVOSANA_OSALLISTUNUT
  })
  const hyväksytyt = items.filter((i) =>
    isHyväksytty(itemArviointi(i) as ArviointiLike | undefined)
  )
  return {
    arvioitu: sum(arvioidut.map(itemLaajuusArvo)),
    hyväksytysti: sum(hyväksytyt.map(itemLaajuusArvo))
  }
}

// Suoritustason lippu tekee KAIKISTA oppiaineista erityisenä tutkintona
// suoritettuja näyttämisen/muokkauksen kannalta, vaikka oppiaineen oma lippu
// pysyisi epätosi - sama sääntö kuin vanhan käyttöliittymän
// lukio.js:n isErityinenTutkinto.
const isErityinenTutkinto = (
  suoritus: LukionOppiaineenSuoritus2019,
  päätasonSuoritusErityinen: boolean
): boolean =>
  suoritus.suoritettuErityisenäTutkintona || päätasonSuoritusErityinen

const oppiaineenArvioituLaajuus = (
  suoritus: LukionOppiaineenSuoritus2019,
  erityinenTutkinto: boolean
): { arvioitu: number; hyväksytysti: number } =>
  erityinenTutkinto
    ? arvioituLaajuus([suoritus])
    : arvioituLaajuus(suoritus.osasuoritukset || [])

// Oppiaineiden (LukionOppiaineenSuoritus2019) lisäksi osasuoritukset-taulukko
// sisältää lukiodiplomit/muut suoritukset/teemaopinnot
// (MuidenLukioOpintojenSuoritus2019) - sisarluokka oppiaineelle, ei sen
// aliluokka, joten se tarvitsee oman tyyppinsä ja oman rivikomponenttinsa
// (MuuSuoritusRivi). Molemmat osuvat samaan suoritus.osasuoritukset-
// taulukkoon, siksi alkuperäinen indeksi talteen jo listaa rakennettaessa.
//
// Rakennetaan oikealla <table>-elementillä OsasuoritusTablen (CSS Grid) sijaan.
// OsasuoritusTable mitoittaa jokaisen rivin ruudukon erikseen oman sisältönsä
// mukaan, joten lyhyen sisällön (esim. yksinumeroinen arvosana) sarakkeet eivät
// osu kohdalleen otsikon tai muiden rivien kanssa - <table> taas mitoittaa
// sarakkeet koko taulukon yli yhtenäisesti, kuten vanhassa käyttöliittymässä.
// Sama ratkaisu kuin components-v2/opiskeluoikeus/OppiaineTable.tsx:ssä (IB).
export const LukionOppiaineet: React.FC<LukionOppiaineetProps> = ({
  form,
  päätasonSuoritus
}) => {
  const suoritus = päätasonSuoritus.suoritus

  if (!isTuettuSuoritus(suoritus)) {
    return null
  }

  // Yksi haara riittää .prop('osasuoritukset'):n optiikalle - kenttä on
  // identtinen molemmissa suoritustyypeissä.
  const path = päätasonSuoritus.path as unknown as FormOptic<
    LukionOpiskeluoikeus,
    LukionOppimääränSuoritus2019
  >

  // suoritettuErityisenäTutkintona on vain LukionOppimääränSuoritus2019:llä,
  // ei LukionOppiaineidenOppimäärienSuoritus2019:llä ("lukion aineopinnot") -
  // ks. Lukio2019.scala.
  const päätasonSuoritusErityinen =
    isLukionOppimääränSuoritus2019(suoritus) &&
    suoritus.suoritettuErityisenäTutkintona

  // Koko osasuoritukset-taulukon optiikka - tarvitaan sekä uuden rivin
  // lisäykseen (appendOptional) että poistoon (deleteAt), toisin kuin
  // yksittäisen rivin .at(index)-poluille. Tyypitetty molempien rivityyppien
  // yhdisteenä, koska ne jakavat saman taulukon - UusiLukionOppiaine saa oman,
  // suppeamman casin (ks. alla).
  const osasuorituksetPath = path.prop(
    'osasuoritukset'
  ) as unknown as FormOptic<
    LukionOpiskeluoikeus,
    | Array<LukionOppiaineenSuoritus2019 | MuidenLukioOpintojenSuoritus2019>
    | undefined
  >

  // Alkuperäinen indeksi talteen suodatuksen yli: FormOptic.at(index) tarvitsee
  // oikean paikan suoritus.osasuoritukset-taulukossa, ei suodatetun listan
  // paikkaa.
  const kaikkiRivit = (suoritus.osasuoritukset || []).flatMap(
    (osasuoritus, index): OsasuoritusRivi[] => {
      if (isLukionOppiaineenSuoritus2019(osasuoritus)) {
        return [{ kind: 'oppiaine', suoritus: osasuoritus, index }]
      }
      if (isMuidenLukioOpintojenSuoritus2019(osasuoritus)) {
        return [{ kind: 'muuSuoritus', suoritus: osasuoritus, index }]
      }
      return []
    }
  )
  // Suodatetaan jo luokitellusta kaikkiRivit-listasta sen sijaan, että
  // suoritus.osasuoritukset luokiteltaisiin uudelleen kahdesti.
  const oppiaineet = kaikkiRivit.flatMap((rivi) =>
    rivi.kind === 'oppiaine' ? [rivi.suoritus] : []
  )
  const muutSuoritukset = kaikkiRivit.flatMap((rivi) =>
    rivi.kind === 'muuSuoritus' ? [rivi.suoritus] : []
  )

  return (
    <div className="LukionOppiaineet">
      <table className="LukionOppiaineet__taulukko">
        <thead>
          <tr>
            <th className="LukionOppiaineet__tila">
              <span style={VISUALLY_HIDDEN}>{t('Tila')}</span>
            </th>
            <th className="LukionOppiaineet__oppiaine">{t('Oppiaine')}</th>
            <th className="LukionOppiaineet__laajuus">
              {t('Arvioitu (opintopistettä)')}
            </th>
            <th className="LukionOppiaineet__laajuus">
              {t('Hyväksytysti arvioitu (opintopistettä)')}
            </th>
            <th className="LukionOppiaineet__arvosana">{t('Arvosana')}</th>
            {form.editMode && (
              <th className="LukionOppiaineet__poisto">
                <span style={VISUALLY_HIDDEN}>{t('Poista')}</span>
              </th>
            )}
          </tr>
        </thead>
        <TestIdLayer id="osasuoritukset">
          <tbody>
            {kaikkiRivit.map((rivi, i) => (
              <TestIdLayer id={i} key={i}>
                {rivi.kind === 'oppiaine' ? (
                  <OppiaineRivi
                    form={form}
                    oppiainePath={
                      path
                        .prop('osasuoritukset')
                        .optional()
                        .at(rivi.index) as unknown as FormOptic<
                        LukionOpiskeluoikeus,
                        LukionOppiaineenSuoritus2019
                      >
                    }
                    suoritus={rivi.suoritus}
                    päätasonSuoritusErityinen={päätasonSuoritusErityinen}
                    onRemove={() =>
                      form.updateAt(osasuorituksetPath, (os) =>
                        deleteAt(rivi.index)(os || [])
                      )
                    }
                  />
                ) : (
                  <MuuSuoritusRivi
                    form={form}
                    muuSuoritusPath={
                      path
                        .prop('osasuoritukset')
                        .optional()
                        .at(rivi.index) as unknown as FormOptic<
                        LukionOpiskeluoikeus,
                        MuidenLukioOpintojenSuoritus2019
                      >
                    }
                    suoritus={rivi.suoritus}
                    onRemove={() =>
                      form.updateAt(osasuorituksetPath, (os) =>
                        deleteAt(rivi.index)(os || [])
                      )
                    }
                  />
                )}
              </TestIdLayer>
            ))}
          </tbody>
        </TestIdLayer>
      </table>
      <UusiLukionOppiaine
        form={form}
        osasuorituksetPath={
          osasuorituksetPath as unknown as FormOptic<
            LukionOpiskeluoikeus,
            LukionOppiaineenSuoritus2019[] | undefined
          >
        }
        olemassaOlevatKoodiarvot={oppiaineet.map(
          (o) => o.koulutusmoduuli.tunniste.koodiarvo
        )}
      />
      <UusiMuuSuoritus
        form={form}
        osasuorituksetPath={osasuorituksetPath}
        olemassaOlevatKoodiarvot={muutSuoritukset.map(
          (m) => m.koulutusmoduuli.tunniste.koodiarvo
        )}
      />
      {onPaikallisia(oppiaineet, muutSuoritukset) && (
        <FootnoteDescriptions
          data={[
            {
              title: 'Paikallinen opintojakso tai oppiaine',
              hint: PAIKALLINEN_HINT
            }
          ]}
        />
      )}
      {(päätasonSuoritusErityinen ||
        oppiaineet.some((s) => s.suoritettuErityisenäTutkintona)) && (
        <FootnoteDescriptions
          data={[
            {
              title: 'Erityisenä tutkintona suoritettu oppiaine',
              hint: ERITYINEN_TUTKINTO_HINT
            }
          ]}
        />
      )}
      <KurssienLaajuusYhteensä
        osasuoritukset={oppiaineet}
        muutSuoritukset={muutSuoritukset}
        päätasonSuoritusErityinen={päätasonSuoritusErityinen}
      />
    </div>
  )
}

// Jaettu oppiaineen ja kurssin/moduulin pakollinen-/suorituskieli-kenttien
// kesken (OppiaineRivi/KurssiLista) - vain itse FormField on yhteinen,
// kutsupaikat vastaavat itse näkyvyysehdoistaan ja ympäröivästä
// div/span-kääreestään, koska ne eroavat tasojen välillä.
const PakollinenField: React.FC<{
  form: FormModel<LukionOpiskeluoikeus>
  path: FormOptic<LukionOpiskeluoikeus, boolean>
}> = ({ form, path }) => (
  <FormField
    form={form}
    path={path}
    view={() => null}
    edit={BooleanEdit}
    editProps={{ label: t('Pakollinen') }}
    testId="pakollinen"
  />
)

const SuorituskieliField: React.FC<{
  form: FormModel<LukionOpiskeluoikeus>
  path: FormOptic<LukionOpiskeluoikeus, Koodistokoodiviite<'kieli'> | undefined>
}> = ({ form, path }) => (
  <FormField
    form={form}
    path={path}
    optional
    view={KoodistoViewSpan}
    edit={KoodistoEdit}
    editProps={{ koodistoUri: 'kieli', zeroValueOption: true }}
    testId="suorituskieli"
  />
)

type OsasuoritusRivi =
  | { kind: 'oppiaine'; suoritus: LukionOppiaineenSuoritus2019; index: number }
  | {
      kind: 'muuSuoritus'
      suoritus: MuidenLukioOpintojenSuoritus2019
      index: number
    }

const OppiaineRivi: React.FC<{
  form: FormModel<LukionOpiskeluoikeus>
  oppiainePath: FormOptic<LukionOpiskeluoikeus, LukionOppiaineenSuoritus2019>
  suoritus: LukionOppiaineenSuoritus2019
  päätasonSuoritusErityinen: boolean
  onRemove: () => void
}> = ({
  form,
  oppiainePath,
  suoritus,
  päätasonSuoritusErityinen,
  onRemove
}) => {
  const kurssit = suoritus.osasuoritukset || []
  const erityinen = isErityinenTutkinto(suoritus, päätasonSuoritusErityinen)
  const { arvioitu, hyväksytysti } = oppiaineenArvioituLaajuus(
    suoritus,
    erityinen
  )
  const valmis = suoritusValmis(suoritus)
  const laajuusPath = (
    oppiainePath.prop('koulutusmoduuli') as unknown as FormOptic<
      LukionOpiskeluoikeus,
      { laajuus?: LaajuusOpintopisteissä }
    >
  ).prop('laajuus')

  return (
    <tr data-koodiarvo={suoritus.koulutusmoduuli.tunniste.koodiarvo}>
      <td className="LukionOppiaineet__tila">
        {valmis ? (
          <span title={t('Suoritus valmis')}>
            <Icon charCode={CHARCODE_VALMIS} />
          </span>
        ) : (
          <span title={t('Suoritus kesken')}>
            <Icon charCode={CHARCODE_KESKEN} />
          </span>
        )}
      </td>
      <td className="LukionOppiaineet__oppiaine">
        <OppiaineNimi
          form={form}
          oppiainePath={oppiainePath}
          suoritus={suoritus}
        />
        <OppiaineFootnoteHints
          suoritus={suoritus}
          päätasonSuoritusErityinen={päätasonSuoritusErityinen}
        />
        {form.editMode && (
          <div className="LukionOppiaineet__pakollinen">
            <PakollinenField
              form={form}
              path={oppiainePath.prop('koulutusmoduuli').prop('pakollinen')}
            />
          </div>
        )}
        {form.editMode &&
          isLukionMatematiikka2019(suoritus.koulutusmoduuli) && (
            <div className="LukionOppiaineet__oppimäärä">
              {t('Oppimäärä')}
              {': '}
              <FormField
                form={form}
                path={(
                  oppiainePath.prop('koulutusmoduuli') as unknown as FormOptic<
                    LukionOpiskeluoikeus,
                    LukionMatematiikka2019
                  >
                ).prop('oppimäärä')}
                view={() => null}
                edit={KoodistoEdit}
                editProps={{ koodistoUri: 'oppiainematematiikka' }}
                testId="oppimääräEdit"
              />
            </div>
          )}
        {form.editMode && (
          <div className="LukionOppiaineet__erityinenTutkinto">
            <FormField
              form={form}
              path={oppiainePath.prop('suoritettuErityisenäTutkintona')}
              view={() => null}
              edit={BooleanEdit}
              editProps={{ label: t('Suoritettu erityisenä tutkintona') }}
              testId="suoritettuErityisenäTutkintona"
            />
          </div>
        )}
        {(form.editMode || suoritus.suorituskieli) && (
          <div className="LukionOppiaineet__suorituskieli">
            {t('Suorituskieli')}
            {': '}
            <SuorituskieliField
              form={form}
              path={oppiainePath.prop('suorituskieli')}
            />
          </div>
        )}
        {(kurssit.length > 0 || form.editMode) && (
          <KurssiLista
            form={form}
            parentPath={oppiainePath}
            kurssit={kurssit}
            renderLisaaModal={({
              olemassaOlevatKoodiarvot,
              onAdd,
              onClose
            }) => (
              <UusiLukionModuuli
                oppiaineKoodiarvo={suoritus.koulutusmoduuli.tunniste.koodiarvo}
                olemassaOlevatKoodiarvot={olemassaOlevatKoodiarvot}
                onAdd={onAdd}
                onClose={onClose}
              />
            )}
          />
        )}
      </td>
      <td className="LukionOppiaineet__laajuus">
        {form.editMode && erityinen ? (
          <FormField
            form={form}
            path={laajuusPath}
            optional
            view={() => null}
            edit={LaajuusOpintopisteissäEdit}
            testId="laajuus"
          />
        ) : (
          <TestIdText id="laajuus.arvioitu">
            {formatNumber(arvioitu)}
          </TestIdText>
        )}
      </td>
      <td className="LukionOppiaineet__laajuus">
        <TestIdText id="laajuus.hyvaksytystiArvioitu">
          {formatNumber(hyväksytysti)}
        </TestIdText>
      </td>
      <td className="LukionOppiaineet__arvosana">
        <FormField
          form={form}
          path={oppiainePath.prop('arviointi')}
          optional
          view={ParasArvosanaKoodiarvoView}
          edit={ParasArvosanaEdit}
          editProps={{
            suoritusClassName: suoritus.$class,
            format: koodiarvoOnly
          }}
          testId="arvosana"
        />
      </td>
      {form.editMode && (
        <td className="LukionOppiaineet__poisto">
          <IconButton
            charCode={CHARCODE_REMOVE}
            label={t('Poista')}
            onClick={onRemove}
            testId="poisto"
          />
        </td>
      )}
    </tr>
  )
}

const PAIKALLINEN_HINT = '*'
const ERITYINEN_TUTKINTO_HINT = '**'

const OppiaineFootnoteHints: React.FC<{
  suoritus: LukionOppiaineenSuoritus2019
  päätasonSuoritusErityinen: boolean
}> = ({ suoritus, päätasonSuoritusErityinen }) => {
  const hints = [
    isPaikallinenLukionOppiaine2019(suoritus.koulutusmoduuli) &&
      PAIKALLINEN_HINT,
    isErityinenTutkinto(suoritus, päätasonSuoritusErityinen) &&
      ERITYINEN_TUTKINTO_HINT
  ].filter(Boolean) as string[]

  return hints.length > 0 ? (
    <sup className="footnote-hint">{` ${hints.join(' ')}`}</sup>
  ) : null
}

// Kurssit/opintojaksot näytetään aina näkyvissä oppiaineen nimen alla samoin
// kuin vanhassa käyttöliittymässä (kurssi/KurssiEditor.jsx). Samat CSS-luokat
// (.kurssit/.kurssi/.tunniste/.arvosana) tulevat globaalista
// style/kurssi.less:stä, joten ulkoasu vastaa vanhaa käyttöliittymää ilman
// uutta tyylittelyä.
// Kurssin yhteiset kentät, joita KurssiLista tarvitsee - molemmat
// kurssityyppiunionit (oppiaineissa/muissa opinnoissa) toteuttavat nämä
// identtisesti (yhteinen LukionModuulinTaiPaikallisenOpintojaksonSuoritus2019-
// trait skeemassa, jolle ei ole TS-tyyppiä valmiiksi generoituna).
type KurssiData = {
  $class: string
  koulutusmoduuli: {
    tunniste: { koodiarvo: string; nimi?: LocalizedString }
    pakollinen: boolean
    laajuus: LaajuusOpintopisteissä
    kuvaus?: LocalizedString
  }
  arviointi?: Arviointi[]
  tunnustettu?: OsaamisenTunnustaminen
  suorituskieli?: Koodistokoodiviite<'kieli'>
}

// Vanhan käyttöliittymän kurssi/KurssiEditor.jsx näyttää kurssin
// lisätiedot (Nimi/Laajuus/Pakollinen/Arvosana/Arviointipäivä/...)
// hiirellä osoittamalla avautuvassa popupissa katselutilassa - sama
// tieto, jota lukio-v2:n rivillä ei aiemmin näkynyt lainkaan (laajuus
// puuttui kokonaan, loput vain kun jo asetettu). Muokkaustilassa sama
// tieto näkyy ja on muokattavissa KurssinTietojenMuokkausModaalin kautta
// (ks. KurssiTunnisteJaInfoPopup) - tämä popup on siis vain katselutilan
// vastine, ei käytössä muokkauksen aikana.
const KurssiInfoPopup: React.FC<{
  kurssi: KurssiData
  paikallinen: boolean
}> = ({ kurssi, paikallinen }) => {
  const paras = parasArviointi(kurssi.arviointi as Arviointi[] | undefined)
  return (
    <KeyValueTable className="KurssiInfoPopup">
      <KeyValueRow localizableLabel="Nimi" innerKeyValueTable>
        <TestIdText id="popup.nimi">
          {t(kurssi.koulutusmoduuli.tunniste.nimi)}
        </TestIdText>
      </KeyValueRow>
      <KeyValueRow localizableLabel="Laajuus" innerKeyValueTable>
        <LaajuusView
          value={kurssi.koulutusmoduuli.laajuus}
          testId="popup.laajuus"
        />
      </KeyValueRow>
      <KeyValueRow localizableLabel="Pakollinen" innerKeyValueTable>
        <BooleanView
          value={kurssi.koulutusmoduuli.pakollinen}
          testId="popup.pakollinen"
        />
      </KeyValueRow>
      {kurssi.suorituskieli && (
        <KeyValueRow localizableLabel="Suorituskieli" innerKeyValueTable>
          <KoodistoViewSpan
            value={kurssi.suorituskieli}
            koodistoUri="kieli"
            testId="popup.suorituskieli"
          />
        </KeyValueRow>
      )}
      {paras && (
        <>
          <KeyValueRow localizableLabel="Arvosana" innerKeyValueTable>
            <TestIdText id="popup.arvosana">
              {paras.arvosana.koodiarvo}
            </TestIdText>
          </KeyValueRow>
          <KeyValueRow localizableLabel="Arviointipäivä" innerKeyValueTable>
            <DateView
              value={(paras as any).päivä}
              testId="popup.arviointipaiva"
            />
          </KeyValueRow>
        </>
      )}
      {/* Tunnustettu jätetään popupin ulkopuolelle: OsaamisenTunnustusView
          kiinnittää "selite"-osan testitunnisteensa aina sisäisesti riippumatta
          annetusta testId-propista, joten se törmäisi rivillä jo olevan,
          katselutilassa aina näkyvän inline-kentän kanssa saman kurssin
          TestIdLayerin sisällä (tämä popup näkyy vain katselutilassa, samoin
          kuin rivin oma inline-kenttä - molemmat olisivat siis samaan aikaan
          DOM:issa) - ks. lukio2019-moduulin-osaamisen-tunnustaminen.spec.ts,
          joka löysi tämän. Tieto ei katoa: se näkyy jo inline-kenttänä aina
          kun tunnustettu on asetettu. */}
      {paikallinen && kurssi.koulutusmoduuli.kuvaus && (
        <KeyValueRow localizableLabel="Kuvaus" innerKeyValueTable>
          <TestIdText id="popup.kuvaus">
            {t(kurssi.koulutusmoduuli.kuvaus)}
          </TestIdText>
        </KeyValueRow>
      )}
    </KeyValueTable>
  )
}

// Sama hybridirakenne kuin vanhan käyttöliittymän kurssi/KurssiEditor.jsx:ssä:
// klikkaus avaa/sulkee muokkausmodaalin vain muokkaustilassa, hiiren vienti
// päälle/pois avaa/sulkee katselutilan infopopupin vain katselutilassa - ei
// koskaan molempia yhtä aikaa, koska form.editMode ei voi olla molempia.
// Tunniste on <button>, ei <span> - sama kuin vanhassa käyttöliittymässä
// (ks. LukioOpiskeluoikeus.ts:n kurssi()-lokaattori, joka hakee sen roolilla
// "button") - tämä on myös saavutettavuuskorjaus: klikattava <span> ei olisi
// nappulan tavoin fokusoitavissa/näppäimistöllä käytettävissä.
const KurssiTunnisteJaInfoPopup = <T extends KurssiData>({
  form,
  kurssiPath,
  kurssi,
  paikallinen
}: {
  form: FormModel<LukionOpiskeluoikeus>
  kurssiPath: FormOptic<LukionOpiskeluoikeus, T>
  kurssi: T
  paikallinen: boolean
}) => {
  const [hoverAuki, setHoverAuki] = useState(false)
  const [muokkausAuki, setMuokkausAuki] = useState(false)
  const editMode = form.editMode

  const tunniste = (
    <button
      type="button"
      className="tunniste"
      title={t(kurssi.koulutusmoduuli.tunniste.nimi)}
      onClick={editMode ? () => setMuokkausAuki(true) : undefined}
      onMouseEnter={!editMode ? () => setHoverAuki(true) : undefined}
      onMouseLeave={!editMode ? () => setHoverAuki(false) : undefined}
    >
      {kurssi.koulutusmoduuli.tunniste.koodiarvo}
    </button>
  )

  if (editMode) {
    return (
      <>
        {tunniste}
        {muokkausAuki && (
          <KurssinTietojenMuokkausModaali
            form={form}
            kurssiPath={kurssiPath}
            kurssi={kurssi}
            paikallinen={paikallinen}
            onClose={() => setMuokkausAuki(false)}
          />
        )}
      </>
    )
  }

  return (
    <PositionalPopupHolder>
      {tunniste}
      <PositionalPopup open={hoverAuki} onDismiss={() => setHoverAuki(false)}>
        {/* Rakennetaan popupin sisältö vasta auetessa - muuten jokaisen
            kurssin KurssiInfoPopup rakennettaisiin heti katselutilan
            alkulatauksella (yleensä 35-60 kurssia), vaikka se näytetään
            kerrallaan korkeintaan yksi. PositionalPopup ei muutenkaan
            poista lapsia DOM:sta suljettuna, pelkkä CSS piilottaa ne. */}
        {hoverAuki && (
          <KurssiInfoPopup kurssi={kurssi} paikallinen={paikallinen} />
        )}
      </PositionalPopup>
    </PositionalPopupHolder>
  )
}

// Vanhan käyttöliittymän kurssi/KurssiPopup.jsx vastine: geneerinen
// PropertiesEditor näyttää siellä kaikki kurssin kentät paitsi
// tunniste/koodiarvo/nimi (lukittu) ja arviointi (piilotettu kunnes
// arviointi on olemassa) - tämä modaali toistaa saman kenttäjoukon
// nimenomaisesti, koska geneeristä skeemaan perustuvaa editoria ei ole.
// Arvosana muokataan tässä (yhdessä Arviointipäivän kanssa, loogisena
// parina) - KurssiListan rivillä näkyy siitä silti aina pelkkä katselu,
// jotta arvosana on edelleen nähtävissä yhdellä silmäyksellä avaamatta
// modaalia.
const KurssinTietojenMuokkausModaali = <T extends KurssiData>({
  form,
  kurssiPath,
  kurssi,
  paikallinen,
  onClose
}: {
  form: FormModel<LukionOpiskeluoikeus>
  kurssiPath: FormOptic<LukionOpiskeluoikeus, T>
  kurssi: T
  paikallinen: boolean
  onClose: () => void
}) => {
  const arviointiIndex = parasArviointiIndex(kurssi.arviointi || [])
  return (
    <Modal onClose={onClose}>
      <ModalTitle>{t(kurssi.koulutusmoduuli.tunniste.nimi)}</ModalTitle>
      <ModalBody>
        <KeyValueTable>
          <KeyValueRow localizableLabel="Nimi" innerKeyValueTable>
            <TestIdText id="nimi">
              {t(kurssi.koulutusmoduuli.tunniste.nimi)}
            </TestIdText>
          </KeyValueRow>
          <KeyValueRow localizableLabel="Pakollinen" innerKeyValueTable>
            <PakollinenField
              form={form}
              path={kurssiPath.prop('koulutusmoduuli').prop('pakollinen')}
            />
          </KeyValueRow>
          <KeyValueRow localizableLabel="Laajuus" innerKeyValueTable>
            <FormField
              form={form}
              path={kurssiPath.prop('koulutusmoduuli').prop('laajuus')}
              view={LaajuusView}
              edit={LaajuusOpintopisteissäEdit}
              testId="laajuus"
            />
          </KeyValueRow>
          <KeyValueRow localizableLabel="Suorituskieli" innerKeyValueTable>
            <SuorituskieliField
              form={form}
              path={kurssiPath.prop('suorituskieli')}
            />
          </KeyValueRow>
          <KeyValueRow localizableLabel="Tunnustettu" innerKeyValueTable>
            <FormField
              form={form}
              path={kurssiPath.prop('tunnustettu')}
              optional
              view={OsaamisenTunnustusView}
              edit={OsaamisenTunnustusEdit}
              editProps={{
                createEmptyTunnustus: () =>
                  OsaamisenTunnustaminen({ selite: finnish('') })
              }}
            />
          </KeyValueRow>
          <KeyValueRow localizableLabel="Arvosana" innerKeyValueTable>
            <FormField
              form={form}
              path={kurssiPath.prop('arviointi')}
              optional
              view={ParasArvosanaKoodiarvoView}
              edit={ParasArvosanaEdit}
              editProps={{
                suoritusClassName: kurssi.$class,
                format: koodiarvoOnly
              }}
              testId="arvosana"
            />
          </KeyValueRow>
          {arviointiIndex !== undefined && (
            <KeyValueRow localizableLabel="Arviointipäivä" innerKeyValueTable>
              <FormField
                form={form}
                path={(
                  kurssiPath
                    .prop('arviointi')
                    .optional()
                    .at(arviointiIndex) as unknown as FormOptic<
                    LukionOpiskeluoikeus,
                    { päivä: string }
                  >
                ).prop('päivä')}
                view={DateView}
                edit={DateEdit}
                testId="arviointipaiva"
              />
            </KeyValueRow>
          )}
          {paikallinen && (
            <KeyValueRow localizableLabel="Kuvaus" innerKeyValueTable>
              <FormField
                form={form}
                path={(
                  kurssiPath.prop('koulutusmoduuli') as unknown as FormOptic<
                    LukionOpiskeluoikeus,
                    LukionPaikallinenOpintojakso2019
                  >
                ).prop('kuvaus')}
                view={LocalizedTextView}
                edit={LocalizedTextEdit}
                editProps={{ large: true }}
                testId="kuvaus"
              />
            </KeyValueRow>
          )}
        </KeyValueTable>
      </ModalBody>
      <ModalFooter>
        <FlatButton onClick={onClose} testId="sulje">
          {t('Sulje')}
        </FlatButton>
      </ModalFooter>
    </Modal>
  )
}

// Jaettu sekä oppiaineen (OppiaineRivi) että lukiodiplomien/muiden
// suoritusten/teemaopintojen (MuuSuoritusRivi) kurssilistalle - rakenne on
// identtinen molemmissa konteksteissa, vain ylätason suoritustyyppi (TParent)
// ja kurssityyppi (T) eroavat, siksi renderLisaaModal-propilla annetaan
// kontekstikohtainen "Lisää osasuoritus" -modaali (UusiLukionModuuli vs.
// UusiLukionMuuSuoritusModuuli).
//
// TParent ei kavenna mitään KurssiLista: n sisällä (parentPath.prop(...)
// joudutaan silti kavantamaan "as unknown as" -castilla, koska optics-ts:n
// FormOptic ei tue union-polkujen kavennusta) - se on silti tarpeellinen,
// jotta kutsujat voivat antaa oman (oppiaineen/muun suorituksen) FormOpticinsa
// sellaisenaan: FormOptic ei ole kovariantti propertyn suhteen (vahvistettu
// tsc:llä), joten löyhempi "FormOptic<LukionOpiskeluoikeus, unknown>" -
// parametrityyppi ei hyväksyisi kutsujien tarkempia FormOpticeja ilman omaa
// castiaan kutsupaikalla.
function KurssiLista<
  TParent extends { osasuoritukset?: T[] },
  T extends KurssiData
>({
  form,
  parentPath,
  kurssit,
  renderLisaaModal
}: {
  form: FormModel<LukionOpiskeluoikeus>
  parentPath: FormOptic<LukionOpiskeluoikeus, TParent>
  kurssit: T[]
  renderLisaaModal: (props: {
    olemassaOlevatKoodiarvot: string[]
    onAdd: (uusi: T) => void
    onClose: () => void
  }) => React.ReactNode
}) {
  const [lisääAuki, setLisääAuki] = useState(false)

  // Koko kurssit-taulukon optiikka - tarvitaan sekä lisäykseen (appendOptional)
  // että poistoon (deleteAt), toisin kuin yksittäisen kurssin .at(i)-poluille.
  const kurssitPath = parentPath.prop('osasuoritukset') as unknown as FormOptic<
    LukionOpiskeluoikeus,
    T[] | undefined
  >

  return (
    <span className="kurssit">
      {kurssit.length > 0 && (
        <ul>
          {kurssit.map((kurssi, i) => {
            const paikallinen =
              isLukionPaikallisenOpintojaksonSuoritus2019(kurssi)
            // TestIdLayer id={i} tarvitaan myös tällä tasolla, kuten
            // oppiainerivien listalla ylempänä - muuten saman oppiaineen
            // kaikki kurssit jakaisivat saman "arvosana"-testitunnisteen.
            const kurssiPath = parentPath
              .prop('osasuoritukset')
              .optional()
              .at(i) as unknown as FormOptic<LukionOpiskeluoikeus, T>
            return (
              <TestIdLayer id={i} key={i}>
                <li className="kurssi">
                  <KurssiTunnisteJaInfoPopup
                    form={form}
                    kurssiPath={kurssiPath}
                    kurssi={kurssi}
                    paikallinen={paikallinen}
                  />
                  {paikallinen && (
                    <sup className="footnote-hint">{PAIKALLINEN_HINT}</sup>
                  )}
                  {form.editMode && (
                    <IconButton
                      charCode={CHARCODE_REMOVE}
                      label={t('Poista')}
                      size="input"
                      onClick={() =>
                        form.updateAt(kurssitPath, (os) =>
                          deleteAt(i)(os || [])
                        )
                      }
                      testId="poisto"
                    />
                  )}
                  {/* Ei edit-propertya: arvosana näkyy rivillä aina pelkkänä
                      katseluna (myös muokkaustilassa), jotta sen näkee
                      avaamatta modaalia - itse muokkaus tapahtuu
                      KurssinTietojenMuokkausModaalissa (ks. yllä). */}
                  <div className="arvosana">
                    <FormField
                      form={form}
                      path={kurssiPath.prop('arviointi')}
                      optional
                      view={ParasArvosanaKoodiarvoView}
                      testId="arvosana"
                    />
                  </div>
                  {/* Pakollinen/Suorituskieli/Tunnustettu ovat muokattavissa
                      KurssinTietojenMuokkausModaalin kautta (ks.
                      KurssiTunnisteJaInfoPopup) - katselutilassa näytetään
                      tässä vain jo asetetut arvot, kuten ennenkin. */}
                  {!form.editMode && kurssi.suorituskieli && (
                    <span className="suorituskieli">
                      {t('Suorituskieli')}
                      {': '}
                      <SuorituskieliField
                        form={form}
                        path={kurssiPath.prop('suorituskieli')}
                      />
                    </span>
                  )}
                  {!form.editMode && kurssi.tunnustettu && (
                    <FormField
                      form={form}
                      path={kurssiPath.prop('tunnustettu')}
                      optional
                      view={OsaamisenTunnustusView}
                      edit={OsaamisenTunnustusEdit}
                      editProps={{
                        createEmptyTunnustus: () =>
                          OsaamisenTunnustaminen({ selite: finnish('') })
                      }}
                    />
                  )}
                </li>
              </TestIdLayer>
            )
          })}
        </ul>
      )}
      {form.editMode && (
        <>
          <FlatButton
            onClick={() => setLisääAuki(true)}
            testId="lisaa-osasuoritus"
          >
            {t('Lisää osasuoritus')}
          </FlatButton>
          {lisääAuki &&
            renderLisaaModal({
              olemassaOlevatKoodiarvot: kurssit.map(
                (k) => k.koulutusmoduuli.tunniste.koodiarvo
              ),
              onAdd: (uusi) => {
                form.updateAt(kurssitPath, (os) => appendOptional(uusi)(os))
                setLisääAuki(false)
              },
              onClose: () => setLisääAuki(false)
            })}
        </>
      )}
    </span>
  )
}

// Lukiodiplomit/muut suoritukset/teemaopinnot -rivi (MuidenLukioOpintojenSuoritus2019)
// - sisarluokka LukionOppiaineenSuoritus2019:lle, ei sen aliluokka. Paljon
// suppeampi kuin OppiaineRivi: ei ole Pakollinen-, Suorituskieli- eikä
// "Suoritettu erityisenä tutkintona" -kenttiä (MuutSuorituksetTaiVastaavat2019
// ei peri niitä), eikä nimeä voi muokata (nimi tulee kiinteästä 3-arvoisesta
// koodistosta, joka valitaan vasta rivin LISÄYSHETKELLÄ - ks. UusiMuuSuoritus).
// Suoritus on Arvioinniton (ei omaa arvosanaa, arvosana-sarake jää tyhjäksi -
// vain kurssien omat arvosanat näkyvät) ja Vahvistukseton (suoritusValmis
// palauttaa aina true Arvioinniton-suorituksille, ks. util/suoritus.ts).
const MuuSuoritusRivi: React.FC<{
  form: FormModel<LukionOpiskeluoikeus>
  muuSuoritusPath: FormOptic<
    LukionOpiskeluoikeus,
    MuidenLukioOpintojenSuoritus2019
  >
  suoritus: MuidenLukioOpintojenSuoritus2019
  onRemove: () => void
}> = ({ form, muuSuoritusPath, suoritus, onRemove }) => {
  const kurssit = suoritus.osasuoritukset || []
  const { arvioitu, hyväksytysti } = arvioituLaajuus(kurssit)
  const valmis = suoritusValmis(suoritus)

  return (
    <tr data-koodiarvo={suoritus.koulutusmoduuli.tunniste.koodiarvo}>
      <td className="LukionOppiaineet__tila">
        {valmis ? (
          <span title={t('Suoritus valmis')}>
            <Icon charCode={CHARCODE_VALMIS} />
          </span>
        ) : (
          <span title={t('Suoritus kesken')}>
            <Icon charCode={CHARCODE_KESKEN} />
          </span>
        )}
      </td>
      <td className="LukionOppiaineet__oppiaine">
        <TestIdText id="nimi" className="LukionOppiaineet__nimi">
          {t(suoritus.koulutusmoduuli.tunniste.nimi)}
        </TestIdText>
        {(kurssit.length > 0 || form.editMode) && (
          <KurssiLista
            form={form}
            parentPath={muuSuoritusPath}
            kurssit={kurssit}
            renderLisaaModal={({
              olemassaOlevatKoodiarvot,
              onAdd,
              onClose
            }) => (
              <UusiLukionMuuSuoritusModuuli
                olemassaOlevatKoodiarvot={olemassaOlevatKoodiarvot}
                onAdd={onAdd}
                onClose={onClose}
              />
            )}
          />
        )}
      </td>
      <td className="LukionOppiaineet__laajuus">
        <TestIdText id="laajuus.arvioitu">{formatNumber(arvioitu)}</TestIdText>
      </td>
      <td className="LukionOppiaineet__laajuus">
        <TestIdText id="laajuus.hyvaksytystiArvioitu">
          {formatNumber(hyväksytysti)}
        </TestIdText>
      </td>
      <td className="LukionOppiaineet__arvosana" />
      {form.editMode && (
        <td className="LukionOppiaineet__poisto">
          <IconButton
            charCode={CHARCODE_REMOVE}
            label={t('Poista')}
            onClick={onRemove}
            testId="poisto"
          />
        </td>
      )}
    </tr>
  )
}

// Rinnakkainen KurssiListan kanssa "muissa opinnoissa" -kontekstia varten -
// sama ulkoasu/toiminnallisuus (arvosana, osaamisen tunnustaminen, poisto,
// lisäys), mutta kurssien koulutusmoduuliunioni ja lisäysmodaali
// (UusiLukionMuuSuoritusModuuli) ovat omansa, koska LD/MS/TO-rivin alla ei ole
// oppiainetta jonka koodiarvon mukaan moduulin tyyppi (ÄI/vieras kieli/muu)
// voisi määräytyä.
// Lukiodiplomit (LD) / muut suoritukset (MS) / teemaopinnot (TO) - kiinteä
// 3-arvoinen koodisto (lukionmuutopinnot), suodatettuna jo lisättyihin
// nähden. Toisin kuin oppiaineilla, näitä ei voi olla paikallisia eikä niiden
// nimeä voi muokata lisäyksen jälkeen - siksi ei "Lisää..."-vaihtoehtoa kuten
// UusiLukionOppiaineessa.
const uusiMuuSuoritus = (
  tunniste: Koodistokoodiviite<'lukionmuutopinnot'>
): MuidenLukioOpintojenSuoritus2019 => {
  const koulutusmoduuli =
    tunniste.koodiarvo === 'LD'
      ? Lukiodiplomit2019({ tunniste: tunniste as any })
      : tunniste.koodiarvo === 'TO'
        ? TemaattisetOpinnot2019({ tunniste: tunniste as any })
        : MuutLukionSuoritukset2019({ tunniste: tunniste as any })
  return MuidenLukioOpintojenSuoritus2019({ koulutusmoduuli })
}

const UusiMuuSuoritus: React.FC<{
  form: FormModel<LukionOpiskeluoikeus>
  osasuorituksetPath: FormOptic<
    LukionOpiskeluoikeus,
    | Array<LukionOppiaineenSuoritus2019 | MuidenLukioOpintojenSuoritus2019>
    | undefined
  >
  olemassaOlevatKoodiarvot: string[]
}> = ({ form, osasuorituksetPath, olemassaOlevatKoodiarvot }) => {
  const koodisto = useKoodisto('lukionmuutopinnot')

  if (!form.editMode) return null

  const options: SelectOption<Koodistokoodiviite<'lukionmuutopinnot'>>[] = (
    koodisto || []
  )
    .filter((k) => !olemassaOlevatKoodiarvot.includes(k.koodiviite.koodiarvo))
    .map((k) => ({
      key: k.koodiviite.koodiarvo,
      label: t(k.koodiviite.nimi),
      value: k.koodiviite
    }))

  const onChange = (
    option?: SelectOption<Koodistokoodiviite<'lukionmuutopinnot'>>
  ) => {
    if (!option?.value) return
    const uusi = uusiMuuSuoritus(option.value)
    form.updateAt(osasuorituksetPath, (os) =>
      appendOptional<
        LukionOppiaineenSuoritus2019 | MuidenLukioOpintojenSuoritus2019
      >(uusi)(os)
    )
  }

  return (
    <Select
      placeholder={t('Lisää muu suoritus')}
      options={options}
      hideEmpty
      onChange={onChange}
      testId="uusi-muu-suoritus"
    />
  )
}

const onPaikallisia = (
  osasuoritukset: LukionOppiaineenSuoritus2019[],
  muutSuoritukset: MuidenLukioOpintojenSuoritus2019[]
) =>
  osasuoritukset.some(
    (s) =>
      isPaikallinenLukionOppiaine2019(s.koulutusmoduuli) ||
      (s.osasuoritukset || []).some(isLukionPaikallisenOpintojaksonSuoritus2019)
  ) ||
  muutSuoritukset.some((m) =>
    (m.osasuoritukset || []).some(isLukionPaikallisenOpintojaksonSuoritus2019)
  )

const KurssienLaajuusYhteensä: React.FC<{
  osasuoritukset: LukionOppiaineenSuoritus2019[]
  muutSuoritukset: MuidenLukioOpintojenSuoritus2019[]
  päätasonSuoritusErityinen: boolean
}> = ({ osasuoritukset, muutSuoritukset, päätasonSuoritusErityinen }) => {
  // Erityisenä tutkintona suoritettu oppiaine lasketaan mukaan ITSENÄÄN, ei
  // kurssiensa kautta - samalla säännöllä kuin oppiaineenArvioituLaajuus.
  // Lukiodiplomit/muut suoritukset/teemaopinnot -riveillä ei ole erityisen
  // tutkinnon käsitettä lainkaan - niiden omat osasuoritukset lasketaan aina
  // mukaan sellaisenaan.
  const kaikkiItemit: LaajuusArvioitavaItem[] = [
    ...osasuoritukset.flatMap((o): LaajuusArvioitavaItem[] =>
      isErityinenTutkinto(o, päätasonSuoritusErityinen)
        ? [o]
        : o.osasuoritukset || []
    ),
    ...muutSuoritukset.flatMap(
      (m): LaajuusArvioitavaItem[] => m.osasuoritukset || []
    )
  ]
  if (!kaikkiItemit.some((i) => itemArviointi(i) !== undefined)) return null
  const { arvioitu, hyväksytysti } = arvioituLaajuus(kaikkiItemit)

  const arvioituYhteensä = removeFloatingPointDrift(arvioitu)
  const hyväksytystiYhteensä = removeFloatingPointDrift(hyväksytysti)

  return (
    <p className="kurssit-yhteensä">
      <TestIdText id="laajuus.arvioitu.yhteensa">
        {t('Arvioitujen osasuoritusten laajuus yhteensä')}
        {': '}
        {formatNumber(arvioituYhteensä)}
      </TestIdText>
      <TestIdText id="laajuus.hyvaksytystiArvioitu.yhteensa">
        {t('Hyväksytysti arvioitujen osasuoritusten laajuus yhteensä')}
        {': '}
        {formatNumber(hyväksytystiYhteensä)}
      </TestIdText>
    </p>
  )
}

// Vain "tasaisen" muotoiset valtakunnalliset oppiaineet - pelkkä tunniste +
// pakollinen-lippu, ei muita pakollisia alikenttiä. Kieliaineet (AI, A/B1/B2/
// B3/AOM) ja matematiikka (MA, tarvitsee oppimäärä-valinnan) jätetään
// lisäysvalikon ulkopuolelle - niiden lisääminen vaatisi toisen valinnan
// (kieli/oppimäärä) heti perään, mitä ei ole vielä toteutettu. Olemassa
// olevan kieliaineen kielen voi jo muokata (ks. OppiaineNimi).
const LISATTAVAT_VALTAKUNNALLISET_KOODIARVOT = [
  'BI',
  'ET',
  'FI',
  'FY',
  'GE',
  'HI',
  'KE',
  'KU',
  'LI',
  'MU',
  'OP',
  'PS',
  'TE',
  'YH',
  'KT'
]

// Pakollinen: true vastaa vanhan UI:n UusiOppiaineDropdown.jsx:n
// setPakollisuus-ylikirjoitusta ("Lisää oppiaine" asettaa pakollinen=true
// kaikille uusille oppiaineille, myös paikallisille), ei kunkin tyypin
// omaa skeemaoletusta.
const uusiValtakunnallinenLukionOppiaine = (
  tunniste: Koodistokoodiviite<'koskioppiaineetyleissivistava'>
) =>
  tunniste.koodiarvo === 'KT'
    ? LukionUskonto2019({ tunniste: tunniste as any, pakollinen: true })
    : LukionMuuValtakunnallinenOppiaine2019({
        tunniste: tunniste as any,
        pakollinen: true
      })

const UUSI_PAIKALLINEN_OPPIAINE_KEY = 'uusi-paikallinen-oppiaine'

const PAIKALLINEN_OPPIAINE_PREF_TYPE = 'paikallinenlukionoppiaine2019'

// Lisää oppiaine -valikko koodistosta, suodatettuna jo lisättyihin nähden,
// plus "Lisää..." paikallisen oppiaineen luontiin ja organisaatiolle aiemmin
// tallennetut paikalliset oppiaineet uudelleenkäytettäväksi - sama idea kuin
// perusopetus-v2/PerusopetuksenOppiaineet.tsx:n UusiPerusopetuksenOppiaine.
// Itse tallennus tapahtuu LukioEditor.tsx:n
// useSyncPaikallisetOppiaineetJaKurssit-hookissa suorituksen tallennuksen
// jälkeen, ei täällä.
const UusiLukionOppiaine: React.FC<{
  form: FormModel<LukionOpiskeluoikeus>
  osasuorituksetPath: FormOptic<
    LukionOpiskeluoikeus,
    LukionOppiaineenSuoritus2019[] | undefined
  >
  olemassaOlevatKoodiarvot: string[]
}> = ({ form, osasuorituksetPath, olemassaOlevatKoodiarvot }) => {
  const koodisto = useKoodisto(
    'koskioppiaineetyleissivistava',
    LISATTAVAT_VALTAKUNNALLISET_KOODIARVOT
  )
  const { organisaatio } = useContext(OpiskeluoikeusContext)
  const { preferences: paikalliset, remove: removePaikallinen } =
    usePreferences<PaikallinenLukionOppiaine2019>(
      organisaatio?.oid,
      PAIKALLINEN_OPPIAINE_PREF_TYPE
    )

  if (!form.editMode) return null

  const options: SelectOption<
    Koodistokoodiviite<'koskioppiaineetyleissivistava'> | PaikallinenKoodi
  >[] = [
    ...(koodisto || [])
      .filter((k) => !olemassaOlevatKoodiarvot.includes(k.koodiviite.koodiarvo))
      .map((k) => ({
        key: k.koodiviite.koodiarvo,
        label: t(k.koodiviite.nimi),
        value: k.koodiviite
      })),
    ...(paikalliset || [])
      .filter((p) => !olemassaOlevatKoodiarvot.includes(p.tunniste.koodiarvo))
      .map((p) => ({
        key: `paikallinen-${p.tunniste.koodiarvo}`,
        label: t(p.tunniste.nimi),
        value: p.tunniste,
        removable: true
      })),
    {
      key: UUSI_PAIKALLINEN_OPPIAINE_KEY,
      label: t('Lisää') + '...',
      isAddNew: true,
      value: undefined
    }
  ]

  const onChange = (
    option?: SelectOption<
      Koodistokoodiviite<'koskioppiaineetyleissivistava'> | PaikallinenKoodi
    >
  ) => {
    if (!option) return

    if (option.key === UUSI_PAIKALLINEN_OPPIAINE_KEY) {
      const newSuoritus = LukionOppiaineenSuoritus2019({
        koulutusmoduuli: PaikallinenLukionOppiaine2019({
          tunniste: PaikallinenKoodi({ koodiarvo: '', nimi: finnish('') }),
          kuvaus: finnish(''),
          // Vanha UI ylikirjoittaa pakollinen=true myös paikallisille
          // oppiaineille (setPakollisuus), vaikka skeeman oma oletus on
          // false - ks. uusiValtakunnallinenLukionOppiaine.
          pakollinen: true
        })
      })
      form.updateAt(osasuorituksetPath, (os) => appendOptional(newSuoritus)(os))
      return
    }

    if (option.key.startsWith('paikallinen-')) {
      const stored = (paikalliset || []).find(
        (p) => p.tunniste.koodiarvo === option.value?.koodiarvo
      )
      if (stored) {
        const newSuoritus = LukionOppiaineenSuoritus2019({
          koulutusmoduuli: stored
        })
        form.updateAt(osasuorituksetPath, (os) =>
          appendOptional(newSuoritus)(os)
        )
      }
      return
    }

    if (option.value) {
      const newSuoritus = LukionOppiaineenSuoritus2019({
        koulutusmoduuli: uusiValtakunnallinenLukionOppiaine(
          option.value as Koodistokoodiviite<'koskioppiaineetyleissivistava'>
        )
      })
      form.updateAt(osasuorituksetPath, (os) => appendOptional(newSuoritus)(os))
    }
  }

  const onRemove = (
    option: SelectOption<
      Koodistokoodiviite<'koskioppiaineetyleissivistava'> | PaikallinenKoodi
    >
  ) => {
    if (option.key.startsWith('paikallinen-') && option.value) {
      removePaikallinen(option.value.koodiarvo)
    }
  }

  return (
    <Select
      placeholder={t('Lisää oppiaine')}
      options={options}
      hideEmpty
      onChange={onChange}
      onRemove={onRemove}
      testId="uusi-oppiaine"
    />
  )
}

// Kieliaineen (esim. "Äidinkieli ja kirjallisuus") nimi koostuu kahdesta
// osasta: kiinteästä oppiaineen nimestä ja muokattavasta kielestä. Jälkimmäinen
// tulee suoraan koodistosta isolla alkukirjaimella ("Suomen kieli ja
// kirjallisuus"), eikä sitä pienennetä - sama kuin vanhassa käyttöliittymässä.
const OppiaineNimi: React.FC<{
  form: FormModel<LukionOpiskeluoikeus>
  oppiainePath: FormOptic<LukionOpiskeluoikeus, LukionOppiaineenSuoritus2019>
  suoritus: LukionOppiaineenSuoritus2019
}> = ({ form, oppiainePath, suoritus }) => {
  const koodistoUri = kieliaineKoodistoUri(suoritus.koulutusmoduuli)
  const paikallinenKoulutusmoduuli = isPaikallinenLukionOppiaine2019(
    suoritus.koulutusmoduuli
  )
    ? suoritus.koulutusmoduuli
    : undefined
  const paikallinenPath = paikallinenKoulutusmoduuli
    ? (oppiainePath.prop('koulutusmoduuli') as unknown as FormOptic<
        LukionOpiskeluoikeus,
        PaikallinenLukionOppiaine2019
      >)
    : undefined
  // Vanhan UI:n LukionOppiaine.jsx:n Nimi-fragmentti: ", valinnainen" näytetään
  // aina paitsi kun nimen tilalla on jo oma kieli-/oppimäärä-/koodi-dropdown
  // tai -tekstikenttä muokkaustilassa (kieliaine, matematiikka tai paikallinen
  // oppiaine - kukin oma renderöintipolkunsa alla).
  const nimiKorvattuMuokkauskentällä =
    form.editMode &&
    (!!paikallinenPath ||
      isLukionMatematiikka2019(suoritus.koulutusmoduuli) ||
      !!koodistoUri)
  const näytäValinnainen =
    !nimiKorvattuMuokkauskentällä && !suoritus.koulutusmoduuli.pakollinen

  return (
    <>
      <TestIdText id="nimi" className="LukionOppiaineet__nimi">
        {paikallinenPath && form.editMode ? (
          <>
            <FormField
              form={form}
              path={paikallinenPath.prop('tunniste').prop('koodiarvo')}
              view={() => null}
              edit={TextEdit}
              editProps={{ placeholder: t('Koodi') }}
              testId="paikallinenKoodi"
            />
            <FormField
              form={form}
              path={paikallinenPath.prop('tunniste').prop('nimi')}
              view={() => null}
              edit={LocalizedTextEdit}
              editProps={{ placeholder: t('Oppiaineen nimi') }}
              testId="paikallinenNimi"
            />
          </>
        ) : isLukionMatematiikka2019(suoritus.koulutusmoduuli) ? (
          // Matematiikan oppimäärän (pitkä/lyhyt/yhteinen opintokokonaisuus)
          // koodistoarvon oma nimi on jo valmiiksi muotoa "Matematiikka,
          // lyhyt oppimäärä" (vahvistettu livenä koodistosta ja vanhasta
          // käyttöliittymästä) - ei siis liitetä perään kuten kieliaineen
          // kieltä, vaan korvataan koko nimi sillä.
          <FormField
            form={form}
            path={(
              oppiainePath.prop('koulutusmoduuli') as unknown as FormOptic<
                LukionOpiskeluoikeus,
                LukionMatematiikka2019
              >
            ).prop('oppimäärä')}
            view={OppimääräNimiView}
            testId="oppimäärä"
          />
        ) : (
          t(suoritus.koulutusmoduuli.tunniste.nimi)
        )}
        {koodistoUri && (
          <>
            {', '}
            <FormField
              form={form}
              path={(
                oppiainePath.prop('koulutusmoduuli') as unknown as FormOptic<
                  LukionOpiskeluoikeus,
                  LukionÄidinkieliJaKirjallisuus2019
                >
              ).prop('kieli')}
              view={KieliNimiView}
              edit={KoodistoEdit}
              editProps={{ koodistoUri }}
              testId="kieli"
            />
          </>
        )}
        {näytäValinnainen && `, ${t('valinnainen')}`}
      </TestIdText>
      {paikallinenPath &&
        (form.editMode || t(paikallinenKoulutusmoduuli!.kuvaus)) && (
          <div className="LukionOppiaineet__kuvaus">
            <FormField
              form={form}
              path={paikallinenPath.prop('kuvaus')}
              view={LocalizedTextView}
              edit={LocalizedTextEdit}
              editProps={{ large: true, ariaLabel: t('Kuvaus') }}
              testId="paikallinenKuvaus"
            />
          </div>
        )}
    </>
  )
}
