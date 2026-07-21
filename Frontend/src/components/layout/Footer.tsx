export function Footer() {
  return (
    <footer className="flex flex-col gap-4 bg-dark px-4 py-9 text-[13px] text-[#B8B8AE] sm:flex-row sm:items-center sm:justify-between sm:px-10">
      <div>© 2026 EasyTicket. Nền tảng bán vé sự kiện.</div>
      <div className="flex flex-wrap gap-5">
        <a href="#" className="text-[13px] text-[#B8B8AE] hover:text-gold">
          Về chúng tôi
        </a>
        <a href="#" className="text-[13px] text-[#B8B8AE] hover:text-gold">
          Điều khoản
        </a>
        <a href="#" className="text-[13px] text-[#B8B8AE] hover:text-gold">
          Hỗ trợ
        </a>
      </div>
    </footer>
  );
}
