import axios from 'axios'

const api = axios.create({ baseURL: '/api', timeout: 10000 })
export interface Receipt {
  id: number
  contractId: number
  crashTime: string
  crashPlace: string
  crashDetail: string
  billAmount: number | null
}
export type ReceiptRequest = Omit<Receipt, 'id'> & { customerId: number }
export async function getReceipts(customerId: number, signal: AbortSignal) {
  const { data } = await api.get<Receipt[]>('/receipt', { params: { customerId }, signal })
  return data
}
export async function getReceipt(id: string, signal: AbortSignal) {
  try {
    const { data } = await api.get<Receipt>(`/receipt/${id}`, { signal })
    return data
  } catch (error) {
    if (axios.isAxiosError(error) && error.response?.status === 404) {
      throw new Response('접수를 찾을 수 없습니다.', { status: 404 })
    }
    throw error
  }
}
export async function createReceipt(request: ReceiptRequest, signal: AbortSignal) {
  const { data } = await api.post<Receipt>('/receipt', request, { signal })
  return data
}
export const receiptTime = (value: string) => value.replace('T', ' ')
export const receiptAmount = (value: number | null) => value === null ? '아직 모름' : `${value.toLocaleString('ko-KR')}원`
