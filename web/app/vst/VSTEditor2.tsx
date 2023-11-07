import React, { useCallback, useContext, useEffect, useMemo } from 'react'
import { useSchema } from '../appstate/constraints'
import { useKoodistoFiller } from '../appstate/koodisto'
import { OpiskeluoikeusContext } from '../appstate/opiskeluoikeus'
import {
  hasPäätasonsuoritusOf,
  usePäätasonSuoritus
} from '../components-v2/containers/EditorContainer'
import { FormOptic, useForm } from '../components-v2/forms/FormModel'
import { AdaptedOpiskeluoikeusEditorProps } from '../components-v2/interoperability/useUiAdapter'
import { OpiskeluoikeusTitle } from '../components-v2/opiskeluoikeus/OpiskeluoikeusTitle'
import { Arviointi } from '../types/fi/oph/koski/schema/Arviointi'
import { isVapaanSivistystyönJotpaKoulutuksenSuoritus } from '../types/fi/oph/koski/schema/VapaanSivistystyonJotpaKoulutuksenSuoritus'
import { VapaanSivistystyönOpiskeluoikeus } from '../types/fi/oph/koski/schema/VapaanSivistystyonOpiskeluoikeus'
import { VapaanSivistystyönPäätasonSuoritus } from '../types/fi/oph/koski/schema/VapaanSivistystyonPaatasonSuoritus'
import { isVapaanSivistystyönVapaatavoitteisenKoulutuksenSuoritus } from '../types/fi/oph/koski/schema/VapaanSivistystyonVapaatavoitteisenKoulutuksenSuoritus'
import { parasArviointi } from '../util/arvioinnit'
import { append } from '../util/fp/arrays'
import { VSTSuoritus } from './common/types'
import { VSTJotpaEditor } from './jotpa/VSTJotpaEditor'
import { vstNimi } from './resolvers'
import { VSTOsasuoritus, isVSTOsasuoritusArvioinnilla } from './typeguards'
import { VSTVapaatavoitteinenEditor } from './vapaatavoitteinen/VSTVapaatavoitteinenEditor'

type VSTEditorProps =
  AdaptedOpiskeluoikeusEditorProps<VapaanSivistystyönOpiskeluoikeus>

export const VSTEditor: React.FC<VSTEditorProps> = (props) => {
  // Opiskeluoikeus
  const opiskeluoikeusSchema = useSchema(
    VapaanSivistystyönOpiskeluoikeus.className
  )
  const form = useForm(props.opiskeluoikeus, false, opiskeluoikeusSchema)
  const fillKoodistot = useKoodistoFiller()
  const { setOrganisaatio } = useContext(OpiskeluoikeusContext)

  // Oppilaitos
  const organisaatio =
    props.opiskeluoikeus.oppilaitos || props.opiskeluoikeus.koulutustoimija
  useEffect(
    () => setOrganisaatio(organisaatio),
    [organisaatio, setOrganisaatio]
  )

  // Päätason suoritus
  const [päätasonSuoritus, setPäätasonSuoritus] = usePäätasonSuoritus(form)

  // TODO TOR-2086: Posta appendOsasuoritus ja createOsasuoritus refaktoroinnin jälkeen
  const appendOsasuoritus = useCallback(
    (
      path: FormOptic<VapaanSivistystyönPäätasonSuoritus, any>,
      uusiOsasuoritus: VSTOsasuoritus
    ) => {
      if (form.editMode) {
        form.updateAt(path, (osasuoritus) => ({
          ...osasuoritus,
          osasuoritukset: append(uusiOsasuoritus)(osasuoritus.osasuoritukset)
        }))
      }
    },
    [form]
  )

  const createOsasuoritus = useCallback(
    (
      suoritusPath: FormOptic<VapaanSivistystyönPäätasonSuoritus, any>,
      osasuoritus: VSTSuoritus
    ) => {
      console.log('createOsasuoritus', { suoritusPath, osasuoritus })
      fillKoodistot(osasuoritus)
        .then((filledOsasuoritus) => {
          appendOsasuoritus(suoritusPath, filledOsasuoritus as any)
        })
        .catch(console.error)
    },
    [appendOsasuoritus, fillKoodistot]
  )

  const suoritusVahvistettu = useMemo(() => {
    if (päätasonSuoritus.suoritus.osasuoritukset === undefined) {
      return false
    }
    const kaikkiArvioinnit = päätasonSuoritus.suoritus.osasuoritukset.flatMap(
      (osasuoritus) => {
        if (isVSTOsasuoritusArvioinnilla(osasuoritus)) {
          if ('arviointi' in osasuoritus) {
            return parasArviointi<Arviointi>(osasuoritus.arviointi || [])
          } else {
            return undefined
          }
        } else {
          return []
        }
      }
    )
    return !kaikkiArvioinnit.every((a) => a !== undefined)
  }, [päätasonSuoritus.suoritus.osasuoritukset])

  // Render
  return (
    <>
      <OpiskeluoikeusTitle
        opiskeluoikeus={form.state}
        opiskeluoikeudenNimi={vstNimi(form.state)}
      />
      {hasPäätasonsuoritusOf(
        isVapaanSivistystyönJotpaKoulutuksenSuoritus,
        päätasonSuoritus
      ) && (
        <VSTJotpaEditor
          form={form}
          oppijaOid={props.oppijaOid}
          päätasonSuoritus={päätasonSuoritus}
          organisaatio={organisaatio}
          suoritusVahvistettu={suoritusVahvistettu}
          invalidatable={props.invalidatable}
          onChangeSuoritus={setPäätasonSuoritus}
          onCreateOsasuoritus={createOsasuoritus}
        />
      )}
      {hasPäätasonsuoritusOf(
        isVapaanSivistystyönVapaatavoitteisenKoulutuksenSuoritus,
        päätasonSuoritus
      ) && (
        <VSTVapaatavoitteinenEditor
          form={form}
          oppijaOid={props.oppijaOid}
          päätasonSuoritus={päätasonSuoritus}
          organisaatio={organisaatio}
          suoritusVahvistettu={suoritusVahvistettu}
          invalidatable={props.invalidatable}
          onChangeSuoritus={setPäätasonSuoritus}
          onCreateOsasuoritus={createOsasuoritus}
        />
      )}
    </>
  )
}
