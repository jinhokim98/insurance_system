import { Link, useLoaderData } from 'react-router-dom'
import type { getProducts } from '../api/products'
import styles from './Products.module.css'

export default function ProductListPage() {
  const products = useLoaderData<typeof getProducts>()

  return (
    <>
      <div className={styles.intro}>
        <h1>보험 상품 목록</h1>
        <p>상품별 보장 내용과 보험료를 확인해 보세요.</p>
      </div>
      <p className={styles.count}>전체 {products.length.toLocaleString('ko-KR')}개 상품</p>
      {products.length === 0 ? (
        <div className={styles.state}><h2>등록된 보험 상품이 없습니다.</h2><p>상품이 등록되면 이곳에서 확인할 수 있습니다.</p></div>
      ) : (
        <ul className={styles.grid}>
          {products.map((product) => (
            <li key={product.id} className={styles.card}>
              <h2><Link to={`/products/${product.id}`}>{product.name}</Link></h2>
              <p className={styles.summary}>{product.description || '등록된 상품 설명이 없습니다.'}</p>
              <dl className={styles.facts}>
                <div><dt>보장 한도</dt><dd>{product.coverageLimit.toLocaleString('ko-KR')}원</dd></div>
                <div><dt>보험료 (고정)</dt><dd>{product.premium.toLocaleString('ko-KR')}원</dd></div>
                <div><dt>보장 기간</dt><dd>{product.coveragePeriodDays.toLocaleString('ko-KR')}일</dd></div>
              </dl>
              <Link className={styles.button} to={`/products/${product.id}`} aria-label={`${product.name} 상세 보기`}>상세 보기 →</Link>
            </li>
          ))}
        </ul>
      )}
    </>
  )
}
