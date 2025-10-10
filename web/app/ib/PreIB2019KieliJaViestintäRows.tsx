import { FormModel } from '../components-v2/forms/FormModel'
import { IBOpiskeluoikeus } from '../types/fi/oph/koski/schema/IBOpiskeluoikeus'
import { ActivePäätasonSuoritus } from '../components-v2/containers/EditorContainer'
import { PreIBSuoritus2019 } from '../types/fi/oph/koski/schema/PreIBSuoritus2019'
import React, { useMemo, useState } from 'react'
import {
  KeyValueRow,
  KeyValueTable
} from '../components-v2/containers/KeyValueTable'
import { FlatButton } from '../components-v2/controls/FlatButton'
import { t } from '../i18n/i18n'
import { UusiOmanÄidinkielenOpinnotModal } from './dialogs/UusiOmanÄidinkielenOpinnotModal'
import { Removable } from '../components-v2/controls/Removable'
import { KoodistoSelect } from '../components-v2/opiskeluoikeus/KoodistoSelect'
import { TestIdText } from '../appstate/useTestId'
import { FormField } from '../components-v2/forms/FormField'
import { DateEdit, DateView } from '../components-v2/controls/DateField'
import {
  LaajuusOpintopisteissäEdit,
  LaajuusView
} from '../components-v2/opiskeluoikeus/LaajuusField'
import { Spacer, SpacerLine } from '../components-v2/layout/Spacer'
import { UusiOmanÄidinkielenOpintojenKurssiModal } from './dialogs/UusiOmanÄidinkielenOpintojenKurssiModal'
import { pipe } from 'fp-ts/function'
import * as A from 'fp-ts/Array'
import {
  Details,
  SuorituksenTilaIcon
} from '../components-v2/opiskeluoikeus/OppiaineTable'
import { LukionOmanÄidinkielenOpintojenOsasuoritus } from '../types/fi/oph/koski/schema/LukionOmanAidinkielenOpintojenOsasuoritus'
import { PathToken } from '../util/laxModify'
import { useBooleanState } from '../util/useBooleanState'
import { IconButton } from '../components-v2/controls/IconButton'
import { CHARCODE_REMOVE } from '../components-v2/texts/Icon'
import { parasArviointi } from '../util/arvioinnit'
import { Arviointi } from '../types/fi/oph/koski/schema/Arviointi'
import { ISO2FinnishDate } from '../date/date'
import { OsaamisenTunnustusView } from '../components-v2/opiskeluoikeus/TunnustusField'
import { EditOmanÄidinkielenOpintojenOsasuoritusModal } from './dialogs/EditOmanÄidinkielenOpintojenOsasuoritusModal'
import { useKoodistoOfConstraint } from '../appstate/koodisto'
import { useChildSchema } from '../appstate/constraints'
import { PuhviKoe2019 } from '../types/fi/oph/koski/schema/PuhviKoe2019'
import { UusiPuhviKoeModal } from './dialogs/UusiPuhviKoeModal'
import {
  LocalizedTextEdit,
  LocalizedTextView
} from '../components-v2/controls/LocalizedTestField'
import { UusiSuullisenKielitaidonKoeModal } from './dialogs/UusiSuullisenKielitaidonKoeModal'
import { SuullisenKielitaidonKoe2019 } from '../types/fi/oph/koski/schema/SuullisenKielitaidonKoe2019'
import { Koodistokoodiviite } from '../types/fi/oph/koski/schema/Koodistokoodiviite'
import { LaajuusOpintopisteissä } from '../types/fi/oph/koski/schema/LaajuusOpintopisteissa'
import { LukionOmanÄidinkielenOpinnot } from '../types/fi/oph/koski/schema/LukionOmanAidinkielenOpinnot'
import { LukionOmanÄidinkielenOpinto } from '../types/fi/oph/koski/schema/LukionOmanAidinkielenOpinto'
import { LukionOmanÄidinkielenOpinnonOsasuorituksenArviointi } from '../types/fi/oph/koski/schema/LukionOmanAidinkielenOpinnonOsasuorituksenArviointi'
import * as O from 'fp-ts/Option'
import * as Ord from 'fp-ts/Ord'
import * as S from 'fp-ts/string'

