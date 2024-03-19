import React from "react"
import { ItemListView } from "./ItemListView"

const ItemPage: React.FC = () => {
  return (
    <div>
      <React.Suspense fallback="...">
        <ItemListView />
      </React.Suspense>
    </div>
  )
}
export default ItemPage
