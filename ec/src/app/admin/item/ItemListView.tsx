"use client"

import { useAtomValue } from "jotai"
import React from "react"
import { ItemState } from "./state/ItemState"

export const ItemListView: React.FC = () => {
  const list = useAtomValue(ItemState.listAtom)
  return (
    <div>
      <ul>
        {list.map((item) => (
          <li>
            {item.id} | {item.name} <div>{item.description}</div>
          </li>
        ))}
      </ul>
    </div>
  )
}
