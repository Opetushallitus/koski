import React, { useCallback, useMemo, useState } from 'react'
import { AdaptedOpiskeluoikeusEditorProps } from '../components-v2/interoperability/useUiAdapter'
import { PerusopetuksenOpiskeluoikeus } from '../types/fi/oph/koski/schema/PerusopetuksenOpiskeluoikeus'
import { useSchema } from '../appstate/constraints'
import { FormModel, useForm } from '../components-v2/forms/FormModel'
import { arviointiVaaditaanVahvistetultaSuoritukselta } from '../components-v2/opiskeluoikeus/validointi'
import { VirkailijaKansalainenContainer } from '../components-v2/containers/VirkailijaKansalainenContainer'
import {
  EditorContainer,
  usePäätasonSuoritus
} from '../components-v2/containers/EditorContainer'
import { RemovePaatasonSuoritus } from '../components-v2/opiskeluoikeus/RemovePaatasonSuoritus'
import { NuortenPerusopetuksenOpiskeluoikeusjakso } from '../types/fi/oph/koski/schema/NuortenPerusopetuksenOpiskeluoikeusjakso'
import { UusiOpiskeluoikeusjakso } from '../components-v2/opiskeluoikeus/UusiOpiskeluoikeudenTilaModal'
import { Spacer } from '../components-v2/layout/Spacer'
import { localize, t } from '../i18n/i18n'
import { LocalizedString } from '../types/fi/oph/koski/schema/LocalizedString'
import { PerusopetuksenSuorituksenTiedot } from './PerusopetuksenSuorituksenTiedot'
import { PerusopetuksenOppiaineet } from './PerusopetuksenOppiaineet'
import { PerusopetuksenLisatiedot } from './PerusopetuksenLisatiedot'
import { PerusopetuksenPäätasonSuoritus } from '../types/fi/oph/koski/schema/PerusopetuksenPaatasonSuoritus'
import { isNuortenPerusopetuksenOppimääränSuoritus } from '../types/fi/oph/koski/schema/NuortenPerusopetuksenOppimaaranSuoritus'
import { isNuortenPerusopetuksenOppiaineenOppimääränSuoritus } from '../types/fi/oph/koski/schema/NuortenPerusopetuksenOppiaineenOppimaaranSuoritus'
import {
  PerusopetuksenVuosiluokanSuoritus,
  isPerusopetuksenVuosiluokanSuoritus
} from '../types/fi/oph/koski/schema/PerusopetuksenVuosiluokanSuoritus'
import { isTerminaalitila } from '../util/opiskeluoikeus'
import { viimeisinOpiskelujaksonTila } from '../util/schema'
import { UusiPerusopetuksenVuosiluokanSuoritusModal } from './UusiPerusopetuksenVuosiluokanSuoritusModal'
import { puuttuvatLuokkaAsteet } from './luokkaAsteenOppiaineet'
import { usePreferences } from '../appstate/preferences'
import { OpiskeluoikeusContext } from '../appstate/opiskeluoikeus'
import {
  isNuortenPerusopetuksenPaikallinenOppiaine,
  NuortenPerusopetuksenPaikallinenOppiaine
} from '../types/fi/oph/koski/schema/NuortenPerusopetuksenPaikallinenOppiaine'
import { isNuortenPerusopetuksenOppiaineenSuoritus } from '../types/fi/oph/koski/schema/NuortenPerusopetuksenOppiaineenSuoritus'
import { perusopetuksenPäätasonSuoritusEq } from './paatasonSuoritusPoisto'
import { useRemovePäätasonSuoritus } from '../components-v2/forms/useRemovePaatasonSuoritus'
import {
  oletuksenaAvattavanSuorituksenIndeksi,
  sortPäätasonSuoritukset
} from './paatasonSuoritustenJarjestys'

export type PerusopetusEditorProps =
  AdaptedOpiskeluoikeusEditorProps<PerusopetuksenOpiskeluoikeus>

