import React, { useState } from 'react'
import { FormModel, FormOptic } from '../components-v2/forms/FormModel'
import { ActivePäätasonSuoritus } from '../components-v2/containers/EditorContainer'
import { LukionOpiskeluoikeus } from '../types/fi/oph/koski/schema/LukionOpiskeluoikeus'
import { LocalizedString } from '../types/fi/oph/koski/schema/LocalizedString'
import { Koodistokoodiviite } from '../types/fi/oph/koski/schema/Koodistokoodiviite'
import {
  KeyColumnedValuesRow,
  KeyValueRow,
  KeyValueTable
} from '../components-v2/containers/KeyValueTable'
import { FormField } from '../components-v2/forms/FormField'
import {
  OrganisaatioEdit,
  OrganisaatioView
} from '../components-v2/opiskeluoikeus/OrganisaatioField'
import {
  KoodistoEdit,
  KoodistoView
} from '../components-v2/opiskeluoikeus/KoodistoField'
import { KoodistoSelect } from '../components-v2/opiskeluoikeus/KoodistoSelect'
import {
  PerusteEdit,
  PerusteView
} from '../components-v2/opiskeluoikeus/PerusteField'
import {
  koodiarvoOnly,
  ParasArvosanaEdit,
  ParasArvosanaKoodiarvoView,
  ParasArvosanaPäiväView
} from '../components-v2/opiskeluoikeus/ArvosanaField'
import {
  LaajuusView,
  LaajuusEdit
} from '../components-v2/opiskeluoikeus/LaajuusField'
import { DateView, DateEdit } from '../components-v2/controls/DateField'
import { SuorituksenVahvistusField } from '../components-v2/opiskeluoikeus/SuorituksenVahvistus'
import { TestIdLayer, TestIdText } from '../appstate/useTestId'
import {
  LocalizedTextEdit,
  LocalizedTextView
} from '../components-v2/controls/LocalizedTestField'
import { TextEdit, TextView } from '../components-v2/controls/TextField'
import { FlatButton } from '../components-v2/controls/FlatButton'
import { RaisedButton } from '../components-v2/controls/RaisedButton'
import { IconButton } from '../components-v2/controls/IconButton'
import { CHARCODE_REMOVE } from '../components-v2/texts/Icon'
import { Removable } from '../components-v2/controls/Removable'
import {
  Modal,
  ModalBody,
  ModalFooter,
  ModalTitle
} from '../components-v2/containers/Modal'
import { DateInput } from '../components-v2/controls/DateInput'
import { parasArviointi } from '../util/arvioinnit'
import { t } from '../i18n/i18n'
import {
  isLukionOppimääränSuoritus2019,
  LukionOppimääränSuoritus2019
} from '../types/fi/oph/koski/schema/LukionOppimaaranSuoritus2019'
import {
  isLukionOppiaineidenOppimäärienSuoritus2019,
  LukionOppiaineidenOppimäärienSuoritus2019
} from '../types/fi/oph/koski/schema/LukionOppiaineidenOppimaarienSuoritus2019'
import { LukionOmanÄidinkielenOpinnot } from '../types/fi/oph/koski/schema/LukionOmanAidinkielenOpinnot'
import { LukionOmanÄidinkielenOpintojenOsasuoritus } from '../types/fi/oph/koski/schema/LukionOmanAidinkielenOpintojenOsasuoritus'
import { LaajuusOpintopisteissä } from '../types/fi/oph/koski/schema/LaajuusOpintopisteissa'
import { HenkilövahvistusPaikkakunnalla } from '../types/fi/oph/koski/schema/HenkilovahvistusPaikkakunnalla'
import { PuhviKoe2019 } from '../types/fi/oph/koski/schema/PuhviKoe2019'
import { SuullisenKielitaidonKoe2019 } from '../types/fi/oph/koski/schema/SuullisenKielitaidonKoe2019'
import { LukiodiplominSuoritusJaArviointi } from '../types/fi/oph/koski/schema/LukiodiplominSuoritusJaArviointi'
import { appendOptional, deleteAt } from '../util/array'
import { ISO2FinnishDate } from '../date/date'
import { BooleanEdit } from '../components-v2/opiskeluoikeus/BooleanField'
import { UusiOmanÄidinkielenOpinnotModal } from './UusiOmanAidinkielenOpinnot'
import { UusiOmanÄidinkielenOpintojenKurssi } from './UusiOmanAidinkielenOpintojenKurssi'

type LukionSuorituksenTiedotProps = {
  form: FormModel<LukionOpiskeluoikeus>
  päätasonSuoritus: ActivePäätasonSuoritus<LukionOpiskeluoikeus>
}

export type TuettuSuoritus =
  LukionOppimääränSuoritus2019 | LukionOppiaineidenOppimäärienSuoritus2019

// path.prop(...) tällä unionilla ei vaadi "as unknown as FormOptic<...>"
// -kavennusta kentille, jotka molemmat TuettuSuoritus-jäsenet jakavat
// identtisenä (esim. puhviKoe, suullisenKielitaidonKokeet, lukiodiplomit2019
// - kaikki kolme periytyvät samasta LukionPäätasonSuoritus2019-traitista).
// Vain jommallekummalle ainoalle ominaiset kentät (esim.
// suoritettuErityisenäTutkintona, koulutusmoduuli.perusteenDiaarinumero)
// vaativat kavennuksen tiettyyn suoritustyyppiin.
export const isTuettuSuoritus = (suoritus: {
  $class: string
}): suoritus is TuettuSuoritus =>
  isLukionOppimääränSuoritus2019(suoritus) ||
  isLukionOppiaineidenOppimäärienSuoritus2019(suoritus)

// Yhdistää tässä tiedostossa toistuvan arviointiasteikkoyleissivistava-
// koodiston valinnan (koodiarvoOnly-muotoilu, numerojärjestys) yhteen
// paikkaan - sekä KoodistoSelectin koodistoUri/format/sort-toiston että
// sen onSelect-paluuarvon kavennuksen "as X['arvosana']" tyyliin: komponentin
// oma tyyppiparametri ei kavenna koodiarvoa, vaikka kutsupaikka tietää
// tarkemman skeematyypin.
const ArvosanaKoodistoSelect = <A extends Koodistokoodiviite>({
  value,
  onSelect,
  testId,
  koodiarvot,
  skipAutoFocus
}: {
  value?: string
  onSelect: (arvosana?: A) => void
  testId: string
  koodiarvot?: string[]
  skipAutoFocus?: boolean
}) => (
  <KoodistoSelect
    koodistoUri="arviointiasteikkoyleissivistava"
    format={koodiarvoOnly}
    sort
    koodiarvot={koodiarvot}
    skipAutoFocus={skipAutoFocus}
    onSelect={(k) => onSelect(k as A | undefined)}
    value={value}
    testId={testId}
  />
)

