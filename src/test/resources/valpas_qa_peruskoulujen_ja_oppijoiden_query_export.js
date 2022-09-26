const fs = require("fs")
const path = require("path")

const oppilaitosOids = fs.readFileSync(path.join(__dirname, "valpas_qa_peruskoulujen_oidit.txt")).toString("utf-8").split("\n").map(row => row.trim())
const oppijaOids = fs.readFileSync(path.join(__dirname, "valpas_qa_oppija_oidit.txt")).toString("utf-8").split("\n").map(row => row.trim())

const query = `WITH oppilaitokset AS (
    SELECT unnest(string_to_array(
        -- Tähän riville kaikki oppilaitos-oidit välilyönnillä erotettuna:
            '${oppilaitosOids.join(" ")}',
            ' '
        )) AS oppilaitos_oid
)
   , loytyvat_oppijat AS (
        SELECT unnest(string_to_array(
            -- Tähän riville kaikki oppija-oidit, jotka löytyvät Valppaasta (valpas_qa_oppija_oidit.txt) välilyönnillä erotettuna:
                '${oppijaOids.join(" ")}',
                ' '
            )) AS oppija_oid
    )
SELECT
    concat(
            oppilaitos_oid,
            ' ',
            (SELECT
                 string_agg(DISTINCT r_opiskeluoikeus.oppija_oid, ' ')
             FROM
                 r_opiskeluoikeus
                     JOIN loytyvat_oppijat ON r_opiskeluoikeus.oppija_oid = loytyvat_oppijat.oppija_oid
             WHERE
                 oppilaitos_oid = oppilaitokset.oppilaitos_oid)
        ) AS row
FROM oppilaitokset;`

fs.writeFileSync(path.join(__dirname, "valpas_qa_peruskoulujen_ja_oppijoiden_oidit.sql"), query)