export type PreIBOmanÄidinkielenOpinnot2019Arvosana =
  LukionOmanÄidinkielenOpinnot['arvosana']
export type PreIBOmanÄidinkielenOpinto = LukionOmanÄidinkielenOpinto['tunniste']
export type PreIBOmanÄidinkielenOpintoOsasuorituksenArvosana =
  LukionOmanÄidinkielenOpinnonOsasuorituksenArviointi['arvosana']
export type PreIBPuhviKoe2019Arvosana = PuhviKoe2019['arvosana']
export type PreIBSuullisenKielitaidonKoe2019Taitotaso =
  SuullisenKielitaidonKoe2019['taitotaso']
export type PreIBSuullisenKielitaidonKoe2019Arvosana =
  SuullisenKielitaidonKoe2019['arvosana']

export type PreIB2019KieliJaViestintäRowsProps = {
  form: FormModel<IBOpiskeluoikeus>
  päätasonSuoritus: ActivePäätasonSuoritus<IBOpiskeluoikeus, PreIBSuoritus2019>
}

export const PreIB2019KieliJaViestintäRows: React.FC<
  PreIB2019KieliJaViestintäRowsProps
> = ({ form, päätasonSuoritus }) => {
  return (
    <>
      <PreIB2019OmanÄidinkielenOpinnotRows
        form={form}
        päätasonSuoritus={päätasonSuoritus}
      />
      <PreIB2019PuhviKoeRows form={form} päätasonSuoritus={päätasonSuoritus} />
      <PreIB2019SuullisenKielitaidonKokeetRows
        form={form}
        päätasonSuoritus={päätasonSuoritus}
      />
    </>
  )
}

const PreIB2019OmanÄidinkielenOpinnotRows: React.FC<
  PreIB2019KieliJaViestintäRowsProps
