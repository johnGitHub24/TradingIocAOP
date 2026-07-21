# 一鍵建置 + 測試三模組（單元 + 整合）。
# 用法：.\scripts\check.ps1

. "$PSScriptRoot\env.ps1"

Push-Location "$PSScriptRoot\.."
try {
    .\gradlew.bat checkAll --console=plain --no-daemon
}
finally {
    Pop-Location
}