// Suorituksen otsikkokentät - muokattavissa kuten vanhassa käyttöliittymässä
// (suoritus/SuoritusEditor.jsx:n yleinen PropertiesEditor ei rajoita näitä
// mitenkään erityisesti lukiolle, ks. suoritusEditorMapping.tsx).
// Poikkeus: koulusivistyskieli ei ole muokattavissa - ks. KoulusivistyskieliRow'n
// oma kommentti.
export const LukionSuorituksenTiedot: React.FC<
  LukionSuorituksenTiedotProps
> = ({ form, päätasonSuoritus }) => {
  const suoritus = päätasonSuoritus.suoritus

  if (!isTuettuSuoritus(suoritus)) {
    // Muut lukion suoritustyypit (2015, pre-IB, LUVA) eivät ole tuettuja.
    return null
  }

  const path = päätasonSuoritus.path as unknown as FormOptic<
    LukionOpiskeluoikeus,
    TuettuSuoritus
  >

  return (
    <>
      <KeyValueTable labelWidth={6}>
        <KoulutusRow suoritus={suoritus} form={form} path={path} />

        <KeyValueRow localizableLabel="Opetussuunnitelma">
          <FormField
            form={form}
            path={path.prop('oppimäärä')}
            view={KoodistoView}
            edit={KoodistoEdit}
            editProps={{ koodistoUri: 'lukionoppimaara' }}
            testId="oppimäärä"
          />
        </KeyValueRow>

        <KeyValueRow localizableLabel="Oppilaitos / toimipiste">
          <FormField
            form={form}
            path={path.prop('toimipiste')}
            view={OrganisaatioView}
            edit={OrganisaatioEdit}
            testId="toimipiste"
          />
        </KeyValueRow>

        {form.editMode && isLukionOppimääränSuoritus2019(suoritus) && (
          <KeyValueRow localizableLabel="Suoritettu erityisenä tutkintona">
            <FormField
              form={form}
              path={(
                path as unknown as FormOptic<
                  LukionOpiskeluoikeus,
                  LukionOppimääränSuoritus2019
                >
              ).prop('suoritettuErityisenäTutkintona')}
              view={() => null}
              edit={BooleanEdit}
              testId="suoritettuErityisenäTutkintona"
            />
          </KeyValueRow>
        )}

        {/* Skeeman oma otsikko tälle kentälle on "Opetuskieli", ei
        "Suorituskieli" (Lukio2019.scala: @Title("Opetuskieli") päätason
        suorituksen suorituskieli-kentällä) - eri otsikko kuin oppiaineen
        omalla suorituskieli-kentällä alempana. */}
        <KeyValueRow localizableLabel="Opetuskieli">
          <FormField
            form={form}
            path={path.prop('suorituskieli')}
            view={KoodistoView}
            edit={KoodistoEdit}
            editProps={{ koodistoUri: 'kieli' }}
            testId="suorituskieli"
          />
        </KeyValueRow>

        {isLukionOppimääränSuoritus2019(suoritus) && (
          <OmanÄidinkielenOpinnotRow
            form={form}
            path={
              path as unknown as FormOptic<
                LukionOpiskeluoikeus,
                LukionOppimääränSuoritus2019
              >
            }
            suoritus={suoritus}
          />
        )}

        <PuhviKoeRow form={form} path={path} puhviKoe={suoritus.puhviKoe} />

        {(form.editMode ||
          (suoritus.suullisenKielitaidonKokeet &&
            suoritus.suullisenKielitaidonKokeet.length > 0)) && (
          <SuullisenKielitaidonKokeetRow
            form={form}
            path={path}
            kokeet={suoritus.suullisenKielitaidonKokeet || []}
          />
        )}

        {(form.editMode ||
          (suoritus.lukiodiplomit2019 &&
            suoritus.lukiodiplomit2019.length > 0)) && (
          <LukiodiplomitRow
            form={form}
            path={path}
            diplomit={suoritus.lukiodiplomit2019 || []}
          />
        )}

        {(form.editMode || suoritus.todistuksellaNäkyvätLisätiedot) && (
          <KeyValueRow localizableLabel="Todistuksella näkyvät lisätiedot">
            <FormField
              form={form}
              path={path.prop('todistuksellaNäkyvätLisätiedot')}
              optional
              view={LocalizedTextView}
              viewProps={{ style: { whiteSpace: 'pre-line' } }}
              edit={LocalizedTextEdit}
              testId="todistuksellaNäkyvätLisätiedot"
            />
          </KeyValueRow>
        )}

        {(form.editMode || suoritus.ryhmä) && (
          <KeyValueRow localizableLabel="Ryhmä">
            <FormField
              form={form}
              path={path.prop('ryhmä')}
              optional
              view={TextView}
              edit={TextEdit}
              testId="ryhmä"
            />
          </KeyValueRow>
        )}

        {isLukionOppimääränSuoritus2019(suoritus) && (
          <KoulusivistyskieliRow suoritus={suoritus} />
        )}
      </KeyValueTable>

      <SuorituksenVahvistusField
        form={form}
        suoritusPath={päätasonSuoritus.path}
        organisaatio={form.state.oppilaitos}
        disableAdd
        vahvistusClass={HenkilövahvistusPaikkakunnalla.className}
      />
    </>
  )
}

const KoulutusRow: React.FC<{
  suoritus: TuettuSuoritus
  form: FormModel<LukionOpiskeluoikeus>
  path: FormOptic<LukionOpiskeluoikeus, TuettuSuoritus>
}> = ({ suoritus, form, path }) =>
  isLukionOppimääränSuoritus2019(suoritus) ? (
    <KeyColumnedValuesRow
      localizableName="Koulutus"
      columnSpans={{ default: [4, 2, '*'], phone: [24, 24, 24] }}
    >
      {[
        <TestIdText key="tunniste" id="koulutus">
          {t(suoritus.koulutusmoduuli.tunniste.nimi)}
        </TestIdText>,
        <TestIdText key="koodiarvo" id="koulutuskoodi">
          {suoritus.koulutusmoduuli.tunniste.koodiarvo}
        </TestIdText>,
        <FormField
          key="peruste"
          form={form}
          path={(
            path as unknown as FormOptic<
              LukionOpiskeluoikeus,
              LukionOppimääränSuoritus2019
            >
          )
            .prop('koulutusmoduuli')
            .prop('perusteenDiaarinumero')}
          optional
          view={PerusteView}
          edit={PerusteEdit}
          editProps={{ suorituksenTyyppi: suoritus.tyyppi.koodiarvo }}
        />
      ]}
    </KeyColumnedValuesRow>
  ) : (
    <KeyColumnedValuesRow
      localizableName="Koulutus"
      columnSpans={{ default: [4, 2, '*'], phone: [24, 24, 24] }}
    >
      {[
        <TestIdText key="tunniste" id="koulutus">
          {t(suoritus.tyyppi.nimi)}
        </TestIdText>,
        <TestIdText key="koodiarvo" id="koulutuskoodi">
          {suoritus.koulutusmoduuli.tunniste.koodiarvo}
        </TestIdText>,
        <FormField
          key="peruste"
          form={form}
          path={(
            path as unknown as FormOptic<
              LukionOpiskeluoikeus,
              LukionOppiaineidenOppimäärienSuoritus2019
            >
          )
            .prop('koulutusmoduuli')
            .prop('perusteenDiaarinumero')}
          optional
          view={PerusteView}
          edit={PerusteEdit}
          editProps={{ suorituksenTyyppi: suoritus.tyyppi.koodiarvo }}
        />
      ]}
    </KeyColumnedValuesRow>
  )

