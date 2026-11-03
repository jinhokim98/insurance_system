import { useEffect, useState } from 'react'
import { Link, useOutletContext } from 'react-router-dom'
import { getContracts, type InsuranceContract } from '../api/customers'
import type { CustomerSelection } from './ProductLayout'
import styles from './Products.module.css'

function dateTime(value: string | null) {
  return value ? value.replace('T', ' ').slice(0, 16) : '미정'
}

function CustomerContracts({ customerId }: { customerId: number }) {
  const [contracts, setContracts] = useState<InsuranceContract[] | null>(null)
  const [error, setError] = useState(false)
  const [attempt, setAttempt] = useState(0)
  useEffect(() => {
    const controller = new AbortController()
    getContracts(customerId, controller.signal).then(data => {
      if (!controller.signal.aborted) setContracts(data)
    }).catch(() => { if (!controller.signal.aborted) setError(true) })
    return () => controller.abort()
  }, [customerId, attempt])

  if (error) return <section className={styles.state} role="alert">
    <h2>계약을 불러오지 못했습니다.</h2><p>잠시 후 다시 시도해 주세요.</p>
    <button className={styles.button} onClick={() => { setError(false); setAttempt(value => value + 1) }}>다시 시도</button>
  </section>
  if (contracts === null) return <p role="status" className={styles.loading}>계약을 불러오는 중입니다…</p>
  if (contracts.length === 0) return <section className={styles.state}><h2>가입한 계약이 없습니다.</h2><p>다른 고객을 선택하거나 보험 상품을 확인해 보세요.</p><Link to="/products">보험 상품 둘러보기</Link></section>

  return <>
    <p className={styles.count}>전체 {contracts.length}건</p>
    <ul className={styles.grid}>
      {contracts.map(contract => <li key={contract.id} className={styles.card}>
        <div className={styles.contractHeading}><h2>계약 #{contract.id}</h2><span className={styles.badge}>{contract.status === 'ACTIVE' ? '보장 중' : contract.status === 'PENDING_PAYMENT' ? '결제 대기' : contract.status}</span></div>
        <dl className={styles.facts}>
          <div><dt>차량 번호</dt><dd>{contract.vehicleNumber || '미등록'}</dd></div>
          <div><dt>보장 한도</dt><dd>{contract.coverageLimit.toLocaleString('ko-KR')}원</dd></div>
          <div><dt>보험료 (고정)</dt><dd>{contract.premium.toLocaleString('ko-KR')}원</dd></div>
          <div><dt>계약 일시</dt><dd>{dateTime(contract.createdAt)}</dd></div>
          <div><dt>보장 시작</dt><dd>{dateTime(contract.coverageStartsAt)}</dd></div>
          <div><dt>보장 종료</dt><dd>{dateTime(contract.coverageEndsAt)}</dd></div>
        </dl>
        <Link className={styles.button} to={`/products/${contract.productId}`}>가입 상품 보기 · #{contract.productId}</Link>
      </li>)}
    </ul>
  </>
}

export default function ContractsPage() {
  const { customerId, customerName } = useOutletContext<CustomerSelection>()
  return <>
    <div className={styles.intro}><h1>내 계약</h1><p>{customerName}님의 보험 계약과 보장 내용을 확인하세요.</p></div>
    <CustomerContracts key={customerId} customerId={customerId} />
  </>
}
