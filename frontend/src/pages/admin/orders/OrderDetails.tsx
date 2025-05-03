import React, { useEffect, useState } from 'react';
import { useParams } from 'react-router-dom';
import {
  Paper,
  Grid,
  Typography,
  Divider,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Chip,
  Alert,
} from '@mui/material';
import { OrderDTO, OrderStatus, PaymentMethod, ShippingMethod } from '../../../types/order';
import { adminOrderService } from '../../../services/adminOrderService';

const OrderDetails: React.FC = () => {
  const { orderId } = useParams<{ orderId: string }>();
  const [order, setOrder] = useState<OrderDTO | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const fetchOrderDetails = async () => {
      try {
        setLoading(true);
        setError(null);
        const data = await adminOrderService.getOrderById(Number(orderId));
        setOrder(data);
      } catch (error) {
        console.error('Error fetching order details:', error);
        setError('Failed to fetch order details. Please try again later.');
      } finally {
        setLoading(false);
      }
    };

    if (orderId) {
      fetchOrderDetails();
    }
  }, [orderId]);

  const formatDate = (date: string) => {
    return new Date(date).toLocaleString();
  };

  const formatPrice = (price: number) => {
    return new Intl.NumberFormat('pl-PL', {
      style: 'currency',
      currency: 'PLN',
    }).format(price);
  };

  const getStatusColor = (status: OrderStatus) => {
    switch (status) {
      case OrderStatus.PENDING:
        return 'warning';
      case OrderStatus.CONFIRMED:
        return 'info';
      case OrderStatus.PROCESSING:
        return 'info';
      case OrderStatus.SHIPPED:
        return 'secondary';
      case OrderStatus.DELIVERED:
        return 'success';
      case OrderStatus.CANCELLED:
        return 'error';
      case OrderStatus.REFUNDED:
        return 'default';
      case OrderStatus.FAILED:
        return 'error';
      default:
        return 'default';
    }
  };

  if (loading) {
    return <div className="p-6">Loading...</div>;
  }

  if (error) {
    return (
      <div className="p-6">
        <Alert severity="error">{error}</Alert>
      </div>
    );
  }

  if (!order) {
    return (
      <div className="p-6">
        <Alert severity="warning">Order not found</Alert>
      </div>
    );
  }

  return (
    <div className="p-6">
      <div className="flex justify-between items-center mb-6">
        <Typography variant="h4" component="h1">
          Order #{order.id}
        </Typography>
        <Chip
          label={order.orderStatus}
          color={getStatusColor(order.orderStatus)}
          variant="outlined"
        />
      </div>

      <Grid container spacing={3}>
        {/* Order Summary */}
        <Grid item xs={12} md={6}>
          <Paper className="p-4">
            <Typography variant="h6" className="mb-3">
              Order Summary
            </Typography>
            <div className="space-y-2">
              <div className="flex justify-between">
                <span className="text-gray-600">Order Date:</span>
                <span>{formatDate(order.orderDate || order.createdAt)}</span>
              </div>
              <div className="flex justify-between">
                <span className="text-gray-600">User ID:</span>
                <span>{order.userId}</span>
              </div>
              <div className="flex justify-between">
                <span className="text-gray-600">Payment Method:</span>
                <span>{order.paymentMethod.replace('_', ' ')}</span>
              </div>
              <div className="flex justify-between">
                <span className="text-gray-600">Shipping Method:</span>
                <span>{order.shippingMethod}</span>
              </div>
              {order.paymentTransactionId && (
                <div className="flex justify-between">
                  <span className="text-gray-600">Transaction ID:</span>
                  <span>{order.paymentTransactionId}</span>
                </div>
              )}
            </div>
          </Paper>
        </Grid>

        {/* Shipping Address */}
        <Grid item xs={12} md={6}>
          <Paper className="p-4">
            <Typography variant="h6" className="mb-3">
              Shipping Address
            </Typography>
            <div className="space-y-1">
              <div>{order.shippingAddress.street}</div>
              <div>
                {order.shippingAddress.zipCode} {order.shippingAddress.city}
              </div>
              <div>{order.shippingAddress.country}</div>
            </div>
          </Paper>
        </Grid>

        {/* Order Items */}
        <Grid item xs={12}>
          <Paper className="p-4">
            <Typography variant="h6" className="mb-3">
              Order Items
            </Typography>
            <TableContainer>
              <Table>
                <TableHead>
                  <TableRow>
                    <TableCell>Product</TableCell>
                    <TableCell align="right">Price</TableCell>
                    <TableCell align="right">Quantity</TableCell>
                    <TableCell align="right">Total</TableCell>
                  </TableRow>
                </TableHead>
                <TableBody>
                  {order.orderItems.map((item) => (
                    <TableRow key={item.id}>
                      <TableCell>{item.productName}</TableCell>
                      <TableCell align="right">{formatPrice(item.price)}</TableCell>
                      <TableCell align="right">{item.quantity}</TableCell>
                      <TableCell align="right">{formatPrice(item.price * item.quantity)}</TableCell>
                    </TableRow>
                  ))}
                  <TableRow>
                    <TableCell colSpan={3} align="right">
                      <strong>Total</strong>
                    </TableCell>
                    <TableCell align="right">
                      <strong>{formatPrice(order.totalPrice)}</strong>
                    </TableCell>
                  </TableRow>
                </TableBody>
              </Table>
            </TableContainer>
          </Paper>
        </Grid>
      </Grid>
    </div>
  );
};

export default OrderDetails;
