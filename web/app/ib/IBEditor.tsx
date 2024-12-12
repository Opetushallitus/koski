import { isEmpty } from 'fp-ts/lib/Array'
import React, { useCallback } from 'react'
import { useSchema } from '../appstate/constraints'
import { useKoodistoFiller } from '../appstate/koodisto'
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
import { Checkbox } from '../components-v2/controls/Checkbox'
import { FlatButton } from '../components-v2/controls/FlatButton'
import { NumberField } from '../components-v2/controls/NumberField'
import { RaisedButton } from '../components-v2/controls/RaisedButton'
import { Select } from '../components-v2/controls/Select'
import { FormModel, useForm } from '../components-v2/forms/FormModel'
import { AdaptedOpiskeluoikeusEditorProps } from '../components-v2/interoperability/useUiAdapter'
import { Spacer } from '../components-v2/layout/Spacer'
import { OpiskeluoikeusTitle } from '../components-v2/opiskeluoikeus/OpiskeluoikeusTitle'
import {
  AddOppiaineenOsasuoritusDialog,
  isArvioinnillinenOppiaine,
  OppiaineTable
} from '../components-v2/opiskeluoikeus/OppiaineTable'
import {
  PaikallinenKoulutus,
  PaikallinenKoulutusFields
} from '../components-v2/opiskeluoikeus/PaikallinenKoulutusFields'
import { SuorituksenVahvistusField } from '../components-v2/opiskeluoikeus/SuorituksenVahvistus'
import { localize, t } from '../i18n/i18n'
import { IBOpiskeluoikeus } from '../types/fi/oph/koski/schema/IBOpiskeluoikeus'
import { IBTutkinto } from '../types/fi/oph/koski/schema/IBTutkinto'
import {
  isLaajuusKursseissa,
  LaajuusKursseissa
} from '../types/fi/oph/koski/schema/LaajuusKursseissa'
import { LukionOpiskeluoikeusjakso } from '../types/fi/oph/koski/schema/LukionOpiskeluoikeusjakso'
import {
  isPaikallinenKoodi,
  PaikallinenKoodi
} from '../types/fi/oph/koski/schema/PaikallinenKoodi'
import { PreIBKoulutusmoduuli2015 } from '../types/fi/oph/koski/schema/PreIBKoulutusmoduuli2015'
import { PreIBKoulutusmoduuli2019 } from '../types/fi/oph/koski/schema/PreIBKoulutusmoduuli2019'
import { PreIBKurssinSuoritus2015 } from '../types/fi/oph/koski/schema/PreIBKurssinSuoritus2015'
import { PreIBSuorituksenOsasuoritus2015 } from '../types/fi/oph/koski/schema/PreIBSuorituksenOsasuoritus2015'
import { appendOptional, deleteAt, replaceLast } from '../util/array'
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
import { UusiPreIB2015OppiaineDialog } from './dialogs/UusiPreIB2015OppiaineDialog'
import { createPreIBKurssinSuoritus2015 } from './oppiaineet/preIBKurssi2015'
import {
  useLukiokurssinTyypit,
  useOppiaineenKurssiOptions
} from './state/options'
import { useIBOsasuoritusState } from './state/osasuoritusState'
import { LukionArviointi } from '../types/fi/oph/koski/schema/LukionArviointi'
import { LukionModuulinTaiPaikallisenOpintojaksonArviointi2019 } from '../types/fi/oph/koski/schema/LukionModuulinTaiPaikallisenOpintojaksonArviointi2019'
import { IBKurssinArviointi } from '../types/fi/oph/koski/schema/IBKurssinArviointi'
import { Arviointi } from '../types/fi/oph/koski/schema/Arviointi'

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
        match(päätasonSuoritus.suoritus.koulutusmoduuli)
          .isClass(PreIBKoulutusmoduuli2015, () => (
            <UusiPreIB2015OppiaineDialog
              päätasonSuoritus={päätasonSuoritus.suoritus}
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

const AddIBOsasuoritusDialog: AddOppiaineenOsasuoritusDialog<
  PreIBKurssinSuoritus2015
> = ({ onAdd, ...props }) => {
  const koulutus = props.oppiaine.koulutusmoduuli
  const state = useIBOsasuoritusState(koulutus, createPreIBKurssinSuoritus2015)

  const lukioTunnisteet = useOppiaineenKurssiOptions(
    !isPaikallinenKoodi(props.oppiaine.koulutusmoduuli.tunniste)
      ? props.oppiaine.koulutusmoduuli.tunniste
      : undefined
  )
  const kurssityypit = useLukiokurssinTyypit(state.lukiokurssinTyyppi.visible)

  const onPaikallinenKoulutus = useCallback(
    (paikallinen?: PaikallinenKoulutus) => {
      if (paikallinen) {
        state.paikallinenTunniste.set(
          PaikallinenKoodi({
            koodiarvo: paikallinen.koodiarvo,
            nimi: localize(paikallinen.nimi)
          })
        )
        state.kuvaus.set(localize(paikallinen.kuvaus))
      }
    },
    [state.kuvaus, state.paikallinenTunniste]
  )

  const addOsasuoritus = useCallback(() => {
    state.result && onAdd(state.result)
  }, [onAdd, state.result])

  return (
    <Modal>
      <ModalTitle>{t('Lisää osasuoritus')}</ModalTitle>
      <ModalBody>
        {state.tunniste.visible && lukioTunnisteet && (
          <label>
            {t('Osasuoritus')}
            <Select
              inlineOptions
              options={lukioTunnisteet}
              value={state.tunniste.value && koodiviiteId(state.tunniste.value)}
              onChange={(o) => state.tunniste.set(o?.value as any)}
              testId="tunniste"
            />
          </label>
        )}
        {state.paikallinenTunniste.visible && (
          <PaikallinenKoulutusFields onChange={onPaikallinenKoulutus} />
        )}
        {state.lukiokurssinTyyppi.visible && kurssityypit && (
          <label>
            {t('Kurssin tyyppi')}
            <DialogSelect
              options={kurssityypit}
              value={
                state.lukiokurssinTyyppi.value &&
                koodiviiteId(state.lukiokurssinTyyppi.value)
              }
              onChange={(o) => state.lukiokurssinTyyppi.set(o?.value)}
              testId="tunniste"
            />
          </label>
        )}
        {state.laajuus.visible && (
          <label>
            {t('Laajuus')}
            <NumberField
              value={state.laajuus.value?.arvo}
              onChange={(arvo) =>
                state.laajuus.set(LaajuusKursseissa({ arvo }))
              }
              testId="laajuus"
            />
          </label>
        )}
        {state.pakollinen.visible && (
          <label>
            <Checkbox
              label={t('Pakollinen')}
              checked={!!state.pakollinen.value}
              onChange={state.pakollinen.set}
              testId="pakollinen"
            />
          </label>
        )}
      </ModalBody>
      <ModalFooter>
        <FlatButton onClick={props.onClose}>{t('Peruuta')}</FlatButton>
        <RaisedButton onClick={addOsasuoritus} disabled={!state.result}>
          {t('Lisää')}
        </RaisedButton>
      </ModalFooter>
    </Modal>
  )
}
