const baseUrl = process.env.KOSKI_BASE_URL || "https://koski.testiopintopolku.fi/koski";
const user = process.env.KOSKI_USER;
const pass = process.env.KOSKI_PASS;
const opiskeluoikeusOid = process.env.TIEDOTE_SMOKETEST_OO_OID;

const pollIntervalMs = Number(process.env.TIEDOTE_POLL_INTERVAL_MS || "5000");
const pollTimeoutMs = Number(process.env.TIEDOTE_POLL_TIMEOUT_MS || "300000");

if (!user || !pass || !opiskeluoikeusOid) {
  console.error("Required env vars: KOSKI_USER, KOSKI_PASS, TIEDOTE_SMOKETEST_OO_OID");
  process.exit(1);
}

const authHeader = "Basic " + Buffer.from(`${user}:${pass}`).toString("base64");

const headers: Record<string, string> = {
  Authorization: authHeader,
  "Content-Type": "application/json",
};

interface TiedoteJob {
  id: string;
  oppijaOid: string;
  opiskeluoikeusOid: string;
  state: string;
  createdAt: string;
  completedAt: string | null;
  worker: string | null;
  attempts: number;
  error: string | null;
  opiskeluoikeusVersio: number;
}

const sleep = (ms: number) => new Promise((resolve) => setTimeout(resolve, ms));

async function resetTiedoteJob(): Promise<void> {
  console.log(`Resetting tiedote job for ${opiskeluoikeusOid}...`);
  const res = await fetch(`${baseUrl}/api/tiedote/jobs/${opiskeluoikeusOid}`, {
    method: "DELETE",
    headers,
  });

  if (res.status === 200) {
    console.log("Existing tiedote job deleted");
  } else if (res.status === 404) {
    console.log("No existing tiedote job (404), proceeding");
  } else {
    throw new Error(`Reset failed with status ${res.status}: ${await res.text()}`);
  }
}

async function triggerRun(): Promise<void> {
  console.log("Triggering tiedote run...");
  const res = await fetch(`${baseUrl}/api/tiedote/run`, { headers });

  if (res.status !== 200) {
    throw new Error(`Trigger failed with status ${res.status}: ${await res.text()}`);
  }
  console.log("Tiedote run triggered");
}

async function pollForCompletion(): Promise<TiedoteJob> {
  console.log(`Polling for completed tiedote job (timeout ${pollTimeoutMs / 1000}s)...`);
  const deadline = Date.now() + pollTimeoutMs;

  while (Date.now() < deadline) {
    const res = await fetch(
      `${baseUrl}/api/tiedote/jobs?state=COMPLETED`,
      { headers }
    );

    if (res.status !== 200) {
      throw new Error(`Poll failed with status ${res.status}: ${await res.text()}`);
    }

    const jobs: TiedoteJob[] = await res.json();
    const job = jobs.find((j) => j.opiskeluoikeusOid === opiskeluoikeusOid);

    if (job) {
      return job;
    }

    // Tarkista myös virhetila
    const errorRes = await fetch(
      `${baseUrl}/api/tiedote/jobs?state=ERROR`,
      { headers }
    );
    if (errorRes.status === 200) {
      const errorJobs: TiedoteJob[] = await errorRes.json();
      const errorJob = errorJobs.find((j) => j.opiskeluoikeusOid === opiskeluoikeusOid);
      if (errorJob) {
        throw new Error(
          `Tiedote job ended in ERROR state: ${errorJob.error || "unknown error"}`
        );
      }
    }

    const remaining = Math.round((deadline - Date.now()) / 1000);
    console.log(`  Not yet completed, ${remaining}s remaining...`);
    await sleep(pollIntervalMs);
  }

  throw new Error(`Tiedote job did not complete within ${pollTimeoutMs / 1000}s`);
}

function assertJob(job: TiedoteJob): void {
  console.log(`Job completed: id=${job.id}, versio=${job.opiskeluoikeusVersio}`);

  if (job.opiskeluoikeusVersio <= 0) {
    throw new Error(
      `opiskeluoikeusVersio is ${job.opiskeluoikeusVersio}, expected > 0 (Kitu may have failed)`
    );
  }

  console.log("Assertions passed: COMPLETED state + versio > 0 (Kitu OK + Tiedotuspalvelu OK)");
}

async function run(): Promise<void> {
  console.log("=== Kielitutkintotodistus tiedote smoke test ===");
  console.log(`Base URL: ${baseUrl}`);
  console.log(`Opiskeluoikeus OID: ${opiskeluoikeusOid}`);
  console.log();

  await resetTiedoteJob();
  await triggerRun();
  const job = await pollForCompletion();
  assertJob(job);

  console.log();
  console.log("=== Smoke test passed! ===");
}

run().catch((err) => {
  console.error(`\nSmoke test FAILED: ${err.message}`);
  process.exit(1);
});