> = ({ form, päätasonSuoritus }) => {
  const path = päätasonSuoritus.path
  const omanÄidinkielenOpinnotEiSyötetty =
    päätasonSuoritus.suoritus.omanÄidinkielenOpinnot === undefined
  const onOsasuorituksia =
    päätasonSuoritus.suoritus.omanÄidinkielenOpinnot?.osasuoritukset &&
    päätasonSuoritus.suoritus.omanÄidinkielenOpinnot?.osasuoritukset.length > 0
  const [
    showLisääOmanÄidinkielenOpinnotModal,
    setShowLisääOmanÄidinkielenOpinnotModal
  ] = useState(false)
  const [showLisääKurssiModal, setShowLisääKurssiModal] = useState(false)
  const removeOmanÄidinkielenOpinnot = () => {
    form.set(
      ...päätasonSuoritus.pathTokens,
      ...['omanÄidinkielenOpinnot']
    )(undefined)
  }

  if (!form.editMode && omanÄidinkielenOpinnotEiSyötetty) {
    return null
  }

  return form.editMode && omanÄidinkielenOpinnotEiSyötetty ? (
    <>
      <SpacerLine />
      <KeyValueRow localizableLabel="Oman äidinkielen opinnot">
        <FlatButton
          testId="omanÄidinkielenOpinnot.lisää"
          onClick={() => setShowLisääOmanÄidinkielenOpinnotModal(true)}
        >
          {t('Lisää täydentävät oman äidinkielen opinnot')}
        </FlatButton>
        {showLisääOmanÄidinkielenOpinnotModal && (
          <UusiOmanÄidinkielenOpinnotModal
            onClose={() => setShowLisääOmanÄidinkielenOpinnotModal(false)}
            onSubmit={(arvosana, kieli, laajuus, arviointipäivä) =>
              form.set(
                ...päätasonSuoritus.pathTokens,
                ...['omanÄidinkielenOpinnot']
              )(
                createLukionOmanÄidinkielenOpinnot(
                  arvosana,
                  kieli,
                  laajuus,
                  arviointipäivä
                )
              )
            }
          />
        )}
      </KeyValueRow>
    </>
  ) : (
    <>
      <SpacerLine />
      <KeyValueRow localizableLabel="Oman äidinkielen opinnot">
        <Removable
          isRemovable={form.editMode}
          onClick={removeOmanÄidinkielenOpinnot}
          testId="omanÄidinkielenOpinnot"
        >
          <KeyValueTable>
            <KeyValueRow localizableLabel="Arvosana" innerKeyValueTable>
              {form.editMode ? (
                <KoodistoSelect
                  koodistoUri={'arviointiasteikkoyleissivistava'}
                  format={(koodiviite) =>
                    koodiviite.koodiarvo + ' ' + t(koodiviite.nimi)
                  }
                  onSelect={(koodiviite) => {
                    koodiviite &&
                      form.set(
                        ...päätasonSuoritus.pathTokens,
                        ...['omanÄidinkielenOpinnot', 'arvosana']
                      )(koodiviite)
                  }}
                  value={
                    päätasonSuoritus.suoritus.omanÄidinkielenOpinnot?.arvosana
                      .koodiarvo
                  }
                  testId="omanÄidinkielenOpinnot.arvosana"
                />
              ) : (
                <TestIdText id="omanÄidinkielenOpinnot.arvosana">
                  {
                    päätasonSuoritus.suoritus.omanÄidinkielenOpinnot?.arvosana
                      .koodiarvo
                  }{' '}
                  {t(
                    päätasonSuoritus.suoritus.omanÄidinkielenOpinnot?.arvosana
                      .nimi
                  )}
                </TestIdText>
              )}
            </KeyValueRow>
            <KeyValueRow localizableLabel="Arviointipäivä" innerKeyValueTable>
              <FormField
                form={form}
                testId="omanÄidinkielenOpinnot.arviointipäivä"
                view={DateView}
                edit={DateEdit}
                editProps={{ align: 'right' }}
                path={path
                  .prop('omanÄidinkielenOpinnot')
                  .optional()
                  .prop('arviointipäivä')}
              />
            </KeyValueRow>
            <KeyValueRow localizableLabel="Kieli" innerKeyValueTable>
              {form.editMode ? (
                <KoodistoSelect
                  koodistoUri={'kielivalikoima'}
                  onSelect={(koodiviite) => {
                    koodiviite &&
                      form.set(
                        ...päätasonSuoritus.pathTokens,
                        ...['omanÄidinkielenOpinnot', 'kieli']
                      )(koodiviite)
                  }}
                  value={
                    päätasonSuoritus.suoritus.omanÄidinkielenOpinnot?.kieli
                      .koodiarvo
                  }
                  testId="omanÄidinkielenOpinnot.kieli"
                />
              ) : (
                <TestIdText id="omanÄidinkielenOpinnot.kieli">
                  {t(
                    päätasonSuoritus.suoritus.omanÄidinkielenOpinnot?.kieli.nimi
                  )}
                </TestIdText>
              )}
            </KeyValueRow>
            <KeyValueRow localizableLabel="Laajuus" innerKeyValueTable>
              <FormField
                form={form}
                path={path
                  .prop('omanÄidinkielenOpinnot')
                  .optional()
                  .prop('laajuus')}
                view={LaajuusView}
                edit={LaajuusOpintopisteissäEdit}
                testId="omanÄidinkielenOpinnot.laajuus"
              />
            </KeyValueRow>
            {onOsasuorituksia && (
              <KeyValueRow
                localizableLabel={'Osasuoritukset'}
                innerKeyValueTable
              >
                <OmanÄidinkielenOpintojenKurssit
                  form={form}
                  päätasonSuoritus={päätasonSuoritus}
                />
              </KeyValueRow>
            )}
            {form.editMode && (
              <KeyValueRow innerKeyValueTable>
                <FlatButton
                  testId="omanÄidinkielenOpinnot.lisääOsasuoritus"
                  onClick={() => setShowLisääKurssiModal(true)}
                >
                  {t('Lisää osasuoritus')}
                </FlatButton>
                <Spacer />
              </KeyValueRow>
            )}
            {showLisääKurssiModal && (
              <UusiOmanÄidinkielenOpintojenKurssiModal
                olemassaOlevatModuulit={
                  päätasonSuoritus.suoritus.omanÄidinkielenOpinnot?.osasuoritukset?.map(
                    (os) => os.koulutusmoduuli.tunniste.koodiarvo
                  ) || []
                }
                onClose={() => setShowLisääKurssiModal(false)}
                onSubmit={(koulutusmoduuli, arvosana, arviointipäivä, kieli) =>
                  pipe(
                    päätasonSuoritus.suoritus.omanÄidinkielenOpinnot
                      ?.osasuoritukset || [],
                    A.append(
                      createLukionOmanÄidinkielenOpinnotOsasuoritus(
                        koulutusmoduuli,
                        arvosana,
                        arviointipäivä,
                        kieli
                      )
                    ),
                    (osasuoritukset) =>
                      form.set(
                        ...päätasonSuoritus.pathTokens,
                        ...['omanÄidinkielenOpinnot', 'osasuoritukset']
                      )(osasuoritukset)
                  )
                }
              />
            )}
          </KeyValueTable>
        </Removable>
      </KeyValueRow>
    </>
  )
}

