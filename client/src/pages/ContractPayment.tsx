import { useRef, useState } from 'react'
import axios from 'axios'
import { useRevalidator } from 'react-router-dom'
import { payContract, type PremiumPayment } from '../api/contracts'
import type { InsuranceContract } from '../api/customers'
import styles from './Products.module.css'

export default function ContractPayment({ contract }: { contract: InsuranceContract }) {
  const revalidator = useRevalidator()
  const [payment, setPayment] = useState<PremiumPayment | null>(null)
  const [pending, setPending] = useState(false)
  const [error, setError] = useState('')
  const submitting = useRef(false)

  async function pay() {
    if (submitting.current || payment || contract.status !== 'PENDING_PAYMENT') return
    submitting.current = true
    setPending(true)
    setError('')
    try {
      const result = await payContract(contract.id)
      setPayment(result)
      void revalidator.revalidate()
    } catch (error) {
      const status = axios.isAxiosError(error) ? error.response?.status : undefined
      setError(status === 404 ? '계약을 찾을 수 없습니다. 계약 목록을 확인해 주세요.'
        : status === 409 ? '계약 상태가 변경되어 납부할 수 없습니다. 계약 상태를 다시 조회해 주세요.'
        : status === 400 ? '납부 요청이 올바르지 않습니다. 계약 정보를 확인해 주세요.'
        : '납부 결과를 확인하지 못했습니다. 계약 상태를 다시 조회한 후 시도해 주세요.')
    } finally {
      submitting.current = false
      setPending(false)
    }
  }

  return <section className={styles.payment} aria-labelledby="payment-heading" aria-busy={pending}>
    <h2 id="payment-heading">보험료 납부</h2>
    {payment ? <div role="status">
      <p className={styles.paymentSuccess}>보험료 납부가 완료되었습니다.</p>
      <dl className={styles.facts}>
        <div><dt>납부 번호</dt><dd>#{payment.id}</dd></div>
        <div><dt>납부 금액</dt><dd>{payment.amount.toLocaleString('ko-KR')}원</dd></div>
        <div><dt>납부 방식</dt><dd>{payment.paymentMethod === 'MOCK' ? '모의 납부' : payment.paymentMethod}</dd></div>
        <div><dt>납부 일시</dt><dd>{payment.paidAt.replace('T', ' ').slice(0, 19)}</dd></div>
      </dl>
    </div> : contract.status === 'ACTIVE' ? <p className={styles.paymentSuccess}>보험료 납부가 완료된 계약입니다.</p>
      : contract.status === 'PENDING_PAYMENT' ? <>
        <p>납부할 보험료: <strong>{contract.premium.toLocaleString('ko-KR')}원</strong></p>
        <p>모의 납부로 처리되며 실제 결제는 발생하지 않습니다.</p>
        <button type="button" className={styles.button} disabled={pending || revalidator.state === 'loading'} onClick={() => void pay()}>
          {pending ? '납부 처리 중…' : '보험료 모의 납부하기'}
        </button>
      </> : <p>납부 가능한 상태가 아닙니다.</p>}
    {error && <div role="alert" className={styles.formError}>
      <p>{error}</p>
      <button type="button" className={styles.button} disabled={pending || revalidator.state === 'loading'} onClick={() => void revalidator.revalidate()}>
        {revalidator.state === 'loading' ? '조회 중…' : '계약 상태 다시 조회'}
      </button>
    </div>}
  </section>
}
