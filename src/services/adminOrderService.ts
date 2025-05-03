import { OrderDTO, OrderSearchCriteria } from '../types/order';
import { PaginatedResponse } from '../types/common';
import api from './api';

export const adminOrderService = {
  getAllOrders: async (
    page: number = 0,
    size: number = 10,
    sort: string = 'id:desc',
    criteria?: Partial<OrderSearchCriteria>
  ): Promise<PaginatedResponse<OrderDTO>> => {
    const params = new URLSearchParams({
      page: page.toString(),
      size: size.toString(),
      sort: sort,
    });

    // Add search criteria to params if they exist
    if (criteria) {
      if (criteria.search) params.append('search', criteria.search);
      if (criteria.status) params.append('status', criteria.status);
      if (criteria.minTotal) params.append('minTotal', criteria.minTotal.toString());
      if (criteria.maxTotal) params.append('maxTotal', criteria.maxTotal.toString());
      if (criteria.startDate) params.append('startDate', criteria.startDate.toISOString());
      if (criteria.endDate) params.append('endDate', criteria.endDate.toISOString());
      if (criteria.userId) params.append('userId', criteria.userId.toString());
      if (criteria.paymentMethod) params.append('paymentMethod', criteria.paymentMethod);
      if (criteria.hasTransactionId !== undefined) {
        params.append('hasTransactionId', criteria.hasTransactionId.toString());
      }
    }

    try {
      console.log('Fetching orders with params:', params.toString());
      const response = await api.get(`/api/v1/admin/orders?${params}`);
      console.log('Orders response:', response.data);
      return response.data;
    } catch (error) {
      console.error('Error fetching orders:', error);
      throw error;
    }
  },

  getOrderById: async (orderId: number): Promise<OrderDTO> => {
    try {
      const response = await api.get(`/api/v1/admin/orders/${orderId}`);
      return response.data;
    } catch (error) {
      console.error(`Error fetching order ${orderId}:`, error);
      throw error;
    }
  },
};
