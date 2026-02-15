import { render, screen } from "@testing-library/react";
import { MemoryRouter, Route, Routes } from "react-router-dom";
import { RequireAuth } from "./RequireAuth";
import { AuthProvider } from "../state/AuthContext";

describe("RequireAuth", () => {
  it("redirects to login when there is no active session", async () => {
    render(
      <AuthProvider>
        <MemoryRouter initialEntries={["/private"]}>
          <Routes>
            <Route path="/login" element={<div>login-page</div>} />
            <Route
              path="/private"
              element={
                <RequireAuth>
                  <div>private-page</div>
                </RequireAuth>
              }
            />
          </Routes>
        </MemoryRouter>
      </AuthProvider>
    );

    expect(await screen.findByText("login-page")).toBeInTheDocument();
  });
});
