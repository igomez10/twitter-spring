import { FormEvent, useState } from "react";
import { Link, useLocation, useNavigate } from "react-router-dom";
import { ApiClientError } from "../lib/api";
import { getErrorMessage } from "../lib/error";
import { useAuth } from "../state/AuthContext";

type LoginLocationState = {
  from?: string;
};

export function LoginPage() {
  const { loginWithPassword } = useAuth();
  const location = useLocation();
  const navigate = useNavigate();
  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState<string | null>(null);
  const [isSubmitting, setIsSubmitting] = useState(false);

  const destination = (location.state as LoginLocationState | null)?.from ?? "/dashboard";

  async function onSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setError(null);
    setIsSubmitting(true);
    try {
      await loginWithPassword(username, password);
      navigate(destination, { replace: true });
    } catch (err) {
      if (err instanceof ApiClientError && (err.status === 401 || err.status === 403)) {
        setError("Invalid credentials");
        return;
      }
      setError(getErrorMessage(err));
    } finally {
      setIsSubmitting(false);
    }
  }

  return (
    <main className="auth-shell">
      <section className="card auth-card">
        <h1 className="title">Log in</h1>
        <p className="subtitle">Access users and tweets with your API credentials.</p>

        <form onSubmit={onSubmit} className="form-grid" data-testid="login-form">
          <div className="full">
            <label htmlFor="username">Username</label>
            <input
              id="username"
              name="username"
              value={username}
              onChange={(event) => setUsername(event.target.value)}
              required
            />
          </div>

          <div className="full">
            <label htmlFor="password">Password</label>
            <input
              id="password"
              name="password"
              type="password"
              value={password}
              onChange={(event) => setPassword(event.target.value)}
              required
            />
          </div>

          <div className="full inline-actions">
            <button className="btn-primary" type="submit" disabled={isSubmitting} data-testid="login-submit">
              {isSubmitting ? "Signing in..." : "Sign in"}
            </button>
            <Link to="/signup">Create account</Link>
          </div>
        </form>

        {error ? <div className="notice notice-error">{error}</div> : null}
      </section>
    </main>
  );
}
