import React, { useContext, useEffect, useMemo } from 'react'
import { useSchema } from '../appstate/constraints'
import { useKoodistoFiller } from '../appstate/koodisto'
import { OpiskeluoikeusContext } from '../appstate/opiskeluoikeus'
import { TestIdRoot } from '../appstate/useTestId'
import {
  hasPäätasonsuoritusOf,
  usePäätasonSuoritus
} from '../components-v2/containers/EditorContainer'
import { useForm } from '../components-v2/forms/FormModel'
import { AdaptedOpiskeluoikeusEditorProps } from '../components-v2/interoperability/useUiAdapter'
import { OpiskeluoikeusTitle } from '../components-v2/opiskeluoikeus/OpiskeluoikeusTitle'
import { Arviointi } from '../types/fi/oph/koski/schema/Arviointi'
import { isOppivelvollisilleSuunnattuMaahanmuuttajienKotoutumiskoulutuksenSuoritus } from '../types/fi/oph/koski/schema/OppivelvollisilleSuunnattuMaahanmuuttajienKotoutumiskoulutuksenSuoritus'
import { isOppivelvollisilleSuunnattuMaahanmuuttajienKotoutumiskoulutuksenSuoritus2022 } from '../types/fi/oph/koski/schema/OppivelvollisilleSuunnattuMaahanmuuttajienKotoutumiskoulutuksenSuoritus2022'
import { isOppivelvollisilleSuunnattuVapaanSivistystyönKoulutuksenSuoritus } from '../types/fi/oph/koski/schema/OppivelvollisilleSuunnattuVapaanSivistystyonKoulutuksenSuoritus'
import { isVapaanSivistystyönJotpaKoulutuksenSuoritus } from '../types/fi/oph/koski/schema/VapaanSivistystyonJotpaKoulutuksenSuoritus'
import { isVapaanSivistystyönLukutaitokoulutuksenSuoritus } from '../types/fi/oph/koski/schema/VapaanSivistystyonLukutaitokoulutuksenSuoritus'
import { VapaanSivistystyönOpiskeluoikeus } from '../types/fi/oph/koski/schema/VapaanSivistystyonOpiskeluoikeus'
import { isVapaanSivistystyönVapaatavoitteisenKoulutuksenSuoritus } from '../types/fi/oph/koski/schema/VapaanSivistystyonVapaatavoitteisenKoulutuksenSuoritus'
import { parasArviointi } from '../util/arvioinnit'
import { VSTJotpaEditor } from './jotpa/VSTJotpaEditor'
import { KOPSEditor } from './kops/KOPSEditor'
import { VSTKoto2012Editor } from './koto2012/VSTKoto2012Editor'
import { VSTKoto2022Editor } from './koto2022/VSTKoto2022Editor'
import { VSTLukutaitoEditor } from './lukutaito/VSTLukutaitoEditor'
import { vstNimi } from './resolvers'
import { isVSTOsasuoritusArvioinnilla } from './typeguards'
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
  const editorProps = {
    form,
    oppijaOid: props.oppijaOid,
    organisaatio,
    suoritusVahvistettu,
    invalidatable: props.invalidatable,
    onChangeSuoritus: setPäätasonSuoritus
  }

  return (
    <TestIdRoot id={päätasonSuoritus.testId}>
      <OpiskeluoikeusTitle
        opiskeluoikeus={form.state}
        opiskeluoikeudenNimi={vstNimi(form.state)}
      />
      {hasPäätasonsuoritusOf(
        isVapaanSivistystyönJotpaKoulutuksenSuoritus,
        päätasonSuoritus
      ) && (
        <VSTJotpaEditor {...editorProps} päätasonSuoritus={päätasonSuoritus} />
      )}
      {hasPäätasonsuoritusOf(
        isVapaanSivistystyönVapaatavoitteisenKoulutuksenSuoritus,
        päätasonSuoritus
      ) && (
        <VSTVapaatavoitteinenEditor
          {...editorProps}
          päätasonSuoritus={päätasonSuoritus}
        />
      )}
      {hasPäätasonsuoritusOf(
        isVapaanSivistystyönLukutaitokoulutuksenSuoritus,
        päätasonSuoritus
      ) && (
        <VSTLukutaitoEditor
          {...editorProps}
          päätasonSuoritus={päätasonSuoritus}
        />
      )}
      {hasPäätasonsuoritusOf(
        isOppivelvollisilleSuunnattuVapaanSivistystyönKoulutuksenSuoritus,
        päätasonSuoritus
      ) && <KOPSEditor {...editorProps} päätasonSuoritus={päätasonSuoritus} />}
      {hasPäätasonsuoritusOf(
        isOppivelvollisilleSuunnattuMaahanmuuttajienKotoutumiskoulutuksenSuoritus,
        päätasonSuoritus
      ) && (
        <VSTKoto2012Editor
          {...editorProps}
          päätasonSuoritus={päätasonSuoritus}
        />
      )}
      {hasPäätasonsuoritusOf(
        isOppivelvollisilleSuunnattuMaahanmuuttajienKotoutumiskoulutuksenSuoritus2022,
        päätasonSuoritus
      ) && (
        <VSTKoto2022Editor
          {...editorProps}
          päätasonSuoritus={päätasonSuoritus}
        />
      )}
    </TestIdRoot>
  )
}
