import { Link } from "react-router-dom";
import { Button } from "@/components/ui/Button";

export function NotFoundPage() {
  return (
    <main className="flex flex-1 flex-col items-center justify-center gap-4 px-4 py-15 text-center">
      <div className="text-5xl font-extrabold text-gold">404</div>
      <p className="text-muted">Không tìm thấy trang bạn yêu cầu.</p>
      <Link to="/">
        <Button variant="dark">Về trang chủ</Button>
      </Link>
    </main>
  );
}
