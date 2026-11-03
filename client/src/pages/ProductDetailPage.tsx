import { Link, useLoaderData } from 'react-router-dom'
import type { getProduct } from '../api/products'
import styles from './Products.module.css'

export default function ProductDetailPage() {
  const product = useLoaderData<typeof getProduct>()

  return (
    <>
      <Link to="/products" className={styles.back}>← 상품 목록으로</Link>
      <article className={styles.detail}>
        <p className={styles.eyebrow}>보험 상품 상세</p>
        <h1>{product.name}</h1>
        <dl className={styles.highlights}>
          <div><dt>보장 한도</dt><dd>{product.coverageLimit.toLocaleString('ko-KR')}원</dd></div>
          <div><dt>보험료 (고정)</dt><dd>{product.premium.toLocaleString('ko-KR')}원</dd></div>
          <div><dt>보장 기간</dt><dd>{product.coveragePeriodDays.toLocaleString('ko-KR')}일</dd></div>
        </dl>
        <section className={styles.description}>
          <h2>상품 설명</h2>
          <p>{product.description || '등록된 상품 설명이 없습니다.'}</p>
        </section>
      </article>
    </>
  )
}
