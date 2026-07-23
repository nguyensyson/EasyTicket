import { useState } from "react";
import { Link, useLocation, useNavigate } from "react-router-dom";
import { useAuth } from "@/hooks/useAuth";
import { Button } from "@/components/ui/Button";
import { ApiError } from "@/lib/apiClient";

const ERROR_MESSAGES: Record<string, string> = {
  INVALID_CREDENTIALS: "Tên đăng nhập hoặc mật khẩu không đúng.",
  VALIDATION_ERROR: "Vui lòng kiểm tra lại thông tin đăng nhập.",
  KEYCLOAK_UNAVAILABLE: "Hệ thống xác thực đang tạm thời gián đoạn. Vui lòng thử lại sau.",
  NETWORK_ERROR: "Không thể kết nối tới máy chủ. Vui lòng thử lại.",
};

export function LoginPage() {
  const location = useLocation();
  const state = location.state as { from?: string; registered?: boolean; username?: string } | null;
  const [username, setUsername] = useState(state?.username || "");
  const [password, setPassword] = useState("");
  const [error, setError] = useState<string | null>(null);
  const [submitting, setSubmitting] = useState(false);
  const { login } = useAuth();
  const navigate = useNavigate();

  const from = state?.from || null;

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault();
    setError(null);
    setSubmitting(true);
    try {
      const user = await login(username, password);
      navigate(from || (user.role === "organizer" ? "/organizer" : "/"), {
        replace: true,
      });
    } catch (err) {
      if (err instanceof ApiError) {
        setError(ERROR_MESSAGES[err.errorCode] || err.message);
      } else {
        setError("Đã có lỗi xảy ra. Vui lòng thử lại.");
      }
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <main className="flex flex-1 items-center justify-center px-4 py-15 sm:px-10">
      <div className="w-full max-w-[400px] rounded-card border border-border bg-white p-7">
        <div className="mb-6 text-center text-2xl font-extrabold text-ink">
          Easy<span className="text-gold">Ticket</span>
        </div>
        <h1 className="mb-1.5 text-xl font-bold">Đăng nhập</h1>
        {state?.registered && (
          <p className="mb-4 rounded-lg bg-green-tint px-3 py-2 text-sm text-green">
            Đăng ký thành công! Vui lòng đăng nhập để tiếp tục.
          </p>
        )}
        <form onSubmit={handleSubmit} className="flex flex-col gap-3">
          <input
            type="text"
            required
            placeholder="Tên đăng nhập"
            value={username}
            onChange={(e) => setUsername(e.target.value)}
            className="w-full rounded-lg border border-border-soft px-3.5 py-2.5 text-sm outline-none focus:border-green"
          />
          <input
            type="password"
            required
            placeholder="Mật khẩu"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            className="w-full rounded-lg border border-border-soft px-3.5 py-2.5 text-sm outline-none focus:border-green"
          />
          {error && (
            <p className="rounded-lg bg-red-50 px-3 py-2 text-sm text-red-600">
              {error}
            </p>
          )}
          <Button type="submit" variant="green" className="mt-2 w-full" disabled={submitting}>
            {submitting ? "Đang đăng nhập..." : "Đăng nhập"}
          </Button>
        </form>
        <p className="mt-5 text-center text-sm text-muted">
          Chưa có tài khoản?{" "}
          <Link to="/register" className="font-semibold text-green">
            Đăng ký
          </Link>
        </p>
      </div>
    </main>
  );
}
