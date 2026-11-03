import axios from "axios";

export interface InsuranceProduct {
  id: number;
  name: string;
  description: string | null;
  coverageLimit: number;
  premium: number;
  coveragePeriodDays: number;
}

// Vite proxies /api requests to http://localhost:8080 during development.
const api = axios.create({ baseURL: "/api", timeout: 10000 });

export async function getProducts(signal: AbortSignal) {
  const { data } = await api.get<InsuranceProduct[]>("/product", { signal });
  return data;
}

export async function getProduct(id: string, signal: AbortSignal) {
  try {
    const { data } = await api.get<InsuranceProduct>(`/product/${id}`, {
      signal,
    });
    return data;
  } catch (error) {
    if (axios.isAxiosError(error) && error.response?.status === 404) {
      throw new Response("상품을 찾을 수 없습니다.", { status: 404 });
    }
    throw error;
  }
}