// Ei muokattavissa: koulusivistyskieli on backendissä laskettu kenttä, ei
// tallennettu - se johdetaan suorituksen pakollisen "Äidinkieli ja
// kirjallisuus" -oppiaineen kielestä (ks.
// YleissivistavaKoulutus.scala:KoulusivistyskieliKieliaineesta). Mikä tahansa
// arvo, jonka asiakas lähettää tälle kentälle, jää backendissä huomiotta -
// varmistettu live-testillä (lomake laski ja lähetti oikean kaksiarvoisen
// listan, mutta tallennuksen jälkeen palautui aina yksiarvoiseksi). Oikea
// tapa "muuttaa" koulusivistyskieltä on muuttaa ÄI-oppiaineen kieltä, joka on
// jo muokattavissa (ks. LukionOppiaineet.tsx:n kieliaineen kieli -slice).
const KoulusivistyskieliRow: React.FC<{
  suoritus: LukionOppimääränSuoritus2019
}> = ({ suoritus }) => {
  const valitut = suoritus.koulusivistyskieli || []
  if (valitut.length === 0) return null
  return (
    <KeyValueRow localizableLabel="Koulusivistyskieli">
      <TestIdText id="koulusivistyskieli">
        {valitut.map((k) => t(k.nimi)).join(', ')}
      </TestIdText>
    </KeyValueRow>
  )
}

// Oikea <table>, ei KeyValueTable: KeyValueTablen nimisarake on CSS-ruudukon
// kiinteä murto-osa (8/24) rivin koko leveydestä, kun taas vanhassa
// käyttöliittymässä nimisarake on sisällön mukaan mitoitettu (max-content,
// pisimmän nimen - "Arviointipäivä" - levyinen) ja arvosarake (tässä
// Osasuoritukset-taulukko) saa loput. Ruudukkoratkaisu teki Osasuoritukset-
// taulukosta sekä liian kapean (sisältö rivittyi) että liian vähän
// sisennetyn verrattuna vanhaan.
//
// Muokattavissa samalla mallilla kuin perusopetus-v2:n vastaava
// (PerusopetuksenSuorituksenTiedot.tsx:n TäydentävätOmanÄidinkielenOpinnotRow)
// - koko lohko voidaan lisätä/poistaa (Removable + modaali), ja lohkon kentät
// (kieli/arvosana/laajuus/arviointipäivä) ovat muokattavissa. Poikkeus: osa-
// suoritusten (kurssien) lisäys/poisto ja per-kurssi arvosana/suorituskieli-
// muokkaus eivät ole perusopetuksen puolella lainkaan (sillä ei ole
// osasuorituksia tässä kohdassa) - rakennettu tätä varten LukionOppiaineet.tsx:n
// jo todistetuilla ParasArvosanaEdit/appendOptional/deleteAt-malleilla.
// tunnustettu-kenttä jätetään tarkoituksella muokkaamattomaksi: se on
// @Hidden skeemassa (LukionOmanÄidinkielenOpintojenOsasuoritus, ainoa
// @Hidden tunnustettu-kenttä koko skeemassa) jo ennen tätä editoria,
// ja vanha käyttöliittymäkään ei näytä sitä edes katselussa.
const OmanÄidinkielenOpinnotRow: React.FC<{
  form: FormModel<LukionOpiskeluoikeus>
  path: FormOptic<LukionOpiskeluoikeus, LukionOppimääränSuoritus2019>
  suoritus: LukionOppimääränSuoritus2019
}> = ({ form, path, suoritus }) => {
  const [lisääOpinnotAuki, setLisääOpinnotAuki] = useState(false)
  const opinnot = suoritus.omanÄidinkielenOpinnot
  const opinnotPath = path.prop('omanÄidinkielenOpinnot')

  if (!form.editMode && !opinnot) return null

  if (form.editMode && !opinnot) {
    return (
      <KeyValueRow localizableLabel="Lukion oppimäärää täydentävät oman äidinkielen opinnot">
        <FlatButton
          testId="omanÄidinkielenOpinnot.lisää"
          onClick={() => setLisääOpinnotAuki(true)}
        >
          {t('Lisää täydentävät oman äidinkielen opinnot')}
        </FlatButton>
        {lisääOpinnotAuki && (
          <UusiOmanÄidinkielenOpinnotModal
            onClose={() => setLisääOpinnotAuki(false)}
            onSubmit={(uusi) => form.updateAt(opinnotPath, () => uusi)}
          />
        )}
      </KeyValueRow>
    )
  }

  if (!opinnot) return null

  const opinnotOptionalPath = opinnotPath.optional()
  const removeOpinnot = () => form.updateAt(opinnotPath, () => undefined)

  return (
    <KeyValueRow localizableLabel="Lukion oppimäärää täydentävät oman äidinkielen opinnot">
      <Removable
        isRemovable={form.editMode}
        onClick={removeOpinnot}
        testId="omanÄidinkielenOpinnot"
      >
        <table className="LukionOmanÄidinkielenOpinnot__tiedot">
          <tbody>
            <tr>
              <td className="LukionOmanÄidinkielenOpinnot__tietoLabel">
                {t('Arvosana')}
              </td>
              <td>
                {form.editMode ? (
                  <ArvosanaKoodistoSelect<
                    LukionOmanÄidinkielenOpinnot['arvosana']
                  >
                    onSelect={(k) =>
                      k &&
                      form.updateAt(
                        opinnotOptionalPath.prop('arvosana'),
                        () => k
                      )
                    }
                    value={opinnot.arvosana.koodiarvo}
                    testId="omanÄidinkielenOpinnot.arvosana"
                  />
                ) : (
                  <TestIdText id="omanÄidinkielenOpinnot.arvosana">
                    {opinnot.arvosana.koodiarvo}
                  </TestIdText>
                )}
              </td>
            </tr>
            {(form.editMode || opinnot.arviointipäivä) && (
              <tr>
                <td className="LukionOmanÄidinkielenOpinnot__tietoLabel">
                  {t('Arviointipäivä')}
                </td>
                <td>
                  <FormField
                    form={form}
                    path={opinnotOptionalPath.prop('arviointipäivä')}
                    optional
                    view={DateView}
                    edit={DateEdit}
                    editProps={{ align: 'right' }}
                    testId="omanÄidinkielenOpinnot.arviointipäivä"
                  />
                </td>
              </tr>
            )}
            <tr>
              <td className="LukionOmanÄidinkielenOpinnot__tietoLabel">
                {t('Kieli')}
              </td>
              <td>
                <FormField
                  form={form}
                  path={opinnotOptionalPath.prop('kieli')}
                  view={KoodistoView}
                  edit={KoodistoEdit}
                  editProps={{ koodistoUri: 'kielivalikoima' }}
                  testId="omanÄidinkielenOpinnot.kieli"
                />
              </td>
            </tr>
            <tr>
              <td className="LukionOmanÄidinkielenOpinnot__tietoLabel">
                {t('Laajuus')}
              </td>
              <td>
                <FormField
                  form={form}
                  path={opinnotOptionalPath.prop('laajuus')}
                  view={LaajuusView}
                  edit={LaajuusEdit}
                  editProps={{
                    createLaajuus: (arvo: number) =>
                      LaajuusOpintopisteissä({ arvo })
                  }}
                  testId="omanÄidinkielenOpinnot.laajuus"
                />
              </td>
            </tr>
            {(form.editMode ||
              (opinnot.osasuoritukset &&
                opinnot.osasuoritukset.length > 0)) && (
              <tr>
                <td className="LukionOmanÄidinkielenOpinnot__tietoLabel">
                  {t('Osasuoritukset')}
                </td>
                <td>
                  <OmanÄidinkielenOpintojenOsasuorituksetLista
                    form={form}
                    opinnotPath={opinnotOptionalPath}
                    osasuoritukset={opinnot.osasuoritukset || []}
                  />
                </td>
              </tr>
            )}
          </tbody>
        </table>
      </Removable>
    </KeyValueRow>
  )
}

