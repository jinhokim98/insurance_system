import axios from 'axios'

const api = axios.create({ baseURL: '/api', timeout: 10000 })
export interface Customer { id: number; name: string }
export interface InsuranceContract {
  id: number
  customerId: number
  productId: number
  createdAt: string | null
  coverageStartsAt: string | null
  coverageEndsAt: string | null
  vehicleNumber: string | null
  coverageLimit: number
  premium: number
  status: 'PENDING_PAYMENT' | 'ACTIVE'
}
export async function getCustomers(signal: AbortSignal) {
  const { data } = await api.get<Customer[]>('/customer', { signal })
  return data.filter(customer => [1, 2, 3].includes(customer.id))
}
export async function getContracts(customerId: number, signal: AbortSignal) {
  const { data } = await api.get<InsuranceContract[]>(`/customer/${customerId}/contracts`, { signal })
  return data
}
