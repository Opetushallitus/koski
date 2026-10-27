import React from 'react'
import { FormModel, FormOptic } from '../components-v2/forms/FormModel'
import { ActivePäätasonSuoritus } from '../components-v2/containers/EditorContainer'
import { LukionOpiskeluoikeus } from '../types/fi/oph/koski/schema/LukionOpiskeluoikeus'
import {
  KeyColumnedValuesRow,
  KeyValueRow,
  KeyValueTable
} from '../components-v2/containers/KeyValueTable'
import { FormField } from '../components-v2/forms/FormField'
import { OrganisaatioView } from '../components-v2/opiskeluoikeus/OrganisaatioField'
import { KoodistoView } from '../components-v2/opiskeluoikeus/KoodistoField'
import { PerusteView } from '../components-v2/opiskeluoikeus/PerusteField'
import { SuorituksenVahvistusField } from '../components-v2/opiskeluoikeus/SuorituksenVahvistus'
import { TestIdText } from '../appstate/useTestId'
import { LocalizedTextView } from '../components-v2/controls/LocalizedTestField'
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
import { Koodistokoodiviite } from '../types/fi/oph/koski/schema/Koodistokoodiviite'
import { HenkilövahvistusPaikkakunnalla } from '../types/fi/oph/koski/schema/HenkilovahvistusPaikkakunnalla'
import { PuhviKoe2019 } from '../types/fi/oph/koski/schema/PuhviKoe2019'
import { SuullisenKielitaidonKoe2019 } from '../types/fi/oph/koski/schema/SuullisenKielitaidonKoe2019'
import { LukiodiplominSuoritusJaArviointi } from '../types/fi/oph/koski/schema/LukiodiplominSuoritusJaArviointi'

type LukionSuorituksenTiedotProps = {
  form: FormModel<LukionOpiskeluoikeus>
  päätasonSuoritus: ActivePäätasonSuoritus<LukionOpiskeluoikeus>
}

type TuettuSuoritus =
  LukionOppimääränSuoritus2019 | LukionOppiaineidenOppimäärienSuoritus2019

// Vaihe 1: vain katselu - kaikki kentät renderöidään vain view-komponenteilla
// (ei edit-propia), joten ne pysyvät lukutilassa vaikka EditorContainerin oma
// "Muokkaa"-painike otetaan käyttöön (FormField ohittaa editMode:n kun edit
// puuttuu, ks. FormField.tsx).
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
            testId="oppimäärä"
          />
        </KeyValueRow>

        <KeyValueRow localizableLabel="Oppilaitos / toimipiste">
          <FormField
            form={form}
            path={path.prop('toimipiste')}
            view={OrganisaatioView}
            testId="toimipiste"
          />
        </KeyValueRow>

        <KeyValueRow localizableLabel="Suorituskieli">
          <FormField
            form={form}
            path={path.prop('suorituskieli')}
            view={KoodistoView}
            testId="suorituskieli"
          />
        </KeyValueRow>

        {isLukionOppimääränSuoritus2019(suoritus) &&
          suoritus.omanÄidinkielenOpinnot && (
            <OmanÄidinkielenOpinnotRow
              opinnot={suoritus.omanÄidinkielenOpinnot}
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
              testId="todistuksellaNäkyvätLisätiedot"
            />
          </KeyValueRow>
        )}

        {suoritus.ryhmä && (
          <KeyValueRow localizableLabel="Ryhmä">
            <TestIdText id="ryhmä">{suoritus.ryhmä}</TestIdText>
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
        />
      ]}
    </KeyColumnedValuesRow>
  )

const KoulusivistyskieliRow: React.FC<{
  suoritus: {
    koulusivistyskieli?: Array<Koodistokoodiviite<'kieli', 'FI' | 'SV'>>
  }
}> = ({ suoritus }) => {
  const koulusivistyskieli = suoritus.koulusivistyskieli
  if (!koulusivistyskieli || koulusivistyskieli.length === 0) return null
  return (
    <KeyValueRow localizableLabel="Koulusivistyskieli">
      <TestIdText id="koulusivistyskieli">
        {koulusivistyskieli.map((k) => t(k.nimi)).join(', ')}
      </TestIdText>
    </KeyValueRow>
  )
}

