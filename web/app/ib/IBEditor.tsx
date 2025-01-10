import { isEmpty } from 'fp-ts/lib/Array'
import React, { useCallback, useMemo } from 'react'
import { useSchema } from '../appstate/constraints'
import { useKoodistoFiller } from '../appstate/koodisto'
import { TestIdRoot, TestIdText } from '../appstate/useTestId'
import {
  EditorContainer,
  usePäätasonSuoritus
} from '../components-v2/containers/EditorContainer'
import { RaisedButton } from '../components-v2/controls/RaisedButton'
import { FormModel, useForm } from '../components-v2/forms/FormModel'
import { AdaptedOpiskeluoikeusEditorProps } from '../components-v2/interoperability/useUiAdapter'
import { Spacer } from '../components-v2/layout/Spacer'
import { OpiskeluoikeusTitle } from '../components-v2/opiskeluoikeus/OpiskeluoikeusTitle'
import {
  Oppiaine,
  OppiaineTable
} from '../components-v2/opiskeluoikeus/OppiaineTable'
import { SuorituksenVahvistusField } from '../components-v2/opiskeluoikeus/SuorituksenVahvistus'
import { t } from '../i18n/i18n'
import { Arviointi } from '../types/fi/oph/koski/schema/Arviointi'
import { isIBAineRyhmäOppiaine } from '../types/fi/oph/koski/schema/IBAineRyhmaOppiaine'
import { IBOpiskeluoikeus } from '../types/fi/oph/koski/schema/IBOpiskeluoikeus'
import { IBPäätasonSuoritus } from '../types/fi/oph/koski/schema/IBPaatasonSuoritus'
import { IBTutkinto } from '../types/fi/oph/koski/schema/IBTutkinto'
import { isLaajuusKursseissa } from '../types/fi/oph/koski/schema/LaajuusKursseissa'
import { LukionOpiskeluoikeusjakso } from '../types/fi/oph/koski/schema/LukionOpiskeluoikeusjakso'
import { isMuidenLukioOpintojenPreIBSuoritus2019 } from '../types/fi/oph/koski/schema/MuidenLukioOpintojenPreIBSuoritus2019'
import { PreIBKoulutusmoduuli2015 } from '../types/fi/oph/koski/schema/PreIBKoulutusmoduuli2015'
import { PreIBKoulutusmoduuli2019 } from '../types/fi/oph/koski/schema/PreIBKoulutusmoduuli2019'
import { appendOptional } from '../util/array'
import { parasArviointi } from '../util/arvioinnit'
import { sum } from '../util/numbers'
import { PäätasonSuoritusOf } from '../util/opiskeluoikeus'
import { match } from '../util/patternmatch'
import { OsasuoritusOf } from '../util/schema'
import { useBooleanState } from '../util/useBooleanState'
import { IBPäätasonSuoritusTiedot } from './IBPaatasonSuoritusTiedot'
import { UusiIBTutkintoOppiaineDialog } from './dialogs/UusiIBTutkintoOppiaineDialog'
import { UusiIBTutkintoOsasuoritusDialog } from './dialogs/UusiIBTutkintoOsasuoritusDialog'
import { UusiPreIB2015OppiaineDialog } from './dialogs/UusiPreIB2015OppiaineDialog'
import { UusiPreIB2015OsasuoritusDialog } from './dialogs/UusiPreIB2015OsasuoritusDialog'
import { UusiPreIB2019OppiaineDialog } from './dialogs/UusiPreIB2019OppiaineDialog'
import { UusiPreIB2019OsasuoritusDialog } from './dialogs/UusiPreIB2019OsasuoritusDialog'
import { containsPaikallinenSuoritus } from '../util/suoritus'
import { IBTutkinnonSuoritus } from '../types/fi/oph/koski/schema/IBTutkinnonSuoritus'

export type IBEditorProps = AdaptedOpiskeluoikeusEditorProps<IBOpiskeluoikeus>

export const IBEditor: React.FC<IBEditorProps> = (props) => {
  const opiskeluoikeusSchema = useSchema('IBOpiskeluoikeus')
  const form = useForm(props.opiskeluoikeus, false, opiskeluoikeusSchema)

  return (
    <>
      <OpiskeluoikeusTitle
        opiskeluoikeus={form.state}
        opiskeluoikeudenNimi={'ib-tutkinto'}
      />
      <IBPäätasonSuoritusEditor {...props} form={form} />
    </>
  )
}

const IBPäätasonSuoritusEditor: React.FC<
  IBEditorProps & {
    form: FormModel<IBOpiskeluoikeus>
  }
