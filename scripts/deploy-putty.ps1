# ============================================================
# SLE Service Deploy Script - Using PuTTY
# Upload pre-built JAR files to remote server and restart service
# ============================================================

[CmdletBinding()]
param(
    [string]$Service = "",
    [switch]$Upload,
    [switch]$Restart,
    [switch]$All,
    [switch]$Test,
    [switch]$Help
)

# Server Configuration
$SERVER_HOST = "192.168.2.36"
$SERVER_USER = "root"
$SERVER_PASSWORD = "Renzhong@123456"
$SERVER_PORT = 22

# Remote Path Configuration
$REMOTE_BASE_PATH = "/usr/local/project/sle"
$PROJECT_ROOT = Split-Path -Parent $PSScriptRoot

# PuTTY Tools Path
$PUTTY_PATH = "C:\Program Files\PuTTY"

# Service List
$SERVICES = @(
    "sle-as-gateway",
    "sle-as-mdm",
    "sle-as-mes",
    "sle-as-tpm",
    "sle-as-qms",
    "sle-as-spm",
    "sle-as-ies",
    "sle-as-nms",
    "sle-as-wms",
    "sle-as-uaa"
)

# Color Output
function Write-ColorOutput {
    param([string]$Message, [string]$Color = "White")
    $colors = @{
        "Red" = [ConsoleColor]::Red
        "Green" = [ConsoleColor]::Green
        "Yellow" = [ConsoleColor]::Yellow
        "Blue" = [ConsoleColor]::Blue
        "White" = [ConsoleColor]::White
        "Cyan" = [ConsoleColor]::Cyan
    }
    Write-Host $Message -ForegroundColor $colors[$Color]
}

# Show Help
function Show-Help {
    Write-Host ""
    Write-Host "SLE Service Deploy Script (PuTTY Version)" -ForegroundColor Cyan
    Write-Host "=========================================" -ForegroundColor Cyan
    Write-Host ""
    Write-Host "Usage: .\deploy-putty.ps1 [options]"
    Write-Host ""
    Write-Host "Options:"
    Write-Host "  -Service <name>    Specify service to deploy"
    Write-Host "  -Upload            Upload JAR files only"
    Write-Host "  -Restart           Restart service only"
    Write-Host "  -All               Upload + Restart (default)"
    Write-Host "  -Test              Test server connection"
    Write-Host "  -Help              Show this help"
    Write-Host ""
    Write-Host "Available services:"
    foreach ($svc in $SERVICES) {
        Write-Host "  - $svc"
    }
    Write-Host ""
    Write-Host "Examples:"
    Write-Host "  .\deploy-putty.ps1 -Service sle-as-mdm -All"
    Write-Host "  .\deploy-putty.ps1 -Service sle-as-mes -Upload"
    Write-Host "  .\deploy-putty.ps1 -Test"
    Write-Host ""
}

# Check Dependencies
function Check-Dependencies {
    Write-ColorOutput "[INFO] Checking PuTTY tools..." "Blue"

    $plinkPath = Join-Path $PUTTY_PATH "plink.exe"
    $pscpPath = Join-Path $PUTTY_PATH "pscp.exe"

    if (-not (Test-Path $plinkPath)) {
        Write-ColorOutput "[ERROR] plink.exe not found in $PUTTY_PATH" "Red"
        Write-Host "Please install PuTTY: https://www.chiark.greenend.org.uk/~sgtatham/putty/latest.html" -ForegroundColor Yellow
        return $false
    }

    if (-not (Test-Path $pscpPath)) {
        Write-ColorOutput "[ERROR] pscp.exe not found in $PUTTY_PATH" "Red"
        return $false
    }

    # Add PuTTY to PATH
    $env:PATH = $env:PATH + ";" + $PUTTY_PATH

    Write-ColorOutput "[OK] plink.exe found" "Green"
    Write-ColorOutput "[OK] pscp.exe found" "Green"

    return $true
}

# Accept Host Key first
function Accept-HostKey {
    Write-ColorOutput "[INFO] Accepting server host key..." "Blue"

    $plink = Join-Path $PUTTY_PATH "plink.exe"

    # Use echo y to automatically accept the host key
    echo y | & $plink -P $SERVER_PORT $SERVER_USER@$SERVER_HOST -pw $SERVER_PASSWORD "echo Host key accepted" 2>&1 | Out-Null

    Write-ColorOutput "[OK] Host key accepted" "Green"
}

# Run Plink Command
function Invoke-Plink {
    param([string]$Command)

    $plink = Join-Path $PUTTY_PATH "plink.exe"

    & $plink -batch -P $SERVER_PORT $SERVER_USER@$SERVER_HOST -pw $SERVER_PASSWORD $Command 2>&1
}

# Test Connection
function Test-ServerConnection {
    Write-ColorOutput "[INFO] Testing server connection..." "Blue"
    Write-Host "  Server: $SERVER_HOST`:$SERVER_PORT"
    Write-Host "  User: $SERVER_USER"
    Write-Host ""

    $result = Invoke-Plink -Command "hostname && echo 'Connection OK'"

    if ($LASTEXITCODE -eq 0) {
        Write-ColorOutput "[SUCCESS] Server connection OK" "Green"
        return $true
    } else {
        Write-ColorOutput "[ERROR] Cannot connect to server" "Red"
        Write-Host $result -ForegroundColor Yellow
        return $false
    }
}

