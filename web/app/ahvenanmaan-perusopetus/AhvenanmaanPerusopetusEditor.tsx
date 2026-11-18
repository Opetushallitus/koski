import React, { useCallback, useMemo, useState } from 'react'
import { OpiskeluoikeusContext } from '../appstate/opiskeluoikeus'
import { useSchema } from '../appstate/constraints'
import { AdaptedOpiskeluoikeusEditorProps } from '../components-v2/interoperability/useUiAdapter'
import {
  EditorContainer,
  usePäätasonSuoritus
} from '../components-v2/containers/EditorContainer'
import { VirkailijaKansalainenContainer } from '../components-v2/containers/VirkailijaKansalainenContainer'
import { FormModel, useForm } from '../components-v2/forms/FormModel'
import { RemovePaatasonSuoritus } from '../components-v2/opiskeluoikeus/RemovePaatasonSuoritus'
import { UusiOpiskeluoikeusjakso } from '../components-v2/opiskeluoikeus/UusiOpiskeluoikeudenTilaModal'
import { arviointiVaaditaanVahvistetultaSuoritukselta } from '../components-v2/opiskeluoikeus/validointi'
import { Spacer } from '../components-v2/layout/Spacer'
import { localize, t } from '../i18n/i18n'
import { AhvenanmaanPerusopetuksenOpiskeluoikeus } from '../types/fi/oph/koski/schema/AhvenanmaanPerusopetuksenOpiskeluoikeus'
import { AhvenanmaanPerusopetuksenOpiskeluoikeusjakso } from '../types/fi/oph/koski/schema/AhvenanmaanPerusopetuksenOpiskeluoikeusjakso'
import { AhvenanmaanPerusopetuksenPäätasonSuoritus } from '../types/fi/oph/koski/schema/AhvenanmaanPerusopetuksenPaatasonSuoritus'
import {
  AhvenanmaanPerusopetuksenVuosiluokanSuoritus,
  isAhvenanmaanPerusopetuksenVuosiluokanSuoritus
} from '../types/fi/oph/koski/schema/AhvenanmaanPerusopetuksenVuosiluokanSuoritus'
import { LocalizedString } from '../types/fi/oph/koski/schema/LocalizedString'
import { isTerminaalitila } from '../util/opiskeluoikeus'
import { viimeisinOpiskelujaksonTila } from '../util/schema'
import { puuttuvatLuokkaAsteet } from '../perusopetus-v2/luokkaAsteenOppiaineet'
import { AhvenanmaanPerusopetuksenLisatiedot } from './AhvenanmaanPerusopetuksenLisatiedot'
import { AhvenanmaanPerusopetuksenOppiaineet } from './AhvenanmaanPerusopetuksenOppiaineet'
import { AhvenanmaanPerusopetuksenSuorituksenTiedot } from './AhvenanmaanPerusopetuksenSuorituksenTiedot'
import {
  ahvenanmaanKoulutuksenNimi,
  isAhvenanmaanOppimääränSuoritus
} from './oppimaaranSuoritus'
import { perusopetuksenPäätasonSuoritusEq } from '../perusopetus-v2/paatasonSuoritusPoisto'
import { useRemovePäätasonSuoritus } from '../components-v2/forms/useRemovePaatasonSuoritus'
import {
  oletuksenaAvattavanSuorituksenIndeksi,
  sortPäätasonSuoritukset
} from '../perusopetus-v2/paatasonSuoritustenJarjestys'
import { UusiAhvenanmaanPerusopetuksenVuosiluokanSuoritusModal } from './UusiAhvenanmaanPerusopetuksenVuosiluokanSuoritusModal'

export type AhvenanmaanPerusopetusEditorProps =
  AdaptedOpiskeluoikeusEditorProps<AhvenanmaanPerusopetuksenOpiskeluoikeus>

const AhvenanmaanPerusopetusEditor: React.FC<
  AhvenanmaanPerusopetusEditorProps