const PerusopetusEditor: React.FC<PerusopetusEditorProps> = (props) => {
  const opiskeluoikeusSchema = useSchema(PerusopetuksenOpiskeluoikeus.className)
  const opiskeluoikeus = React.useMemo(
    () => ({
      ...props.opiskeluoikeus,
      suoritukset: sortPerusopetuksenSuoritukset(
        props.opiskeluoikeus.suoritukset
      )
    }),
    [props.opiskeluoikeus]
  )
  const form = useForm(opiskeluoikeus, false, opiskeluoikeusSchema, [
    arviointiVaaditaanVahvistetultaSuoritukselta
  ])

  const { setOrganisaatio } = React.useContext(OpiskeluoikeusContext)
  React.useEffect(() => {
    setOrganisaatio(props.opiskeluoikeus.oppilaitos)
    return () => setOrganisaatio(undefined)
  }, [props.opiskeluoikeus.oppilaitos, setOrganisaatio])

  useSyncPaikallisetOppiaineet(
    form.state,
    form.isSaved,
    props.opiskeluoikeus.oppilaitos?.oid
  )

  return (
    <VirkailijaKansalainenContainer
      opiskeluoikeus={form.state}
      opiskeluoikeudenNimi={t(
        form.state.suoritukset[0]?.koulutusmoduuli?.tunniste?.nimi
      )}
    >
      <PerusopetuksenPäätasonSuoritusEditor {...props} form={form} />
    </VirkailijaKansalainenContainer>
  )
}

const PerusopetuksenPäätasonSuoritusEditor: React.FC<
  PerusopetusEditorProps & {
    form: FormModel<PerusopetuksenOpiskeluoikeus>
  }
> = (props) => {
  const initialSuoritusIndex = useMemo(
    () =>
      oletuksenaAvattavanSuorituksenIndeksi(
        props.form.state.suoritukset,
        isNuortenPerusopetuksenOppimääränSuoritus,
        isPerusopetuksenVuosiluokanSuoritus
      ),
    [props.form.state.suoritukset]
  )

  const [päätasonSuoritus, setPäätasonSuoritus] = usePäätasonSuoritus(
    props.form,
    initialSuoritusIndex
  )

  const createOpiskeluoikeusjakso = (
    seed: UusiOpiskeluoikeusjakso<NuortenPerusopetuksenOpiskeluoikeusjakso>
  ) => NuortenPerusopetuksenOpiskeluoikeusjakso(seed)

  const addSuoritusProps = useVuosiluokanSuorituksenLisäys(
    props.form,
    setPäätasonSuoritus
  )

  const removePäätasonSuoritus = useRemovePäätasonSuoritus(
    props.form,
    päätasonSuoritus.suoritus,
    perusopetuksenPäätasonSuoritusEq,
    () => setPäätasonSuoritus(0)
  )

  return (
    <>
      <EditorContainer
        form={props.form}
        oppijaOid={props.oppijaOid}
        invalidatable={props.invalidatable}
        suoritusIndex={päätasonSuoritus.index}
        onChangeSuoritus={setPäätasonSuoritus}
        testId={päätasonSuoritus.testId}
        createOpiskeluoikeusjakso={createOpiskeluoikeusjakso}
        lisätiedotContainer={PerusopetuksenLisatiedot}
        suorituksenNimi={perusopetuksenSuorituksenNimi}
        {...addSuoritusProps.editorContainerProps}
      >
        {props.form.state.suoritukset.length > 1 && (
          <RemovePaatasonSuoritus
            form={props.form}
            päätasonSuoritus={päätasonSuoritus}
            removePäätasonSuoritus={removePäätasonSuoritus}
            invalidatable={props.invalidatable}
          />
        )}
        <PerusopetuksenSuorituksenTiedot
          form={props.form}
          päätasonSuoritus={päätasonSuoritus}
        />
        <Spacer />
        <PerusopetuksenOppiaineet
          form={props.form}
          päätasonSuoritus={päätasonSuoritus}
        />
        <Spacer />
      </EditorContainer>
      {addSuoritusProps.modal}
    </>
  )
}

