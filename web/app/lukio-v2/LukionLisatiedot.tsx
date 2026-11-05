import React from 'react'
import { ButtonGroup } from '../components-v2/containers/ButtonGroup'
import {
  KeyValueRow,
  KeyValueTable
} from '../components-v2/containers/KeyValueTable'
import { FlatButton } from '../components-v2/controls/FlatButton'
import { FormField } from '../components-v2/forms/FormField'
import { FormListField } from '../components-v2/forms/FormListField'
import { FormModel } from '../components-v2/forms/FormModel'
import {
  AikajaksoEdit,
  AikajaksoView
} from '../components-v2/opiskeluoikeus/AikajaksoField'
import {
  BooleanEdit,
  BooleanView
} from '../components-v2/opiskeluoikeus/BooleanField'
import { LocalizedTextView } from '../components-v2/controls/LocalizedTestField'
import {
  emptyMaksuttomuuus,
  MaksuttomuusEdit,
  MaksuttomuusView
} from '../components-v2/opiskeluoikeus/MaksuttomuusField'
import { uusiTyhjäAikajakso } from '../components-v2/opiskeluoikeus/uusiJakso'
import {
  emptyErityisenKoulutustehtävänJakso,
  ErityisenKoulutustehtävänJaksoEdit,
  ErityisenKoulutustehtävänJaksoView
} from '../components-v2/opiskeluoikeus/ErityisenKoulutustehtavanJaksoField'
import {
  emptyUlkomaanjakso,
  UlkomaanjaksoEdit,
  UlkomaanjaksoView
} from '../components-v2/opiskeluoikeus/UlkomaanjaksoField'
import { t } from '../i18n/i18n'
import { Aikajakso } from '../types/fi/oph/koski/schema/Aikajakso'
import { LukionOpiskeluoikeudenLisätiedot } from '../types/fi/oph/koski/schema/LukionOpiskeluoikeudenLisatiedot'
import { LukionOpiskeluoikeus } from '../types/fi/oph/koski/schema/LukionOpiskeluoikeus'
import { OikeuttaMaksuttomuuteenPidennetty } from '../types/fi/oph/koski/schema/OikeuttaMaksuttomuuteenPidennetty'
import { append } from '../util/fp/arrays'

