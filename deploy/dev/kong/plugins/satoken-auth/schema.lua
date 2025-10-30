return {
  name = "satoken-auth",
  fields = {
    { config = {
        type = "record",
        fields = {
          { auth_service_url = { type = "string", required = true } },
          { token_header_name = { type = "string", default = "Authorization" } },
        },
    }, },
  },
}


