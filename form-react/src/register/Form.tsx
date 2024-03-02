import { useAtom, useAtomValue } from "jotai"
import React from "react"
import { FormState } from "./FormState"
import { Box, Stack, Typography, TextField, Button } from "@mui/material"

export const Form: React.FC = () => {
  const [name, nameDispatch] = useAtom(FormState.nameAtom)
  const [zip, zipDispatch] = useAtom(FormState.zipAtom)
  const [address, addressDispatch] = useAtom(FormState.addressAtom)
  const [mail, mailDispatch] = useAtom(FormState.mailAtom)
  const submittable = useAtomValue(FormState.submitButtonAtom)

  return (
    <Box p={2}>
      <form
        method="post"
        action=""
        onSubmit={(e) => {
          e.preventDefault()
          alert(
            "以下の情報を送信しました（してません）。\n" +
              `名前: ${name.value}\n` +
              `郵便番号: ${zip.value}\n` +
              `住所: ${address.value}` +
              `メールアドレス: ${mail.value}\n`,
          )
        }}
      >
        <Stack direction="column" spacing={2}>
          <Box>
            <Typography>ユーザー情報を入力してください。</Typography>
          </Box>
          <Box>
            <TextField
              fullWidth
              label="名前"
              variant="outlined"
              size="small"
              value={name.value}
              onChange={(e) =>
                nameDispatch({ type: "change", value: e.target.value })
              }
              error={!name.isValid}
              helperText={name.errorMessage}
            />
          </Box>
          <Box>
            <TextField
              fullWidth
              label="郵便番号"
              variant="outlined"
              size="small"
              value={zip.value}
              onChange={(e) =>
                zipDispatch({
                  type: "change",
                  value: e.target.value,
                })
              }
              onBlur={(e) =>
                zipDispatch({
                  type: "blur",
                  value: e.target.value,
                })
              }
              error={!zip.isValid}
              helperText={zip.errorMessage}
            />
          </Box>
          <Box>
            <TextField
              fullWidth
              label="住所"
              variant="outlined"
              size="small"
              value={address.value}
              onChange={(e) =>
                addressDispatch({ type: "change", value: e.target.value })
              }
              onBlur={(e) =>
                addressDispatch({ type: "blur", value: e.target.value })
              }
              error={!address.isValid}
              helperText={address.errorMessage}
            />
          </Box>
          <Box>
            <TextField
              fullWidth
              label="メールアドレス"
              variant="outlined"
              size="small"
              value={mail.value}
              onChange={(e) =>
                mailDispatch({ type: "change", value: e.target.value })
              }
              onBlur={(e) =>
                mailDispatch({ type: "blur", value: e.target.value })
              }
              error={!mail.isValid}
              helperText={mail.errorMessage}
            />
          </Box>

          <Box>
            <Button type="submit" variant="contained" disabled={!submittable}>
              送信
            </Button>
          </Box>
        </Stack>
      </form>
    </Box>
  )
}