// yksityisopiskelija, oikeusMaksuttomaanAsuntolapaikkaan ja
// alle18vuotiaanAikuistenLukiokoulutuksenAloittamisenSyy ovat
// deprekoituja/redundantteja - backend tiputtaa ne aina tallennuksessa
// riippumatta siitä, mitä lomakkeelta lähetetään (varmistettu live-testillä
// sekä OppijaValidationLukioSpec.scala:n testillä "Lisätiedon kenttiä
// yksityisopiskelija ja alle18vuotiaanAikuistenLukiokoulutuksenAloittamisenSyy
// ei oteta vastaan siirrossa" ja validation/RedundantinDatanPoisto.scala:lla).
// Siksi näille ei rakenneta muokkauskomponenttia - FormField ilman edit-
// propertya näyttää arvon pelkästään katselutilassa, piilossa kun arvoa ei
// ole (esim. kaikilla nykyisillä testioppijoilla). Mahdollinen vanha data
// jää siis näkyviin, mutta sitä ei voi enää muuttaa kummassakaan
// käyttöliittymässä - vanha käyttöliittymäkin näyttäisi nämä muokattavina,
// mutta tallennus katoaisi yhtä lailla huomaamatta; tämä on tarkoituksella
// rehellisempi kuin se.
// Loput kentät - mukaan lukien listamuotoiset jaksot - toteutetaan tässä
// samalla tavalla kuin ib/IBLisatiedot.tsx, joka käyttää samaa
// LukionOpiskeluoikeudenLisätiedot-tyyppiä.
export const LukionLisätiedot: React.FC<{
  form: FormModel<LukionOpiskeluoikeus>
}> = ({ form }) => {
  const path = form.root.prop('lisätiedot').valueOr(emptyLisätiedot)

  const addErityisenKoulutustehtävänJakso = () => {
    form.updateAt(
      path.prop('erityisenKoulutustehtävänJaksot').valueOr([]),
      append(emptyErityisenKoulutustehtävänJakso)
    )
  }

  const addUlkomaanjakso = () => {
    form.updateAt(
      path.prop('ulkomaanjaksot').valueOr([]),
      append(emptyUlkomaanjakso)
    )
  }

  const addSisäoppilaitosmainenMajoitus = () => {
    form.updateAt(
      path.prop('sisäoppilaitosmainenMajoitus').valueOr([]),
      append(uusiTyhjäAikajakso())
    )
  }

  const addMaksuttomuusjakso = () => {
    form.updateAt(
      path.prop('maksuttomuus').valueOr([]),
      append(emptyMaksuttomuuus)
    )
  }

  const addOikeuttaMaksuttomuuteenPidennetty = () => {
    form.updateAt(
      path.prop('oikeuttaMaksuttomuuteenPidennetty').valueOr([]),
      append(
        OikeuttaMaksuttomuuteenPidennetty({
          alku: '',
          loppu: ''
        })
      )
    )
  }

  return (
    <KeyValueTable>
      <KeyValueRow localizableLabel="Pidennetty päättymispäivä" largeLabel>
        <FormField
          form={form}
          path={path.prop('pidennettyPäättymispäivä')}
          view={BooleanView}
          edit={BooleanEdit}
          testId="pidennettyPäättymispäivä"
        />
      </KeyValueRow>

      <KeyValueRow localizableLabel="Ulkomainen vaihto-opiskelija" largeLabel>
        <FormField
          form={form}
          path={path.prop('ulkomainenVaihtoopiskelija')}
          view={BooleanView}
          edit={BooleanEdit}
          testId="ulkomainenVaihtoopiskelija"
        />
      </KeyValueRow>

      <KeyValueRow
        localizableLabel="Erityisen koulutustehtävän jaksot"
        largeLabel
      >
        <FormListField
          form={form}
          path={path.prop('erityisenKoulutustehtävänJaksot')}
          view={ErityisenKoulutustehtävänJaksoView}
          edit={ErityisenKoulutustehtävänJaksoEdit}
          testId="erityisenKoulutustehtävänJaksot"
          removable
        />
        {form.editMode && (
          <ButtonGroup>
            <FlatButton onClick={addErityisenKoulutustehtävänJakso}>
              {t('Lisää')}
            </FlatButton>
          </ButtonGroup>
        )}
      </KeyValueRow>

      <KeyValueRow localizableLabel="Ulkomaanjaksot" largeLabel>
        <FormListField
          form={form}
          path={path.prop('ulkomaanjaksot')}
          view={UlkomaanjaksoView}
          edit={UlkomaanjaksoEdit}
          testId="ulkomaanjaksot"
          removable
        />
        {form.editMode && (
          <ButtonGroup>
            <FlatButton onClick={addUlkomaanjakso}>{t('Lisää')}</FlatButton>
          </ButtonGroup>
        )}
      </KeyValueRow>

      <KeyValueRow localizableLabel="Sisäoppilaitosmainen majoitus" largeLabel>
        <FormListField
          form={form}
          path={path.prop('sisäoppilaitosmainenMajoitus')}
          view={AikajaksoView}
          edit={AikajaksoEdit}
          editProps={{
            createAikajakso: Aikajakso
          }}
          testId="sisäoppilaitosmainenMajoitus"
          removable
        />
        {form.editMode && (
          <ButtonGroup>
            <FlatButton onClick={addSisäoppilaitosmainenMajoitus}>
              {t('Lisää')}
            </FlatButton>
          </ButtonGroup>
        )}
      </KeyValueRow>

      <KeyValueRow localizableLabel="Maksuttomuus" largeLabel>
        <FormListField
          form={form}
          path={path.prop('maksuttomuus')}
          view={MaksuttomuusView}
          edit={MaksuttomuusEdit}
          testId="maksuttomuus"
          removable
        />
        {form.editMode && (
          <ButtonGroup>
            <FlatButton onClick={addMaksuttomuusjakso}>{t('Lisää')}</FlatButton>
          </ButtonGroup>
        )}
      </KeyValueRow>

      <KeyValueRow
        localizableLabel="Oikeutta maksuttomuuteen pidennetty"
        largeLabel
      >
        <FormListField
          form={form}
          path={path.prop('oikeuttaMaksuttomuuteenPidennetty')}
          view={AikajaksoView}
          edit={AikajaksoEdit}
          editProps={{
            createAikajakso: ({ alku, loppu }) =>
              loppu
                ? OikeuttaMaksuttomuuteenPidennetty({ alku, loppu })
                : undefined
          }}
          testId="oikeuttaMaksuttomuuteenPidennetty"
          removable
        />
        {form.editMode && (
          <ButtonGroup>
            <FlatButton onClick={addOikeuttaMaksuttomuuteenPidennetty}>
              {t('Lisää')}
            </FlatButton>
          </ButtonGroup>
        )}
      </KeyValueRow>

      <KeyValueRow localizableLabel="Yksityisopiskelija" largeLabel>
        <FormField
          form={form}
          path={path.prop('yksityisopiskelija')}
          optional
          view={BooleanView}
          viewProps={{ hideFalse: true }}
          testId="yksityisopiskelija"
        />
      </KeyValueRow>

      <KeyValueRow
        localizableLabel="Oikeus maksuttomaan asuntolapaikkaan"
        largeLabel
      >
        <FormField
          form={form}
          path={path.prop('oikeusMaksuttomaanAsuntolapaikkaan')}
          optional
          view={BooleanView}
          viewProps={{ hideFalse: true }}
          testId="oikeusMaksuttomaanAsuntolapaikkaan"
        />
      </KeyValueRow>

      {form.state.lisätiedot
        ?.alle18vuotiaanAikuistenLukiokoulutuksenAloittamisenSyy && (
        <KeyValueRow
          localizableLabel="Syy alle 18-vuotiaana aloitettuun opiskeluun aikuisten lukiokoulutuksessa"
          largeLabel
        >
          <FormField
            form={form}
            path={path.prop(
              'alle18vuotiaanAikuistenLukiokoulutuksenAloittamisenSyy'
            )}
            optional
            view={LocalizedTextView}
            testId="alle18vuotiaanAikuistenLukiokoulutuksenAloittamisenSyy"
          />
        </KeyValueRow>
      )}
    </KeyValueTable>
  )
}

const emptyLisätiedot = LukionOpiskeluoikeudenLisätiedot()
