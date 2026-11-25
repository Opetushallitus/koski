import React, { useCallback, useContext, useEffect } from 'react'
import { AdaptedOpiskeluoikeusEditorProps } from '../components-v2/interoperability/useUiAdapter'
import { LukionOpiskeluoikeus } from '../types/fi/oph/koski/schema/LukionOpiskeluoikeus'
import { useSchema } from '../appstate/constraints'
import { OpiskeluoikeusContext } from '../appstate/opiskeluoikeus'
import { usePreferences } from '../appstate/preferences'
import { FormModel, useForm } from '../components-v2/forms/FormModel'
import { VirkailijaKansalainenContainer } from '../components-v2/containers/VirkailijaKansalainenContainer'
import {
  EditorContainer,
  usePäätasonSuoritus
} from '../components-v2/containers/EditorContainer'
import { LukionOpiskeluoikeusjakso } from '../types/fi/oph/koski/schema/LukionOpiskeluoikeusjakso'
import { LukionOpiskeluoikeudenTila } from '../types/fi/oph/koski/schema/LukionOpiskeluoikeudenTila'
import { UusiOpiskeluoikeusjakso } from '../components-v2/opiskeluoikeus/UusiOpiskeluoikeudenTilaModal'
import {
  OpiskeluoikeudenTilaEdit,
  OpiskeluoikeudenTilaView
} from '../components-v2/opiskeluoikeus/OpiskeluoikeudenTila'
import { Spacer } from '../components-v2/layout/Spacer'
import { localize, t } from '../i18n/i18n'
import { LocalizedString } from '../types/fi/oph/koski/schema/LocalizedString'
import { LukionSuorituksenTiedot } from './LukionSuorituksenTiedot'
import { LukionOppiaineet } from './LukionOppiaineet'
import { LukionPäätasonSuoritus } from '../types/fi/oph/koski/schema/LukionPaatasonSuoritus'
import {
  KeyValueRow,
  KeyValueTable
} from '../components-v2/containers/KeyValueTable'
import {
  BooleanEdit,
  BooleanView
} from '../components-v2/opiskeluoikeus/BooleanField'
import { FormField } from '../components-v2/forms/FormField'
import { lukionOppiaineenArviointiVaaditaanVahvistetultaSuoritukselta } from './validointi'
import { RemovePaatasonSuoritus } from '../components-v2/opiskeluoikeus/RemovePaatasonSuoritus'
import { deleteAt } from '../util/fp/arrays'
import { deletePäätasonSuoritus } from '../util/koskiApi'
import {
  getOpiskeluoikeusOid,
  getVersionumero,
  mergeOpiskeluoikeusVersionumeroAndRefetch
} from '../util/opiskeluoikeus'
import { isLukionOppiaineenSuoritus2019 } from '../types/fi/oph/koski/schema/LukionOppiaineenSuoritus2019'
import { isMuidenLukioOpintojenSuoritus2019 } from '../types/fi/oph/koski/schema/MuidenLukioOpintojenSuoritus2019'
import {
  isPaikallinenLukionOppiaine2019,
  PaikallinenLukionOppiaine2019
} from '../types/fi/oph/koski/schema/PaikallinenLukionOppiaine2019'
import { isLukionPaikallisenOpintojaksonSuoritus2019 } from '../types/fi/oph/koski/schema/LukionPaikallisenOpintojaksonSuoritus2019'
import { LukionPaikallinenOpintojakso2019 } from '../types/fi/oph/koski/schema/LukionPaikallinenOpintojakso2019'
import { LukionLisätiedot } from './LukionLisatiedot'

export type LukioEditorProps =
  AdaptedOpiskeluoikeusEditorProps<LukionOpiskeluoikeus>