const OmanÄidinkielenOpintojenOsasuorituksetLista: React.FC<{
  form: FormModel<LukionOpiskeluoikeus>
  opinnotPath: FormOptic<LukionOpiskeluoikeus, LukionOmanÄidinkielenOpinnot>
  osasuoritukset: LukionOmanÄidinkielenOpintojenOsasuoritus[]
}> = ({ form, opinnotPath, osasuoritukset }) => {
  const [lisääKurssiAuki, setLisääKurssiAuki] = useState(false)
  const osasuorituksetPath = opinnotPath.prop(
    'osasuoritukset'
  ) as unknown as FormOptic<
    LukionOpiskeluoikeus,
    LukionOmanÄidinkielenOpintojenOsasuoritus[] | undefined
  >

  return (
    <>
      {form.editMode ? (
        <OmanÄidinkielenOpintojenOsasuorituksetEditList
          form={form}
          opinnotPath={opinnotPath}
          osasuoritukset={osasuoritukset}
          osasuorituksetPath={osasuorituksetPath}
        />
      ) : (
        <OmanÄidinkielenOpintojenOsasuorituksetViewTable
          osasuoritukset={osasuoritukset}
        />
      )}
      {form.editMode && (
        <FlatButton
          testId="omanÄidinkielenOpinnot.lisääKurssi"
          onClick={() => setLisääKurssiAuki(true)}
        >
          {t('Lisää osasuoritus')}
        </FlatButton>
      )}
      {lisääKurssiAuki && (
        <UusiOmanÄidinkielenOpintojenKurssi
          olemassaOlevatKoodiarvot={osasuoritukset.map(
            (o) => o.koulutusmoduuli.tunniste.koodiarvo
          )}
          onAdd={(uusi) => {
            form.updateAt(osasuorituksetPath, (os) => appendOptional(uusi)(os))
            setLisääKurssiAuki(false)
          }}
          onClose={() => setLisääKurssiAuki(false)}
        />
      )}
    </>
  )
}

// Oikea <table>, ei KeyValueTable: sama syy kuin LukionOppiaineet.tsx:ssä
// (lukio-v2/LukioEditor.less) - tämä on aidosti taulukollinen rakenne
// (Kurssi/Suorituskieli/Arviointi per rivi), kuten vanhan käyttöliittymän
// katselutilan "table.tabular-array". Arviointi-solu sisältää Arvosanan ja
// Arviointipäivän samaan tapaan kuin Kurssi-solu sisältää Laajuuden -
// sisäkkäinen KeyValueTable, ei oma sarake kummallekaan.
const OmanÄidinkielenOpintojenOsasuorituksetViewTable: React.FC<{
  osasuoritukset: LukionOmanÄidinkielenOpintojenOsasuoritus[]
}> = ({ osasuoritukset }) => (
  <TestIdLayer id="omanÄidinkielenOpinnot.osasuoritukset">
    <table className="LukionOmanÄidinkielenOpinnot__osasuoritukset">
      <thead>
        <tr>
          <th className="LukionOmanÄidinkielenOpinnot__kurssi">
            {t('Kurssi')}
          </th>
          <th className="LukionOmanÄidinkielenOpinnot__suorituskieli">
            {t('Suorituskieli')}
          </th>
          <th className="LukionOmanÄidinkielenOpinnot__arviointi">
            {t('Arviointi')}
          </th>
        </tr>
      </thead>
      <tbody>
        {osasuoritukset.map((osasuoritus, i) => {
          const paras =
            osasuoritus.arviointi && parasArviointi(osasuoritus.arviointi)
          return (
            <TestIdLayer id={i} key={i} wrap="tr">
              <td className="LukionOmanÄidinkielenOpinnot__kurssi">
                <TestIdText id="nimi">
                  {t(osasuoritus.koulutusmoduuli.tunniste.nimi)}
                </TestIdText>
                <KeyValueTable>
                  <KeyValueRow localizableLabel="Laajuus" innerKeyValueTable>
                    <TestIdText id="laajuus">
                      {osasuoritus.koulutusmoduuli.laajuus.arvo}{' '}
                      {t(osasuoritus.koulutusmoduuli.laajuus.yksikkö.lyhytNimi)}
                    </TestIdText>
                  </KeyValueRow>
                </KeyValueTable>
              </td>
              <td className="LukionOmanÄidinkielenOpinnot__suorituskieli">
                {osasuoritus.suorituskieli && (
                  <TestIdText id="suorituskieli">
                    {t(osasuoritus.suorituskieli.nimi)}
                  </TestIdText>
                )}
              </td>
              <td className="LukionOmanÄidinkielenOpinnot__arviointi">
                {paras && (
                  <KeyValueTable>
                    <KeyValueRow localizableLabel="Arvosana" innerKeyValueTable>
                      <TestIdText id="arvosana.value">
                        {paras.arvosana.koodiarvo}
                      </TestIdText>
                    </KeyValueRow>
                    <KeyValueRow
                      localizableLabel="Arviointipäivä"
                      innerKeyValueTable
                    >
                      <TestIdText id="arviointipäivä.value">
                        {ISO2FinnishDate(paras.päivä)}
                      </TestIdText>
                    </KeyValueRow>
                  </KeyValueTable>
                )}
              </td>
            </TestIdLayer>
          )
        })}
      </tbody>
    </table>
  </TestIdLayer>
)

