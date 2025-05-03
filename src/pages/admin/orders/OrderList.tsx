import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import {
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Paper,
  TablePagination,
  TextField,
  MenuItem,
  FormControl,
  InputLabel,
  Select,
  Alert,
  IconButton,
} from '@mui/material';
import { adminOrderService } from '../../../services/adminOrderService';
import { OrderDTO, OrderStatus, PaymentMethod, OrderSearchCriteria } from '../../../types/order';
import { PaginatedResponse } from '../../../types/common';
import VisibilityIcon from '@mui/icons-material/Visibility';

const OrderList: React.FC = () => {
  const navigate = useNavigate();
  const [orders, setOrders] = useState<OrderDTO[]>([]);
  const [page, setPage] = useState(0);
  const [rowsPerPage, setRowsPerPage] = useState(10);
  const [totalElements, setTotalElements] = useState(0);
  const [searchTerm, setSearchTerm] = useState('');
  const [selectedStatus, setSelectedStatus] = useState<OrderStatus | ''>('');
  const [selectedPaymentMethod, setSelectedPaymentMethod] = useState<PaymentMethod | ''>('');
  const [error, setError] = useState<string | null>(null);

  const fetchOrders = async () => {
    try {
      setError(null);
      const criteria: Partial<OrderSearchCriteria> = {
        search: searchTerm || undefined,
        status: selectedStatus || undefined,
        paymentMethod: selectedPaymentMethod || undefined,
      };

      const response = await adminOrderService.getAllOrders(page, rowsPerPage, 'id:desc', criteria);
      setOrders(response.content);
      setTotalElements(response.totalElements);
    } catch (error) {
      console.error('Error fetching orders:', error);
      setError('Failed to fetch orders. Please try again later.');
    }
  };

  useEffect(() => {
    fetchOrders();
  }, [page, rowsPerPage, searchTerm, selectedStatus, selectedPaymentMethod]);

  const handleChangePage = (event: unknown, newPage: number) => {
    setPage(newPage);
  };

  const handleChangeRowsPerPage = (event: React.ChangeEvent<HTMLInputElement>) => {
    setRowsPerPage(parseInt(event.target.value, 10));
    setPage(0);
  };

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
        return 'text-yellow-600';
      case OrderStatus.CONFIRMED:
        return 'text-blue-400';
      case OrderStatus.PROCESSING:
        return 'text-blue-600';
      case OrderStatus.SHIPPED:
        return 'text-purple-600';
      case OrderStatus.DELIVERED:
        return 'text-green-600';
      case OrderStatus.CANCELLED:
        return 'text-red-600';
      case OrderStatus.REFUNDED:
        return 'text-gray-600';
      case OrderStatus.FAILED:
        return 'text-red-800';
      default:
        return '';
    }
  };

  const handleViewOrder = (orderId: number) => {
    navigate(`/admin/orders/${orderId}`);
  };

  return (
    <div className="p-6">
      <h1 className="text-2xl font-semibold mb-6">Orders</h1>

      {error && (
        <Alert severity="error" className="mb-4">
          {error}
        </Alert>
      )}

      <div className="grid grid-cols-1 md:grid-cols-3 gap-4 mb-6">
        <TextField
          label="Search Orders"
          variant="outlined"
          value={searchTerm}
          onChange={(e) => setSearchTerm(e.target.value)}
          fullWidth
        />

        <FormControl fullWidth>
          <InputLabel>Status</InputLabel>
          <Select
            value={selectedStatus}
            label="Status"
            onChange={(e) => setSelectedStatus(e.target.value as OrderStatus)}
          >
            <MenuItem value="">All</MenuItem>
            {Object.values(OrderStatus).map((status) => (
              <MenuItem key={status} value={status}>
                {status}
              </MenuItem>
            ))}
          </Select>
        </FormControl>

        <FormControl fullWidth>
          <InputLabel>Payment Method</InputLabel>
          <Select
            value={selectedPaymentMethod}
            label="Payment Method"
            onChange={(e) => setSelectedPaymentMethod(e.target.value as PaymentMethod)}
          >
            <MenuItem value="">All</MenuItem>
            {Object.values(PaymentMethod).map((method) => (
              <MenuItem key={method} value={method}>
                {method.replace('_', ' ')}
              </MenuItem>
            ))}
          </Select>
        </FormControl>
      </div>

      <TableContainer component={Paper}>
        <Table>
          <TableHead>
            <TableRow>
              <TableCell>Order ID</TableCell>
              <TableCell>Date</TableCell>
              <TableCell>User ID</TableCell>
              <TableCell>Status</TableCell>
              <TableCell>Payment Method</TableCell>
              <TableCell>Total</TableCell>
              <TableCell align="right">Actions</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {orders.map((order) => (
              <TableRow key={order.id} hover>
                <TableCell>{order.id}</TableCell>
                <TableCell>{formatDate(order.orderDate || order.createdAt)}</TableCell>
                <TableCell>{order.userId}</TableCell>
                <TableCell>
                  <span className={getStatusColor(order.orderStatus)}>{order.orderStatus}</span>
                </TableCell>
                <TableCell>{order.paymentMethod.replace('_', ' ')}</TableCell>
                <TableCell>{formatPrice(order.totalPrice)}</TableCell>
                <TableCell align="right">
                  <IconButton
                    onClick={() => handleViewOrder(order.id)}
                    size="small"
                    title="View Order Details"
                  >
                    <VisibilityIcon />
                  </IconButton>
                </TableCell>
              </TableRow>
            ))}
            {orders.length === 0 && (
              <TableRow>
                <TableCell colSpan={7} align="center">
                  No orders found
                </TableCell>
              </TableRow>
            )}
          </TableBody>
        </Table>
        <TablePagination
          rowsPerPageOptions={[5, 10, 25]}
          component="div"
          count={totalElements}
          rowsPerPage={rowsPerPage}
          page={page}
          onPageChange={handleChangePage}
          onRowsPerPageChange={handleChangeRowsPerPage}
        />
      </TableContainer>
    </div>
  );
};

export default OrderList;
