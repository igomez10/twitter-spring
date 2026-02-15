import { useMemo, useState } from "react";
import { useNavigate } from "react-router-dom";
import { TweetSection } from "../components/TweetSection";
import { UserSection } from "../components/UserSection";
import { useAuth } from "../state/AuthContext";

type Tab = "users" | "tweets";

export function DashboardPage() {
  const { session, logout } = useAuth();
  const navigate = useNavigate();
  const [tab, setTab] = useState<Tab>("users");

  const canReadUsers = useMemo(
    () => Boolean(session?.actions.includes("user:read") || session?.actions.includes("user:write")),
    [session]
  );
  const canReadTweets = useMemo(
    () => Boolean(session?.actions.includes("tweet:read") || session?.actions.includes("tweet:write")),
    [session]
  );

  if (!session) {
    return null;
  }

  const handleUnauthorized = () => {
    logout();
    navigate("/login", { replace: true });
  };

  return (
    <main className="layout">
      <header className="header">
        <div>
          <h1 className="title">Twitter Spring Console</h1>
          <p className="subtitle">Signed in as @{session.username}</p>
        </div>
        <div className="inline-actions">
          <div className="tabs" role="tablist" aria-label="Dashboard sections">
            <button
              type="button"
              className={`tab ${tab === "users" ? "active" : ""}`}
              onClick={() => setTab("users")}
              data-testid="tab-users"
            >
              Users
            </button>
            <button
              type="button"
              className={`tab ${tab === "tweets" ? "active" : ""}`}
              onClick={() => setTab("tweets")}
              data-testid="tab-tweets"
            >
              Tweets
            </button>
          </div>
          <button type="button" className="btn-secondary" onClick={logout} data-testid="logout-button">
            Log out
          </button>
        </div>
      </header>

      {tab === "users" ? (
        canReadUsers ? (
          <UserSection token={session.token} onUnauthorized={handleUnauthorized} />
        ) : (
          <section className="card">
            <p className="notice notice-error">Your token does not include any user permissions.</p>
          </section>
        )
      ) : canReadTweets ? (
        <TweetSection token={session.token} onUnauthorized={handleUnauthorized} />
      ) : (
        <section className="card">
          <p className="notice notice-error">Your token does not include any tweet permissions.</p>
        </section>
      )}
    </main>
  );
}
