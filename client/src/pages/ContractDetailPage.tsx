import { Link, useLoaderData } from 'react-router-dom'
import type { getContract } from '../api/contracts'
import ContractPayment from './ContractPayment'
import styles from './Products.module.css'

const dateTime = (value: string | null) => value ? value.replace('T', ' ').slice(0, 16) : '미정'

export default function ContractDetailPage() {
  const contract = useLoaderData<typeof getContract>()
  return <>
    <Link to="/contracts" className={styles.back}>← 내 계약 목록</Link>
    <article className={styles.detail}>
      <p className={styles.eyebrow}>보험 계약 상세</p>
      <h1>계약 #{contract.id}</h1>
      <span className={styles.badge}>{contract.status === 'PENDING_PAYMENT' ? '납부 대기' : contract.status === 'ACTIVE' ? '보장 중' : contract.status}</span>
      {contract.status === 'PENDING_PAYMENT' && <p>가입 신청이 완료되었습니다. 보험료 납부 대기 상태입니다.</p>}
      <dl className={styles.highlights}>
        <div><dt>차량번호</dt><dd>{contract.vehicleNumber || '미등록'}</dd></div>
        <div><dt>보험료 (고정)</dt><dd>{contract.premium.toLocaleString('ko-KR')}원</dd></div>
        <div><dt>보장 한도</dt><dd>{contract.coverageLimit.toLocaleString('ko-KR')}원</dd></div>
      </dl>
      <dl className={styles.facts}>
        <div><dt>가입 고객</dt><dd>고객 #{contract.customerId}</dd></div>
        <div><dt>계약 일시</dt><dd>{dateTime(contract.createdAt)}</dd></div>
        <div><dt>보장 시작</dt><dd>{dateTime(contract.coverageStartsAt)}</dd></div>
        <div><dt>보장 종료</dt><dd>{dateTime(contract.coverageEndsAt)}</dd></div>
      </dl>
      <Link to={`/products/${contract.productId}`}>가입 상품 보기</Link>
      <ContractPayment key={contract.id} contract={contract} />
    </article>
  </>
}