// Pre-IB/LUVA eivät ole toistauseksi tuettuja suoritustyyppejä vain "Lukion oppimäärä"
// ja "Lukion aineopinnot" 2019.
const LukioEditor: React.FC<LukioEditorProps> = (props) => {
  const opiskeluoikeusSchema = useSchema(LukionOpiskeluoikeus.className)
  const form = useForm(props.opiskeluoikeus, false, opiskeluoikeusSchema, [
    lukionOppiaineenArviointiVaaditaanVahvistetultaSuoritukselta
  ])

  // paikallisten oppiaineiden/kurssien sama ratkaisu kuin
  // perusopetus-v2/PerusopetusEditor.tsx:ssä.
  const { setOrganisaatio } = useContext(OpiskeluoikeusContext)
  useEffect(() => {
    setOrganisaatio(props.opiskeluoikeus.oppilaitos)
    return () => setOrganisaatio(undefined)
  }, [props.opiskeluoikeus.oppilaitos, setOrganisaatio])

  useSyncPaikallisetOppiaineetJaKurssit(
    form.state,
    form.isSaved,
    props.opiskeluoikeus.oppilaitos?.oid
  )

  return (
    <VirkailijaKansalainenContainer
      opiskeluoikeus={form.state}
      opiskeluoikeudenNimi={t(lukionSuorituksenNimi(form.state.suoritukset[0]))}
    >
      <LukionPäätasonSuoritusEditor {...props} form={form} />
    </VirkailijaKansalainenContainer>
  )
}

const LukionPäätasonSuoritusEditor: React.FC<
  LukioEditorProps & { form: FormModel<LukionOpiskeluoikeus> }
> = (props) => {
  const [päätasonSuoritus, setPäätasonSuoritus] = usePäätasonSuoritus(
    props.form,
    0
  )

  const createOpiskeluoikeusjakso = (
    seed: UusiOpiskeluoikeusjakso<LukionOpiskeluoikeusjakso>
  ) => LukionOpiskeluoikeusjakso(seed)

  const removePäätasonSuoritus = useCallback(async () => {
    const poistettava =
      props.form.initialState.suoritukset[päätasonSuoritus.index]
    const oid = getOpiskeluoikeusOid(props.form.state)
    const versio = getVersionumero(props.form.state)
    if (!poistettava || !oid || versio === undefined) {
      return
    }

    setPäätasonSuoritus(0)

    const opiskeluoikeusPoistonJälkeen: LukionOpiskeluoikeus = {
      ...props.form.originalState,
      suoritukset: deleteAt(
        props.form.originalState.suoritukset,
        päätasonSuoritus.index
      )
    }
    props.form.save(
      () => deletePäätasonSuoritus(oid, versio, poistettava),
      (ooVersiot) => () =>
        mergeOpiskeluoikeusVersionumeroAndRefetch<LukionOpiskeluoikeus>(
          ooVersiot
        )(opiskeluoikeusPoistonJälkeen)
    )
  }, [props.form, päätasonSuoritus.index, setPäätasonSuoritus])

  return (
    <div className="LukioEditor">
      <EditorContainer
        form={props.form}
        oppijaOid={props.oppijaOid}
        invalidatable={props.invalidatable}
        suoritusIndex={päätasonSuoritus.index}
        onChangeSuoritus={setPäätasonSuoritus}
        testId={päätasonSuoritus.testId}
        createOpiskeluoikeusjakso={createOpiskeluoikeusjakso}
        suorituksenNimi={lukionSuorituksenNimi}
        lisätiedotContainer={LukionLisätiedot}
        opiskeluoikeudenTilaEditor={
          <>
            <FormField
              form={props.form}
              path={props.form.root.prop('tila')}
              view={OpiskeluoikeudenTilaView<LukionOpiskeluoikeudenTila>}
              edit={OpiskeluoikeudenTilaEdit<LukionOpiskeluoikeudenTila>}
              editProps={{
                // Vanha käyttöliittymä sallii opiskeluoikeuden tilan
                // merkitsemisen "Valmistunut"-tilaan, vaikka suoritusta ei
                // olisi vielä vahvistettu - EditorContainerin oletusarvo
                // (kaikilla suorituksilla on vahvistus) estäisi tämän muuten.
                enableValmistuminen: true,
                createJakso: createOpiskeluoikeusjakso
              }}
            />
            <Spacer />
            <LukionOpiskeluoikeudenLisäkentät form={props.form} />
          </>
        }
      >
        {props.form.state.suoritukset.length > 1 && (
          <RemovePaatasonSuoritus
            form={props.form}
            päätasonSuoritus={päätasonSuoritus}
            removePäätasonSuoritus={removePäätasonSuoritus}
            invalidatable={props.invalidatable}
          />
        )}
        <LukionSuorituksenTiedot
          form={props.form}
          päätasonSuoritus={päätasonSuoritus}
        />
        <Spacer />
        <LukionOppiaineet
          form={props.form}
          päätasonSuoritus={päätasonSuoritus}
        />
        <Spacer />
      </EditorContainer>
    </div>
  )
}

