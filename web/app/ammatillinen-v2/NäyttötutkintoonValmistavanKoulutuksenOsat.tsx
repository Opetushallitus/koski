import React, { useCallback, useState } from 'react'
import { ActivePäätasonSuoritus } from '../components-v2/containers/EditorContainer'
import { Column, ColumnRow } from '../components-v2/containers/Columns'
import {
  LocalizedTextEdit,
  LocalizedTextView
} from '../components-v2/controls/LocalizedTestField'
import { FlatButton } from '../components-v2/controls/FlatButton'
import { FormField } from '../components-v2/forms/FormField'
import { FormModel, FormOptic } from '../components-v2/forms/FormModel'
import { KoodistoSelect } from '../components-v2/opiskeluoikeus/KoodistoSelect'
import {
  OsasuoritusProperty,
  OsasuoritusPropertyValue
} from '../components-v2/opiskeluoikeus/OsasuoritusProperty'
import {
  OsasuoritusRowData,
  OsasuoritusTable
} from '../components-v2/opiskeluoikeus/OsasuoritusTable'
import { finnish, t } from '../i18n/i18n'
import { AmmatillinenOpiskeluoikeus } from '../types/fi/oph/koski/schema/AmmatillinenOpiskeluoikeus'
import { Koodistokoodiviite } from '../types/fi/oph/koski/schema/Koodistokoodiviite'
import {
  isMuuValtakunnallinenTutkinnonOsa,
  MuuValtakunnallinenTutkinnonOsa
} from '../types/fi/oph/koski/schema/MuuValtakunnallinenTutkinnonOsa'
import { NäyttötutkintoonValmistavanKoulutuksenOsa } from '../types/fi/oph/koski/schema/NayttotutkintoonValmistavanKoulutuksenOsa'
import { NäyttötutkintoonValmistavanKoulutuksenOsanSuoritus } from '../types/fi/oph/koski/schema/NayttotutkintoonValmistavanKoulutuksenOsanSuoritus'
import { NäyttötutkintoonValmistavanKoulutuksenSuoritus } from '../types/fi/oph/koski/schema/NayttotutkintoonValmistavanKoulutuksenSuoritus'
import {
  isPaikallinenNäyttötutkintoonValmistavanKoulutuksenOsa,
  PaikallinenNäyttötutkintoonValmistavanKoulutuksenOsa
} from '../types/fi/oph/koski/schema/PaikallinenNayttotutkintoonValmistavanKoulutuksenOsa'
import { PaikallinenKoodi } from '../types/fi/oph/koski/schema/PaikallinenKoodi'
import { YhteinenTutkinnonOsa } from '../types/fi/oph/koski/schema/YhteinenTutkinnonOsa'
import { append, deleteAt } from '../util/fp/arrays'
import {
  NewPaikallinenModal,
  NewToisestaTutkinnostaModal,
  yhteisenTutkinnonOsat,
  YhteisenTutkinnonOsatTunniste
} from './OsasuoritusTables'
import { useTutkinnonOsat } from './useTutkinnonOsat'

type ValmistavanKoulutuksenOsatProps = {
  form: FormModel<AmmatillinenOpiskeluoikeus>
  oppilaitosOid?: string
  päätasonSuoritus: ActivePäätasonSuoritus<
    AmmatillinenOpiskeluoikeus,
    NäyttötutkintoonValmistavanKoulutuksenSuoritus
  >
}

const KOULUTUKSEN_OSA = 'Koulutuksen osa'

// Näyttötutkintoon valmistavan koulutuksen osilla ei ole arviointia eikä
// laajuutta, joten taulukossa on vain osan nimi ja paikallisille osille kuvaus.
export const NäyttötutkintoonValmistavanKoulutuksenOsat = ({
  form,
  oppilaitosOid,
  päätasonSuoritus
}: ValmistavanKoulutuksenOsatProps) => {
  const osasuoritukset = päätasonSuoritus.suoritus.osasuoritukset || []
  const osasuorituksetPath = päätasonSuoritus.path
    .prop('osasuoritukset')
    .valueOr([])

  const rows: OsasuoritusRowData<typeof KOULUTUKSEN_OSA>[] = osasuoritukset.map(
    (s, index) => {
      // Kuvaus on paikallisella osalla pakollinen ja valtakunnallisella
      // muulla kuin yhteisellä tutkinnon osalla vapaaehtoinen
      const km = s.koulutusmoduuli
      const näytäKuvaus =
        (isPaikallinenNäyttötutkintoonValmistavanKoulutuksenOsa(km) ||
          isMuuValtakunnallinenTutkinnonOsa(km)) &&
        (form.editMode || km.kuvaus !== undefined)
      return {
        suoritusIndex: päätasonSuoritus.index,
        osasuoritusIndex: index,
        expandable: näytäKuvaus,
        columns: {
          [KOULUTUKSEN_OSA]: t(s.koulutusmoduuli.tunniste.nimi)
        },
        content: näytäKuvaus ? (
          <OsasuoritusProperty label="Kuvaus">
            <OsasuoritusPropertyValue>
              <FormField
                form={form}
                path={(
                  osasuorituksetPath
                    .at(index)
                    .prop('koulutusmoduuli') as FormOptic<
                    AmmatillinenOpiskeluoikeus,
                    | PaikallinenNäyttötutkintoonValmistavanKoulutuksenOsa
                    | MuuValtakunnallinenTutkinnonOsa
                  >
                ).prop('kuvaus')}
                view={LocalizedTextView}
                edit={LocalizedTextEdit}
                editProps={{ large: true }}
                testId="kuvaus"
              />
            </OsasuoritusPropertyValue>
          </OsasuoritusProperty>
        ) : undefined
      }
    }
  )

  return (
    <OsasuoritusTable
      editMode={form.editMode}
      rows={
        rows.length === 0 && form.editMode
          ? [
              // Saadaan otsikkorivi näkyviin muokkaustilassa, kun osia ei ole
              {
                suoritusIndex: päätasonSuoritus.index,
                osasuoritusIndex: 0,
                expandable: false,
                columns: { [KOULUTUKSEN_OSA]: null }
              }
            ]
          : rows
      }
      onRemove={
        rows.length === 0
          ? undefined
          : (rowIndex) =>
              form.updateAt(osasuorituksetPath, (os) => deleteAt(os, rowIndex))
      }
      addNewOsasuoritusView={NewValmistavanKoulutuksenOsa}
      addNewOsasuoritusViewProps={{
        form,
        oppilaitosOid,
        suoritus: päätasonSuoritus.suoritus,
        osasuorituksetPath
      }}
    />
  )
}

