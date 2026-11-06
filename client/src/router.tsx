import { createBrowserRouter, redirect } from 'react-router-dom'
import { getProduct, getProducts } from './api/products'
import { getContract } from './api/contracts'
import ContractDetailPage from './pages/ContractDetailPage'
import ContractErrorPage from './pages/ContractErrorPage'
import ContractsPage from './pages/ContractsPage'
import ProductLayout from './pages/ProductLayout'
import ProductListPage from './pages/ProductListPage'
import ProductDetailPage from './pages/ProductDetailPage'
import ProductErrorPage from './pages/ProductErrorPage'

export const router = createBrowserRouter([
  {
    path: '/',
    Component: ProductLayout,
    children: [
      { index: true, loader: () => redirect('/products') },
      {
        path: 'products',
        Component: ProductListPage,
        loader: ({ request }) => getProducts(request.signal),
        ErrorBoundary: ProductErrorPage,
        HydrateFallback: () => <p role="status">상품을 불러오는 중입니다…</p>,
      },
      {
        path: 'products/:productId',
        Component: ProductDetailPage,
        loader: ({ params, request }) => {
          const id = params.productId ?? ''
          if (!/^[1-9]\d*$/.test(id)) {
            throw new Response('상품을 찾을 수 없습니다.', { status: 404 })
          }
          return getProduct(id, request.signal)
        },
        ErrorBoundary: ProductErrorPage,
        HydrateFallback: () => <p role="status">상품을 불러오는 중입니다…</p>,
      },
      {
        path: 'contracts/:contractId',
        Component: ContractDetailPage,
        loader: ({ params, request }) => {
          const id = params.contractId ?? ''
          if (!/^[1-9]\d*$/.test(id)) throw new Response('계약을 찾을 수 없습니다.', { status: 404 })
          return getContract(id, request.signal)
        },
        ErrorBoundary: ContractErrorPage,
        HydrateFallback: () => <p role="status">계약을 불러오는 중입니다…</p>,
      },
      { path: 'contracts', Component: ContractsPage },
      { path: '*', Component: ProductErrorPage },
    ],
  },
])
