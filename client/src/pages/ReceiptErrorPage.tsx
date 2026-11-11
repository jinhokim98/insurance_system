import {
  isRouteErrorResponse,
  Link,
  useRevalidator,
  useRouteError,
} from "react-router-dom";
import styles from "./Products.module.css";

export default function ReceiptErrorPage() {
  const error = useRouteError();
  const revalidator = useRevalidator();
  const notFound = isRouteErrorResponse(error) && error.status === 404;
  return (
    <section className={styles.state} role="alert">
      <h1>
        {notFound
          ? "접수을 찾을 수 없습니다."
          : "접수 정보를 불러오지 못했습니다."}
      </h1>
      <p>
        {notFound
          ? "접수 번호를 확인해 주세요."
          : "접수 직후라면 다시 접수하지 말고 조회를 다시 시도해 주세요."}
      </p>
      <div className={styles.actions}>
        {!notFound && (
          <button
            className={styles.button}
            disabled={revalidator.state === "loading"}
            onClick={() => void revalidator.revalidate()}
          >
            {revalidator.state === "loading" ? "조회 중…" : "다시 조회"}
          </button>
        )}
        <Link to="/receipts">내 접수 목록</Link>
      </div>
    </section>
  );
}