const lukionSuorituksenNimi = (s: LukionPäätasonSuoritus): LocalizedString =>
  s.tyyppi.nimi || localize(s.tyyppi.koodiarvo)

const useSyncPaikallisetOppiaineetJaKurssit = (
  state: LukionOpiskeluoikeus,
  isSaved: boolean,
  organisaatioOid?: string
) => {
  const { store: storeOppiaine } =
    usePreferences<PaikallinenLukionOppiaine2019>(
      organisaatioOid,
      'paikallinenlukionoppiaine2019'
    )
  const { store: storeKurssi } =
    usePreferences<LukionPaikallinenOpintojakso2019>(
      organisaatioOid,
      'lukionpaikallinenopintojakso2019'
    )

  useEffect(() => {
    if (!isSaved || !organisaatioOid) return

    const paikallisetOppiaineet: PaikallinenLukionOppiaine2019[] = []
    const paikallisetKurssit: LukionPaikallinenOpintojakso2019[] = []

    for (const suoritus of state.suoritukset) {
      const osasuoritukset = (suoritus as { osasuoritukset?: unknown[] })
        .osasuoritukset
      if (!osasuoritukset) continue
      for (const osasuoritus of osasuoritukset) {
        if (isLukionOppiaineenSuoritus2019(osasuoritus)) {
          if (
            isPaikallinenLukionOppiaine2019(osasuoritus.koulutusmoduuli) &&
            osasuoritus.koulutusmoduuli.tunniste.koodiarvo
          ) {
            paikallisetOppiaineet.push(osasuoritus.koulutusmoduuli)
          }
        } else if (!isMuidenLukioOpintojenSuoritus2019(osasuoritus)) {
          continue
        }
        for (const kurssi of osasuoritus.osasuoritukset || []) {
          if (
            isLukionPaikallisenOpintojaksonSuoritus2019(kurssi) &&
            kurssi.koulutusmoduuli.tunniste.koodiarvo
          ) {
            paikallisetKurssit.push(kurssi.koulutusmoduuli)
          }
        }
      }
    }

    const seenOppiaineet = new Set<string>()
    for (const oppiaine of paikallisetOppiaineet) {
      const koodi = oppiaine.tunniste.koodiarvo
      if (seenOppiaineet.has(koodi)) continue
      seenOppiaineet.add(koodi)
      storeOppiaine(koodi, oppiaine)
    }

    const seenKurssit = new Set<string>()
    for (const kurssi of paikallisetKurssit) {
      const koodi = kurssi.tunniste.koodiarvo
      if (seenKurssit.has(koodi)) continue
      seenKurssit.add(koodi)
      storeKurssi(koodi, kurssi)
    }
  }, [isSaved, state, organisaatioOid, storeOppiaine, storeKurssi])
}

const LukionOpiskeluoikeudenLisäkentät: React.FC<{
  form: FormModel<LukionOpiskeluoikeus>
}> = ({ form }) =>
  !form.editMode && !form.state.oppimääräSuoritettu ? null : (
    <KeyValueTable>
      <KeyValueRow localizableLabel="Oppimäärä suoritettu">
        <FormField
          form={form}
          path={form.root.prop('oppimääräSuoritettu').valueOr(false)}
          view={BooleanView}
          edit={BooleanEdit}
          viewProps={{ trueText: t('kyllä'), falseText: t('ei') }}
          testId="oppimääräSuoritettu"
        />
      </KeyValueRow>
    </KeyValueTable>
  )

export default LukioEditor