const useVuosiluokanSuorituksenLisäys = (
  form: FormModel<PerusopetuksenOpiskeluoikeus>,
  setPäätasonSuoritus: (suoritusIndex: number) => void
) => {
  const [modalVisible, setModalVisible] = useState(false)

  const missingLuokkaAsteet = useMemo(
    () =>
      puuttuvatLuokkaAsteet(
        form.state.suoritukset.flatMap((s) =>
          isPerusopetuksenVuosiluokanSuoritus(s) && !s.jääLuokalle
            ? [s.koulutusmoduuli.tunniste.koodiarvo]
            : []
        )
      ),
    [form.state.suoritukset]
  )

  const voiLisätä = useMemo(() => {
    const vTila = viimeisinOpiskelujaksonTila(form.state.tila)
    if (!vTila || isTerminaalitila(vTila)) return false
    if (missingLuokkaAsteet.length === 0) return false
    return !form.state.suoritukset.some(
      isNuortenPerusopetuksenOppiaineenOppimääränSuoritus
    )
  }, [form.state.suoritukset, form.state.tila, missingLuokkaAsteet.length])

  const onCreateSuoritus = useCallback(() => {
    setModalVisible(true)
  }, [])

  const onSubmit = useCallback(
    (uusiSuoritus: PerusopetuksenVuosiluokanSuoritus) => {
      const suoritukset = sortPerusopetuksenSuoritukset([
        ...form.state.suoritukset,
        uusiSuoritus
      ])
      const newIndex = suoritukset.indexOf(uusiSuoritus)

      form.modify('suoritukset')(() => suoritukset)
      setModalVisible(false)
      setPäätasonSuoritus(
        newIndex >= 0 ? newIndex : form.state.suoritukset.length
      )
    },
    [form, setPäätasonSuoritus]
  )

  const editorContainerProps = voiLisätä
    ? {
        suorituksenLisäys: localize(t('lisää vuosiluokan suoritus')),
        onCreateSuoritus
      }
    : {}

  const modal = modalVisible ? (
    <UusiPerusopetuksenVuosiluokanSuoritusModal
      opiskeluoikeus={form.state}
      onSubmit={onSubmit}
      onClose={() => setModalVisible(false)}
    />
  ) : null

  return { editorContainerProps, modal }
}

const perusopetuksenSuorituksenNimi = (
  s: PerusopetuksenPäätasonSuoritus
): LocalizedString => {
  if (isNuortenPerusopetuksenOppimääränSuoritus(s)) {
    return localize(t('Päättötodistus'))
  }
  return (
    s.koulutusmoduuli.tunniste.nimi ||
    localize(s.koulutusmoduuli.tunniste.koodiarvo)
  )
}

const sortPerusopetuksenSuoritukset = (
  suoritukset: PerusopetuksenPäätasonSuoritus[]
): PerusopetuksenPäätasonSuoritus[] =>
  sortPäätasonSuoritukset(
    suoritukset,
    isNuortenPerusopetuksenOppimääränSuoritus
  )

/**
 * Tallentaa käyttäjän luomat paikalliset oppiaineet organisaation preferenceihin,
 * kun opiskeluoikeus on tallennettu onnistuneesti. Näin ne löytyvät uudelleen
 * pudotusvalikosta seuraavilla muokkauskerroilla.
 */
const useSyncPaikallisetOppiaineet = (
  state: PerusopetuksenOpiskeluoikeus,
  isSaved: boolean,
  organisaatioOid?: string
) => {
  const { store } = usePreferences<NuortenPerusopetuksenPaikallinenOppiaine>(
    organisaatioOid,
    'nuortenperusopetuksenpaikallinenoppiaine'
  )

  React.useEffect(() => {
    if (!isSaved || !organisaatioOid) return
    const paikalliset: NuortenPerusopetuksenPaikallinenOppiaine[] = []
    for (const suoritus of state.suoritukset) {
      const osasuoritukset = (suoritus as { osasuoritukset?: unknown[] })
        .osasuoritukset
      if (!osasuoritukset) continue
      for (const s of osasuoritukset) {
        if (
          isNuortenPerusopetuksenOppiaineenSuoritus(s) &&
          isNuortenPerusopetuksenPaikallinenOppiaine(s.koulutusmoduuli) &&
          s.koulutusmoduuli.tunniste.koodiarvo
        ) {
          paikalliset.push(s.koulutusmoduuli)
        }
      }
    }
    // Dedup by koodiarvo (same local koodi might appear as both pakollinen/valinnainen)
    const seen = new Set<string>()
    for (const p of paikalliset) {
      const koodi = p.tunniste.koodiarvo
      if (seen.has(koodi)) continue
      seen.add(koodi)
      store(koodi, p)
    }
  }, [isSaved, state, organisaatioOid, store])
}

export default PerusopetusEditor
