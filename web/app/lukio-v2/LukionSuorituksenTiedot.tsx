import React, { useState } from 'react'
import { FormModel, FormOptic } from '../components-v2/forms/FormModel'
import { ActivePäätasonSuoritus } from '../components-v2/containers/EditorContainer'
import { LukionOpiskeluoikeus } from '../types/fi/oph/koski/schema/LukionOpiskeluoikeus'
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
  ParasArvosanaKoodiarvoView
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
import { IconButton } from '../components-v2/controls/IconButton'
import { CHARCODE_REMOVE } from '../components-v2/texts/Icon'
import { Removable } from '../components-v2/controls/Removable'
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

type TuettuSuoritus =
  LukionOppimääränSuoritus2019 | LukionOppiaineidenOppimäärienSuoritus2019

// Suorituksen otsikkokentät - muokattavissa kuten vanhassa käyttöliittymässä
// (suoritus/SuoritusEditor.jsx:n yleinen PropertiesEditor ei rajoita näitä
// mitenkään erityisesti lukiolle, ks. suoritusEditorMapping.tsx).
// Poikkeus: koulusivistyskieli ei ole muokattavissa - ks. KoulusivistyskieliRow'n
// oma kommentti.
export const LukionSuorituksenTiedot: React.FC<
  LukionSuorituksenTiedotProps
