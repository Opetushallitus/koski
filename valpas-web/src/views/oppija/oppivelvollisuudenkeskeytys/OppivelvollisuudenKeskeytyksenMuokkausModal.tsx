import React, { useCallback } from "react"
import {
  deleteOppivelvollisuudenKeskeytys,
  updateOppivelvollisuudenKeskeytys,
} from "../../../api/api"
import { useApiMethod, useOnApiSuccess } from "../../../api/apiHooks"
import { isError } from "../../../api/apiUtils"
import { Modal } from "../../../components/containers/Modal"
import { SecondaryHeading } from "../../../components/typography/headings"
import { t } from "../../../i18n/i18n"
import {
  kuntavalvontaAllowed,
  useOrganisaatiotOfRole,
} from "../../../state/accessRights"
import { HenkilöLaajatTiedot } from "../../../state/apitypes/henkilo"
import { OppivelvollisuudenKeskeytys } from "../../../state/apitypes/oppivelvollisuudenkeskeytys"
import { formatDateRange } from "../../../utils/date"
import {
  OppivelvollisuudenKeskeytysForm,
  OppivelvollisuudenKeskeytysFormValues,
} from "./OppivelvollisuudenKeskeytysForm"

export type OppivelvollisuudenKeskeytysMuokkausModalProps = {
  henkilö: HenkilöLaajatTiedot
  keskeytys: OppivelvollisuudenKeskeytys
  onClose: () => void
  onSubmit: () => void
  onDelete: () => void
}

export const OppivelvollisuudenKeskeytyksenMuokkausModal = (
  props: OppivelvollisuudenKeskeytysMuokkausModalProps
) => {
  const organisaatiot = useOrganisaatiotOfRole(kuntavalvontaAllowed)
  const create = useApiMethod(updateOppivelvollisuudenKeskeytys)
  const submit = useCallback(
    (form: OppivelvollisuudenKeskeytysFormValues) => {
      create.call({
        id: props.keskeytys.id,
        alku: form.alku,
        loppu: form.loppu,
      })
    },
    [create, props.keskeytys.id]
  )

  useOnApiSuccess(create, props.onSubmit)

  const remove = useApiMethod(deleteOppivelvollisuudenKeskeytys)
  useOnApiSuccess(remove, props.onDelete)

  const handleDelete = useCallback(() => {
    if (
      confirm(
        t("ovkeskeytys__poista_varmistus", {
          oppija: `${props.henkilö.sukunimi} ${props.henkilö.etunimet} (${props.henkilö.hetu})`,
          aikaväli: formatDateRange(
            props.keskeytys.alku,
            props.keskeytys.loppu
          ),
        })
      )
    ) {
      remove.call(props.keskeytys.id)
    }
  }, [
    remove,
    props.henkilö.etunimet,
    props.henkilö.hetu,
    props.henkilö.sukunimi,
    props.keskeytys.alku,
    props.keskeytys.id,
    props.keskeytys.loppu,
  ])

  return (
    <Modal title={t("ovkeskeytys__muokkaus_otsikko")} onClose={props.onClose}>
      <SecondaryHeading>
        {props.henkilö.sukunimi} {props.henkilö.etunimet}
        {props.henkilö.hetu && ` (${props.henkilö.hetu})`}
      </SecondaryHeading>
      <OppivelvollisuudenKeskeytysForm
        organisaatiot={organisaatiot}
        muokattavaKeskeytys={props.keskeytys}
        onSubmit={submit}
        onDelete={handleDelete}
        errors={isError(create) ? create.errors : []}
      />
    </Modal>
  )
}