const OmanÄidinkielenOpintojenKurssit: React.FC<
  PreIB2019KieliJaViestintäRowsProps
> = ({ form, päätasonSuoritus }) => {
  const osasuoritukset = useMemo(() => {
    return (
      päätasonSuoritus.suoritus.omanÄidinkielenOpinnot?.osasuoritukset || []
    )
  }, [päätasonSuoritus.suoritus.omanÄidinkielenOpinnot?.osasuoritukset])

  const osasuorituksetSorted = useMemo(() => {
    return A.sort(osasuoritusOrd)(osasuoritukset)
  }, [osasuoritukset])

  return (
    <table className="OppiaineTable">
      <thead>
        <tr>
          <th></th>
          <th>{t('Kurssit')}</th>
        </tr>
      </thead>
      <tbody>
        <tr>
          <td className="OppiaineRow__icon">
            {päätasonSuoritus.suoritus.omanÄidinkielenOpinnot && (
              <SuorituksenTilaIcon
                suoritus={päätasonSuoritus.suoritus.omanÄidinkielenOpinnot}
              />
            )}
          </td>
          <td className="OppiaineRow__oppiaine">
            <div className="OppiaineRow__kurssit">
              {osasuorituksetSorted.map((os) => {
                const index = osasuoritukset.indexOf(os)
                const osasuoritusId = `omanÄidinkielenOpinnot-${os.koulutusmoduuli.tunniste.koodiarvo}-${index}`
                return (
                  <OmanÄidinkielenOpintojenKurssi
                    form={form}
                    päätasonSuoritus={päätasonSuoritus}
                    osasuoritus={os}
                    osasuoritusPath={[
                      ...päätasonSuoritus.pathTokens,
                      'omanÄidinkielenOpinnot',
                      'osasuoritukset',
                      index
                    ]}
                    index={index}
                    tooltipId={osasuoritusId}
                    key={osasuoritusId}
                  />
                )
              })}
            </div>
          </td>
        </tr>
      </tbody>
    </table>
  )
}

type OmanÄidinkielenOpintojenKurssiProps =
  PreIB2019KieliJaViestintäRowsProps & {
    osasuoritus: LukionOmanÄidinkielenOpintojenOsasuoritus
    osasuoritusPath: PathToken[]
    index: number
    tooltipId: string
  }

const OmanÄidinkielenOpintojenKurssi: React.FC<
  OmanÄidinkielenOpintojenKurssiProps