> = ({ form, päätasonSuoritus }) => {
  const suoritus = päätasonSuoritus.suoritus

  if (
    !isLukionOppimääränSuoritus2019(suoritus) &&
    !isLukionOppiaineidenOppimäärienSuoritus2019(suoritus)
  ) {
    // Muut lukion suoritustyypit (2015, pre-IB, LUVA) ei kuulu vaiheen 1
    // laajuuteen.
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

        {suoritus.puhviKoe && <PuhviKoeRow puhviKoe={suoritus.puhviKoe} />}

        {suoritus.suullisenKielitaidonKokeet &&
          suoritus.suullisenKielitaidonKokeet.length > 0 && (
            <SuullisenKielitaidonKokeetRow
              kokeet={suoritus.suullisenKielitaidonKokeet}
            />
          )}

        {suoritus.lukiodiplomit2019 &&
          suoritus.lukiodiplomit2019.length > 0 && (
            <LukiodiplomitRow diplomit={suoritus.lukiodiplomit2019} />
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
// tunnustettu-kenttä jätetään tarkoituksella muokkaamattomaksi - se on
// @Hidden, ks. tämän session aiempi tutkimus/päätös.
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
                  <KoodistoSelect
                    koodistoUri="arviointiasteikkoyleissivistava"
                    format={koodiarvoOnly}
                    sort
                    onSelect={(k) =>
                      k &&
                      form.updateAt(
                        opinnotOptionalPath.prop('arvosana'),
                        () => k as LukionOmanÄidinkielenOpinnot['arvosana']
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
                {form.editMode ? (
                  <KoodistoSelect
                    koodistoUri="kielivalikoima"
                    onSelect={(k) =>
                      k &&
                      form.updateAt(opinnotOptionalPath.prop('kieli'), () => k)
                    }
                    value={opinnot.kieli.koodiarvo}
                    testId="omanÄidinkielenOpinnot.kieli"
                  />
                ) : (
                  <TestIdText id="omanÄidinkielenOpinnot.kieli">
                    {t(opinnot.kieli.nimi)}
                  </TestIdText>
                )}
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

// Oikea <table>, ei KeyValueTable: sama syy kuin LukionOppiaineet.tsx:ssä
// (lukio-v2/LukioEditor.less) - tämä on aidosti taulukollinen rakenne
// (Kurssi/Suorituskieli/Arviointi per rivi), kuten vanhassa
// käyttöliittymässä, ei pino yksittäisiä kenttä-arvo-pareja.
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
              {form.editMode && (
                <th className="LukionOmanÄidinkielenOpinnot__poisto" />
              )}
            </tr>
          </thead>
          <tbody>
            {osasuoritukset.map((osasuoritus, i) => {
              const kurssiPath = opinnotPath
                .prop('osasuoritukset')
                .optional()
                .at(i) as unknown as FormOptic<
                LukionOpiskeluoikeus,
                LukionOmanÄidinkielenOpintojenOsasuoritus
              >
              return (
                <TestIdLayer id={i} key={i} wrap="tr">
                  <td className="LukionOmanÄidinkielenOpinnot__kurssi">
                    <TestIdText id="nimi">
                      {t(osasuoritus.koulutusmoduuli.tunniste.nimi)}
                    </TestIdText>
                    <KeyValueTable>
                      <KeyValueRow
                        localizableLabel="Laajuus"
                        innerKeyValueTable
                      >
                        <TestIdText id="laajuus">
                          {osasuoritus.koulutusmoduuli.laajuus.arvo}{' '}
                          {t(
                            osasuoritus.koulutusmoduuli.laajuus.yksikkö
                              .lyhytNimi
                          )}
                        </TestIdText>
                      </KeyValueRow>
                    </KeyValueTable>
                  </td>
                  <td className="LukionOmanÄidinkielenOpinnot__suorituskieli">
                    <FormField
                      form={form}
                      path={kurssiPath.prop('suorituskieli')}
                      optional
                      view={KoodistoView}
                      edit={KoodistoEdit}
                      editProps={{ koodistoUri: 'kieli' }}
                      testId="suorituskieli"
                    />
                  </td>
                  <td className="LukionOmanÄidinkielenOpinnot__arviointi">
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
                  </td>
                  {form.editMode && (
                    <td className="LukionOmanÄidinkielenOpinnot__poisto">
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
                    </td>
                  )}
                </TestIdLayer>
              )
            })}
          </tbody>
        </table>
      </TestIdLayer>
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

const PuhviKoeRow: React.FC<{ puhviKoe: PuhviKoe2019 }> = ({ puhviKoe }) => (
  <KeyValueRow localizableLabel="Puhvi-koe">
    <KeyValueTable>
      <KeyValueRow localizableLabel="Arvosana" innerKeyValueTable>
        <TestIdText id="puhviKoe.arvosana">
          {puhviKoe.arvosana.koodiarvo}
        </TestIdText>
      </KeyValueRow>
      {puhviKoe.kuvaus && (
        <KeyValueRow localizableLabel="Kuvaus" innerKeyValueTable>
          <TestIdText id="puhviKoe.kuvaus">{t(puhviKoe.kuvaus)}</TestIdText>
        </KeyValueRow>
      )}
      <KeyValueRow localizableLabel="Arviointipäivä" innerKeyValueTable>
        <TestIdText id="puhviKoe.paiva">
          {ISO2FinnishDate(puhviKoe.päivä)}
        </TestIdText>
      </KeyValueRow>
    </KeyValueTable>
  </KeyValueRow>
)

const SuullisenKielitaidonKokeetRow: React.FC<{
  kokeet: SuullisenKielitaidonKoe2019[]
}> = ({ kokeet }) => (
  <KeyValueRow localizableLabel="Suullisen kielitaidon kokeet">
    <>
      {kokeet.map((koe, i) => (
        <KeyValueTable key={i}>
          <KeyValueRow localizableLabel="Kieli" innerKeyValueTable>
            <TestIdText id="kieli">{t(koe.kieli.nimi)}</TestIdText>
          </KeyValueRow>
          <KeyValueRow localizableLabel="Arvosana" innerKeyValueTable>
            <TestIdText id="arvosana">{koe.arvosana.koodiarvo}</TestIdText>
          </KeyValueRow>
          <KeyValueRow localizableLabel="Taitotaso" innerKeyValueTable>
            <TestIdText id="taitotaso">{t(koe.taitotaso.nimi)}</TestIdText>
          </KeyValueRow>
          {koe.kuvaus && (
            <KeyValueRow localizableLabel="Kuvaus" innerKeyValueTable>
              <TestIdText id="kuvaus">{t(koe.kuvaus)}</TestIdText>
            </KeyValueRow>
          )}
          <KeyValueRow localizableLabel="Arviointipäivä" innerKeyValueTable>
            <TestIdText id="paiva">{ISO2FinnishDate(koe.päivä)}</TestIdText>
          </KeyValueRow>
        </KeyValueTable>
      ))}
    </>
  </KeyValueRow>
)

const LukiodiplomitRow: React.FC<{
  diplomit: LukiodiplominSuoritusJaArviointi[]
}> = ({ diplomit }) => (
  <KeyValueRow localizableLabel="Lukiodiplomit">
    <>
      {diplomit.map((diplomi, i) => (
        <KeyValueTable key={i}>
          <KeyValueRow localizableLabel="Arvosana" innerKeyValueTable>
            <TestIdText id="arvosana">{diplomi.arvosana.koodiarvo}</TestIdText>
          </KeyValueRow>
          <KeyValueRow localizableLabel="Arviointipäivä" innerKeyValueTable>
            <TestIdText id="paiva">{ISO2FinnishDate(diplomi.päivä)}</TestIdText>
          </KeyValueRow>
          {diplomi.kuvaus && (
            <KeyValueRow localizableLabel="Kuvaus" innerKeyValueTable>
              <TestIdText id="kuvaus">{t(diplomi.kuvaus)}</TestIdText>
            </KeyValueRow>
          )}
          <KeyValueRow localizableLabel="Tunniste" innerKeyValueTable>
            <TestIdText id="tunniste">{t(diplomi.tunniste.nimi)}</TestIdText>
          </KeyValueRow>
        </KeyValueTable>
      ))}
    </>
  </KeyValueRow>
)
