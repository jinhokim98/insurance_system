import axios from 'axios'
import type { InsuranceContract } from './customers'

const api = axios.create({ baseURL: '/api', timeout: 10000 })
export interface PremiumPayment {
  id: number
  contractId: number
  amount: number
  paymentMethod: string
  paidAt: string
}

export async function payContract(contractId: number) {
  const { data } = await api.post<PremiumPayment>(`/contracts/${contractId}/mock-payment`)
  return data
}

export interface CreateContractRequest {
  customerId: number
  productId: number
  vehicleNumber: string
  coverageStartDate: string
}
export async function createContract(request: CreateContractRequest) {
  const { data } = await api.post<number>('/contract', request)
  return data
}
export async function getContract(id: string, signal: AbortSignal) {
  try {
    const { data } = await api.get<InsuranceContract>(`/contracts/${id}`, { signal })
    return data
  } catch (error) {
    if (axios.isAxiosError(error) && error.response?.status === 404) {
      throw new Response('계약을 찾을 수 없습니다.', { status: 404 })
    }
    throw error
  }
}