> = ({ form, oppijaOid, invalidatable, opiskeluoikeus }) => {
  const [päätasonSuoritus, setPäätasonSuoritus] = usePäätasonSuoritus(form)
  const organisaatio =
    opiskeluoikeus.oppilaitos || opiskeluoikeus.koulutustoimija
  const kurssejaYhteensä = useSuoritetutKurssitYhteensä(
    päätasonSuoritus.suoritus
  )
  const [addOppiaineVisible, showAddOppiaineDialog, hideAddOppiaineDialog] =
    useBooleanState(false)
  const fillKoodistot = useKoodistoFiller()

  const addOppiaine = useCallback(
    async (oppiaine: OsasuoritusOf<IBPäätasonSuoritus>) => {
      form.modify(
        ...päätasonSuoritus.pathTokens,
        'osasuoritukset'
      )(appendOptional(await fillKoodistot(oppiaine)))
      hideAddOppiaineDialog()
    },
    [fillKoodistot, form, hideAddOppiaineDialog, päätasonSuoritus.pathTokens]
  )

  const valmis = useOsasuorituksetValmiit(päätasonSuoritus.suoritus)

  const paikallisiaSuorituksia = useMemo(
    () =>
      päätasonSuoritus.suoritus.$class !== IBTutkinnonSuoritus.className &&
      containsPaikallinenSuoritus(päätasonSuoritus.suoritus),
    [päätasonSuoritus]
  )

  return (
    <EditorContainer
      form={form}
      oppijaOid={oppijaOid}
      invalidatable={invalidatable}
      onChangeSuoritus={setPäätasonSuoritus}
      createOpiskeluoikeusjakso={LukionOpiskeluoikeusjakso}
    >
      <TestIdRoot id={päätasonSuoritus.testId}>
        <IBPäätasonSuoritusTiedot
          form={form}
          päätasonSuoritus={päätasonSuoritus}
        />

        <Spacer />

        <SuorituksenVahvistusField
          form={form}
          suoritusPath={päätasonSuoritus.path}
          organisaatio={organisaatio}
          disableAdd={!valmis}
        />

        <Spacer />

        <OppiaineTable
          selectedSuoritus={päätasonSuoritus}
          form={form}
          groupBy={groupByAineryhmä}
          addOsasuoritusDialog={match(päätasonSuoritus.suoritus.koulutusmoduuli)
            .isClass(
              PreIBKoulutusmoduuli2015,
              () => UusiPreIB2015OsasuoritusDialog
            )
            .isClass(
              PreIBKoulutusmoduuli2019,
              () => UusiPreIB2019OsasuoritusDialog
            )
            .isClass(IBTutkinto, () => UusiIBTutkintoOsasuoritusDialog)
            .get()}
        />

        <footer className="IBPäätasonSuoritusEditor__footer">
          {form.editMode && (
            <RaisedButton onClick={showAddOppiaineDialog} testId="addOppiaine">
              {t('Lisää oppiaine')}
            </RaisedButton>
          )}
          {kurssejaYhteensä !== null && (
            <>
              <div className="IBPäätasonSuoritusEditor__yhteensä">
                {t('Suoritettujen kurssien laajuus yhteensä')}
                {': '}
                <TestIdText id="suoritettujaKurssejaYhteensä">
                  {kurssejaYhteensä}
                </TestIdText>
              </div>
              {paikallisiaSuorituksia && (
                <div>{`* = ${t('paikallinen kurssi tai oppiaine')}`}</div>
              )}
            </>
          )}
        </footer>

        {addOppiaineVisible &&
          organisaatio &&
          match(päätasonSuoritus.suoritus.koulutusmoduuli)
            .isClass(PreIBKoulutusmoduuli2015, () => (
              <UusiPreIB2015OppiaineDialog
                päätasonSuoritus={päätasonSuoritus.suoritus}
                onClose={hideAddOppiaineDialog}
                onSubmit={addOppiaine}
                organisaatioOid={organisaatio?.oid}
              />
            ))
            .isClass(PreIBKoulutusmoduuli2019, () => (
              <UusiPreIB2019OppiaineDialog
                päätasonSuoritus={päätasonSuoritus.suoritus}
                onClose={hideAddOppiaineDialog}
                onSubmit={addOppiaine}
                organisaatioOid={organisaatio?.oid}
              />
            ))
            .isClass(IBTutkinto, () => (
              <UusiIBTutkintoOppiaineDialog
                päätasonSuoritus={päätasonSuoritus.suoritus}
                onClose={hideAddOppiaineDialog}
                onSubmit={addOppiaine}
                organisaatioOid={organisaatio?.oid}
              />
            ))
            .getOrNull()}
      </TestIdRoot>
    </EditorContainer>
  )
}

const groupByAineryhmä = (oppiaine: Oppiaine): string =>
  isIBAineRyhmäOppiaine(oppiaine.koulutusmoduuli)
    ? t(oppiaine.koulutusmoduuli.ryhmä.nimi)
    : ''

const useSuoritetutKurssitYhteensä = (
  pts: PäätasonSuoritusOf<IBOpiskeluoikeus>
): number | null => {
  const laajuudet = (pts.osasuoritukset || []).flatMap((oppiaine) =>
    (oppiaine.osasuoritukset || []).flatMap((kurssi) =>
      isLaajuusKursseissa(kurssi.koulutusmoduuli.laajuus)
        ? [kurssi.koulutusmoduuli.laajuus.arvo]
        : []
    )
  )
  return isEmpty(laajuudet) ? null : sum(laajuudet)
}

const useOsasuorituksetValmiit = (pts: IBPäätasonSuoritus): boolean =>
  useMemo(() => {
    const oppiaineet = pts.osasuoritukset
    if (!oppiaineet || oppiaineet.length === 0) return false

    return oppiaineet.every((oppiaine) => {
      if (isMuidenLukioOpintojenPreIBSuoritus2019(oppiaine)) {
        return true
      }
      const arviointi =
        oppiaine.arviointi && parasArviointi(oppiaine.arviointi as Arviointi[])
      return !!arviointi?.hyväksytty
    })
  }, [pts.osasuoritukset])