// Pino yksittäisiä kenttä-arvo-pareja per kurssi (KeyValueTable), ei taulukko
// - vastaa vanhan käyttöliittymän muokkaustilan rakennetta (jokainen kurssi
// omana "Nimi / Laajuus / Suorituskieli / Arvosana / Arviointipäivä"
// -lohkonaan), toisin kuin katselutilan tiivis taulukko yllä. Sama
// PuhviKoeRow:n jo käyttämä KeyValueTable+innerKeyValueTable-malli tässä
// tiedostossa.
const OmanÄidinkielenOpintojenOsasuorituksetEditList: React.FC<{
  form: FormModel<LukionOpiskeluoikeus>
  opinnotPath: FormOptic<LukionOpiskeluoikeus, LukionOmanÄidinkielenOpinnot>
  osasuoritukset: LukionOmanÄidinkielenOpintojenOsasuoritus[]
  osasuorituksetPath: FormOptic<
    LukionOpiskeluoikeus,
    LukionOmanÄidinkielenOpintojenOsasuoritus[] | undefined
  >
}> = ({ form, opinnotPath, osasuoritukset, osasuorituksetPath }) => (
  <TestIdLayer id="omanÄidinkielenOpinnot.osasuoritukset">
    <ul className="LukionOmanÄidinkielenOpinnot__osasuorituksetEdit">
      {osasuoritukset.map((osasuoritus, i) => {
        const kurssiPath = opinnotPath
          .prop('osasuoritukset')
          .optional()
          .at(i) as unknown as FormOptic<
          LukionOpiskeluoikeus,
          LukionOmanÄidinkielenOpintojenOsasuoritus
        >
        return (
          <TestIdLayer id={i} key={i} wrap="li">
            <KeyValueTable>
              <KeyValueRow localizableLabel="Nimi">
                <div className="LukionOmanÄidinkielenOpinnot__osasuoritusNimi">
                  <TestIdText id="nimi">
                    {t(osasuoritus.koulutusmoduuli.tunniste.nimi)}
                  </TestIdText>
                  <IconButton
                    charCode={CHARCODE_REMOVE}
                    label={t('Poista')}
                    size="input"
                    onClick={() =>
                      form.updateAt(osasuorituksetPath, (os) =>
                        deleteAt(i)(os || [])
                      )
                    }
                    testId="poisto"
                  />
                </div>
              </KeyValueRow>
              <KeyValueRow localizableLabel="Laajuus">
                <TestIdText id="laajuus">
                  {osasuoritus.koulutusmoduuli.laajuus.arvo}{' '}
                  {t(osasuoritus.koulutusmoduuli.laajuus.yksikkö.lyhytNimi)}
                </TestIdText>
              </KeyValueRow>
              <KeyValueRow localizableLabel="Suorituskieli">
                <FormField
                  form={form}
                  path={kurssiPath.prop('suorituskieli')}
                  optional
                  view={KoodistoView}
                  edit={KoodistoEdit}
                  editProps={{ koodistoUri: 'kieli' }}
                  testId="suorituskieli"
                />
              </KeyValueRow>
              <KeyValueRow localizableLabel="Arviointi">
                <KeyValueTable>
                  <KeyValueRow localizableLabel="Arvosana" innerKeyValueTable>
                    <FormField
                      form={form}
                      path={kurssiPath.prop('arviointi')}
                      optional
                      view={ParasArvosanaKoodiarvoView}
                      edit={ParasArvosanaEdit}
                      editProps={{
                        suoritusClassName: osasuoritus.$class,
                        format: koodiarvoOnly
                      }}
                      testId="arvosana"
                    />
                  </KeyValueRow>
                  <KeyValueRow
                    localizableLabel="Arviointipäivä"
                    innerKeyValueTable
                  >
                    <FormField
                      form={form}
                      path={kurssiPath.prop('arviointi')}
                      optional
                      view={ParasArvosanaPäiväView}
                      testId="arviointipäivä"
                    />
                  </KeyValueRow>
                </KeyValueTable>
              </KeyValueRow>
            </KeyValueTable>
          </TestIdLayer>
        )
      })}
    </ul>
  </TestIdLayer>
)

// Muokattavissa samalla mallilla kuin OmanÄidinkielenOpinnotRow: koko lohko
// voidaan lisätä/poistaa (Removable + modaali), ja lohkon kentät ovat
// muokattavissa. Arvosana ei kulje FormField+KoodistoEdit:n kautta kuten
// Kuvaus/Arviointipäivä, koska KoodistoEdit/KoodistoView eivät tue
// koodiarvoOnly/sort-muotoilua, jota arvosanan näyttäminen paljaana
// koodiarvona (esim. "8") vaatii - sama rajoitus kuin oman äidinkielen
// opintojen arvosanalla.
const PuhviKoeRow: React.FC<{
  form: FormModel<LukionOpiskeluoikeus>
  path: FormOptic<LukionOpiskeluoikeus, TuettuSuoritus>
  puhviKoe?: PuhviKoe2019
}> = ({ form, path, puhviKoe }) => {
  const [lisääAuki, setLisääAuki] = useState(false)
  const puhviKoeOptionalPath = path.prop('puhviKoe').optional()

  if (!form.editMode && !puhviKoe) return null

  if (form.editMode && !puhviKoe) {
    return (
      <KeyValueRow localizableLabel="Puhvi-koe">
        <FlatButton testId="puhviKoe.lisää" onClick={() => setLisääAuki(true)}>
          {t('Lisää Puhvi-koe')}
        </FlatButton>
        {lisääAuki && (
          <UusiPuhviKoeModal
            onClose={() => setLisääAuki(false)}
            onSubmit={(uusi) => {
              form.updateAt(path.prop('puhviKoe'), () => uusi)
              setLisääAuki(false)
            }}
          />
        )}
      </KeyValueRow>
    )
  }

  if (!puhviKoe) return null

  return (
    <KeyValueRow localizableLabel="Puhvi-koe">
      {/* Removable kutistaa sisältönsä (siis tämän KeyValueTablen) leveyden
          sisällön mukaiseksi KeyValueTable.less:n yleissäännöllä
          (".KeyValueTable .Removable { width: auto }"), joten Arvosana-
          valikko ei ole yhtä leveä kuin esim. Suullisen kielitaidon kokeet
          -lohkon oma, Removable-kiertämätön arvosana. Tämä on kuitenkin sama,
          jo tuotannossa oleva malli kuin perusopetus-v2:n/IB:n omilla "Oman
          äidinkielen opinnot"/"Puhvi-koe" -lohkoilla (ks.
          perusopetus-v2/PerusopetuksenSuorituksenTiedot.tsx,
          ib/PreIB2019KieliJaViestintäRows.tsx) - ei korjata tätä erikseen
          vain lukiolle, koska se veisi poispäin yhtenäisestä käytännöstä. */}
      <Removable
        isRemovable={form.editMode}
        onClick={() => form.updateAt(path.prop('puhviKoe'), () => undefined)}
        testId="puhviKoe"
      >
        <KeyValueTable>
          <KeyValueRow localizableLabel="Arvosana" innerKeyValueTable>
            {form.editMode ? (
              <ArvosanaKoodistoSelect<PuhviKoe2019['arvosana']>
                onSelect={(k) =>
                  k &&
                  form.updateAt(puhviKoeOptionalPath.prop('arvosana'), () => k)
                }
                value={puhviKoe.arvosana.koodiarvo}
                testId="puhviKoe.arvosana"
              />
            ) : (
              <TestIdText id="puhviKoe.arvosana">
                {puhviKoe.arvosana.koodiarvo}
              </TestIdText>
            )}
          </KeyValueRow>
          {(form.editMode || puhviKoe.kuvaus) && (
            <KeyValueRow localizableLabel="Kuvaus" innerKeyValueTable>
              <FormField
                form={form}
                path={puhviKoeOptionalPath.prop('kuvaus')}
                optional
                view={LocalizedTextView}
                edit={LocalizedTextEdit}
                editProps={{ large: true }}
                testId="puhviKoe.kuvaus"
              />
            </KeyValueRow>
          )}
          <KeyValueRow localizableLabel="Arviointipäivä" innerKeyValueTable>
            <FormField
              form={form}
              path={puhviKoeOptionalPath.prop('päivä')}
              view={DateView}
              edit={DateEdit}
              testId="puhviKoe.paiva"
            />
          </KeyValueRow>
        </KeyValueTable>
      </Removable>
    </KeyValueRow>
  )
}

