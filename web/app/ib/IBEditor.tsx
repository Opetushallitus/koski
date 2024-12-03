import React from 'react'
import { useSchema } from '../appstate/constraints'
import { FormModel, useForm } from '../components-v2/forms/FormModel'
import { AdaptedOpiskeluoikeusEditorProps } from '../components-v2/interoperability/useUiAdapter'
import { OpiskeluoikeusTitle } from '../components-v2/opiskeluoikeus/OpiskeluoikeusTitle'
import { IBOpiskeluoikeus } from '../types/fi/oph/koski/schema/IBOpiskeluoikeus'
import { t } from '../i18n/i18n'
import {
  EditorContainer,
  usePäätasonSuoritus
} from '../components-v2/containers/EditorContainer'
import { LukionOpiskeluoikeusjakso } from '../types/fi/oph/koski/schema/LukionOpiskeluoikeusjakso'
import {
  ibKoulutusNimi,
  IBPäätasonSuoritusTiedot
} from './IBPaatasonSuoritusTiedot'
import { Spacer } from '../components-v2/layout/Spacer'
import { SuorituksenVahvistusField } from '../components-v2/opiskeluoikeus/SuorituksenVahvistus'
import { OppiaineTable } from '../components-v2/opiskeluoikeus/OppiaineTable'
import { sum } from '../util/numbers'
import { PäätasonSuoritusOf } from '../util/opiskeluoikeus'
import { isLaajuusKursseissa } from '../types/fi/oph/koski/schema/LaajuusKursseissa'
import { OsasuoritusOf } from '../util/schema'
import { isEmpty } from 'fp-ts/lib/Array'

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
        <div className="IBPäätasonSuoritusEditor__yhteensä">
          {t('Suoritettujen kurssien määrä yhteensä')}
          {': '}
          {kurssejaYhteensä}
        </div>
      )}
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