type NewValmistavanKoulutuksenOsaProps = {
  form: FormModel<AmmatillinenOpiskeluoikeus>
  oppilaitosOid?: string
  suoritus: NäyttötutkintoonValmistavanKoulutuksenSuoritus
  osasuorituksetPath: FormOptic<
    AmmatillinenOpiskeluoikeus,
    NäyttötutkintoonValmistavanKoulutuksenOsanSuoritus[]
  >
}

const NewValmistavanKoulutuksenOsa = ({
  form,
  oppilaitosOid,
  suoritus,
  osasuorituksetPath
}: NewValmistavanKoulutuksenOsaProps) => {
  const [paikallinenModal, setPaikallinenModal] = useState(false)
  const [toisestaTutkinnostaModal, setToisestaTutkinnostaModal] =
    useState(false)
  const lisättävätTutkinnonOsat = useTutkinnonOsat(
    suoritus.tutkinto.perusteenDiaarinumero
  )

  const lisää = (koulutusmoduuli: NäyttötutkintoonValmistavanKoulutuksenOsa) =>
    form.updateAt(
      osasuorituksetPath,
      append(
        NäyttötutkintoonValmistavanKoulutuksenOsanSuoritus({ koulutusmoduuli })
      )
    )

  const format = useCallback(
    (osa: Koodistokoodiviite) => osa.koodiarvo + ' ' + t(osa.nimi),
    []
  )
  const filter = useCallback(
    (osa: Koodistokoodiviite) => {
      const osat = lisättävätTutkinnonOsat.osat.map((o) => o.koodiarvo)
      return osat.length === 0 || osat.includes(osa.koodiarvo)
    },
    [lisättävätTutkinnonOsat]
  )

  return (
    <ColumnRow>
      <Column span={12}>
        <KoodistoSelect
          addNewText={'Lisää tutkinnon osa'}
          koodistoUri="tutkinnonosat"
          format={format}
          filter={filter}
          onSelect={(tunniste) =>
            tunniste && lisää(valtakunnallinenTutkinnonOsa(tunniste))
          }
          testId="uusi-muu-tutkinnonosa"
        />
      </Column>
      <Column span={6}>
        <FlatButton
          withAddIcon
          onClick={() => setPaikallinenModal(true)}
          testId="lisaa-paikallinen-osa"
        >
          {t('Lisää paikallinen tutkinnon osa')}
        </FlatButton>
        {paikallinenModal && (
          <NewPaikallinenModal
            onClose={() => setPaikallinenModal(false)}
            onSubmit={(nimi) => {
              lisää(
                PaikallinenNäyttötutkintoonValmistavanKoulutuksenOsa({
                  tunniste: PaikallinenKoodi({
                    koodiarvo: nimi,
                    nimi: finnish(nimi)
                  }),
                  kuvaus: finnish(nimi)
                })
              )
              setPaikallinenModal(false)
            }}
          />
        )}
      </Column>
      <Column span={6}>
        <FlatButton
          withAddIcon
          onClick={() => setToisestaTutkinnostaModal(true)}
          testId="lisaa-osa-toisesta-tutkinnosta"
        >
          {t('Lisää tutkinnon osa toisesta tutkinnosta')}
        </FlatButton>
        {toisestaTutkinnostaModal && (
          <NewToisestaTutkinnostaModal
            oppilaitosOid={oppilaitosOid}
            onClose={() => setToisestaTutkinnostaModal(false)}
            onSubmit={(_tutkinto, tunniste) => {
              // Valmistavan koulutuksen osalle ei tallenneta tutkintoa
              lisää(valtakunnallinenTutkinnonOsa(tunniste))
              setToisestaTutkinnostaModal(false)
            }}
          />
        )}
      </Column>
    </ColumnRow>
  )
}

const valtakunnallinenTutkinnonOsa = (
  tunniste: Koodistokoodiviite<'tutkinnonosat', string>
): NäyttötutkintoonValmistavanKoulutuksenOsa =>
  yhteisenTutkinnonOsat.includes(tunniste.koodiarvo)
    ? YhteinenTutkinnonOsa({
        tunniste: tunniste as YhteisenTutkinnonOsatTunniste,
        pakollinen: false
      })
    : MuuValtakunnallinenTutkinnonOsa({ tunniste, pakollinen: false })
