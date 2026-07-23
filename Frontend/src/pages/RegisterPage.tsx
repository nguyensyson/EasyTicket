import { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { Button } from "@/components/ui/Button";
import { register } from "@/services/userService";
import { ApiError } from "@/lib/apiClient";
import type { UserRole } from "@/types/event";

const ROLE_OPTIONS: { value: UserRole; label: string; hint: string }[] = [
  { value: "buyer", label: "Người mua vé", hint: "Tìm & đặt vé sự kiện" },
  { value: "organizer", label: "Nhà tổ chức", hint: "Tạo & quản lý sự kiện" },
];

const ERROR_MESSAGES: Record<string, string> = {
  USER_ALREADY_EXISTS: "Tên đăng nhập hoặc email đã được sử dụng.",
  REGISTRATION_FAILED: "Đăng ký thất bại. Vui lòng thử lại sau.",
  KEYCLOAK_UNAVAILABLE: "Hệ thống xác thực đang tạm thời gián đoạn. Vui lòng thử lại sau.",
  NETWORK_ERROR: "Không thể kết nối tới máy chủ. Vui lòng thử lại.",
};

export function RegisterPage() {
  const [fullName, setFullName] = useState("");
  const [username, setUsername] = useState("");
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [role, setRole] = useState<UserRole>("buyer");
  const [error, setError] = useState<string | null>(null);
  const [submitting, setSubmitting] = useState(false);
  const navigate = useNavigate();

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault();
    setError(null);
    setSubmitting(true);
    try {
      await register(role, { username, password, email, fullName });
      navigate("/login", {
        replace: true,
        state: { registered: true, username },
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
        <h1 className="mb-1.5 text-xl font-bold">Tạo tài khoản</h1>
        <p className="mb-6 text-sm text-muted">
          Đăng ký tài khoản để bắt đầu mua vé hoặc tổ chức sự kiện.
        </p>
        <form onSubmit={handleSubmit} className="flex flex-col gap-3">
          <div className="mb-1 grid grid-cols-2 gap-2.5">
            {ROLE_OPTIONS.map((opt) => (
              <button
                key={opt.value}
                type="button"
                onClick={() => setRole(opt.value)}
                className={`cursor-pointer rounded-lg border px-3 py-2.5 text-left transition-colors ${
                  role === opt.value
                    ? "border-green bg-green-tint"
                    : "border-border-soft bg-white"
                }`}
              >
                <div className="text-sm font-bold">{opt.label}</div>
                <div className="text-xs text-muted">{opt.hint}</div>
              </button>
            ))}
          </div>
          <input
            type="text"
            required
            placeholder="Họ và tên"
            value={fullName}
            onChange={(e) => setFullName(e.target.value)}
            className="w-full rounded-lg border border-border-soft px-3.5 py-2.5 text-sm outline-none focus:border-green"
          />
          <input
            type="text"
            required
            minLength={3}
            maxLength={50}
            placeholder="Tên đăng nhập"
            value={username}
            onChange={(e) => setUsername(e.target.value)}
            className="w-full rounded-lg border border-border-soft px-3.5 py-2.5 text-sm outline-none focus:border-green"
          />
          <input
            type="email"
            required
            placeholder="Email"
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            className="w-full rounded-lg border border-border-soft px-3.5 py-2.5 text-sm outline-none focus:border-green"
          />
          <input
            type="password"
            required
            minLength={8}
            maxLength={128}
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
          <Button
            type="submit"
            variant="green"
            className="mt-2 w-full"
            disabled={submitting}
          >
            {submitting
              ? "Đang xử lý..."
              : role === "organizer"
                ? "Đăng ký tài khoản tổ chức"
                : "Đăng ký"}
          </Button>
        </form>
        <p className="mt-5 text-center text-sm text-muted">
          Đã có tài khoản?{" "}
          <Link to="/login" className="font-semibold text-green">
            Đăng nhập
          </Link>
        </p>
      </div>
    </main>
  );
}