const UusiPuhviKoeModal: React.FC<{
  onClose: () => void
  onSubmit: (puhviKoe: PuhviKoe2019) => void
}> = ({ onClose, onSubmit }) => {
  const [arvosana, setArvosana] = useState<
    PuhviKoe2019['arvosana'] | undefined
  >(undefined)
  const [kuvaus, setKuvaus] = useState<LocalizedString | undefined>(undefined)
  const [päivä, setPäivä] = useState<string | undefined>(undefined)

  const payload =
    arvosana !== undefined && päivä !== undefined
      ? { arvosana, kuvaus, päivä }
      : undefined

  return (
    <Modal onClose={onClose}>
      <ModalTitle>{t('Lisää Puhvi-koe')}</ModalTitle>
      <ModalBody>
        <label>
          {t('Arvosana')}
          <ArvosanaKoodistoSelect<PuhviKoe2019['arvosana']>
            skipAutoFocus
            onSelect={(k) => k && setArvosana(k)}
            value={arvosana?.koodiarvo}
            testId="arvosana"
          />
        </label>
        <label>
          {t('Kuvaus')}
          <LocalizedTextEdit
            value={kuvaus}
            onChange={setKuvaus}
            large
            testId="kuvaus"
          />
        </label>
        <label>
          {t('Arviointipäivä')}
          <DateInput
            value={päivä}
            onChange={setPäivä}
            testId="paiva"
            align="right"
          />
        </label>
      </ModalBody>
      <ModalFooter>
        <FlatButton onClick={onClose} testId="cancel">
          {t('Peruuta')}
        </FlatButton>
        <RaisedButton
          disabled={!payload}
          onClick={() => payload && onSubmit(PuhviKoe2019(payload))}
          testId="confirm"
        >
          {t('Lisää')}
        </RaisedButton>
      </ModalFooter>
    </Modal>
  )
}

// Katselutila: tiivis KeyValueTable per koe, muuttumaton. Muokkaustila:
// pino yksittäisiä kenttä-arvo-pareja per koe (sama malli kuin
// OmanÄidinkielenOpintojenOsasuorituksetEditList), koska rivillä on useita
// muokattavia kenttiä eikä tiivis taulukko sovi editointiin.
const SuullisenKielitaidonKokeetRow: React.FC<{
  form: FormModel<LukionOpiskeluoikeus>
  path: FormOptic<LukionOpiskeluoikeus, TuettuSuoritus>
  kokeet: SuullisenKielitaidonKoe2019[]
}> = ({ form, path, kokeet }) => {
  const [lisääAuki, setLisääAuki] = useState(false)
  const kokeetPath = path.prop('suullisenKielitaidonKokeet')

  if (!form.editMode && kokeet.length === 0) return null

  return (
    <KeyValueRow localizableLabel="Suullisen kielitaidon kokeet">
      <>
        {form.editMode ? (
          <TestIdLayer id="suullisenKielitaidonKokeet">
            <ul className="LukioEditor__arviointiListaEdit">
              {kokeet.map((koe, i) => {
                const koePath = kokeetPath.optional().at(i)
                return (
                  <TestIdLayer id={i} key={i} wrap="li">
                    <KeyValueTable>
                      <KeyValueRow localizableLabel="Kieli" innerKeyValueTable>
                        <div className="LukioEditor__arviointiEnsimmäinenKenttäJaPoisto">
                          <FormField
                            form={form}
                            path={koePath.prop('kieli')}
                            view={KoodistoView}
                            edit={KoodistoEdit}
                            editProps={{ koodistoUri: 'kielivalikoima' }}
                            testId="kieli"
                          />
                          <IconButton
                            charCode={CHARCODE_REMOVE}
                            label={t('Poista')}
                            size="input"
                            onClick={() =>
                              form.updateAt(kokeetPath, (os) =>
                                deleteAt(i)(os || [])
                              )
                            }
                            testId="poisto"
                          />
                        </div>
                      </KeyValueRow>
                      <KeyValueRow
                        localizableLabel="Arvosana"
                        innerKeyValueTable
                      >
                        <ArvosanaKoodistoSelect<
                          SuullisenKielitaidonKoe2019['arvosana']
                        >
                          onSelect={(k) =>
                            k &&
                            form.updateAt(koePath.prop('arvosana'), () => k)
                          }
                          value={koe.arvosana.koodiarvo}
                          testId="arvosana"
                        />
                      </KeyValueRow>
                      <KeyValueRow
                        localizableLabel="Taitotaso"
                        innerKeyValueTable
                      >
                        <FormField
                          form={form}
                          path={koePath.prop('taitotaso')}
                          view={KoodistoView}
                          edit={KoodistoEdit}
                          editProps={{
                            koodistoUri:
                              'arviointiasteikkokehittyvankielitaidontasot'
                          }}
                          testId="taitotaso"
                        />
                      </KeyValueRow>
                      <KeyValueRow localizableLabel="Kuvaus" innerKeyValueTable>
                        <FormField
                          form={form}
                          path={koePath.prop('kuvaus')}
                          optional
                          view={LocalizedTextView}
                          edit={LocalizedTextEdit}
                          editProps={{ large: true }}
                          testId="kuvaus"
                        />
                      </KeyValueRow>
                      <KeyValueRow
                        localizableLabel="Arviointipäivä"
                        innerKeyValueTable
                      >
                        <FormField
                          form={form}
                          path={koePath.prop('päivä')}
                          view={DateView}
                          edit={DateEdit}
                          testId="paiva"
                        />
                      </KeyValueRow>
                    </KeyValueTable>
                  </TestIdLayer>
                )
              })}
            </ul>
          </TestIdLayer>
        ) : (
          <ul className="LukioEditor__arviointiListaEdit">
            {kokeet.map((koe, i) => (
              <TestIdLayer id={i} key={i} wrap="li">
                <KeyValueTable>
                  <KeyValueRow localizableLabel="Kieli" innerKeyValueTable>
                    <TestIdText id="kieli">{t(koe.kieli.nimi)}</TestIdText>
                  </KeyValueRow>
                  <KeyValueRow localizableLabel="Arvosana" innerKeyValueTable>
                    <TestIdText id="arvosana">
                      {koe.arvosana.koodiarvo}
                    </TestIdText>
                  </KeyValueRow>
                  <KeyValueRow localizableLabel="Taitotaso" innerKeyValueTable>
                    <TestIdText id="taitotaso">
                      {t(koe.taitotaso.nimi)}
                    </TestIdText>
                  </KeyValueRow>
                  {koe.kuvaus && (
                    <KeyValueRow localizableLabel="Kuvaus" innerKeyValueTable>
                      <TestIdText id="kuvaus">{t(koe.kuvaus)}</TestIdText>
                    </KeyValueRow>
                  )}
                  <KeyValueRow
                    localizableLabel="Arviointipäivä"
                    innerKeyValueTable
                  >
                    <TestIdText id="paiva">
                      {ISO2FinnishDate(koe.päivä)}
                    </TestIdText>
                  </KeyValueRow>
                </KeyValueTable>
              </TestIdLayer>
            ))}
          </ul>
        )}
        {form.editMode && (
          <FlatButton
            testId="suullisenKielitaidonKokeet.lisää"
            onClick={() => setLisääAuki(true)}
          >
            {t('Lisää koe')}
          </FlatButton>
        )}
        {lisääAuki && (
          <UusiSuullisenKielitaidonKoeModal
            onClose={() => setLisääAuki(false)}
            onSubmit={(uusi) => {
              form.updateAt(kokeetPath, (os) => appendOptional(uusi)(os))
              setLisääAuki(false)
            }}
          />
        )}
      </>
    </KeyValueRow>
  )
}

