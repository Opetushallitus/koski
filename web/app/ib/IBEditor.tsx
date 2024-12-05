import { isEmpty } from 'fp-ts/lib/Array'
import React, { useCallback } from 'react'
import { useSchema } from '../appstate/constraints'
import {
  EditorContainer,
  usePäätasonSuoritus
} from '../components-v2/containers/EditorContainer'
import {
  Modal,
  ModalBody,
  ModalFooter,
  ModalTitle
} from '../components-v2/containers/Modal'
import { FlatButton } from '../components-v2/controls/FlatButton'
import { RaisedButton } from '../components-v2/controls/RaisedButton'
import { FormModel, useForm } from '../components-v2/forms/FormModel'
import { AdaptedOpiskeluoikeusEditorProps } from '../components-v2/interoperability/useUiAdapter'
import { Spacer } from '../components-v2/layout/Spacer'
import { OpiskeluoikeusTitle } from '../components-v2/opiskeluoikeus/OpiskeluoikeusTitle'
import { OppiaineTable } from '../components-v2/opiskeluoikeus/OppiaineTable'
import { SuorituksenVahvistusField } from '../components-v2/opiskeluoikeus/SuorituksenVahvistus'
import { t } from '../i18n/i18n'
import { IBOpiskeluoikeus } from '../types/fi/oph/koski/schema/IBOpiskeluoikeus'
import { IBTutkinto } from '../types/fi/oph/koski/schema/IBTutkinto'
import { isLaajuusKursseissa } from '../types/fi/oph/koski/schema/LaajuusKursseissa'
import { LukionOpiskeluoikeusjakso } from '../types/fi/oph/koski/schema/LukionOpiskeluoikeusjakso'
import { PreIBKoulutusmoduuli2015 } from '../types/fi/oph/koski/schema/PreIBKoulutusmoduuli2015'
import { PreIBKoulutusmoduuli2019 } from '../types/fi/oph/koski/schema/PreIBKoulutusmoduuli2019'
import { PreIBSuorituksenOsasuoritus2015 } from '../types/fi/oph/koski/schema/PreIBSuorituksenOsasuoritus2015'
import { appendOptional } from '../util/array'
import { koodiviiteId } from '../util/koodisto'
import { sum } from '../util/numbers'
import { PäätasonSuoritusOf } from '../util/opiskeluoikeus'
import { match } from '../util/patternmatch'
import { useBooleanState } from '../util/useBooleanState'
import { DialogSelect } from '../uusiopiskeluoikeus/components/DialogSelect'
import {
  ibKoulutusNimi,
  IBPäätasonSuoritusTiedot
} from './IBPaatasonSuoritusTiedot'
import {
  preIB2015Oppiainekategoriat,
  useAineryhmäOptions,
  useKielivalikoimaOptions,
  useMatematiikanOppimääräOptions,
  usePreIBTunnisteOptions,
  useÄidinkielenKieliOptions
} from './state/options'
import { useUusiPreIB2015OppiaineState } from './state/preIBOppiaine'
import { useKoodistoFiller } from '../appstate/koodisto'

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

      <OppiaineTable suoritus={päätasonSuoritus.suoritus} />

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
        match(päätasonSuoritus.suoritus.koulutusmoduuli)
          .isClass(PreIBKoulutusmoduuli2015, () => (
            <UusiPreIB2015OppiaineDialog
              onClose={hideAddOppiaineDialog}
              onSubmit={addOppiaine}
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

type UusiPreIB2015OppiaineDialogProps = {
  onClose: () => void
  onSubmit: (oppiaine: PreIBSuorituksenOsasuoritus2015) => void
}

const UusiPreIB2015OppiaineDialog: React.FC<
  UusiPreIB2015OppiaineDialogProps
> = (props) => {
  const state = useUusiPreIB2015OppiaineState()
  const tunnisteet = usePreIBTunnisteOptions(preIB2015Oppiainekategoriat)
  const kielet = useKielivalikoimaOptions(state.kieli.visible)
  const matematiikanOppimäärät = useMatematiikanOppimääräOptions(
    state.matematiikanOppimäärä.visible
  )
  const ryhmät = useAineryhmäOptions(state.ryhmä.visible)
  const äidinkielenKielet = useÄidinkielenKieliOptions(
    state.äidinkielenKieli.visible
  )

  const onSubmit = useCallback(() => {
    if (state.result) {
      props.onSubmit(state.result)
    }
  }, [props, state.result])

  return (
    <Modal>
      <ModalTitle>{t('Oppiaineen lisäys')}</ModalTitle>
      <ModalBody>
        {tunnisteet && (
          <label>
            {t('Oppiaine')}
            <DialogSelect
              options={tunnisteet}
              value={state.tunniste.value && koodiviiteId(state.tunniste.value)}
              onChange={(o) => state.tunniste.set(o?.value)}
              testId="tunniste"
            />
          </label>
        )}
        {state.kieli.visible && kielet && (
          <label>
            {t('Kieli')}
            <DialogSelect
              options={kielet}
              value={state.kieli.value && koodiviiteId(state.kieli.value)}
              onChange={(o) => state.kieli.set(o?.value)}
              testId="kieli"
            />
          </label>
        )}
        {state.ryhmä.visible && ryhmät && (
          <label>
            {t('Aineryhmä')}
            <DialogSelect
              options={ryhmät}
              value={state.ryhmä.value && koodiviiteId(state.ryhmä.value)}
              onChange={(o) => state.ryhmä.set(o?.value)}
              testId="ryhmä"
            />
          </label>
        )}
        {state.matematiikanOppimäärä.visible && matematiikanOppimäärät && (
          <label>
            {t('Oppimäärä')}
            <DialogSelect
              options={matematiikanOppimäärät}
              value={
                state.matematiikanOppimäärä.value &&
                koodiviiteId(state.matematiikanOppimäärä.value)
              }
              onChange={(o) => state.matematiikanOppimäärä.set(o?.value)}
              testId="matematiikanOppimäärä"
            />
          </label>
        )}
        {state.äidinkielenKieli.visible && äidinkielenKielet && (
          <label>
            {t('Kieli')}
            <DialogSelect
              options={äidinkielenKielet}
              value={
                state.äidinkielenKieli.value &&
                koodiviiteId(state.äidinkielenKieli.value)
              }
              onChange={(o) => state.äidinkielenKieli.set(o?.value)}
              testId="äidinkielenKieli"
            />
          </label>
        )}
      </ModalBody>
      <ModalFooter>
        <FlatButton onClick={props.onClose} testId="cancel">
          {t('Peruuta')}
        </FlatButton>
        <RaisedButton
          onClick={onSubmit}
          disabled={!state.result}
          testId="submit"
        >
          {t('Lisää opiskeluoikeus')}
        </RaisedButton>
      </ModalFooter>
    </Modal>
  )
}