# Format datetime for display
function Get-FormattedDateTime {
    param([DateTime]$DateTime)
    return $DateTime.ToString("yyyy-MM-dd HH:mm:ss")
}

# Find and List JAR Files
function Find-JarFiles {
    param([string]$ServiceName)

    $targetPath = Join-Path $PROJECT_ROOT "$ServiceName\target"

    Write-ColorOutput "[INFO] Looking for JAR files in: $targetPath" "Blue"

    if (-not (Test-Path $targetPath)) {
        Write-ColorOutput "[ERROR] Target directory not found: $targetPath" "Red"
        return $null
    }

    # Find main JAR and .original JAR
    $jarFiles = @()

    # Find main JAR files (sle-as-*.jar but not .original, sources, javadoc)
    $mainJar = Get-ChildItem -Path $targetPath -Filter "sle-as-*.jar" | Where-Object {
        $_.Name -notmatch "(sources|javadoc|\.original)"
    }

    $origJar = Get-ChildItem -Path $targetPath -Filter "sle-as-*.jar.original"

    if ($mainJar) {
        if ($mainJar -is [Array]) {
            $jarFiles += $mainJar
        } else {
            $jarFiles += @($mainJar)
        }
    }
    if ($origJar) {
        if ($origJar -is [Array]) {
            $jarFiles += $origJar
        } else {
            $jarFiles += @($origJar)
        }
    }

    if ($jarFiles.Count -eq 0) {
        # Fallback: find any JAR with sle-as in name
        $jarFiles = @(Get-ChildItem -Path $targetPath -Filter "*sle*.jar")
    }

    if ($jarFiles.Count -eq 0) {
        Write-ColorOutput "[ERROR] No JAR files found for service: $ServiceName" "Red"
        Write-Host "Please ensure the JAR files exist in: $targetPath" -ForegroundColor Yellow
        return $null
    }

    # Display found files with creation time
    Write-ColorOutput "[INFO] Found JAR files:" "Blue"
    $currentTime = Get-FormattedDateTime -DateTime (Get-Date)
    Write-Host "  Scan time: $currentTime" -ForegroundColor Gray
    Write-Host ""

    foreach ($jar in $jarFiles) {
        $size = "{0:N2} MB" -f ($jar.Length / 1MB)
        $createTime = Get-FormattedDateTime -DateTime $jar.CreationTime
        Write-Host "  - $($jar.Name)" -ForegroundColor White
        Write-Host "    Size: $size | Created: $createTime" -ForegroundColor Gray
    }

    return $jarFiles
}

# Upload Files using PSCP
function Upload-JarFiles {
    param([string]$ServiceName)

    $jarFiles = Find-JarFiles -ServiceName $ServiceName
    if ($null -eq $jarFiles) {
        return $false
    }

    # Create remote directory
    Write-ColorOutput "[INFO] Creating remote directory..." "Blue"
    Invoke-Plink -Command "mkdir -p $REMOTE_BASE_PATH/$ServiceName/target" | Out-Null

    # Upload files
    Write-ColorOutput "[INFO] Uploading JAR files..." "Blue"

    $pscp = Join-Path $PUTTY_PATH "pscp.exe"
    $uploadFailed = $false
    $uploadStartTime = Get-Date

    foreach ($jar in $jarFiles) {
        Write-Host "  Uploading: $($jar.Name)..." -NoNewline

        $localSize = $jar.Length
        $remotePath = "$REMOTE_BASE_PATH/$ServiceName/target/$($jar.Name)"
        $fileUploadStart = Get-Date

        & $pscp -batch -P $SERVER_PORT -pw $SERVER_PASSWORD $jar.FullName "$SERVER_USER@$SERVER_HOST`:$remotePath" 2>&1

        $fileUploadEnd = Get-Date
        $uploadDuration = ($fileUploadEnd - $fileUploadStart).TotalSeconds

        if ($LASTEXITCODE -ne 0) {
            Write-ColorOutput "    [FAIL]" "Red"
            $uploadFailed = $true
            continue
        }

        # Verify file size on remote
        $remoteSize = Invoke-Plink -Command "stat -c %s $remotePath 2>/dev/null || stat -f %z $remotePath 2>/dev/null"
        $remoteSize = [long]$remoteSize.Trim()

        if ($remoteSize -eq $localSize) {
            $uploadTime = Get-FormattedDateTime -DateTime $fileUploadEnd
            Write-ColorOutput "    [OK] Uploaded at $uploadTime ($('{0:N2}' -f ($localSize / 1MB)) MB, ${uploadDuration}s)" "Green"
        } else {
            Write-ColorOutput "    [WARN] Size mismatch: local=$localSize remote=$remoteSize" "Yellow"
            $uploadFailed = $true
        }
    }

    if ($uploadFailed) {
        return $false
    }

    $totalDuration = ((Get-Date) - $uploadStartTime).TotalSeconds
    Write-Host ""
    Write-ColorOutput "[SUCCESS] All files uploaded (Total time: ${totalDuration}s)" "Green"
    return $true
}

