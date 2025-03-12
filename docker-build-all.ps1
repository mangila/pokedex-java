Write-Output "build and create tarballs for all images"

$workDir = Get-Location
Write-Output "set working dir : $workDir"

$scriptPath = "$workDir\api\docker-build.ps1"
Set-Location -Path (Split-Path -Path $scriptPath)
Write-Output "run ps: $scriptPath"
& $scriptPath

Set-Location -Path $workDir
Write-Output "set working dir : $workDir"

$scriptPath = "$workDir\backstage\docker-build.ps1"
Set-Location -Path (Split-Path -Path $scriptPath)
Write-Output "run ps: $scriptPath"
& $scriptPath