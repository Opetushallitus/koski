import React, { useContext, useEffect, useMemo } from 'react'
import { useSchema } from '../appstate/constraints'
import { OpiskeluoikeusContext } from '../appstate/opiskeluoikeus'
import { TestIdRoot } from '../appstate/useTestId'
import {
  ActivePäätasonSuoritus,
  hasPäätasonsuoritusOf,
  usePäätasonSuoritus
} from '../components-v2/containers/EditorContainer'
import { FormModel, useForm } from '../components-v2/forms/FormModel'
import { AdaptedOpiskeluoikeusEditorProps } from '../components-v2/interoperability/useUiAdapter'
import { OpiskeluoikeusTitle } from '../components-v2/opiskeluoikeus/OpiskeluoikeusTitle'
import { t } from '../i18n/i18n'
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
import { VSTVapaatavoitteinenEditor } from './vapaatavoitteinen/VSTVapaatavoitteinenEditor'
import { isVSTSuoritusArvioinnilla } from './common/arviointi'
import { isVapaanSivistystyönKoulutuksenPäätasonSuoritus } from '../types/fi/oph/koski/schema/VapaanSivistystyonKoulutuksenPaatasonSuoritus'
import { isVapaanSivistystyönOsaamismerkinSuoritus } from '../types/fi/oph/koski/schema/VapaanSivistystyonOsaamismerkinSuoritus'
import { VSTOsaamismerkkiEditor } from './osaamismerkki/VSTOsaamismerkkiEditor'
import { OsaamismerkkiKuva } from './osaamismerkki/OsaamismerkkiKuva'
import { useTree } from '../appstate/tree'
import { useKansalainenTaiSuoritusjako } from '../appstate/user'
import { Koulutustoimija } from '../types/fi/oph/koski/schema/Koulutustoimija'
import { Oppilaitos } from '../types/fi/oph/koski/schema/Oppilaitos'
import { VapaanSivistystyönPäätasonSuoritus } from '../types/fi/oph/koski/schema/VapaanSivistystyonPaatasonSuoritus'
import { OsaamismerkkiTitle } from './osaamismerkki/OsaamismerkkiTitle'

type VSTEditorProps =
  AdaptedOpiskeluoikeusEditorProps<VapaanSivistystyönOpiskeluoikeus>

export const VSTEditor: React.FC<VSTEditorProps> = (props) => {
  // Opiskeluoikeus
  const opiskeluoikeusSchema = useSchema(
    VapaanSivistystyönOpiskeluoikeus.className
  )
  const form = useForm(props.opiskeluoikeus, false, opiskeluoikeusSchema)
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

  const suorituksenVahvistaminenEiMahdollista = useMemo(() => {
    if (
      isVapaanSivistystyönKoulutuksenPäätasonSuoritus(päätasonSuoritus.suoritus)
    ) {
      if (päätasonSuoritus.suoritus.osasuoritukset === undefined) {
        return false
      }
      const kaikkiArvioinnit = päätasonSuoritus.suoritus.osasuoritukset.flatMap(
        (osasuoritus) => {
          if (isVSTSuoritusArvioinnilla(osasuoritus)) {
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
    } else if (
      isVapaanSivistystyönOsaamismerkinSuoritus(päätasonSuoritus.suoritus)
    ) {
      return !(
        parasArviointi<Arviointi>(päätasonSuoritus.suoritus.arviointi || []) !==
        undefined
      )
    } else {
      return false
    }
  }, [päätasonSuoritus.suoritus])

  // Render
  const editorProps = {
    form,
    oppijaOid: props.oppijaOid,
    organisaatio,
    suorituksenVahvistaminenEiMahdollista,
    invalidatable: props.invalidatable,
    onChangeSuoritus: setPäätasonSuoritus
  }

  const osaamismerkki = hasPäätasonsuoritusOf(
    isVapaanSivistystyönOsaamismerkinSuoritus,
    päätasonSuoritus
  )

  const { TreeNode, ...tree } = useTree()
  const kansalainenTaiSuoritusjako = useKansalainenTaiSuoritusjako()

  return (
    <>
      {kansalainenTaiSuoritusjako ? (
        <TreeNode>
          {osaamismerkki ? (
            <OsaamismerkkiTitle
              opiskeluoikeus={form.state}
              opiskeluoikeudenNimi={vstNimi(form.state)}
              kuva={<OsaamismerkkiKuva päätasonSuoritus={päätasonSuoritus} />}
              tree={tree}
            />
          ) : (
            <OpiskeluoikeusTitle
              opiskeluoikeus={form.state}
              opiskeluoikeudenNimi={vstNimi(form.state)}
              tree={tree}
            />
          )}
          {tree.isOpen && (
            <PäätasonSuoritusEditor
              editorProps={editorProps}
              päätasonSuoritus={päätasonSuoritus}
            />
          )}
        </TreeNode>
      ) : (
        <>
          {osaamismerkki ? (
            <OsaamismerkkiTitle
              opiskeluoikeus={form.state}
              opiskeluoikeudenNimi={vstNimi(form.state)}
              kuva={<OsaamismerkkiKuva päätasonSuoritus={päätasonSuoritus} />}
            />
          ) : (
            <OpiskeluoikeusTitle
              opiskeluoikeus={form.state}
              opiskeluoikeudenNimi={vstNimi(form.state)}
            />
          )}
          <PäätasonSuoritusEditor
            editorProps={editorProps}
            päätasonSuoritus={päätasonSuoritus}
          />
        </>
      )}
    </>
  )
}

const PäätasonSuoritusEditor: React.FC<{
  editorProps: {
    form: FormModel<VapaanSivistystyönOpiskeluoikeus>
    oppijaOid: string
    organisaatio: Koulutustoimija | Oppilaitos | undefined
    suorituksenVahvistaminenEiMahdollista: boolean
    invalidatable: boolean
    onChangeSuoritus: (suoritusIndex: number) => void
  }
  päätasonSuoritus: ActivePäätasonSuoritus<
    VapaanSivistystyönOpiskeluoikeus,
    VapaanSivistystyönPäätasonSuoritus
  >
}> = (props) => {
  const { editorProps, päätasonSuoritus } = props

  return (
    <TestIdRoot id={päätasonSuoritus.testId}>
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
      {hasPäätasonsuoritusOf(
        isVapaanSivistystyönOsaamismerkinSuoritus,
        päätasonSuoritus
      ) && (
        <VSTOsaamismerkkiEditor
          {...editorProps}
          päätasonSuoritus={päätasonSuoritus}
        />
      )}
    </TestIdRoot>
  )
}

export const vstNimi = (opiskeluoikeus: VapaanSivistystyönOpiskeluoikeus) =>
  `${t(opiskeluoikeus.suoritukset[0]?.koulutusmoduuli.tunniste.nimi)}`
