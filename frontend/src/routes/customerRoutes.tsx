import { Route } from 'react-router-dom';
import CustomerLayout from '../pages/customer/CustomerLayout';
import Orders from '../pages/customer/Orders';
import AccountOverview from '../pages/customer/account/AccountOverview';
import AccountEdit from '../pages/customer/account/AccountEdit';
import AccountSecurity from '../pages/customer/account/AccountSecurity';
import Cart from '../pages/customer/cart';
import OrderSummary from '../pages/customer/checkout';
import RouteGuard from '../components/guards/RouteGuard';
import OrderList from '../pages/customer/orders/OrderList';
import OrderDetails from '../pages/customer/orders/OrderDetails';

export const CustomerRoutes = (
  <Route
    path="/customer"
    element={
      <RouteGuard requiredRoles={['ROLE_USER']}>
        <CustomerLayout />
      </RouteGuard>
    }
  >
    <Route path="cart" element={<Cart />} />
    <Route path="checkout" element={<OrderSummary />} />
    <Route path="orders" element={<OrderList />} />
    <Route path="orders/:orderId" element={<OrderDetails />} />
    <Route path="account">
      <Route index element={<AccountOverview />} />
      <Route path="edit" element={<AccountEdit />} />
      <Route path="security" element={<AccountSecurity />} />
    </Route>
  </Route>
);