const OmanÄidinkielenOpinnotRow: React.FC<{
  opinnot: LukionOmanÄidinkielenOpinnot
}> = ({ opinnot }) => (
  <KeyValueRow localizableLabel="Lukion oppimäärää täydentävät oman äidinkielen opinnot">
    <KeyValueTable>
      <KeyValueRow localizableLabel="Arvosana" innerKeyValueTable>
        <TestIdText id="omanÄidinkielenOpinnot.arvosana">
          {opinnot.arvosana.koodiarvo}
        </TestIdText>
      </KeyValueRow>
      {opinnot.arviointipäivä && (
        <KeyValueRow localizableLabel="Arviointipäivä" innerKeyValueTable>
          <TestIdText id="omanÄidinkielenOpinnot.arviointipäivä">
            {opinnot.arviointipäivä}
          </TestIdText>
        </KeyValueRow>
      )}
      <KeyValueRow localizableLabel="Kieli" innerKeyValueTable>
        <TestIdText id="omanÄidinkielenOpinnot.kieli">
          {t(opinnot.kieli.nimi)}
        </TestIdText>
      </KeyValueRow>
      <KeyValueRow localizableLabel="Laajuus" innerKeyValueTable>
        <TestIdText id="omanÄidinkielenOpinnot.laajuus">
          {opinnot.laajuus.arvo} {t(opinnot.laajuus.yksikkö.lyhytNimi)}
        </TestIdText>
      </KeyValueRow>
      {opinnot.osasuoritukset && opinnot.osasuoritukset.length > 0 && (
        <KeyValueRow localizableLabel="Osasuoritukset" innerKeyValueTable>
          <OmanÄidinkielenOpintojenOsasuorituksetLista
            osasuoritukset={opinnot.osasuoritukset}
          />
        </KeyValueRow>
      )}
    </KeyValueTable>
  </KeyValueRow>
)

const OmanÄidinkielenOpintojenOsasuorituksetLista: React.FC<{
  osasuoritukset: LukionOmanÄidinkielenOpintojenOsasuoritus[]
}> = ({ osasuoritukset }) => (
  <>
    {osasuoritukset.map((osasuoritus, i) => {
      const arviointi =
        osasuoritus.arviointi?.[osasuoritus.arviointi.length - 1]
      return (
        <KeyValueTable key={i}>
          <KeyValueRow localizableLabel="Kurssi" innerKeyValueTable>
            <TestIdText id="nimi">
              {t(osasuoritus.koulutusmoduuli.tunniste.nimi)}
            </TestIdText>
          </KeyValueRow>
          {osasuoritus.suorituskieli && (
            <KeyValueRow localizableLabel="Suorituskieli" innerKeyValueTable>
              <TestIdText id="suorituskieli">
                {t(osasuoritus.suorituskieli.nimi)}
              </TestIdText>
            </KeyValueRow>
          )}
          {arviointi && (
            <>
              <KeyValueRow localizableLabel="Arvosana" innerKeyValueTable>
                <TestIdText id="arvosana">
                  {arviointi.arvosana.koodiarvo}
                </TestIdText>
              </KeyValueRow>
              <KeyValueRow localizableLabel="Arviointipäivä" innerKeyValueTable>
                <TestIdText id="arviointipaiva">{arviointi.päivä}</TestIdText>
              </KeyValueRow>
            </>
          )}
        </KeyValueTable>
      )
    })}
  </>
)

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
        <TestIdText id="puhviKoe.paiva">{puhviKoe.päivä}</TestIdText>
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
            <TestIdText id="paiva">{koe.päivä}</TestIdText>
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
            <TestIdText id="paiva">{diplomi.päivä}</TestIdText>
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
