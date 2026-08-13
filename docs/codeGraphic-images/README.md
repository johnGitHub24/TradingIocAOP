# CodeGraphic image export

Source: `docs/codeGraphic.html`  
Tool: `@mermaid-js/mermaid-cli@11`（dark）  
Script: `..\EngineeringOS\eos-minimal\hooks\export-codeGraphic-images.ps1`

| File | Tab |
|------|-----|
| `01-miniioc.svg` / `.png` | mini-ioc |
| `02-aop.svg` / `.png` | 六大切面 |
| `03-order.svg` / `.png` | 下單流程 |
| `04-modules.svg` / `.png` | 模組 |

Re-run from project root:

```powershell
& "..\EngineeringOS\eos-minimal\hooks\export-codeGraphic-images.ps1" -ProjectRoot .
```
