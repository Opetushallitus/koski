import { format, parseISO } from "date-fns"
import React, { useEffect, useState } from "react"
import { T } from "../../i18n/i18n"
import { InfoIcon } from "../icons/Icon"
import "./Aikaleima.less"

export const Aikaleima = () => {
  const [time, setTime] = useState<Date | null>(null)
  useEffect(() => {
    const ab = new AbortController()
    fetch("/koski/valpas/api/paivitysaika", {
      signal: ab.signal,
      method: "GET",
    }).then(async (res) => {
      if (res.ok) {
        const data = await res.json()
        try {
          setTime(parseISO(data))
        } catch (err) {
          console.error(err)
        }
      }
    })
    return () => {
      ab.abort()
    }
  }, [])
  return (
    <div className="aikaleima">
      {time && (
        <>
          <div>
            <InfoIcon />
          </div>
          <div>
            <T id="valpas-report-timestamp-part-1" />{" "}
            {format(time, "dd.LL.yyyy HH:mm:ss")}{" "}
            <T id="valpas-report-timestamp-part-2" />
          </div>
        </>
      )}
    </div>
  )
}