> = ({
  form,
  päätasonSuoritus,
  osasuoritus,
  osasuoritusPath,
  index,
  tooltipId
}) => {
  const [tooltipVisible, openTooltip, closeTooltip] = useBooleanState(false)
  const [editModalVisible, openEditModal, closeEditModal] =
    useBooleanState(false)

  return (
    <div className="Kurssi">
      <button
        className={`Kurssi__tunniste${form.editMode ? ' Kurssi__clickable' : ''}`}
        onClick={form.editMode ? openEditModal : undefined}
        onTouchStart={openTooltip}
        onMouseEnter={openTooltip}
        onMouseLeave={closeTooltip}
        onFocus={openTooltip}
        onBlur={closeTooltip}
        aria-describedby={tooltipId}
      >
        <TestIdText id={`omanÄidinkielenOpinnot.${index}.osasuoritus`}>
          {osasuoritus.koulutusmoduuli.tunniste.koodiarvo}
        </TestIdText>
      </button>
      {form.editMode && (
        <IconButton
          charCode={CHARCODE_REMOVE}
          label={t('Poista')}
          size="input"
          onClick={removeAt(
            päätasonSuoritus.suoritus.omanÄidinkielenOpinnot?.osasuoritukset,
            form,
            [
              ...päätasonSuoritus.pathTokens,
              ...['omanÄidinkielenOpinnot', 'osasuoritukset']
            ],
            index
          )}
          testId={`omanÄidinkielenOpinnot.${index}.delete`}
        />
      )}
      <div className="Kurssi__arvosana">
        <TestIdText id={`omanÄidinkielenOpinnot.${index}.arvosana`}>
          {osasuoritus.arviointi
            ? parasArviointi(osasuoritus.arviointi as Arviointi[])?.arvosana
                .koodiarvo
            : '–'}
        </TestIdText>
      </div>
      {tooltipVisible && (
        <Details id={tooltipId}>
          <KeyValueTable>
            <KeyValueRow localizableLabel="Nimi">
              <TestIdText id="nimi">
                {t(osasuoritus.koulutusmoduuli.tunniste.nimi)}
              </TestIdText>
            </KeyValueRow>
            <KeyValueRow localizableLabel="Laajuus">
              <TestIdText id="laajuus">
                {osasuoritus.koulutusmoduuli.laajuus?.arvo}{' '}
                {t(osasuoritus.koulutusmoduuli.laajuus?.yksikkö.nimi)}
              </TestIdText>
            </KeyValueRow>
            <KeyValueRow localizableLabel="Suorituskieli">
              <TestIdText id="suorituskieli">
                {t(osasuoritus.suorituskieli?.nimi)}
              </TestIdText>
            </KeyValueRow>
            {osasuoritus.arviointi && (
              <KeyValueRow localizableLabel="Arviointi">
                {osasuoritus.arviointi.map((arviointi, arviointiIndex) => (
                  <KeyValueTable key={arviointiIndex}>
                    <KeyValueRow localizableLabel="Arvosana" innerKeyValueTable>
                      <TestIdText id={`arvosana.${arviointiIndex}`}>
                        {`${arviointi.arvosana.koodiarvo} (${t(arviointi.arvosana.nimi)})`}
                      </TestIdText>
                    </KeyValueRow>
                    <KeyValueRow
                      localizableLabel="Arviointipäivä"
                      innerKeyValueTable
                    >
                      <TestIdText id={`arviointipäivä.${arviointiIndex}`}>
                        {ISO2FinnishDate(arviointi.päivä)}
                      </TestIdText>
                    </KeyValueRow>
                  </KeyValueTable>
                ))}
              </KeyValueRow>
            )}
            {osasuoritus.tunnustettu && (
              <KeyValueRow localizableLabel="Tunnustettu">
                <OsaamisenTunnustusView
                  value={osasuoritus.tunnustettu}
                  index={index}
                />
              </KeyValueRow>
            )}
          </KeyValueTable>
        </Details>
      )}
      {editModalVisible && (
        <EditOmanÄidinkielenOpintojenOsasuoritusModal
          form={form}
          osasuoritus={osasuoritus}
          osasuoritusPath={osasuoritusPath}
          onClose={closeEditModal}
        />
      )}
    </div>
  )
}

