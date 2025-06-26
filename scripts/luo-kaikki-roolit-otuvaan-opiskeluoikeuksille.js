const fs = require("fs");

const palvelu = "KOSKI";

const run = async () => {
  const getJson = async (url) => {
    const response = await fetch(url);
    return await response.json();
  };

  const otuvaSqlFiles = await Promise.all(
    (
      await getJson(
        "https://api.github.com/repos/opetushallitus/otuva/contents/kayttooikeus-service/src/main/resources/db/migration"
      )
    ).map(async (e) => {
      const response = await fetch(e.download_url);
      return await response.text();
    })
  );

  const olemassaolevatRoolit = otuvaSqlFiles.flatMap((sql) =>
    sql
      .split("\n")
      .filter((row) => row.includes(`insertkayttooikeus('${palvelu}',`))
      .map((row) => {
        const match = row.match(/.*insertkayttooikeus\('(\w+)',\s*'(\w+)'/);
        return match[2];
      })
  );

  const schema = await getJson(
    "https://opintopolku.fi/koski/api/documentation/koski-oppija-schema.json"
  );

  const defs = schema["definitions"];

  const getKoodistonimet = async (koodistoUri) => {
    const koodisto = await getJson(
      `https://virkailija.opintopolku.fi/koodisto-service/rest/codes/${koodistoUri}`
    );

    const versio = koodisto.latestKoodistoVersio.versio;

    const koodit = await getJson(
      `https://virkailija.opintopolku.fi/koodisto-service/rest/codeelement/codes/withrelations/${koodistoUri}/${versio}`
    );

    return Object.fromEntries(
      koodit.map((k) => [
        k.koodiArvo,
        k.metadata.find((m) => m.kieli === "FI").nimi,
      ])
    );
  };

  const ooKoodit = await getKoodistonimet("opiskeluoikeudentyyppi");
  const suoritusKoodit = await getKoodistonimet("suorituksentyyppi");

  const parseRef = (o) =>
    o.anyOf !== undefined
      ? parseRefs(o.anyOf)
      : o.$ref !== undefined
      ? parseRef(defs[o.$ref.replace("#/definitions/", "")])
      : o;

  const parseRefs = (objs) => objs.map(parseRef);

  const ensureArr = (a) => (Array.isArray(a) ? a : [a]);

  const oos = parseRef(defs.opiskeluoikeus);

  const printSql = (rooli, kuvaus) => {
    if (!olemassaolevatRoolit.includes(rooli)) {
      console.log(
        `SELECT insertkayttooikeus('${palvelu}', '${rooli}', '${kuvaus}');`
      );
    }
  };

  oos.forEach((oo) => {
    const ooTyyppi = oo.properties.tyyppi.properties.koodiarvo.enum[0];
    const suoritukset = ensureArr(
      parseRef(oo.properties.suoritukset.items)
    ).flatMap((s) => s.properties.tyyppi.properties.koodiarvo.enum);

    printSql(ooTyyppi.toUpperCase(), ooKoodit[ooTyyppi]);

    if (suoritukset.length > 1) {
      suoritukset.forEach((s) => {
        const rooli = `${ooTyyppi}__${s}`.toUpperCase();
        const kuvaus = `${ooKoodit[ooTyyppi]}: ${suoritusKoodit[s]}`;
        printSql(rooli, kuvaus);
      });
    }
  });
};

run().catch(console.error);
