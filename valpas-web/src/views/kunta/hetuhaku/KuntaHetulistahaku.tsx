import bem from "bem-ts"
import * as A from "fp-ts/Array"
import React, { useMemo, useState } from "react"
import { downloadRouhintaHetuilla } from "../../../api/api"
import { useApiMethod } from "../../../api/apiHooks"
import { isError, isLoading } from "../../../api/apiUtils"
import { RaisedButton } from "../../../components/buttons/RaisedButton"
import { Form } from "../../../components/forms/Form"
import { TextAreaField } from "../../../components/forms/TextField"
import { Spinner } from "../../../components/icons/Spinner"
import { Password } from "../../../components/Password"
import { ApiErrors } from "../../../components/typography/error"
import { TertiaryHeading } from "../../../components/typography/headings"
import { T, TParagraphs, useLanguage } from "../../../i18n/i18n"
import { parseHetulikes } from "../../../state/hetu"
import { usePassword } from "../../../state/password"
import "./KuntaHetulistahaku.less"
import { Rouhintaohje } from "./Rouhintaohje"

const b = bem("kuntahetulista")

export const KuntaHetulistahaku = () => {
  const password = usePassword()
  const hetus = useHetulist()
  const lang = useLanguage()
  const download = useApiMethod(downloadRouhintaHetuilla)

  const submit = () => {
    hetus.set(hetus.list.join("\n"))
    download.call({
      hetut: hetus.list,
      password,
      lang,
    })
  }

  return (
    <Form className={b()}>
      <TertiaryHeading>
        <T id="rouhinta_hae_usealla_hetulla" />
      </TertiaryHeading>
      <Rouhintaohje className={b("ohje")}>
        <TParagraphs id="rouhinta_hetuhaku_ohje" />
      </Rouhintaohje>
      <TextAreaField value={hetus.fieldValue} onChange={hetus.set} />
      <div>
        <Password>{password}</Password>
      </div>
      {isError(download) && <ApiErrors errors={download.errors} />}
      <RaisedButton
        onClick={submit}
        disabled={A.isEmpty(hetus.list) || isLoading(download)}
      >
        <T
          id="rouhinta_lataa_raportti"
          params={{ lukumäärä: hetus.list.length }}
        />
      </RaisedButton>
      {isLoading(download) && <Spinner />}
    </Form>
  )
}

const useHetulist = () => {
  const [state, setState] = useState("")

  return useMemo(
    () => ({
      fieldValue: state,
      set: setState,
      list: parseHetulikes(state),
    }),
    [state],
  )
}
