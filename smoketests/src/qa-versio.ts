import { setTimeout as sleep } from "node:timers/promises";

const serverUrl = "https://koski.testiopintopolku.fi";
const statusPath = "/koski/api/status";
const timeoutMillis = 5 * 60 * 1000;
const intervalMillis = 10 * 1000;
const requestTimeoutMillis = 10 * 1000;
const commitHashRegex = /^[0-9a-f]{40}$/;

function readExpectedCommitHash(): string {
  return (process.env.EXPECTED_COMMIT_HASH ?? "").toLowerCase();
}

async function readCommitHash(): Promise<string> {
  // cacheBust ohittaa mahdolliset välimuistit matkan varrella
  const statusResponse = await fetch(
    `${serverUrl}${statusPath}?cacheBust=${Date.now()}`,
    {
      signal: AbortSignal.timeout(requestTimeoutMillis),
    },
  );

  if (!statusResponse.ok) throw new Error(`HTTP ${statusResponse.status}`);

  const { commitHash } = (await statusResponse.json()) as {
    commitHash?: string;
  };

  return String(commitHash);
}

async function run(): Promise<void> {
  const expectedCommitHash = readExpectedCommitHash();
  if (!commitHashRegex.test(expectedCommitHash)) {
    throw new Error(`Invalid expected git commit hash: ${expectedCommitHash}`);
  }

  console.log(`Waiting for QA git commit ${expectedCommitHash}`);
  const deadlineMillis = Date.now() + timeoutMillis;

  while (Date.now() < deadlineMillis) {
    try {
      const observedCommitHash = await readCommitHash();
      console.log(
        `Expected ${expectedCommitHash}, observed ${observedCommitHash}`,
      );
      if (observedCommitHash.toLowerCase() === expectedCommitHash) return;
    } catch (error) {
      console.log(
        `Expected ${expectedCommitHash}, status request failed: ${error}`,
      );
    }
    await sleep(intervalMillis);
  }

  throw new Error(`Timed out waiting for QA git commit ${expectedCommitHash}`);
}

run().catch((error) => {
  console.error(error);
  process.exit(1);
});
