import { createContext, useContext, useMemo, useState, type ReactNode } from "react";
import { requestToken } from "../lib/api";
import { clearSession, loadSession, saveSession } from "../lib/session";
import type { Session } from "../lib/types";

type AuthContextType = {
  session: Session | null;
  loginWithPassword: (username: string, password: string) => Promise<void>;
  logout: () => void;
};

const AuthContext = createContext<AuthContextType | undefined>(undefined);

type AuthProviderProps = {
  children: ReactNode;
};

export function AuthProvider({ children }: AuthProviderProps) {
  const [session, setSession] = useState<Session | null>(() => loadSession());

  const logout = () => {
    clearSession();
    setSession(null);
  };

  const loginWithPassword = async (username: string, password: string) => {
    const response = await requestToken({ username, password });
    const nextSession: Session = {
      token: response.access_token,
      actions: response.actions,
      username
    };
    saveSession(nextSession);
    setSession(nextSession);
  };

  const value = useMemo(
    () => ({
      session,
      loginWithPassword,
      logout
    }),
    [session]
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth(): AuthContextType {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error("useAuth must be used within AuthProvider");
  }
  return context;
}
