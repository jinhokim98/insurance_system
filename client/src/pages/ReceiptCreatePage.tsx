import { useEffect, useRef, useState, type FormEvent } from 'react'
import axios from 'axios'
import { Link, useNavigate, useOutletContext } from 'react-router-dom'
import { getContracts, type InsuranceContract } from '../api/customers'
import { createReceipt, receiptTime } from '../api/receipts'
import type { CustomerSelection } from './ProductLayout'
import styles from './Products.module.css'

const seoulNow = () => {
  const parts = new Intl.DateTimeFormat('sv-SE', { timeZone: 'Asia/Seoul', year: 'numeric', month: '2-digit', day: '2-digit', hour: '2-digit', minute: '2-digit', second: '2-digit', hourCycle: 'h23' }).format(new Date())
  return parts.replace(' ', 'T')
}
function ReceiptForm({ customerId }: { customerId: number }) {
  const navigate = useNavigate()
  const [contracts, setContracts] = useState<InsuranceContract[] | null>(null)
  const [loadError, setLoadError] = useState(false)
  const [attempt, setAttempt] = useState(0)
  const [contractId, setContractId] = useState('')
  const [crashTime, setCrashTime] = useState('')
  const [crashPlace, setCrashPlace] = useState('')
  const [crashDetail, setCrashDetail] = useState('')
  const [billAmount, setBillAmount] = useState('')
  const [pending, setPending] = useState(false)
  const [error, setError] = useState('')
  const submitting = useRef(false)
  const submission = useRef<AbortController | null>(null)
  useEffect(() => () => submission.current?.abort(), [])
  useEffect(() => {
    const controller = new AbortController()
    getContracts(customerId, controller.signal).then(data => {
      if (!controller.signal.aborted) setContracts(data.filter(contract => contract.customerId === customerId && contract.status === 'ACTIVE'))
    }).catch(() => { if (!controller.signal.aborted) setLoadError(true) })
    return () => controller.abort()
  }, [customerId, attempt])
  const contract = contracts?.find(value => value.id === Number(contractId))
  async function submit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    if (submitting.current) return
    if (!contract || contract.customerId !== customerId) { setError('선택한 고객의 납부 완료 계약을 선택해 주세요.'); return }
    const time = crashTime.length === 16 ? `${crashTime}:00` : crashTime
    if (!time || time > seoulNow()) { setError('사고 시각을 현재 이전으로 입력해 주세요.'); return }
    if (!contract.coverageStartsAt || !contract.coverageEndsAt || time < contract.coverageStartsAt || time > contract.coverageEndsAt) { setError('사고 시각은 계약의 보장 기간 안에 있어야 합니다.'); return }
    if (!crashPlace.trim() || !crashDetail.trim()) { setError('사고 장소와 경위를 입력해 주세요.'); return }
    const amount = billAmount.trim() === '' ? null : Number(billAmount)
    if (amount !== null && (!Number.isSafeInteger(amount) || amount < 0)) { setError('예상 금액은 0 이상의 정수로 입력해 주세요.'); return }
    submitting.current = true
    setPending(true)
    setError('')
    const controller = new AbortController()
    submission.current = controller
    try {
      const receipt = await createReceipt({ customerId, contractId: contract.id, crashTime: time, crashPlace: crashPlace.trim(), crashDetail: crashDetail.trim(), billAmount: amount }, controller.signal)
      if (!controller.signal.aborted) await navigate(`/receipts/${receipt.id}`)
    } catch (cause) {
      if (controller.signal.aborted) return
      const status = axios.isAxiosError(cause) ? cause.response?.status : undefined
      setError(status === 400 ? '입력값과 계약의 고객·보장 기간을 확인해 주세요.' : status === 404 ? '계약을 찾을 수 없습니다.' : status === 409 ? '보험료 납부가 완료된 계약만 접수할 수 있습니다.' : '저장 결과를 확인하지 못했습니다. 내 접수 목록에서 생성 여부를 확인한 후 다시 시도해 주세요.')
    } finally {
      submitting.current = false
      if (!controller.signal.aborted) setPending(false)
    }
  }
  if (loadError) return <div role="alert"><p>계약을 불러오지 못했습니다.</p><button onClick={() => { setLoadError(false); setAttempt(value => value + 1) }}>다시 시도</button></div>
  if (contracts === null) return <p role="status">계약을 불러오는 중입니다…</p>
  if (!contracts.length) return <section className={styles.state}><p>접수 가능한 납부 완료 계약이 없습니다.</p><Link to="/contracts">내 계약 확인</Link></section>
  return <form onSubmit={submit} aria-busy={pending}>
    <fieldset disabled={pending} className={styles.formFields}>
      <label htmlFor="receipt-contract">계약</label>
      <select id="receipt-contract" required value={contractId} onChange={event => setContractId(event.target.value)}><option value="">계약을 선택해 주세요</option>{contracts.map(value => <option key={value.id} value={value.id}>계약 #{value.id} · {value.vehicleNumber || '차량 미등록'}</option>)}</select>
      {contract && <p>보장 기간 (한국 시간): {contract.coverageStartsAt ? receiptTime(contract.coverageStartsAt) : '미정'} ~ {contract.coverageEndsAt ? receiptTime(contract.coverageEndsAt) : '미정'}</p>}
      <label htmlFor="crash-time">사고 시각 (한국 시간)</label>
      <input id="crash-time" type="datetime-local" step="1" required value={crashTime} onChange={event => setCrashTime(event.target.value)} />
      <label htmlFor="crash-place">사고 장소</label>
      <input id="crash-place" required value={crashPlace} onChange={event => setCrashPlace(event.target.value)} />
      <label htmlFor="crash-detail">사고 경위</label>
      <textarea id="crash-detail" required rows={6} value={crashDetail} onChange={event => setCrashDetail(event.target.value)} />
      <label htmlFor="bill-amount">예상 금액 (원, 선택)</label>
      <input id="bill-amount" type="number" min="0" max={Number.MAX_SAFE_INTEGER} step="1" aria-describedby="amount-help" value={billAmount} onChange={event => setBillAmount(event.target.value)} />
      <p id="amount-help">아직 모르면 비워 두세요. 0원과 구분해서 저장합니다.</p>
      {error && <p role="alert" className={styles.formError}>{error}</p>}
      <button type="submit" className={styles.button}>{pending ? '저장 중…' : '접수 저장'}</button>
    </fieldset>
  </form>
}
export default function ReceiptCreatePage() {
  const { customerId, customerName } = useOutletContext<CustomerSelection>()
  return <><Link className={styles.back} to="/receipts">← 내 접수 목록</Link><article className={styles.detail}><h1>접수 작성</h1><p>{customerName}님의 사고 정보를 입력해 주세요.</p><ReceiptForm key={customerId} customerId={customerId} /></article></>
}
