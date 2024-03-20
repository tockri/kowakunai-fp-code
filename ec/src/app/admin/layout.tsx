import { Provider } from "jotai"
import Link from "next/link"
import React from "react"

const AdminLayout: React.FC<React.PropsWithChildren> = ({ children }) => {
  return (
    <Provider>
      <header>
        <Link href="/admin">管理者ページ</Link>
      </header>
      <section>{children}</section>
    </Provider>
  )
}
export default AdminLayout