const PreIB2019PuhviKoeRows: React.FC<PreIB2019KieliJaViestintäRowsProps> = ({
  form,
  päätasonSuoritus
}) => {
  const path = päätasonSuoritus.path
  const arvosanat =
    useKoodistoOfConstraint(
      useChildSchema(PuhviKoe2019.className, 'arvosana')
    ) || []

  const puhviKoeEiSyötetty = päätasonSuoritus.suoritus.puhviKoe === undefined
  const [showModal, setShowModal] = useState(false)
  const removePuhviKoe = () => {
    form.set(...päätasonSuoritus.pathTokens, ...['puhviKoe'])(undefined)
  }

  if (!form.editMode && puhviKoeEiSyötetty) {
    return null
  }
  return form.editMode && puhviKoeEiSyötetty ? (
    <>
      <SpacerLine />
      <KeyValueRow localizableLabel="Toisen asteen puheviestintätaitojen päättökoe">
        <FlatButton onClick={() => setShowModal(true)} testId="puhviKoe.lisää">
          {t('Lisää toisen asteen puheviestintätaitojen päättökoe')}
        </FlatButton>
        {showModal && (
          <UusiPuhviKoeModal
            onClose={() => setShowModal(false)}
            onSubmit={(arvosana, päivä) =>
              form.set(
                ...päätasonSuoritus.pathTokens,
                ...['puhviKoe']
              )(createPuhviKoe2019(arvosana, päivä))
            }
          />
        )}
      </KeyValueRow>
    </>
  ) : (
    <>
      <SpacerLine />
      <KeyValueRow localizableLabel="Toisen asteen puheviestintätaitojen päättökoe">
        <Removable
          isRemovable={form.editMode}
          onClick={removePuhviKoe}
          testId="puhviKoe"
        >
          <KeyValueTable>
            <KeyValueRow localizableLabel="Arvosana" innerKeyValueTable>
              {form.editMode ? (
                <KoodistoSelect
                  koodistoUri={'arviointiasteikkoyleissivistava'}
                  format={(koodiviite) =>
                    koodiviite.koodiarvo + ' ' + t(koodiviite.nimi)
                  }
                  koodiarvot={arvosanat.map(
                    (arvosana) => arvosana.koodiviite.koodiarvo
                  )}
                  onSelect={(koodiviite) => {
                    koodiviite &&
                      form.set(
                        ...päätasonSuoritus.pathTokens,
                        ...['puhviKoe', 'arvosana']
                      )(koodiviite)
                  }}
                  value={päätasonSuoritus.suoritus.puhviKoe?.arvosana.koodiarvo}
                  testId="puhviKoe.arvosana"
                />
              ) : (
                <TestIdText id="puhviKoe.arvosana">
                  {päätasonSuoritus.suoritus.puhviKoe?.arvosana.koodiarvo}{' '}
                  {t(päätasonSuoritus.suoritus.puhviKoe?.arvosana.nimi)}
                </TestIdText>
              )}
            </KeyValueRow>
            <KeyValueRow localizableLabel="Kuvaus" innerKeyValueTable>
              <FormField
                form={form}
                view={LocalizedTextView}
                edit={LocalizedTextEdit}
                testId="puhviKoe.kuvaus"
                path={path.prop('puhviKoe').optional().prop('kuvaus')}
              />
            </KeyValueRow>
            <KeyValueRow localizableLabel="Päivä" innerKeyValueTable>
              <FormField
                form={form}
                testId="puhviKoe.päivä"
                view={DateView}
                edit={DateEdit}
                editProps={{ align: 'right' }}
                path={path.prop('puhviKoe').optional().prop('päivä')}
              />
            </KeyValueRow>
          </KeyValueTable>
        </Removable>
      </KeyValueRow>
    </>
  )
}

const PreIB2019SuullisenKielitaidonKokeetRows: React.FC<
  PreIB2019KieliJaViestintäRowsProps
> = ({ form, päätasonSuoritus }) => {
  const [showModal, setShowModal] = useState(false)

  return (
    <>
      {A.isNonEmpty(
        päätasonSuoritus.suoritus.suullisenKielitaidonKokeet || []
      ) && <SpacerLine />}
      <KeyValueRow localizableLabel="Suullisen kielitaidon kokeet">
        {päätasonSuoritus.suoritus.suullisenKielitaidonKokeet?.map(
          (koe, index) => {
            return (
              <Removable
                isRemovable={form.editMode}
                onClick={removeAt(
                  päätasonSuoritus.suoritus.suullisenKielitaidonKokeet,
                  form,
                  [
                    ...päätasonSuoritus.pathTokens,
                    ...['suullisenKielitaidonKokeet']
                  ],
                  index
                )}
                key={`suullisenKielitaidonKoe.${index}`}
                testId={`suullisenKielitaidonKoe.${index}`}
              >
                <PreIB2019SuullisenKielitaidonKoeRows
                  form={form}
                  päätasonSuoritus={päätasonSuoritus}
                  index={index}
                />
                <SpacerLine />
              </Removable>
            )
          }
        )}
        {form.editMode && (
          <>
            <FlatButton
              onClick={() => setShowModal(true)}
              testId="suullisenKielitaidonKoe.lisää"
            >
              {t('Lisää suullisen kielitaidon koe')}
            </FlatButton>
            <Spacer />
          </>
        )}
        {showModal && (
          <UusiSuullisenKielitaidonKoeModal
            onClose={() => setShowModal(false)}
            onSubmit={(kieli, arvosana, taitotaso, päivä) =>
              pipe(
                päätasonSuoritus.suoritus.suullisenKielitaidonKokeet || [],
                A.append(
                  createSuullisenKielitaidonKoe2019(
                    kieli,
                    arvosana,
                    taitotaso,
                    päivä
                  )
                ),
                (kokeet) =>
                  form.set(
                    ...päätasonSuoritus.pathTokens,
                    ...['suullisenKielitaidonKokeet']
                  )(kokeet)
              )
            }
          />
        )}
      </KeyValueRow>
    </>
  )
}

