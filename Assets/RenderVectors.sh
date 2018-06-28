#!/bin/zsh

zmodload zsh/mathfunc
# shopt -s nullglob

drawableFolder='../Fit/app/src/main/res/drawable'

#mkdir -p $drawableFolder-mdpi
#mkdir -p $drawableFolder-hdpi
#mkdir -p $drawableFolder-xhdpi
#mkdir -p $drawableFolder-xxhdpi

mdpi=72
((hdpi = int(mdpi * 1.5)))
((xhdpi = int(mdpi * 2)))
((xxhdpi = int(mdpi * 3)))

for f in Vectors/*.pdf
do
	filename=${f##*/}
	filename=${filename%.*}
	filename=${filename%@*}
	echo "Processing $f   $filename"

	/usr/local/bin/gs -q -dNOPAUSE -dBATCH -sDEVICE=pngalpha -r$mdpi -sOutputFile=$drawableFolder-mdpi/$filename.png $f
	/usr/local/bin/gs -q -dNOPAUSE -dBATCH -sDEVICE=pngalpha -r$hdpi -sOutputFile=$drawableFolder-hdpi/$filename.png $f
	/usr/local/bin/gs -q -dNOPAUSE -dBATCH -sDEVICE=pngalpha -r$xhdpi -sOutputFile=$drawableFolder-xhdpi/$filename.png $f
	/usr/local/bin/gs -q -dNOPAUSE -dBATCH -sDEVICE=pngalpha -r$xxhdpi -sOutputFile=$drawableFolder-xxhdpi/$filename.png $f
done
