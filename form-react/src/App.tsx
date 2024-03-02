import { Provider } from "jotai"
import React from "react"

import { Form } from "./register/Form"

const App: React.FC = () => {
  return (
    <Provider>
      <Form />
    </Provider>
  )
}

export default App
