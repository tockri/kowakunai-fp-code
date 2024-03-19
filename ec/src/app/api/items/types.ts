export type Item = {
  id: number
  name: string
  description: string
  price: number | null
  createdAt: Date
  updatedAt: Date
}

export type Photo = {
  id: number
  itemId: number
  mimeType: string
  width: number
  height: number
}
