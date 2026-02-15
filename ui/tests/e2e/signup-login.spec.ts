import { expect, test } from "@playwright/test";
import { uniqueSuffix } from "./helpers";

test("visit signup, create account, and login with created credentials", async ({ page }) => {
  const suffix = uniqueSuffix();
  const username = `signup${suffix}`;
  const password = `Pass${suffix}!`;
  const handle = `signup${suffix}`;
  const email = `signup${suffix}@example.com`;

  await page.goto("/signup");
  await page.getByLabel("First name").fill("Signup");
  await page.getByLabel("Last name").fill("Flow");
  await page.getByLabel("Email").fill(email);
  await page.getByLabel("Handle").fill(handle);
  await page.getByLabel("Username").fill(username);
  await page.getByLabel("Password").fill(password);
  await page.getByTestId("signup-submit").click();

  await expect(page.getByText("Account created. You can now sign in.")).toBeVisible();

  await page.goto("/login");
  await page.getByLabel("Username").fill(username);
  await page.getByLabel("Password").fill(password);
  await page.getByTestId("login-submit").click();

  await expect(page.getByRole("heading", { name: "Twitter Spring Console" })).toBeVisible();
  await expect(page.getByText(`Signed in as @${username}`)).toBeVisible();
});
