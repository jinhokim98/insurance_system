import { isRouteErrorResponse, Link, useRevalidator, useRouteError } from 'react-router-dom'
import styles from './Products.module.css'

export default function ProductErrorPage() {
  const error = useRouteError()
  const revalidator = useRevalidator()
  const notFound = !error || (isRouteErrorResponse(error) && error.status === 404)

  return (
    <section role="alert" className={styles.state}>
      <h1>{notFound ? '요청하신 상품 또는 페이지를 찾을 수 없습니다.' : '상품을 불러오지 못했습니다.'}</h1>
      <p>{notFound ? '주소를 확인하거나 상품 목록에서 다시 선택해 주세요.' : '잠시 후 다시 시도해 주세요.'}</p>
      <div className={styles.actions}>
        {!notFound && <button className={styles.button} onClick={() => void revalidator.revalidate()} disabled={revalidator.state === 'loading'}>{revalidator.state === 'loading' ? '불러오는 중…' : '다시 시도'}</button>}
        <Link to="/products" className={styles.back}>상품 목록으로</Link>
      </div>
    </section>
  )
}
