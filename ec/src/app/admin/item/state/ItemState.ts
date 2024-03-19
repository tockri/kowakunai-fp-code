import { atom } from "jotai"
import { atomFamily } from "jotai/utils"
import { Item } from "../../../api/items/types"

const listAtom = atom(async () => {
  const res = await fetch("/api/items")
  const list: readonly Item[] = await res.json()
  return list
})

const itemAtom = atomFamily((id: number) =>
  atom(async (get) => {
    const list = await get(listAtom)
    return list.find((l) => l.id === id)
  })
)

export const ItemState = {
  listAtom,
  itemAtom
}
