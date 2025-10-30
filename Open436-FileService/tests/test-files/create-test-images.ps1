# 创建测试图片文件
# 使用 .NET 的 System.Drawing 库生成真实的图片

Add-Type -AssemblyName System.Drawing

function Create-TestImage {
    param(
        [string]$FilePath,
        [int]$Width,
        [int]$Height,
        [string]$Text
    )
    
    $bitmap = New-Object System.Drawing.Bitmap($Width, $Height)
    $graphics = [System.Drawing.Graphics]::FromImage($bitmap)
    
    # 填充背景色
    $graphics.Clear([System.Drawing.Color]::LightBlue)
    
    # 绘制文本
    $font = New-Object System.Drawing.Font("Arial", 20)
    $brush = New-Object System.Drawing.SolidBrush([System.Drawing.Color]::DarkBlue)
    $graphics.DrawString($Text, $font, $brush, 10, 10)
    
    # 保存图片
    $extension = [System.IO.Path]::GetExtension($FilePath).ToLower()
    if ($extension -eq ".jpg" -or $extension -eq ".jpeg") {
        $bitmap.Save($FilePath, [System.Drawing.Imaging.ImageFormat]::Jpeg)
    } elseif ($extension -eq ".png") {
        $bitmap.Save($FilePath, [System.Drawing.Imaging.ImageFormat]::Png)
    } else {
        $bitmap.Save($FilePath, [System.Drawing.Imaging.ImageFormat]::Bmp)
    }
    
    # 清理资源
    $graphics.Dispose()
    $bitmap.Dispose()
    $font.Dispose()
    $brush.Dispose()
    
    Write-Host "Created: $FilePath ($(Get-Item $FilePath | Select-Object -ExpandProperty Length) bytes)" -ForegroundColor Green
}

# 创建测试图片
$scriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path

# 1. 头像图片 (200x200)
Create-TestImage -FilePath "$scriptDir\avatar.jpg" -Width 200 -Height 200 -Text "Avatar"

# 2. 帖子图片 (800x600)
Create-TestImage -FilePath "$scriptDir\post-image.png" -Width 800 -Height 600 -Text "Post Image"

# 3. 图标 (64x64)
Create-TestImage -FilePath "$scriptDir\icon.png" -Width 64 -Height 64 -Text "Icon"

Write-Host ""
Write-Host "All test images created successfully!" -ForegroundColor Cyan

