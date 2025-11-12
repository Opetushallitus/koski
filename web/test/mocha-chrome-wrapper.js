/* eslint-disable @typescript-eslint/no-require-imports */
process.env.CHROME_PATH = require('puppeteer').executablePath()


process.on("unhandledRejection", (reason) => {
  console.error(reason);
  process.exit(1);
});

const { spawnSync } = require("child_process");

const args = process.argv.slice(2);
spawnSync("node", ["node_modules/mocha-chrome/cli.js", ...args], {
  stdio: "inherit",
});
