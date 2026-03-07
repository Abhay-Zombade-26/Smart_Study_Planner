# Download PNG icons for the application
Write-Host "?? Downloading PNG icons..." -ForegroundColor Cyan

# Create icons directory if it doesn't exist
New-Item -ItemType Directory -Force -Path src/main/resources/icons | Out-Null

# List of icons to download (using Bootstrap Icons CDN)
$icons = @(
    @{name="goal"; url="https://raw.githubusercontent.com/twbs/icons/main/icons/target.svg"},
    @{name="dashboard"; url="https://raw.githubusercontent.com/twbs/icons/main/icons/speedometer2.svg"},
    @{name="study"; url="https://raw.githubusercontent.com/twbs/icons/main/icons/book.svg"},
    @{name="github"; url="https://raw.githubusercontent.com/twbs/icons/main/icons/github.svg"},
    @{name="profile"; url="https://raw.githubusercontent.com/twbs/icons/main/icons/person-circle.svg"},
    @{name="logout"; url="https://raw.githubusercontent.com/twbs/icons/main/icons/box-arrow-right.svg"},
    @{name="refresh"; url="https://raw.githubusercontent.com/twbs/icons/main/icons/arrow-repeat.svg"},
    @{name="plan"; url="https://raw.githubusercontent.com/twbs/icons/main/icons/rocket.svg"},
    @{name="folder"; url="https://raw.githubusercontent.com/twbs/icons/main/icons/folder.svg"},
    @{name="check"; url="https://raw.githubusercontent.com/twbs/icons/main/icons/check-circle.svg"},
    @{name="error"; url="https://raw.githubusercontent.com/twbs/icons/main/icons/exclamation-circle.svg"},
    @{name="loading"; url="https://raw.githubusercontent.com/twbs/icons/main/icons/hourglass.svg"}
)

$successCount = 0
$failCount = 0

foreach ($icon in $icons) {
    $outputPath = "src/main/resources/icons/$($icon.name).svg"
    Write-Host "  Downloading $($icon.name).svg..." -NoNewline
    
    try {
        # Download the SVG directly
        Invoke-WebRequest -Uri $icon.url -OutFile $outputPath -ErrorAction Stop
        Write-Host " ?" -ForegroundColor Green
        $successCount++
    } catch {
        Write-Host " ?" -ForegroundColor Red
        Write-Host "    Error: $($_.Exception.Message)" -ForegroundColor Red
        $failCount++
    }
}

Write-Host "`n?? Download complete: $successCount successful, $failCount failed" -ForegroundColor Cyan

if ($failCount -eq 0) {
    Write-Host "? All icons downloaded successfully!" -ForegroundColor Green
} else {
    Write-Host "?? Some icons failed to download. You may need to download them manually." -ForegroundColor Yellow
}
