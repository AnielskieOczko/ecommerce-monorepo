import { AddressDTO } from './common';
import { CartDTO } from './cart';
import { UserResponseDTO } from './user';

export enum OrderStatus {
  PENDING = 'PENDING',
  CONFIRMED = 'CONFIRMED',
  PROCESSING = 'PROCESSING',
  SHIPPED = 'SHIPPED',
  DELIVERED = 'DELIVERED',
  CANCELLED = 'CANCELLED',
  REFUNDED = 'REFUNDED',
  FAILED = 'FAILED',
}

export enum PaymentMethod {
  CREDIT_CARD = 'CREDIT_CARD',
  PAYPAL = 'PAYPAL',
  BANK_TRANSFER = 'BANK_TRANSFER',
  BLIK = 'BLIK',
}

export enum ShippingMethod {
  INPOST = 'INPOST',
  DHL = 'DHL',
}

export interface OrderItemDTO {
  id: number;
  orderId: number;
  productId: number;
  productName: string;
  quantity: number;
  price: number;
}

export interface ShippingAddressDTO {
  street: string;
  city: string;
  zipCode: string;
  country: string;
}

export interface OrderDTO {
  id: number;
  userId: number;
  orderItems: OrderItemDTO[];
  totalPrice: number;
  shippingAddress: ShippingAddressDTO;
  shippingMethod: ShippingMethod;
  paymentMethod: PaymentMethod;
  paymentTransactionId?: string;
  orderDate: string;
  orderStatus: OrderStatus;
  createdAt: string;
  updatedAt: string;
}

export interface OrderCreationRequest {
  shippingAddress: ShippingAddressDTO;
  paymentMethod: PaymentMethod;
  cart: CartDTO;
}

export interface OrderSearchCriteria {
  search?: string;
  status?: OrderStatus;
  minTotal?: number;
  maxTotal?: number;
  startDate?: Date;
  endDate?: Date;
  userId?: number;
  paymentMethod?: PaymentMethod;
  hasTransactionId?: boolean;
}

export interface StatusUpdateRequest {
  newStatus: OrderStatus;
}
