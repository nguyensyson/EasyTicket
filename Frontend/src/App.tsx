import { BrowserRouter, Route, Routes } from "react-router-dom";
import { CartProvider } from "@/context/CartContext";
import { AuthProvider } from "@/context/AuthContext";
import { OrderProvider } from "@/context/OrderContext";
import { OrganizerEventProvider } from "@/context/OrganizerEventContext";
import { MainLayout } from "@/components/layout/MainLayout";
import { OrganizerLayout } from "@/components/organizer/OrganizerLayout";
import { HomePage } from "@/pages/HomePage";
import { EventDetailPage } from "@/pages/EventDetailPage";
import { CheckoutPage } from "@/pages/CheckoutPage";
import { OrderSuccessPage } from "@/pages/OrderSuccessPage";
import { LoginPage } from "@/pages/LoginPage";
import { RegisterPage } from "@/pages/RegisterPage";
import { OrderHistoryPage } from "@/pages/OrderHistoryPage";
import { NotFoundPage } from "@/pages/NotFoundPage";
import { OrganizerDashboardPage } from "@/pages/organizer/OrganizerDashboardPage";
import { OrganizerEventsPage } from "@/pages/organizer/OrganizerEventsPage";
import { OrganizerEventFormPage } from "@/pages/organizer/OrganizerEventFormPage";
import { OrganizerEventDashboardPage } from "@/pages/organizer/OrganizerEventDashboardPage";

function App() {
  return (
    <AuthProvider>
      <CartProvider>
        <OrderProvider>
          <OrganizerEventProvider>
            <BrowserRouter>
              <Routes>
                <Route element={<MainLayout />}>
                  <Route path="/" element={<HomePage />} />
                  <Route path="/events/:id" element={<EventDetailPage />} />
                  <Route path="/checkout/:id" element={<CheckoutPage />} />
                  <Route
                    path="/order-success/:orderId"
                    element={<OrderSuccessPage />}
                  />
                  <Route path="/login" element={<LoginPage />} />
                  <Route path="/register" element={<RegisterPage />} />
                  <Route path="/account/orders" element={<OrderHistoryPage />} />
                  <Route path="*" element={<NotFoundPage />} />
                </Route>

                <Route path="/organizer" element={<OrganizerLayout />}>
                  <Route index element={<OrganizerDashboardPage />} />
                  <Route path="events" element={<OrganizerEventsPage />} />
                  <Route path="events/new" element={<OrganizerEventFormPage />} />
                  <Route
                    path="events/:id/edit"
                    element={<OrganizerEventFormPage />}
                  />
                  <Route
                    path="events/:id/dashboard"
                    element={<OrganizerEventDashboardPage />}
                  />
                </Route>
              </Routes>
            </BrowserRouter>
          </OrganizerEventProvider>
        </OrderProvider>
      </CartProvider>
    </AuthProvider>
  );
}

export default App;