> = (props) => {
  const opiskeluoikeusSchema = useSchema(
    AhvenanmaanPerusopetuksenOpiskeluoikeus.className
  )
  const opiskeluoikeus = React.useMemo(
    () => ({
      ...props.opiskeluoikeus,
      suoritukset: sortSuoritukset(props.opiskeluoikeus.suoritukset)
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

  return (
    <VirkailijaKansalainenContainer
      opiskeluoikeus={form.state}
      opiskeluoikeudenNimi={ahvenanmaanKoulutuksenNimi(
        form.state.suoritukset[0]
      )}
    >
      <AhvenanmaanPerusopetuksenPäätasonSuoritusEditor {...props} form={form} />
    </VirkailijaKansalainenContainer>
  )
}

const AhvenanmaanPerusopetuksenPäätasonSuoritusEditor: React.FC<
  AhvenanmaanPerusopetusEditorProps & {
    form: FormModel<AhvenanmaanPerusopetuksenOpiskeluoikeus>
  }
> = (props) => {
  const initialSuoritusIndex = useMemo(
    () =>
      oletuksenaAvattavanSuorituksenIndeksi(
        props.form.state.suoritukset,
        isAhvenanmaanOppimääränSuoritus,
        isAhvenanmaanPerusopetuksenVuosiluokanSuoritus
      ),
    [props.form.state.suoritukset]
  )

  const [päätasonSuoritus, setPäätasonSuoritus] = usePäätasonSuoritus(
    props.form,
    initialSuoritusIndex
  )

  const addSuoritusProps = useVuosiluokanSuorituksenLisäys(
    props.form,
    setPäätasonSuoritus
  )

  const createOpiskeluoikeusjakso = (
    seed: UusiOpiskeluoikeusjakso<AhvenanmaanPerusopetuksenOpiskeluoikeusjakso>
  ) => AhvenanmaanPerusopetuksenOpiskeluoikeusjakso(seed)

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
        lisätiedotContainer={AhvenanmaanPerusopetuksenLisatiedot}
        suorituksenNimi={suorituksenNimi}
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
        <AhvenanmaanPerusopetuksenSuorituksenTiedot
          form={props.form}
          päätasonSuoritus={päätasonSuoritus}
        />
        <Spacer />
        <AhvenanmaanPerusopetuksenOppiaineet
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
  form: FormModel<AhvenanmaanPerusopetuksenOpiskeluoikeus>,
  setPäätasonSuoritus: (suoritusIndex: number) => void
) => {
  const [modalVisible, setModalVisible] = useState(false)

  const missingLuokkaAsteet = useMemo(
    () =>
      puuttuvatLuokkaAsteet(
        form.state.suoritukset.flatMap((s) =>
          isAhvenanmaanPerusopetuksenVuosiluokanSuoritus(s) && !s.jääLuokalle
            ? [s.koulutusmoduuli.tunniste.koodiarvo]
            : []
        )
      ),
    [form.state.suoritukset]
  )

  const voiLisätä = useMemo(() => {
    const vTila = viimeisinOpiskelujaksonTila(form.state.tila)
    if (!vTila || isTerminaalitila(vTila)) return false
    return missingLuokkaAsteet.length > 0
  }, [form.state.tila, missingLuokkaAsteet.length])

  const onCreateSuoritus = useCallback(() => setModalVisible(true), [])

  const onSubmit = useCallback(
    (uusiSuoritus: AhvenanmaanPerusopetuksenVuosiluokanSuoritus) => {
      const suoritukset = sortSuoritukset([
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
    <UusiAhvenanmaanPerusopetuksenVuosiluokanSuoritusModal
      opiskeluoikeus={form.state}
      onSubmit={onSubmit}
      onClose={() => setModalVisible(false)}
    />
  ) : null

  return { editorContainerProps, modal }
}

const suorituksenNimi = (
  s: AhvenanmaanPerusopetuksenPäätasonSuoritus
): LocalizedString => {
  if (isAhvenanmaanOppimääränSuoritus(s)) {
    return localize(t('Päättötodistus'))
  }
  return (
    s.koulutusmoduuli.tunniste.nimi ||
    localize(s.koulutusmoduuli.tunniste.koodiarvo)
  )
}

const sortSuoritukset = (
  suoritukset: AhvenanmaanPerusopetuksenPäätasonSuoritus[]
): AhvenanmaanPerusopetuksenPäätasonSuoritus[] =>
  sortPäätasonSuoritukset(suoritukset, isAhvenanmaanOppimääränSuoritus)

export default AhvenanmaanPerusopetusEditor
