import { atomFamily } from "jotai/utils"
import { atom } from "jotai"
import { ItemDao } from "@prisma/client"

const listAtom = atom(async () => {
  const res = await fetch("/admin/item/api")
  const list: readonly ItemDao[] = await res.json()
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