const UusiSuullisenKielitaidonKoeModal: React.FC<{
  onClose: () => void
  onSubmit: (koe: SuullisenKielitaidonKoe2019) => void
}> = ({ onClose, onSubmit }) => {
  const [kieli, setKieli] = useState<
    SuullisenKielitaidonKoe2019['kieli'] | undefined
  >(undefined)
  const [arvosana, setArvosana] = useState<
    SuullisenKielitaidonKoe2019['arvosana'] | undefined
  >(undefined)
  const [taitotaso, setTaitotaso] = useState<
    SuullisenKielitaidonKoe2019['taitotaso'] | undefined
  >(undefined)
  const [kuvaus, setKuvaus] = useState<LocalizedString | undefined>(undefined)
  const [päivä, setPäivä] = useState<string | undefined>(undefined)

  const payload =
    kieli !== undefined &&
    arvosana !== undefined &&
    taitotaso !== undefined &&
    päivä !== undefined
      ? { kieli, arvosana, taitotaso, kuvaus, päivä }
      : undefined

  return (
    <Modal onClose={onClose}>
      <ModalTitle>{t('Lisää koe')}</ModalTitle>
      <ModalBody>
        <label>
          {t('Kieli')}
          <KoodistoSelect
            koodistoUri="kielivalikoima"
            skipAutoFocus
            onSelect={(k) => setKieli(k)}
            value={kieli?.koodiarvo}
            testId="kieli"
          />
        </label>
        <label>
          {t('Arvosana')}
          <ArvosanaKoodistoSelect<SuullisenKielitaidonKoe2019['arvosana']>
            onSelect={(k) => k && setArvosana(k)}
            value={arvosana?.koodiarvo}
            testId="arvosana"
          />
        </label>
        <label>
          {t('Taitotaso')}
          <KoodistoSelect
            koodistoUri="arviointiasteikkokehittyvankielitaidontasot"
            onSelect={(k) =>
              setTaitotaso(k as SuullisenKielitaidonKoe2019['taitotaso'])
            }
            value={taitotaso?.koodiarvo}
            testId="taitotaso"
          />
        </label>
        <label>
          {t('Kuvaus')}
          <LocalizedTextEdit
            value={kuvaus}
            onChange={setKuvaus}
            large
            testId="kuvaus"
          />
        </label>
        <label>
          {t('Arviointipäivä')}
          <DateInput
            value={päivä}
            onChange={setPäivä}
            testId="paiva"
            align="right"
          />
        </label>
      </ModalBody>
      <ModalFooter>
        <FlatButton onClick={onClose} testId="cancel">
          {t('Peruuta')}
        </FlatButton>
        <RaisedButton
          disabled={!payload}
          onClick={() =>
            payload && onSubmit(SuullisenKielitaidonKoe2019(payload))
          }
          testId="confirm"
        >
          {t('Lisää')}
        </RaisedButton>
      </ModalFooter>
    </Modal>
  )
}

