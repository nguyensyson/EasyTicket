import { Outlet } from "react-router-dom";
import { Header } from "./Header";
import { Footer } from "./Footer";

export function MainLayout() {
  return (
    <div className="flex min-h-screen flex-col bg-cream text-ink">
      <Header />
      <Outlet />
      <Footer />
    </div>
  );
}
