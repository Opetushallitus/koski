const { apply_patch } = require("jsonpatch");
const { writeFileSync, existsSync, readFileSync } = require("fs");

const run = async () => {
  const file = readFileSync("historia.jsons");
  const history = file
    .toString()
    .split("\n")
    .filter((s) => !!s)
    .map(JSON.parse);

  history.sort((a, b) => a.versionumero - b.versionumero);

  history.reduce((data, patch) => {
    const hotfix = `hotfix-${patch.versionumero}.json`;
    const patchOrHotfix = existsSync(hotfix)
      ? (console.log(`Yliajetaan hotfix patchille ${patch.versionumero}`),
        JSON.parse(readFileSync(hotfix)))
      : patch.muutos;

    return patchOrHotfix.reduce((prevData, singlePatch, singlePatchIndex) => {
      try {
        return apply_patch(prevData, [singlePatch]);
      } catch (e) {
        console.log(
          `Versionumero ${patch.versionumero} aiheuttaa virheen: ${e.message}\n`
        );
        console.log(`muutos[${singlePatchIndex}]:`, singlePatch);
        writeFileSync("dump.json", JSON.stringify(prevData, null, 2));
        writeFileSync(hotfix, JSON.stringify(patch.muutos, null, 2));
        process.exit(1);
      }
    }, data);
  }, {});

  console.log("Historia on kunnossa.");
};

run().catch(console.error);
