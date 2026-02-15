import { render, screen, waitFor } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { MemoryRouter, Route, Routes } from "react-router-dom";
import { vi } from "vitest";
import { LoginPage } from "./LoginPage";
import { AuthProvider } from "../state/AuthContext";
import * as api from "../lib/api";
import { ACTIONS_KEY, TOKEN_KEY, USERNAME_KEY } from "../lib/session";

vi.mock("../lib/api", async () => {
  const actual = await vi.importActual<typeof import("../lib/api")>("../lib/api");
  return {
    ...actual,
    requestToken: vi.fn()
  };
});

describe("LoginPage", () => {
  it("stores session and navigates to dashboard", async () => {
    const tokenSpy = vi.mocked(api.requestToken);
    tokenSpy.mockResolvedValue({ access_token: "token-123", actions: ["user:write"] });

    render(
      <AuthProvider>
        <MemoryRouter initialEntries={["/login"]}>
          <Routes>
            <Route path="/login" element={<LoginPage />} />
            <Route path="/dashboard" element={<div>dashboard</div>} />
          </Routes>
        </MemoryRouter>
      </AuthProvider>
    );

    await userEvent.type(screen.getByLabelText("Username"), "nacho");
    await userEvent.type(screen.getByLabelText("Password"), "password123");
    await userEvent.click(screen.getByTestId("login-submit"));

    expect(await screen.findByText("dashboard")).toBeInTheDocument();
    expect(localStorage.getItem(TOKEN_KEY)).toBe("token-123");
    expect(localStorage.getItem(USERNAME_KEY)).toBe("nacho");
    expect(localStorage.getItem(ACTIONS_KEY)).toBe(JSON.stringify(["user:write"]));
  });

  it("renders error for invalid credentials", async () => {
    const tokenSpy = vi.mocked(api.requestToken);
    tokenSpy.mockRejectedValue(new api.ApiClientError(401, "Unauthorized", { message: "Invalid credentials" }));

    render(
      <AuthProvider>
        <MemoryRouter initialEntries={["/login"]}>
          <Routes>
            <Route path="/login" element={<LoginPage />} />
            <Route path="/dashboard" element={<div>dashboard</div>} />
          </Routes>
        </MemoryRouter>
      </AuthProvider>
    );

    await userEvent.type(screen.getByLabelText("Username"), "bad");
    await userEvent.type(screen.getByLabelText("Password"), "credentials");
    await userEvent.click(screen.getByTestId("login-submit"));

    await waitFor(() => {
      expect(screen.getByText("Invalid credentials")).toBeInTheDocument();
    });
  });
});
