import { useState } from "react";
import { Link, useLocation, useNavigate } from "react-router-dom";
import { useAuth } from "@/hooks/useAuth";
import { Button } from "@/components/ui/Button";
import type { UserRole } from "@/types/event";

const ROLE_OPTIONS: { value: UserRole; label: string }[] = [
  { value: "buyer", label: "Người mua vé" },
  { value: "organizer", label: "Nhà tổ chức" },
];

export function LoginPage() {
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [role, setRole] = useState<UserRole>("buyer");
  const { login } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();

  const from = (location.state as { from?: string } | null)?.from || null;

  function handleSubmit(e: React.FormEvent) {
    e.preventDefault();
    login(email, role);
    navigate(from || (role === "organizer" ? "/organizer" : "/"), {
      replace: true,
    });
  }

  return (
    <main className="flex flex-1 items-center justify-center px-4 py-15 sm:px-10">
      <div className="w-full max-w-[400px] rounded-card border border-border bg-white p-7">
        <div className="mb-6 text-center text-2xl font-extrabold text-ink">
          Easy<span className="text-gold">Ticket</span>
        </div>
        <h1 className="mb-1.5 text-xl font-bold">Đăng nhập</h1>
        <p className="mb-6 text-sm text-muted">
          Bản demo giao diện — chưa kết nối User Service, đăng nhập chỉ mô
          phỏng phiên làm việc cục bộ. Nếu email đã từng đăng ký, vai trò lưu
          từ trước sẽ được dùng lại thay vì lựa chọn dưới đây.
        </p>
        <form onSubmit={handleSubmit} className="flex flex-col gap-3">
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
            placeholder="Mật khẩu"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            className="w-full rounded-lg border border-border-soft px-3.5 py-2.5 text-sm outline-none focus:border-green"
          />
          <div className="grid grid-cols-2 gap-2.5">
            {ROLE_OPTIONS.map((opt) => (
              <button
                key={opt.value}
                type="button"
                onClick={() => setRole(opt.value)}
                className={`cursor-pointer rounded-lg border px-3 py-2.5 text-sm font-semibold transition-colors ${
                  role === opt.value
                    ? "border-green bg-green-tint"
                    : "border-border-soft bg-white"
                }`}
              >
                {opt.label}
              </button>
            ))}
          </div>
          <Button type="submit" variant="green" className="mt-2 w-full">
            Đăng nhập
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
