#!/bin/bash
set -e

SRC="app/src/main/res/drawable/tinypaws_launcher_icon_1785455321724.jpg"

echo "Using source image: $SRC"

# 1. Create a clean PNG drawable version for high-res adaptive foreground
convert "$SRC" -resize 512x512 app/src/main/res/drawable/custom_app_icon.png
convert "$SRC" -resize 512x512 app/src/main/res/drawable/custom_app_icon.jpg

# Also create round mask version if needed
convert "$SRC" -resize 512x512 \
  \( +clone -threshold -1 -negate -fill white -draw "circle 256,256 256,0" \) \
  -alpha off -compose copy_opacity -composite \
  app/src/main/res/drawable/custom_app_icon_round.png

# 2. Sizes for mipmaps:
# mdpi: 48x48
# hdpi: 72x72
# xhdpi: 96x96
# xxhdpi: 144x144
# xxxhdpi: 192x192

declare -A DENSITIES
DENSITIES["mdpi"]=48
DENSITIES["hdpi"]=72
DENSITIES["xhdpi"]=96
DENSITIES["xxhdpi"]=144
DENSITIES["xxxhdpi"]=192

for DENSITY in "${!DENSITIES[@]}"; do
    SIZE=${DENSITIES[$DENSITY]}
    DIR="app/src/main/res/mipmap-$DENSITY"
    mkdir -p "$DIR"
    
    RADIUS=$((SIZE / 2))
    
    echo "Generating $DENSITY icons ($SIZEx$SIZE)..."
    
    # Square webp & png
    convert "$SRC" -resize "${SIZE}x${SIZE}" "$DIR/ic_launcher.webp"
    convert "$SRC" -resize "${SIZE}x${SIZE}" "$DIR/ic_launcher.png"
    
    # Round webp & png
    convert "$SRC" -resize "${SIZE}x${SIZE}" \
      \( +clone -threshold -1 -negate -fill white -draw "circle $RADIUS,$RADIUS $RADIUS,0" \) \
      -alpha off -compose copy_opacity -composite \
      "$DIR/ic_launcher_round.webp"
      
    convert "$SRC" -resize "${SIZE}x${SIZE}" \
      \( +clone -threshold -1 -negate -fill white -draw "circle $RADIUS,$RADIUS $RADIUS,0" \) \
      -alpha off -compose copy_opacity -composite \
      "$DIR/ic_launcher_round.png"
done

echo "All icon mipmaps and drawables generated successfully!"
