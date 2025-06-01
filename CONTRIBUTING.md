# Contributing

## Generating OpenAPI docs

```bash
go install github.com/swaggo/swag/v2/cmd/swag@latest
swag init -v3.1
```

## Updating the bible DB

```bash
cd web
fly ssh sftp shell
put data/bible.db /tmp/bible.db
fly ssh console
rm -rf /app/data/bible.db*
mv /tmp/bible.db /app/data/bible.db
```
