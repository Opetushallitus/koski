import React from 'react'
import { AdaptedOpiskeluoikeusEditorProps } from '../components-v2/interoperability/useUiAdapter'
import { LukionOpiskeluoikeus } from '../types/fi/oph/koski/schema/LukionOpiskeluoikeus'
import { useSchema } from '../appstate/constraints'
import { FormModel, useForm } from '../components-v2/forms/FormModel'
import { VirkailijaKansalainenContainer } from '../components-v2/containers/VirkailijaKansalainenContainer'
import {
  EditorContainer,
  usePäätasonSuoritus
} from '../components-v2/containers/EditorContainer'
import { LukionOpiskeluoikeusjakso } from '../types/fi/oph/koski/schema/LukionOpiskeluoikeusjakso'
import { UusiOpiskeluoikeusjakso } from '../components-v2/opiskeluoikeus/UusiOpiskeluoikeudenTilaModal'
import { Spacer } from '../components-v2/layout/Spacer'
import { localize, t } from '../i18n/i18n'
import { LocalizedString } from '../types/fi/oph/koski/schema/LocalizedString'
import { LukionSuorituksenTiedot } from './LukionSuorituksenTiedot'
import { LukionOppiaineet } from './LukionOppiaineet'
import { LukionPäätasonSuoritus } from '../types/fi/oph/koski/schema/LukionPaatasonSuoritus'

export type LukioEditorProps =
  AdaptedOpiskeluoikeusEditorProps<LukionOpiskeluoikeus>

// Vaihe 1: vain katselu "Lukion oppimäärä" ja "Lukion aineopinnot"
// -suoritustyypeille 2019 (nuorten/aikuisten). Ei muokkausta, ei dialogeja,
// ei pre-IB/LUVA/erityinen tutkinto/oma äidinkieli/omat tiedot -näkymää
// vielä - ks. TOR-2777:n suunnitelma.
const LukioEditor: React.FC<LukioEditorProps> = (props) => {
  const opiskeluoikeusSchema = useSchema(LukionOpiskeluoikeus.className)
  const form = useForm(props.opiskeluoikeus, false, opiskeluoikeusSchema, [])

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

  return (
    <EditorContainer
      form={props.form}
      oppijaOid={props.oppijaOid}
      invalidatable={props.invalidatable}
      suoritusIndex={päätasonSuoritus.index}
      onChangeSuoritus={setPäätasonSuoritus}
      testId={päätasonSuoritus.testId}
      createOpiskeluoikeusjakso={createOpiskeluoikeusjakso}
      suorituksenNimi={lukionSuorituksenNimi}
    >
      <LukionSuorituksenTiedot
        form={props.form}
        päätasonSuoritus={päätasonSuoritus}
      />
      <Spacer />
      <LukionOppiaineet form={props.form} päätasonSuoritus={päätasonSuoritus} />
      <Spacer />
    </EditorContainer>
  )
}

const lukionSuorituksenNimi = (s: LukionPäätasonSuoritus): LocalizedString =>
  s.tyyppi.nimi || localize(s.tyyppi.koodiarvo)

export default LukioEditor
