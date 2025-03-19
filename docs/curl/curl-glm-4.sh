curl -X POST \
        -H "Authorization: Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiIsInNpZ25fdHlwZSI6IlNJR04ifQ.eyJhcGlfa2V5IjoiNzNjZmU5NjMwOTNlNGNkNmI0ODFlYjFhMjBmY2Y1Y2MiLCJleHAiOjE3NDIzNTMxMTYzMjAsInRpbWVzdGFtcCI6MTc0MjM1MTMxNjMyNn0.tSWsGN4qB_Hjap5sZovJ6ffwxz4k8pCH_gla6tnfgUM" \
        -H "Content-Type: application/json" \
        -H "User-Agent: Mozilla/4.0 (compatible; MSIE 5.0; Windows NT; DigExt)" \
        -d '{
          "model":"glm-4",
          "stream": "true",
          "messages": [
              {
                  "role": "user",
                  "content": "1+1"
              }
          ]
        }' \
  https://open.bigmodel.cn/api/paas/v4/chat/completions