# Verify Upload
function Verify-Upload {
    param([string]$ServiceName)

    Write-ColorOutput "[INFO] Verifying uploaded files:" "Blue"

    Invoke-Plink -Command "ls -lh $REMOTE_BASE_PATH/$ServiceName/target/"*sle*".jar 2>/dev/null || ls -lh $REMOTE_BASE_PATH/$ServiceName/target/*.jar"
}

# Restart Service
function Restart-Service {
    param([string]$ServiceName)

    $scriptName = "docker-rm-" + ($ServiceName -replace "sle-as-", "")

    Write-ColorOutput "[INFO] Executing restart script: $scriptName`.sh" "Blue"
    Write-Host ""

    Invoke-Plink -Command "cd $REMOTE_BASE_PATH && sh $scriptName`.sh"

    if ($LASTEXITCODE -eq 0) {
        Write-ColorOutput "[SUCCESS] Service restarted" "Green"
        return $true
    } else {
        Write-ColorOutput "[ERROR] Restart failed" "Red"
        return $false
    }
}

# Check Service Status
function Get-ServiceStatus {
    param([string]$ServiceName)

    Write-ColorOutput "[INFO] Service status:" "Blue"

    $plink = Join-Path $PUTTY_PATH "plink.exe"

    # Get docker container info
    $dockerCmd = "docker ps -a --filter name=$ServiceName --format 'table {{.Names}}\t{{.Image}}\t{{.Status}}\t{{.Ports}}'"

    $output = & $plink -batch -P $SERVER_PORT $SERVER_USER@$SERVER_HOST -pw $SERVER_PASSWORD $dockerCmd 2>&1

    Write-Host $output

    # Also show running containers count
    $countCmd = "docker ps --filter name=$ServiceName | wc -l"
    $count = & $plink -batch -P $SERVER_PORT $SERVER_USER@$SERVER_HOST -pw $SERVER_PASSWORD $countCmd 2>&1

    if ($count -match "^\s*1$") {
        Write-ColorOutput "  No running container found" "Yellow"
    } else {
        Write-ColorOutput "  Container is running" "Green"
    }
}

# Select Service
function Select-Service {
    Write-Host ""
    Write-ColorOutput "Available services:" "Cyan"

    for ($i = 0; $i -lt $SERVICES.Count; $i++) {
        Write-Host "  $($i + 1)). $($SERVICES[$i])"
    }
    Write-Host ""

    $choice = Read-Host "Enter service name or number"
    $serviceName = ""

    if ($choice -match "^\d+$") {
        $index = [int]$choice - 1
        if ($index -ge 0 -and $index -lt $SERVICES.Count) {
            $serviceName = $SERVICES[$index]
        }
    } else {
        $serviceName = $choice
    }

    if ([string]::IsNullOrEmpty($serviceName)) {
        Write-ColorOutput "[ERROR] Invalid selection" "Red"
        return $null
    }

    return $serviceName
}

# ========== MAIN ==========

if ($Help) {
    Show-Help
    exit 0
}

# Check Dependencies
if (-not (Check-Dependencies)) {
    exit 1
}

# Test Mode
if ($Test) {
    Accept-HostKey
    Test-ServerConnection
    exit 0
}

# Select Service
if ([string]::IsNullOrEmpty($Service)) {
    $Service = Select-Service
    if ([string]::IsNullOrEmpty($Service)) {
        exit 1
    }
}

Write-Host ""
Write-ColorOutput "========================================" "Cyan"
Write-ColorOutput "  SLE Service Deploy (PuTTY)" "Cyan"
Write-ColorOutput "========================================" "Cyan"
Write-Host ""
Write-Host "  Service:     $Service" -ForegroundColor White
Write-Host "  Server:      $SERVER_HOST" -ForegroundColor White
Write-Host "  Remote Path: $REMOTE_BASE_PATH/$Service/target/" -ForegroundColor White
Write-Host ""

# Default to All if no action specified
if (-not $Upload -and -not $Restart) {
    $All = $true
}

# Accept host key first
Accept-HostKey

# Upload
if ($All -or $Upload) {
    # Test connection first
    if (-not (Test-ServerConnection)) {
        Write-ColorOutput "[ERROR] Cannot connect to server" "Red"
        exit 1
    }

    if (-not (Upload-JarFiles -ServiceName $Service)) {
        Write-ColorOutput "[ERROR] Upload failed" "Red"
        exit 1
    }

    Verify-Upload -ServiceName $Service
    Write-Host ""
}

# Restart
if ($All -or $Restart) {
    if (-not (Restart-Service -ServiceName $Service)) {
        Write-ColorOutput "[ERROR] Restart failed" "Red"
        exit 1
    }
    Write-Host ""
}

# Status
Get-ServiceStatus -ServiceName $Service

Write-Host ""
Write-ColorOutput "========================================" "Green"
Write-ColorOutput "  Deploy Complete!" "Green"
Write-ColorOutput "========================================" "Green"
Write-Host ""

exit 0
