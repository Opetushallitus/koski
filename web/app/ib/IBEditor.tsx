import { isEmpty } from 'fp-ts/lib/Array'
import React, { useCallback } from 'react'
import { useSchema } from '../appstate/constraints'
import { useKoodistoFiller } from '../appstate/koodisto'
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
  isArvioinnillinenOppiaine,
  OppiaineTable
} from '../components-v2/opiskeluoikeus/OppiaineTable'
import { SuorituksenVahvistusField } from '../components-v2/opiskeluoikeus/SuorituksenVahvistus'
import { t } from '../i18n/i18n'
import { Arviointi } from '../types/fi/oph/koski/schema/Arviointi'
import { IBKurssinArviointi } from '../types/fi/oph/koski/schema/IBKurssinArviointi'
import { IBOpiskeluoikeus } from '../types/fi/oph/koski/schema/IBOpiskeluoikeus'
import { IBTutkinto } from '../types/fi/oph/koski/schema/IBTutkinto'
import { isLaajuusKursseissa } from '../types/fi/oph/koski/schema/LaajuusKursseissa'
import { LukionArviointi } from '../types/fi/oph/koski/schema/LukionArviointi'
import { LukionModuulinTaiPaikallisenOpintojaksonArviointi2019 } from '../types/fi/oph/koski/schema/LukionModuulinTaiPaikallisenOpintojaksonArviointi2019'
import { LukionOpiskeluoikeusjakso } from '../types/fi/oph/koski/schema/LukionOpiskeluoikeusjakso'
import { PreIBKoulutusmoduuli2015 } from '../types/fi/oph/koski/schema/PreIBKoulutusmoduuli2015'
import { PreIBKoulutusmoduuli2019 } from '../types/fi/oph/koski/schema/PreIBKoulutusmoduuli2019'
import { PreIBKurssinSuoritus2015 } from '../types/fi/oph/koski/schema/PreIBKurssinSuoritus2015'
import { PreIBSuorituksenOsasuoritus2015 } from '../types/fi/oph/koski/schema/PreIBSuorituksenOsasuoritus2015'
import { appendOptional, deleteAt, replaceLast } from '../util/array'
import { sum } from '../util/numbers'
import { PäätasonSuoritusOf } from '../util/opiskeluoikeus'
import { match } from '../util/patternmatch'
import { useBooleanState } from '../util/useBooleanState'
import {
  ibKoulutusNimi,
  IBPäätasonSuoritusTiedot
} from './IBPaatasonSuoritusTiedot'
import { AddIBOsasuoritusDialog } from './dialogs/AddIBOsasuoritusDialog'
import { UusiPreIB2015OppiaineDialog } from './dialogs/UusiPreIB2015OppiaineDialog'

export type IBEditorProps = AdaptedOpiskeluoikeusEditorProps<IBOpiskeluoikeus>

export const IBEditor: React.FC<IBEditorProps> = (props) => {
  const opiskeluoikeusSchema = useSchema('IBOpiskeluoikeus')
  const form = useForm(props.opiskeluoikeus, false, opiskeluoikeusSchema)

  return (
    <>
      <OpiskeluoikeusTitle
        opiskeluoikeus={form.state}
        opiskeluoikeudenNimi={ibKoulutusNimi(form.state)}
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
    async (oppiaine: PreIBSuorituksenOsasuoritus2015) => {
      form.updateAt(
        päätasonSuoritus.path.prop('osasuoritukset') as any,
        appendOptional(await fillKoodistot(oppiaine))
      )
      hideAddOppiaineDialog()
    },
    [fillKoodistot, form, hideAddOppiaineDialog, päätasonSuoritus.path]
  )

  const deleteOppiaine = useCallback(
    (index: number) => {
      form.updateAt(
        päätasonSuoritus.path.prop('osasuoritukset').optional(),
        (ts) => deleteAt(index)(ts as any[])
      )
    },
    [form, päätasonSuoritus.path]
  )

  const deleteKurssi = useCallback(
    (oppiaineIndex: number, kurssiIndex: number) => {
      form.updateAt(
        päätasonSuoritus.path
          .prop('osasuoritukset')
          .optional()
          .at(oppiaineIndex)
          .prop('osasuoritukset')
          .optional(),
        (ts) => deleteAt(kurssiIndex)(ts as any[])
      )
    },
    [form, päätasonSuoritus.path]
  )

  const addOsasuoritus = useCallback(
    (oppiaineIndex: number, osasuoritus: PreIBKurssinSuoritus2015) => {
      form.updateAt(
        päätasonSuoritus.path
          .prop('osasuoritukset')
          .optional()
          .at(oppiaineIndex)
          .prop('osasuoritukset') as any,
        appendOptional(osasuoritus)
      )
    },
    [form, päätasonSuoritus.path]
  )

  const addKurssiArviointi = useCallback(
    (
      oppiaineIndex: number,
      osasuoritusIndex: number,
      arviointi:
        | LukionArviointi
        | IBKurssinArviointi
        | LukionModuulinTaiPaikallisenOpintojaksonArviointi2019
    ) => {
      form.updateAt(
        päätasonSuoritus.path
          .prop('osasuoritukset')
          .optional()
          .at(oppiaineIndex)
          .prop('osasuoritukset')
          .optional()
          .at(osasuoritusIndex)
          .prop('arviointi') as any,
        replaceLast(arviointi)
      )
    },
    [form, päätasonSuoritus.path]
  )

  const addOppiaineArviointi = useCallback(
    (oppiaineIndex: number, arviointi: Arviointi) => {
      form.updateAt(
        päätasonSuoritus.path
          .prop('osasuoritukset')
          .optional()
          .at(oppiaineIndex)
          .guard(isArvioinnillinenOppiaine)
          .prop('arviointi') as any,
        replaceLast(arviointi)
      )
    },
    [form, päätasonSuoritus.path]
  )

  return (
    <EditorContainer
      form={form}
      oppijaOid={oppijaOid}
      invalidatable={invalidatable}
      onChangeSuoritus={() => console.log('todo: onChangeSuoritus')}
      createOpiskeluoikeusjakso={LukionOpiskeluoikeusjakso}
    >
      <IBPäätasonSuoritusTiedot
        form={form}
        päätasonSuoritus={päätasonSuoritus}
      />

      <Spacer />

      <SuorituksenVahvistusField
        form={form}
        suoritusPath={päätasonSuoritus.path}
        organisaatio={organisaatio}
        disableAdd={true} // TODO
      />

      <Spacer />

      <OppiaineTable
        suoritus={päätasonSuoritus.suoritus}
        form={form}
        onDelete={deleteOppiaine}
        onDeleteKurssi={deleteKurssi}
        addOsasuoritusDialog={AddIBOsasuoritusDialog}
        onAddOsasuoritus={addOsasuoritus}
        onArviointi={addKurssiArviointi}
        onOppiaineArviointi={addOppiaineArviointi}
      />

      {kurssejaYhteensä !== null && (
        <footer className="IBPäätasonSuoritusEditor__footer">
          {form.editMode && (
            <RaisedButton onClick={showAddOppiaineDialog}>
              {t('Lisää oppiaine')}
            </RaisedButton>
          )}
          <div className="IBPäätasonSuoritusEditor__yhteensä">
            {t('Suoritettujen kurssien määrä yhteensä')}
            {': '}
            {kurssejaYhteensä}
          </div>
        </footer>
      )}

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
          .isClass(PreIBKoulutusmoduuli2019, () => <p>TODO</p>)
          .isClass(IBTutkinto, () => <p>TODO</p>)
          .getOrNull()}
    </EditorContainer>
  )
}

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
