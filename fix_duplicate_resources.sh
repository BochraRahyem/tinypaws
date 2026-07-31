#!/bin/bash
set -e

# Remove all webp duplicate files in mipmap folders
find app/src/main/res/mipmap-* -name "*.webp" -delete

# Remove duplicate custom_app_icon.jpg in drawable if custom_app_icon.png exists
if [ -f app/src/main/res/drawable/custom_app_icon.png ]; then
    rm -f app/src/main/res/drawable/custom_app_icon.jpg
fi

echo "Duplicates cleaned up successfully!"
