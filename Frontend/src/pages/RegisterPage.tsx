import { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { useAuth } from "@/hooks/useAuth";
import { Button } from "@/components/ui/Button";
import type { UserRole } from "@/types/event";

const ROLE_OPTIONS: { value: UserRole; label: string; hint: string }[] = [
  { value: "buyer", label: "Người mua vé", hint: "Tìm & đặt vé sự kiện" },
  { value: "organizer", label: "Nhà tổ chức", hint: "Tạo & quản lý sự kiện" },
];

export function RegisterPage() {
  const [name, setName] = useState("");
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [role, setRole] = useState<UserRole>("buyer");
  const { register } = useAuth();
  const navigate = useNavigate();

  function handleSubmit(e: React.FormEvent) {
    e.preventDefault();
    register(name, email, role);
    navigate(role === "organizer" ? "/organizer" : "/", { replace: true });
  }

  return (
    <main className="flex flex-1 items-center justify-center px-4 py-15 sm:px-10">
      <div className="w-full max-w-[400px] rounded-card border border-border bg-white p-7">
        <div className="mb-6 text-center text-2xl font-extrabold text-ink">
          Easy<span className="text-gold">Ticket</span>
        </div>
        <h1 className="mb-1.5 text-xl font-bold">Tạo tài khoản</h1>
        <p className="mb-6 text-sm text-muted">
          Bản demo giao diện — chưa kết nối User Service, đăng ký chỉ mô
          phỏng phiên làm việc cục bộ.
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
            value={name}
            onChange={(e) => setName(e.target.value)}
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
            placeholder="Mật khẩu"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            className="w-full rounded-lg border border-border-soft px-3.5 py-2.5 text-sm outline-none focus:border-green"
          />
          <Button type="submit" variant="green" className="mt-2 w-full">
            {role === "organizer" ? "Đăng ký & vào kênh tổ chức" : "Đăng ký"}
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