type PreIB2019SuullisenKielitaidonKoeRowsProps =
  PreIB2019KieliJaViestintäRowsProps & {
    index: number
  }

const PreIB2019SuullisenKielitaidonKoeRows: React.FC<
  PreIB2019SuullisenKielitaidonKoeRowsProps
> = ({ form, päätasonSuoritus, index }) => {
  const path = päätasonSuoritus.path
  const taitotasot =
    useKoodistoOfConstraint(
      useChildSchema(SuullisenKielitaidonKoe2019.className, 'taitotaso')
    ) || []
  const arvosanat =
    useKoodistoOfConstraint(
      useChildSchema(SuullisenKielitaidonKoe2019.className, 'arvosana')
    ) || []

  return (
    <KeyValueTable>
      <KeyValueRow localizableLabel="Kieli" innerKeyValueTable>
        {form.editMode ? (
          <KoodistoSelect
            koodistoUri={'kielivalikoima'}
            onSelect={(koodiviite) => {
              koodiviite &&
                form.set(
                  ...päätasonSuoritus.pathTokens,
                  ...['suullisenKielitaidonKokeet', index, 'kieli']
                )(koodiviite)
            }}
            value={
              päätasonSuoritus.suoritus.suullisenKielitaidonKokeet?.at(index)
                ?.kieli.koodiarvo
            }
            testId={`suullisenKielitaidonKokeet.${index}.kieli`}
          />
        ) : (
          <TestIdText id={`suullisenKielitaidonKokeet.${index}.kieli`}>
            {t(
              päätasonSuoritus.suoritus.suullisenKielitaidonKokeet?.at(index)
                ?.kieli.nimi
            )}
          </TestIdText>
        )}
      </KeyValueRow>
      <KeyValueRow localizableLabel="Arvosana" innerKeyValueTable>
        {form.editMode ? (
          <KoodistoSelect
            koodistoUri={'arviointiasteikkoyleissivistava'}
            format={(koodiviite) =>
              koodiviite.koodiarvo + ' ' + t(koodiviite.nimi)
            }
            koodiarvot={arvosanat.map(
              (arvosana) => arvosana.koodiviite.koodiarvo
            )}
            onSelect={(koodiviite) => {
              koodiviite &&
                form.set(
                  ...päätasonSuoritus.pathTokens,
                  ...['suullisenKielitaidonKokeet', index, 'arvosana']
                )(koodiviite)
            }}
            value={
              päätasonSuoritus.suoritus.suullisenKielitaidonKokeet?.at(index)
                ?.arvosana.koodiarvo
            }
            testId={`suullisenKielitaidonKokeet.${index}.arvosana`}
          />
        ) : (
          <TestIdText id={`suullisenKielitaidonKokeet.${index}.arvosana`}>
            {
              päätasonSuoritus.suoritus.suullisenKielitaidonKokeet?.at(index)
                ?.arvosana.koodiarvo
            }{' '}
            {t(
              päätasonSuoritus.suoritus.suullisenKielitaidonKokeet?.at(index)
                ?.arvosana.nimi
            )}
          </TestIdText>
        )}
      </KeyValueRow>
      <KeyValueRow localizableLabel="Taitotaso" innerKeyValueTable>
        {form.editMode ? (
          <KoodistoSelect
            koodistoUri={'arviointiasteikkokehittyvankielitaidontasot'}
            koodiarvot={taitotasot.map(
              (taitotaso) => taitotaso.koodiviite.koodiarvo
            )}
            onSelect={(koodiviite) => {
              koodiviite &&
                form.set(
                  ...päätasonSuoritus.pathTokens,
                  ...['suullisenKielitaidonKokeet', index, 'taitotaso']
                )(koodiviite)
            }}
            value={
              päätasonSuoritus.suoritus.suullisenKielitaidonKokeet?.at(index)
                ?.taitotaso.koodiarvo
            }
            testId={`suullisenKielitaidonKokeet.${index}.taitotaso`}
          />
        ) : (
          <TestIdText id={`suullisenKielitaidonKokeet.${index}.taitotaso`}>
            {t(
              päätasonSuoritus.suoritus.suullisenKielitaidonKokeet?.at(index)
                ?.taitotaso.nimi
            )}
          </TestIdText>
        )}
      </KeyValueRow>
      <KeyValueRow localizableLabel="Kuvaus" innerKeyValueTable>
        <FormField
          form={form}
          view={LocalizedTextView}
          edit={LocalizedTextEdit}
          testId={`suullisenKielitaidonKokeet.${index}.kuvaus`}
          path={path
            .prop('suullisenKielitaidonKokeet')
            .optional()
            .at(index)
            .prop('kuvaus')}
        />
      </KeyValueRow>
      <KeyValueRow localizableLabel="Päivä" innerKeyValueTable>
        <FormField
          form={form}
          testId={`suullisenKielitaidonKokeet.${index}.päivä`}
          view={DateView}
          edit={DateEdit}
          editProps={{ align: 'right' }}
          path={path
            .prop('suullisenKielitaidonKokeet')
            .optional()
            .at(index)
            .prop('päivä')}
        />
      </KeyValueRow>
    </KeyValueTable>
  )
}

