import React from 'react'
import { ActivePäätasonSuoritus } from '../components-v2/containers/EditorContainer'
import {
  KeyValueRow,
  KeyValueTable
} from '../components-v2/containers/KeyValueTable'
import { FormModel } from '../components-v2/forms/FormModel'
import { t } from '../i18n/i18n'
import { IBOpiskeluoikeus } from '../types/fi/oph/koski/schema/IBOpiskeluoikeus'

export type IBTutkintTiedotProps = {
  form: FormModel<IBOpiskeluoikeus>
  päätasonSuoritus: ActivePäätasonSuoritus<IBOpiskeluoikeus>
}

export const IBPäätasonSuoritusTiedot: React.FC<IBTutkintTiedotProps> = ({
  form,
  päätasonSuoritus
}) => {
  const opiskeluoikeus = form.state

  return (
    <KeyValueTable>
      <KeyValueRow localizableLabel="Koulutus">
        {t(ibKoulutusNimi(opiskeluoikeus))}
      </KeyValueRow>
      <KeyValueRow localizableLabel="Oppilaitos / toimipiste">
        {t(päätasonSuoritus.suoritus.toimipiste.nimi)}
      </KeyValueRow>
      <KeyValueRow localizableLabel="Suorituskieli">
        {t(päätasonSuoritus.suoritus.suorituskieli.nimi)}
      </KeyValueRow>
    </KeyValueTable>
  )
}

export const ibKoulutusNimi = (opiskeluoikeus: IBOpiskeluoikeus): string =>
  `${t(opiskeluoikeus.suoritukset[0]?.koulutusmoduuli.tunniste.nimi)}`
