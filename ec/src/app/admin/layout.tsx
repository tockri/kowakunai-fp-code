import React from "react"
import Link from "next/link"

const AdminLayout: React.FC<React.PropsWithChildren> = ({ children }) => {
  return (
    <>
      <header>
        <Link href="/admin">管理者ページ</Link>
      </header>
      <section>{children}</section>
    </>
  )
}
export default AdminLayout
