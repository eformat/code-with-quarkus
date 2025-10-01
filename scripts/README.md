# test using cursor

Enable quarkus devui mcp

```bash
cat ~/.quarkus/dev-mcp.properties
enabled=true
```

Start quarkus dev mode

```bash
mvn quarkus:dev
```

Check sse

```bash
curl -X POST -H "Content-Type: application/json" \
  -d '{"jsonrpc": "2.0", "id": 1, "method": "tools/list"}' \
  http://localhost:8080/q/dev-mcp | jq .
```

Add cursor

```bash
cat ~/.cursor/mcp.json
{
  "mcpServers": {
    "local-quarkus": {
      "transport": "http",
      "url": "http://localhost:8080/q/dev-mcp",
      "headers": {
        "Content-Type": "application/json"
        }
    }
  }
}
```

Prompts

```bash
Q: test out the todo endpoint /renarde
A: Want me to automate this flow as a small shell script or wire it via the MCP workspace tool so you can trigger it from Cursor?
Q: yes give me a bash script i can run
```

Test it myself

```bash
./test_renarde_todos.sh "hello mike"
[1/4] Checking http://localhost:8080/renarde ...
OK: /renarde looks good
[2/4] Getting CSRF token from http://localhost:8080/Todos/todos ...
OK: CSRF token acquired
[3/4] Posting new todo: hello mike ...
OK: POST submitted
[4/4] Verifying the todo appears on the page ...
SUCCESS: Found todo on page -> Hello Mike
Done.
```
