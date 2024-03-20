export type Item = Readonly<{
  id: number
  name: string
  description: string
  price: number | null
  createdAt: Date
  updatedAt: Date
}>

export type Photo = Readonly<{
  id: number
  itemId: number
  mimeType: string
  width: number
  height: number
}>