const createSuullisenKielitaidonKoe2019 = (
  kieli: Koodistokoodiviite<'kielivalikoima'>,
  arvosana: PreIBSuullisenKielitaidonKoe2019Arvosana,
  taitotaso: PreIBSuullisenKielitaidonKoe2019Taitotaso,
  päivä: string
): SuullisenKielitaidonKoe2019 => {
  return SuullisenKielitaidonKoe2019({ päivä, arvosana, taitotaso, kieli })
}

const createLukionOmanÄidinkielenOpinnot = (
  arvosana: PreIBOmanÄidinkielenOpinnot2019Arvosana,
  kieli: Koodistokoodiviite<'kielivalikoima'>,
  laajuus: LaajuusOpintopisteissä,
  arviointipäivä?: string
): LukionOmanÄidinkielenOpinnot => {
  return LukionOmanÄidinkielenOpinnot({
    arvosana,
    arviointipäivä: arviointipäivä,
    laajuus,
    kieli
  })
}

const createLukionOmanÄidinkielenOpinnotOsasuoritus = (
  koulutusmoduuli: LukionOmanÄidinkielenOpinto,
  arvosana?: PreIBOmanÄidinkielenOpintoOsasuorituksenArvosana,
  arviointipäivä?: string,
  kieli?: Koodistokoodiviite<'kieli'>
): LukionOmanÄidinkielenOpintojenOsasuoritus => {
  return LukionOmanÄidinkielenOpintojenOsasuoritus({
    koulutusmoduuli,
    arviointi:
      arvosana && arviointipäivä
        ? [
            LukionOmanÄidinkielenOpinnonOsasuorituksenArviointi({
              arvosana,
              päivä: arviointipäivä
            })
          ]
        : undefined,
    suorituskieli: kieli
  })
}

const createPuhviKoe2019 = (
  arvosana: PreIBPuhviKoe2019Arvosana,
  päivä: string
): PuhviKoe2019 => {
  return PuhviKoe2019({
    arvosana,
    päivä
  })
}

const removeAt =
  (
    arr: any[] | undefined,
    form: FormModel<IBOpiskeluoikeus>,
    path: PathToken[],
    index: number
  ) =>
  () => {
    pipe(
      arr || [],
      A.deleteAt(index),
      O.fold(
        () =>
          console.error(`Could not remove at ${index}, original array:`, arr),
        (newArray) => form.set(...path)(newArray)
      )
    )
  }

const osasuoritusOrd = Ord.contramap<
  string,
  LukionOmanÄidinkielenOpintojenOsasuoritus
>((osasuoritus: LukionOmanÄidinkielenOpintojenOsasuoritus) => {
  return osasuoritus.koulutusmoduuli.tunniste.koodiarvo
})(S.Ord)
