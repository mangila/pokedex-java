Write-Output "apply redis and mongodb from localhost"
minikube kubectl -- apply -f k8s-external.yml

Write-Output "apply and upload everything to minikube"
$workDir = Get-Location
Write-Output "set working dir : $workDir"

$scriptPath = "$workDir\api\minikube.ps1"
Set-Location -Path (Split-Path -Path $scriptPath)
Write-Output "run ps: $scriptPath"
& $scriptPath

Set-Location -Path $workDir
Write-Output "set working dir : $workDir"

$scriptPath = "$workDir\backstage\minikube.ps1"
Set-Location -Path (Split-Path -Path $scriptPath)
Write-Output "run ps: $scriptPath"
& $scriptPath
