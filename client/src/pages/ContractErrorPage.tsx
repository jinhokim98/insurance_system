import { isRouteErrorResponse, Link, useRevalidator, useRouteError } from 'react-router-dom'
import styles from './Products.module.css'

export default function ContractErrorPage() {
  const error = useRouteError()
  const revalidator = useRevalidator()
  const notFound = isRouteErrorResponse(error) && error.status === 404
  return <section className={styles.state} role="alert">
    <h1>{notFound ? '계약을 찾을 수 없습니다.' : '계약 정보를 불러오지 못했습니다.'}</h1>
    <p>{notFound ? '계약 번호를 확인해 주세요.' : '가입 직후라면 다시 가입하지 말고 조회를 다시 시도해 주세요.'}</p>
    <div className={styles.actions}>
      {!notFound && <button className={styles.button} disabled={revalidator.state === 'loading'} onClick={() => void revalidator.revalidate()}>{revalidator.state === 'loading' ? '조회 중…' : '다시 조회'}</button>}
      <Link to="/contracts">내 계약 목록</Link>
    </div>
  </section>
}
