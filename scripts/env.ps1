# Set JDK 21 for this project.
# Usage (dot-source so env vars apply to current shell):
#   . .\scripts\env.ps1

$jdk21 = "C:\Program Files\Java\jdk-21"

if (-not (Test-Path $jdk21)) {
    Write-Warning "JDK 21 not found: $jdk21"
} else {
    $env:JAVA_HOME = $jdk21
    $env:PATH = "$env:JAVA_HOME\bin;$env:PATH"
    Write-Output "JAVA_HOME = $env:JAVA_HOME"
    java -version
}