// Katselutila: tiivis KeyValueTable per lukiodiplomi, muuttumaton.
// Muokkaustila: sama pino-per-rivi-malli kuin SuullisenKielitaidonKokeetRow'lla.
const LukiodiplomitRow: React.FC<{
  form: FormModel<LukionOpiskeluoikeus>
  path: FormOptic<LukionOpiskeluoikeus, TuettuSuoritus>
  diplomit: LukiodiplominSuoritusJaArviointi[]
}> = ({ form, path, diplomit }) => {
  const [lisääAuki, setLisääAuki] = useState(false)
  const diplomitPath = path.prop('lukiodiplomit2019')

  if (!form.editMode && diplomit.length === 0) return null

  return (
    <KeyValueRow localizableLabel="Lukiodiplomit">
      <>
        {form.editMode ? (
          <TestIdLayer id="lukiodiplomit2019">
            <ul className="LukioEditor__arviointiListaEdit">
              {diplomit.map((diplomi, i) => {
                const diplomiPath = diplomitPath.optional().at(i)
                return (
                  <TestIdLayer id={i} key={i} wrap="li">
                    <KeyValueTable>
                      <KeyValueRow
                        localizableLabel="Tunniste"
                        innerKeyValueTable
                      >
                        <div className="LukioEditor__arviointiEnsimmäinenKenttäJaPoisto">
                          <TestIdText id="tunniste">
                            {t(diplomi.tunniste.nimi)}
                          </TestIdText>
                          <IconButton
                            charCode={CHARCODE_REMOVE}
                            label={t('Poista')}
                            size="input"
                            onClick={() =>
                              form.updateAt(diplomitPath, (os) =>
                                deleteAt(i)(os || [])
                              )
                            }
                            testId="poisto"
                          />
                        </div>
                      </KeyValueRow>
                      <KeyValueRow
                        localizableLabel="Arvosana"
                        innerKeyValueTable
                      >
                        <ArvosanaKoodistoSelect<
                          LukiodiplominSuoritusJaArviointi['arvosana']
                        >
                          koodiarvot={LUKIODIPLOMIN_ARVOSANA_KOODIARVOT}
                          onSelect={(k) =>
                            k &&
                            form.updateAt(diplomiPath.prop('arvosana'), () => k)
                          }
                          value={diplomi.arvosana.koodiarvo}
                          testId="arvosana"
                        />
                      </KeyValueRow>
                      <KeyValueRow localizableLabel="Kuvaus" innerKeyValueTable>
                        <FormField
                          form={form}
                          path={diplomiPath.prop('kuvaus')}
                          optional
                          view={LocalizedTextView}
                          edit={LocalizedTextEdit}
                          editProps={{ large: true }}
                          testId="kuvaus"
                        />
                      </KeyValueRow>
                      <KeyValueRow
                        localizableLabel="Arviointipäivä"
                        innerKeyValueTable
                      >
                        <FormField
                          form={form}
                          path={diplomiPath.prop('päivä')}
                          view={DateView}
                          edit={DateEdit}
                          testId="paiva"
                        />
                      </KeyValueRow>
                    </KeyValueTable>
                  </TestIdLayer>
                )
              })}
            </ul>
          </TestIdLayer>
        ) : (
          <ul className="LukioEditor__arviointiListaEdit">
            {diplomit.map((diplomi, i) => (
              <TestIdLayer id={i} key={i} wrap="li">
                <KeyValueTable>
                  <KeyValueRow localizableLabel="Arvosana" innerKeyValueTable>
                    <TestIdText id="arvosana">
                      {diplomi.arvosana.koodiarvo}
                    </TestIdText>
                  </KeyValueRow>
                  <KeyValueRow
                    localizableLabel="Arviointipäivä"
                    innerKeyValueTable
                  >
                    <TestIdText id="paiva">
                      {ISO2FinnishDate(diplomi.päivä)}
                    </TestIdText>
                  </KeyValueRow>
                  {diplomi.kuvaus && (
                    <KeyValueRow localizableLabel="Kuvaus" innerKeyValueTable>
                      <TestIdText id="kuvaus">{t(diplomi.kuvaus)}</TestIdText>
                    </KeyValueRow>
                  )}
                  <KeyValueRow localizableLabel="Tunniste" innerKeyValueTable>
                    <TestIdText id="tunniste">
                      {t(diplomi.tunniste.nimi)}
                    </TestIdText>
                  </KeyValueRow>
                </KeyValueTable>
              </TestIdLayer>
            ))}
          </ul>
        )}
        {form.editMode && (
          <FlatButton
            testId="lukiodiplomit2019.lisää"
            onClick={() => setLisääAuki(true)}
          >
            {t('Lisää lukiodiplomi')}
          </FlatButton>
        )}
        {lisääAuki && (
          <UusiLukiodiplomiModal
            onClose={() => setLisääAuki(false)}
            onSubmit={(uusi) => {
              form.updateAt(diplomitPath, (os) => appendOptional(uusi)(os))
              setLisääAuki(false)
            }}
          />
        )}
      </>
    </KeyValueRow>
  )
}

// Vain nämä 8 koodiarvoa ovat sallittuja Lukio2019.scala:n
// LukiodiplominSuoritusJaArviointi.tunniste-kentälle (@KoodistoKoodiarvo),
// samasta moduulikoodistolops2021-koodistosta kuin valtakunnalliset moduulit.
const LUKIODIPLOMIN_TUNNISTE_KOODIARVOT = [
  'KOLD1',
  'KULD2',
  'KÄLD3',
  'LILD4',
  'MELD5',
  'MULD6',
  'TALD7',
  'TELD8'
]

// Lukiodiplomin arvosana-asteikko ei sisällä S/H-arvosanoja, toisin kuin
// Puhvi-kokeen/suullisen kielitaidon kokeen arvosana - ks. Lukio2019.scala:n
// LukiodiplominSuoritusJaArviointi.
const LUKIODIPLOMIN_ARVOSANA_KOODIARVOT = ['4', '5', '6', '7', '8', '9', '10']

const UusiLukiodiplomiModal: React.FC<{
  onClose: () => void
  onSubmit: (diplomi: LukiodiplominSuoritusJaArviointi) => void
}> = ({ onClose, onSubmit }) => {
  const [tunniste, setTunniste] = useState<
    LukiodiplominSuoritusJaArviointi['tunniste'] | undefined
  >(undefined)
  const [arvosana, setArvosana] = useState<
    LukiodiplominSuoritusJaArviointi['arvosana'] | undefined
  >(undefined)
  const [kuvaus, setKuvaus] = useState<LocalizedString | undefined>(undefined)
  const [päivä, setPäivä] = useState<string | undefined>(undefined)

  const payload =
    tunniste !== undefined && arvosana !== undefined && päivä !== undefined
      ? { tunniste, arvosana, kuvaus, päivä }
      : undefined

  return (
    <Modal onClose={onClose}>
      <ModalTitle>{t('Lisää lukiodiplomi')}</ModalTitle>
      <ModalBody>
        <label>
          {t('Tunniste')}
          <KoodistoSelect
            koodistoUri="moduulikoodistolops2021"
            koodiarvot={LUKIODIPLOMIN_TUNNISTE_KOODIARVOT}
            skipAutoFocus
            onSelect={(k) =>
              setTunniste(k as LukiodiplominSuoritusJaArviointi['tunniste'])
            }
            value={tunniste?.koodiarvo}
            testId="tunniste"
          />
        </label>
        <label>
          {t('Arvosana')}
          <ArvosanaKoodistoSelect<LukiodiplominSuoritusJaArviointi['arvosana']>
            koodiarvot={LUKIODIPLOMIN_ARVOSANA_KOODIARVOT}
            onSelect={(k) => k && setArvosana(k)}
            value={arvosana?.koodiarvo}
            testId="arvosana"
          />
        </label>
        <label>
          {t('Kuvaus')}
          <LocalizedTextEdit
            value={kuvaus}
            onChange={setKuvaus}
            large
            testId="kuvaus"
          />
        </label>
        <label>
          {t('Arviointipäivä')}
          <DateInput
            value={päivä}
            onChange={setPäivä}
            testId="paiva"
            align="right"
          />
        </label>
      </ModalBody>
      <ModalFooter>
        <FlatButton onClick={onClose} testId="cancel">
          {t('Peruuta')}
        </FlatButton>
        <RaisedButton
          disabled={!payload}
          onClick={() =>
            payload && onSubmit(LukiodiplominSuoritusJaArviointi(payload))
          }
          testId="confirm"
        >
          {t('Lisää')}
        </RaisedButton>
      </ModalFooter>
    </Modal>
  )
}
